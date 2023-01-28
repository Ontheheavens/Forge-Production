package forgprod.hullmods.tooltip;

import java.awt.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;

import forgprod.abilities.conversion.logic.LogicFactory;
import forgprod.abilities.conversion.logic.base.ProductionLogic;
import forgprod.abilities.conversion.logic.production.HullPartsProductionLogic;
import forgprod.abilities.conversion.support.ProductionConstants;
import forgprod.abilities.conversion.support.ProductionType;
import forgprod.abilities.interaction.panel.components.Common;
import forgprod.abilities.modules.dataholders.ProductionCapacity;
import forgprod.settings.SettingsHolder;

/**
 * @author Ontheheavens
 * @since 11.12.2022
 */

public class TooltipCapacitySection {

    public static String getCapacitySpriteName(ProductionType type) {
        String cargoFolder = "graphics/icons/cargo/";
        String file = "";
        switch (type) {
            case METALS_PRODUCTION:
                file = "materials.png";
                break;
            case TRANSPLUTONICS_PRODUCTION:
                file = "raremetals.png";
                break;
            case FUEL_PRODUCTION:
                file = "fuel.png";
                break;
            case SUPPLIES_PRODUCTION:
                file = "supplies.png";
                break;
            case MACHINERY_PRODUCTION:
                file = "heavymachinery.png";
                break;
            case HULL_PARTS_PRODUCTION:
                return Global.getSettings().getSpriteName("forgprod_cargo", "hull_parts");
        }
        return cargoFolder + file;
    }

    /**
     * @param capacity can be null, therefore separate argument for type is needed.
     */
    public static void addCapacityPanel(TooltipMakerAPI tooltip, ShipAPI ship, ProductionType type,
                                        ProductionCapacity capacity, boolean installed) {
        Color highlightColor = Misc.getHighlightColor();
        TooltipMakerAPI capacityPanel = tooltip.beginImageWithText(null, 32f);
        capacityPanel.addImage(Global.getSettings().getSpriteName("forgprod_ui", "frame"),
                64f, 64f, 20f);
        UIComponentAPI capacityIconFrame = capacityPanel.getPrev();
        capacityIconFrame.getPosition().setXAlignOffset(-360f);
        addCapacityIcon(capacityPanel, type, capacityIconFrame);
        capacityPanel.setTextWidthOverride(270f);
        String capacityName = ProductionConstants.PRODUCTION_NAMES.get(type) + " Production";
        String status = "";
        Color capacityColor = Misc.getTooltipTitleAndLightHighlightColor();
        Color statusColor = capacityColor;
        if (capacity != null) {
            boolean toggled = capacity.isToggledOn();
            boolean active = capacity.isActive();
            boolean hasThroughput = capacity.getCurrentThroughput() > 0f;
            if (!installed) {
                status = "(not installed)";
                statusColor = highlightColor;
            } else if (active && hasThroughput) {
                status = "(active)";
                statusColor = highlightColor;
            } else if (toggled) {
                status = "(inactive)";
                statusColor = Misc.getNegativeHighlightColor();
            } else {
                status = "(disabled)";
                statusColor = Misc.getGrayColor();
            }
        }
        Color[] labelHighlights = {capacityColor, statusColor};
        capacityPanel.addPara("%s %s", 0f, labelHighlights,
                capacityName, status).getPosition().aboveRight(capacityIconFrame, 0f);
        capacityPanel.setTextWidthOverride(250f);
        LabelAPI capacityNameLabel = (LabelAPI) capacityPanel.getPrev();
        capacityNameLabel.getPosition().setXAlignOffset(285f);
        capacityPanel.addSpacer(2f);
        capacityPanel.getPrev().getPosition().setXAlignOffset(10f);
        addCapacitySpec(capacityPanel, ship, type, capacity, installed);
        tooltip.addImageWithText(6f);
        tooltip.addSpacer(-140f);
    }

    // Is added on top of an anchor frame.
    private static void addCapacityIcon(TooltipMakerAPI capacityPanel, ProductionType type, UIComponentAPI anchor) {
        capacityPanel.addImage(Global.getSettings().getSpriteName("forgprod_ui", "frame"),
                42f, 42f, -45f);
        UIComponentAPI backgroundFrame = capacityPanel.getPrev();
        backgroundFrame.getPosition().belowMid(anchor, -53f);
        capacityPanel.addImage(getCapacitySpriteName(type), 52f, 52f, 20f);
        float iconOffset = capacityPanel.getPrev().getPosition().getY() - anchor.getPosition().getY();
        PositionAPI iconPosition = capacityPanel.getPrev().getPosition();
        iconPosition.belowMid(anchor, iconOffset + 4f);
    }

    private static void addCapacitySpec(TooltipMakerAPI capacityPanel, ShipAPI ship, ProductionType type,
                                        ProductionCapacity capacity, boolean installed) {
        ProductionLogic logic = LogicFactory.getLogic(type);
        int shipCycles = ProductionConstants.SHIPSIZE_CAPACITY.get(ship.getHullSize()) * SettingsHolder.GRANULARITY;
        float bonusOutput = 0;
        if (capacity != null) {
            FleetMemberAPI member = ship.getFleetMember();
            if (installed && member != null) {
                CampaignFleetAPI fleet = member.getFleetData().getFleet();
                shipCycles = capacity.getModifiedCapacity(fleet);
                bonusOutput = logic.getCycleOutputBonus(fleet);
                addThroughputLine(capacityPanel, capacity, ship.getFleetMember().getFleetData().getFleet());
                capacityPanel.getPrev().getPosition().setXAlignOffset(-150f);
                capacityPanel.getPrev().getPosition().setYAlignOffset(-35f);
            }
        }
        float primaryInputValue = (shipCycles * logic.getCyclePrimaryInputAmount());
        float outputValue = (shipCycles * (logic.getCycleOutputAmount() + bonusOutput));
        float meansValue = (shipCycles * logic.getCycleMachineryUse());
        CommoditySpecAPI primaryInput = getCommoditySpecByType(type, false, true);
        CommoditySpecAPI output = getCommoditySpecByType(type, true, false);
        String meansIcon = Global.getSettings().getCommoditySpec(Commodities.HEAVY_MACHINERY).getIconName();
        boolean hasSecondaryInput = logic.hasSecondaryInput();
        TooltipCapacitySection.addSpecificationLine(capacityPanel, primaryInput.getIconName(),
                primaryInputValue, 0, "Input", primaryInput.getName(), capacity);
        if (hasSecondaryInput) {
            CommoditySpecAPI secondaryInput = getCommoditySpecByType(type, false, false);
            float secondaryInputValue = (shipCycles * logic.getCycleSecondaryInputAmount());
            TooltipCapacitySection.addSpecificationLine(capacityPanel, secondaryInput.getIconName(),
                    secondaryInputValue, 0, "Input", secondaryInput.getName(), capacity);
        }
        String outputName;
        String outputIconName;
        if (logic instanceof HullPartsProductionLogic) {
            HullPartsProductionLogic partsLogic = (HullPartsProductionLogic) logic;
            CommoditySpecAPI tertiaryInput = getCommoditySpecById(partsLogic.getTertiaryInputType());
            float tertiaryInputValue = shipCycles * (partsLogic.getCycleTertiaryInputAmount());
            TooltipCapacitySection.addSpecificationLine(capacityPanel, tertiaryInput.getIconName(),
                    tertiaryInputValue, 0, "Input", tertiaryInput.getName(), capacity);
            CommoditySpecAPI quaternaryInput = getCommoditySpecById(partsLogic.getQuaternaryInputType());
            float quaternaryInputValue = shipCycles * (partsLogic.getCycleQuaternaryInputAmount());
            TooltipCapacitySection.addSpecificationLine(capacityPanel, quaternaryInput.getIconName(),
                    quaternaryInputValue, 0, "Input", "Machinery", capacity);
            outputName = ProductionConstants.PRODUCTION_NAMES.get(ProductionType.HULL_PARTS_PRODUCTION);
            outputIconName = getCapacitySpriteName(ProductionType.HULL_PARTS_PRODUCTION);
        } else {
            outputName = output.getName();
            outputIconName = output.getIconName();
            if (outputName.equals("Heavy Machinery")) {
                outputName = "Machinery";
            }
        }
        TooltipCapacitySection.addSpecificationLine(capacityPanel, outputIconName,
                outputValue, (bonusOutput * shipCycles), "Output", outputName, capacity);
        TooltipCapacitySection.addSpecificationLine(capacityPanel, meansIcon,
                meansValue, 0, "Means", "Machinery", capacity);
    }

    private static void addThroughputLine(TooltipMakerAPI capacityPanel,
                                          ProductionCapacity capacity, CampaignFleetAPI fleet) {
        capacityPanel.addPara("Throughput:", 6f);
        UIComponentAPI anchor = capacityPanel.getPrev();
        float offsetX = -10f;
        anchor.getPosition().setXAlignOffset(offsetX);
        capacityPanel.addSpacer(0f);
        capacityPanel.getPrev().getPosition().setXAlignOffset(-offsetX);
        int throughput = Math.round(capacity.getCurrentThroughput() * 100f);
        capacityPanel.addPara(throughput + "%", Misc.getHighlightColor(),
                0f).getPosition().rightOfMid(anchor, -347f);
        LabelAPI value = (LabelAPI) capacityPanel.getPrev();
        value.setAlignment(Alignment.TR);
        Pair<String, Color> status = Common.retrieveCapacityThroughputStatus(capacity, fleet);
        String statusLabel = status.one;
        Color statusColor = status.two;
        capacityPanel.addPara(statusLabel, statusColor, 0f).getPosition().rightOfMid(anchor, -90f);
        capacityPanel.addSpacer(-33f);
    }

    private static void addSpecificationLine(TooltipMakerAPI capacityPanel, String iconSprite, float value,
                                             float bonusValue, String description, String commodity,
                                             ProductionCapacity capacity) {
        TooltipMakerAPI specificationLine = capacityPanel.beginImageWithText(null, 32f);
        specificationLine.setTextWidthOverride(250f);
        specificationLine.addPara(description + ":", 0f);
        UIComponentAPI desc = specificationLine.getPrev();
        desc.getPosition().setXAlignOffset(0f);
        specificationLine.addImage(iconSprite, 24f, 24f, 0f);
        UIComponentAPI icon = specificationLine.getPrev();
        icon.getPosition().rightOfMid(desc, -200f);
        if (capacity != null) {
            if (!capacity.isActive() || capacity.getCurrentThroughput() <= 0f) {
                value = 0;
            }
        }
        String displayedValueLabel = Misc.getRoundedValueMaxOneAfterDecimal(value);
        specificationLine.setParaFontColor(Misc.getHighlightColor());
        String format = displayedValueLabel + "×";
        boolean bonusValid = bonusValue > 0 && capacity != null && capacity.getCurrentThroughput() > 0f;
        if (bonusValid) {
           String bonusValueLabel = "(+" + Misc.getRoundedValueMaxOneAfterDecimal(bonusValue) + ")";
           format = displayedValueLabel + bonusValueLabel + "×";
        }
        specificationLine.addPara(format, 0f).getPosition().rightOfTop(desc, -345f);
        LabelAPI valueLabel = (LabelAPI) specificationLine.getPrev();
        if (bonusValid) {
            valueLabel.setHighlightColor(Misc.getPositiveHighlightColor());
            valueLabel.setHighlight(displayedValueLabel.length(), format.length() - 2);
        }
        valueLabel.getPosition().setYAlignOffset(1f);
        valueLabel.setAlignment(Alignment.RMID);
        specificationLine.setParaFontColor(Misc.getTextColor());
        specificationLine.addPara(commodity, 1f).getPosition().rightOfMid(desc, -90f);
        capacityPanel.addImageWithText(6f);
        capacityPanel.addSpacer(-53f);
    }

    public static CommoditySpecAPI getCommoditySpecByType(ProductionType type, boolean isOutput, boolean primaryInput) {
        ProductionLogic logic = LogicFactory.getLogic(type);
        String commodityId;
        if (isOutput) {
            commodityId = logic.getOutputType();
        } else if (primaryInput) {
            commodityId = logic.getPrimaryInputType();
        } else {
            commodityId = logic.getSecondaryInputType();
        }
        return getCommoditySpecById(commodityId);
    }

    public static CommoditySpecAPI getCommoditySpecById(String commodityId) {
        return Global.getSettings().getCommoditySpec(commodityId);
    }

}
