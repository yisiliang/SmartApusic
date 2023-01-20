package com.yisiliang.idea.plugins.apusic.conf;

import com.intellij.execution.CantRunException;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.JavaSdk;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.containers.JBIterable;
import com.yisiliang.idea.plugins.apusic.utils.PluginUtils;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ApusicCommandLineStateTest {

    @Test
    public void testFilename() {
        File file = new File("/Users/YiSiliang/.sdkman/candidates/java/current/bin/java");
        System.out.println(file.getName());
        System.out.println(file.getPath());


    }

    @Test
    public void testPath() {

        JBIterable.from(StringUtil.tokenize("c:\\Software\\apache-tomcat-8.5.85\\lib\\*", ";"))
                .filter(element -> {
            element = element.trim();
            return !element.isEmpty();
        }).forEach(System.out::println);
        StringUtil.tokenize("c:\\Software\\apache-tomcat-8.5.85\\lib\\*", ";").forEach(System.out::println);
    }

    @Test
    public void test() throws CantRunException {
        String userHome = System.getProperty("user.home");
        Project project = Mockito.mock(Project.class);
        Sdk sdk = Mockito.mock(Sdk.class);
        JavaSdk javaSdk = Mockito.mock(JavaSdk.class);
        Mockito.when(javaSdk.getVMExecutablePath(Mockito.any())).thenReturn("/Users/YiSiliang/.sdkman/candidates/java/current/bin/java");
        Mockito.when(sdk.getSdkType()).thenReturn(javaSdk);
        Mockito.when(project.getName()).thenReturn("demo-project-name");
        PropertiesComponent mockPropertiesComponent = Mockito.mock(PropertiesComponent.class);
        Mockito.when(mockPropertiesComponent.getBoolean(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(true);
        Mockito.when(project.getService(Mockito.any())).thenReturn(mockPropertiesComponent);

        Path apusicInstallationPath = Paths.get("/Users/YiSiliang/Software/apache-tomcat-8.5.85/");
        Path workingPath = Paths.get(userHome, ".SmartApusic", project.getName());
        JavaParameters javaParams = new JavaParameters();
        javaParams.setWorkingDirectory(workingPath.toFile());
        javaParams.setJdk(sdk);


        javaParams.getClassPath().add(apusicInstallationPath.resolve("classes").toFile());

        javaParams.getClassPath().addAllFiles(PluginUtils.listJars(apusicInstallationPath.resolve("common").toFile()));
        javaParams.getClassPath().add("c:\\Software\\apache-tomcat-8.5.85\\lib\\*");
        project.getService(PropertiesComponent.class).setValue(ExecutionUtil.PROPERTY_DYNAMIC_CLASSPATH, true);
//        javaParams.getClassPath().addAllFiles(PluginUtils.listJars(apusicInstallationPath.resolve("lib").toFile()));
//        javaParams.getClassPath().addAllFiles(PluginUtils.listJars(apusicInstallationPath.resolve("lib" + File.separator + "ext").toFile()));
        javaParams.setMainClass("APUSIC_MAIN_CLASS");


        ParametersList vmParams = javaParams.getVMParametersList();
        vmParams.addParametersString("-Dspring.profiles.active=dev");
        vmParams.addProperty("PARAM_DOMAIN_HOME", "domain");
        vmParams.addProperty("ENV_DOMAIN_HOME", "domain");
        vmParams.addProperty("ENV_APUSIC_HOME", "apusic_home");
        javaParams.getProgramParametersList().add("-root");
        javaParams.getProgramParametersList().add(apusicInstallationPath.toString());

        System.out.println(javaParams.toCommandLine().getCommandLineString());

    }
}
