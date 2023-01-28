package forgprod.abilities.interaction.panel.components.tabs.modules;

import java.awt.*;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;

import forgprod.abilities.conversion.support.ProductionConstants;
import forgprod.abilities.interaction.panel.ControlPanelManager;
import forgprod.abilities.interaction.panel.PanelConstants;
import forgprod.abilities.interaction.panel.components.Common;
import forgprod.abilities.interaction.panel.components.tabs.ModulesTab;
import forgprod.abilities.interaction.panel.objects.Button;
import forgprod.abilities.interaction.panel.objects.ExclusiveButton;
import forgprod.abilities.modules.dataholders.ProductionModule;

/**
 * @author Ontheheavens
 * @since 01.01.2023
 */

public class ModuleList {

    private static CustomPanelAPI listInstance;

    private static boolean listRedrawQueued;

    public static void create(CustomPanelAPI moduleTab) {
        float width = 250f;
        float listHeight = 478f;
        float verticalOffset = PanelConstants.HEADER_HEIGHT + 27f;
        TooltipMakerAPI moduleListHeader = createHeader(moduleTab, width, listHeight);
        moduleTab.addUIElement(moduleListHeader).inTL(PanelConstants.PANEL_CONTENT_OFFSET + 2f, -verticalOffset);
        ModuleList.renderModuleList();
    }

    public static boolean isListRedrawQueued() {
        return listRedrawQueued;
    }

    public static void setListRedrawQueued(boolean listRedrawQueued) {
        ModuleList.listRedrawQueued = listRedrawQueued;
    }

    public static void renderModuleList() {
        CustomPanelAPI tabInstance = ModulesTab.getTabInstance();
        if (tabInstance == null) return;
        if (listInstance != null) {
            tabInstance.removeComponent(listInstance);
        }
        CustomPanelAPI moduleList = createModuleList(tabInstance);
        float verticalOffset = PanelConstants.HEADER_HEIGHT + 27f;
        tabInstance.addComponent(moduleList).inTL(PanelConstants.PANEL_CONTENT_OFFSET + 2f, -(verticalOffset - 22f));
        ModuleList.listInstance = moduleList;
    }

    private static CustomPanelAPI createModuleList(CustomPanelAPI moduleTab) {
        float width = 250f;
        float listHeight = 478f;
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        List<ProductionModule> modules = ModulesTab.getModulesForList(fleet, true);
        CustomPanelAPI listContainer = moduleTab.createCustomPanel(width - 2f,
                listHeight, ControlPanelManager.getInstance().getPlugin());
        TooltipMakerAPI moduleList = listContainer.createUIElement(width - 2f, listHeight, true);
        moduleList.setForceProcessInput(true);
        if (modules.size() == 0) {
            addHiddenModulesNote(moduleList, fleet);
        } else {
            for (ProductionModule module : modules) {
                addModuleEntry(moduleList, module, width, module == ModulesTab.getModuleQueuedToShow());
            }
        }
        float verticalOffsetMultiplier = 110f;
        moduleList.addSpacer(-(modules.size() * verticalOffsetMultiplier));
        listContainer.addUIElement(moduleList);
        return listContainer;
    }

    private static TooltipMakerAPI createHeader(CustomPanelAPI moduleTab, float width, float listHeight) {
        TooltipMakerAPI moduleListHeader = moduleTab.createUIElement(width, 20f, false);
        moduleListHeader.addSectionHeading("Ships", Alignment.MID, 2f);
        moduleListHeader.setForceProcessInput(false);
        UIComponentAPI rightBoundaryLine = ModuleList.addScrollerBackgroundLine(moduleListHeader, listHeight - 6f);
        rightBoundaryLine.getPosition().inTL(width - 6f, 3f);
        rightBoundaryLine.getPosition().setYAlignOffset(-19f);
        ModuleList.addSortingButtons(moduleListHeader);
        return moduleListHeader;
    }

    private static void addSortingButtons(TooltipMakerAPI moduleListHeader) {
        ButtonAPI toggleHeavyIndustryButton = createDisplayTagButton(moduleListHeader,
                ProductionConstants.HEAVY_INDUSTRY_MODULE);
        toggleHeavyIndustryButton.getPosition().inTR(1f, 1f);
        ButtonAPI toggleFuelProductionButton = createDisplayTagButton(moduleListHeader,
                ProductionConstants.FUEL_PRODUCTION_MODULE);
        toggleFuelProductionButton.getPosition().leftOfMid(toggleHeavyIndustryButton, 2f);
        ButtonAPI toggleRefiningButton = createDisplayTagButton(moduleListHeader,
                ProductionConstants.REFINING_MODULE);
        toggleRefiningButton.getPosition().leftOfMid(toggleFuelProductionButton, 2f);
    }

    private static ButtonAPI createDisplayTagButton(TooltipMakerAPI moduleListHeader, final String associatedHullmodId) {
        Color buttonColor = Misc.interpolateColor(Misc.scaleColorOnly(PanelConstants.DARK_PLAYER_COLOR, 1.2f),
                PanelConstants.PLAYER_COLOR, 0.1f);
        Color highlightColor = Misc.interpolateColor(Misc.scaleColorOnly(PanelConstants.PLAYER_COLOR, 0.9f),
                PanelConstants.DARK_PLAYER_COLOR, 0.1f);
        String firstLetter = Character.toString(ProductionConstants.HULLMOD_NAMES.get(associatedHullmodId).charAt(0));
        ButtonAPI tagButtonInstance = moduleListHeader.addButton(firstLetter, null,
                highlightColor, buttonColor, Alignment.MID,
                CutStyle.ALL, 16, 16f, 0f);
        if (ModulesTab.MODULE_DISPLAY_TAGS.get(associatedHullmodId)) {
            tagButtonInstance.highlight();
        }
        final String moduleName = ProductionConstants.HULLMOD_NAMES.get(associatedHullmodId);
        final float nameWidth = moduleListHeader.computeStringWidth(moduleName);
        TooltipMakerAPI.TooltipCreator tagButtonTooltip = new BaseTooltipCreator() {
            @Override
            public float getTooltipWidth(Object tooltipParam) {
                return 185f + nameWidth;
            }
            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                tooltip.addTitle("Display Options");
                String status = "Shown";
                if (!ModulesTab.MODULE_DISPLAY_TAGS.get(associatedHullmodId)) {
                    status = "Hidden";
                }
                tooltip.addPara("Ships with %s installed: %s", 6f, Misc.getTextColor(),
                        Misc.getHighlightColor(), moduleName,
                        status);
            }
        };
        moduleListHeader.addTooltipToPrevious(tagButtonTooltip, TooltipMakerAPI.TooltipLocation.ABOVE);
        new Button(tagButtonInstance, Button.Type.DISPLAY_TAG) {
            @Override
            public void applyEffect() {
                if (!ModulesTab.MODULE_DISPLAY_TAGS.get(associatedHullmodId)) {
                    ModulesTab.MODULE_DISPLAY_TAGS.put(associatedHullmodId, true);
                    this.getInner().highlight();
                } else {
                    ModulesTab.MODULE_DISPLAY_TAGS.put(associatedHullmodId, false);
                    this.getInner().unhighlight();
                }
                ModuleList.setListRedrawQueued(true);
            }
        };
        return tagButtonInstance;
    }

    private static UIComponentAPI addScrollerBackgroundLine(TooltipMakerAPI moduleListHeader, float height) {
        TooltipMakerAPI lineContainer = moduleListHeader.beginImageWithText(null, 2f);
        Color transparent = Misc.scaleAlpha(PanelConstants.DARK_PLAYER_COLOR, 0f);
        Color playerColor = PanelConstants.PLAYER_COLOR;
        Color modified = new Color(playerColor.getRed() - 20, playerColor.getGreen() - 10, playerColor.getBlue());
        ButtonAPI scrollerLine = lineContainer.addAreaCheckbox("", null, modified,
                transparent, modified, 5f, height + 10f, 0f);
        scrollerLine.setHighlightBrightness(1f);
        scrollerLine.highlight();
        scrollerLine.getPosition().setXAlignOffset(-240f);
        scrollerLine.setEnabled(false);
        moduleListHeader.addImageWithText(0f);
        return moduleListHeader.getPrev();
    }

    private static void addModuleEntry(TooltipMakerAPI moduleList, final ProductionModule module,
                                       float width, boolean createChecked) {
        ButtonAPI frameCheckbox = addCheckbox(moduleList, module, width, createChecked);
        frameCheckbox.getPosition().setXAlignOffset(0f);
        moduleList.addSpacer(0f);
        moduleList.getPrev().getPosition().setXAlignOffset(0f);
        UIComponentAPI shipIcon = Common.addShipIcon(moduleList, module, 48f, 2f, false);
        shipIcon.getPosition().belowLeft(frameCheckbox, -110f);
        addModuleIcon(moduleList, module);
        addModuleInfo(moduleList, module);
        moduleList.addSpacer(-194f);
    }

    private static ButtonAPI addCheckbox(TooltipMakerAPI moduleList, final ProductionModule module,
                                         float width, boolean createChecked) {
        Color checkedColor = Misc.scaleColorOnly(PanelConstants.DARK_PLAYER_COLOR, 0.7f);
        ButtonAPI checkbox = moduleList.addAreaCheckbox("", null, PanelConstants.PLAYER_COLOR,
                checkedColor, PanelConstants.BRIGHT_PLAYER_COLOR,
                width - 7f, 120f, 1f);
        if (createChecked) {
            checkbox.setChecked(true);
            checkbox.highlight();
            checkbox.setEnabled(false);
            checkbox.setButtonDisabledPressedSound("ui_button_pressed");
        }
        new ExclusiveButton(checkbox, Button.Type.MODULE, createChecked) {
            @Override
            public void applyEffect() {
                if (!(ModulesTab.getShownModule() == module) || ModulesTab.getModuleCard() == null) {
                    ModulesTab.setModuleQueuedToShow(module);
                    ModulesTab.setCardRedrawQueued(true);
                    this.getInner().highlight();
                    this.getInner().setEnabled(false);
                    this.getInner().setButtonDisabledPressedSound("ui_button_pressed");
                }
            }
        };
        return checkbox;
    }

    private static void addModuleIcon(TooltipMakerAPI moduleList, ProductionModule module) {
        TooltipMakerAPI modulePanel = moduleList.beginImageWithText(null, 32f);
        modulePanel.addImage(Global.getSettings().getSpriteName("forgprod_ui", "frame_small"),
                36f, 36f, 20f);
        UIComponentAPI moduleIconFrame = modulePanel.getPrev();
        moduleIconFrame.getPosition().setXAlignOffset(-(222f));
        moduleIconFrame.getPosition().setYAlignOffset(45f);
        modulePanel.addImage(Global.getSettings().getSpriteName("forgprod_hullmods", module.getHullmodId()),
                32f, 32f, 20f);
        float iconOffset = modulePanel.getPrev().getPosition().getY() - moduleIconFrame.getPosition().getY();
        PositionAPI iconPosition = modulePanel.getPrev().getPosition();
        iconPosition.belowMid(moduleIconFrame, iconOffset + 18f);
        moduleList.addImageWithText(2f);
    }

    private static void addModuleInfo(TooltipMakerAPI moduleList, ProductionModule module) {
        TooltipMakerAPI moduleInfoPanel = moduleList.beginImageWithText(null, 64f);
        FleetMemberAPI member = module.getParentFleetMember();
        moduleInfoPanel.setTextWidthOverride(200f);
        String shortenedShipName = moduleInfoPanel.shortenString(member.getShipName(), 175);
        moduleInfoPanel.setParaFont("graphics/fonts/orbitron12condensed.fnt");
        LabelAPI shipNameLabel = moduleInfoPanel.addPara(shortenedShipName, PanelConstants.PLAYER_COLOR, 2f);
        moduleInfoPanel.setParaFontDefault();
        shipNameLabel.getPosition().setXAlignOffset(-175f);
        shipNameLabel.getPosition().setYAlignOffset(216f);
        moduleInfoPanel.addPara(member.getHullSpec().getHullNameWithDashClass(), 2f);
        moduleInfoPanel.addPara(member.getVariant().getDesignation(), 2f);
        String moduleName = ProductionConstants.HULLMOD_NAMES.get(module.getHullmodId());
        moduleInfoPanel.addSpacer(20f);
        moduleInfoPanel.addPara(moduleName, Misc.getHighlightColor(), 2f);
        moduleList.addImageWithText(2f);
    }

    private static void addHiddenModulesNote(TooltipMakerAPI moduleList, CampaignFleetAPI fleet) {
        moduleList.setParaFont("graphics/fonts/insignia21LTaa.fnt");
        moduleList.setTextWidthOverride(210f);
        LabelAPI noEligibleNote = moduleList.addPara("No module meets display criteria.", Misc.getGrayColor(), 0f);
        noEligibleNote.getPosition().inMid();
        noEligibleNote.setAlignment(Alignment.MID);
        noEligibleNote.getPosition().setYAlignOffset(-210f);
        int totalModules = ModulesTab.getModulesForList(fleet, false).size();
        LabelAPI hiddenCountNote = moduleList.addPara("(" + totalModules + " modules hidden)", Misc.getGrayColor(), 0f);
        hiddenCountNote.getPosition().belowLeft((UIComponentAPI) noEligibleNote, 4f);
        hiddenCountNote.getPosition().setXAlignOffset(22f);
        moduleList.setParaFontDefault();
    }

}
