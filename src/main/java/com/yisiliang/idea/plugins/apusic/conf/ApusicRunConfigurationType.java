package com.yisiliang.idea.plugins.apusic.conf;

import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.SimpleConfigurationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.NotNullLazyValue;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Author : zengkid
 * Date   : 2/16/2017
 * Time   : 3:11 PM
 */
public class ApusicRunConfigurationType extends SimpleConfigurationType {

    private static final Icon APUSIC_ICON = IconLoader.getIcon("/icon/apusic.svg", ApusicRunConfigurationType.class);

    protected ApusicRunConfigurationType() {
        super("com.yisiliang.idea.plugins.apusic",
                "Smart Apusic",
                "Configuration to run Apusic server",
                NotNullLazyValue.createValue(() -> APUSIC_ICON));
    }

    @Override
    public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new ApusicRunConfiguration(project, this, "");
    }

}
