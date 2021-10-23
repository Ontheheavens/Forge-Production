package data.scripts.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.scripts.abilities.ForgeProduction;
import java.util.HashSet;
import java.util.Set;

public class ForgeHullmodsListener implements EveryFrameScript {

    // Here: Declared constants

    public static final Set<String> ALL_FORGE_HULLMODS = new HashSet<>();

    static {
        ALL_FORGE_HULLMODS.add("forge_refinery_module");
    }

    // Here: Inherited methods

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused() {
        return false;
    }

    public void advance(float amount) {
        ForgeProduction.setUseAllowedByListener(hasForgeShips());
    }

    //Here: Custom methods

    private boolean hasForgeShips() {
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

    private boolean isValid(FleetMemberAPI member) {
        // Forge Hullmods are only applicable to cruisers or capitals, so check only for them
        if (
            !isSize(member, ShipAPI.HullSize.CRUISER) ||
            !isSize(member, ShipAPI.HullSize.CAPITAL_SHIP)
        ) {
            return false;
        }

        // Forge Hullmods are inactive on mothballed ships
        if (member.isMothballed()) {
            return false;
        }
        return true;
    }

    private boolean isForgeShip(FleetMemberAPI member) {
        for (String forgeHullmod : ALL_FORGE_HULLMODS) {
            if (member.getVariant().getHullMods().contains(forgeHullmod)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSize(FleetMemberAPI member, String hullSize) {
        return member.getHullSpec().getHullSize().equals(hullSize);
    }
}
