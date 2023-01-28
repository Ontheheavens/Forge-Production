package forgprod.abilities.interaction.wrappers;

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;

import forgprod.abilities.interaction.ControlPanelPlugin;

/**
 * @author Ontheheavens
 * @since 24.12.2022
 */

public class ControlDialogDelegate implements CustomVisualDialogDelegate {

    protected DialogCallbacks callbacks;
    protected final ControlPanelPlugin plugin;
    protected InteractionDialogAPI dialog;

    public ControlDialogDelegate(ControlPanelPlugin panelPlugin, InteractionDialogAPI dialog) {
        this.plugin = panelPlugin;
        this.dialog = dialog;
    }

    @Override
    public void init(CustomPanelAPI panel, DialogCallbacks callbacks) {
        this.callbacks = callbacks;
        plugin.init(panel, callbacks, dialog);
    }

    @Override
    public CustomUIPanelPlugin getCustomPanelPlugin() { return plugin; }

    @Override
    public float getNoiseAlpha() { return 0; }

    @Override
    public void advance(float amount) {}

    @Override
    public void reportDismissed(int option) {}

}
