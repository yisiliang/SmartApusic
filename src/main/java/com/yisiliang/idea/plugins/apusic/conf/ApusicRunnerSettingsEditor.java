package com.yisiliang.idea.plugins.apusic.conf;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ApusicRunnerSettingsEditor extends SettingsEditor<ApusicRunConfiguration> {

    private final ApusicRunnerSettingsForm form;

    public ApusicRunnerSettingsEditor(Project project) {
        form = new ApusicRunnerSettingsForm(project);
    }

    @Override
    protected void resetEditorFrom(@NotNull ApusicRunConfiguration configuration) {
        form.resetFrom(configuration);
    }

    @Override
    protected void applyEditorTo(@NotNull ApusicRunConfiguration configuration) throws ConfigurationException {
        form.applyTo(configuration);
    }

    @Override
    protected @NotNull JComponent createEditor() {
        return form.getMainPanel();
    }

}
