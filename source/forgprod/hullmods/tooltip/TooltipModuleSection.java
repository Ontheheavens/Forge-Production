package forgprod.hullmods.tooltip;

import java.util.ArrayList;
import java.util.Objects;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;

import forgprod.abilities.conversion.support.ProductionConstants;
import forgprod.abilities.conversion.support.checks.FleetwideProductionChecks;
import forgprod.abilities.conversion.support.checks.ItemBonusesChecks;
import forgprod.abilities.modules.FleetwideModuleManager;
import forgprod.abilities.modules.dataholders.ProductionModule;
import forgprod.hullmods.checks.HullmodStateChecks;
import forgprod.settings.SettingsHolder;

/**
 * @author Ontheheavens
 * @since 13.12.2022
 */

public class TooltipModuleSection {

    public static void addModulePanel(TooltipMakerAPI tooltip, ShipAPI ship, String hullmodId,
                                      float width, boolean expanded) {
        TooltipMakerAPI modulePanel = tooltip.beginImageWithText(null, 32f);
        modulePanel.addImage(Global.getSettings().getSpriteName("forgprod_ui", "frame_small"), 36f, 36f, 20f);
        UIComponentAPI moduleIconFrame = modulePanel.getPrev();
        float customWidthOffset = width - 370f;
        moduleIconFrame.getPosition().setXAlignOffset(-(360f + customWidthOffset));
        modulePanel.addImage(Global.getSettings().getSpriteName("forgprod_hullmods", hullmodId), 32f, 32f, 20f);
        float iconOffset = modulePanel.getPrev().getPosition().getY() - moduleIconFrame.getPosition().getY();
        PositionAPI iconPosition = modulePanel.getPrev().getPosition();
        iconPosition.belowMid(moduleIconFrame, iconOffset + 18f);
        float textWidth = 310f + customWidthOffset;
        modulePanel.setTextWidthOverride(textWidth);
        String description = ProductionConstants.HULLMOD_DESCRIPTIONS.get(hullmodId);
        modulePanel.addPara(description, 0f).getPosition().rightOfTop(moduleIconFrame, 8f);
        LabelAPI descriptionLabel = (LabelAPI) modulePanel.getPrev();
        descriptionLabel.getPosition().setYAlignOffset(4f);
        float textSize = descriptionLabel.computeTextWidth(description);
        float textSizeRatio = textSize / textWidth;
        int textLineOffset = (int) Math.ceil(textSizeRatio) * 7;
        tooltip.addImageWithText(0f);
        UIComponentAPI panel = tooltip.getPrev();
        tooltip.addSpacer(-95f).getPosition().belowLeft(panel, 0f);
        tooltip.addSpacer(30f - textLineOffset);
        if (ship == null || !expanded) {
            return;
        }
        addStatsBlock(tooltip, ship, customWidthOffset, hullmodId);
        tooltip.addSpacer(4f);
    }

    private static void addStatsBlock(TooltipMakerAPI tooltip, ShipAPI ship, float widthOffset, String hullmodId) {
        float breakdownChance = SettingsHolder.BASE_BREAKDOWN_CHANCE;
        float breakdownSizeMinimal = SettingsHolder.BREAKDOWN_SEVERITY - (SettingsHolder.BREAKDOWN_SEVERITY / 2);
        float breakdownSizeMaximal = SettingsHolder.BREAKDOWN_SEVERITY + (SettingsHolder.BREAKDOWN_SEVERITY / 2);
        float accidentChance = SettingsHolder.BASE_BREAKDOWN_CHANCE / 2;
        CampaignFleetAPI fleet = HullmodStateChecks.getFleetOfShip(ship);
        ArrayList<Float> breakdownStats = null;
        if (fleet != null) {
            breakdownStats = getFleetBreakdownStats(fleet);
        }
        boolean shouldAddItemBonuses = false;
        if (breakdownStats != null) {
            shouldAddItemBonuses = true;
            breakdownChance = breakdownStats.get(0);
            breakdownSizeMinimal = breakdownStats.get(1);
            breakdownSizeMaximal = breakdownStats.get(2);
            accidentChance = breakdownStats.get(3);
        }
        float offset = widthOffset / 2;
        float gridItemWidth = 170f + offset;
        tooltip.beginGrid(gridItemWidth, 2);
        tooltip.addToGrid(0, 0, "Breakdown chance",
                (Misc.getRoundedValueMaxOneAfterDecimal(breakdownChance * 100f)) + "%");
        tooltip.addToGrid(1, 0, "Casualty chance",
                (Misc.getRoundedValueMaxOneAfterDecimal((breakdownChance * accidentChance) * 100f) + "%"));
        tooltip.addGrid(4f);
        offset = 10f;
        tooltip.getPrev().getPosition().setXAlignOffset(offset);
        tooltip.addSpacer(0f);
        tooltip.getPrev().getPosition().setXAlignOffset(-offset);
        tooltip.beginGrid(gridItemWidth, 2);
        tooltip.addToGrid(0, 0, "Breakdown size",
                (Misc.getRoundedValueMaxOneAfterDecimal(breakdownSizeMinimal * 100f) + "%") + " - " +
                        (Misc.getRoundedValueMaxOneAfterDecimal(breakdownSizeMaximal * 100f) + "%"));
        tooltip.addToGrid(1, 0, "CR decrease",
                Misc.getRoundedValueMaxOneAfterDecimal(SettingsHolder.DAILY_CR_DECREASE * 100f) + "%");
        tooltip.addGrid(8f);
        tooltip.getPrev().getPosition().setXAlignOffset(offset);
        tooltip.addSpacer(-2f);
        tooltip.getPrev().getPosition().setXAlignOffset(-offset);
        if (shouldAddItemBonuses) {
            addItemBonuses(tooltip, fleet, ship, hullmodId, widthOffset);
            tooltip.addSpacer(4f);
        }
    }



    public static ArrayList<Float> getFleetBreakdownStats(CampaignFleetAPI fleet) {
        ArrayList<Float> breakdownStats = new ArrayList<>();
        if (fleet != null) {
            breakdownStats.add(0, FleetwideProductionChecks.getBreakdownChance(fleet));
            breakdownStats.add(1, FleetwideProductionChecks.getMinimalBreakdownSeverity(fleet));
            breakdownStats.add(2, FleetwideProductionChecks.getMaximalBreakdownSeverity(fleet));
            breakdownStats.add(3, FleetwideProductionChecks.getFatalAccidentChance(fleet));
            return breakdownStats;
        }
        return null;
    }

    private static void addItemBonuses(TooltipMakerAPI tooltip, CampaignFleetAPI fleet, ShipAPI ship,
                                       String hullmodId, float widthOffset) {
        if (ItemBonusesChecks.hasAnySpecialItem(fleet)) {
            tooltip.addButton("", null, 370f + widthOffset, 0f,
                    12f);
        } else {
            return;
        }
        SpecialItemSpecAPI nanoforgeSpec = ItemBonusesChecks.getBestNanoforgeInCargoSpec(fleet);
        boolean hasNanoforge = (nanoforgeSpec != null);
        if (hasNanoforge) {
            float breakdownDecrease = (1f - ItemBonusesChecks.getNanoforgeBreakdownDecrease(fleet));
            String nanoforgeBreakdownLabel = "Breakdown reduction";
            String nanoforgeBreakdownValue = Misc.getRoundedValueMaxOneAfterDecimal(breakdownDecrease * 100f) + "%";
            addItemLine(tooltip, nanoforgeBreakdownLabel, nanoforgeBreakdownValue, nanoforgeSpec, widthOffset);
        }
        float refiningBonus = ItemBonusesChecks.getRefiningBonus(fleet);
        float fuelProductionBonus = ItemBonusesChecks.getFuelProductionBonus(fleet);
        float heavyIndustryBonus = ItemBonusesChecks.getHeavyIndustryBonus(fleet);
        switch (hullmodId) {
            case ProductionConstants.REFINING_MODULE:
                if (refiningBonus > 0f) {
                    String refiningBonusValue = getBonusValue(ship, refiningBonus);
                    SpecialItemSpecAPI catalyticCoreSpec = Global.getSettings().getSpecialItemSpec(Items.CATALYTIC_CORE);
                    addItemLine(tooltip, "", refiningBonusValue + "×", catalyticCoreSpec, widthOffset);
                }
                break;
            case ProductionConstants.FUEL_PRODUCTION_MODULE:
                if (fuelProductionBonus > 0f) {
                    String fuelProductionBonusValue = getBonusValue(ship, fuelProductionBonus);
                    SpecialItemSpecAPI synchrotronCoreSpec = Global.getSettings().getSpecialItemSpec(Items.SYNCHROTRON);
                    addItemLine(tooltip, "", fuelProductionBonusValue + "×", synchrotronCoreSpec, widthOffset);
                }
                break;
            case ProductionConstants.HEAVY_INDUSTRY_MODULE:
                ProductionModule module = FleetwideModuleManager.getInstance().getModuleIndex().get(ship.getFleetMember());
                boolean moduleExists = (module != null);
                if (moduleExists && module.getCapacitiesMode() == ProductionModule.ProductionMode.PRIMARY) {
                    if (heavyIndustryBonus > 0f) {
                        String heavyIndustryBonusValue = getBonusValue(ship, heavyIndustryBonus);
                        addItemLine(tooltip, "", heavyIndustryBonusValue + "×", nanoforgeSpec, widthOffset);
                    }
                }
                break;
        }
    }

    private static String getBonusValue(ShipAPI ship, float cycleBonus) {
        int shipCycles = ProductionConstants.SHIPSIZE_CAPACITY.get(ship.getHullSize()) * SettingsHolder.GRANULARITY;
        return Misc.getRoundedValueMaxOneAfterDecimal(cycleBonus * shipCycles);
    }

    private static void addItemLine(TooltipMakerAPI tooltip, String label, String value,
                                    SpecialItemSpecAPI spec, float widthOffset) {
        TooltipMakerAPI itemLinePanel = tooltip.beginImageWithText(null, 24f);
        itemLinePanel.setTextWidthOverride(250f);
        if (Objects.equals(label, "")) {
            label = "Bonus output";
        }
        LabelAPI descriptionLabel = itemLinePanel.addPara(label + ":", 2f);
        descriptionLabel.getPosition().setXAlignOffset(-(360f + widthOffset));
        float offset = widthOffset / 2;
        LabelAPI valueLabel = itemLinePanel.addPara(value, Misc.getPositiveHighlightColor(), 0f);
        valueLabel.getPosition().rightOfMid((UIComponentAPI) descriptionLabel, -329f + offset);
        valueLabel.setAlignment(Alignment.RMID);
        LabelAPI itemNameLabel = itemLinePanel.addPara("(       " + spec.getName() + ")", 0f);
        itemNameLabel.getPosition().rightOfMid((UIComponentAPI) valueLabel, 8f + offset);
        itemLinePanel.addImage(spec.getIconName(), 24f, 0f);
        itemLinePanel.getPrev().getPosition().rightOfMid((UIComponentAPI) itemNameLabel, -245f + offset);
        itemLinePanel.addSpacer(-40f);
        itemLinePanel.getPrev().getPosition().setYAlignOffset(-40f);
        tooltip.addImageWithText(8f);
        tooltip.addSpacer(-16f);
    }

}
