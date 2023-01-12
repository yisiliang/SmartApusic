package com.poratu.idea.plugins.apusic.conf;

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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileFilters;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.PathUtil;
import com.poratu.idea.plugins.apusic.utils.PluginUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Author : zengkid
 * Date   : 2017-02-17
 * Time   : 11:10 AM
 */

public class ApusicCommandLineState extends JavaCommandLineState {

    private static final String JDK_JAVA_OPTIONS = "JDK_JAVA_OPTIONS";
    private static final String ENV_JDK_JAVA_OPTIONS = "--add-opens=java.base/java.lang=ALL-UNNAMED " + "--add-opens=java.base/java.io=ALL-UNNAMED " + "--add-opens=java.base/java.util=ALL-UNNAMED " + "--add-opens=java.base/java.util.concurrent=ALL-UNNAMED " + "--add-opens=java.rmi/sun.rmi.transport=ALL-UNNAMED";

    private static final String APUSIC_MAIN_CLASS = "com.apusic.server.Main";
    private static final String PARAM_DOMAIN_HOME = "com.apusic.domain.home";
    private static final String PARAM_APUSIC_HOME = "root";
    private static final String PARAM_CATALINA_HOME = "com.apusic.domain.home";
    private static final String PARAM_CATALINA_BASE = "catalina.base";
    private static final String PARAM_CATALINA_TMPDIR = "java.io.tmpdir";
    private static final String PARAM_LOGGING_CONFIG = "java.util.logging.config.file";
    private static final String PARAM_LOGGING_MANAGER = "java.util.logging.manager";
    private static final String PARAM_LOGGING_MANAGER_VALUE = "org.apache.juli.ClassLoaderLogManager";
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
            Path workingPath = PluginUtils.getWorkingPath(configuration);
            workingPath.toFile().mkdirs();

            Path apusicInstallationPath = Paths.get(configuration.getApusicInfo().getPath());
            Project project = configuration.getProject();
            String vmOptions = configuration.getVmOptions();
            Map<String, String> envOptions = configuration.getEnvOptions();

            // Copy the Apusic configuration files to the working directory

            ProjectRootManager manager = ProjectRootManager.getInstance(project);

            JavaParameters javaParams = new JavaParameters();
            javaParams.setDefaultCharset(project);
            javaParams.setWorkingDirectory(workingPath.toFile());
            javaParams.setJdk(manager.getProjectSdk());


            javaParams.getClassPath().add(apusicInstallationPath.resolve("classes").toFile());

            javaParams.getClassPath().addAllFiles(listJars(apusicInstallationPath.resolve("common").toFile()));
            javaParams.getClassPath().addAllFiles(listJars(apusicInstallationPath.resolve("lib").toFile()));
            javaParams.getClassPath().addAllFiles(listJars(apusicInstallationPath.resolve("lib" + File.separator + "ext").toFile()));
            javaParams.setMainClass(APUSIC_MAIN_CLASS);

            javaParams.setPassParentEnvs(configuration.isPassParentEnvs());
            if (envOptions != null) {
                javaParams.setEnv(envOptions);
            }

            ParametersList vmParams = javaParams.getVMParametersList();
            vmParams.addParametersString(vmOptions);
            vmParams.addProperty(PARAM_DOMAIN_HOME, configuration.getDomain());
            javaParams.getProgramParametersList().add("-root");
            javaParams.getProgramParametersList().add(configuration.getApusicInfo().getPath());
            return javaParams;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private List<File> listJars(File dir) {
        File[] ret = dir.listFiles(FileFilters.filesWithExtension("jar"));
        List<File> fileList = new ArrayList<>();
        if (ret == null) {
            return fileList;
        }
        fileList.addAll(Arrays.asList(ret));
        return fileList;
    }

    @Nullable
    @Override
    protected ConsoleView createConsole(@NotNull Executor executor) {
        return new ServerConsoleView(configuration);
    }


}
