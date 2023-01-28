package forgprod.abilities.interaction.panel.components.tabs.modules;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

import forgprod.abilities.modules.dataholders.ProductionModule.ProductionMode;

/**
 * @author Ontheheavens
 * @since 16.01.2023
 */

public class ModeButtonTooltip implements TooltipCreator {

    private final ProductionMode associatedMode;

    public ModeButtonTooltip(ProductionMode mode) {
        this.associatedMode = mode;
    }

    @Override
    public boolean isTooltipExpandable(Object tooltipParam) {
        return false;
    }

    @Override
    public float getTooltipWidth(Object tooltipParam) {
        return 200;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
        String modeTitle = "Primary";
        if (associatedMode == ProductionMode.SECONDARY) {
            modeTitle = "Secondary";
        }
        tooltip.addTitle(modeTitle + " Mode", Misc.getBasePlayerColor());
        tooltip.addPara("Production mode determines which module capacities can be activated.", Misc.getTextColor(), 10f);
    }

}
