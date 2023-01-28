package com.yisiliang.idea.plugins.apusic.conf;

import com.intellij.configurationStore.XmlSerializer;
import com.intellij.diagnostic.logging.LogConfigurationPanel;
import com.intellij.execution.ExecutionBundle;
import com.intellij.execution.Executor;
import com.intellij.execution.JavaRunConfigurationExtensionManager;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.yisiliang.idea.plugins.apusic.setting.ApusicInfo;
import com.yisiliang.idea.plugins.apusic.setting.ApusicServerManagerState;
import com.yisiliang.idea.plugins.apusic.utils.PluginUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Author : zengkid
 * Date   : 2/16/2017
 * Time   : 3:14 PM
 */
public class ApusicRunConfiguration extends LocatableConfigurationBase<LocatableRunConfigurationOptions> implements RunProfileWithCompileBeforeLaunchOption {

    private ApusicRunConfigurationOptions apusicOptions = new ApusicRunConfigurationOptions();

    protected ApusicRunConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory, String name) {
        super(project, factory, name);
        ApusicServerManagerState applicationService = ApplicationManager.getApplication().getService(ApusicServerManagerState.class);
        List<ApusicInfo> apusicInfos = applicationService.getApusicInfos();
        if (!apusicInfos.isEmpty()) {
            apusicOptions.setApusicInfo(apusicInfos.get(0));
            apusicOptions.setDomain(PluginUtils.getDefaultDomain(apusicOptions.getApusicInfo().getPath()));
        }

        List<VirtualFile> webRoots = PluginUtils.findWebRoots(project);
        if (!webRoots.isEmpty()) {
            String contextPath = PluginUtils.extractContextPath(project);
            apusicOptions.setDocBase(webRoots.get(0).getPath());
            apusicOptions.setContextPath("/" + contextPath);
        }
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        Project project = getProject();
        SettingsEditorGroup<ApusicRunConfiguration> group = new SettingsEditorGroup<>();
        ApusicRunnerSettingsEditor apusicSetting = new ApusicRunnerSettingsEditor(project);

        group.addEditor(ExecutionBundle.message("run.configuration.configuration.tab.title"), apusicSetting);
        group.addEditor(ExecutionBundle.message("logs.tab.title"), new LogConfigurationPanel<>());
        JavaRunConfigurationExtensionManager.getInstance().appendEditors(this, group);
        return group;
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        if (apusicOptions.getApusicInfo() == null) {
            throw new RuntimeConfigurationError("Apusic server is not selected");
        }
        if (StringUtil.isEmpty(apusicOptions.getDomain())) {
            throw new RuntimeConfigurationError("Apusic domain is empty");
        }
    }

    @Override
    public void onNewConfigurationCreated() {
        super.onNewConfigurationCreated();

    }

    @Override
    public Module @NotNull [] getModules() {
        ModuleManager moduleManager = ModuleManager.getInstance(getProject());
        return moduleManager.getModules();
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) {
        return new ApusicCommandLineState(executionEnvironment, this);
    }

    @Override
    public void readExternal(@NotNull Element element) throws InvalidDataException {
        super.readExternal(element);
        XmlSerializer.deserializeInto(element, apusicOptions);
    }

    @Override
    public void writeExternal(@NotNull Element element) throws WriteExternalException {
        super.writeExternal(element);
        XmlSerializer.serializeObjectInto(apusicOptions, element);
    }


    public ApusicInfo getApusicInfo() {
        return apusicOptions.getApusicInfo();
    }

    public void setApusicInfo(ApusicInfo apusicInfo) {
        apusicOptions.setApusicInfo(apusicInfo);
    }

    public String getDomain() {
        return apusicOptions.getDomain();
    }

    public void setDomain(String domain) {
        apusicOptions.setDomain(domain);
    }

    public String getVmOptions() {
        return apusicOptions.getVmOptions();
    }

    public void setVmOptions(String vmOptions) {
        apusicOptions.setVmOptions(vmOptions);
    }

    public boolean isAddLibsAndClasses() {
        return apusicOptions.isAddLibsAndClasses();
    }

    public void setAddLibsAndClasses(boolean addLibsAndClasses) {
        apusicOptions.setAddLibsAndClasses(addLibsAndClasses);
    }


    public String getDocBase() {
        return apusicOptions.getDocBase();
    }

    public void setDocBase(String docBase) {
        apusicOptions.setDocBase(docBase);
    }

    public String getContextPath() {
        return apusicOptions.getContextPath();
    }

    public void setContextPath(String contextPath) {
        apusicOptions.setContextPath(contextPath);
    }

    public Map<String, String> getEnvOptions() {
        return apusicOptions.getEnvOptions();
    }

    public void setEnvOptions(Map<String, String> envOptions) {
        apusicOptions.setEnvOptions(envOptions);
    }

    public Boolean isPassParentEnvs() {
        return apusicOptions.isPassParentEnvs();
    }

    public void setPassParentEnvironmentVariables(Boolean passParentEnvs) {
        apusicOptions.setPassParentEnvs(passParentEnvs);
    }

    @Override
    public RunConfiguration clone() {
        ApusicRunConfiguration configuration = (ApusicRunConfiguration) super.clone();
        configuration.apusicOptions = XmlSerializerUtil.createCopy(apusicOptions);
        return configuration;
    }

    private static class ApusicRunConfigurationOptions implements Serializable {
        private ApusicInfo apusicInfo;
        private String domain;
        private String vmOptions = "-server -Xms1024m -Xmx1024m";
        private Map<String, String> envOptions;
        private Boolean passParentEnvs = true;
        private String contextPath;
        private String docBase;
        private boolean addLibsAndClasses;

        public boolean isAddLibsAndClasses() {
            return addLibsAndClasses;
        }

        public void setAddLibsAndClasses(boolean addLibsAndClasses) {
            this.addLibsAndClasses = addLibsAndClasses;
        }

        public String getDocBase() {
            return docBase;
        }

        public void setDocBase(String docBase) {
            this.docBase = docBase;
        }

        public String getContextPath() {
            return contextPath;
        }

        public void setContextPath(String contextPath) {
            this.contextPath = contextPath;
        }

        public ApusicInfo getApusicInfo() {
            return apusicInfo;
        }

        public void setApusicInfo(ApusicInfo apusicInfo) {
            this.apusicInfo = apusicInfo;
        }


        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getVmOptions() {
            return vmOptions;
        }

        public void setVmOptions(String vmOptions) {
            this.vmOptions = vmOptions;
        }

        public Map<String, String> getEnvOptions() {
            return envOptions;
        }

        public void setEnvOptions(Map<String, String> envOptions) {
            this.envOptions = envOptions;
        }

        public Boolean isPassParentEnvs() {
            return passParentEnvs;
        }

        public void setPassParentEnvs(Boolean passParentEnvs) {
            this.passParentEnvs = passParentEnvs;
        }
    }

}
