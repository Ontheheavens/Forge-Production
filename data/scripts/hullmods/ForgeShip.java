package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;

import java.util.Map;
import java.util.HashMap;

public class ForgeShip extends BaseHullMod {

    private static final Map<ShipAPI.HullSize, Float> shipCargoMalus = new HashMap<>();
    static {
        shipCargoMalus.put(ShipAPI.HullSize.DESTROYER, 50f);
        shipCargoMalus.put(ShipAPI.HullSize.CRUISER, 200f);
        shipCargoMalus.put(ShipAPI.HullSize.CAPITAL_SHIP, 400f);
    }

    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        float cargoMalus = shipCargoMalus.get(hullSize);
        stats.getCargoMod().modifyFlat(id, -cargoMalus);
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (shipCargoMalus.get(ShipAPI.HullSize.DESTROYER)).intValue();
        if (index == 1) return "" + (shipCargoMalus.get(ShipAPI.HullSize.CRUISER)).intValue();
        if (index == 2) return "" + (shipCargoMalus.get(ShipAPI.HullSize.CAPITAL_SHIP)).intValue();
        return null;
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return ((ship.getHullSize() == ShipAPI.HullSize.DESTROYER ||
                ship.getHullSize() == ShipAPI.HullSize.CRUISER ||
                ship.getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP) &&
                ship.getVariant().hasHullMod(HullMods.CIVGRADE) &&
                (!ship.isStationModule()));
    }

    public String getUnapplicableReason(ShipAPI ship) {
        if (ship != null &&
            ship.getHullSize() != ShipAPI.HullSize.CAPITAL_SHIP &&
            ship.getHullSize() != ShipAPI.HullSize.CAPITAL_SHIP &&
            ship.getHullSize() != ShipAPI.HullSize.CRUISER) {
            return "Can only be installed on destroyers, cruisers and capital ships";
        }

        if (ship != null &&
            !ship.getVariant().hasHullMod(HullMods.CIVGRADE)) {
            return "Can only be installed on civilian-grade hulls";
        }

        if (ship != null && ship.isStationModule()) {
            return "Can not be installed on modules";
        }

        return null;
    }

}
