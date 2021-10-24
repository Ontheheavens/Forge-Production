package data.scripts.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import java.util.HashSet;
import java.util.Set;

public class ForgeShipEligibilityChecker {

    // Here: Declared constants

    public static final Set<String> ALL_FORGE_HULLMODS = new HashSet<>();

    static {
        ALL_FORGE_HULLMODS.add("forge_refinery_module");
    }

    public static boolean hasForgeShips() {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) {
            return false;
        }

        for (FleetMemberAPI member : playerFleet.getMembersWithFightersCopy()) {
            if (!isValid(member)) {
                continue;
            }
            if (isForgeShip(member)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValid(FleetMemberAPI member) {
        // Forge Hullmods are only applicable to cruisers or capitals, so check only for them
        if (
                isSize(member, ShipAPI.HullSize.CRUISER) ||
                isSize(member, ShipAPI.HullSize.CAPITAL_SHIP)
        ) {
            return true;
        }

        // Forge Hullmods are inactive on mothballed ships
        if (member.isMothballed()) {
            return false;
        }
        return false;
    }

    public static boolean isForgeShip(FleetMemberAPI member) {
        for (String forgeHullmod : ALL_FORGE_HULLMODS) {
            if (member.getVariant().getHullMods().contains(forgeHullmod)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSize(FleetMemberAPI member, ShipAPI.HullSize hullSize) {
        return member.getHullSpec().getHullSize().equals(hullSize);
    }

}
