package forgprod.abilities.interaction.panel.components.tabs;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;

import forgprod.abilities.conversion.support.ProductionConstants;
import forgprod.abilities.interaction.panel.components.tabs.modules.ModuleCard;
import forgprod.abilities.interaction.panel.components.tabs.modules.ModuleList;
import forgprod.abilities.modules.FleetwideModuleManager;
import forgprod.abilities.modules.dataholders.ProductionModule;

import static forgprod.abilities.interaction.panel.PanelConstants.PANEL_CONTENT_OFFSET;

/**
 * @author Ontheheavens
 * @since 29.12.2022
 */

public class ModulesTab {

    private static ProductionModule shownModule;

    private static ProductionModule moduleQueuedToShow;

    private static CustomPanelAPI tabInstance;

    private static CustomPanelAPI moduleCard;

    private static boolean cardRedrawQueued;

    public static Map<String, Boolean> MODULE_DISPLAY_TAGS = new HashMap<>();
    static {
        MODULE_DISPLAY_TAGS.put(ProductionConstants.REFINING_MODULE, true);
        MODULE_DISPLAY_TAGS.put(ProductionConstants.FUEL_PRODUCTION_MODULE, true);
        MODULE_DISPLAY_TAGS.put(ProductionConstants.HEAVY_INDUSTRY_MODULE, true);
    }

    public static CustomPanelAPI create(CustomPanelAPI panel, float width, CustomUIPanelPlugin plugin) {
        CustomPanelAPI moduleTab = panel.createCustomPanel(width, 400f, plugin);
        ModulesTab.tabInstance = moduleTab;
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        if (fleet == null) return null;
        List<ProductionModule> modules = getModulesForList(fleet, false);
        if (modules.size() == 0) {
            ModulesTab.addNoModulesNote(panel, width);
            return moduleTab;
        }
        if (shownModule != null) {
            moduleQueuedToShow = shownModule;
        } else {
            moduleQueuedToShow = modules.get(0);
        }
        ModuleList.create(moduleTab);
        ModulesTab.renderModuleCard();
        PositionAPI tabPosition = panel.addComponent(moduleTab);
        tabPosition.setYAlignOffset(1f);
        return moduleTab;
    }

    public static void setModuleCard(CustomPanelAPI moduleCard) {
        ModulesTab.moduleCard = moduleCard;
    }

    public static void setModuleQueuedToShow(ProductionModule module) {
        ModulesTab.moduleQueuedToShow = module;
    }

    public static ProductionModule getModuleQueuedToShow() {
        return moduleQueuedToShow;
    }

    public static CustomPanelAPI getTabInstance() {
        return tabInstance;
    }

    public static ProductionModule getShownModule() {
        return shownModule;
    }

    public static CustomPanelAPI getModuleCard() {
        return moduleCard;
    }

    public static boolean isCardRedrawQueued() {
        return cardRedrawQueued;
    }

    public static void setCardRedrawQueued(boolean cardRedrawQueued) {
        ModulesTab.cardRedrawQueued = cardRedrawQueued;
    }

    private static boolean isDisplayTagEnabled(ProductionModule module) {
        String hullmodId = module.getHullmodId();
        return MODULE_DISPLAY_TAGS.get(hullmodId);
    }

    public static List<ProductionModule> getModulesForList(CampaignFleetAPI fleet, boolean applyTags) {
        List<ProductionModule> modules = new LinkedList<>();
        Map<FleetMemberAPI, ProductionModule> moduleIndex = FleetwideModuleManager.getInstance().getModuleIndex();
        List<FleetMemberAPI> membersList = fleet.getFleetData().getMembersListCopy();
        for (FleetMemberAPI member : membersList) {
            ProductionModule checkedModule = moduleIndex.get(member);
            if (checkedModule != null) {
                if (applyTags && !isDisplayTagEnabled(checkedModule)) {
                    continue;
                }
                modules.add(checkedModule);
            }
        }
        return modules;
    }

    public static void renderModuleCard() {
        if (tabInstance == null) return;
        if (moduleCard != null) {
            tabInstance.removeComponent(moduleCard);
        }
        // Additional check in case refresh failed for some reason.
        if (moduleQueuedToShow.getModuleCapacities() == null) {
            FleetwideModuleManager.getInstance().refreshIndexes(Global.getSector().getPlayerFleet());
            return;
        }
        boolean queuedModuleHasCapacities = moduleQueuedToShow.getModuleCapacities().size() > 0;
        if (!queuedModuleHasCapacities) return;
        CustomPanelAPI moduleCard = ModuleCard.create(tabInstance, moduleQueuedToShow);
        shownModule = moduleQueuedToShow;
        tabInstance.addComponent(moduleCard).inTL(263f, -147f);
        ModulesTab.moduleCard = moduleCard;
    }

    public static void addNoModulesNote(CustomPanelAPI panel, float width) {
        TooltipMakerAPI notePanel = panel.createUIElement(width - PANEL_CONTENT_OFFSET - 4f,
                120f, false);
        notePanel.setForceProcessInput(false);
        notePanel.setParaFont("graphics/fonts/insignia21LTaa.fnt");
        notePanel.setTextWidthOverride(800f);
        LabelAPI noteText = notePanel.addPara("Your fleet does not have any production modules installed.",
                Misc.getGrayColor(), 20f);
        noteText.getPosition().setXAlignOffset(210f);
        noteText.getPosition().setYAlignOffset(-380f);
        panel.addUIElement(notePanel).inTMid(PANEL_CONTENT_OFFSET + 2f);
    }

}
