package forgprod.abilities.interaction.panel.components;

import java.awt.*;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;

import forgprod.abilities.ForgeProductionAbility;
import forgprod.abilities.conversion.support.checks.FleetwideProductionChecks;
import forgprod.abilities.interaction.panel.ControlPanelAssembly;
import forgprod.abilities.interaction.panel.ControlPanelManager;
import forgprod.abilities.interaction.panel.PanelConstants;
import forgprod.abilities.interaction.panel.objects.Button;
import forgprod.abilities.interaction.panel.objects.ExclusiveButton;
import forgprod.hullmods.tooltip.TooltipModuleSection;
import forgprod.settings.SettingsHolder;

import static forgprod.abilities.interaction.panel.PanelConstants.PANEL_CONTENT_OFFSET;

/**
 * @author Ontheheavens
 * @since 29.12.2022
 */

public class Header {

    public static TooltipMakerAPI create(CustomPanelAPI panel, float width) {
        TooltipMakerAPI header = panel.createUIElement(width - PANEL_CONTENT_OFFSET - 4f,
                PanelConstants.HEADER_HEIGHT, false);
        header.addSectionHeading("Forge production", Alignment.MID, 2f);
        UIComponentAPI headerAnchor = header.addSpacer(0f);
        header.setForceProcessInput(true);
        UIComponentAPI headerInfoContainer = Header.createHeaderInfo(panel, width);
        header.addCustom(headerInfoContainer, 0f).getPosition().inTL(0f, 0f);
        ButtonAPI tabButtonsLine = Common.addLine(header, width - PANEL_CONTENT_OFFSET - 263f);
        tabButtonsLine.getPosition().belowLeft(headerAnchor, PanelConstants.HEADER_HEIGHT + 1f);
        tabButtonsLine.getPosition().setXAlignOffset(-5f);
        addTabButtons(header);
        panel.addUIElement(header).inTMid(PANEL_CONTENT_OFFSET + 2f);
        return header;
    }

    // Final section needs to have width of 330f.
    private static UIComponentAPI createHeaderInfo(CustomPanelAPI panel, float width) {
        CustomPanelAPI headerInfoContainer = panel.createCustomPanel(width,
                PanelConstants.HEADER_HEIGHT - 20f, ControlPanelManager.getInstance().getPlugin());
        TooltipMakerAPI iconContainer = addReportIcon(headerInfoContainer);
        headerInfoContainer.addUIElement(iconContainer).inTL(5f, 35f);
        TooltipMakerAPI framing = addFraming(headerInfoContainer);
        headerInfoContainer.addUIElement(framing).inTL(5f, 27f);
        TooltipMakerAPI coordinationSection = addCoordinationSection(headerInfoContainer);
        headerInfoContainer.addUIElement(coordinationSection).inTL(79f, 34f);
        TooltipMakerAPI safetySection = addSafetySection(headerInfoContainer);
        headerInfoContainer.addUIElement(safetySection).rightOfTop(coordinationSection, 6f);
        TooltipMakerAPI logisticsSection = addLogisticsSection(headerInfoContainer);
        headerInfoContainer.addUIElement(logisticsSection).rightOfTop(safetySection, 6f);
        return headerInfoContainer;
    }

    private static TooltipMakerAPI addFraming(CustomPanelAPI headerInfoContainer) {
        float width = 916f;
        float height = 84f;
        TooltipMakerAPI framingContainer = headerInfoContainer.createUIElement(width, height, false);
        ButtonAPI topLine = Common.addLine(framingContainer, width);
        topLine.getPosition().inTL(0f, 0f);
        ButtonAPI bottomLine = Common.addLine(framingContainer, width);
        bottomLine.getPosition().inTL(0f, height);
        return framingContainer;
    }

    private static TooltipMakerAPI addReportIcon(CustomPanelAPI headerInfoContainer) {
        TooltipMakerAPI iconContainer = headerInfoContainer.createUIElement(80f, 80f, false);
        String frame = "frame";
        if (Global.getSector().getPlayerFleet().getAbility("forge_production").isActiveOrInProgress()) {
            frame = "frame_bright";
        }
        String frameIconName = Global.getSettings().getSpriteName("forgprod_ui", frame);
        float frameWidth = 68f;
        iconContainer.addImage(frameIconName, frameWidth, 0f);
        UIComponentAPI frameInstance = iconContainer.getPrev();
        frameInstance.getPosition().inTL(0f, 0f);
        String reportIconName = Global.getSettings().getSpriteName("forgprod_intel",
                "forge_production_report_big");
        iconContainer.addImage(reportIconName,  0f);
        UIComponentAPI iconInstance = iconContainer.getPrev();
        iconInstance.getPosition().rightOfMid(frameInstance, -66f);
        ButtonAPI frameTopLine = Common.addLine(iconContainer, frameWidth);
        frameTopLine.getPosition().aboveLeft(frameInstance, 1f);
        ButtonAPI frameBottomLine = Common.addLine(iconContainer, frameWidth);
        frameBottomLine.getPosition().belowLeft(frameInstance, 1f);
        return iconContainer;
    }

    private static TooltipMakerAPI addCoordinationSection(CustomPanelAPI headerInfoContainer) {
        TooltipMakerAPI sectionContainer = headerInfoContainer.createUIElement(250f, 85f, false);
        String abilityStatusValue = "Standby";
        Color statusColor = Misc.getHighlightColor();
        int totalVolume = FleetwideProductionChecks.getTotalModuleVolume();
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        AbilityPlugin forgProdAbilityPlugin = fleet.getAbility("forge_production");
        ForgeProductionAbility castedAbilityPlugin = (ForgeProductionAbility) forgProdAbilityPlugin;
        if (castedAbilityPlugin.isActiveOrInProgress()) {
            abilityStatusValue = "Ongoing";
        } else if (castedAbilityPlugin.isOnCooldown()) {
            abilityStatusValue = "Terminating";
            statusColor = Misc.getNegativeHighlightColor();
        } else if (totalVolume < 1 || !FleetwideProductionChecks.hasMinimumMachinery(fleet) ||
                !castedAbilityPlugin.checkUsableAbilities()) {
            abilityStatusValue = "Disabled";
            statusColor = Misc.getGrayColor();
        }
        sectionContainer.addSectionHeading("Overview", Alignment.MID, 0f);
        sectionContainer.addSpacer(5f);
        sectionContainer.getPrev().getPosition().setXAlignOffset(-2f);
        addSpecLineToSection(sectionContainer, "Production status:", abilityStatusValue,
                statusColor, false);
        sectionContainer.addSpacer(2f);
        int modulesCount = FleetwideProductionChecks.getTotalModuleCount();
        Color modulesColor = Misc.getHighlightColor();
        if (modulesCount < 1) {
            modulesColor = Misc.getNegativeHighlightColor();
        }
        addSpecLineToSection(sectionContainer, "Total modules installed:", String.valueOf(modulesCount),
                modulesColor, false);
        sectionContainer.addSpacer(2f);
        String moduleVolume = totalVolume + Strings.X;
        Color volumeColor = Misc.getHighlightColor();
        if (totalVolume < 1) {
            volumeColor = Misc.getNegativeHighlightColor();
        }
        addSpecLineToSection(sectionContainer, "Total module volume:", moduleVolume, volumeColor, false);
        return sectionContainer;
    }

    private static TooltipMakerAPI addSafetySection(CustomPanelAPI headerInfoContainer) {
        TooltipMakerAPI sectionContainer = headerInfoContainer.createUIElement(250f, 85f, false);
        sectionContainer.addSectionHeading("Safety", Alignment.MID, 0f);
        sectionContainer.addSpacer(5f);
        sectionContainer.getPrev().getPosition().setXAlignOffset(-2f);
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        List<Float> statistics = TooltipModuleSection.getFleetBreakdownStats(fleet);
        float breakdownChance = statistics.get(0);
        float breakdownSizeMinimal = statistics.get(1);
        float breakdownSizeMaximal = statistics.get(2);
        float accidentChance = statistics.get(3);
        Color highlight = Misc.getHighlightColor();
        addSpecLineToSection(sectionContainer, "Breakdown chance:",
                (Misc.getRoundedValueMaxOneAfterDecimal(breakdownChance * 100f)) + "%",
                highlight, false);
        sectionContainer.addSpacer(2f);
        String breakdownSizeValue = (Misc.getRoundedValueMaxOneAfterDecimal(breakdownSizeMinimal * 100f) + "%") +
                " - " + (Misc.getRoundedValueMaxOneAfterDecimal(breakdownSizeMaximal * 100f) + "%");
        addSpecLineToSection(sectionContainer, "Breakdown size:", breakdownSizeValue, highlight, false);
        sectionContainer.addSpacer(2f);
        addSpecLineToSection(sectionContainer, "Casualty chance:",
                Misc.getRoundedValueMaxOneAfterDecimal((breakdownChance * accidentChance) * 100f) + "%",
                highlight, false);
        return sectionContainer;
    }

    private static TooltipMakerAPI addLogisticsSection(CustomPanelAPI headerInfoContainer) {
        float width = 330f;
        TooltipMakerAPI sectionContainer = headerInfoContainer.createUIElement(width, 85f, false);
        sectionContainer.addSectionHeading("Upkeep", Alignment.MID, 0f);
        sectionContainer.addSpacer(5f);
        sectionContainer.getPrev().getPosition().setXAlignOffset(-2f);
        Color highlight = Misc.getHighlightColor();
        int totalVolume = FleetwideProductionChecks.getTotalModuleVolume();
        int crewmanSalary = Global.getSettings().getInt("crewSalary");
        int salaryIncrease = totalVolume * SettingsHolder.CREW_REQ_PER_CAPACITY * crewmanSalary;
        int supplyIncrease = totalVolume * SettingsHolder.SUPPLY_REQ_PER_CAPACITY;
        addSpecLineToSection(sectionContainer, "Active production CR decrease:",
                Misc.getRoundedValueMaxOneAfterDecimal((SettingsHolder.DAILY_CR_DECREASE) * 100f) + "%",
                highlight, true);
        sectionContainer.addSpacer(2f);
        addSpecLineToSection(sectionContainer, "Total additional crew salary:",
                (Misc.getRoundedValueMaxOneAfterDecimal(salaryIncrease)) + Strings.C,
                highlight, true);
        sectionContainer.addSpacer(2f);
        addSpecLineToSection(sectionContainer, "Total monthly maintenance:", String.valueOf(supplyIncrease),
                highlight, true);
        return sectionContainer;
    }

    private static void addSpecLineToSection(TooltipMakerAPI sectionContainer, String desc,
                                             String value, Color valueColor, boolean lastSection) {
        TooltipMakerAPI lineContainer = sectionContainer.beginImageWithText(null, 2f);
        lineContainer.addSpacer(0f);
        float widthOffset = 0f;
        if (lastSection) {
            widthOffset = 80f;
        }
        lineContainer.getPrev().getPosition().setXAlignOffset(-(242f + widthOffset));
        lineContainer.setTextWidthOverride(250f + widthOffset);
        LabelAPI descriptionLabel = lineContainer.addPara(desc, 0f);
        LabelAPI descriptionValue;
        if (desc.equals("Total monthly maintenance:")) {
            descriptionValue = lineContainer.addPara("%s supplies", 0f,
                    Misc.getHighlightColor(), value);
        } else {
            descriptionValue = lineContainer.addPara(value, valueColor, 0f);
        }
        descriptionValue.getPosition().rightOfMid((UIComponentAPI) descriptionLabel, -(252f + widthOffset));
        descriptionValue.getPosition().setYAlignOffset(1f);
        descriptionValue.setAlignment(Alignment.RMID);
        lineContainer.addSpacer(-15f);
        sectionContainer.addImageWithText(0f);
    }

    // Offsets by X-axis for consequent buttons are width of button (250f) plus spacer (3f).
    private static void addTabButtons(TooltipMakerAPI header) {
        ButtonAPI modulesTabButton = createTabButton(header, PanelConstants.ShownTab.MODULES);
        float height = PanelConstants.HEADER_HEIGHT + 1f;
        modulesTabButton.getPosition().inTL(0f, height);
    }

    @SuppressWarnings("SameParameterValue")
    private static ButtonAPI createTabButton(TooltipMakerAPI header, final PanelConstants.ShownTab tab) {
        ButtonAPI tabButtonInstance = header.addButton(PanelConstants.TAB_NAMES.get(tab), null,
                PanelConstants.PLAYER_COLOR, PanelConstants.DARK_PLAYER_COLOR, Alignment.MID,
                CutStyle.TOP, 250f, 18f, 2f);
        if (ControlPanelAssembly.getShownTab() == tab) {
            tabButtonInstance.highlight();
        }
        new ExclusiveButton(tabButtonInstance, Button.Type.TAB) {
            @Override
            public void applyEffect() {
                if (ControlPanelAssembly.getShownTab() != tab) {
                    ControlPanelAssembly.setShownTab(tab);
                    ControlPanelManager.getInstance().setQueuedTabRedraw(true);
                    this.getInner().highlight();
                }
            }
        };
        return tabButtonInstance;
    }

}
