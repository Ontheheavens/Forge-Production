package forgprod.abilities.tooltip;

import java.awt.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import forgprod.abilities.ForgeProductionAbility;
import forgprod.abilities.conversion.support.checks.FleetwideProductionChecks;
import forgprod.abilities.tooltip.components.TooltipIncomeSection;
import forgprod.abilities.tooltip.components.TooltipShipSection;
import forgprod.settings.SettingsHolder;

/**
 * @author Ontheheavens
 * @since 23.11.2022
 */
public class TooltipAssembly {

    public static void assembleTooltip(CampaignFleetAPI fleet, TooltipMakerAPI tooltip, String name,
                                       boolean turnedOn, boolean isActive, boolean expanded,
                                       ForgeProductionAbility plugin) {
        Color gray = Misc.getGrayColor();
        Color highlight = Misc.getHighlightColor();
        String status = " (off)";
        if (turnedOn) {
            status = " (on)";
        }
        LabelAPI title = tooltip.addTitle(name + status);
        title.highlightLast(status);
        title.setHighlightColor(gray);
        String percentageCR = (int)(SettingsHolder.MINIMUM_CR_PERCENT * 100f) + "%";
        String percentageMachinery = (int)(SettingsHolder.MINIMUM_MACHINERY_PERCENT * 100f) + "%";
        float pad = 6f;
        tooltip.addPara("Control forge production of your fleet and monitor fleet production capacity.", pad);
        boolean hasInstalledModules = FleetwideProductionChecks.hasInstalledModules(fleet);
        boolean hasActiveModules = FleetwideProductionChecks.hasActiveModules() &&
                fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY) >= 1;
        boolean shouldShowInfo = !expanded || (!hasInstalledModules && !hasActiveModules);
        if (shouldShowInfo) {
            tooltip.addPara("Your fleet must have ships with active forge modules to produce goods. " +
                    "Ships that are mothballed, do not receive repairs or have less than %s CR " +
                            "are unable to use their forge modules. " +
                    "Production also uses heavy machinery, which must be present but is not consumed, " +
                            "and can break accidentally. " +
                    "At least %s of total needed machinery must be available to produce goods.",
                    pad, highlight, percentageCR, percentageMachinery);
        }

        if (SettingsHolder.ENABLE_DETECT_AT_RANGE_PENALTY && shouldShowInfo) {
            tooltip.addPara("Increases the range at which the fleet can be detected by %s.",
                    pad, highlight, (int) SettingsHolder.SENSOR_PROFILE_INCREASE + "%");
        }

        if (SettingsHolder.ENABLE_SLOW_MOVE_PENALTY && shouldShowInfo) {
            tooltip.addPara("Causes the fleet to %s.", pad, highlight, "move slowly");
        }

        if (expanded) {
            if (!FleetwideProductionChecks.hasActiveModules()) {
                addCannotForgeNoActiveModules(tooltip);
            } else if (FleetwideProductionChecks.hasActiveModules() && !FleetwideProductionChecks.hasMinimumMachinery(fleet)) {
                addCannotForgeNoMachinery(fleet, tooltip);
            } else if (!plugin.isUsable() && !plugin.checkUsableAbilities()) {
                addCannotForgeDueAbilityConflict(tooltip);
            }
        }

        if (expanded) {
            addExpandedInfo(fleet, tooltip, isActive);
            tooltip.addSectionHeading("Incompatibilities", Alignment.MID, 8f);
            tooltip.addSpacer(-2f);
        }

        if (!expanded) {
            if (!FleetwideProductionChecks.hasActiveModules()) {
                addCannotForgeNoActiveModules(tooltip);
            } else if (FleetwideProductionChecks.hasActiveModules() && !FleetwideProductionChecks.hasMinimumMachinery(fleet)) {
                addCannotForgeNoMachinery(fleet, tooltip);
            } else if (!plugin.isUsable() && !plugin.checkUsableAbilities()) {
                addCannotForgeDueAbilityConflict(tooltip);
            }
        }

        if (SettingsHolder.ENABLE_SLOW_MOVE_PENALTY && !expanded) {
            tooltip.addPara("*A fleet is considered slow-moving at a burn level of half that of its slowest ship.", gray, pad);
        }
    }

    public static void addCannotForgeNoActiveModules(TooltipMakerAPI tooltip) {
        float pad = 6f;
        Color negativeHighlight = Misc.getNegativeHighlightColor();
        tooltip.addPara("Your fleet currently cannot conduct forging operations: no active forge modules.", negativeHighlight, pad);
    }

    public static void addCannotForgeDueAbilityConflict(TooltipMakerAPI tooltip) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        ForgeProductionAbility forgeAbilityPlugin = (ForgeProductionAbility) fleet.getAbilities().get("forge_production");
        String incompatibleName = "";
        for (AbilityPlugin otherAbility : fleet.getAbilities().values()) {
            if (otherAbility.isActiveOrInProgress() && !forgeAbilityPlugin.isCompatible(otherAbility)) {
                incompatibleName = otherAbility.getSpec().getName();
            }
        }
        float pad = 6f;
        Color negativeHighlight = Misc.getNegativeHighlightColor();
        tooltip.addPara("Your fleet currently cannot conduct forging operations: %s enabled.", pad,
                negativeHighlight, Misc.getHighlightColor(), incompatibleName);
    }

    public static void addCannotForgeNoMachinery(CampaignFleetAPI fleet, TooltipMakerAPI tooltip) {
        float pad = 6f;
        Color negativeHighlight = Misc.getNegativeHighlightColor();
        if (fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY) >= 1) {
            tooltip.addPara("Production halted: insufficient machinery.", negativeHighlight, pad);
        } else {
            tooltip.addPara("Your fleet currently cannot conduct forging operations: no machinery available.", negativeHighlight, pad);
        }
    }

    public static void addExpandedInfo(CampaignFleetAPI fleet, TooltipMakerAPI tooltip, boolean isActive) {
        if (FleetwideProductionChecks.hasInstalledModules(fleet)) {
            TooltipShipSection.addShipList(tooltip, fleet);
        }
        if (FleetwideProductionChecks.hasActiveModules() &&
                fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY) >= 1) {
            TooltipIncomeSection.addProductionBreakdown(fleet, tooltip, isActive);
        }
    }

}