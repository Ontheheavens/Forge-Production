package forgprod.abilities.interaction.panel.components.tabs.modules;

import java.awt.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;

import forgprod.abilities.conversion.support.ProductionConstants;
import forgprod.abilities.conversion.support.ProductionType;
import forgprod.abilities.conversion.support.checks.ModuleConditionChecks;
import forgprod.abilities.interaction.panel.ControlPanelManager;
import forgprod.abilities.interaction.panel.PanelConstants;
import forgprod.abilities.interaction.panel.components.Common;
import forgprod.abilities.interaction.panel.components.tabs.ModulesTab;
import forgprod.abilities.interaction.panel.objects.Button;
import forgprod.abilities.interaction.panel.objects.ExclusiveButton;
import forgprod.abilities.modules.dataholders.ProductionCapacity;
import forgprod.abilities.modules.dataholders.ProductionModule;

/**
 * @author Ontheheavens
 * @since 01.01.2023
 */

public class ModuleCard {

    public static CustomPanelAPI create(CustomPanelAPI moduleTab, ProductionModule module) {
        if (module == null) return null;

        CustomUIPanelPlugin plugin = ControlPanelManager.getInstance().getPlugin();
        float width = 668f;
        CustomPanelAPI moduleCard = moduleTab.createCustomPanel(width, 500, plugin);
        TooltipMakerAPI cardHeader = ModuleCard.addHeader(moduleCard, width, module);
        moduleCard.addUIElement(cardHeader).inTL(0f, 0f);
        float offset = 0f;
        for (ProductionCapacity capacity : module.getModuleCapacities()) {
            TooltipMakerAPI capacityPanel = ModuleCapacityPanel.createCapacityPanel(moduleCard, capacity, width);
            moduleCard.addUIElement(capacityPanel).belowRight(cardHeader, offset - 177f);
            offset += 191f;
            if (capacity.getProductionType() == ProductionType.HULL_PARTS_PRODUCTION) {
                CustomPanelAPI blueprintSection = HullBlueprintSection.create(moduleCard, width);
                blueprintSection.getPosition().inTL(0f, 335f);
            }
        }
        return moduleCard;
    }

    private static TooltipMakerAPI addHeader(CustomPanelAPI moduleCard, float width, ProductionModule module) {
        TooltipMakerAPI cardHeader = moduleCard.createUIElement(width, 100, false);
        cardHeader.setForceProcessInput(true);
        cardHeader.addSectionHeading("Module", Alignment.MID, 2f);
        UIComponentAPI headingAnchor = cardHeader.addSpacer(0f);
        UIComponentAPI shipIcon = Common.addShipIcon(cardHeader, module, 72f, 4f, true);
        UIComponentAPI shipInfo = ModuleCard.addShipInfo(cardHeader, module);
        shipInfo.getPosition().rightOfTop(shipIcon, -660f);
        UIComponentAPI imageAnchor = cardHeader.addSpacer(0f);
        imageAnchor.getPosition().inTR(104f, 21f);
        String illustrationIcon = Global.getSettings().getSpriteName("forgprod_hullmods",
                module.getHullmodId() + "_big");
        cardHeader.addImage(illustrationIcon, 0f);
        UIComponentAPI illustrationAnchor = cardHeader.getPrev();
        illustrationAnchor.getPosition().belowLeft(imageAnchor, 0f);
        cardHeader.addTooltipToPrevious(new ModuleTooltip(module), TooltipMakerAPI.TooltipLocation.BELOW);
        UIComponentAPI moduleInfo = ModuleCard.addModuleInfo(cardHeader, module);
        moduleInfo.getPosition().leftOfTop(illustrationAnchor, 264f);
        cardHeader.addSpacer(0f).getPosition().belowLeft(headingAnchor, 78f);
        ModuleCard.addCapacitiesSubHeader(cardHeader, module);
        return cardHeader;
    }

    private static void addCapacitiesSubHeader(TooltipMakerAPI cardHeader, ProductionModule module) {
        String headerAddendum = "";
        boolean isHeavyIndustryModule = module.getHullmodId().equals(ProductionConstants.HEAVY_INDUSTRY_MODULE);
        if (isHeavyIndustryModule) {
            if (module.getCapacitiesMode() == ProductionModule.ProductionMode.PRIMARY) {
                headerAddendum = " (primary)";
            } else if (module.getCapacitiesMode() == ProductionModule.ProductionMode.SECONDARY) {
                headerAddendum = " (secondary)";
            }
        }
        cardHeader.addSectionHeading("Capacities" + headerAddendum, Alignment.MID, 2f);
        UIComponentAPI capacitiesHeading = cardHeader.getPrev();
        if (isHeavyIndustryModule) {
            ButtonAPI primaryModeButton = ModuleCard.createModeButton(cardHeader,
                    module, ProductionModule.ProductionMode.PRIMARY);
            primaryModeButton.getPosition().rightOfBottom(capacitiesHeading, -108f);
            primaryModeButton.getPosition().setYAlignOffset(1f);
            ButtonAPI secondaryModeButton = ModuleCard.createModeButton(cardHeader,
                    module, ProductionModule.ProductionMode.SECONDARY);
            secondaryModeButton.getPosition().rightOfBottom(capacitiesHeading, -50f);
            secondaryModeButton.getPosition().setYAlignOffset(1f);
            cardHeader.addSpacer(-36f);
        }
    }

    private static ButtonAPI createModeButton(TooltipMakerAPI cardHeader, final ProductionModule module,
                                              final ProductionModule.ProductionMode mode) {
        String name = "I";
        if (mode == ProductionModule.ProductionMode.SECONDARY) {
            name = "II";
        }
        Color buttonColor = Misc.interpolateColor(Misc.scaleColorOnly(PanelConstants.DARK_PLAYER_COLOR, 1.2f),
                PanelConstants.PLAYER_COLOR, 0.1f);
        Color highlightColor = Misc.interpolateColor(Misc.scaleColorOnly(PanelConstants.PLAYER_COLOR, 0.9f),
                PanelConstants.DARK_PLAYER_COLOR, 0.1f);
        ButtonAPI modeButtonInstance = cardHeader.addButton(name, null,
                highlightColor, buttonColor, Alignment.MID,
                CutStyle.ALL, 54f, 16f, 2f);
        cardHeader.addTooltipToPrevious(new ModeButtonTooltip(mode), TooltipMakerAPI.TooltipLocation.BELOW);
        if (module.getCapacitiesMode() == mode) {
            modeButtonInstance.highlight();
        }
        new ExclusiveButton(modeButtonInstance, Button.Type.MODULE_MODE) {
            @Override
            public void applyEffect() {
                if (module.getCapacitiesMode() != mode) {
                    module.setCapacitiesMode(mode);
                    ModulesTab.setCardRedrawQueued(true);
                }
            }
        };
        return modeButtonInstance;
    }

    private static UIComponentAPI addShipInfo(TooltipMakerAPI cardHeader, ProductionModule module) {
        FleetMemberAPI member = module.getParentFleetMember();
        Color highlight = Misc.getHighlightColor();
        TooltipMakerAPI infoPanel = cardHeader.beginImageWithText(null, 1f);
        infoPanel.setTextWidthOverride(250f);
        infoPanel.addPara(member.getShipName(), PanelConstants.PLAYER_COLOR, 2f);
        String hullsize = "Cruiser";
        if (member.getHullSpec().getHullSize() != ShipAPI.HullSize.CRUISER) {
            hullsize = "Capital";
        }
        infoPanel.addPara("Hull size: " + hullsize, 2f, highlight, hullsize);
        String reason = "Operational (CR: " +
                Misc.getRoundedValueMaxOneAfterDecimal(member.getRepairTracker().getCR() * 100f) + "%)";
        String status = "Status: %s";
        Color reasonColor = highlight;
        if (!ModuleConditionChecks.isOperational(member)) {
            reasonColor = Misc.getNegativeHighlightColor();
            String disabled = "Inactive ";
            if (member.getRepairTracker().isMothballed()) {
                reason = disabled + "(mothballed)";
            } else if (member.getRepairTracker().isSuspendRepairs()) {
                reason = disabled + "(repairs suspended)";
            } else {
                reason = disabled + "(CR: " +
                        Misc.getRoundedValueMaxOneAfterDecimal(member.getRepairTracker().getCR() * 100f) + "%)";
            }
        }
        infoPanel.addPara(status, 2f, Misc.getTextColor(), reasonColor, reason);
        MutableStat suppliesPerMonth = member.getStats().getSuppliesPerMonth();
        float maintenance = suppliesPerMonth.getModifiedValue() / 30f;
        String maintenanceLabel = Misc.getRoundedValueMaxOneAfterDecimal(maintenance);
        infoPanel.addPara("Ship daily maintenance: " + maintenanceLabel + " supplies",
                2f, Misc.getTextColor(), highlight, maintenanceLabel);
        cardHeader.addImageWithText(2f);
        return cardHeader.getPrev();
    }

    private static UIComponentAPI addModuleInfo(TooltipMakerAPI cardHeader, ProductionModule module) {
        Color highlight = Misc.getHighlightColor();
        TooltipMakerAPI infoPanel = cardHeader.beginImageWithText(null, 1f);
        infoPanel.setTextWidthOverride(250f);
        String moduleId = "Production Module #" + module.getParentFleetMember().getId().toUpperCase();
        LabelAPI idLine = infoPanel.addPara(moduleId, PanelConstants.PLAYER_COLOR, 2f);
        idLine.setAlignment(Alignment.RMID);
        ShipAPI.HullSize shipSize = module.getParentFleetMember().getHullSpec().getHullSize();
        int baseCapacity = ProductionConstants.SHIPSIZE_CAPACITY.get(shipSize);
        String baseCapacityLabel = baseCapacity + "×";
        String moduleSize = "Module volume: " + baseCapacityLabel;
        LabelAPI sizeLine = infoPanel.addPara(moduleSize, 2f, Misc.getTextColor(), highlight, baseCapacityLabel);
        sizeLine.setAlignment(Alignment.RMID);
        cardHeader.addImageWithText(2f);
        return cardHeader.getPrev();
    }

}
