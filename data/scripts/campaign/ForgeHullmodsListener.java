package data.scripts.campaign;

import java.util.Set;
import java.util.HashSet;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.IntervalUtil;

public class ForgeHullmodsListener implements EveryFrameScript {

    //This is how often we check for hullmods
    private final IntervalUtil checkForHullmodsTimer = new IntervalUtil(0.5f, 1f);

    public static final Set<String> ALL_FORGE_HULLMODS = new HashSet<>();
    static
    {
        ALL_FORGE_HULLMODS.add("forge_refinery_module");
    }

    public boolean setVariable()

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused() {
        return false;
    }

    public void advance(float amount) {

        checkForHullmodsTimer.advance(amount);

        if (checkForHullmodsTimer.intervalElapsed()) {

            if (ForgeHullmodsListener.checkForgeShipsInPlayerFleet()) {

                set global variable = 1;

            }

        }

    }

    private static boolean checkForgeShipsInPlayerFleet() {

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) {
            return false;
        }

        // Check later if there's more convenient way to .getMembers

        for (FleetMemberAPI member : playerFleet.getMembersWithFightersCopy()) {

            // Forge Hullmods will be only applicable to cruisers or capitals, so check only for them

            if (member.getHullSpec().getHullSize() != ShipAPI.HullSize.CRUISER ||
                member.getHullSpec().getHullSize() != ShipAPI.HullSize.CAPITAL_SHIP) {
                continue;
            }

            for (String forgeHullmod : ALL_FORGE_HULLMODS) {
                if (member.getVariant().getHullMods().contains(forgeHullmod)) {
                    return true;
                }
            }
        }
        return false;
    }


}