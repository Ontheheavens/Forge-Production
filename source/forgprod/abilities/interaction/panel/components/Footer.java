package forgprod.abilities.interaction.panel.components;

import com.fs.starfarer.api.ui.*;

import forgprod.abilities.interaction.panel.ControlPanelManager;
import forgprod.abilities.interaction.panel.PanelConstants;
import forgprod.abilities.interaction.panel.objects.Button;

import static forgprod.abilities.interaction.panel.PanelConstants.PANEL_CONTENT_OFFSET;

/**
 * @author Ontheheavens
 * @since 29.12.2022
 */

public class Footer {

    public static TooltipMakerAPI create(CustomPanelAPI panel, float width) {
        float offset = PANEL_CONTENT_OFFSET * 2;
        TooltipMakerAPI footer = panel.createUIElement(width, 10f, false);
        footer.setForceProcessInput(true);
        ButtonAPI footerLine = Common.addLine(footer, (width - offset + 4f) - 259f);
        footerLine.getPosition().setYAlignOffset(17f);
        footerLine.getPosition().setXAlignOffset(4f);
        Footer.createExitButton(footer);
        panel.addUIElement(footer).inBR(2f, 2f);
        return footer;
    }

    private static void createExitButton(TooltipMakerAPI footer) {
        footer.setButtonFontVictor14();
        ButtonAPI exitButtonInstance = footer.addButton("DISMISS", null, PanelConstants.PLAYER_COLOR,
                PanelConstants.DARK_PLAYER_COLOR, Alignment.MID, CutStyle.TL_BR, 250f, 25f, 2f);
        float offset = PANEL_CONTENT_OFFSET;
        exitButtonInstance.getPosition().inBR(offset, offset);
        new Button(exitButtonInstance, Button.Type.STANDARD) {
            @Override
            public void applyEffect() {
                ControlPanelManager.getInstance().dismissPanel();
            }
        };
    }

}
