package data.scripts.hullmods;

import java.util.Map;
import java.util.HashMap;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;

public class ForgeHullmodsGeneral {

    public static final Map<ShipAPI.HullSize, Integer> shipSizeEffect = new HashMap<>();
    static {

        shipSizeEffect.put(ShipAPI.HullSize.DEFAULT, 0);
        shipSizeEffect.put(ShipAPI.HullSize.FIGHTER, 0);
        shipSizeEffect.put(ShipAPI.HullSize.FRIGATE, 0);
        shipSizeEffect.put(ShipAPI.HullSize.DESTROYER, 0);
        shipSizeEffect.put(ShipAPI.HullSize.CRUISER, 4);
        shipSizeEffect.put(ShipAPI.HullSize.CAPITAL_SHIP, 8);

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
        return ((ship.getHullSize() == ShipAPI.HullSize.CRUISER ||
                 ship.getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP) &&
                 ship.getVariant().hasHullMod(HullMods.CIVGRADE) &&
               (!ship.isStationModule()) &&
                (ship.getVariant().getHullSpec().getCargo() > shipCargoMalus.get(ship.getHullSize())));
    }

}
