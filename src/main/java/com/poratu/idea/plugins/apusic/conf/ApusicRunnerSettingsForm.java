package com.poratu.idea.plugins.apusic.conf;

import com.intellij.execution.configuration.EnvironmentVariablesTextFieldWithBrowseButton;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.UIBundle;
import com.intellij.ui.components.fields.ExtendableTextComponent;
import com.intellij.ui.components.fields.ExtendableTextField;
import com.intellij.util.ui.FormBuilder;
import com.poratu.idea.plugins.apusic.setting.ApusicInfo;
import com.poratu.idea.plugins.apusic.setting.ApusicServerManagerState;
import com.poratu.idea.plugins.apusic.utils.PluginUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ApusicRunnerSettingsForm implements Disposable {

    private final Project project;
    private JPanel mainPanel;
    private final JPanel apusicField = new JPanel(new BorderLayout());
    private final ApusicComboBox apusicComboBox = new ApusicComboBox();
    private final TextFieldWithBrowseButton docBaseField = new TextFieldWithBrowseButton();
    private final JTextField contextPathField = new JTextField();
    private final JTextField portField = new JTextField();
    private final JTextField adminPort = new JTextField();
    private final RawCommandLineEditor vmOptions = new RawCommandLineEditor();
    private final EnvironmentVariablesTextFieldWithBrowseButton envOptions = new EnvironmentVariablesTextFieldWithBrowseButton();

    ApusicRunnerSettingsForm(Project project) {
        this.project = project;
        JButton configurationButton = new JButton("Configure...");
        configurationButton.addActionListener(e -> PluginUtils.openApusicConfiguration());

        apusicField.add(apusicComboBox, BorderLayout.CENTER);
        apusicField.add(configurationButton, BorderLayout.EAST);

        initDeploymentDirectory();
        buildForm();
    }

    private void initDeploymentDirectory() {
        FileChooserDescriptor descriptor = new IgnoreOutputFileChooserDescriptor(project);
        docBaseField.addBrowseFolderListener("Select Deployment Directory", "Please the directory to deploy",
                project, descriptor);
    }

    private void buildForm() {
        FormBuilder builder = FormBuilder.createFormBuilder()
                .addLabeledComponent("Apusic server:", apusicField)
                .addLabeledComponent("Deployment directory:", docBaseField)
                .addLabeledComponent("Context path:", contextPathField)
                .addLabeledComponent("Server port:", portField)
                .addLabeledComponent("Admin port:", adminPort)
                .addLabeledComponent("VM options:", vmOptions)
                .addLabeledComponent("Env options:", envOptions)
                .addComponentFillVertically(new JPanel(), 0);

        mainPanel = builder.getPanel();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void resetFrom(ApusicRunConfiguration configuration) {
        apusicComboBox.setSelectedItem(configuration.getApusicInfo());
        docBaseField.setText(configuration.getDocBase());
        contextPathField.setText(configuration.getContextPath());
        portField.setText(String.valueOf(configuration.getPort()));
        adminPort.setText(String.valueOf(configuration.getAdminPort()));
        vmOptions.setText(configuration.getVmOptions());
        if (configuration.getEnvOptions() != null) {
            envOptions.setEnvs(configuration.getEnvOptions());
        }
        envOptions.setPassParentEnvs(configuration.isPassParentEnvs());
    }

    public void applyTo(ApusicRunConfiguration configuration) throws ConfigurationException {
        try {
            configuration.setApusicInfo((ApusicInfo) apusicComboBox.getSelectedItem());
            configuration.setDocBase(docBaseField.getText());
            configuration.setContextPath(contextPathField.getText());
            configuration.setPort(PluginUtils.parsePort(portField.getText()));
            configuration.setAdminPort(PluginUtils.parsePort(adminPort.getText()));
            configuration.setVmOptions(vmOptions.getText());
            configuration.setEnvOptions(envOptions.getEnvs());
            configuration.setPassParentEnvironmentVariables(envOptions.isPassParentEnvs());
        } catch (Exception e) {
            throw new ConfigurationException(e.getMessage());
        }
    }

    @Override
    public void dispose() {
        mainPanel = null;
    }

    private static class ApusicComboBox extends JComboBox<ApusicInfo> {

        ApusicComboBox() {
            super();

            List<ApusicInfo> apusicInfos = ApusicServerManagerState.getInstance().getApusicInfos();
            ComboBoxModel<ApusicInfo> model = new CollectionComboBoxModel<>(apusicInfos);
            setModel(model);

            initBrowsableEditor();
        }

        private void initBrowsableEditor() {
            ComboBoxEditor editor = new ApusicComboBoxEditor(this);
            setEditor(editor);
            setEditable(true);
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

    private static class IgnoreOutputFileChooserDescriptor extends FileChooserDescriptor {
        private static final FileChooserDescriptor singleFolderDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        private final Project project;

        public IgnoreOutputFileChooserDescriptor(Project project) {
            super(singleFolderDescriptor);
            this.project = project;
        }

        @Override
        public boolean isFileVisible(VirtualFile file, boolean showHiddenFiles) {
            Module[] modules = ModuleManager.getInstance(project).getModules();

            for (Module module : modules) {
                VirtualFile[] excludeRoots = ModuleRootManager.getInstance(module).getExcludeRoots();
                for (VirtualFile excludeFile : excludeRoots) {
                    if (excludeFile.equals(file)) {
                        return false;
                    }
                }
            }

            return super.isFileVisible(file, showHiddenFiles);
        }
    }

}

