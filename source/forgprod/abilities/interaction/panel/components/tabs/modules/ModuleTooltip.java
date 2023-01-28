package forgprod.abilities.interaction.panel.components.tabs.modules;

import java.awt.*;

import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import com.fs.starfarer.api.util.Misc;

import forgprod.abilities.conversion.support.ProductionConstants;
import forgprod.abilities.interaction.panel.PanelConstants;
import forgprod.abilities.modules.dataholders.ProductionModule;

/**
 * @author Ontheheavens
 * @since 03.01.2023
 */

public class ModuleTooltip implements TooltipCreator {

    private final ProductionModule module;

    public ModuleTooltip(ProductionModule module) {
        this.module = module;
    }

    @Override
    public boolean isTooltipExpandable(Object tooltipParam) {
        return false;
    }

    @Override
    public float getTooltipWidth(Object tooltipParam) {
        return 220;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
        tooltip.addTitle(ProductionConstants.HULLMOD_NAMES.get(module.getHullmodId()), PanelConstants.PLAYER_COLOR);
        Color designType = new Color(155, 155, 155, 255);
        String design = "Domain Restricted";
        tooltip.addPara("Design type: " + design, 10f, Misc.getGrayColor(), designType, design);
        String description = ProductionConstants.HULLMOD_DESCRIPTIONS.get(module.getHullmodId());
        tooltip.addPara(description, 10f);
    }

}
