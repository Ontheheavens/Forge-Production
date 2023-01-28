package forgprod.abilities.tooltip.components;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;

import forgprod.abilities.conversion.support.ProductionConstants;
import forgprod.abilities.conversion.support.ProductionType;
import forgprod.abilities.modules.FleetwideModuleManager;
import forgprod.abilities.modules.dataholders.ProductionCapacity;
import forgprod.abilities.modules.dataholders.ProductionModule;
import forgprod.settings.SettingsHolder;

public class TooltipShipSection {

    public static void addShipList(TooltipMakerAPI tooltip, CampaignFleetAPI fleet) {
        tooltip.addSectionHeading("Production modules", Alignment.MID, 10f);
        tooltip.addSpacer(6f);
        tooltip.getPrev().getPosition().setXAlignOffset(6);
        Color textColor = Misc.getTextColor();
        Color highlightColor = Misc.getHighlightColor();
        FleetwideModuleManager.getInstance().refreshIndexes(fleet);
        List<FleetMemberAPI> members = getShipsWithModulesSorted(fleet);
        int displayThatMany = SettingsHolder.SHIP_LIST_SIZE;
        int shipsOverTooltipCap = members.size() - displayThatMany;
        int counter = 1;
        for (FleetMemberAPI member : members) {
            if (counter > displayThatMany) {
                break;
            }
            addModulePanel(tooltip, member);
            counter++;
        }
        tooltip.addSpacer(2f);
        if ((counter > displayThatMany) && !(shipsOverTooltipCap == 0)) {
            String shipsOverCapString = String.valueOf(shipsOverTooltipCap);
            String format = "...and %s more.";
            tooltip.addPara(format, 0f, textColor, highlightColor, shipsOverCapString);
        }
        tooltip.addSpacer(-2f);
        tooltip.getPrev().getPosition().setXAlignOffset(-6f);
    }

    private static List<FleetMemberAPI> getShipsWithModulesSorted(CampaignFleetAPI fleet) {
        List<FleetMemberAPI> membersWithModules = new LinkedList<>();
        List<FleetMemberAPI> inactiveMembers = new LinkedList<>();
        Map<FleetMemberAPI, ProductionModule> moduleIndex = FleetwideModuleManager.getInstance().getModuleIndex();
        List<FleetMemberAPI> membersList = fleet.getFleetData().getMembersListCopy();
        for (FleetMemberAPI member : membersList) {
            if (moduleIndex.get(member) != null) {
                if (moduleIndex.get(member).hasActiveCapacities()) {
                    membersWithModules.add(member);
                } else {
                    inactiveMembers.add(member);
                }
            }
        }
        membersWithModules.addAll(inactiveMembers);
        return membersWithModules;
    }

    private static void addModulePanel(TooltipMakerAPI tooltip, FleetMemberAPI member) {
        Color textColor = Misc.getTextColor();
        Color nameColor = Misc.getBasePlayerColor();
        String shipName = tooltip.shortenString(member.getShipName(), 175);
        String shipClass = member.getHullSpec().getHullNameWithDashClass();
        ProductionModule module = FleetwideModuleManager.getInstance().getModuleIndex().get(member);
        boolean hasSecondCapacity = (module.getModuleCapacities().size() > 1);
        ProductionCapacity firstCapacityInstance = module.getModuleCapacities().get(0);
        ProductionCapacity secondCapacityInstance = null;
        if (hasSecondCapacity) {
            secondCapacityInstance = module.getModuleCapacities().get(1);
        }
        TooltipMakerAPI modulePanel = tooltip.beginImageWithText(null, 32f);
        List<FleetMemberAPI> iconList = new ArrayList<>();
        iconList.add(member);
        modulePanel.addShipList(1, 1, 36f, nameColor, iconList, 4f);
        UIComponentAPI memberIcon = modulePanel.getPrev();
        memberIcon.getPosition().setXAlignOffset(-355f);
        modulePanel.setTextWidthOverride(200f);
        modulePanel.addPara(shipName, nameColor, 0f).getPosition().rightOfTop(memberIcon, 10f);
        modulePanel.addPara(shipClass, textColor, 0f).getPosition().rightOfBottom(memberIcon, 10f);
        if (!hasSecondCapacity) {
            addCapacityLine(modulePanel, firstCapacityInstance, memberIcon, Alignment.RMID);
        } else {
            addCapacityLine(modulePanel, firstCapacityInstance, memberIcon, Alignment.TR);
            addCapacityLine(modulePanel, secondCapacityInstance, memberIcon, Alignment.BR);
        }
        modulePanel.addSpacer(0f).getPosition().belowLeft(memberIcon, 0f);
        String frameName = Global.getSettings().getSpriteName("forgprod_ui", "frame_small");
        modulePanel.addImage(frameName, 28f, 28f, 0f);
        UIComponentAPI frame = modulePanel.getPrev();
        frame.getPosition().rightOfMid(memberIcon, 283f);
        frame.getPosition().setYAlignOffset(-1f);
        String moduleIconName = Global.getSettings().getSpriteName("forgprod_hullmods", module.getHullmodId());
        modulePanel.addImage(moduleIconName, 24f, 24f, 0f);
        UIComponentAPI moduleIcon = modulePanel.getPrev();
        moduleIcon.getPosition().belowMid(frame, -26f);
        tooltip.addImageWithText(2f);
        tooltip.addSpacer(-93f);
        if (hasSecondCapacity) {
            tooltip.addSpacer(-17f);
        }
    }

    private static void addCapacityLine(TooltipMakerAPI modulePanel, ProductionCapacity capacity,
                                        UIComponentAPI anchor, Alignment place) {
        ProductionType type = capacity.getProductionType();
        String capacityName = ProductionConstants.PRODUCTION_NAMES.get(type);
        Color capacityColor = Misc.getHighlightColor();
        if ((!capacity.isActive() || capacity.getCurrentThroughput() <= 0f) && capacity.isToggledOn()) {
            capacityColor = Misc.getNegativeHighlightColor();
        } else if (!capacity.isToggledOn()) {
            capacityColor = Misc.getGrayColor();
        }
        float capacityLineXOffset = 83f;
        if (place == Alignment.TR) {
            modulePanel.addPara(capacityName, capacityColor, 1f).getPosition().rightOfTop(anchor, capacityLineXOffset);
        } else if (place == Alignment.BR) {
            modulePanel.addPara(capacityName, capacityColor, 1f).getPosition().rightOfBottom(anchor, capacityLineXOffset);
        } else if (place == Alignment.RMID) {
            modulePanel.addPara(capacityName, capacityColor, 1f).getPosition().rightOfMid(anchor, capacityLineXOffset);
        }
        LabelAPI capacityLabel = (LabelAPI) modulePanel.getPrev();
        capacityLabel.setAlignment(Alignment.RMID);
        capacityLabel.getPosition().setXAlignOffset(75f);
        capacityLabel.getPosition().setYAlignOffset(1f);
        modulePanel.addSpacer(0f);
        modulePanel.getPrev().getPosition().setXAlignOffset(10f);
    }

}
