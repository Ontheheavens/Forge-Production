package forgprod.hullmods.checks;

import java.util.HashSet;
import java.util.Set;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import forgprod.abilities.conversion.support.ProductionConstants;

/**
 * @author Ontheheavens
 * @since 23.11.2021
 */
public class HullmodStateChecks {

    public static boolean hasOtherModules(ShipAPI ship, String module) {
        boolean hasForgeModule = false;
        HashSet<String> modulesToCheck = new HashSet<>(ProductionConstants.ALL_HULLMODS);
        modulesToCheck.remove(module);
        for (String forgeHullmod : modulesToCheck) {
            if (ship != null && ship.getVariant().getHullMods().contains(forgeHullmod)) {
                hasForgeModule = true;
            }
        }
        return hasForgeModule;
    }

    public static boolean isForgeHullmodInstallValid(ShipAPI ship) {
        return (isValidHullsize(ship.getHullSpec()) && !isModule(ship));
    }

    // Needs a hacky workaround due to the way Alex codes things.
    public static boolean isModule(ShipAPI ship) {
        return ship.getVariant().getHints().contains(ShipHullSpecAPI.ShipTypeHints.UNBOARDABLE) ||
               ship.getVariant().getHints().contains(ShipHullSpecAPI.ShipTypeHints.UNDER_PARENT) ||
               ship.isStationModule() || (ship.getParentStation() != null);
    }

    public static boolean isValidHullsize(ShipHullSpecAPI hullSpec) {
        Set<ShipAPI.HullSize> validSizes = new HashSet<>();
        validSizes.add(ShipAPI.HullSize.CRUISER);
        validSizes.add(ShipAPI.HullSize.CAPITAL_SHIP);
        for (ShipAPI.HullSize size : validSizes) {
            if (hullSpec.getHullSize().equals(size)) {
                return true;
            }
        }
        return false;
    }

    public static CampaignFleetAPI getFleetOfShip(ShipAPI ship) {
        if (ship == null) return null;
        if (ship.getFleetMember() == null) return null;
        FleetMemberAPI member = ship.getFleetMember();
        if (member.getFleetData() == null) return null;
        FleetDataAPI data = member.getFleetData();
        if (data.getFleet() == null) return null;
        return data.getFleet();
    }

}
