package data.forge.abilities;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import data.forge.abilities.conversion.*;
import data.forge.abilities.conversion.support.ForgeConversionGeneral;
import data.forge.abilities.tooltip.ForgeProductionTooltip;
import data.forge.campaign.ForgeConditionChecker;
import data.forge.plugins.ForgeSettings;
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
import static data.forge.abilities.conversion.support.ForgeConversionVariables.*;

public class ForgeProduction extends BaseToggleAbility {

    private static final Vector2f ZERO = new Vector2f();

    private int notificationCounter = 0;

    private final IntervalUtil productionInterval = new IntervalUtil(1f,1f);

    protected void activateImpl() {
        interruptIncompatible();
    }

    @Override
    protected String getActivationText() {
        return "Commencing forge production";
    }

    protected void applyEffect(float amount, float level) {

        CampaignFleetAPI fleet = getFleet();

        if (fleet == null) return;
        if (!isActive()) return;

        if (ENABLE_DETECT_AT_RANGE_PENALTY) {
            fleet.getStats().getDetectedRangeMod().modifyPercent(getModId(), ForgeSettings.SENSOR_PROFILE_INCREASE, "Forge production");
        }

        if (ENABLE_SLOW_MOVE_PENALTY) {
            fleet.goSlowOneFrame();
        }

        float days = Global.getSector().getClock().convertToDays(amount);

        productionInterval.advance(days);

        if (productionInterval.intervalElapsed()) {

            ForgeRefiningLogic.startRefining(fleet);

            ForgeCentrifugingLogic.startCentrifuging(fleet);

            ForgeManufacturingLogic.startManufacturing(fleet);

            ForgeAssemblingLogic.startAssembling(fleet);

            if (goodsWereForged()) {

                fleet.addFloatingText("Forged goods", Misc.setAlpha(Misc.getTextColor(), 255), 0.5f);

                if(PLAY_SOUND_NOTIFICATION) {
                    Global.getSoundPlayer().playSound("ui_cargo_raremetals", 1f, 1f, fleet.getLocation(), ZERO);
                }

                clearGoodsStatus();
            }

            if (SHOW_NOTIFICATION && goodsProducedReport()) {
                notificationCounter += 1;
            }

            boolean notificationCounterElapsed = (notificationCounter >= NOTIFICATION_INTERVAL);

            if (SHOW_NOTIFICATION && (notificationCounterElapsed)) {
                ForgeProductionReport intel = new ForgeProductionReport();
                Global.getSector().getIntelManager().addIntel(intel);
                notificationCounter = 0;
            }

        }

        if (!checkUsableForMachinery()) {
            deactivate();
        }
        disableIncompatible();
    }

    protected void deactivateImpl() { cleanupImpl(); }

    @Override
    protected void cleanupImpl() {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;

        if (ENABLE_DETECT_AT_RANGE_PENALTY) {
            fleet.getStats().getDetectedRangeMod().unmodify(getModId());
        }
    }

    private boolean checkUsableForMachinery() {
        boolean hasMachinery = ForgeConditionChecker.getPlayerCargo(Commodities.HEAVY_MACHINERY) > 0;
        return (hasActiveForgeShips() && hasMachinery);
    }

    @Override
    public boolean isUsable() {
        if (!isActivateCooldown &&
                getProgressFraction() > 0 && getProgressFraction() < 1 &&
                getDeactivationDays() > 0) return false;

        if (!checkUsableForMachinery())
            return false;

        return super.isUsable();
    }

    @Override
    public boolean showActiveIndicator() { return isActive(); }

    @Override
    public boolean hasTooltip() { return true; }

    @Override
    public float getTooltipWidth() {
        return 390f;
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {

        Color gray = Misc.getGrayColor();
        Color highlight = Misc.getHighlightColor();

        String status = " (off)";
        if (turnedOn) {
            status = " (on)";
        }

        LabelAPI title = tooltip.addTitle(this.spec.getName() + status);
        title.highlightLast(status);
        title.setHighlightColor(highlight);

        String percentageCR = "10%";
        String percentageMachinery = "10%";

        float pad = 6f;
        tooltip.addPara("Control forge production of your fleet and monitor fleet forging capacity.", pad);
        if (!expanded) {
            tooltip.addPara("Your fleet must have ships with active forge modules to produce goods. " +
                    "Ships that are mothballed, do not receive repairs or have less than %s CR are unable to use their forge modules. " +
                    "Production also uses heavy machinery, which must be present but is not consumed, and can break accidentally. " +
                    "At least %s of total needed machinery must be available to produce goods.", pad, highlight, percentageCR, percentageMachinery);
        }

        if (!hasActiveForgeShips() && expanded) {
            ForgeProductionTooltip.addCannotForgeNoActiveShips(tooltip);
        }

        if (hasActiveForgeShips() && ForgeConversionGeneral.getMachineryAvailability() < 0.1f && expanded) {
            ForgeProductionTooltip.addCannotForgeNoMachinery(tooltip);
        }

        if (expanded) {
            ForgeProductionTooltip.addExpandedInfo(tooltip, this.isActive());
        }

        if (ENABLE_DETECT_AT_RANGE_PENALTY && !expanded) {
            tooltip.addPara("Increases the range at which the fleet can be detected by %s.",
                    pad, highlight,  (int) ForgeSettings.SENSOR_PROFILE_INCREASE + "%");
        }

        if (ENABLE_SLOW_MOVE_PENALTY && !expanded) {
            tooltip.addPara("Causes the fleet to %s.",
                    pad, highlight, "move slowly");

            tooltip.addPara("*A fleet is considered slow-moving at a burn level of half that of its slowest ship.", gray, pad);
        }

        if (!hasActiveForgeShips() && !expanded) {
            ForgeProductionTooltip.addCannotForgeNoActiveShips(tooltip);
        }

        if (hasActiveForgeShips() && ForgeConversionGeneral.getMachineryAvailability() < 0.1f && !expanded) {
            ForgeProductionTooltip.addCannotForgeNoMachinery(tooltip);
        }

        addIncompatibleToTooltip(tooltip, expanded);
    }

    @Override
    protected void addIncompatibleToTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        addIncompatibleToTooltip(tooltip, "Incompatible with the following abilities:",
                "Expand tooltip to view production specifics and conflicting abilities",
                expanded);
    }

    @Override
    protected boolean isCompatible(AbilityPlugin other) {
        if(!ENABLE_BURN_ABILITIES_INCOMPATIBILITY)
            return true;
        if (other.getId().equals("sustained_burn") || other.getId().equals("emergency_burn")) {
            return false;
        }
        return true;
    }

    @Override
    public List<AbilityPlugin> getInterruptedList() {
        List<AbilityPlugin> result = new ArrayList<>();
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return result;
        for (AbilityPlugin curr : fleet.getAbilities().values()) {
            if (curr == this) continue;
            if (!isCompatible(curr)) {
                result.add(curr);
            }
        }
        Collections.sort(result, new Comparator<AbilityPlugin>() {
            public int compare(AbilityPlugin o1, AbilityPlugin o2) {

                return o1.getSpec().getSortOrder() - o2.getSpec().getSortOrder();
            }
        });
        return result;
    }

    @Override
    protected void interruptIncompatible() {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;
        for (AbilityPlugin curr : fleet.getAbilities().values()) {
            if (curr == this) continue;
            if (!isCompatible(curr) && curr.isActive()) {
                curr.deactivate();
            }
        }
    }

    @Override
    protected void disableIncompatible() {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;
        for (AbilityPlugin curr : fleet.getAbilities().values()) {
            if (curr == this) continue;
            if (!isCompatible(curr)) {
                curr.forceDisable();
            }
        }
    }

}