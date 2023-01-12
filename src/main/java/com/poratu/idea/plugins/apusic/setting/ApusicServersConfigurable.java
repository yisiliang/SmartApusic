package com.poratu.idea.plugins.apusic.setting;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.MasterDetailsComponent;
import com.intellij.ui.CommonActionsPanel;
import com.intellij.util.IconUtil;
import com.poratu.idea.plugins.apusic.utils.PluginUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Author : zengkid
 * Date   : 2017-02-23
 * Time   : 00:14
 */
public class ApusicServersConfigurable extends MasterDetailsComponent {

    @Override
    public String getDisplayName() {
        return "Apusic Server";
    }

    @Override
    public String getHelpTopic() {
        return "Smart Apusic Help";
    }

    public ApusicServersConfigurable() {
        initTree();
    }

    @Override
    protected @Nullable List<AnAction> createActions(boolean fromPopup) {
        List<AnAction> actions = new ArrayList<>();
        actions.add(new AddApusicAction());
        // noinspection MissingRecentApi - the inspection of the next line is incorrect. It is available in 193+, actually
        actions.add(new MyDeleteAction());
        return actions;
    }

    @Override
    public boolean isModified() {
        boolean modified = super.isModified();
        if (modified) {
            return true;
        }

        int size = ApusicServerManagerState.getInstance().getApusicInfos().size();
        return myRoot.getChildCount() != size;
    }

    @Override
    public void reset() {
        myRoot.removeAllChildren();

        ApusicServerManagerState state = ApusicServerManagerState.getInstance();
        for (ApusicInfo info : state.getApusicInfos()) {
            addNode(info, false);
        }
        super.reset();
    }

    @Override
    public void apply() throws ConfigurationException {
        super.apply();

        List<ApusicInfo> apusicInfos = ApusicServerManagerState.getInstance().getApusicInfos();
        apusicInfos.clear();

        for (int i = 0; i < myRoot.getChildCount(); i++) {
            ApusicInfoConfigurable configurable = (ApusicInfoConfigurable) ((MyNode) myRoot.getChildAt(i)).getConfigurable();
            apusicInfos.add(configurable.getEditableObject());
        }
    }

    @Override
    protected boolean wasObjectStored(Object editableObject) {
        // noinspection SuspiciousMethodCalls
        return ApusicServerManagerState.getInstance().getApusicInfos().contains(editableObject);
    }

    private void addNode(ApusicInfo apusicInfo, boolean selectInTree) {
        ApusicInfoConfigurable configurable = new ApusicInfoConfigurable(apusicInfo, TREE_UPDATER, this::validateName);
        MyNode node = new MyNode(configurable);
        addNode(node, myRoot);

        if (selectInTree) {
            selectNodeInTree(node);
        }
    }

    private void validateName(String name) throws ConfigurationException {
        for (int i = 0; i < myRoot.getChildCount(); i++) {
            ApusicInfoConfigurable configurable = (ApusicInfoConfigurable) ((MyNode) myRoot.getChildAt(i)).getConfigurable();
            if (configurable.getEditableObject().getName().equals(name)) {
                throw new ConfigurationException("Duplicate name: \"" + name + "\"");
            }
        }
    }

    private class AddApusicAction extends DumbAwareAction {
        public AddApusicAction() {
            super("Add", "Add a Apusic server", IconUtil.getAddIcon());
            registerCustomShortcutSet(CommonActionsPanel.getCommonShortcut(CommonActionsPanel.Buttons.ADD), myTree);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            PluginUtils.chooseApusic(this::createUniqueName, apusicInfo -> addNode(apusicInfo, true));
        }

        private String createUniqueName(String preferredName) {
            List<String> existingNames = new ArrayList<>();

            for (int i = 0; i < myRoot.getChildCount(); i++) {
                String displayName = ((MyNode) myRoot.getChildAt(i)).getDisplayName();
                existingNames.add(displayName);
            }

            return PluginUtils.generateSequentName(existingNames, preferredName);
        }
    }

}
