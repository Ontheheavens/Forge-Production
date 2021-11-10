package data.scripts.hullmods;

import java.awt.*;
import java.util.Set;
import java.util.HashSet;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.plugins.ForgeSettings;

import static data.scripts.hullmods.ForgeHullmodsGeneral.*;

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

            int shipSize = shipSizeEffect.get(hullSize);
            float getCurrentCR = (ship.getFleetMember().getRepairTracker().getCR() / 0.7f);
            int shipModifier = (int) Math.ceil(shipSize * getCurrentCR);

            Color highlightColor = Misc.getHighlightColor();

            //Here: First line

            Color [] firstLineHighlights = {
                    highlightColor, highlightColor, highlightColor, highlightColor
            };

            String volatiles = String.valueOf(((int)(ForgeSettings.VOLATILES_TO_CENTRIFUGE) * shipModifier));
            String fuel = String.valueOf(((int)(ForgeSettings.FUEL_PRODUCED) * shipModifier));

            String firstLine = " • Centrifuges %s Volatiles into %s Fuel.";

            tooltip.addPara(firstLine, pad, firstLineHighlights, volatiles, fuel);

            //Here: Second line

            Color [] secondLineHighlights = {
                    highlightColor, highlightColor
            };

            String heavyMachineryUsage = String.valueOf((int)(ForgeSettings.HEAVY_MACHINERY_CENTRIFUGING_USAGE) * shipModifier);
            String breakdownChance = ((int)((float) ForgeSettings.BASE_BREAKDOWN_CHANCE * 100) + "%");

            String secondLine = " • Uses %s Heavy Machinery, with %s chance of breakdown." ;

            tooltip.addPara(secondLine, 2f, secondLineHighlights, heavyMachineryUsage, breakdownChance);

            //Here: Third line

            Color [] thirdLineHighlights = {highlightColor};

            String crDecrease = ((int)(((float) ForgeSettings.CR_PRODUCTION_DECAY)*100) + "%");
            String thirdLine = " • Refining drains %s CR per day. Ships with low CR have lowered output and lose less CR." ;

            tooltip.addPara(thirdLine, 2f, thirdLineHighlights, crDecrease);

            //Here: S-Mod lines

            if (isForModSpec) {
                tooltip.addPara("If this hullmod is built in, additional crew requirement and maintenance cost increase are nullified.", Misc.getGrayColor(), 10f);
            } else if (ship.getVariant().getSMods().contains("forge_centrifuge_module")) {
                tooltip.addPara("S-mod Bonus: Additional crew requirement and maintenance cost increase are nullified.", Misc.getPositiveHighlightColor(), 10f);
            } else if (ship.getHullSpec().isBuiltInMod("forge_centrifuge_module")) {
                tooltip.addPara("Built-in Bonus: Additional crew requirement and maintenance cost increase are nullified.", Misc.getPositiveHighlightColor(), 10f);
            } else {
                tooltip.addPara("If this hullmod is built in, additional crew requirement and maintenance cost increase are nullified.", Misc.getGrayColor(), 10f);
            }

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

        if (ship != null &&
                ship.getHullSize() != ShipAPI.HullSize.CAPITAL_SHIP &&
                ship.getHullSize() != ShipAPI.HullSize.CRUISER) {
            return "Can only be installed on cruisers and capital ships";
        }

        if (ship != null &&
                !ship.getVariant().hasHullMod(HullMods.CIVGRADE)) {
            return "Can only be installed on civilian-grade hulls";
        }

        if (ship != null && ship.isStationModule()) {
            return "Can not be installed on modules";
        }

        if (ship != null && (ship.getVariant().getHullSpec().getCargo() < shipCargoMalus.get(ship.getHullSize()))) {
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
