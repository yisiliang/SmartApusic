package com.poratu.idea.plugins.apusic.setting;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.NamedConfigurable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ApusicInfoConfigurable extends NamedConfigurable<ApusicInfo> {
    private final ApusicInfo apusicInfo;
    private final ApusicInfoComponent apusicInfoView;
    private String displayName;
    private final ApusicNameValidator<String> nameValidator;

    public ApusicInfoConfigurable(ApusicInfo apusicInfo, Runnable treeUpdater, ApusicNameValidator<String> nameValidator) {
        super(true, treeUpdater);
        this.apusicInfo = apusicInfo;
        this.apusicInfoView = new ApusicInfoComponent(apusicInfo);
        this.displayName = apusicInfo.getName();
        this.nameValidator = nameValidator;
    }

    @Override
    public void setDisplayName(String name) {
        this.displayName = name;
    }

    @Override
    public ApusicInfo getEditableObject() {
        return apusicInfo;
    }

    @Override
    public String getBannerSlogan() {
        return null;
    }

    @Override
    public JComponent createOptionsPanel() {
        return apusicInfoView.getMainPanel();
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    protected void checkName(@NonNls @NotNull String name) throws ConfigurationException {
        super.checkName(name);
        if (name.equals(apusicInfo.getName())) {
            return;
        }
        nameValidator.validate(name);
    }

    @Override
    public boolean isModified() {
        return !displayName.equals(apusicInfo.getName());
    }

    @Override
    public void apply() {
        apusicInfo.setName(displayName);
    }
}

