package data.forge.hullmods;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.forge.abilities.conversion.ForgeTypes;
import data.forge.plugins.ForgeSettings;

import static data.forge.hullmods.ForgeHullmodsGeneral.*;

public class ForgeAssemblyModule extends BaseHullMod {

    private static final Set<String> OTHER_FORGE_HULLMODS = new HashSet<>();
    static
    {
        OTHER_FORGE_HULLMODS.add("forge_refinery_module");
        OTHER_FORGE_HULLMODS.add("forge_centrifuge_module");
        OTHER_FORGE_HULLMODS.add("forge_manufacture_module");
    }

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {

        if (stats.getVariant().getSMods().contains("forge_assembly_module") ||
                stats.getVariant().getHullSpec().isBuiltInMod("forge_assembly_module")) {

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
                    highlightColor, highlightColor, highlightColor
            };

            String heavyMachinery = String.valueOf(((int)(ForgeSettings.HEAVY_MACHINERY_PRODUCED) * getShipCapacityMod(ship,hullSize)));
            String metal = String.valueOf(((int)(ForgeSettings.METAL_TO_ASSEMBLE) * getShipCapacityMod(ship,hullSize)));
            String transplutonics = String.valueOf(((int)(ForgeSettings.TRANSPLUTONICS_TO_ASSEMBLE) * getShipCapacityMod(ship,hullSize)));

            String firstLine = " • Assembles %s heavy machinery from %s metal and %s transplutonics.";

            tooltip.addPara(firstLine, pad, firstLineHighlights, heavyMachinery, metal, transplutonics);

            //Here: Second line

            Color [] secondLineHighlights = {
                    highlightColor, highlightColor
            };

            String heavyMachineryUsage = String.valueOf((int)(ForgeSettings.HEAVY_MACHINERY_ASSEMBLING_USAGE) * getShipCapacityMod(ship,hullSize));
            String breakdownChance = ((int)((float) ForgeSettings.BASE_BREAKDOWN_CHANCE * 100) + "%");

            String secondLine = " • Uses %s heavy machinery, with %s chance of breakdown." ;

            tooltip.addPara(secondLine, 2f, secondLineHighlights, heavyMachineryUsage, breakdownChance);

            //Here: Third line

            addCRNoteLine(tooltip);

            //Here: Fourth line

            addInactiveLine(tooltip);

            //Here: S-Mod lines

            addSModLine(tooltip, ship, isForModSpec, ForgeTypes.ASSEMBLING) ;

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
