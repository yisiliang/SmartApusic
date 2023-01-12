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
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PathsList;
import com.poratu.idea.plugins.apusic.utils.PluginUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Author : zengkid
 * Date   : 2017-02-17
 * Time   : 11:10 AM
 */

public class ApusicCommandLineState extends JavaCommandLineState {

    private static final String JDK_JAVA_OPTIONS = "JDK_JAVA_OPTIONS";
    private static final String ENV_JDK_JAVA_OPTIONS = "--add-opens=java.base/java.lang=ALL-UNNAMED " +
            "--add-opens=java.base/java.io=ALL-UNNAMED " +
            "--add-opens=java.base/java.util=ALL-UNNAMED " +
            "--add-opens=java.base/java.util.concurrent=ALL-UNNAMED " +
            "--add-opens=java.rmi/sun.rmi.transport=ALL-UNNAMED";

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
            Module module = configuration.getModule();
            if (workingPath == null || module == null) {
                throw new ExecutionException("The Module Root specified is not a module according to Intellij");
            }
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
            javaParams.getClassPath().add(apusicInstallationPath.resolve("common/*").toFile());
            javaParams.getClassPath().add(apusicInstallationPath.resolve("lib/*").toFile());
            javaParams.getClassPath().add(apusicInstallationPath.resolve("lib/ext/*").toFile());
            javaParams.setMainClass(APUSIC_MAIN_CLASS);

            javaParams.setPassParentEnvs(configuration.isPassParentEnvs());
            if (envOptions != null) {
                javaParams.setEnv(envOptions);
            }

            ParametersList vmParams = javaParams.getVMParametersList();
            vmParams.addParametersString(vmOptions);
            vmParams.addProperty(PARAM_DOMAIN_HOME, configuration.getDomain());

            javaParams.getProgramParametersList().add("-root " + configuration.getApusicInfo().getPath());
            return javaParams;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Nullable
    @Override
    protected ConsoleView createConsole(@NotNull Executor executor) {
        return new ServerConsoleView(configuration);
    }


}
