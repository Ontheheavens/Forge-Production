package data.scripts.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.util.IntervalUtil;

public class RefineryProductionListener implements EveryFrameScript {

    // Here: Declared constants

    private final IntervalUtil productionTimer = new IntervalUtil(0.9f,1.1f); // In days

    // Here: Inherited methods

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused() {
        return false;
    }

    public void advance(float amount) {

        float days = Global.getSector().getClock().convertToDays(amount);

        productionTimer.advance(days);

        if (productionTimer.intervalElapsed()) {

            CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

            FleetMemberAPI member = (FleetMemberAPI) playerFleet.getFleetData().getMembersListCopy();

        }

    }
}