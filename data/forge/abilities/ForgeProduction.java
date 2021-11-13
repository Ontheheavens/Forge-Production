package data.forge.abilities;

import java.awt.Color;

import data.forge.abilities.conversion.*;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility;

import data.forge.campaign.ForgeProductionReport;

import static data.forge.campaign.ForgeConditionChecker.*;
import static data.forge.plugins.ForgeSettings.*;
import static data.forge.abilities.conversion.ForgeConversionVariables.*;

public class ForgeProduction extends BaseToggleAbility {

    // Here: Declared constants

    private static final Vector2f ZERO = new Vector2f();

    private int notificationCounter = 0;

    private final IntervalUtil productionInterval = new IntervalUtil(1f,1f);

    // Here: Inherited methods

    protected void activateImpl() { }

    protected void applyEffect(float amount, float level) {

        CampaignFleetAPI fleet = getFleet();

        if (fleet == null) return;
        if (!isActive()) return;

        float days = Global.getSector().getClock().convertToDays(amount);

        productionInterval.advance(days);

        if (productionInterval.intervalElapsed()) {

            ForgeRefiningLogic.startRefining(fleet);

            ForgeCentrifugingLogic.startCentrifuging(fleet);

            ForgeManufacturingLogic.startManufacturing(fleet);

            ForgeAssemblingLogic.startAssembling(fleet);

            if (goodsWereForged ()) {

                fleet.addFloatingText("Forged goods", Misc.setAlpha(Misc.getTextColor(), 255), 0.5f);

                if(PLAY_SOUND_NOTIFICATION) {
                    Global.getSoundPlayer().playSound("ui_cargo_raremetals", 1f, 1f, fleet.getLocation(), ZERO);
                }

                clearGoodsStatus ();
            }

            if (SHOW_NOTIFICATION && goodsProducedReport ()) {
                notificationCounter += 1;
            }

            boolean notificationCounterElapsed = (notificationCounter >= NOTIFICATION_INTERVAL);

            if (SHOW_NOTIFICATION && (notificationCounterElapsed)) {
                ForgeProductionReport intel = new ForgeProductionReport();
                Global.getSector().getIntelManager().addIntel(intel);
                notificationCounter = 0;
            }

        }

    }

    protected void deactivateImpl() { cleanupImpl(); }

    protected void cleanupImpl() { }

    @Override
    public boolean isUsable() {
        if (!isActivateCooldown &&
                getProgressFraction() > 0 && getProgressFraction() < 1 &&
                getDeactivationDays() > 0) return false;

        if (!hasActiveForgeShips())
            return false;

        return super.isUsable();
    }

    @Override
    public boolean showActiveIndicator() { return isActive(); }

    @Override
    public boolean hasTooltip() { return true; }

    @Override
    public float getTooltipWidth() {
        return 375f;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {

        Color highlight = Misc.getHighlightColor();
        Color negativeHighlight = Misc.getNegativeHighlightColor();

        String status = " (off)";
        if (turnedOn) {
            status = " (on)";
        }

        LabelAPI title = tooltip.addTitle(this.spec.getName() + status);
        title.highlightLast(status);
        title.setHighlightColor(highlight);

        tooltip.setLowGridRowHeight();

        float pad = 6f;
        tooltip.addPara("Control forge production of your fleet.", pad);
        tooltip.addPara("Your fleet must have ships with active forge modules to produce goods. " +
                "Ships that are mothballed or do not receive repairs are unable to use their forge modules. " +
                "Production also uses heavy machinery, which must be present but is not consumed, and can break accidentally.", pad);

        if (hasActiveForgeShips() && expanded) {
            ForgeProductionTooltip.addOperationalShipsBreakdown(tooltip);
        }

        if (hasInactiveForgeShips() && expanded) {
            ForgeProductionTooltip.addInactiveShipsBreakdown(tooltip);
        }

        if (!hasActiveForgeShips()) {
            tooltip.addPara("Your fleet currently cannot conduct forging operations.", negativeHighlight, pad);
        }

        if (expanded) {
            ForgeProductionTooltip.addExpandedInfo(tooltip);
        }

        addIncompatibleToTooltip(tooltip, expanded);
    }

    @Override
    protected void addIncompatibleToTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        addIncompatibleToTooltip(tooltip, "Interrupts the following abilities when activated:",
                "Expand tooltip to view production specifics and conflicting abilities",
                expanded);
    }

}