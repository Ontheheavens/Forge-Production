package data.scripts.abilities;

import java.awt.Color;

import data.scripts.abilities.conversion.ForgeCentrifugingLogic;
import org.lwjgl.util.vector.Vector2f;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

import data.scripts.campaign.ForgeConditionChecker;
import data.scripts.campaign.ForgeProductionReport;
import data.scripts.abilities.conversion.ForgeRefiningLogic;
import static data.scripts.plugins.ForgeSettings.*;
import static data.scripts.abilities.conversion.ForgeConversionVariables.*;

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

            float oreAvailable = fleet.getCargo().getCommodityQuantity(Commodities.ORE);

            if (oreAvailable > ORE_TO_REFINE) {
                ForgeRefiningLogic.refineOre(fleet);
            }

            float transplutonicOreAvailable = fleet.getCargo().getCommodityQuantity(Commodities.RARE_ORE);

            if (transplutonicOreAvailable > TRANSPLUTONIC_ORE_TO_REFINE) {
                ForgeRefiningLogic.refineTransplutonicOre(fleet);
            }

            if (oreWasRefined || transplutonicsWereRefined) {

                ForgeRefiningLogic.applyRefiningCRMalus(fleet);

                fleet.addFloatingText("Refined ores", Misc.setAlpha(Misc.getTextColor(), 255), 0.5f);

                if(PLAY_SOUND_NOTIFICATION) {
                    Global.getSoundPlayer().playSound("ui_cargo_raremetals", 1f, 1f, fleet.getLocation(), ZERO);
                }

                oreWasRefined = false;
                transplutonicsWereRefined = false;
            }

            float volatilesAvailable = fleet.getCargo().getCommodityQuantity(Commodities.VOLATILES);

            int possibleCentrifugingCycles = ForgeCentrifugingLogic.getPossibleCentrifugingCycles(fleet);

            if (volatilesAvailable > VOLATILES_TO_CENTRIFUGE && possibleCentrifugingCycles >= 1) {
                ForgeCentrifugingLogic.centrifugeFuel(fleet);
            }

            if (fuelWasCentrifuged) {

                ForgeCentrifugingLogic.applyCentrifugingCRMalus(fleet);

                fleet.addFloatingText("Centrifuged fuel", Misc.setAlpha(Misc.getTextColor(), 255), 0.5f);

                if(PLAY_SOUND_NOTIFICATION) {
                    Global.getSoundPlayer().playSound("ui_cargo_fuel", 1f, 1f, fleet.getLocation(), ZERO);
                }

                fuelWasCentrifuged = false;
            }

            if (SHOW_NOTIFICATION && ((oreRefiningReport || transplutonicsRefiningReport || fuelCentrifugingReport))) {
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

        if (!ForgeConditionChecker.hasActiveForgeShips())
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

        float pad = 10f;
        tooltip.addPara("Control forge production of your fleet.", pad);

        if (ForgeConditionChecker.hasActiveForgeShips()) {
            ForgeProductionTooltip.addOperationalShipsBreakdown(tooltip);
        }

        if (ForgeConditionChecker.hasInactiveForgeShips()) {
            ForgeProductionTooltip.addInactiveShipsBreakdown(tooltip);
        }

        if (!ForgeConditionChecker.hasActiveForgeShips()) {
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