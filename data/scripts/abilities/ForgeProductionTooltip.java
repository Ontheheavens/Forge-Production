package data.scripts.abilities;

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
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import data.scripts.abilities.conversion.ForgeRefiningLogic;
import data.scripts.campaign.ForgeConditionChecker;

import static data.scripts.plugins.ForgeSettings.*;
import static data.scripts.abilities.conversion.ForgeRefiningLogic.getRefiningCapacity;
import static data.scripts.abilities.conversion.ForgeCentrifugingLogic.getCentrifugingCapacity;
import static data.scripts.campaign.ForgeConditionChecker.isOperational;

public class ForgeProductionTooltip {

    public static void addOperationalShipsBreakdown(TooltipMakerAPI tooltip) {
        
        Color textColor = Misc.getTextColor();
        Color highlightColor = Misc.getHighlightColor();
        Color positiveHighlightColor = Misc.getPositiveHighlightColor();

        String indent = "   ";
        float pad = 10f;

        tooltip.addPara("Ships with operational forging capabilities:", pad);

        java.util.List<FleetMemberAPI> forgeShips = getOperationalForgeShipsList();

        int counter = 1;
        int displayThatMany = SHIP_LIST_SIZE;
        int shipsOverTooltipCap = forgeShips.size() - displayThatMany;

        for (FleetMemberAPI member : forgeShips) {

            if (counter > displayThatMany) {
                String shipsOverCapString = String.valueOf(shipsOverTooltipCap);
                String format = "   ...and %s more.";
                tooltip.addPara(format, 2, textColor, highlightColor, shipsOverCapString);
                break;
            }

            String shipName = member.getShipName();
            String shipClass = member.getHullSpec().getHullNameWithDashClass();
            String forgeModule = getForgeHullmodOfForgeShip(member);
            String format = indent + shipName + ", " + shipClass + " - " + forgeModule;

            tooltip.addPara(format, 2, highlightColor, positiveHighlightColor, getForgeHullmodOfForgeShip(member));
            counter++;

        }
    }


    public static void addInactiveShipsBreakdown(TooltipMakerAPI tooltip) {

        Color negativeHighlight = Misc.getNegativeHighlightColor();
        Color gray = Misc.getGrayColor();
        Color highlightColor = Misc.getHighlightColor();

        String indent = "   ";
        float pad = 10f;

            tooltip.addPara("Ships with inactive forge modules:", pad);

            List<FleetMemberAPI> forgeShips = getInactiveForgeShipsList();

            int counter = 1;
            int displayThatMany = SHIP_LIST_SIZE;
            int shipsOverTooltipCap = forgeShips.size() - displayThatMany;

            for (FleetMemberAPI member : forgeShips) {

                if (counter > displayThatMany) {
                    String shipsOverCapString = String.valueOf(shipsOverTooltipCap);
                    String format = "   ...and %s more.";
                    tooltip.addPara(format, 2, gray, highlightColor, shipsOverCapString);
                    break;
                }

                String shipName = member.getShipName();
                String shipClass = member.getHullSpec().getHullNameWithDashClass();
                String forgeModule = getForgeHullmodOfForgeShip(member);
                String format = indent + shipName + ", " + shipClass + " - " + forgeModule;

                tooltip.addPara(format, 2, gray, negativeHighlight, getForgeHullmodOfForgeShip(member));
                counter++;

            }
    }

    public static void addExpandedInfo(TooltipMakerAPI tooltip) {

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        // Expand later
        if ((ForgeRefiningLogic.getRefiningCapacity(fleet) > 0) ||
                (getCentrifugingCapacity(fleet) > 0))
        {
            addProductionBreakdown(tooltip);
        }

        if (ForgeConditionChecker.hasSpecialItem(Items.CORRUPTED_NANOFORGE)) {
            addCorruptedNanoforgeNote(tooltip);
        }

        if (ForgeConditionChecker.hasSpecialItem(Items.PRISTINE_NANOFORGE)) {
            addPristineNanoforgeNote(tooltip);
        }

        if (ForgeConditionChecker.hasSpecialItem(Items.CATALYTIC_CORE)) {
            addCatalyticCoreNote(tooltip);
        }

        if (ForgeConditionChecker.hasSpecialItem(Items.SYNCHROTRON)) {
            addSynchrotronCoreNote(tooltip);
        }

    }

    public static void addProductionBreakdown(TooltipMakerAPI tooltip) {

        Color textColor = Misc.getTextColor();
        Color highlightColor = Misc.getHighlightColor();

        String indent = "   ";
        float pad = 10f;

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        tooltip.addPara("Forge production specifics:", pad);

        if (getRefiningCapacity(fleet) > 0) {

            int maxMetalProductionCapacity = ((int) Math.ceil(getRefiningCapacity(fleet) * (METAL_PRODUCED + ForgeConditionChecker.getCatalyticCoreBonus())));
            String metalFormat = indent + "Maximum metal production is " + Misc.getWithDGS(maxMetalProductionCapacity) + ".";

            tooltip.addPara(metalFormat, 2, textColor, highlightColor, Misc.getWithDGS(maxMetalProductionCapacity));

            int maxTransplutonicsProductionCapacity = (int) Math.ceil(getRefiningCapacity(fleet) * (TRANSPLUTONICS_PRODUCED + ForgeConditionChecker.getCatalyticCoreBonus()));
            String transplutonicsFormat = indent + "Maximum transplutonics production is " + Misc.getWithDGS(maxTransplutonicsProductionCapacity) + ".";

            tooltip.addPara(transplutonicsFormat, 2, textColor, highlightColor, Misc.getWithDGS(maxTransplutonicsProductionCapacity));

        }

        if (getCentrifugingCapacity(fleet) > 0) {

            Color negativeHighlight = Misc.getNegativeHighlightColor();

            int fuelInTanks = (int) fleet.getCargo().getCommodityQuantity(Commodities.FUEL);
            int maxTanksCapacity = (int) fleet.getCargo().getMaxFuel();
            int availableTanksCapacity = maxTanksCapacity - fuelInTanks;

            int maxFuelProductionCapacity = (int) Math.ceil(getCentrifugingCapacity(fleet) * (FUEL_PRODUCED + ForgeConditionChecker.getSynchrotronCoreBonus()));
            String fuelFormat = indent + "Maximum fuel production is " + Misc.getWithDGS(maxFuelProductionCapacity) + ".";
            String tanksAreFullFormat = indent + " (Fuel production is halted: tanks are almost full)";

            tooltip.addPara(fuelFormat, 2, textColor, highlightColor, Misc.getWithDGS(maxFuelProductionCapacity));

            if (availableTanksCapacity < (FUEL_PRODUCED + ForgeConditionChecker.getSynchrotronCoreBonus()) ) {
                tooltip.addPara(tanksAreFullFormat, negativeHighlight, 2);
            }

        }

        if (getCentrifugingCapacity(fleet) > 0 || getRefiningCapacity(fleet) > 0) {

            Color negativeHighlight = Misc.getNegativeHighlightColor();
            Color [] positiveHighlights = {textColor, highlightColor, highlightColor};
            Color [] negativeHighlights = {textColor, negativeHighlight, highlightColor};

            float maxMachineryNeededFirst = Math.max(getRefiningCapacity(fleet) * HEAVY_MACHINERY_REFINING_USAGE,
                    getCentrifugingCapacity(fleet) * HEAVY_MACHINERY_CENTRIFUGING_USAGE);
            // Expand later
            float maxMachineryNeededSecond = Math.max(getRefiningCapacity(fleet) * HEAVY_MACHINERY_REFINING_USAGE,
                    getCentrifugingCapacity(fleet) * HEAVY_MACHINERY_CENTRIFUGING_USAGE);
            int maxMachineryNeededFinal = (int) Math.max(maxMachineryNeededFirst, maxMachineryNeededSecond);
            int heavyMachineryAvailable = (int) fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY);

            String machineryNeeded = Misc.getWithDGS(maxMachineryNeededFinal);
            String machineryAvailable = Misc.getWithDGS(heavyMachineryAvailable);

            String format = "%sAvailable heavy machinery: %s/%s.";

            if (heavyMachineryAvailable >= maxMachineryNeededFinal ) {
                tooltip.addPara(format, 2, positiveHighlights, indent, machineryAvailable, machineryNeeded);
            }
            else {
                tooltip.addPara(format, 2, negativeHighlights, indent, machineryAvailable, machineryNeeded);
            }

        }

    }

    public static void addCorruptedNanoforgeNote(TooltipMakerAPI tooltip) {

        Color textColor = Misc.getTextColor();
        Color highlightColor = Misc.getHighlightColor();
        float pad = 5f;

        String qualityBonus = ((int)(100-((float) CORRUPTED_NANOFORGE_QUALITY_BONUS*100)) + "%");
        String nanoforge = "Corrupted Nanoforge";
        String formatCorrupted = "%s increases manufacturing/assembling output, and reduces " +
                "breakdowns and CR loss by %s.";

        tooltip.addPara(formatCorrupted, pad, textColor, highlightColor, nanoforge, qualityBonus);

    }

    public static void addPristineNanoforgeNote(TooltipMakerAPI tooltip) {

        Color textColor = Misc.getTextColor();
        Color highlightColor = Misc.getHighlightColor();
        float pad = 5f;

        String qualityBonus = ((int)(100-((float) PRISTINE_NANOFORGE_QUALITY_BONUS*100)) + "%");
        String nanoforge = "Pristine Nanoforge";
        String formatPristine = "%s increases manufacturing/assembling output, and reduces " +
                "breakdowns and CR loss by %s.";

        tooltip.addPara(formatPristine, pad, textColor, highlightColor, nanoforge, qualityBonus);

    }

    public static void addCatalyticCoreNote(TooltipMakerAPI tooltip) {

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        Color textColor = Misc.getTextColor();
        Color highlightColor = Misc.getHighlightColor();
        float pad = 5f;

        int fleetCapacity = (int) Math.ceil(getRefiningCapacity(fleet));
        String refiningBonus = Misc.getWithDGS((int) Math.ceil(CATALYTIC_CORE_REFINING_BONUS * fleetCapacity));
        String specialItem = "Catalytic Core";

        String formatNoCapacity = specialItem + " increases refining output.";
        String formatCapacity = specialItem + " increases refining output by " + refiningBonus + ".";

        if (fleetCapacity > 0 ) {
            tooltip.addPara(formatCapacity, pad, textColor, highlightColor, specialItem, refiningBonus);}
        else {
            tooltip.addPara(formatNoCapacity, pad, textColor, highlightColor, specialItem);}

    }

    public static void addSynchrotronCoreNote(TooltipMakerAPI tooltip) {

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        Color textColor = Misc.getTextColor();
        Color highlightColor = Misc.getHighlightColor();
        float pad = 5f;

        int fleetCapacity = (int) Math.ceil(getCentrifugingCapacity(fleet));
        String centrifugingBonus = Misc.getWithDGS((int) Math.ceil(CATALYTIC_CORE_REFINING_BONUS * fleetCapacity));
        String specialItem = "Synchrotron Core";

        String formatNoCapacity = specialItem + " increases centrifuging output.";
        String formatCapacity = specialItem + " increases centrifuging output by " + centrifugingBonus + ".";

        if (fleetCapacity > 0 ) {
            tooltip.addPara(formatCapacity, pad, textColor, highlightColor, specialItem, centrifugingBonus);}
        else {
            tooltip.addPara(formatNoCapacity, pad, textColor, highlightColor, specialItem);}

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
