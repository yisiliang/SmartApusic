package com.yisiliang.idea.plugins.apusic.runner;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.application.ApplicationConfigurationType;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.yisiliang.idea.plugins.apusic.conf.ApusicRunConfiguration;
import com.yisiliang.idea.plugins.apusic.conf.ApusicRunConfigurationType;
import com.yisiliang.idea.plugins.apusic.setting.ApusicInfo;
import com.yisiliang.idea.plugins.apusic.setting.ApusicServerManagerState;
import com.yisiliang.idea.plugins.apusic.utils.PluginUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ApusicRunConfigurationProducer extends LazyRunConfigurationProducer<ApusicRunConfiguration> {
    private static final Logger LOG = Logger.getInstance(ApusicRunConfigurationProducer.class);

    @NotNull
    @Override
    public ConfigurationFactory getConfigurationFactory() {
        return ConfigurationTypeUtil.findConfigurationType(ApusicRunConfigurationType.class);
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull ApusicRunConfiguration configuration, @NotNull ConfigurationContext context, @NotNull Ref<PsiElement> sourceElement) {
        Module module = context.getModule();
        if (module == null) {
            return false;
        }

        // Skip if it contains a main class, to avoid conflict with the default Application run configuration
        PsiClass psiClass = ApplicationConfigurationType.getMainClass(context.getPsiLocation());
        if (psiClass != null) {
            return false;
        }

        List<VirtualFile> webRoots = PluginUtils.findWebRoots(context.getLocation());
        if (webRoots.isEmpty()) {
            return false;
        }

        List<ApusicInfo> apusicInfos = ApusicServerManagerState.getInstance().getApusicInfos();
        if (!apusicInfos.isEmpty()) {
            LOG.info("auto select apusic server");
            configuration.setApusicInfo(apusicInfos.get(0));
        }
        String contextPath = PluginUtils.extractContextPath(module);
        LOG.info("configuration = " + configuration + " hash = " + configuration.hashCode());
        LOG.info("docBase = " + webRoots.get(0).getPath());
        LOG.info("contextPath = /" + contextPath);
        configuration.setDocBase(webRoots.get(0).getPath());
        configuration.setContextPath("/" + contextPath);
        configuration.setName("Apusic: " + contextPath);
        return true;
    }

    @Override
    public boolean isPreferredConfiguration(ConfigurationFromContext self, ConfigurationFromContext other) {
        return false;
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull ApusicRunConfiguration configuration, @NotNull ConfigurationContext context) {
        List<VirtualFile> webRoots = PluginUtils.findWebRoots(context.getLocation());
        return webRoots.stream().anyMatch(webRoot -> webRoot.getPath().equals(configuration.getDocBase()));
    }

}
