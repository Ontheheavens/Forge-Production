package forgprod.abilities.interaction.panel.components.tabs.modules;

import java.awt.*;
import java.util.List;
import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.ListMap;
import com.fs.starfarer.api.util.Misc;

import forgprod.abilities.conversion.logic.production.HullPartsProductionLogic;
import forgprod.abilities.conversion.support.ProductionType;
import forgprod.abilities.interaction.panel.ControlPanelManager;
import forgprod.abilities.interaction.panel.PanelConstants;
import forgprod.abilities.interaction.panel.components.Common;
import forgprod.abilities.interaction.panel.objects.Button;
import forgprod.abilities.interaction.panel.objects.ExclusiveButton;
import forgprod.abilities.modules.FleetwideModuleManager;
import forgprod.hullmods.tooltip.TooltipCapacitySection;

import static forgprod.abilities.conversion.support.ProductionConstants.HULL_PARTS_OUTPUT;
import static forgprod.abilities.conversion.support.checks.FleetwideProductionChecks.getFleetCapacityForType;
import static forgprod.abilities.conversion.support.checks.ItemBonusesChecks.hasSpecialItem;

/**
 * @author Ontheheavens
 * @since 20.01.2023
 */

public class HullBlueprintSection {

    private static CustomPanelAPI sectionInstance;
    private static TooltipMakerAPI variantCardInstance;
    private static boolean cardRedrawQueued;

    public static CustomPanelAPI create(CustomPanelAPI moduleCard, float width) {
        CustomUIPanelPlugin plugin = ControlPanelManager.getInstance().getPlugin();
        CustomPanelAPI blueprintSection = moduleCard.createCustomPanel(width, 200, plugin);
        HullBlueprintSection.sectionInstance = blueprintSection;
        TooltipMakerAPI header = HullBlueprintSection.addHeader(blueprintSection, width);
        blueprintSection.addUIElement(header).inTL(0f, 0f);
        HullBlueprintSection.renderVariantCard();
        float listWidth = 295f;
        float listHeight = 104f;
        TooltipMakerAPI listFrame = HullBlueprintSection.addListFrame(blueprintSection, listWidth, listHeight);
        blueprintSection.addUIElement(listFrame).inTR(12f, 26f);
        TooltipMakerAPI blueprintList = HullBlueprintSection.addBlueprintList(blueprintSection,
                listWidth + 1f, listHeight - 4f);
        blueprintSection.addUIElement(blueprintList).inTR(11f, 49f);
        moduleCard.addComponent(blueprintSection);
        return blueprintSection;
    }

    public static boolean isCardRedrawQueued() {
        return cardRedrawQueued;
    }

    public static void setCardRedrawQueued(boolean cardRedrawQueued) {
        HullBlueprintSection.cardRedrawQueued = cardRedrawQueued;
    }

    public static void renderVariantCard() {
        if (sectionInstance == null) return;
        if (variantCardInstance != null) {
            sectionInstance.removeComponent(variantCardInstance);
        }
        TooltipMakerAPI variantCard = HullBlueprintSection.createVariantCard(sectionInstance);
        sectionInstance.addUIElement(variantCard).inTL(12f, 26f);
        HullBlueprintSection.variantCardInstance = variantCard;
    }

    private static TooltipMakerAPI addHeader(CustomPanelAPI blueprintSection, float width) {
        TooltipMakerAPI sectionHeader = blueprintSection.createUIElement(width, 100, false);
        sectionHeader.setForceProcessInput(true);
        Color transparent = Misc.scaleAlpha(PanelConstants.DARK_PLAYER_COLOR, 0f);
        String label = "Hulls";
        float labelWidth = sectionHeader.computeStringWidth(label);
        float lineWidth = (width - (labelWidth + 22f)) / 2;
        sectionHeader.addSectionHeading(label, PanelConstants.PLAYER_COLOR, transparent, Alignment.MID, 0f);
        ButtonAPI titleLeftLine = Common.addLine(sectionHeader, lineWidth);
        float verticalOffset = -10f;
        titleLeftLine.getPosition().inTL(0f, 0f);
        titleLeftLine.getPosition().setYAlignOffset(verticalOffset);
        ButtonAPI titleRightLine = Common.addLine(sectionHeader, lineWidth);
        titleRightLine.getPosition().inTR(0f, 0f);
        titleRightLine.getPosition().setYAlignOffset(verticalOffset);
        return sectionHeader;
    }

    private static TooltipMakerAPI createVariantCard(CustomPanelAPI blueprintSection) {
        float cardWidth = 90f;
        float height = 104f;
        TooltipMakerAPI variantCard = blueprintSection.createUIElement(cardWidth, height, false);
        variantCard.addSectionHeading("Blueprint", Alignment.MID, 3f);
        TooltipMakerAPI.TooltipCreator qualityOverviewTooltip = new BaseTooltipCreator() {
            @Override
            public float getTooltipWidth(Object tooltipParam) {
                return 350f;
            }
            @SuppressWarnings("UnusedAssignment")
            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                HullPartsProductionLogic logic = HullPartsProductionLogic.getInstance();
                CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
                tooltip.addTitle("Quality Modifiers", PanelConstants.PLAYER_COLOR);
                String quality = (int) (logic.getHullQuality() * 100f) + "%";
                float averageDMods = DefaultFleetInflater.getAverageDmodsForQuality(logic.getHullQuality());
                String averageDModsRounded = Misc.getRoundedValueMaxOneAfterDecimal(averageDMods);
                tooltip.addPara("Ship quality: %s, an average of %s d-mods per hull.", 10f,
                        Misc.getHighlightColor(), quality, averageDModsRounded);
                tooltip.beginGridFlipped(340f, 1, 45f, 6f);
                int row = 0;
                tooltip.addToGrid(0, row++, "Parts production: Makeshift Production Forge", "+20%");
                if (logic.hasOperationalSalvageRigs()) {
                    tooltip.addToGrid(0, row++, "Hull assembly: Salvage Rigs", "+10%");
                }
                if (hasSpecialItem(fleet, Items.PRISTINE_NANOFORGE)) {
                    tooltip.addToGrid(0, row++, "Special equipment: Pristine Nanoforge", "+50%");
                } else if (hasSpecialItem(fleet, Items.CORRUPTED_NANOFORGE)) {
                    tooltip.addToGrid(0, row++, "Special equipment: Corrupted Nanoforge", "+20%");
                }
                tooltip.addGrid(4f);

            }
        };
        variantCard.addTooltipToPrevious(qualityOverviewTooltip, TooltipMakerAPI.TooltipLocation.ABOVE);
        ButtonAPI headingLine = Common.addLine(variantCard, cardWidth);
        headingLine.getPosition().setXAlignOffset(-5f);
        UIComponentAPI frame = addFrameBox(variantCard, cardWidth, height);
        frame.getPosition().inTL(10f, 22f);
        UIComponentAPI variantIcon = addVariantIcon(variantCard);
        variantIcon.getPosition().rightOfBottom(headingLine, -81f);
        variantIcon.getPosition().setYAlignOffset(-86f);
        UIComponentAPI progressBar = addProgressBar(variantCard, cardWidth);
        progressBar.getPosition().rightOfBottom(frame, -cardWidth + 12f);
        progressBar.getPosition().setYAlignOffset(3f);
        UIComponentAPI specifications = addBlueprintSpec(variantCard);
        specifications.getPosition().rightOfTop(frame, -62f);
        specifications.getPosition().setYAlignOffset(30f);
        return variantCard;
    }

    private static UIComponentAPI addFrameBox(TooltipMakerAPI variantCard, float width, float height) {
        TooltipMakerAPI frameContainer = variantCard.beginImageWithText(null, 2f);
        frameContainer.setForceProcessInput(false);
        Color baseBoxColor = Misc.interpolateColor(PanelConstants.DARK_PLAYER_COLOR,
                PanelConstants.BRIGHT_PLAYER_COLOR, 0.5f);
        Color boxColor = Misc.scaleColorOnly(baseBoxColor, 0.9f);
        ButtonAPI box = frameContainer.addAreaCheckbox("", null, PanelConstants.PLAYER_COLOR,
                boxColor, PanelConstants.PLAYER_COLOR,
                width, height, 0f);
        box.getPosition().setXAlignOffset(-width);
        ButtonAPI barToppingLine = Common.addLine(frameContainer, width - 2f);
        barToppingLine.getPosition().belowLeft(box, -22f);
        barToppingLine.getPosition().setXAlignOffset(1f);
        variantCard.addImageWithText(0f);
        return variantCard.getPrev();
    }

    private static UIComponentAPI addVariantIcon(TooltipMakerAPI variantCard) {
        List<FleetMemberAPI> variantAsList = new ArrayList<>();
        ShipVariantAPI designatedVariant = FleetwideModuleManager.getInstance().getDesignatedVariant();
        FleetMemberAPI dummyMember = Global.getFactory().createFleetMember(FleetMemberType.SHIP, designatedVariant);
        variantAsList.add(dummyMember);
        variantCard.addShipList(1, 1, 72f, PanelConstants.PLAYER_COLOR, variantAsList, 0f);
        return variantCard.getPrev();
    }

    private static UIComponentAPI addProgressBar(TooltipMakerAPI variantCard, float width) {
        TooltipMakerAPI barContainer = variantCard.beginImageWithText(null, 2f);
        barContainer.setForceProcessInput(false);
        HullPartsProductionLogic logic = HullPartsProductionLogic.getInstance();
        float cost = logic.computeDesignatedHullCostInParts();
        float accumulated = FleetwideModuleManager.getInstance().getAccumulatedHullParts();
        float progressMultiplier = accumulated / cost;
        if (progressMultiplier > 1f) {
            progressMultiplier = 1f;
        }
        TextFieldAPI bar = barContainer.addTextField((width - 6f) * progressMultiplier, 16f,
                "graphics/fonts/orbitron12condensed.fnt", 0f);
        bar.setMidAlignment();
        Color backgroundColor = Misc.interpolateColor(PanelConstants.DARK_PLAYER_COLOR,
                PanelConstants.BRIGHT_PLAYER_COLOR, 0.65f);
        bar.setBgColor(backgroundColor);
        PositionAPI barPosition = barContainer.getPrev().getPosition();
        barPosition.setXAlignOffset(-width + 1f);
        barPosition.setYAlignOffset(-15f);
        barContainer.setTextWidthOverride(100f);
        String progressValue = Misc.getRoundedValueMaxOneAfterDecimal(100f * progressMultiplier);
        LabelAPI progressLabel = barContainer.addPara(progressValue + "%", PanelConstants.PLAYER_COLOR, 0f);
        progressLabel.getPosition().setYAlignOffset(16f);
        progressLabel.getPosition().setXAlignOffset(-8f);
        progressLabel.setAlignment(Alignment.MID);
        variantCard.addImageWithText(0f);
        return variantCard.getPrev();
    }

    private static UIComponentAPI addBlueprintSpec(TooltipMakerAPI variantCard) {
        TooltipMakerAPI specContainer = variantCard.beginImageWithText(null, 2f);
        FleetwideModuleManager manager = FleetwideModuleManager.getInstance();
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        ShipVariantAPI variant = manager.getDesignatedVariant();
        specContainer.addSpacer(4f);
        String creditsIconName = Global.getSettings().getSpriteName("forgprod_ui", "credits");
        HullPartsProductionLogic logic = HullPartsProductionLogic.getInstance();
        Color highlight = Misc.getHighlightColor();
        addSpecificationLine(specContainer, logic.getHullPrice(variant), creditsIconName, "Price", highlight);
        String partsIconName = TooltipCapacitySection.getCapacitySpriteName(ProductionType.HULL_PARTS_PRODUCTION);
        float costValue = logic.computeDesignatedHullCostInParts();
        addSpecificationLine(specContainer, costValue, partsIconName, "Cost", highlight);
        addTooltipToCostLine(specContainer, variant);
        float stockValue = manager.getAccumulatedHullParts();
        addSpecificationLine(specContainer, stockValue, partsIconName, "Stock", highlight);
        float capacityFromModules = getFleetCapacityForType(fleet, ProductionType.HULL_PARTS_PRODUCTION);
        float capacityByRigs = logic.getPossibleAssemblingCapacity();
        float hullPartsIncome = Math.min(capacityFromModules, capacityByRigs) * HULL_PARTS_OUTPUT;
        Color incomeColor = highlight;
        if (capacityFromModules > capacityByRigs) {
            incomeColor = Misc.getNegativeHighlightColor();
        }
        addSpecificationLine(specContainer, hullPartsIncome, partsIconName, "Income", incomeColor);
        addTooltipToIncomeLine(specContainer);
        String hullsIconName = Global.getSettings().getSpriteName("forgprod_ui", "hull_parts_symbolic");
        float hullsOutput = hullPartsIncome / costValue;
        addSpecificationLine(specContainer, hullsOutput, hullsIconName, "Output", incomeColor);
        variantCard.addImageWithText(2f);
        return variantCard.getPrev();
    }

    private static void addTooltipToCostLine(TooltipMakerAPI specContainer, final ShipVariantAPI variant) {
        String costTooltipAnchorName = Global.getSettings().getSpriteName("forgprod_ui",
                "cost_tooltip_anchor");
        specContainer.setForceProcessInput(true);
        specContainer.addImage(costTooltipAnchorName, 0f);
        TooltipMakerAPI.TooltipCreator costTooltip = new BaseTooltipCreator() {
            @Override
            public float getTooltipWidth(Object tooltipParam) {
                return 215f;
            }
            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                tooltip.addTitle("Cost Modifiers", PanelConstants.PLAYER_COLOR);
                tooltip.beginGridFlipped(210f, 1, 45f, 6f);
                if (variant.getHullSpec().hasTag(Factions.DERELICT)) {
                    tooltip.addToGrid(0, 0, "Built-in blueprint", Strings.X + "0.6");
                } else {
                    tooltip.addToGrid(0, 0, "Extraneous blueprint", Strings.X + "1.8");
                }
                if (variant.getHullSpec().getShieldType() != ShieldAPI.ShieldType.NONE) {
                    tooltip.addToGrid(0, 1, "Shield emitter machinery", Strings.X + "1.2");
                }
                tooltip.addGrid(4f);
                tooltip.addSpacer(2f);
            }
        };
        specContainer.addTooltipToPrevious(costTooltip, TooltipMakerAPI.TooltipLocation.LEFT);
        UIComponentAPI anchorImage = specContainer.getPrev();
        anchorImage.getPosition().setYAlignOffset(15f);
        float offset = -11f;
        anchorImage.getPosition().setXAlignOffset(offset);
        specContainer.addSpacer(2f);
        specContainer.getPrev().getPosition().setXAlignOffset(-offset);
    }

    private static void addTooltipToIncomeLine(TooltipMakerAPI specContainer) {
        String incomeTooltipAnchorName = Global.getSettings().getSpriteName("forgprod_ui",
                "income_tooltip_anchor");
        specContainer.setForceProcessInput(true);
        specContainer.addImage(incomeTooltipAnchorName, 0f);
        TooltipMakerAPI.TooltipCreator costTooltip = new BaseTooltipCreator() {
            @Override
            public float getTooltipWidth(Object tooltipParam) {
                return 350f;
            }
            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                tooltip.addTitle("Hull Parts Assembly", PanelConstants.PLAYER_COLOR);
                tooltip.addPara("Hull parts produced by individual forge modules are " +
                        "unsuitable for bulk storage and need to be assembled into complete hulls " +
                        "in short order.", 10f);
                tooltip.addPara("Since proper orbital works are unavailable in circumstance of moving fleet, " +
                        "hull assembly must involve makeshift scaffolding and manual " +
                        "parts fitting.", 6f);
                tooltip.addPara("Hence, production is effectively " +
                        "limited by %s, which can be increased by " +
                        "acquiring construction vessels like %s.", 6f, Misc.getHighlightColor(),
                        "assembling capacity", "Salvage Rigs");
                tooltip.addSpacer(12f);
                float separatorWidth = 120f;
                ButtonAPI separatorLeft = Common.addLine(tooltip, separatorWidth);
                LabelAPI separatorTitle = tooltip.addTitle("Limiting factors", PanelConstants.PLAYER_COLOR);
                separatorTitle.getPosition().rightOfMid(separatorLeft, 4f);
                ButtonAPI separatorRight = Common.addLine(tooltip, separatorWidth);
                separatorRight.getPosition().rightOfMid((UIComponentAPI) separatorTitle, 4f);
                separatorRight.getPosition().setXAlignOffset(-244f);
                separatorRight.getPosition().setYAlignOffset(-1f);
                CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
                HullPartsProductionLogic logic = HullPartsProductionLogic.getInstance();
                int productionFromModules = (int) (getFleetCapacityForType(fleet, ProductionType.HULL_PARTS_PRODUCTION)
                        * HULL_PARTS_OUTPUT);
                int assemblyCapacityFromRigs = (int) (logic.getPossibleAssemblingCapacity() * HULL_PARTS_OUTPUT);
                tooltip.beginGrid(310f, 1);
                tooltip.addToGrid(0, 0, "Total module production:", productionFromModules + Strings.X);
                Color assemblyCapacityColor = Misc.getHighlightColor();
                if (productionFromModules > assemblyCapacityFromRigs) {
                    assemblyCapacityColor = Misc.getNegativeHighlightColor();
                }
                tooltip.addToGrid(0, 1, "Total assembling capacity:",
                        assemblyCapacityFromRigs + Strings.X, assemblyCapacityColor);
                tooltip.addGrid(12f);
                tooltip.getPrev().getPosition().setXAlignOffset(-210f);
                tooltip.addSpacer(-10f);
            }
        };
        specContainer.addTooltipToPrevious(costTooltip, TooltipMakerAPI.TooltipLocation.ABOVE);
        UIComponentAPI anchorImage = specContainer.getPrev();
        anchorImage.getPosition().setYAlignOffset(15f);
        float offset = -10f;
        anchorImage.getPosition().setXAlignOffset(offset);
        specContainer.addSpacer(2f);
        specContainer.getPrev().getPosition().setXAlignOffset(-offset);
    }

    private static void addSpecificationLine(TooltipMakerAPI variantCard, float value,
                                             String iconName, String description, Color valueColor) {
        TooltipMakerAPI specificationLine = variantCard.beginImageWithText(null, 32f);
        String valueName = "Hull Parts";
        if (description.equals("Output")) {
            valueName = "Hulls";
        } else if (description.equals("Price")) {
            valueName = "Credits";
        }
        specificationLine.setTextWidthOverride(250f);
        specificationLine.addPara(description + ":", 0f);
        UIComponentAPI desc = specificationLine.getPrev();
        desc.getPosition().setXAlignOffset(0f);
        specificationLine.addImage(iconName, 24f, 24f, 0f);
        UIComponentAPI icon = specificationLine.getPrev();
        icon.getPosition().rightOfMid(desc, PanelConstants.SPEC_LINE_ICON_OFFSET);
        String displayedValueLabel = Misc.getRoundedValueMaxOneAfterDecimal(value);
        String format = displayedValueLabel + "×";
        if (description.equals("Price")) {
            displayedValueLabel = Misc.getWithDGS(value);
            format = displayedValueLabel;
        }
        specificationLine.setParaFontColor(valueColor);
        specificationLine.addPara(format, 0f).getPosition().rightOfTop(desc, -345f);
        LabelAPI valueLabel = (LabelAPI) specificationLine.getPrev();
        valueLabel.getPosition().setYAlignOffset(1f);
        valueLabel.setAlignment(Alignment.RMID);
        specificationLine.setParaFontColor(Misc.getTextColor());
        specificationLine.addPara(valueName, 1f).getPosition().rightOfMid(desc, -90f);
        variantCard.addImageWithText(6f);
        variantCard.addSpacer(-53f);
    }

    private static TooltipMakerAPI addListFrame(CustomPanelAPI blueprintSection, float frameWidth,
                                                float frameHeight) {
        TooltipMakerAPI listFrame = blueprintSection.createUIElement(frameWidth, frameHeight, false);
        listFrame.addSectionHeading("Available", Alignment.MID, 3f);
        TooltipMakerAPI.TooltipCreator blueprintsOverviewTooltip = new BaseTooltipCreator() {
            @Override
            public float getTooltipWidth(Object tooltipParam) {
                return 350f;
            }
            @Override
            public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                tooltip.addTitle("Blueprint Limitations", PanelConstants.PLAYER_COLOR);
                tooltip.addPara("Developed by Domain engineers in times before advent of High Tech " +
                        "fleet doctrines, Mothership autoforge modules were not expected to produce " +
                        "anything but the simplest hull parts and systems of Explorarium drones.", 10f);
                tooltip.addPara("Consequently, even the basic Low Tech spaceship blueprints loaded " +
                        "in the module through software modifications are manufactured with " +
                        "considerably lower efficiency, while any piece of more delicate machinery like " +
                        "phase coils, flux shunts or drive field modulators is completely out of reach for " +
                        "autoforge production capacities.", 6f);
                Color[] highlights = {Misc.getHighlightColor(), Misc.getDesignTypeColor("Explorarium"),
                        Misc.getDesignTypeColor("Low Tech")};
                tooltip.addPara("Only %s blueprints of %s or %s design types are supported.", 6f,
                        highlights, "Frigate", "Explorarium", "Low Tech");
                tooltip.addPara("Unsupported built-ins:", 6f);
                tooltip.setBulletedListMode("   - ");
                tooltip.setBulletColor(Misc.getTextColor());
                tooltip.setParaFontColor(Misc.getHighlightColor());
                List<String> unsupportedHullmods = getUnsupportedHullmodsIds();
                for (String hullmodId : unsupportedHullmods) {
                    String hullmodName = Global.getSettings().getHullModSpec(hullmodId).getDisplayName();
                    tooltip.addPara(hullmodName, 4f);
                }
                tooltip.setBulletedListMode("");
                tooltip.setParaFontColor(Misc.getTextColor());
            }
        };
        listFrame.addTooltipToPrevious(blueprintsOverviewTooltip, TooltipMakerAPI.TooltipLocation.ABOVE);
        ButtonAPI footingLine = Common.addLine(listFrame, frameWidth);
        footingLine.getPosition().setYAlignOffset(-(frameHeight + 3f));
        footingLine.getPosition().setXAlignOffset(-5f);
        return listFrame;
    }

    private static TooltipMakerAPI addBlueprintList(CustomPanelAPI blueprintSection, float listWidth,
                                                    float listHeight) {
        TooltipMakerAPI listPanel = blueprintSection.createUIElement(listWidth, listHeight, true);
        List<ShipVariantAPI> blueprints = sortBlueprintsByCost(getAvailableBlueprints());
        float summaryOffset = 0f;
        for (ShipVariantAPI blueprint : blueprints) {
            addBlueprintEntry(listPanel, blueprint, listWidth);
            summaryOffset += 92f;
        }
        listPanel.addSpacer(-summaryOffset);
        return listPanel;
    }

    private static void addBlueprintEntry(TooltipMakerAPI listPanel, ShipVariantAPI blueprint, float entryWidth) {
        boolean createChecked = FleetwideModuleManager.getInstance().getDesignatedVariant() == blueprint;
        ButtonAPI frameCheckbox = addCheckbox(listPanel, blueprint, entryWidth - 5f, createChecked);
        frameCheckbox.getPosition().setXAlignOffset(0f);
        listPanel.addSpacer(0f);
        listPanel.getPrev().getPosition().setXAlignOffset(0f);
        FleetMemberAPI dummyMember = Global.getFactory().createFleetMember(FleetMemberType.SHIP, blueprint);
        List<FleetMemberAPI> memberAsList = new ArrayList<>();
        memberAsList.add(dummyMember);
        listPanel.addShipList(1, 1, 30f, PanelConstants.PLAYER_COLOR, memberAsList, 0f);
        UIComponentAPI hullIcon = listPanel.getPrev();
        hullIcon.getPosition().setYAlignOffset(30f);
        String hullClass = blueprint.getHullSpec().getHullNameWithDashClass();
        LabelAPI hullClassLabel = listPanel.addPara(hullClass, PanelConstants.PLAYER_COLOR, 0f);
        hullClassLabel.getPosition().rightOfMid(hullIcon, 4f);
        hullClassLabel.getPosition().setYAlignOffset(0f);
        listPanel.addImage(TooltipCapacitySection.getCapacitySpriteName(ProductionType.HULL_PARTS_PRODUCTION),
                24f, 24f, 0f);
        UIComponentAPI partsIcon = listPanel.getPrev();
        partsIcon.getPosition().rightOfMid(hullIcon, 228f);
        partsIcon.getPosition().setYAlignOffset(0f);
        HullPartsProductionLogic logic = HullPartsProductionLogic.getInstance();
        String hullCost = Misc.getRoundedValueMaxOneAfterDecimal(logic.computeHullCostInParts(blueprint)) + "×";
        LabelAPI hullCostLabel = listPanel.addPara(hullCost, Misc.getHighlightColor(), 0f);
        hullCostLabel.getPosition().leftOfMid(partsIcon, 2f);
        hullCostLabel.getPosition().setYAlignOffset(0f);
        hullCostLabel.setAlignment(Alignment.RMID);
        listPanel.addSpacer(10f);
        listPanel.getPrev().getPosition().setXAlignOffset(25f);
    }

    private static ButtonAPI addCheckbox(TooltipMakerAPI listPanel, final ShipVariantAPI blueprint,
                                         float width, boolean createChecked) {
        Color checkedColor = Misc.scaleColorOnly(PanelConstants.DARK_PLAYER_COLOR, 0.7f);
        ButtonAPI checkbox = listPanel.addAreaCheckbox("", null, PanelConstants.PLAYER_COLOR,
                Misc.scaleAlpha(checkedColor, 0.99f), PanelConstants.BRIGHT_PLAYER_COLOR,
                width, 30f, 1f);
        if (createChecked) {
            checkbox.setChecked(true);
            checkbox.highlight();
            checkbox.setEnabled(false);
            checkbox.setButtonDisabledPressedSound("ui_button_pressed");
        }
        new ExclusiveButton(checkbox, Button.Type.BLUEPRINT, createChecked) {
            @Override
            public void applyEffect() {
                if (!(FleetwideModuleManager.getInstance().getDesignatedVariant() == blueprint) ||
                      HullBlueprintSection.variantCardInstance == null) {
                    FleetwideModuleManager.getInstance().setDesignatedVariant(blueprint);
                    HullBlueprintSection.setCardRedrawQueued(true);
                    this.getInner().highlight();
                    this.getInner().setEnabled(false);
                    this.getInner().setButtonDisabledPressedSound("ui_button_pressed");
                }
            }
        };
        return checkbox;
    }

    private static Set<ShipVariantAPI> getAvailableBlueprints() {
        Set<String> allKnown = Global.getSector().getPlayerFaction().getKnownShips();
        Set<ShipVariantAPI> resultVariants = new HashSet<>();
        ListMap<String> allVariants = Global.getSettings().getHullIdToVariantListMap();
        for (String blueprintId : allKnown) {
            ShipHullSpecAPI spec = Global.getSettings().getHullSpec(blueprintId);
            if (!isEligibleHull(spec) || !isEligibleTech(spec)) {
                continue;
            }
            List<String> hullVariants = allVariants.getList(spec.getBaseHullId());
            ShipVariantAPI targetVariant = null;
            for (String variantId : hullVariants) {
                ShipVariantAPI checked = Global.getSettings().getVariant(variantId);
                if (checked.isGoalVariant()) {
                    targetVariant = checked;
                }
            }
            if (targetVariant != null) {
                resultVariants.add(targetVariant);
            }
        }
        resultVariants.add(Global.getSettings().getVariant("picket_Assault"));
        resultVariants.add(Global.getSettings().getVariant("warden_Defense"));
        resultVariants.add(Global.getSettings().getVariant("sentry_FS"));
        return resultVariants;
    }

    private static List<ShipVariantAPI> sortBlueprintsByCost(Set<ShipVariantAPI> blueprintSet) {
        List<ShipVariantAPI> result = new ArrayList<>(blueprintSet);
        Collections.sort(result, new Comparator<ShipVariantAPI>() {
            @Override
            public int compare(ShipVariantAPI first, ShipVariantAPI second) {
                HullPartsProductionLogic logic = HullPartsProductionLogic.getInstance();
                Float firstCost = logic.computeHullCostInParts(first);
                Float secondCost = logic.computeHullCostInParts(second);
                return firstCost.compareTo(secondCost);
            }
        });
        return result;
    }

    private static List<String> getUnsupportedHullmodsIds() {
        List<String> unsupportedHullmods = new ArrayList<>();
        unsupportedHullmods.add("phasefield");
        unsupportedHullmods.add("fluxshunt");
        unsupportedHullmods.add("delicate");
        unsupportedHullmods.add("high_maintenance");
        unsupportedHullmods.add("drive_field_stabilizer");
        return unsupportedHullmods;
    }

    private static boolean isEligibleHull(ShipHullSpecAPI spec) {
        if (spec.getHullSize() != ShipAPI.HullSize.FRIGATE) {
            return false;
        }
        List<String> unsupportedHullmods = getUnsupportedHullmodsIds();
        for (String hullmod : unsupportedHullmods) {
            if (spec.isBuiltInMod(hullmod)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isEligibleTech(ShipHullSpecAPI spec) {
        Set<String> eligibleTech = new HashSet<>();
        eligibleTech.add("Explorarium");
        eligibleTech.add("Low Tech");
        for (String tech : eligibleTech) {
            if (spec.getManufacturer().equals(tech)) {
                return true;
            }
        }
        return false;
    }

}
