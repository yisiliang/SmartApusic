package com.poratu.idea.plugins.apusic.setting;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.XCollection;
import com.poratu.idea.plugins.apusic.utils.PluginUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * Author : zengkid
 * Date   : 2017-03-05
 * Time   : 15:20
 */

@State(name = "ApusicServerConfiguration", storages = @Storage("smart.apusic.xml"))
public class ApusicServerManagerState implements PersistentStateComponent<ApusicServerManagerState> {

    @XCollection(elementTypes = ApusicInfo.class)
    private final List<ApusicInfo> apusicInfos = new ArrayList<>();

    public static ApusicServerManagerState getInstance() {
        return ApplicationManager.getApplication().getService(ApusicServerManagerState.class);
    }

    @NotNull
    public List<ApusicInfo> getApusicInfos() {
        return apusicInfos;
    }

    @Nullable
    @Override
    public ApusicServerManagerState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ApusicServerManagerState apusicSettingsState) {
        XmlSerializerUtil.copyBean(apusicSettingsState, this);
    }

    public static Optional<ApusicInfo> createApusicInfo(String apusicHome) {
        return createApusicInfo(apusicHome, ApusicServerManagerState::generateApusicName);
    }

    public static Optional<ApusicInfo> createApusicInfo(String apusicHome, UnaryOperator<String> nameGenerator) {
        File jarFile = Paths.get(apusicHome, "lib/apusic.jar").toFile();
        if (!jarFile.exists()) {
            Messages.showErrorDialog("Can not find lib/apusic.jar in " + apusicHome, "Error");
            return Optional.empty();
        }

        final ApusicInfo apusicInfo = new ApusicInfo();
        apusicInfo.setPath(apusicHome);

        try (JarFile jar = new JarFile(jarFile)) {
            ZipEntry entry = jar.getEntry("META-INF/MANIFEST.MF");
            String serverInfo = "Apusic";
            String serverNumber = "unknown";
            InputStream inputStream = jar.getInputStream(entry);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while (true) {
                String line = bufferedReader.readLine();
                if (line != null) {
                    if (line.startsWith("Specification-Version:")) {
                        serverNumber = line.substring("Specification-Version:".length() - 1).trim();
                        break;
                    }
                } else {
                    break;
                }
            }

            String name = nameGenerator == null ? generateApusicName(serverInfo) : nameGenerator.apply(serverInfo);
            apusicInfo.setName(name);
            apusicInfo.setVersion(serverNumber);
        } catch (IOException e) {
            Messages.showErrorDialog("Can not read server version in " + apusicHome, "Error");
            return Optional.empty();
        }

        return Optional.of(apusicInfo);
    }

    private static String generateApusicName(String name) {
        List<ApusicInfo> existingServers = getInstance().getApusicInfos();
        List<String> existingNames = existingServers.stream()
                .map(ApusicInfo::getName)
                .collect(Collectors.toList());

        return PluginUtils.generateSequentName(existingNames, name);
    }

}
