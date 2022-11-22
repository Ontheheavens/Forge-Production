package data.forge.abilities.tooltip;

import data.forge.abilities.ForgeProduction;
import data.forge.plugins.ForgeSettings;

import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;
import java.util.List;

import static data.forge.plugins.ForgeSettings.SHIP_LIST_SIZE;

public class ForgeTooltipShips {
    public static void addOperationalShipsBreakdown(TooltipMakerAPI tooltip) {

        Color textColor = Misc.getTextColor();
        Color highlightColor = Misc.getHighlightColor();

        String indent = "   ";
        float pad = 10f;

        tooltip.addSectionHeading("Operational ships", Alignment.MID, pad);
        tooltip.addSpacer(6f);

        java.util.List<FleetMemberAPI> forgeShips = ForgeProductionTooltip.getOperationalForgeShipsList();

        int counter = 1;
        int rowNumber = 0;
        int displayThatMany = SHIP_LIST_SIZE;
        int shipsOverTooltipCap = forgeShips.size() - displayThatMany;

        tooltip.beginGrid(380f, 1, textColor);

        for (FleetMemberAPI member : forgeShips) {

            if (counter > displayThatMany) {
                break;
            }

            String shipName = member.getShipName();
            String shipNameShort = tooltip.shortenString(shipName, 175);
            String shipClass = member.getHullSpec().getHullNameWithDashClass();
            String forgeModule = ForgeProductionTooltip.getForgeHullmodOfForgeShip(member);

            String label = indent + shipNameShort + ", " + shipClass;

            tooltip.addToGrid(0, rowNumber++, label, forgeModule, highlightColor);

            counter++;

        }

        tooltip.addGrid(1);

        if ((counter > displayThatMany) && !(shipsOverTooltipCap == 0)) {
            String shipsOverCapString = String.valueOf(shipsOverTooltipCap);
            String format = "   ...and %s more.";
            tooltip.addPara(format, 3, textColor, highlightColor, shipsOverCapString);
        }

    }


    public static void addInactiveShipsBreakdown(TooltipMakerAPI tooltip) {

        Color negativeHighlightColor = Misc.getNegativeHighlightColor();
        Color grayColor = Misc.getGrayColor();
        Color highlightColor = Misc.getHighlightColor();

        String indent = "   ";
        float pad = 10f;

        tooltip.addSectionHeading("Inactive ships", Alignment.MID, pad);
        tooltip.addSpacer(6f);

        List<FleetMemberAPI> forgeShips = ForgeProductionTooltip.getInactiveForgeShipsList();

        int counter = 1;
        int rowNumber = 0;
        int displayThatMany = SHIP_LIST_SIZE;
        int shipsOverTooltipCap = forgeShips.size() - displayThatMany;

        tooltip.beginGrid(380f, 1, grayColor);

        for (FleetMemberAPI member : forgeShips) {

            if (counter > displayThatMany) {
                break;
            }

            String shipName = member.getShipName();
            String shipNameShort = tooltip.shortenString(shipName, 175);
            String shipClass = member.getHullSpec().getHullNameWithDashClass();
            String forgeModule = ForgeProductionTooltip.getForgeHullmodOfForgeShip(member);

            String label = indent + shipNameShort + ", " + shipClass;

            tooltip.addToGrid(0, rowNumber++, label, forgeModule, negativeHighlightColor);
            counter++;

        }

        tooltip.addGrid(3);

        if ((counter > displayThatMany) && !(shipsOverTooltipCap == 0)) {
            String shipsOverCapString = String.valueOf(shipsOverTooltipCap);
            String format = "   ...and %s more.";
            tooltip.addPara(format, 3, grayColor, highlightColor, shipsOverCapString);
        }
    }

}
