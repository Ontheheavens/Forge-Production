package forgprod.abilities.interaction.wrappers;

import java.util.Map;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.EngagementResultAPI;

import forgprod.abilities.interaction.ControlPanelPlugin;
import forgprod.abilities.interaction.panel.PanelConstants;

/**
 * Used as intermediate for calling custom production control panel.
 * @author Ontheheavens
 * @since 15.11.2022
 */

public class ControlInteractionPlugin implements InteractionDialogPlugin {

    private enum Options {
        INIT,
        EXIT
    }

    private InteractionDialogAPI dialog;

    @Override
    public void init(InteractionDialogAPI dialog) {
        this.dialog = dialog;
        dialog.setOptionOnEscape("", Options.EXIT);
        dialog.setPromptText("");
        optionSelected("", Options.INIT);
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (optionData == null) return;
        Options option = (Options) optionData;
        switch (option) {
            case INIT:
                dialog.showCustomVisualDialog(PanelConstants.PANEL_WIDTH, PanelConstants.PANEL_HEIGHT,
                        new ControlDialogDelegate(new ControlPanelPlugin(), this.dialog));
                break;
            case EXIT:
                dialog.dismiss();
                break;
        }
    }

    @Override
    public void optionMousedOver(String optionText, Object optionData) {}

    @Override
    public void advance(float amount) {}

    @Override
    public void backFromEngagement(EngagementResultAPI battleResult) {}

    @Override
    public Object getContext() {
        return null;
    }

    @Override
    public Map<String, MemoryAPI> getMemoryMap() {
        return null;
    }

}
