package data.forge.hullmods.support;

import java.awt.*;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.forge.abilities.conversion.support.ForgeTypes;
import data.forge.campaign.ForgeConditionChecker;
import data.forge.plugins.ForgeSettings;

import static data.forge.hullmods.support.ForgeHullmodsGeneral.getShipCapacityWithCR;

public class ForgeHullmodsTooltip {

    public static void addRequirementsLines(TooltipMakerAPI tooltip) {
        String hullsizeRequirement;
        if (ForgeSettings.ENABLE_CIVGRADE_REQUIREMENT) {
            hullsizeRequirement = "Installable only on cruisers or capital ships with civilian-grade hull.";
        } else {
            hullsizeRequirement = "Installable only on cruisers or capital ships.";
        }
        tooltip.addPara(hullsizeRequirement, 6f);

        if (!ForgeSettings.ENABLE_CARGO_REQUIREMENT) {
            String cargoRequirementNullified = "Cargo requirements nullified.";
            tooltip.addPara(cargoRequirementNullified, Misc.getPositiveHighlightColor(), 2f);
        }
    }

    public static void addShipStateNote(TooltipMakerAPI tooltip, ShipAPI ship, ShipAPI.HullSize hullSize, String forgeType) {

        boolean isInstalled = ship.getVariant().hasHullMod(forgeType);
        boolean isOperational = ForgeConditionChecker.isOperational(ship);
        boolean hasMachinery = ForgeConditionChecker.hasMinimumMachinery();
        boolean hasCapacity = getShipCapacityWithCR(ship, hullSize) >= 1;

        String indent = "    ";

        Color stateHighlightColor = Misc.getHighlightColor();
        Color stateTextColor = Misc.getTextColor();

        String stateHighlight = "Not installed:" ;
        if (isInstalled && !isOperational) {
            stateHighlight = "Module inactive:" ;
        }
        if (isInstalled && isOperational) {
            stateHighlight = "Module active:" ;
        }

        String shipStateLine = "%s showing default values." ;
        if (isInstalled && !isOperational) {
            shipStateLine = "%s showing default values." ;
        }
        if (isInstalled && isOperational) {
            shipStateLine = "%s showing current values." ;
        }
        if (isInstalled && isOperational && !hasMachinery && hasCapacity) {
            stateTextColor = Misc.getNegativeHighlightColor();
            shipStateLine = "%s insufficient machinery." ;
        }
        if (isInstalled && isOperational && hasMachinery && !hasCapacity) {
            stateTextColor = Misc.getNegativeHighlightColor();
            shipStateLine = "%s low CR." ;
        }
        if (isInstalled && isOperational && !hasMachinery && !hasCapacity) {
            stateTextColor = Misc.getNegativeHighlightColor();
            shipStateLine = "%s low CR." ;
        }

        tooltip.setBulletedListMode(indent);
        tooltip.addPara(shipStateLine, 10f, stateTextColor, stateHighlightColor, stateHighlight);
        tooltip.setBulletedListMode(null);

    }

    public static void addMachineryLine(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship,
                                        ForgeTypes.Conversion type, String forgeType) {

        Color highlightColor = Misc.getHighlightColor();

        Color [] secondLineHighlights = {
                highlightColor, highlightColor
        };

        boolean isOperational = ForgeConditionChecker.isOperational(ship);
        boolean hasMachinery = ForgeConditionChecker.hasMinimumMachinery();
        boolean isInstalled = ship.getVariant().hasHullMod(forgeType);
        boolean hasCapacity = ForgeHullmodsGeneral.getShipCapacityMod(ship, hullSize, type) >= 1;

        int shipMachineryModifier = ForgeHullmodsGeneral.getShipMachineryMod(ship,hullSize);

        if (!isOperational || !isInstalled) {
            shipMachineryModifier = ForgeHullmodsGeneral.shipSizeEffect.get(hullSize);
        }

        float heavyMachineryUsageValue = ForgeTypes.MACHINERY_USAGE.get(type) * shipMachineryModifier;
        String heavyMachineryUsage = String.valueOf((int)Math.floor(heavyMachineryUsageValue));
        if (heavyMachineryUsageValue < 1) {
            heavyMachineryUsage = String.valueOf((int)Math.ceil(heavyMachineryUsageValue));
        }

        String breakdownChance = ((int)(ForgeSettings.BASE_BREAKDOWN_CHANCE * 100) + "%");

        String secondLine = "Uses %s heavy machinery, with %s chance of breakdown." ;
        String secondLineNoCapacity = "Uses heavy machinery, with %s chance of breakdown." ;

        if (!isOperational) {
            tooltip.addPara(secondLine, 2f, secondLineHighlights, heavyMachineryUsage, breakdownChance);
        }
        if (isOperational && hasCapacity && hasMachinery) {
            tooltip.addPara(secondLine, 2f, secondLineHighlights, heavyMachineryUsage, breakdownChance);
        }
        if (isOperational && (!hasCapacity || !hasMachinery)) {
            tooltip.addPara(secondLineNoCapacity, 2f, highlightColor, breakdownChance);
        }

    }

    public static void addCRNoteLine(TooltipMakerAPI tooltip) {

        Color [] thirdLineHighlights = {Misc.getHighlightColor()};

        String crDecrease = ((int)(( ForgeSettings.CR_PRODUCTION_DECAY)*100) + "%");
        String CRNoteLine = "Refining drains %s CR per day. Ships with low CR have lowered output and lose less CR." ;

        tooltip.addPara(CRNoteLine, 2f, thirdLineHighlights, crDecrease);

    }

    public static void addInactiveLine(TooltipMakerAPI tooltip) {

        Color [] fourthLineHighlights = {Misc.getHighlightColor()};
        String CR = "10%";
        String InactiveLine = "Forge module cannot be activated if ship is mothballed, does not receive repairs or has less than %s CR.";

        tooltip.addPara(InactiveLine, 2f, fourthLineHighlights, CR);

    }

    public static void addSModLine(TooltipMakerAPI tooltip, ShipAPI ship, boolean isForModSpec, String forgeType) {

        if (isForModSpec) {
            tooltip.addPara("If this hullmod is built in, additional crew requirement and maintenance cost increase are nullified.", Misc.getGrayColor(), 10f);
        } else if (ship.getVariant().getSMods().contains(forgeType)) {
            tooltip.addPara("S-mod Bonus: Additional crew requirement and maintenance cost increase are nullified.", Misc.getPositiveHighlightColor(), 10f);
        } else if (ship.getHullSpec().isBuiltInMod(forgeType)) {
            tooltip.addPara("Built-in Bonus: Additional crew requirement and maintenance cost increase are nullified.", Misc.getPositiveHighlightColor(), 10f);
        } else {
            tooltip.addPara("If this hullmod is built in, additional crew requirement and maintenance cost increase are nullified.", Misc.getGrayColor(), 10f);
        }

    }

}
