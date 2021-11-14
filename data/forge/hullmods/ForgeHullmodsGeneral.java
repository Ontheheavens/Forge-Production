package data.forge.hullmods;

import java.awt.*;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.forge.plugins.ForgeSettings;

public class ForgeHullmodsGeneral {

    public static final Set<String> ALL_FORGE_HULLMODS = new HashSet<>();
    static
    {
        ALL_FORGE_HULLMODS.add("forge_refinery_module");
        ALL_FORGE_HULLMODS.add("forge_centrifuge_module");
        ALL_FORGE_HULLMODS.add("forge_manufacture_module");
        ALL_FORGE_HULLMODS.add("forge_assembly_module");
    }

    public static Map<ShipAPI.HullSize, Integer> shipSizeEffect = new HashMap<>();
    static {

        shipSizeEffect.put(ShipAPI.HullSize.DEFAULT, 0);
        shipSizeEffect.put(ShipAPI.HullSize.FIGHTER, 0);
        shipSizeEffect.put(ShipAPI.HullSize.FRIGATE, 0);
        shipSizeEffect.put(ShipAPI.HullSize.DESTROYER, 0);
        shipSizeEffect.put(ShipAPI.HullSize.CRUISER, ForgeSettings.CAPACITY_CRUISER);
        shipSizeEffect.put(ShipAPI.HullSize.CAPITAL_SHIP, ForgeSettings.CAPACITY_CAPITAL);

    }

    public static final Map<ShipAPI.HullSize, Float> shipCargoMalus = new HashMap<>();
    static {
        shipCargoMalus.put(ShipAPI.HullSize.DEFAULT, 0f);
        shipCargoMalus.put(ShipAPI.HullSize.FIGHTER, 0f);
        shipCargoMalus.put(ShipAPI.HullSize.FRIGATE, 0f);
        shipCargoMalus.put(ShipAPI.HullSize.DESTROYER, 0f);
        shipCargoMalus.put(ShipAPI.HullSize.CRUISER, 200f);
        shipCargoMalus.put(ShipAPI.HullSize.CAPITAL_SHIP, 400f);
    }

    public static final Map<ShipAPI.HullSize, Float> shipMaintenanceIncrease = new HashMap<>();
    static {
        shipMaintenanceIncrease.put(ShipAPI.HullSize.DEFAULT, 0f);
        shipMaintenanceIncrease.put(ShipAPI.HullSize.FIGHTER, 0f);
        shipMaintenanceIncrease.put(ShipAPI.HullSize.FRIGATE, 0f);
        shipMaintenanceIncrease.put(ShipAPI.HullSize.DESTROYER, 0f);
        shipMaintenanceIncrease.put(ShipAPI.HullSize.CRUISER, 2f);
        shipMaintenanceIncrease.put(ShipAPI.HullSize.CAPITAL_SHIP, 4f);
    }

    public static final Map<ShipAPI.HullSize, Float> skeletonCrewRequirement = new HashMap<>();
    static {
        skeletonCrewRequirement.put(ShipAPI.HullSize.DEFAULT, 0f);
        skeletonCrewRequirement.put(ShipAPI.HullSize.FIGHTER, 0f);
        skeletonCrewRequirement.put(ShipAPI.HullSize.FRIGATE, 0f);
        skeletonCrewRequirement.put(ShipAPI.HullSize.DESTROYER, 0f);
        skeletonCrewRequirement.put(ShipAPI.HullSize.CRUISER, 20f);
        skeletonCrewRequirement.put(ShipAPI.HullSize.CAPITAL_SHIP, 40f);
    }

    public static boolean isForgeHullmodInstallValid(ShipAPI ship) {
        return (isValidSize(ship) &&
                isCivGrade(ship) &&
                isNotModule(ship) &&
                hasCargoCapacity(ship));
    }

    public static boolean isValidSize(ShipAPI ship) {
        return (ship.getHullSize() == ShipAPI.HullSize.CRUISER ||
                ship.getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP);
    }

    public static boolean isCivGrade(ShipAPI ship) {
        return ship.getVariant().hasHullMod(HullMods.CIVGRADE);
    }

    public static boolean isNotModule(ShipAPI ship) {
        return !ship.isStationModule();
    }

    public static boolean hasCargoCapacity(ShipAPI ship) {
        return (ship.getVariant().getHullSpec().getCargo() > shipCargoMalus.get(ship.getHullSize()));
    }

    public static int getShipCapacityMod (ShipAPI ship, ShipAPI.HullSize hullSize) {
        int shipSize = shipSizeEffect.get(hullSize);
        float getCurrentCR = (ship.getFleetMember().getRepairTracker().getCR() / 0.7f);
        return (int) Math.ceil(shipSize * getCurrentCR);
    }

    public static void addCRNoteLine(TooltipMakerAPI tooltip) {

        Color [] thirdLineHighlights = {Misc.getHighlightColor()};

        String crDecrease = ((int)(( ForgeSettings.CR_PRODUCTION_DECAY)*100) + "%");
        String CRNoteLine = " • Refining drains %s CR per day. Ships with low CR have lowered output and lose less CR." ;

        tooltip.addPara(CRNoteLine, 2f, thirdLineHighlights, crDecrease);

    }

    public static void addInactiveLine(TooltipMakerAPI tooltip) {

        Color [] thirdLineHighlights = {Misc.getHighlightColor()};

        String crDecrease = ((int)(( ForgeSettings.CR_PRODUCTION_DECAY)*100) + "%");
        String InactiveLine = " • Ships that are mothballed or do not receive repairs cannot activate their forge modules." ;

        tooltip.addPara(InactiveLine, 2f, thirdLineHighlights, crDecrease);

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
