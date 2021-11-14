package data.forge.hullmods;

import java.awt.*;
import java.util.Set;
import java.util.HashSet;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.forge.abilities.conversion.ForgeTypes;
import data.forge.plugins.ForgeSettings;
import static data.forge.hullmods.ForgeHullmodsGeneral.*;

public class ForgeCentrifugeModule extends BaseHullMod {

    private static final Set<String> OTHER_FORGE_HULLMODS = new HashSet<>();
    static
    {
        OTHER_FORGE_HULLMODS.add("forge_refinery_module");
        OTHER_FORGE_HULLMODS.add("forge_manufacture_module");
        OTHER_FORGE_HULLMODS.add("forge_assembly_module");
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {

        if (stats.getVariant().getSMods().contains("forge_centrifuge_module") ||
                stats.getVariant().getHullSpec().isBuiltInMod("forge_centrifuge_module")) {

            float cargoMalus = shipCargoMalus.get(hullSize);
            stats.getCargoMod().modifyFlat(id, -cargoMalus);

            float nullifiedMalus = 0f;
            stats.getSuppliesPerMonth().modifyFlat(id, nullifiedMalus);
            stats.getMinCrewMod().modifyFlat(id, nullifiedMalus);

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

        if (ship != null)
        {
            float pad = 10f;
            tooltip.addSectionHeading("Details", Alignment.MID, pad);

            Color highlightColor = Misc.getHighlightColor();

            //Here: First line

            Color [] firstLineHighlights = {
                    highlightColor, highlightColor, highlightColor, highlightColor
            };

            String volatiles = String.valueOf((int)(ForgeSettings.VOLATILES_TO_CENTRIFUGE * getShipCapacityMod(ship,hullSize)));
            String fuel = String.valueOf((int)(ForgeSettings.FUEL_PRODUCED * getShipCapacityMod(ship,hullSize)));

            String firstLine = " • Centrifuges %s fuel from %s volatiles.";
            String firstLineNoCapacity = " • Centrifuges fuel from volatiles.";

            if (getShipCapacityMod(ship,hullSize) >= 1) {
                tooltip.addPara(firstLine, pad, firstLineHighlights, fuel, volatiles);
            }
            if (getShipCapacityMod(ship,hullSize) < 1) {
                tooltip.addPara(firstLineNoCapacity, pad);
            }

            //Here: Second line

            Color [] secondLineHighlights = {
                    highlightColor, highlightColor
            };

            String heavyMachineryUsage = String.valueOf((int)(ForgeSettings.HEAVY_MACHINERY_CENTRIFUGING_USAGE * getShipCapacityMod(ship,hullSize)));
            String breakdownChance = ((int)(ForgeSettings.BASE_BREAKDOWN_CHANCE * 100) + "%");

            String secondLine = " • Uses %s heavy machinery, with %s chance of breakdown." ;
            String secondLineNoCapacity = " • Uses heavy machinery, with %s chance of breakdown." ;

            if (getShipCapacityMod(ship,hullSize) >= 1) {
                tooltip.addPara(secondLine, 2f, secondLineHighlights, heavyMachineryUsage, breakdownChance);
            }
            if (getShipCapacityMod(ship,hullSize) < 1) {
                tooltip.addPara(secondLineNoCapacity, 2f, highlightColor, breakdownChance);
            }

            //Here: Third line

            addCRNoteLine(tooltip);

            //Here: Fourth line

            addInactiveLine(tooltip);

            //Here: S-Mod lines

            addSModLine(tooltip, ship, isForModSpec, ForgeTypes.CENTRIFUGING) ;

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

        if (ship != null && !isValidSize(ship)) {
            return "Can only be installed on cruisers and capital ships";
        }

        if (ship != null && !isCivGrade(ship)) {
            return "Can only be installed on civilian-grade hulls";
        }

        if (ship != null && !isNotModule(ship)) {
            return "Can not be installed on modules";
        }

        if (ship != null && !hasCargoCapacity(ship)) {
            return "Can not be installed on ships with insufficient cargo space";
        }

        boolean hasForgeModule = false;

        for (String forgeHullmod : OTHER_FORGE_HULLMODS) {
            if (ship != null && ship.getVariant().getHullMods().contains(forgeHullmod)) {
                hasForgeModule = true;
            }
        }

        if (hasForgeModule) {
            return "Can only install one module per Forge Ship";
        }

        return null;
    }

}
