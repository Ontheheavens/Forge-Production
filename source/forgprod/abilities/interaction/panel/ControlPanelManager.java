package forgprod.abilities.interaction.panel;

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate.DialogCallbacks;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;

import forgprod.abilities.interaction.panel.components.Sidebar;
import forgprod.abilities.interaction.panel.components.tabs.ModulesTab;
import forgprod.abilities.interaction.panel.components.tabs.modules.HullBlueprintSection;
import forgprod.abilities.interaction.panel.components.tabs.modules.ModuleList;
import forgprod.abilities.interaction.panel.listeners.ButtonListener;

/**
 * @author Ontheheavens
 * @since 30.12.2022
 */

public class ControlPanelManager {

    private static ControlPanelManager instance;
    private DialogCallbacks callbacks;
    private InteractionDialogAPI dialog;
    private CustomPanelAPI panel;
    private CustomUIPanelPlugin plugin;
    private boolean queuedPanelRedraw = false;
    private boolean queuedTabRedraw = false;

    public ControlPanelManager(CustomPanelAPI panel, DialogCallbacks callbacks,
                               InteractionDialogAPI dialog, CustomUIPanelPlugin plugin) {
        this.panel = panel;
        this.callbacks = callbacks;
        this.dialog = dialog;
        this.plugin = plugin;
        ControlPanelManager.instance = this;
    }

    public static ControlPanelManager getInstance() {
        return instance;
    }

    public boolean isPanelRedrawQueued() {
        return queuedPanelRedraw;
    }

    public boolean isTabRedrawQueued() {
        return queuedTabRedraw;
    }

    public void setQueuedPanelRedraw(boolean queuedPanelRedraw) {
        this.queuedPanelRedraw = queuedPanelRedraw;
    }

    public void setQueuedTabRedraw(boolean queuedTabRedraw) {
        this.queuedTabRedraw = queuedTabRedraw;
    }

    public CustomPanelAPI getPanel() {
        return panel;
    }

    public CustomUIPanelPlugin getPlugin() {
        return plugin;
    }

    public void advance() {
        ButtonListener.checkIndex();
        if (HullBlueprintSection.isCardRedrawQueued()) {
            HullBlueprintSection.renderVariantCard();
            HullBlueprintSection.setCardRedrawQueued(false);
        }
        if (ModulesTab.isCardRedrawQueued()) {
            ModulesTab.renderModuleCard();
            Sidebar.renderContent();
            ModulesTab.setCardRedrawQueued(false);
        }
        if (ModuleList.isListRedrawQueued()) {
            ModuleList.renderModuleList();
            ModuleList.setListRedrawQueued(false);
        }
        if (this.isTabRedrawQueued()) {
            ControlPanelAssembly.renderTab(panel, this.plugin);
            this.setQueuedTabRedraw(false);
        }
        if (this.isPanelRedrawQueued()) {
            ControlPanelAssembly.renderPanel(panel, this.plugin);
            this.setQueuedPanelRedraw(false);
        }
    }

    public void dismissPanel() {
        // Dismissing callbacks dialog means just closing the custom panel.
        this.callbacks.dismissDialog();
        // Dismissing dialog itself means exit from initialization dialog too.
        this.dialog.dismiss();
    }

}
