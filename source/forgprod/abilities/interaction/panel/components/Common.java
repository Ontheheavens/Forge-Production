package forgprod.abilities.interaction.panel.components;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;

import forgprod.abilities.conversion.support.checks.FleetwideProductionChecks;
import forgprod.abilities.interaction.panel.PanelConstants;
import forgprod.abilities.modules.dataholders.ProductionCapacity;
import forgprod.abilities.modules.dataholders.ProductionModule;

/**
 * @author Ontheheavens
 * @since 01.01.2023
 */

public class Common {

    public static ButtonAPI addLine(TooltipMakerAPI tooltip, float width) {
        ButtonAPI line = tooltip.addButton("", null, PanelConstants.PLAYER_COLOR,
                PanelConstants.DARK_PLAYER_COLOR, width, 0f, 0f);
        line.setEnabled(false);
        line.highlight();
        return line;
    }

    public static UIComponentAPI addShipIcon(TooltipMakerAPI tooltip, final ProductionModule module,
                                             float size, float pad, boolean interactive) {
        List<FleetMemberAPI> memberAsList = new ArrayList<>();
        memberAsList.add(module.getParentFleetMember());
        if (interactive) {
            tooltip.addShipList(1, 1, size, PanelConstants.PLAYER_COLOR, memberAsList, pad);
        } else {
            TooltipMakerAPI shipIconPanel = tooltip.beginImageWithText(null, size);
            shipIconPanel.setForceProcessInput(false);
            float frameSize = size + 8f;
            shipIconPanel.addImage(null, frameSize, frameSize, 0f);
            UIComponentAPI shipIconFrame = shipIconPanel.getPrev();
            shipIconFrame.getPosition().setXAlignOffset(-(222f));
            shipIconPanel.addShipList(1, 1, size, PanelConstants.PLAYER_COLOR, memberAsList, pad);
            PositionAPI shipIconPosition = shipIconPanel.getPrev().getPosition();
            shipIconPosition.belowLeft(shipIconFrame, -54f);
            shipIconPosition.setXAlignOffset(-6f);
            tooltip.addImageWithText(0f);
        }
        return tooltip.getPrev();
    }

    public static Pair<String, Color> retrieveCapacityThroughputStatus(ProductionCapacity capacity, CampaignFleetAPI fleet) {
        float availability = FleetwideProductionChecks.getMachineryAvailability(fleet);
        String status = "";
        Color statusColor = Misc.getTextColor();
        if (capacity.isActive() && availability < 1 && FleetwideProductionChecks.hasMinimumMachinery(fleet)) {
            status = "(" + ((int) (availability * 100f)) + "% machinery)";
            statusColor = Misc.getNegativeHighlightColor();
        } else if (capacity.isActive() && (capacity.getCurrentThroughput() > 0f)) {
            status = "(" + (capacity.getParentActiveCapacitiesAmount()) + " active)";
            statusColor = Misc.getHighlightColor();
        } else if (capacity.isToggledOn() && !capacity.parentShipOperational()) {
            status = "(ship inactive)";
            statusColor = Misc.getNegativeHighlightColor();
        } else if ((capacity.getCurrentThroughput() <= 0f) && !FleetwideProductionChecks.hasMinimumMachinery(fleet)) {
            status = "(low machinery)";
            statusColor = Misc.getNegativeHighlightColor();
        } else if (!capacity.isToggledOn()) {
            status = "(toggled off)";
            statusColor = Misc.getGrayColor();
        }
        return new Pair<>(status, statusColor);
    }

}
