package data.forge.abilities.tooltip;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.forge.abilities.conversion.support.ForgeConversionGeneral;
import data.forge.abilities.conversion.support.ForgeTypes;
import data.forge.campaign.ForgeConditionChecker;

import java.awt.*;

import static data.forge.abilities.conversion.support.ForgeConversionGeneral.getForgingCapacityWithCR;
import static data.forge.abilities.conversion.support.ForgeTypes.*;
import static data.forge.plugins.ForgeSettings.*;

public class ForgeTooltipBreakdown {

    public static void addProductionBreakdown(TooltipMakerAPI tooltip, boolean isActive) {

        float pad = 10f;

        tooltip.addSectionHeading("Production details", Alignment.MID, pad);
        tooltip.addSpacer(3f);

        if (ForgeConditionChecker.hasMinimumMachinery()) {
            addMachineryRequirementLine(tooltip, isActive);

            tooltip.addSpacer(3f);

            addProductionValues(tooltip, isActive);
        }

        if (!ForgeConditionChecker.hasMinimumMachinery()) {
            addInsufficientMachineryLine(tooltip);
        }

    }

    private static void addProductionValues(TooltipMakerAPI tooltip, boolean isActive) {

        Color textColor = Misc.getTextColor();
        Color highlightColor = Misc.getHighlightColor();
        Color negativeHighlight = Misc.getNegativeHighlightColor();

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        float pad = 10f;

        int gridSize = 0;
        float gridWidth = 185f;
        tooltip.beginGridFlipped(gridWidth, 2, 45f, pad);

        String tanksAreFullFormat = "     Halted: tanks full";
        if (!isActive) {
            tanksAreFullFormat = "  Standby: tanks full";
        }

        String noInputsFormat = "     Halted: no inputs";
        if (!isActive) {
            noInputsFormat = "  Standby: no inputs";
        }
        String lowInputsFormat = "  Working: low inputs";
        if (!isActive) {
            lowInputsFormat = "Standby: low inputs";
        }
        String workingFormat = "                    Working";
        if (!isActive) {
            workingFormat = "                  Standby";
        }

        if (getForgingCapacityWithCR(fleet, REFINERY) >= 1) {

            int machineryAvailable = (int) Math.floor(ForgeConversionGeneral.getMachineryUsageCycles(ForgeTypes.Conversion.REFINING, ForgeTypes.REFINERY));

            int maxMetalProductionCapacity = ((int) Math.floor(Math.min(getForgingCapacityWithCR(fleet, REFINERY), machineryAvailable) *
                    (METAL_PRODUCED + ForgeConditionChecker.getCatalyticCoreBonus())));
            String metalLabel = "Metal / day";

            tooltip.addToGrid(0, gridSize++, metalLabel, "+" + Misc.getWithDGS(maxMetalProductionCapacity), highlightColor);

            boolean noOre = ForgeConditionChecker.getPlayerCargo(Commodities.ORE) < ORE_TO_REFINE;

            if (noOre) {
                tooltip.setGridLabelColor(negativeHighlight);
                tooltip.addToGrid(1, 0, noInputsFormat, "", negativeHighlight);
            } else {
                tooltip.setGridLabelColor(highlightColor);
                tooltip.addToGrid(1, 0, workingFormat, "", highlightColor);
            }
            tooltip.setGridLabelColor(textColor);

            int maxTransplutonicsProductionCapacity = (int) Math.floor(Math.min(getForgingCapacityWithCR(fleet, REFINERY), machineryAvailable) *
                    (TRANSPLUTONICS_PRODUCED + ForgeConditionChecker.getCatalyticCoreBonus()));
            String transplutonicsLabel = "Transplutonics / day";

            tooltip.addToGrid(0, gridSize++, transplutonicsLabel, "+" + Misc.getWithDGS(maxTransplutonicsProductionCapacity), highlightColor);

            boolean noTransplutonicOre = ForgeConditionChecker.getPlayerCargo(Commodities.RARE_ORE) < TRANSPLUTONIC_ORE_TO_REFINE;

            if (noTransplutonicOre) {
                tooltip.setGridLabelColor(negativeHighlight);
                tooltip.addToGrid(1, gridSize - 1, noInputsFormat, "", negativeHighlight);
            } else {
                tooltip.setGridLabelColor(highlightColor);
                tooltip.addToGrid(1, gridSize - 1, workingFormat, "", highlightColor);
            }
            tooltip.setGridLabelColor(textColor);

        }

        if (getForgingCapacityWithCR(fleet, CENTRIFUGE) >= 1) {

            int machineryAvailable = (int) Math.floor(ForgeConversionGeneral.getMachineryUsageCycles(ForgeTypes.Conversion.CENTRIFUGING, ForgeTypes.CENTRIFUGE));

            int maxFuelProductionCapacity = (int) Math.floor(Math.min(getForgingCapacityWithCR(fleet, CENTRIFUGE), machineryAvailable) *
                    (FUEL_PRODUCED + ForgeConditionChecker.getSynchrotronCoreBonus()));

            String fuelLabel = "Fuel / day";

            tooltip.addToGrid(0, gridSize++, fuelLabel, "+" + Misc.getWithDGS(maxFuelProductionCapacity), highlightColor);

            if (ForgeConditionChecker.areFuelTanksFull(fleet)) {
                tooltip.setGridLabelColor(negativeHighlight);
                tooltip.addToGrid(1, gridSize - 1, tanksAreFullFormat, "", negativeHighlight);
                tooltip.setGridLabelColor(textColor);
            } else if (ForgeConditionChecker.getPlayerCargo(Commodities.VOLATILES) < VOLATILES_TO_CENTRIFUGE) {
                tooltip.setGridLabelColor(negativeHighlight);
                tooltip.addToGrid(1, gridSize - 1, noInputsFormat, "", negativeHighlight);
                tooltip.setGridLabelColor(textColor);
            } else {
                tooltip.setGridLabelColor(highlightColor);
                tooltip.addToGrid(1, gridSize - 1, workingFormat, "", highlightColor);
                tooltip.setGridLabelColor(textColor);
            }

        }

        if (getForgingCapacityWithCR(fleet, MANUFACTURE) >= 1) {

            int machineryAvailable = (int) Math.floor(ForgeConversionGeneral.getMachineryUsageCycles(ForgeTypes.Conversion.MANUFACTURING, ForgeTypes.MANUFACTURE));

            int maxSuppliesProductionCapacity = (int) Math.floor(Math.min(getForgingCapacityWithCR(fleet, MANUFACTURE), machineryAvailable) *
                    (SUPPLIES_PRODUCED + ForgeConditionChecker.getNanoforgeManufacturingBonus()));
            String suppliesLabel = "Supplies / day";

            tooltip.addToGrid(0, gridSize++, suppliesLabel, "+" + Misc.getWithDGS(maxSuppliesProductionCapacity), highlightColor);

            boolean noMetal = ForgeConditionChecker.getPlayerCargo(Commodities.METALS) < METAL_TO_MANUFACTURE;
            boolean noTransplutonics = ForgeConditionChecker.getPlayerCargo(Commodities.RARE_METALS) < TRANSPLUTONICS_TO_MANUFACTURE;
            boolean noInputs = noMetal || noTransplutonics;

            boolean hasRefining = getForgingCapacityWithCR(fleet, REFINERY) >= 1;
            boolean hasOre = ForgeConditionChecker.getPlayerCargo(Commodities.ORE) > ORE_TO_REFINE;
            boolean hasTransplutonicOre = ForgeConditionChecker.getPlayerCargo(Commodities.RARE_ORE) > TRANSPLUTONIC_ORE_TO_REFINE;
            boolean hasRawOres = hasOre && hasTransplutonicOre;
            boolean hasOresAndRefining = hasRefining && hasRawOres;
            boolean refiningOreOperational = hasRefining && hasOre;
            boolean refiningTransplutonicsOperational = hasRefining && hasTransplutonicOre;
            boolean hasOneRawAndOneProcessed = (!noMetal && refiningTransplutonicsOperational)
                                            || (!noTransplutonics && refiningOreOperational);

            if (noInputs && !hasOresAndRefining && !hasOneRawAndOneProcessed) {
                tooltip.setGridLabelColor(negativeHighlight);
                tooltip.addToGrid(1, gridSize - 1, noInputsFormat, "", negativeHighlight);
            } else if (noInputs || hasOneRawAndOneProcessed) {
                tooltip.setGridLabelColor(highlightColor);
                tooltip.addToGrid(1, gridSize - 1, lowInputsFormat, "", highlightColor);
            } else {
                tooltip.setGridLabelColor(highlightColor);
                tooltip.addToGrid(1, gridSize - 1, workingFormat, "", highlightColor);
            }
            tooltip.setGridLabelColor(textColor);

        }

        if (getForgingCapacityWithCR(fleet, ASSEMBLY) > 0) {

            int machineryAvailable = (int) Math.floor(ForgeConversionGeneral.getMachineryUsageCycles(ForgeTypes.Conversion.ASSEMBLING, ForgeTypes.ASSEMBLY));

            int maxHeavyMachineryProductionCapacity = (int) Math.floor(Math.min(getForgingCapacityWithCR(fleet, ASSEMBLY), machineryAvailable) *
                    (HEAVY_MACHINERY_PRODUCED + ForgeConditionChecker.getNanoforgeAssemblingBonus()));
            String machineryLabel = "Machinery / day";

            tooltip.addToGrid(0, gridSize++, machineryLabel, "+" + Misc.getWithDGS(maxHeavyMachineryProductionCapacity), highlightColor);

            boolean noMetal = ForgeConditionChecker.getPlayerCargo(Commodities.METALS) < METAL_TO_ASSEMBLE;
            boolean noTransplutonics = ForgeConditionChecker.getPlayerCargo(Commodities.RARE_METALS) < TRANSPLUTONICS_TO_ASSEMBLE;
            boolean noInputs = noMetal || noTransplutonics;

            boolean hasRefining = getForgingCapacityWithCR(fleet, REFINERY) >= 1;
            boolean hasOre = ForgeConditionChecker.getPlayerCargo(Commodities.ORE) > ORE_TO_REFINE;
            boolean hasTransplutonicOre = ForgeConditionChecker.getPlayerCargo(Commodities.RARE_ORE) > TRANSPLUTONIC_ORE_TO_REFINE;
            boolean hasRawOres = hasOre && hasTransplutonicOre;
            boolean hasOresAndRefining = hasRefining && hasRawOres;
            boolean refiningOreOperational = hasRefining && hasOre;
            boolean refiningTransplutonicsOperational = hasRefining && hasTransplutonicOre;
            boolean hasOneRawAndOneProcessed = (!noMetal && refiningTransplutonicsOperational)
                    || (!noTransplutonics && refiningOreOperational);

            if (noInputs && !hasOresAndRefining && !hasOneRawAndOneProcessed) {
                tooltip.setGridLabelColor(negativeHighlight);
                tooltip.addToGrid(1, gridSize - 1, noInputsFormat, "", negativeHighlight);
            } else if (noInputs || hasOneRawAndOneProcessed) {
                tooltip.setGridLabelColor(highlightColor);
                tooltip.addToGrid(1, gridSize - 1, lowInputsFormat, "", highlightColor);
            } else {
                tooltip.setGridLabelColor(highlightColor);
                tooltip.addToGrid(1, gridSize - 1, workingFormat, "", highlightColor);
            }
            tooltip.setGridLabelColor(textColor);

        }

        tooltip.addGrid(0);

    }

    public static void addMachineryRequirementLine(TooltipMakerAPI tooltip, boolean isActive) {

        Color highlightColor = Misc.getHighlightColor();
        Color negativeHighlight = Misc.getNegativeHighlightColor();

        float pad = 5f;

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        Color [] positiveHighlights = {highlightColor};
        Color [] negativeHighlights = {negativeHighlight, highlightColor, highlightColor};

        int maxMachineryNeededFinal = (int) Math.ceil(ForgeConversionGeneral.getTotalFleetMachineryRequirement(fleet));
        int heavyMachineryAvailable = (int) fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY);

        int machineryPercentage = (int) (ForgeConversionGeneral.getMachineryAvailability() * 100f);
        String machineryUsagePercentage = machineryPercentage + "%";

        String machineryNeeded = Misc.getWithDGS(maxMachineryNeededFinal);
        String machineryAvailable = Misc.getWithDGS(heavyMachineryAvailable);

        String usingOrCanUse = "Using";
        if (!isActive) {
            usingOrCanUse = "Can use";
        }

        String demandMetFormat = " all %s needed heavy machinery:";
        String demandNotMetFormat = " %s (out of %s needed, %s ratio) machinery:";

        tooltip.setBulletedListMode("   ");

        if (heavyMachineryAvailable >= maxMachineryNeededFinal ) {
            tooltip.addPara(usingOrCanUse + demandMetFormat, pad, positiveHighlights, machineryNeeded);
        }
        else {
            tooltip.addPara(usingOrCanUse + demandNotMetFormat, pad, negativeHighlights, machineryAvailable, machineryNeeded, machineryUsagePercentage);
        }

        tooltip.setBulletedListMode(null);

    }

    public static void addInsufficientMachineryLine(TooltipMakerAPI tooltip) {

        Color highlightColor = Misc.getHighlightColor();
        Color negativeHighlight = Misc.getNegativeHighlightColor();

        float pad = 5f;

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        Color [] tooFewMachineryHighlights = {negativeHighlight, negativeHighlight, highlightColor, negativeHighlight};

        int maxMachineryNeededFinal = (int) Math.ceil(ForgeConversionGeneral.getTotalFleetMachineryRequirement(fleet));
        int heavyMachineryAvailable = (int) fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY);

        int machineryPercentage = (int) (ForgeConversionGeneral.getMachineryAvailability() * 100f);
        String machineryUsagePercentage = machineryPercentage + "%";

        String machineryNeeded = Misc.getWithDGS(maxMachineryNeededFinal);
        String machineryAvailable = Misc.getWithDGS(heavyMachineryAvailable);
        String tooFewMachinery = "Production halted:";

        String tooFewMachineryFormat = "%s only %s (out of %s needed, %s ratio) machinery is available.";

        tooltip.setBulletedListMode("   ");

        if (ForgeConversionGeneral.getMachineryAvailability() > 0 && ForgeConditionChecker.getPlayerCargo(Commodities.HEAVY_MACHINERY) >= 1 ) {
            tooltip.addPara(tooFewMachineryFormat, pad, tooFewMachineryHighlights, tooFewMachinery, machineryAvailable, machineryNeeded, machineryUsagePercentage);
        }

        tooltip.setBulletedListMode(null);

    }

}
