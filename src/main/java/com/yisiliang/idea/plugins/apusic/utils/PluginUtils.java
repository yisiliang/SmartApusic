package com.yisiliang.idea.plugins.apusic.utils;

import com.intellij.execution.Location;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleFileIndex;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.io.FileFilters;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import com.yisiliang.idea.plugins.apusic.conf.ApusicRunConfiguration;
import com.yisiliang.idea.plugins.apusic.setting.ApusicInfo;
import com.yisiliang.idea.plugins.apusic.setting.ApusicServerManagerState;
import com.yisiliang.idea.plugins.apusic.setting.ApusicServersConfigurable;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Author : zengkid
 * Date   : 2017-03-06
 * Time   : 21:35
 */
public final class PluginUtils {
    private static final int MIN_PORT_VALUE = 0;
    private static final int MAX_PORT_VALUE = 65535;

    private PluginUtils() {
    }

    /**
     * Generate a sequent name based on the existing names
     *
     * @param existingNames existing names, e.g. ["apusic 7", "apusic 8", "apusic 9"]
     * @param preferredName preferred name, e.g. "apusic 8"
     * @return sequent name, e.g. "apusic 8 (2)"
     */
    public static String generateSequentName(List<String> existingNames, String preferredName) {
        int maxSequent = 0;
        for (String existingName : existingNames) {
            Pattern pattern = Pattern.compile("^" + StringUtil.escapeToRegexp(preferredName) + "(?:\\s\\((\\d+)\\))?$");
            Matcher matcher = pattern.matcher(existingName);
            if (matcher.matches()) {
                String seq = matcher.group(1);
                if (seq == null) {
                    // No sequent implies that the sequent is 1
                    maxSequent = 1;
                } else {
                    maxSequent = Math.max(maxSequent, Integer.parseInt(seq));
                }
            }
        }

        return maxSequent == 0 ? preferredName : preferredName + " (" + (maxSequent + 1) + ")";
    }

    public static void chooseApusic(Consumer<ApusicInfo> callback) {
        chooseApusic(null, callback);
    }

    public static void chooseApusic(UnaryOperator<String> nameGenerator, Consumer<ApusicInfo> callback) {
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory
                .createSingleFolderDescriptor()
                .withTitle("Select Apusic Server")
                .withDescription("Select the directory of the Apusic Server");

        FileChooser.chooseFile(descriptor, null, null, file -> ApusicServerManagerState
                .createApusicInfo(file.getPath(), nameGenerator)
                .ifPresent(callback));
    }

    public static Path getWorkingPath(ApusicRunConfiguration configuration) {
        String userHome = System.getProperty("user.home");
        Project project = configuration.getProject();
        return Paths.get(userHome, ".SmartApusic", project.getName());
    }

    public static Path getApusicLogsDirPath(ApusicRunConfiguration configuration) {
        Path workingDir = getWorkingPath(configuration);
        if (workingDir != null) {
            return workingDir.resolve("logs");
        }
        return null;
    }


    public static void openApusicConfiguration() {
        ShowSettingsUtil.getInstance().showSettingsDialog(null, ApusicServersConfigurable.class);
    }


    public static String extractContextPath(Module module) {
        return extractContextPath(module.getName());
    }

    public static String extractContextPath(Project project) {
        return extractContextPath(project.getName());
    }

    public static String extractContextPath(String name) {
        String s = StringUtil.trimEnd(name, ".main");
        String lastElement = ArrayUtil.getLastElement(s.split("\\."));
        return StringUtil.replace(lastElement, "_", "-");
    }

    public static String getDefaultDomain(String apusicPath) {
        File file = new File(apusicPath, "domains");
        file = new File(file, "mydomain");
        return file.getAbsolutePath();
    }

    public static void closeSafe(Closeable... closeables) {
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    //ignore
                }
            }
        }
    }

    public static List<VirtualFile> findWebRoots(Module module) {
        List<VirtualFile> webRoots = new ArrayList<>();
        if (module == null) {
            return webRoots;
        }

        ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
        ModuleFileIndex fileIndex = moduleRootManager.getFileIndex();
        VirtualFile[] sourceRoots = moduleRootManager.getSourceRoots(false);
        List<VirtualFile> parentRoots = Stream.of(sourceRoots)
                .map(VirtualFile::getParent)
                .distinct()
                .collect(Collectors.toList());

        for (VirtualFile parentRoot : parentRoots) {
            fileIndex.iterateContentUnderDirectory(parentRoot, file -> {
                Path path = Paths.get(file.getPath(), "WEB-INF");
                if (Files.exists(path)) {
                    webRoots.add(file);
                }
                return true;
            }, file -> {
                if (file.isDirectory()) {
                    String path = file.getPath();
                    return webRoots.stream().noneMatch(root -> file.getPath().startsWith(root.getPath())) && !path.contains("node_modules");
                }
                return false;
            });
        }

        return webRoots;
    }

    public static List<VirtualFile> findWebRoots(Project project) {
        Module[] modules = ModuleManager.getInstance(project).getModules();
        List<VirtualFile> webRoots = new ArrayList<>();

        for (Module module : modules) {
            webRoots.addAll(findWebRoots(module));
        }

        return webRoots;
    }

    public static boolean isUnderTestSources(@Nullable Location<?> location) {
        if (location == null) {
            return false;
        }

        VirtualFile file = location.getVirtualFile();
        if (file == null) {
            return false;
        }

        return ProjectFileIndex.getInstance(location.getProject()).isInTestSourceContent(file);
    }

    public static List<File> listJars(File dir) {
        File[] ret = dir.listFiles(FileFilters.filesWithExtension("jar"));
        List<File> fileList = new ArrayList<>();
        if (ret == null) {
            return fileList;
        }
        fileList.addAll(Arrays.asList(ret));
        return fileList;
    }

    public static List<VirtualFile> findWebRoots(@Nullable Location<?> location) {
        if (location == null) {
            return ContainerUtil.emptyList();
        }

        boolean isTestFile = isUnderTestSources(location);
        if (isTestFile) {
            return ContainerUtil.emptyList();
        }

        return findWebRoots(location.getModule());
    }
}
