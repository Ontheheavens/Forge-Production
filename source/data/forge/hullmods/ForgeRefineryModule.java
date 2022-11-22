package data.forge.hullmods;

import java.awt.*;
import java.util.Set;
import java.util.HashSet;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.util.Misc;

import data.forge.abilities.conversion.support.ForgeTypes;
import data.forge.campaign.ForgeConditionChecker;
import data.forge.hullmods.support.ForgeHullmodsTooltip;
import data.forge.plugins.ForgeSettings;

import static data.forge.hullmods.support.ForgeHullmodsGeneral.*;

public class ForgeRefineryModule extends BaseHullMod {

    private static final Set<String> OTHER_FORGE_HULLMODS = new HashSet<>();
    static
    {
        OTHER_FORGE_HULLMODS.add("forge_centrifuge_module");
        OTHER_FORGE_HULLMODS.add("forge_manufacture_module");
        OTHER_FORGE_HULLMODS.add("forge_assembly_module");
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {

        if (stats.getVariant().getSMods().contains("forge_refinery_module") ||
            stats.getVariant().getHullSpec().isBuiltInMod("forge_refinery_module")) {

            float cargoMalus = shipCargoMalus.get(hullSize);
            stats.getCargoMod().modifyFlat(id, -cargoMalus);

            float nullifiedMalus = 0f;
            stats.getSuppliesPerMonth().modifyFlat(id, -nullifiedMalus);
            stats.getMinCrewMod().modifyFlat(id, -nullifiedMalus);

        }
        else {

            float cargoMalus = shipCargoMalus.get(hullSize);
            stats.getCargoMod().modifyFlat(id, -cargoMalus);

            float maintenanceIncrease = shipMaintenanceIncrease.get(hullSize);
            stats.getSuppliesPerMonth().modifyFlat(id, maintenanceIncrease);

            float skeletonCrew = skeletonCrewRequirement.get(hullSize);
            stats.getMinCrewMod().modifyFlat(id, skeletonCrew);

        }
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (shipCargoMalus.get(ShipAPI.HullSize.CRUISER)).intValue();
        if (index == 1) return "" + (shipCargoMalus.get(ShipAPI.HullSize.CAPITAL_SHIP)).intValue();
        if (index == 2) return "" + (skeletonCrewRequirement.get(ShipAPI.HullSize.CRUISER)).intValue();
        if (index == 3) return "" + (skeletonCrewRequirement.get(ShipAPI.HullSize.CAPITAL_SHIP)).intValue();
        if (index == 4) return "" + (shipMaintenanceIncrease.get(ShipAPI.HullSize.CRUISER)).intValue();
        if (index == 5) return "" + (shipMaintenanceIncrease.get(ShipAPI.HullSize.CAPITAL_SHIP)).intValue();
        return null;
    }

    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {

        ForgeHullmodsTooltip.addRequirementsLines(tooltip);

        if (ship != null)
        {
            float pad = 10f;
            tooltip.addSectionHeading("Details", Alignment.MID, pad);

            Color highlightColor = Misc.getHighlightColor();

            boolean isOperational = ForgeConditionChecker.isOperational(ship);
            boolean hasMachinery = ForgeConditionChecker.hasMinimumMachinery();
            boolean isInstalled = ship.getVariant().hasHullMod(ForgeTypes.REFINERY);
            boolean hasCapacity = getShipCapacityWithCR(ship, hullSize) >= 1;

            int shipModifier = getShipCapacityMod(ship,hullSize, ForgeTypes.Conversion.REFINING);

            if (!isOperational || !isInstalled) {
                shipModifier = shipSizeEffect.get(hullSize);
            }

            ForgeHullmodsTooltip.addShipStateNote(tooltip, ship, hullSize, ForgeTypes.REFINERY);

            tooltip.setBulletedListMode(" • ");

            // Here: First line

            Color [] firstLineHighlights = {
                    highlightColor, highlightColor, highlightColor, highlightColor
            };

            String ore = String.valueOf((int)Math.ceil(ForgeSettings.ORE_TO_REFINE * shipModifier));
            String metal = String.valueOf((int)Math.floor(ForgeSettings.METAL_PRODUCED * shipModifier));
            String transplutonicOre = String.valueOf((int)Math.ceil(ForgeSettings.TRANSPLUTONIC_ORE_TO_REFINE * shipModifier));

            float transplutonicsValue = ForgeSettings.TRANSPLUTONICS_PRODUCED * shipModifier;
            String transplutonics = String.valueOf((int)Math.floor(transplutonicsValue));
            if (transplutonicsValue < 1) {
                transplutonics = String.valueOf((int)Math.ceil(transplutonicsValue));
            }

            String firstLine = "Refines %s metal from %s ore and %s transplutonics from %s transplutonic ore.";
            String firstLineNoCapacity = "Refines metal from ore and transplutonics from transplutonic ore.";

            if (!isOperational) {
                tooltip.addPara(firstLine, 4f, firstLineHighlights,
                        metal, ore, transplutonics, transplutonicOre);
            }
            if (isOperational && hasCapacity && hasMachinery) {
                tooltip.addPara(firstLine, 4f, firstLineHighlights,
                        metal, ore, transplutonics, transplutonicOre);
            }
            if (isOperational && (!hasCapacity || !hasMachinery)) {
                tooltip.addPara(firstLineNoCapacity, 4f);
            }

            // Here: Second line

            ForgeHullmodsTooltip.addMachineryLine(tooltip, hullSize, ship, ForgeTypes.Conversion.REFINING,  ForgeTypes.REFINERY);

            // Here: Third line

            ForgeHullmodsTooltip.addCRNoteLine(tooltip);

            // Here: Fourth line

            ForgeHullmodsTooltip.addInactiveLine(tooltip);

            tooltip.setBulletedListMode(null);

            // Here: S-Mod lines

            ForgeHullmodsTooltip.addSModLine(tooltip, ship, isForModSpec, ForgeTypes.REFINERY) ;

        }

    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {

        boolean hasForgeModule = false;

        for (String forgeHullmod : OTHER_FORGE_HULLMODS) {
            if(ship.getVariant().getHullMods().contains(forgeHullmod)) {
                hasForgeModule = true;
            }
        }

        return (isForgeHullmodInstallValid(ship) && !hasForgeModule);

    }

    public String getUnapplicableReason(ShipAPI ship) {
        if (ship != null && !ForgeConditionChecker.isValidHullsize(ship)) {
            return REASON_WRONG_HULLSIZE;
        }
        if (ship != null && !isCivGrade(ship) && ForgeSettings.ENABLE_CIVGRADE_REQUIREMENT) {
            return REASON_NOT_CIVGRADE;
        }
        if (ship != null && !isNotModule(ship)) {
            return REASON_IS_MODULE;
        }
        if (ship != null && !hasCargoCapacity(ship) && ForgeSettings.ENABLE_CARGO_REQUIREMENT) {
            return REASON_NO_CARGO_SPACE;
        }
        boolean hasForgeModule = false;
        for (String forgeHullmod : OTHER_FORGE_HULLMODS) {
            if (ship != null && ship.getVariant().getHullMods().contains(forgeHullmod)) {
                hasForgeModule = true;
            }
        }
        if (hasForgeModule) {
            return REASON_ALREADY_HAS;
        }
        return null;
    }

}
