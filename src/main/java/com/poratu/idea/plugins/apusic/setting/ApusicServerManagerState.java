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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.UnaryOperator;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * Author : zengkid
 * Date   : 2017-03-05
 * Time   : 15:20
 */

@State(name = "ServerConfiguration", storages = @Storage("smart.apusic.xml"))
public class ApusicServerManagerState implements PersistentStateComponent<ApusicServerManagerState> {

    @XCollection(elementTypes = ApusicInfo.class)
    private final List<ApusicInfo> apusicInfos = new ArrayList<>();

    public static ApusicServerManagerState getInstance() {
        return ApplicationManager.getApplication().getService(ApusicServerManagerState.class);
    }

    @NotNull
    public List<ApusicInfo> getTomcatInfos() {
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

    public static Optional<ApusicInfo> createTomcatInfo(String apusicHome) {
        return createTomcatInfo(apusicHome, ApusicServerManagerState::generateTomcatName);
    }

    public static Optional<ApusicInfo> createTomcatInfo(String apusicHome, UnaryOperator<String> nameGenerator) {
        File jarFile = Paths.get(apusicHome, "lib/catalina.jar").toFile();
        if (!jarFile.exists()) {
            Messages.showErrorDialog("Can not find catalina.jar in " + apusicHome, "Error");
            return Optional.empty();
        }

        final ApusicInfo apusicInfo = new ApusicInfo();
        apusicInfo.setPath(apusicHome);

        try (JarFile jar = new JarFile(jarFile)) {
            ZipEntry entry = jar.getEntry("org/apache/catalina/util/ServerInfo.properties");
            Properties p = new Properties();
            try (InputStream is = jar.getInputStream(entry)) {
                p.load(is);
            }
            String serverInfo = p.getProperty("server.info");
            String serverNumber = p.getProperty("server.number");
            String name = nameGenerator == null ? generateTomcatName(serverInfo) : nameGenerator.apply(serverInfo);
            apusicInfo.setName(name);
            apusicInfo.setVersion(serverNumber);
        } catch (IOException e) {
            Messages.showErrorDialog("Can not read server version in " + apusicHome, "Error");
            return Optional.empty();
        }

        return Optional.of(apusicInfo);
    }

    private static String generateTomcatName(String name) {
        List<ApusicInfo> existingServers = getInstance().getTomcatInfos();
        List<String> existingNames = existingServers.stream()
                .map(ApusicInfo::getName)
                .collect(Collectors.toList());

        return PluginUtils.generateSequentName(existingNames, name);
    }

}
