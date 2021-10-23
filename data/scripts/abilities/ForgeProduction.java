package data.scripts.abilities;

import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility;

import data.scripts.campaign.ForgeHullmodsListener;

public class ForgeProduction extends BaseToggleAbility {

    protected void activateImpl() { }

    protected void applyEffect(float amount, float level) { }

    protected void deactivateImpl() { cleanupImpl(); }

    protected void cleanupImpl() { }



    @Override
    public boolean isUsable() {
        if (!isActivateCooldown &&
                getProgressFraction() > 0 && getProgressFraction() < 1 &&
                getDeactivationDays() > 0) return false;

        if (!ForgeHullmodsListener.checkForgeShipsInPlayerFleet())
            return false;

        return super.isUsable();
    }

}