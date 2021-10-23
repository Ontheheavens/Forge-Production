package data.scripts.campaign;

import java.util.Set;
import java.util.HashSet;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.IntervalUtil;

import data.scripts.abilities.ForgeProduction;

public class ForgeHullmodsListener implements EveryFrameScript {

    // Here: Declared constants

    private final IntervalUtil checkForHullmodsTimer = new IntervalUtil(0.5f, 1f);

    public static final Set<String> ALL_FORGE_HULLMODS = new HashSet<>();
    static
    {
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

        checkForHullmodsTimer.advance(amount);

        if (checkForHullmodsTimer.intervalElapsed()) {

            if (ForgeHullmodsListener.checkForgeShipsInPlayerFleet()) {

                ForgeProduction.setUseAllowedByListener(true);

            }

        }

    }

    //Here: Custom methods

    private static boolean checkForgeShipsInPlayerFleet() {

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) {
            return false;
        }

        for (FleetMemberAPI member : playerFleet.getMembersWithFightersCopy()) {

            // Forge Hullmods are only applicable to cruisers or capitals, so check only for them
            if (member.getHullSpec().getHullSize() != ShipAPI.HullSize.CRUISER ||
                member.getHullSpec().getHullSize() != ShipAPI.HullSize.CAPITAL_SHIP) {
                continue;
            }

            // Forge Hullmods are inactive on mothballed ships
            if (member.isMothballed()) {
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
