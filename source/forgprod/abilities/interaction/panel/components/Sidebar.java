package forgprod.abilities.interaction.panel.components;

import java.awt.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;

import forgprod.abilities.conversion.support.ProductionType;
import forgprod.abilities.conversion.support.checks.FleetwideProductionChecks;
import forgprod.abilities.conversion.support.checks.ItemBonusesChecks;
import forgprod.abilities.interaction.panel.ControlPanelManager;
import forgprod.abilities.interaction.panel.PanelConstants;

import static forgprod.abilities.conversion.support.checks.FleetwideProductionChecks.getFleetCapacityForType;

/**
 * @author Ontheheavens
 * @since 02.01.2023
 */

public class Sidebar {

    private static CustomPanelAPI framePanel;
    private static CustomPanelAPI sidebarContent;

    private static float width = 250f;

    public static CustomPanelAPI create(CustomPanelAPI panel, CustomUIPanelPlugin plugin) {
        CustomPanelAPI sidebar = panel.createCustomPanel(width, 400f, plugin);
        framePanel = sidebar;
        Sidebar.createIllustration(sidebar, width);
        Sidebar.createFrame(sidebar, width);
        Sidebar.renderContent();
        panel.addComponent(sidebar).inTL(938f, 129f);
        return sidebar;
    }

    private static void createIllustration(CustomPanelAPI sidebar, float width) {
        TooltipMakerAPI illustrationContainer = sidebar.createUIElement(width, 1f, false);
        illustrationContainer.addImage(Global.getSettings().getSpriteName("forgprod_ui",
                "forge_production_illustration"), 2f);
        illustrationContainer.getPrev().getPosition().setXAlignOffset(-45f);
        illustrationContainer.getPrev().getPosition().setYAlignOffset(-45f);
        sidebar.addUIElement(illustrationContainer).inTL(47f, -143f);
    }

    private static void createFrame(CustomPanelAPI sidebar, float width) {
        TooltipMakerAPI sidebarHeader = sidebar.createUIElement(width, 20f, false);
        sidebarHeader.addSectionHeading("Equipment", Alignment.MID, 2f);
        UIComponentAPI header = sidebarHeader.getPrev();
        ButtonAPI footerLine = Common.addLine(sidebarHeader, width);
        footerLine.getPosition().belowLeft(header, 0f);
        footerLine.getPosition().setXAlignOffset(-5f);
        sidebar.addUIElement(sidebarHeader).inTL(2f, 2f);
        TooltipMakerAPI sidebarFrame = sidebar.createUIElement(width, 380f, false);
        sidebarFrame.setForceProcessInput(false);
        Sidebar.addFrameBox(sidebarFrame, width);
        sidebar.addUIElement(sidebarFrame).belowLeft(sidebarHeader, 0f);
    }

    private static void addFrameBox(TooltipMakerAPI sidebarFrame, float width) {
        TooltipMakerAPI sidebarBoxContainer = sidebarFrame.beginImageWithText(null, 2f);
        Color baseBoxColor = Misc.interpolateColor(PanelConstants.DARK_PLAYER_COLOR,
                PanelConstants.BRIGHT_PLAYER_COLOR, 0.5f);
        Color boxColor = Misc.scaleColorOnly(baseBoxColor, 0.9f);
        sidebarBoxContainer.addAreaCheckbox("", null, PanelConstants.PLAYER_COLOR,
                boxColor, PanelConstants.PLAYER_COLOR,
                width, 503, 0f);
        sidebarBoxContainer.getPrev().getPosition().setXAlignOffset(-245f);
        sidebarFrame.addImageWithText(2f);
    }

    public static void renderContent() {
        if (framePanel == null) return;
        if (sidebarContent != null) {
            framePanel.removeComponent(sidebarContent);
        }
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        CustomPanelAPI sidebarContentPanel = framePanel.createCustomPanel(width - 4, 500f,
                ControlPanelManager.getInstance().getPlugin());
        TooltipMakerAPI machineryPanel = sidebarContentPanel.createUIElement(width - 4, 100f, false);
        machineryPanel.addSpacer(20f);
        Sidebar.addMachinerySection(machineryPanel);
        sidebarContentPanel.addUIElement(machineryPanel).inTL(4f, 105f);
        SpecialItemSpecAPI nanoforgeSpec = ItemBonusesChecks.getBestNanoforgeInCargoSpec(fleet);
        float panelOffset = -37f;
        if (nanoforgeSpec != null) {
            TooltipMakerAPI nanoforgePanel = sidebarContentPanel.createUIElement(width - 4, 100f, false);
            Sidebar.addNanoforgeSection(nanoforgePanel, nanoforgeSpec);
            sidebarContentPanel.addUIElement(nanoforgePanel).belowLeft(machineryPanel, panelOffset);
            panelOffset += 117f;
        }
        if (ItemBonusesChecks.hasSpecialItem(fleet, Items.CATALYTIC_CORE)) {
            TooltipMakerAPI catalyticCorePanel = sidebarContentPanel.createUIElement(width - 4, 100f, false);
            Sidebar.addCatalyticCoreSection(catalyticCorePanel, Global.getSettings().getSpecialItemSpec(Items.CATALYTIC_CORE));
            sidebarContentPanel.addUIElement(catalyticCorePanel).belowLeft(machineryPanel, panelOffset);
            panelOffset += 94f;
        }
        if (ItemBonusesChecks.hasSpecialItem(fleet, Items.SYNCHROTRON)) {
            TooltipMakerAPI synchrotronPanel = sidebarContentPanel.createUIElement(width - 4, 100f, false);
            Sidebar.addSynchrotronSection(synchrotronPanel, Global.getSettings().getSpecialItemSpec(Items.SYNCHROTRON));
            sidebarContentPanel.addUIElement(synchrotronPanel).belowLeft(machineryPanel, panelOffset);
        }
        framePanel.addComponent(sidebarContentPanel);
        Sidebar.sidebarContent = sidebarContentPanel;
    }

    private static void addMachinerySection(TooltipMakerAPI machineryPanel) {
        String iconName = Global.getSettings().getSpriteName("forgprod_ui", "production_small");
        UIComponentAPI machineryIcon = Sidebar.createEquipmentIcon(machineryPanel, iconName);
        String title = "Heavy Machinery";
        LabelAPI machineryTitle = machineryPanel.addTitle(title);
        machineryTitle.getPosition().rightOfMid(machineryIcon, -197f);
        machineryTitle.getPosition().setYAlignOffset(8f);
        ButtonAPI titleLine = Common.addLine(machineryPanel, 230f);
        titleLine.getPosition().leftOfBottom((UIComponentAPI) machineryTitle, -194f);
        titleLine.getPosition().setYAlignOffset(-14f);
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        float machineryPresent = fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY);
        float machineryRequirement = FleetwideProductionChecks.getFleetMachineryRequirement();
        String requirementValue = Misc.getWithDGS(machineryRequirement);
        String availableValue = Misc.getWithDGS(machineryPresent);
        machineryPanel.addSpacer(6f);
        Sidebar.addMachineryLine(machineryPanel, "Required", requirementValue + "×");
        Sidebar.addMachineryLine(machineryPanel, "Available", availableValue + "×");
        Sidebar.addRatioLine(machineryPanel, fleet);
    }

    private static UIComponentAPI createEquipmentIcon(TooltipMakerAPI sidebarPanel, String iconName) {
        TooltipMakerAPI equipmentIconContainer = sidebarPanel.beginImageWithText(null, 32f);
        equipmentIconContainer.addImage(Global.getSettings().getSpriteName("forgprod_ui", "frame"),
                28f, 28f, 10f);
        UIComponentAPI capacityIconFrame = equipmentIconContainer.getPrev();
        capacityIconFrame.getPosition().setXAlignOffset(-233f);
        equipmentIconContainer.addImage(iconName, 24f, 24f, 0f);
        PositionAPI iconPosition = equipmentIconContainer.getPrev().getPosition();
        float iconOffset = (iconPosition.getY() - capacityIconFrame.getPosition().getY()) - 2f;
        iconPosition.belowMid(capacityIconFrame, iconOffset);
        sidebarPanel.addImageWithText(2f);
        return sidebarPanel.getPrev();
    }

    private static void addMachineryLine(TooltipMakerAPI tooltip, String description, String value) {
        TooltipMakerAPI specificationLine = tooltip.beginImageWithText(null, 32f);
        specificationLine.setTextWidthOverride(250f);
        specificationLine.addPara(description + ":", Misc.getTextColor(), 0f);
        UIComponentAPI desc = specificationLine.getPrev();
        desc.getPosition().setXAlignOffset(-234f);
        CommoditySpecAPI machinerySpec = Global.getSettings().getCommoditySpec(Commodities.HEAVY_MACHINERY);
        specificationLine.addImage(machinerySpec.getIconName(), 24f, 24f, 0f);
        UIComponentAPI icon = specificationLine.getPrev();
        icon.getPosition().rightOfMid(desc, -185f);
        LabelAPI valueLabel = specificationLine.addPara(value, Misc.getHighlightColor(), 0f);
        valueLabel.getPosition().rightOfTop(desc, -358f);
        valueLabel.getPosition().setYAlignOffset(1f);
        valueLabel.setAlignment(Alignment.RMID);
        specificationLine.addPara("Machinery", Misc.getTextColor(), 1f).getPosition().rightOfMid(desc, -106f);
        tooltip.addImageWithText(6f);
        tooltip.addSpacer(-53f);
    }

    private static void addRatioLine(TooltipMakerAPI sidebarPanel, CampaignFleetAPI fleet) {
        TooltipMakerAPI specificationLine = sidebarPanel.beginImageWithText(null, 32f);
        specificationLine.setTextWidthOverride(250f);
        specificationLine.addPara("Ratio:", Misc.getTextColor(), 0f);
        UIComponentAPI desc = specificationLine.getPrev();
        desc.getPosition().setXAlignOffset(-234f);
        String ratioDesc = "(full output)";
        Color ratioColor = Misc.getHighlightColor();
        String ratioIcon = Global.getSettings().getSpriteName("forgprod_ui", "production_satisfied");
        float availability = FleetwideProductionChecks.getMachineryAvailability(fleet);
        String availabilityValue = Misc.getRoundedValueMaxOneAfterDecimal((int) (availability * 100f));
        if (availability < 1f) {
            ratioColor = Misc.getNegativeHighlightColor();
            ratioIcon = Global.getSettings().getSpriteName("forgprod_ui", "production_shortage");
            ratioDesc = "(low output)";
            if (!FleetwideProductionChecks.hasMinimumMachinery(fleet)) {
                ratioDesc = "(no output)";
            }
        }
        if (fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY) < 1f) {
            ratioDesc = "";
        }
        specificationLine.addImage(ratioIcon, 24f, 24f, 0f);
        UIComponentAPI icon = specificationLine.getPrev();
        icon.getPosition().rightOfMid(desc, -185f);
        LabelAPI valueLabel = specificationLine.addPara(availabilityValue + "%", ratioColor, 0f);
        valueLabel.getPosition().rightOfTop(desc, -361f);
        valueLabel.getPosition().setYAlignOffset(1f);
        valueLabel.setAlignment(Alignment.RMID);
        specificationLine.addPara(ratioDesc, ratioColor, 1f).getPosition().rightOfMid(desc, -106f);
        sidebarPanel.addImageWithText(6f);
        sidebarPanel.addSpacer(-53f);
    }

    private static void addNanoforgeSection(TooltipMakerAPI nanoforgePanel, SpecialItemSpecAPI nanoforge) {
        String iconName = nanoforge.getIconName();
        UIComponentAPI nanoforgeIcon = Sidebar.createEquipmentIcon(nanoforgePanel, iconName);
        String title = nanoforge.getName();
        LabelAPI nanoforgeTitle = nanoforgePanel.addTitle(title);
        nanoforgeTitle.getPosition().rightOfMid(nanoforgeIcon, -197f);
        nanoforgeTitle.getPosition().setYAlignOffset(8f);
        ButtonAPI titleLine = Common.addLine(nanoforgePanel, 230f);
        titleLine.getPosition().leftOfBottom((UIComponentAPI) nanoforgeTitle, -194f);
        titleLine.getPosition().setYAlignOffset(-14f);
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        int suppliesCapacity = getFleetCapacityForType(fleet, ProductionType.SUPPLIES_PRODUCTION);
        int machineryCapacity = getFleetCapacityForType(fleet, ProductionType.MACHINERY_PRODUCTION);
        float breakdownsBonus = ItemBonusesChecks.getNanoforgeBreakdownDecrease(fleet);
        float bonusOutput = ItemBonusesChecks.getHeavyIndustryBonus(fleet);
        String suppliesBonus = Misc.getRoundedValueMaxOneAfterDecimal(bonusOutput * suppliesCapacity);
        String machineryBonus = Misc.getRoundedValueMaxOneAfterDecimal(bonusOutput * machineryCapacity);
        CommoditySpecAPI suppliesSpec = Global.getSettings().getCommoditySpec(Commodities.SUPPLIES);
        CommoditySpecAPI machinerySpec = Global.getSettings().getCommoditySpec(Commodities.HEAVY_MACHINERY);
        nanoforgePanel.addSpacer(6f);
        Sidebar.addBreakdownDecreaseLine(nanoforgePanel, breakdownsBonus);
        Sidebar.addBonusOutputLine(nanoforgePanel, suppliesSpec, suppliesBonus);
        Sidebar.addBonusOutputLine(nanoforgePanel, machinerySpec, machineryBonus);
    }

    private static void addCatalyticCoreSection(TooltipMakerAPI catalyticCorePanel, SpecialItemSpecAPI catalyticCoreSpec) {
        String iconName = catalyticCoreSpec.getIconName();
        UIComponentAPI catalyticCoreIcon = Sidebar.createEquipmentIcon(catalyticCorePanel, iconName);
        String title = catalyticCoreSpec.getName();
        LabelAPI catalyticCoreTitle = catalyticCorePanel.addTitle(title);
        catalyticCoreTitle.getPosition().rightOfMid(catalyticCoreIcon, -197f);
        catalyticCoreTitle.getPosition().setYAlignOffset(8f);
        ButtonAPI titleLine = Common.addLine(catalyticCorePanel, 230f);
        titleLine.getPosition().leftOfBottom((UIComponentAPI) catalyticCoreTitle, -194f);
        titleLine.getPosition().setYAlignOffset(-14f);
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        int metalsCapacity = getFleetCapacityForType(fleet, ProductionType.METALS_PRODUCTION);
        int transplutonicsCapacity = getFleetCapacityForType(fleet, ProductionType.TRANSPLUTONICS_PRODUCTION);
        float bonusOutput = ItemBonusesChecks.getRefiningBonus(fleet);
        String metalsBonus = Misc.getRoundedValueMaxOneAfterDecimal(bonusOutput * metalsCapacity);
        String transplutonicsBonus = Misc.getRoundedValueMaxOneAfterDecimal(bonusOutput * transplutonicsCapacity);
        CommoditySpecAPI metalsSpec = Global.getSettings().getCommoditySpec(Commodities.METALS);
        CommoditySpecAPI transplutonicsSpec = Global.getSettings().getCommoditySpec(Commodities.RARE_METALS);
        catalyticCorePanel.addSpacer(6f);
        Sidebar.addBonusOutputLine(catalyticCorePanel, metalsSpec, metalsBonus);
        Sidebar.addBonusOutputLine(catalyticCorePanel, transplutonicsSpec, transplutonicsBonus);
    }

    private static void addSynchrotronSection(TooltipMakerAPI synchrotronPanel, SpecialItemSpecAPI synchrotronSpec) {
        String iconName = synchrotronSpec.getIconName();
        UIComponentAPI synchrotronIcon = Sidebar.createEquipmentIcon(synchrotronPanel, iconName);
        String title = synchrotronSpec.getName();
        LabelAPI synchrotronTitle = synchrotronPanel.addTitle(title);
        synchrotronTitle.getPosition().rightOfMid(synchrotronIcon, -197f);
        synchrotronTitle.getPosition().setYAlignOffset(8f);
        ButtonAPI titleLine = Common.addLine(synchrotronPanel, 230f);
        titleLine.getPosition().leftOfBottom((UIComponentAPI) synchrotronTitle, -194f);
        titleLine.getPosition().setYAlignOffset(-14f);
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        int fuelCapacity = getFleetCapacityForType(fleet, ProductionType.FUEL_PRODUCTION);
        float bonusOutput = ItemBonusesChecks.getFuelProductionBonus(fleet);
        String fuelBonus = Misc.getRoundedValueMaxOneAfterDecimal(bonusOutput * fuelCapacity);
        CommoditySpecAPI fuelSpec = Global.getSettings().getCommoditySpec(Commodities.FUEL);
        synchrotronPanel.addSpacer(6f);
        Sidebar.addBonusOutputLine(synchrotronPanel, fuelSpec, fuelBonus);
    }

    private static void addBonusOutputLine(TooltipMakerAPI itemPanel, final CommoditySpecAPI outputSpec, final String value) {
        TooltipMakerAPI specificationLine = itemPanel.beginImageWithText(null, 2f);
        specificationLine.setTextWidthOverride(250f);
        final LabelAPI description = specificationLine.addPara("Increases output:", Misc.getTextColor(), 0f);
        description.setHighlightColor(Misc.getPositiveHighlightColor());
        description.setHighlight(10, description.getText().length() - 2);
        description.getPosition().setXAlignOffset(-234f);
        specificationLine.addImage(outputSpec.getIconName(), 24f, 24f, 0f);
        UIComponentAPI icon = specificationLine.getPrev();
        icon.getPosition().rightOfMid((UIComponentAPI) description, -135f);
        String outputName = outputSpec.getName();
        if (outputName.equals("Heavy Machinery")) {
            outputName = "Machinery";
        }
        String outputNameShort = specificationLine.shortenString(outputName, 85f);
        specificationLine.addPara(outputNameShort, Misc.getTextColor(),
                1f).getPosition().rightOfMid((UIComponentAPI) description, -106f);
        itemPanel.addImageWithText(6f);
        String anchorImageName = Global.getSettings().getSpriteName("forgprod_ui", "effect_tooltip_anchor");
        itemPanel.addImage(anchorImageName, 0f);
        final String finalOutputName = outputName;
        final float outputNameWidth = itemPanel.computeStringWidth(value + finalOutputName);
        TooltipMakerAPI.TooltipCreator outputLineTooltip = new BaseTooltipCreator() {
            @Override
            public float getTooltipWidth(Object tooltipParam) { return 185 + outputNameWidth; }
            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                tooltip.addPara("Total output increase:", Misc.getTextColor(),
                        0f).getPosition().inLMid(4f);
                tooltip.addImage(outputSpec.getIconName(), 24f, 24f, 0f);
                UIComponentAPI icon = tooltip.getPrev();
                icon.getPosition().inLMid(142f);
                String valueLabel = value + "× ";
                tooltip.addPara(valueLabel + finalOutputName, 0f, Misc.getTextColor(),
                        Misc.getPositiveHighlightColor(), valueLabel).getPosition().inRMid(-165f);
                tooltip.addSpacer(-40f);
            }
        };
        itemPanel.addTooltipToPrevious(outputLineTooltip, TooltipMakerAPI.TooltipLocation.LEFT);
        UIComponentAPI anchorImage = itemPanel.getPrev();
        anchorImage.getPosition().setYAlignOffset(53f);
        float offset = 63f;
        anchorImage.getPosition().setXAlignOffset(offset);
        itemPanel.addSpacer(0f);
        itemPanel.getPrev().getPosition().setXAlignOffset(-offset);
        itemPanel.addSpacer(2f);
    }

    private static void addBreakdownDecreaseLine(TooltipMakerAPI itemPanel, final float value) {
        TooltipMakerAPI specificationLine = itemPanel.beginImageWithText(null, 32f);
        specificationLine.setTextWidthOverride(250f);
        LabelAPI description = specificationLine.addPara("Increases quality:", Misc.getTextColor(), 0f);
        description.setHighlightColor(Misc.getPositiveHighlightColor());
        description.setHighlight(10, description.getText().length() - 2);
        description.getPosition().setXAlignOffset(-234f);
        final String breakdownsIconName = Global.getSettings().getSpriteName("forgprod_ui",
                "production_breakdowns");
        specificationLine.addImage(breakdownsIconName, 24f, 24f, 0f);
        UIComponentAPI icon = specificationLine.getPrev();
        icon.getPosition().rightOfMid((UIComponentAPI) description, -135f);
        specificationLine.addPara("Breakdowns", Misc.getTextColor(),
                1f).getPosition().rightOfMid((UIComponentAPI) description, -106f);
        itemPanel.addImageWithText(6f);
        itemPanel.addSpacer(-38f);
        final String valueFormat = Misc.getRoundedValueMaxOneAfterDecimal((int) (100 - (value * 100)));
        String anchorImageName = Global.getSettings().getSpriteName("forgprod_ui", "effect_tooltip_anchor");
        itemPanel.addImage(anchorImageName, 0f);
        TooltipMakerAPI.TooltipCreator outputLineTooltip = new BaseTooltipCreator() {
            @Override
            public float getTooltipWidth(Object tooltipParam) {
                return 250;
            }
            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                tooltip.addPara("Breakdown reduction:", Misc.getTextColor(),
                        0f).getPosition().inTL(4f, -2f);
                tooltip.addImage(breakdownsIconName, 24f, 24f, 0f);
                UIComponentAPI icon = tooltip.getPrev();
                icon.getPosition().inTL(145f, -7f);
                String valueLabel = "-" + valueFormat + "%";
                tooltip.addPara("%s Chance", 0f, Misc.getTextColor(),
                        Misc.getPositiveHighlightColor(), valueLabel).getPosition().inTR(-165f, -2f);
                tooltip.addSpacer(-35f);
                tooltip.addPara("Breakdown reduction:", Misc.getTextColor(),
                        0f).getPosition().inTL(4f, 21f);
                tooltip.addImage(breakdownsIconName, 24f, 24f, 0f);
                UIComponentAPI iconSecondRow = tooltip.getPrev();
                iconSecondRow.getPosition().inTL(145f, 16f);
                String valueLabelSecond = "-" + valueFormat + "%";
                tooltip.addPara("%s Size", 0f, Misc.getTextColor(),
                        Misc.getPositiveHighlightColor(), valueLabelSecond).getPosition().inTR(-165f, 21f);
                tooltip.addSpacer(-36f);
            }
        };
        itemPanel.addTooltipToPrevious(outputLineTooltip, TooltipMakerAPI.TooltipLocation.LEFT);
        UIComponentAPI anchorImage = itemPanel.getPrev();
        anchorImage.getPosition().setYAlignOffset(15f);
        float offset = 63f;
        anchorImage.getPosition().setXAlignOffset(offset);
        itemPanel.addSpacer(0f);
        itemPanel.getPrev().getPosition().setXAlignOffset(-offset);
        itemPanel.addSpacer(2f);
    }

}
