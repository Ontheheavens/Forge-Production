package forgprod.abilities.interaction;

import org.lwjgl.input.Keyboard;

import java.util.List;

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate.DialogCallbacks;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;

import forgprod.abilities.interaction.panel.ControlPanelAssembly;
import forgprod.abilities.interaction.panel.ControlPanelManager;
import forgprod.abilities.interaction.panel.PanelConstants;

/**
 * @author Ontheheavens
 * @since 25.12.2022
 */

public class ControlPanelPlugin implements CustomUIPanelPlugin {

    public void init(CustomPanelAPI panel, DialogCallbacks callbacks, InteractionDialogAPI dialog) {
        new ControlPanelManager(panel, callbacks, dialog, this);
        ControlPanelAssembly.setShownTab(PanelConstants.ShownTab.MODULES);
        ControlPanelAssembly.renderPanel(panel, this);
    }

    @Override
    public void positionChanged(PositionAPI position) {}

    @Override
    public void renderBelow(float alphaMult) {
    }

    @Override
    public void render(float alphaMult) {}

    @Override
    public void advance(float amount) {
        ControlPanelManager.getInstance().advance();
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        for (InputEventAPI event : events) {
            if (event.isConsumed()) continue;
            if (event.isKeyDownEvent() && event.getEventValue() == Keyboard.KEY_ESCAPE) {
                event.consume();
                ControlPanelManager.getInstance().dismissPanel();
                return;
            }
        }
    }

}
