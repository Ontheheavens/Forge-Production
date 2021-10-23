package data.scripts.abilities;

import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility;

public class ForgeProduction extends BaseToggleAbility {

    // Here: Declared constants

    private static boolean useAllowedByListener = false;

    // Here: Inherited methods

    protected void activateImpl() { }

    protected void applyEffect(float amount, float level) { }

    protected void deactivateImpl() { cleanupImpl(); }

    protected void cleanupImpl() { }

    @Override
    public boolean isUsable() {
        if (!isActivateCooldown &&
                getProgressFraction() > 0 && getProgressFraction() < 1 &&
                getDeactivationDays() > 0) return false;

        if (!useAllowedByListener)
            return false;

        return super.isUsable();
    }

    //Here: Custom methods

    public static void setUseAllowedByListener (boolean useAllowed) {
        useAllowedByListener = useAllowed;
    }

}