package data.forge.abilities.tooltip;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.forge.campaign.ForgeConditionChecker;

import java.awt.*;

import static data.forge.abilities.conversion.ForgeConversionGeneral.getForgingCapacity;
import static data.forge.abilities.conversion.ForgeTypes.*;
import static data.forge.abilities.conversion.ForgeTypes.ASSEMBLING;
import static data.forge.plugins.ForgeSettings.*;
import static data.forge.plugins.ForgeSettings.HEAVY_MACHINERY_ASSEMBLING_USAGE;

public class ForgeTooltipBreakdown {

    public static void addProductionBreakdown(TooltipMakerAPI tooltip) {

        Color textColor = Misc.getTextColor();
        Color highlightColor = Misc.getHighlightColor();
        Color negativeHighlight = Misc.getNegativeHighlightColor();

        float pad = 10f;

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        tooltip.addSectionHeading("Production details", Alignment.MID, pad);
        tooltip.addSpacer(3f);

        addMachineryRequirementLine(tooltip);

        tooltip.addSpacer(3f);

        int gridSize = 0;
        float gridWidth = 190f;
        tooltip.beginGridFlipped(gridWidth, 2, 45f, pad);

        if (getForgingCapacity(fleet, REFINING) >= 1) {

            int maxMetalProductionCapacity = ((int) Math.ceil(getForgingCapacity(fleet, REFINING) *
                    (METAL_PRODUCED + ForgeConditionChecker.getCatalyticCoreBonus())));
            String metalLabel = "Metal / day";

            tooltip.addToGrid(0, gridSize++, metalLabel, "+" + Misc.getWithDGS(maxMetalProductionCapacity), highlightColor);

            int maxTransplutonicsProductionCapacity = (int) Math.ceil(getForgingCapacity(fleet, REFINING) *
                    (TRANSPLUTONICS_PRODUCED + ForgeConditionChecker.getCatalyticCoreBonus()));
            String transplutonicsLabel = "Transplutonics / day";

            tooltip.addToGrid(0, gridSize++, transplutonicsLabel, "+" + Misc.getWithDGS(maxTransplutonicsProductionCapacity), highlightColor);

        }

        if (getForgingCapacity(fleet, CENTRIFUGING) >= 1) {

            int maxFuelProductionCapacity = (int) Math.ceil(getForgingCapacity(fleet, CENTRIFUGING) *
                    (FUEL_PRODUCED + ForgeConditionChecker.getSynchrotronCoreBonus()));

            String tanksAreFullFormat = "(Halted: tanks full)";
            String fuelLabel = "Fuel / day";

            tooltip.addToGrid(0, gridSize++, fuelLabel, "+" + Misc.getWithDGS(maxFuelProductionCapacity), highlightColor);

            if (ForgeConditionChecker.areFuelTanksFull(fleet)) {
                tooltip.setGridLabelColor(negativeHighlight);
                tooltip.addToGrid(1, gridSize - 1, tanksAreFullFormat, "", negativeHighlight);
                tooltip.setGridLabelColor(textColor);
            }

        }

        if (getForgingCapacity(fleet, MANUFACTURING) >= 1) {

            int maxSuppliesProductionCapacity = (int) Math.ceil(getForgingCapacity(fleet, MANUFACTURING) *
                    (SUPPLIES_PRODUCED + ForgeConditionChecker.getNanoforgeManufacturingBonus()));
            String suppliesLabel = "Supplies / day";

            tooltip.addToGrid(0, gridSize++, suppliesLabel, "+" + Misc.getWithDGS(maxSuppliesProductionCapacity), highlightColor);

        }

        if (getForgingCapacity(fleet, ASSEMBLING) > 0) {

            int maxHeavyMachineryProductionCapacity = (int) Math.ceil(getForgingCapacity(fleet, ASSEMBLING) *
                    (HEAVY_MACHINERY_PRODUCED + ForgeConditionChecker.getNanoforgeAssemblingBonus()));
            String machineryLabel = "Machinery / day";

            tooltip.addToGrid(0, gridSize++, machineryLabel, "+" + Misc.getWithDGS(maxHeavyMachineryProductionCapacity), highlightColor);

        }

        tooltip.addGrid(0);

    }

    public static void addMachineryRequirementLine(TooltipMakerAPI tooltip) {

        Color textColor = Misc.getTextColor();
        Color highlightColor = Misc.getHighlightColor();
        Color negativeHighlight = Misc.getNegativeHighlightColor();

        String indent = "   ";
        float pad = 5f;

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        Color [] positiveHighlights = {textColor, highlightColor};
        Color [] negativeHighlights = {textColor, negativeHighlight, highlightColor};

        float maxMachineryNeededFirst = Math.max(getForgingCapacity(fleet, REFINING) * HEAVY_MACHINERY_REFINING_USAGE,
                getForgingCapacity(fleet, CENTRIFUGING) * HEAVY_MACHINERY_CENTRIFUGING_USAGE);
        float maxMachineryNeededSecond = Math.max(getForgingCapacity(fleet, MANUFACTURING) * HEAVY_MACHINERY_MANUFACTURING_USAGE,
                getForgingCapacity(fleet, ASSEMBLING) * HEAVY_MACHINERY_ASSEMBLING_USAGE);
        int maxMachineryNeededFinal = (int) Math.max(maxMachineryNeededFirst, maxMachineryNeededSecond);
        int heavyMachineryAvailable = (int) fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY);

        String machineryNeeded = Misc.getWithDGS(maxMachineryNeededFinal);
        String machineryAvailable = Misc.getWithDGS(heavyMachineryAvailable);

        String demandMetFormat = "%sUsing all %s needed heavy machinery:";
        String demandNotMetFormat = "%sUsing %s (out of %s needed) machinery:";

        if (heavyMachineryAvailable >= maxMachineryNeededFinal ) {
            tooltip.addPara(demandMetFormat, pad, positiveHighlights, indent, machineryNeeded);
        }
        else {
            tooltip.addPara(demandNotMetFormat, pad, negativeHighlights, indent, machineryAvailable, machineryNeeded);
        }

    }

}
