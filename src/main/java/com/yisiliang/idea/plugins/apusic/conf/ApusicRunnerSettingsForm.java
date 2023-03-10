package com.yisiliang.idea.plugins.apusic.conf;

import com.intellij.execution.configuration.EnvironmentVariablesTextFieldWithBrowseButton;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.UIBundle;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.ui.FormBuilder;
import com.yisiliang.idea.plugins.apusic.setting.ApusicInfo;
import com.yisiliang.idea.plugins.apusic.setting.ApusicServerManagerState;
import com.yisiliang.idea.plugins.apusic.utils.PluginConstant;
import com.yisiliang.idea.plugins.apusic.utils.PluginUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ApusicRunnerSettingsForm implements Disposable {

    private final Project project;
    private final JPanel apusicField = new JPanel(new BorderLayout());
    private final JTextField domainField = new JTextField();
    private final JTextField contextPathField = new JTextField();
    private final JCheckBox addLibsAndClassesCheckBox = new JCheckBox();
    private final TextFieldWithBrowseButton docBaseField = new TextFieldWithBrowseButton();

    private final ApusicComboBox apusicComboBox = new ApusicComboBox(domainField);
    private final RawCommandLineEditor vmOptions = new RawCommandLineEditor();
    private final RawCommandLineEditor externalClasspathEditor = new RawCommandLineEditor();
    private final EnvironmentVariablesTextFieldWithBrowseButton envOptions = new EnvironmentVariablesTextFieldWithBrowseButton();
    private JPanel mainPanel;

    ApusicRunnerSettingsForm(Project project) {
        this.project = project;
        JButton configurationButton = new JButton("Configure...");
        configurationButton.addActionListener(e -> PluginUtils.openApusicConfiguration());

        apusicField.add(apusicComboBox, BorderLayout.CENTER);
        apusicField.add(configurationButton, BorderLayout.EAST);

        buildForm();
    }


    private void buildForm() {
        FormBuilder builder = FormBuilder.createFormBuilder()
                .addLabeledComponent(PluginUtils.getI18NValue(PluginConstant.APUSIC_SERVER_KEY), apusicField)
                .addLabeledComponent(PluginUtils.getI18NValue(PluginConstant.DOMAIN_KEY), domainField)
                .addLabeledComponent(PluginUtils.getI18NValue(PluginConstant.DEPLOYMENT_DIRECTORY_KEY), docBaseField)
                .addLabeledComponent(PluginUtils.getI18NValue(PluginConstant.CONTEXT_PATH_KEY), contextPathField)
                .addLabeledComponent(PluginUtils.getI18NValue(PluginConstant.ADD_LIBRARIES_AND_CLASSES_KEY), addLibsAndClassesCheckBox)
                .addLabeledComponent(PluginUtils.getI18NValue(PluginConstant.EXTERNAL_CLASSPATH_KEY), externalClasspathEditor)
                .addLabeledComponent(PluginUtils.getI18NValue(PluginConstant.VM_OPTIONS_KEY), vmOptions)
                .addLabeledComponent(PluginUtils.getI18NValue(PluginConstant.ENV_OPTIONS_KEY), envOptions)
                .addComponentFillVertically(new JPanel(), 0);

        initFormComponents();
        mainPanel = builder.getPanel();
    }

    private void initFormComponents() {
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor();
        VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
        if (projectDir != null && projectDir.isDirectory()) {
            descriptor.setRoots(projectDir);
        }
        docBaseField.addBrowseFolderListener(PluginUtils.getI18NValue(PluginConstant.DOC_BASE_BROWSE_TITLE_KEY),
                PluginUtils.getI18NValue(PluginConstant.DOC_BASE_BROWSE_DESC_KEY),
                project, descriptor);
        addLibsAndClassesCheckBox.setToolTipText(PluginUtils.getI18NValue(PluginConstant.ADD_CHECKBOX_TOOL_TIP_TEXT_KEY));
    }


    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void resetFrom(ApusicRunConfiguration configuration) {
        apusicComboBox.setSelectedItem(configuration.getApusicInfo());
        domainField.setText(configuration.getDomain());
        vmOptions.setText(configuration.getVmOptions());
        if (configuration.getEnvOptions() != null) {
            envOptions.setEnvs(configuration.getEnvOptions());
        }
        contextPathField.setText(configuration.getContextPath());
        docBaseField.setText(configuration.getDocBase());
        envOptions.setPassParentEnvs(configuration.isPassParentEnvs());
        addLibsAndClassesCheckBox.setSelected(configuration.isAddLibsAndClasses());
        externalClasspathEditor.setText(configuration.getExternalClasspath());
    }

    public void applyTo(ApusicRunConfiguration configuration) throws ConfigurationException {
        try {
            ApusicInfo selectedApusicInfo = (ApusicInfo) apusicComboBox.getSelectedItem();
            configuration.setApusicInfo(selectedApusicInfo);
            configuration.setDomain(domainField.getText());
            configuration.setDocBase(docBaseField.getText());
            configuration.setContextPath(contextPathField.getText());
            configuration.setAddLibsAndClasses(addLibsAndClassesCheckBox.isSelected());
            configuration.setVmOptions(vmOptions.getText());
            configuration.setEnvOptions(envOptions.getEnvs());
            configuration.setPassParentEnvironmentVariables(envOptions.isPassParentEnvs());
            configuration.setExternalClasspath(externalClasspathEditor.getText());
        } catch (Exception e) {
            throw new ConfigurationException(e.getMessage());
        }
    }

    @Override
    public void dispose() {
        mainPanel = null;
    }

    private static class ApusicComboBox extends JComboBox<ApusicInfo> {
        private JTextField domainField;

        ApusicComboBox() {
            super();

            List<ApusicInfo> apusicInfos = ApusicServerManagerState.getInstance().getApusicInfos();
            ComboBoxModel<ApusicInfo> model = new CollectionComboBoxModel<>(apusicInfos);
            setModel(model);

            initBrowsableEditor();
        }

        ApusicComboBox(JTextField domainField) {
            super();

            List<ApusicInfo> apusicInfos = ApusicServerManagerState.getInstance().getApusicInfos();
            ComboBoxModel<ApusicInfo> model = new CollectionComboBoxModel<>(apusicInfos);
            setModel(model);

            initBrowsableEditor();

            this.domainField = domainField;
        }

        private void initBrowsableEditor() {
            ComboBoxEditor editor = new ApusicComboBoxEditor(this);
            setEditor(editor);
            setEditable(true);
        }

        @Override
        protected void fireItemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                ApusicInfo selectedApusicInfo = (ApusicInfo) this.getSelectedItem();
                if (selectedApusicInfo != null && StringUtil.isEmpty(domainField.getText())) {
                    domainField.setText(PluginUtils.getDefaultDomain(selectedApusicInfo.getPath()));
                }
                super.fireItemStateChanged(e);
            }
        }
    }

    private static class ApusicComboBoxEditor extends BasicComboBoxEditor {
        private static final ApusicComboBoxTextComponentAccessor TEXT_COMPONENT_ACCESSOR = new ApusicComboBoxTextComponentAccessor();
        private final ApusicComboBox comboBox;
        private boolean fileDialogOpened;

        public ApusicComboBoxEditor(ApusicComboBox comboBox) {
            this.comboBox = comboBox;
        }

        @Override
        protected JTextField createEditorComponent() {
            ExtendableTextField editor = new ExtendableTextField();
            editor.addExtension(createBrowseExtension());
            editor.setBorder(null);
            editor.setEditable(false);
            editor.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1 && !fileDialogOpened) {
                        if (comboBox.isPopupVisible()) {
                            comboBox.hidePopup();
                        } else {
                            comboBox.showPopup();
                        }
                    }
                }
            });
            return editor;
        }

        private ExtendableTextComponent.Extension createBrowseExtension() {
            String tooltip = UIBundle.message("component.with.browse.button.browse.button.tooltip.text");
            Runnable browseRunnable = () -> {
                fileDialogOpened = true;
                PluginUtils.chooseApusic(apusicInfo -> TEXT_COMPONENT_ACCESSOR.setText(comboBox, apusicInfo.getPath()));
                SwingUtilities.invokeLater(() -> fileDialogOpened = false);
            };
            return ExtendableTextComponent.Extension.create(AllIcons.General.OpenDisk, AllIcons.General.OpenDiskHover,
                    tooltip, browseRunnable);
        }
    }

    private static class ApusicComboBoxTextComponentAccessor implements TextComponentAccessor<JComboBox<ApusicInfo>> {

        @Override
        public String getText(JComboBox<ApusicInfo> component) {
            return component.getEditor().getItem().toString();
        }

        @Override
        public void setText(JComboBox<ApusicInfo> comboBox, @NotNull String text) {
            ApusicServerManagerState.createApusicInfo(text).ifPresent(apusicInfo -> {
                CollectionComboBoxModel<ApusicInfo> model = (CollectionComboBoxModel<ApusicInfo>) comboBox.getModel();
                model.add(apusicInfo);
                comboBox.setSelectedItem(apusicInfo);
            });
        }

    }

}

