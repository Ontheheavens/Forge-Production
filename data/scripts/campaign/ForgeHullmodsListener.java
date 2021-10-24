package data.scripts.campaign;

import com.fs.starfarer.api.EveryFrameScript;

import data.scripts.abilities.ForgeProduction;

public class ForgeHullmodsListener implements EveryFrameScript {

    // Here: Inherited methods

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused() {
        return false;
    }

    public void advance(float amount) {
        ForgeProduction.setUseAllowedByListener(ForgeShipEligibilityChecker.hasForgeShips());
    }

}
