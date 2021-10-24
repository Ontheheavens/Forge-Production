package data.scripts.abilities;

import java.awt.Color;

import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility;
import com.fs.starfarer.api.util.Misc;

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

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {

        Color highlight = Misc.getHighlightColor();

        String status = " (off)";
        if (turnedOn) {
            status = " (on)";
        }

        LabelAPI title = tooltip.addTitle(spec.getName() + status);
        title.highlightLast(status);
        title.setHighlightColor(highlight);

        float pad = 10f;
        tooltip.addPara("Control forge production of your fleet.", pad);

    }

    //Here: Custom methods

    public static void setUseAllowedByListener (boolean useAllowed) {
        useAllowedByListener = useAllowed;
    }

}