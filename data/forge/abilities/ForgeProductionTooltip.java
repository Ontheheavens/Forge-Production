package data.forge.abilities;

import java.awt.*;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Collections;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.forge.abilities.tooltip.ForgeTooltipBreakdown;
import data.forge.abilities.tooltip.ForgeTooltipItems;
import data.forge.campaign.ForgeConditionChecker;

import static data.forge.plugins.ForgeSettings.*;
import static data.forge.campaign.ForgeConditionChecker.isOperational;

public class ForgeProductionTooltip {

    public static void addOperationalShipsBreakdown(TooltipMakerAPI tooltip) {
        
        Color textColor = Misc.getTextColor();
        Color highlightColor = Misc.getHighlightColor();
        Color positiveHighlightColor = Misc.getPositiveHighlightColor();

        String indent = "   ";
        float pad = 6f;

        tooltip.addSectionHeading("Operational ships", Alignment.MID, pad);
        tooltip.addSpacer(6f);

        java.util.List<FleetMemberAPI> forgeShips = getOperationalForgeShipsList();

        int counter = 1;
        int rowNumber = 0;
        int displayThatMany = SHIP_LIST_SIZE;
        int shipsOverTooltipCap = forgeShips.size() - displayThatMany;

        tooltip.beginGrid(370f, 1, textColor);

        for (FleetMemberAPI member : forgeShips) {

            if (counter > displayThatMany) {
                break;
            }

            String shipName = member.getShipName();
            String shipNameShort = tooltip.shortenString(shipName, 175);
            String shipClass = member.getHullSpec().getHullNameWithDashClass();
            String forgeModule = getForgeHullmodOfForgeShip(member);

            String label = indent + shipNameShort + ", " + shipClass;

            tooltip.addToGrid(0, rowNumber++, label, forgeModule, positiveHighlightColor);

            counter++;

        }

        tooltip.addGrid(1);

        if ((counter > displayThatMany) && !(shipsOverTooltipCap == 0)) {
            String shipsOverCapString = String.valueOf(shipsOverTooltipCap);
            String format = "   ...and %s more.";
            tooltip.addPara(format, 3, textColor, highlightColor, shipsOverCapString);
        }

    }


    public static void addInactiveShipsBreakdown(TooltipMakerAPI tooltip) {

        Color negativeHighlightColor = Misc.getNegativeHighlightColor();
        Color grayColor = Misc.getGrayColor();
        Color highlightColor = Misc.getHighlightColor();

        String indent = "   ";
        float pad = 6f;

        tooltip.addSectionHeading("Inactive ships", Alignment.MID, pad);
        tooltip.addSpacer(6f);

        List<FleetMemberAPI> forgeShips = getInactiveForgeShipsList();

        int counter = 1;
        int rowNumber = 0;
        int displayThatMany = SHIP_LIST_SIZE;
        int shipsOverTooltipCap = forgeShips.size() - displayThatMany;

        tooltip.beginGrid(370f, 1, grayColor);

        for (FleetMemberAPI member : forgeShips) {

            if (counter > displayThatMany) {
                break;
            }

            String shipName = member.getShipName();
            String shipNameShort = tooltip.shortenString(shipName, 175);
            String shipClass = member.getHullSpec().getHullNameWithDashClass();
            String forgeModule = getForgeHullmodOfForgeShip(member);

            String label = indent + shipNameShort + ", " + shipClass;

            tooltip.addToGrid(0, rowNumber++, label, forgeModule, negativeHighlightColor);
            counter++;

        }

        tooltip.addGrid(3);

        if ((counter > displayThatMany) && !(shipsOverTooltipCap == 0)) {
            String shipsOverCapString = String.valueOf(shipsOverTooltipCap);
            String format = "   ...and %s more.";
            tooltip.addPara(format, 3, grayColor, highlightColor, shipsOverCapString);
        }
    }

    public static void addExpandedInfo(TooltipMakerAPI tooltip) {

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        if (ForgeConditionChecker.hasAnyForgingCapacity(fleet)) {
            ForgeTooltipBreakdown.addProductionBreakdown(tooltip);
        }

        if (ForgeConditionChecker.hasAnySpecialItem()) {
            addSpecialItemsBreakdown(tooltip);
        }

    }

    public static void addSpecialItemsBreakdown(TooltipMakerAPI tooltip) {

        float pad = 10f;
        tooltip.addSectionHeading("Special effects", Alignment.MID, pad);
        tooltip.addSpacer(4f);

        if (ForgeConditionChecker.hasSpecialItem(Items.CORRUPTED_NANOFORGE)) {
            ForgeTooltipItems.addCorruptedNanoforgeNote(tooltip);
        }

        if (ForgeConditionChecker.hasSpecialItem(Items.PRISTINE_NANOFORGE)) {
            ForgeTooltipItems.addPristineNanoforgeNote(tooltip);
        }

        if (ForgeConditionChecker.hasSpecialItem(Items.CATALYTIC_CORE)) {
            ForgeTooltipItems.addCatalyticCoreNote(tooltip);
        }

        if (ForgeConditionChecker.hasSpecialItem(Items.SYNCHROTRON)) {
            ForgeTooltipItems.addSynchrotronCoreNote(tooltip);
        }

    }

    protected static List<FleetMemberAPI> getOperationalForgeShipsList() {
        return getForgeShipsWithActivity(true);
    }

    protected static List<FleetMemberAPI> getInactiveForgeShipsList() {
        return getForgeShipsWithActivity(false);
    }

    protected static List<FleetMemberAPI> getForgeShipsWithActivity(boolean isActiveCheck) {
        List<String> forgeHullMods = new ArrayList<>();{
            forgeHullMods.add("forge_refinery_module");
            forgeHullMods.add("forge_centrifuge_module");
            forgeHullMods.add("forge_manufacture_module");
            forgeHullMods.add("forge_assembly_module");}
        List<FleetMemberAPI> membersWithHullMod = new LinkedList<>();
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        for (FleetMemberAPI forgeShip : playerFleet.getFleetData().getMembersListCopy()) {
            if (isActiveCheck ^ isOperational(forgeShip)) {
                continue;}
            if (!Collections.disjoint(forgeShip.getVariant().getHullMods(), forgeHullMods))
                membersWithHullMod.add(forgeShip);
        }
        return membersWithHullMod;
    }

    protected static String getForgeHullmodOfForgeShip(FleetMemberAPI member) {

        if (member.getVariant().hasHullMod("forge_refinery_module")) {
            return "Refinery";
        }

        if (member.getVariant().hasHullMod("forge_centrifuge_module")) {
            return "Centrifuge";
        }

        if (member.getVariant().hasHullMod("forge_manufacture_module")) {
            return "Manufacture";
        }

        if (member.getVariant().hasHullMod("forge_assembly_module")) {
            return "Assembly";
        }

        return null;
    }

}
