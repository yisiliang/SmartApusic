import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.testFramework.IdeaTestUtil;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;


public class ClassPathTest {
    @Test
    public void testClassPath() {
        File file = new File("/tmp");
        System.out.println(file.toPath().resolve("classes/*").toFile());
    }

    @Test
    public void name() {
        try {
            File file = new File("/tmp");
            Path workingPath = file.toPath();
            workingPath.toFile().mkdirs();


            // Copy the Apusic configuration files to the working directory


            JavaParameters javaParams = new JavaParameters();
            javaParams.setWorkingDirectory(workingPath.toFile());
            File jdkFile = new File("/Library/Java/JavaVirtualMachines/jdk-17.0.3.1.jdk/Contents/Home");
            IdeaTestUtil.createMockJdk("jdk17", jdkFile.getPath());

            javaParams.setJdk(IdeaTestUtil.getMockJdk11());

            javaParams.getClassPath().add(workingPath.resolve("classes").toFile());
            javaParams.getClassPath().add(workingPath.resolve("common/*").toFile());
            javaParams.getClassPath().add(workingPath.resolve("lib/*").toFile());
            javaParams.getClassPath().add(workingPath.resolve("lib/ext/*").toFile());
            javaParams.setMainClass("com.apusic.server.Main");


            ParametersList vmParams = javaParams.getVMParametersList();
            vmParams.addParametersString("-Xms1024m");
            vmParams.addProperty("com.apusic.domain.home", "domain");

            javaParams.getProgramParametersList().add("-root " + "rootDir");
            System.out.println(javaParams.toCommandLine());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
