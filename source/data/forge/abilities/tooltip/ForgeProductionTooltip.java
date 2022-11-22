package data.forge.abilities.tooltip;

import java.awt.*;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Collections;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.forge.campaign.ForgeConditionChecker;

import static data.forge.campaign.ForgeConditionChecker.*;

public class ForgeProductionTooltip {

    public static void addCannotForgeNoActiveShips(TooltipMakerAPI tooltip) {
        float pad = 6f;
        Color negativeHighlight = Misc.getNegativeHighlightColor();
        tooltip.addPara("Your fleet currently cannot conduct forging operations: no active forge ships.", negativeHighlight, pad);
    }

    public static void addCannotForgeNoMachinery(TooltipMakerAPI tooltip) {
        float pad = 6f;
        Color negativeHighlight = Misc.getNegativeHighlightColor();
        if (ForgeConditionChecker.getPlayerCargo(Commodities.HEAVY_MACHINERY) > 0) {
            tooltip.addPara("Production halted: insufficient machinery.", negativeHighlight, pad);
        } else {
            tooltip.addPara("Your fleet currently cannot conduct forging operations: no machinery available.", negativeHighlight, pad);
        }
    }

    public static void addExpandedInfo(TooltipMakerAPI tooltip, boolean isActive) {

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        if (hasActiveForgeShips()) {
            ForgeTooltipShips.addOperationalShipsBreakdown(tooltip);
        }

        if (hasInactiveForgeShips()) {
            ForgeTooltipShips.addInactiveShipsBreakdown(tooltip);
        }

        if (ForgeConditionChecker.hasAnyForgingCapacity(fleet) && ForgeConditionChecker.getPlayerCargo(Commodities.HEAVY_MACHINERY) >= 1) {
            ForgeTooltipBreakdown.addProductionBreakdown(tooltip, isActive);
        }

        if (ForgeConditionChecker.hasAnySpecialItem()) {
            ForgeProductionTooltip.addSpecialItemsBreakdown(tooltip);
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
