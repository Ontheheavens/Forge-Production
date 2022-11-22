package data.forge.hullmods.support;

import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;

import data.forge.abilities.conversion.support.ForgeConversionGeneral;
import data.forge.abilities.conversion.support.ForgeTypes;
import data.forge.campaign.ForgeConditionChecker;
import data.forge.plugins.ForgeSettings;

public class ForgeHullmodsGeneral {

    public static final String REASON_WRONG_HULLSIZE = "Can only be installed on cruisers and capital ships";
    public static final String REASON_NOT_CIVGRADE = "Can only be installed on civilian-grade hulls";
    public static final String REASON_IS_MODULE = "Can not be installed on modules";
    public static final String REASON_NO_CARGO_SPACE = "Can not be installed on ships with insufficient cargo space";
    public static final String REASON_ALREADY_HAS = "Can only install one Forge module per ship";

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
        return (ForgeConditionChecker.isValidHullsize(ship) &&
                isCivGrade(ship) &&
                isNotModule(ship) &&
                hasCargoCapacity(ship));
    }

    public static boolean isCivGrade(ShipAPI ship) {
        if (!ForgeSettings.ENABLE_CIVGRADE_REQUIREMENT) {
            return true;
        }
        boolean hasCivHullmod = ship.getVariant().hasHullMod(HullMods.CIVGRADE);
        boolean hasArcheanCivHullmod = ship.getVariant().hasHullMod("archeus_civgrade");
        return (hasCivHullmod || hasArcheanCivHullmod);
    }

    public static boolean isNotModule(ShipAPI ship) {
        return !ship.isStationModule();
    }

    public static boolean hasCargoCapacity(ShipAPI ship) {
        if (!ForgeSettings.ENABLE_CARGO_REQUIREMENT) {
            return true;
        }
        return (ship.getVariant().getHullSpec().getCargo() > shipCargoMalus.get(ship.getHullSize()));
    }

    public static int getShipCapacityMod (ShipAPI ship, ShipAPI.HullSize hullSize, ForgeTypes.Conversion type) {
        int shipSize = shipSizeEffect.get(hullSize);
        float getCurrentCR = (ship.getFleetMember().getRepairTracker().getCR() / 0.7f);
        return (int) Math.floor(Math.min((int) Math.floor(shipSize * getCurrentCR), getShipMachineryCycles(ship, hullSize, type)));
    }

    public static int getShipCapacityWithCR (ShipAPI ship, ShipAPI.HullSize hullSize) {
        int shipSize = shipSizeEffect.get(hullSize);
        float getCurrentCR = (ship.getFleetMember().getRepairTracker().getCR() / 0.7f);
        return (int) Math.floor(shipSize * getCurrentCR);
    }

    public static int getShipMachineryMod (ShipAPI ship, ShipAPI.HullSize hullSize) {
        int shipSize = shipSizeEffect.get(hullSize);
        float getCurrentCR = (ship.getFleetMember().getRepairTracker().getCR() / 0.7f);
        return (int) Math.ceil((int) Math.floor(shipSize * getCurrentCR) * ForgeConversionGeneral.getMachineryAvailability());
    }

    public static int getShipMachineryCycles (ShipAPI ship, ShipAPI.HullSize hullSize, ForgeTypes.Conversion type) {
        int shipSize = shipSizeEffect.get(hullSize);
        float getCurrentCR = (ship.getFleetMember().getRepairTracker().getCR() / 0.7f);
        int shipCapacity = (int) Math.floor(shipSize * getCurrentCR);
        float machineryAvailable = (shipCapacity * ForgeTypes.MACHINERY_USAGE.get(type)) * ForgeConversionGeneral.getMachineryAvailability();
        float machineryCycles = machineryAvailable / ForgeTypes.MACHINERY_USAGE.get(type);
        return (int) Math.floor(machineryCycles);
    }

}
