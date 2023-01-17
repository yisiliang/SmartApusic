package com.yisiliang.idea.plugins.apusic.setting;

import com.intellij.openapi.Disposable;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;

public class ApusicInfoComponent implements Disposable {

    private JPanel mainPanel;

    public ApusicInfoComponent(ApusicInfo apusicInfo) {
        JBLabel versionLabel = new JBLabel(apusicInfo.getVersion());
        JBLabel locationLabel = new JBLabel(apusicInfo.getPath());
        mainPanel = FormBuilder.createFormBuilder()
                .setVerticalGap(UIUtil.LARGE_VGAP)
                .addLabeledComponent("Version:", versionLabel)
                .addLabeledComponent("Location:", locationLabel)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
        mainPanel.setBorder(JBUI.Borders.empty(0, 10));
    }

    public JComponent getMainPanel() {
        return mainPanel;
    }

    @Override
    public void dispose() {
        mainPanel = null;
    }

}
