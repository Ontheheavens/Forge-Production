package forgprod.abilities.interaction.panel.components.tabs.modules;

import java.awt.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;

import forgprod.abilities.conversion.logic.base.ProductionLogic;
import forgprod.abilities.conversion.logic.production.FuelProductionLogic;
import forgprod.abilities.conversion.logic.production.HullPartsProductionLogic;
import forgprod.abilities.conversion.support.ProductionConstants;
import forgprod.abilities.conversion.support.ProductionType;
import forgprod.abilities.interaction.panel.PanelConstants;
import forgprod.abilities.interaction.panel.components.Common;
import forgprod.abilities.interaction.panel.components.tabs.ModulesTab;
import forgprod.abilities.interaction.panel.objects.Button;
import forgprod.abilities.interaction.panel.objects.ExclusiveButton;
import forgprod.abilities.modules.dataholders.ProductionCapacity;
import forgprod.hullmods.tooltip.TooltipCapacitySection;

/**
 * @author Ontheheavens
 * @since 04.01.2023
 */

public class ModuleCapacityPanel {

    public static TooltipMakerAPI createCapacityPanel(CustomPanelAPI moduleCard, ProductionCapacity capacity,
                                                   float width) {
        TooltipMakerAPI capacityPanel = moduleCard.createUIElement(width, 200, false);
        ButtonAPI headerLine = Common.addLine(capacityPanel, width);
        headerLine.getPosition().setXAlignOffset(0f);
        UIComponentAPI title = addCapacityTitle(capacityPanel, capacity);
        title.getPosition().setXAlignOffset(-549f);
        UIComponentAPI iconAnchor = ModuleCapacityPanel.createCapacityIcon(capacityPanel,
                capacity.getProductionType(), capacity.isToggledOn());
        iconAnchor.getPosition().inTL(-286f, 37f);
        UIComponentAPI capacitySpec = ModuleCapacityPanel.addCapacitySpec(capacityPanel, capacity);
        capacitySpec.getPosition().belowLeft(title, 0f);
        capacitySpec.getPosition().setXAlignOffset(525f);
        float specYOffset = 19f;
        if (shouldAddStatusNote(capacity)) {
            specYOffset = 34f;
        }
        capacitySpec.getPosition().setYAlignOffset(specYOffset);
        ButtonAPI toggleButton = createToggleButton(capacityPanel, capacity);
        toggleButton.getPosition().belowLeft(iconAnchor, 4f);
        toggleButton.getPosition().setYAlignOffset(138f);
        toggleButton.getPosition().setXAlignOffset(298f);
        if (capacity.getProductionType() == ProductionType.FUEL_PRODUCTION) {
            ButtonAPI footerLine = Common.addLine(capacityPanel, width);
            footerLine.getPosition().setXAlignOffset(-12f);
            footerLine.getPosition().setYAlignOffset(-36f);
        }
        return capacityPanel;
    }

    private static UIComponentAPI createCapacityIcon(TooltipMakerAPI capacityPanel, ProductionType type, boolean enabled) {
        String imageId = "frame_bright";
        if (!enabled) {
            imageId = "frame";
        }
        TooltipMakerAPI capacityIconContainer = capacityPanel.beginImageWithText(null, 32f);
        capacityIconContainer.addImage(Global.getSettings().getSpriteName("forgprod_ui", imageId),
                90f, 90f, 0f);
        UIComponentAPI capacityIconFrame = capacityIconContainer.getPrev();
        capacityIconFrame.getPosition().setXAlignOffset(-360f);
        capacityIconContainer.addImage(Global.getSettings().getSpriteName("forgprod_ui", imageId),
                64f, 64f, 0f);
        UIComponentAPI backgroundFrame = capacityIconContainer.getPrev();
        backgroundFrame.getPosition().belowMid(capacityIconFrame, -77f);
        capacityIconContainer.addImage(TooltipCapacitySection.getCapacitySpriteName(type),
                80f, 80f, 0f);
        PositionAPI iconPosition = capacityIconContainer.getPrev().getPosition();
        float iconOffset = (iconPosition.getY() - capacityIconFrame.getPosition().getY()) - 18f;
        iconPosition.belowMid(capacityIconFrame, iconOffset);
        capacityPanel.addImageWithText(2f);
        return capacityPanel.getPrev();
    }

    private static ButtonAPI createToggleButton(TooltipMakerAPI capacityPanel, final ProductionCapacity capacity) {
        boolean enabled = capacity.isToggledOn();
        String status = "Enabled";
        Color buttonColor = Misc.interpolateColor(Misc.scaleColorOnly(PanelConstants.DARK_PLAYER_COLOR, 1.4f),
                PanelConstants.PLAYER_COLOR, 0.15f);
        if (!enabled) {
            status = "Disabled";
            buttonColor = PanelConstants.DARK_PLAYER_COLOR;
        }
        ButtonAPI toggleButtonInstance = capacityPanel.addButton(status, null,
                PanelConstants.PLAYER_COLOR, buttonColor, Alignment.MID,
                CutStyle.BOTTOM, 90f, 22f, 2f);
        // Isn't actually exclusive.
        new ExclusiveButton(toggleButtonInstance, Button.Type.STANDARD) {
            @Override
            public void affectOthersInGroup() {
            }
            @Override
            public void applyEffect() {
                capacity.setToggledOn(!capacity.isToggledOn());
                ModulesTab.setCardRedrawQueued(true);
            }
        };
        return toggleButtonInstance;
    }

    private static UIComponentAPI addCapacityTitle(TooltipMakerAPI capacityPanel, ProductionCapacity capacity) {
        TooltipMakerAPI capacityTitleContainer = capacityPanel.beginImageWithText(null, 2f);
        capacityTitleContainer.setTextWidthOverride(250f);
        ProductionType type = capacity.getProductionType();
        String capacityName = ProductionConstants.PRODUCTION_NAMES.get(type) + " Production";
        String status;
        Color statusColor;
        boolean toggled = capacity.isToggledOn();
        boolean active = capacity.isActive();
        boolean hasThroughput = capacity.getCurrentThroughput() > 0f;
        if (active && hasThroughput) {
            status = "Active";
            statusColor = Misc.getHighlightColor();
        } else if (toggled) {
            status = "Inactive";
            statusColor = Misc.getNegativeHighlightColor();
        } else {
            status = "Disabled";
            statusColor = Misc.getGrayColor();
        }
        capacityTitleContainer.setParaFont("graphics/fonts/orbitron12condensed.fnt");
        capacityTitleContainer.addPara(capacityName, PanelConstants.PLAYER_COLOR, 10f);
        capacityTitleContainer.setParaFontDefault();
        LabelAPI statusLabel = capacityTitleContainer.addPara("Status:", 8f);

        boolean tanksFull = (capacity.getProductionType() == ProductionType.FUEL_PRODUCTION) &&
                FuelProductionLogic.getInstance().areFuelTanksFull(Global.getSector().getPlayerFleet());
        boolean rosterFull = (capacity.getProductionType() == ProductionType.HULL_PARTS_PRODUCTION) &&
                HullPartsProductionLogic.getInstance().isPlayerFleetRosterFull();
        boolean noActiveRigs = (capacity.getProductionType() == ProductionType.HULL_PARTS_PRODUCTION) &&
                !HullPartsProductionLogic.getInstance().hasOperationalSalvageRigs();
        boolean shouldAddStatusNote = tanksFull || rosterFull || noActiveRigs;
        float statusValueXPad = -90f;
        LabelAPI statusNoteLabel = null;
        if (shouldAddStatusNote) {
            statusValueXPad = -45f;
            String statusNote = "";
            String tanksFullNote = "(tanks full)";
            String rosterFullNote = "(roster full)";
            String noActiveRigsNote = "(no active rigs)";
            if (tanksFull) {
                statusNote = tanksFullNote;
            }
            if (rosterFull) {
                statusNote = rosterFullNote;
            }
            if (noActiveRigs) {
                statusNote = noActiveRigsNote;
            }
            statusNoteLabel = capacityTitleContainer.addPara(statusNote, Misc.getNegativeHighlightColor(), 0f);
            statusNoteLabel.getPosition().rightOfMid((UIComponentAPI) statusLabel, -90f);
        }
        LabelAPI statusValue = capacityTitleContainer.addPara(status, statusColor, 0f);
        statusValue.getPosition().rightOfMid((UIComponentAPI) statusLabel, statusValueXPad);
        if (shouldAddStatusNote) {
            statusValue.getPosition().leftOfMid((UIComponentAPI) statusNoteLabel, 7f);
            statusValue.getPosition().setYAlignOffset(1f);
            statusValue.setAlignment(Alignment.RMID);
        }
        capacityPanel.addImageWithText(2f);
        return capacityPanel.getPrev();
    }

    private static boolean shouldAddStatusNote(ProductionCapacity capacity) {
        boolean tanksFull = (capacity.getProductionType() == ProductionType.FUEL_PRODUCTION) &&
                FuelProductionLogic.getInstance().areFuelTanksFull(Global.getSector().getPlayerFleet());
        boolean rosterFull = (capacity.getProductionType() == ProductionType.HULL_PARTS_PRODUCTION) &&
                HullPartsProductionLogic.getInstance().isPlayerFleetRosterFull();
        boolean noActiveRigs = (capacity.getProductionType() == ProductionType.HULL_PARTS_PRODUCTION) &&
                !HullPartsProductionLogic.getInstance().hasOperationalSalvageRigs();
        return (tanksFull || rosterFull || noActiveRigs);
    }

    private static UIComponentAPI addCapacitySpec(TooltipMakerAPI capacityPanel, ProductionCapacity capacity) {
        CampaignFleetAPI fleet = capacity.getParentModule().getParentFleetMember().getFleetData().getFleet();
        ProductionLogic logic = capacity.getLogic();
        ProductionType type = capacity.getProductionType();
        int shipCycles = capacity.getModifiedCapacity(fleet);
        float bonusOutput = logic.getCycleOutputBonus(fleet);
        TooltipMakerAPI capacitySpecContainer = capacityPanel.beginImageWithText(null, 2f);
        capacitySpecContainer.setTextWidthOverride(270f);
        UIComponentAPI anchor = capacitySpecContainer.addSpacer(0f);
        float offsetX = -520f;
        anchor.getPosition().setXAlignOffset(offsetX);
        capacityPanel.addSpacer(0f);
        capacityPanel.getPrev().getPosition().setXAlignOffset(-offsetX);
        ModuleCapacityPanel.addThroughputLine(capacitySpecContainer, capacity, fleet);
        capacitySpecContainer.getPrev().getPosition().setXAlignOffset(-150f);
        capacitySpecContainer.getPrev().getPosition().setYAlignOffset(-35f);
        float primaryInputValue = (shipCycles * logic.getCyclePrimaryInputAmount());
        float outputValue = (shipCycles * (logic.getCycleOutputAmount() + bonusOutput));
        float meansValue = (shipCycles * logic.getCycleMachineryUse());
        CommoditySpecAPI primaryInput = TooltipCapacitySection.getCommoditySpecByType(type, false, true);
        CommoditySpecAPI output = TooltipCapacitySection.getCommoditySpecByType(type, true, false);
        String meansIcon = Global.getSettings().getCommoditySpec(Commodities.HEAVY_MACHINERY).getIconName();
        boolean hasSecondaryInput = logic.hasSecondaryInput();
        ModuleCapacityPanel.addSpecificationLine(capacitySpecContainer, primaryInput.getIconName(),
                primaryInputValue, 0, "Input", primaryInput.getName(), capacity);
        if (hasSecondaryInput) {
            CommoditySpecAPI secondaryInput = TooltipCapacitySection.getCommoditySpecByType(type, false, false);
            float secondaryInputValue = (shipCycles * logic.getCycleSecondaryInputAmount());
            ModuleCapacityPanel.addSpecificationLine(capacitySpecContainer, secondaryInput.getIconName(),
                    secondaryInputValue, 0, "Input", secondaryInput.getName(), capacity);
        }
        String outputName;
        String outputIconName;
        if (logic instanceof HullPartsProductionLogic) {
            HullPartsProductionLogic partsLogic = (HullPartsProductionLogic) logic;
            CommoditySpecAPI tertiaryInput = TooltipCapacitySection.getCommoditySpecById(partsLogic.getTertiaryInputType());
            float tertiaryInputValue = shipCycles * (partsLogic.getCycleTertiaryInputAmount());
            ModuleCapacityPanel.addSpecificationLine(capacitySpecContainer, tertiaryInput.getIconName(),
                    tertiaryInputValue, 0, "Input", tertiaryInput.getName(), capacity);
            CommoditySpecAPI quaternaryInput = TooltipCapacitySection.getCommoditySpecById(partsLogic.getQuaternaryInputType());
            float quaternaryInputValue = shipCycles * (partsLogic.getCycleQuaternaryInputAmount());
            ModuleCapacityPanel.addSpecificationLine(capacitySpecContainer, quaternaryInput.getIconName(),
                    quaternaryInputValue, 0, "Input", "Machinery", capacity);
            outputName = ProductionConstants.PRODUCTION_NAMES.get(ProductionType.HULL_PARTS_PRODUCTION);
            outputIconName = TooltipCapacitySection.getCapacitySpriteName(ProductionType.HULL_PARTS_PRODUCTION);
        } else {
            outputName = output.getName();
            outputIconName = output.getIconName();
            if (outputName.equals("Heavy Machinery")) {
                outputName = "Machinery";
            }
        }
        ModuleCapacityPanel.addSpecificationLine(capacitySpecContainer, outputIconName,
                outputValue, (bonusOutput * shipCycles), "Output", outputName, capacity);
        ModuleCapacityPanel.addSpecificationLine(capacitySpecContainer, meansIcon,
                meansValue, 0, "Means", "Machinery", capacity);
        capacityPanel.addImageWithText(4f);
        return capacityPanel.getPrev();
    }

    private static void addThroughputLine(TooltipMakerAPI capacityPanel,
                                          ProductionCapacity capacity, CampaignFleetAPI fleet) {
        LabelAPI anchor = capacityPanel.addPara("Throughput:", 11f);
        int throughput = (int) (capacity.getCurrentThroughput() * 100f);
        capacityPanel.addPara(throughput + "%", Misc.getHighlightColor(),
                0f).getPosition().rightOfMid((UIComponentAPI) anchor, -388f);
        LabelAPI value = (LabelAPI) capacityPanel.getPrev();
        value.setAlignment(Alignment.TR);
        Pair<String, Color> status = Common.retrieveCapacityThroughputStatus(capacity, fleet);
        String statusLabel = status.one;
        Color statusColor = status.two;
        capacityPanel.addPara(statusLabel, statusColor, 0f).getPosition().rightOfMid((UIComponentAPI) anchor, -110f);
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
        icon.getPosition().rightOfMid(desc, PanelConstants.SPEC_LINE_ICON_OFFSET);
        if (!capacity.isActive() || capacity.getCurrentThroughput() <= 0f) {
            value = 0;
        }
        String displayedValueLabel = Misc.getRoundedValueMaxOneAfterDecimal(value);
        specificationLine.setParaFontColor(Misc.getHighlightColor());
        String format = displayedValueLabel + "×";
        boolean bonusValid = bonusValue > 0 && capacity.getCurrentThroughput() > 0f;
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

}
