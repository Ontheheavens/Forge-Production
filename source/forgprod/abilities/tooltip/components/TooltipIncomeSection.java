package forgprod.abilities.tooltip.components;

import java.awt.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;

import forgprod.abilities.conversion.logic.production.FuelProductionLogic;
import forgprod.abilities.conversion.logic.production.HullPartsProductionLogic;
import forgprod.abilities.conversion.support.ProductionType;
import forgprod.abilities.conversion.support.checks.FleetwideProductionChecks;
import forgprod.abilities.conversion.support.checks.ItemBonusesChecks;
import forgprod.abilities.modules.FleetwideModuleManager;
import forgprod.hullmods.tooltip.TooltipCapacitySection;

import static forgprod.abilities.conversion.support.ProductionConstants.*;
import static forgprod.abilities.conversion.support.checks.FleetwideProductionChecks.getFleetCapacityForType;

public class TooltipIncomeSection {

    public static void addProductionBreakdown(CampaignFleetAPI fleet, TooltipMakerAPI tooltip, boolean isActive) {
        float pad = 10f;
        tooltip.addSectionHeading("Daily production", Alignment.MID, pad);
        tooltip.addSpacer(5f);
        addMachineryRequirementLine(fleet, tooltip);
        if (FleetwideProductionChecks.hasMinimumMachinery(fleet)) {
            tooltip.addSpacer(6f);
            addProductionValues(tooltip, fleet, isActive);
        } else {
            tooltip.addSpacer(6f);
        }
    }

    public static void addMachineryRequirementLine(CampaignFleetAPI fleet, TooltipMakerAPI tooltip) {
        Color highlightColor = Misc.getHighlightColor();
        Color negativeHighlight = Misc.getNegativeHighlightColor();
        float availability = FleetwideProductionChecks.getMachineryAvailability(fleet);
        String machineryUsagePercentage = (int) (availability * 100f) + "%";
        Color ratioColor = highlightColor;
        String ratioIcon = Global.getSettings().getSpriteName("forgprod_ui", "production_satisfied");
        String status = "Full output";
        if (availability < 1f) {
            ratioColor = negativeHighlight;
            ratioIcon = Global.getSettings().getSpriteName("forgprod_ui", "production_shortage");
            status = "Low output";
            if (!FleetwideProductionChecks.hasMinimumMachinery(fleet)) {
                status = "No output";
            }
        }
        TooltipMakerAPI availabilityLine = tooltip.beginImageWithText(null, 2f);
        availabilityLine.addImage(ratioIcon, 24f, 24f, 0f);
        UIComponentAPI icon = availabilityLine.getPrev();
        icon.getPosition().setXAlignOffset(-344f);
        availabilityLine.setTextWidthOverride(200f);
        availabilityLine.addPara("Machinery available:", Misc.getTextColor(), 0f).getPosition().rightOfMid(icon, 75f);
        availabilityLine.getPrev().getPosition().setYAlignOffset(-1f);
        availabilityLine.addPara(machineryUsagePercentage, ratioColor, 0f).getPosition().rightOfMid(icon, 0f);
        LabelAPI ratioLabel = (LabelAPI) availabilityLine.getPrev();
        ratioLabel.setAlignment(Alignment.RMID);
        ratioLabel.getPosition().setXAlignOffset(-132f);
        availabilityLine.addPara(status, ratioColor, 0f).getPosition().rightOfMid(icon, 117f);
        LabelAPI incomeStatus = (LabelAPI) availabilityLine.getPrev();
        incomeStatus.setAlignment(Alignment.RMID);
        tooltip.addImageWithText(1f);
        tooltip.addSpacer(-53f);
    }

    private static void addProductionValues(TooltipMakerAPI tooltip, CampaignFleetAPI fleet, boolean isActive) {
        Color highlightColor = Misc.getHighlightColor();
        Color negativeHighlight = Misc.getNegativeHighlightColor();
        String workingFormat = "Working";
        String lowInputsFormat = "Working: low inputs";
        String noInputsFormat = "Halted: no inputs";
        String tanksAreFullFormat = "Halted: tanks full";
        String rosterFullFormat = "Halted: roster full";
        String noRigsFormat = "Halted: no active rigs";
        if (!isActive) {
            workingFormat = "Standby";
            lowInputsFormat = "Standby: low inputs";
            noInputsFormat = "Standby: no inputs";
            tanksAreFullFormat = "Standby: tanks full";
            rosterFullFormat = "Standby: roster full";
            noRigsFormat = "Standby: no active rigs";
        }
        if (getFleetCapacityForType(fleet, ProductionType.METALS_PRODUCTION) >= 1) {
            float metalsIncome = getFleetCapacityForType(fleet, ProductionType.METALS_PRODUCTION) *
                    (METAL_OUTPUT + ItemBonusesChecks.getRefiningBonus(fleet));
            boolean noOre = fleet.getCargo().getCommodityQuantity(Commodities.ORE) < ORE_INPUT;
            String metalsStatus = workingFormat;
            Color metalsColor = highlightColor;
            if (noOre) {
                metalsStatus = noInputsFormat;
                metalsColor = negativeHighlight;
            }
            addIncomeLine(tooltip, ProductionType.METALS_PRODUCTION, metalsIncome, metalsStatus, metalsColor);
        }
        if (getFleetCapacityForType(fleet, ProductionType.TRANSPLUTONICS_PRODUCTION) >= 1) {
            float transplutonicsIncome = getFleetCapacityForType(fleet, ProductionType.TRANSPLUTONICS_PRODUCTION) *
                    (TRANSPLUTONICS_OUTPUT + ItemBonusesChecks.getRefiningBonus(fleet));
            boolean noTransplutonicOre = fleet.getCargo().getCommodityQuantity(Commodities.RARE_ORE) < TRANSPLUTONIC_ORE_INPUT;
            String transplutonicsStatus = workingFormat;
            Color transplutonicsColor = highlightColor;
            if (noTransplutonicOre) {
                transplutonicsStatus = noInputsFormat;
                transplutonicsColor = negativeHighlight;
            }
            addIncomeLine(tooltip, ProductionType.TRANSPLUTONICS_PRODUCTION, transplutonicsIncome, transplutonicsStatus, transplutonicsColor);
        }

        if (getFleetCapacityForType(fleet, ProductionType.FUEL_PRODUCTION) >= 1) {
            float fuelIncome = getFleetCapacityForType(fleet, ProductionType.FUEL_PRODUCTION) *
                    (FUEL_OUTPUT + ItemBonusesChecks.getFuelProductionBonus(fleet));
            boolean noVolatiles = fleet.getCargo().getCommodityQuantity(Commodities.VOLATILES) < VOLATILES_INPUT;
            boolean tanksFull = FuelProductionLogic.getInstance().areFuelTanksFull(fleet);
            String fuelStatus = workingFormat;
            Color fuelColor = highlightColor;
            if (tanksFull) {
                fuelStatus = tanksAreFullFormat;
                fuelColor = negativeHighlight;
            } else if (noVolatiles) {
                fuelStatus = noInputsFormat;
                fuelColor = negativeHighlight;
            }
            addIncomeLine(tooltip, ProductionType.FUEL_PRODUCTION, fuelIncome, fuelStatus, fuelColor);
        }

        boolean producesMetals = getFleetCapacityForType(fleet, ProductionType.METALS_PRODUCTION) >= 1;
        boolean producesTransplutonics = getFleetCapacityForType(fleet, ProductionType.TRANSPLUTONICS_PRODUCTION) >= 1;
        boolean hasOre = fleet.getCargo().getCommodityQuantity(Commodities.ORE) > ORE_INPUT;
        boolean hasTransplutonicOre = fleet.getCargo().getCommodityQuantity(Commodities.RARE_ORE) > TRANSPLUTONIC_ORE_INPUT;
        boolean producesAllMaterials = (hasOre && producesMetals) && (hasTransplutonicOre && producesTransplutonics);

        if (getFleetCapacityForType(fleet, ProductionType.SUPPLIES_PRODUCTION) >= 1) {
            float suppliesIncome = getFleetCapacityForType(fleet, ProductionType.SUPPLIES_PRODUCTION) *
                    (SUPPLIES_OUTPUT + ItemBonusesChecks.getHeavyIndustryBonus(fleet));
            String suppliesStatus = workingFormat;
            Color suppliesColor = highlightColor;
            boolean hasMetal = fleet.getCargo().getCommodityQuantity(Commodities.METALS) > METALS_INPUT_SUPPLIES;
            boolean hasTransplutonics = fleet.getCargo().getCommodityQuantity(Commodities.RARE_METALS) > TRANSPLUTONICS_INPUT_SUPPLIES;
            boolean hasInputs = hasMetal && hasTransplutonics;
            boolean producesDeficient = (hasMetal && (hasTransplutonicOre && producesTransplutonics))
                    || (hasTransplutonics && (hasOre && producesMetals));
            if (!hasInputs && (producesAllMaterials || producesDeficient)) {
                suppliesStatus = lowInputsFormat;
            } else if (!hasInputs) {
                suppliesStatus = noInputsFormat;
                suppliesColor = negativeHighlight;
            }
            addIncomeLine(tooltip, ProductionType.SUPPLIES_PRODUCTION, suppliesIncome, suppliesStatus, suppliesColor);
        }

        if (getFleetCapacityForType(fleet, ProductionType.MACHINERY_PRODUCTION) > 0) {
            float machineryIncome = getFleetCapacityForType(fleet, ProductionType.MACHINERY_PRODUCTION) *
                    (MACHINERY_OUTPUT + ItemBonusesChecks.getHeavyIndustryBonus(fleet));
            String machineryStatus = workingFormat;
            Color machineryColor = highlightColor;
            boolean hasMetal = fleet.getCargo().getCommodityQuantity(Commodities.METALS) > METALS_INPUT_MACHINERY;
            boolean hasTransplutonics = fleet.getCargo().getCommodityQuantity(Commodities.RARE_METALS) > TRANSPLUTONICS_INPUT_MACHINERY;
            boolean hasInputs = hasMetal && hasTransplutonics;
            boolean producesDeficient = (hasMetal && (hasTransplutonicOre && producesTransplutonics))
                    || (hasTransplutonics && (hasOre && producesMetals));
            if (!hasInputs && (producesAllMaterials || producesDeficient)) {
                machineryStatus = lowInputsFormat;
            } else if (!hasInputs) {
                machineryStatus = noInputsFormat;
                machineryColor = negativeHighlight;
            }
            addIncomeLine(tooltip, ProductionType.MACHINERY_PRODUCTION, machineryIncome, machineryStatus, machineryColor);
        }

        if (getFleetCapacityForType(fleet, ProductionType.HULL_PARTS_PRODUCTION) > 0) {
            HullPartsProductionLogic logic = HullPartsProductionLogic.getInstance();
            float capacityFromModules = getFleetCapacityForType(fleet, ProductionType.HULL_PARTS_PRODUCTION);
            float capacityByRigs = logic.getPossibleAssemblingCapacity();
            float hullPartsIncome = Math.min(capacityFromModules, capacityByRigs) * HULL_PARTS_OUTPUT;
            String hullsStatus = workingFormat;
            Color hullsColor = highlightColor;
            Color hullsIncomeColor = highlightColor;
            if (capacityFromModules > capacityByRigs) {
                hullsIncomeColor = negativeHighlight;
            }
            boolean fleetRosterFull = logic.isPlayerFleetRosterFull();
            boolean hasRigs = logic.hasOperationalSalvageRigs();
            boolean hasMetal = fleet.getCargo().getCommodityQuantity(Commodities.METALS) > METALS_INPUT_HULL_PARTS;
            boolean hasTransplutonics = fleet.getCargo().getCommodityQuantity(Commodities.RARE_METALS) > TRANSPLUTONICS_INPUT_HULL_PARTS;
            boolean hasSupplies = fleet.getCargo().getCommodityQuantity(Commodities.SUPPLIES) > SUPPLIES_INPUT_HULL_PARTS;
            boolean hasMachinery = fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY) > MACHINERY_INPUT_HULL_PARTS;
            boolean hasInputs = hasMetal && hasTransplutonics && hasSupplies && hasMachinery;
            boolean producesDeficient = (hasMetal && (hasTransplutonicOre && producesTransplutonics))
                    || (hasTransplutonics && (hasOre && producesMetals));
            if (fleetRosterFull) {
                hullsStatus = rosterFullFormat;
                hullsColor = negativeHighlight;
            } else if (!hasRigs) {
                hullsStatus = noRigsFormat;
                hullsColor = negativeHighlight;
            } else if (!hasInputs && (producesAllMaterials || producesDeficient)) {
                hullsStatus = lowInputsFormat;
            } else if (!hasInputs) {
                hullsStatus = noInputsFormat;
                hullsColor = negativeHighlight;
            }
            addIncomeLine(tooltip, ProductionType.HULL_PARTS_PRODUCTION, capacityFromModules * HULL_PARTS_OUTPUT,
                    hullsStatus, hullsColor);
            addHullAssemblyLine(tooltip, hullPartsIncome, hullsIncomeColor);
        }
    }

    private static void addIncomeLine (TooltipMakerAPI tooltip, ProductionType type,
                                       float value, String status, Color statusColor) {
        TooltipMakerAPI incomeRow = tooltip.beginImageWithText(null, 24f);
        incomeRow.addImage(TooltipCapacitySection.getCapacitySpriteName(type), 24f, 24f, 0f);
        UIComponentAPI icon = incomeRow.getPrev();
        icon.getPosition().setXAlignOffset(-343f);
        incomeRow.setTextWidthOverride(200f);
        String incomeLine = Misc.getRoundedValueMaxOneAfterDecimal(value) + "×";
        incomeRow.addPara(incomeLine, Misc.getHighlightColor(), 0f).getPosition().rightOfMid(icon, 0f);
        LabelAPI income = (LabelAPI) incomeRow.getPrev();
        income.setAlignment(Alignment.RMID);
        income.getPosition().setXAlignOffset(-130f);
        incomeRow.addPara(PRODUCTION_NAMES.get(type), Misc.getTextColor(), 0f).getPosition().rightOfMid(icon, 75f);
        LabelAPI incomeLabel = (LabelAPI) incomeRow.getPrev();
        incomeLabel.setAlignment(Alignment.LMID);
        incomeRow.addPara(status, statusColor, 0f).getPosition().rightOfMid(icon, 116f);
        LabelAPI incomeStatus = (LabelAPI) incomeRow.getPrev();
        incomeStatus.setAlignment(Alignment.RMID);
        tooltip.addImageWithText(1f);
        tooltip.addSpacer(-47f);
    }

    private static void addHullAssemblyLine(TooltipMakerAPI tooltip, float hullPartsIncome, Color incomeColor) {
        HullPartsProductionLogic logic = HullPartsProductionLogic.getInstance();
        float costValue = logic.computeDesignatedHullCostInParts();
        float hullsOutput = hullPartsIncome / costValue;
        TooltipMakerAPI incomeRow = tooltip.beginImageWithText(null, 24f);
        String hullsIconName = Global.getSettings().getSpriteName("forgprod_ui", "hull_parts_symbolic");
        incomeRow.addImage(hullsIconName, 24f, 24f, 0f);
        UIComponentAPI icon = incomeRow.getPrev();
        icon.getPosition().setXAlignOffset(-343f);
        incomeRow.setTextWidthOverride(200f);
        String incomeLine = Misc.getRoundedValueMaxOneAfterDecimal(hullsOutput) + "×";
        incomeRow.addPara(incomeLine, incomeColor, 0f).getPosition().rightOfMid(icon, 0f);
        LabelAPI income = (LabelAPI) incomeRow.getPrev();
        income.setAlignment(Alignment.RMID);
        income.getPosition().setXAlignOffset(-130f);
        String hulls = "Hulls";
        if (hullPartsIncome <= 1f) {
            hulls = "Hull";
        }
        incomeRow.addPara(hulls, Misc.getTextColor(), 0f).getPosition().rightOfMid(icon, 75f);
        LabelAPI incomeLabel = (LabelAPI) incomeRow.getPrev();
        incomeLabel.setAlignment(Alignment.LMID);
        float accumulated = FleetwideModuleManager.getInstance().getAccumulatedHullParts();
        float progressMultiplier = accumulated / costValue;
        String progressValue = Misc.getRoundedValueMaxOneAfterDecimal(100f * progressMultiplier) + "%";
        incomeRow.addPara("Progress: " + progressValue, Misc.getHighlightColor(),
                0f).getPosition().rightOfMid(icon, 117f);
        LabelAPI incomeStatus = (LabelAPI) incomeRow.getPrev();
        incomeStatus.setAlignment(Alignment.RMID);
        tooltip.addImageWithText(1f);
        tooltip.addSpacer(-47f);
    }

}
