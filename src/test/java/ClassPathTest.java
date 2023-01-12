import com.intellij.execution.CommandLineUtil;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.io.FileFilters;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtilRt;
import com.intellij.testFramework.IdeaTestUtil;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ClassPathTest {
    @Test
    public void testClassPath() {
        File file = new File("/tmp");
        System.out.println(file.toPath().resolve("classes/*").toFile());
    }

    @Test
    public void name() {
        try {
            Sdk jdk7 = IdeaTestUtil.getMockJdk17();
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

    @Test
    public void listFile() {
        List<File> filesByMask = FileUtil.findFilesByMask(Pattern.compile("*.jar"), new File("/Library/Java/JavaVirtualMachines/jdk1.8.0_331.jdk/Contents/Home/jre/lib"));
        System.out.println(filesByMask);
    }



    //
//
//    private static void addToWindowsCommandLine(String command, List<String> parameters, List<? super String> commandLine) {
//        boolean isCmdParam = isWinShell(command);
//        int cmdInvocationDepth = isWinShellScript(command) ? 2 : isCmdParam ? 1 : 0;
//
//        CommandLineUtil.QuoteFlag quoteFlag = new CommandLineUtil.QuoteFlag(false);
//        for (int i = 0; i < parameters.size(); i++) {
//            String parameter = parameters.get(i);
//
//            parameter = StringUtilRt.unquoteString(parameter, INESCAPABLE_QUOTE);
//            boolean inescapableQuoting = !parameter.equals(parameters.get(i));
//
//            if (parameter.isEmpty()) {
//                commandLine.add(QQ);
//                continue;
//            }
//
//            if (isCmdParam && parameter.startsWith("/") && parameter.length() == 2) {
//                commandLine.add(parameter);
//                continue;
//            }
//
//            String parameterPrefix = "";
//            if (isCmdParam) {
//                Matcher m = WIN_QUIET_COMMAND.matcher(parameter);
//                if (m.matches()) {
//                    parameterPrefix = m.group(1);  // @...
//                    parameter = m.group(2);
//                }
//
//                if (parameter.equalsIgnoreCase("echo")) {
//                    // no further quoting, only ^-escape and wrap the whole "echo ..." into double quotes
//                    String parametersJoin = String.join(" ", parameters.subList(i, parameters.size()));
//                    quoteFlag.toggle();
//                    parameter = escapeParameter(parametersJoin, quoteFlag, cmdInvocationDepth, false);
//                    commandLine.add(parameter);  // prefix is already included
//                    break;
//                }
//
//                if (!parameter.equalsIgnoreCase("call")) {
//                    isCmdParam = isWinShell(parameter);
//                    if (isCmdParam || isWinShellScript(parameter)) {
//                        cmdInvocationDepth++;
//                    }
//                }
//            }
//
//            if (cmdInvocationDepth > 0 && !isCmdParam || inescapableQuoting) {
//                parameter = escapeParameter(parameter, quoteFlag, cmdInvocationDepth, !inescapableQuoting);
//            }
//            else {
//                parameter = backslashEscapeQuotes(parameter);
//            }
//
//            commandLine.add(parameterPrefix.isEmpty() ? parameter : parameterPrefix + parameter);
//        }
//    }
}
