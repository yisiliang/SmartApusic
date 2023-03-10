package com.yisiliang.idea.plugins.apusic.conf;

import com.intellij.debugger.settings.DebuggerSettings;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.JavaCommandLineState;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.process.KillableProcessHandler;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PathsList;
import com.yisiliang.idea.plugins.apusic.utils.PluginUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Author : zengkid
 * Date   : 2017-02-17
 * Time   : 11:10 AM
 */

public class ApusicCommandLineState extends JavaCommandLineState {
    private static final Logger LOG = Logger.getInstance(ApusicCommandLineState.class);

    private static final String JDK_JAVA_OPTIONS = "JDK_JAVA_OPTIONS";
    private static final String ENV_JDK_JAVA_OPTIONS = "--add-opens=java.base/java.lang=ALL-UNNAMED " + "--add-opens=java.base/java.io=ALL-UNNAMED " + "--add-opens=java.base/java.util=ALL-UNNAMED " + "--add-opens=java.base/java.util.concurrent=ALL-UNNAMED " + "--add-opens=java.rmi/sun.rmi.transport=ALL-UNNAMED";

    private static final String APUSIC_MAIN_CLASS = "com.apusic.server.Main";
    private static final String PARAM_DOMAIN_HOME = "com.apusic.domain.home";
    private static final String ENV_DOMAIN_HOME = "DOMAIN_HOME";
    private static final String ENV_APUSIC_HOME = "APUSIC_HOME";
    private static final String SMART_APUSIC_EXTERNAL_LIBRARY = "smart-apusic-external-library";
    private static final String SMART_APUSIC_EXTERNAL_CLASS = "smart-apusic-external-class";
    private static final String SMART_APUSIC_BASE_FILE = "smart-apusic-base-file";
    private static final String SMART_APUSIC_CLASS_LOADER = "com.apusic.web.ServletClassLoaderDelegate";
    private static final String SMART_APUSIC_CLASS = "com.apusic.web.container.ExternalServletClassLoaderDelegate";
    private ApusicRunConfiguration configuration;

    protected ApusicCommandLineState(@NotNull ExecutionEnvironment environment) {
        super(environment);
    }

    protected ApusicCommandLineState(ExecutionEnvironment environment, ApusicRunConfiguration configuration) {
        this(environment);
        this.configuration = configuration;
    }

    @Override
    protected GeneralCommandLine createCommandLine() throws ExecutionException {
        GeneralCommandLine commandLine = super.createCommandLine();

        // Set JDK_JAVA_OPTIONS
        String originalJdkJavaOptions = commandLine.getEnvironment().get(JDK_JAVA_OPTIONS);
        String jdkJavaOptions = originalJdkJavaOptions == null ? ENV_JDK_JAVA_OPTIONS : originalJdkJavaOptions + " " + ENV_JDK_JAVA_OPTIONS;
        return commandLine.withEnvironment(JDK_JAVA_OPTIONS, jdkJavaOptions);
    }

    @Override
    @NotNull
    protected OSProcessHandler startProcess() throws ExecutionException {
        OSProcessHandler progressHandler = super.startProcess();
        if (progressHandler instanceof KillableProcessHandler) {
            boolean shouldKillSoftly = !DebuggerSettings.getInstance().KILL_PROCESS_IMMEDIATELY;
            ((KillableProcessHandler) progressHandler).setShouldKillProcessSoftly(shouldKillSoftly);
        }
        return progressHandler;
    }

    @Override
    protected JavaParameters createJavaParameters() {
        try {
            String domain = configuration.getDomain();
            String name = configuration.getName();
            String contextPath = configuration.getContextPath();
            String docBase = configuration.getDocBase();

            PluginUtils.changeDomainConfig(domain, name, contextPath, docBase);

            Path workingPath = PluginUtils.getWorkingPath(configuration);
            File workingPathFile = workingPath.toFile();
            workingPathFile.mkdirs();

            File apusicHome = new File(configuration.getApusicInfo().getPath());

            Path apusicInstallationPath = Paths.get(configuration.getApusicInfo().getPath());
            Project project = configuration.getProject();
            String vmOptions = configuration.getVmOptions();
            Map<String, String> envOptions = configuration.getEnvOptions();

            // Copy the Apusic configuration files to the working directory

            ProjectRootManager manager = ProjectRootManager.getInstance(project);

            JavaParameters javaParams = new JavaParameters();
            javaParams.setDefaultCharset(project);
            javaParams.setWorkingDirectory(workingPathFile);
            javaParams.setJdk(manager.getProjectSdk());

            ParametersList vmParams = javaParams.getVMParametersList();

            if (configuration.isAddLibsAndClasses()) {
                addLibsAndClasses(workingPathFile, project, javaParams, vmParams);
            }

            String externalClasspath = configuration.getExternalClasspath();
            if (StringUtil.isNotEmpty(externalClasspath)) {
                String[] extPaths = externalClasspath.split(",");
                for (String extPath : extPaths) {
                    if (StringUtil.isNotEmpty(extPath)) {
                        javaParams.getClassPath().add(new File(extPath));
                    }
                }
            }

            //1
            javaParams.getClassPath().add(apusicInstallationPath.resolve("classes").toFile());
            //2
            javaParams.getClassPath().addAllFiles(PluginUtils.listJars(apusicInstallationPath.resolve("sp").toFile()));
            javaParams.getClassPath().addAllFiles(PluginUtils.listZips(apusicInstallationPath.resolve("sp").toFile()));
            //3
            javaParams.getClassPath().addAllFiles(PluginUtils.listJars(apusicInstallationPath.resolve("common").toFile()));
            //4
            javaParams.getClassPath().addAllFiles(PluginUtils.listJars(apusicInstallationPath.resolve("lib").toFile()));
            javaParams.getClassPath().addAllFiles(PluginUtils.listZips(apusicInstallationPath.resolve("lib").toFile()));
            //5
            javaParams.getClassPath().addAllFiles(PluginUtils.listJars(apusicInstallationPath.resolve("lib" + File.separator + "ext").toFile()));
            javaParams.getClassPath().addAllFiles(PluginUtils.listZips(apusicInstallationPath.resolve("lib" + File.separator + "ext").toFile()));

            //6
            javaParams.getClassPath().addAllFiles(PluginUtils.listJars(new File(domain, "lib")));
            javaParams.getClassPath().addAllFiles(PluginUtils.listZips(new File(domain, "lib")));

            javaParams.setMainClass(APUSIC_MAIN_CLASS);

            javaParams.setPassParentEnvs(configuration.isPassParentEnvs());
            if (envOptions != null) {
                javaParams.setEnv(envOptions);
            }

            vmParams.addParametersString(vmOptions);
            vmParams.addProperty(PARAM_DOMAIN_HOME, configuration.getDomain());
            vmParams.addProperty(ENV_DOMAIN_HOME, configuration.getDomain());
            vmParams.addProperty(ENV_APUSIC_HOME, apusicHome.getAbsolutePath());

            File docBaseFile = new File(configuration.getDocBase());
            vmParams.addProperty(SMART_APUSIC_BASE_FILE, docBaseFile.getAbsolutePath());
            javaParams.getProgramParametersList().add("-root");
            javaParams.getProgramParametersList().add(apusicHome.getAbsolutePath());
            javaParams.setUseDynamicClasspath(project);

            return javaParams;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

    }

    private void addLibsAndClasses(File workingPathFile, Project project, JavaParameters javaParams, ParametersList vmParams) throws IOException {
        File jarsPathFile = new File(workingPathFile, "lib");
        File extPathFile = new File(workingPathFile, "ext");
        FileUtil.delete(jarsPathFile);
        FileUtil.delete(extPathFile);
        jarsPathFile.mkdirs();
        extPathFile.mkdirs();
        List<File> moduleJars = getModuleJars(configuration.getProject());
        if (!moduleJars.isEmpty()) {
            for (File moduleJar : moduleJars) {
                File targetFile = new File(jarsPathFile, moduleJar.getName());
                FileUtil.copy(moduleJar, targetFile);
            }
        }
        File extLoader = new File(extPathFile, "apusic-external-classloader.jar");

        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("lib/apusic-external-classloader.dat");
        if (resourceAsStream != null) {
            FileOutputStream fileOutputStream = new FileOutputStream(extLoader);
            FileUtil.copy(resourceAsStream, fileOutputStream);
            PluginUtils.closeSafe(resourceAsStream, fileOutputStream);
        } else {
            LOG.error("can't getResourceAsStream by lib/apusic-external-classloader.dat");
        }
        javaParams.getClassPath().add(extLoader);

        vmParams.addProperty(SMART_APUSIC_CLASS_LOADER, SMART_APUSIC_CLASS);
        vmParams.addProperty(SMART_APUSIC_EXTERNAL_LIBRARY, jarsPathFile.getAbsolutePath());
        vmParams.addProperty(SMART_APUSIC_EXTERNAL_CLASS, getModuleClasses(project));
    }

    private List<File> getModuleJars(Project project) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        List<File> fileList = new ArrayList<>();
        for (Module module : modules) {
            PathsList pathsList = OrderEnumerator.orderEntries(module)
                    .withoutSdk().runtimeOnly().productionOnly().getPathsList();
            if (pathsList.isEmpty()) {
                continue;
            }
            pathsList.getVirtualFiles().forEach(file -> {
                if (file.getCanonicalPath() != null && !file.isDirectory()) {
                    fileList.add(new File(file.getCanonicalPath()));
                }
            });
        }
        return fileList;
    }

    private String getModuleClasses(Project project) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        StringBuffer stringBuffer = new StringBuffer();
        for (Module module : modules) {
            PathsList pathsList = OrderEnumerator.orderEntries(module).productionOnly()
                    .classes().getPathsList();
            if (pathsList.isEmpty()) {
                continue;
            }
            pathsList.getVirtualFiles().forEach(file -> {
                if (file.getCanonicalPath() != null && file.isDirectory()) {
                    stringBuffer.append(file.getCanonicalPath()).append(File.pathSeparatorChar);
                }
            });
        }
        return stringBuffer.toString();
    }

    @Nullable
    @Override
    protected ConsoleView createConsole(@NotNull Executor executor) {
        return new ServerConsoleView(configuration);
    }


}
