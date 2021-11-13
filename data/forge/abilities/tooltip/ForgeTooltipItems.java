package data.forge.abilities.tooltip;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import java.awt.*;

import static data.forge.abilities.conversion.ForgeConversionGeneral.getForgingCapacity;
import static data.forge.abilities.conversion.ForgeTypes.*;
import static data.forge.abilities.conversion.ForgeTypes.CENTRIFUGING;
import static data.forge.plugins.ForgeSettings.*;
import static data.forge.plugins.ForgeSettings.CATALYTIC_CORE_REFINING_BONUS;

public class ForgeTooltipItems {

    public static void addCorruptedNanoforgeNote(TooltipMakerAPI tooltip) {

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        Color textColor = Misc.getTextColor();
        Color highlightColor = Misc.getHighlightColor();
        float pad = 5f;

        int manufacturingCapacity = (int) Math.ceil(getForgingCapacity(fleet, MANUFACTURING));
        int assemblingCapacity = (int) Math.ceil(getForgingCapacity(fleet, ASSEMBLING));
        String manufacturingBonus = Misc.getWithDGS((int) Math.ceil(CORRUPTED_NANOFORGE_MANUFACTURING_BONUS * manufacturingCapacity));
        String assemblingBonus = Misc.getWithDGS((int) Math.ceil(CORRUPTED_NANOFORGE_ASSEMBLING_BONUS * assemblingCapacity));
        String qualityBonus = ((int)(100-((float) CORRUPTED_NANOFORGE_QUALITY_BONUS*100)) + "%");
        String nanoforge = "Corrupted Nanoforge";
        String formatNoCapacity = "%s increases output of supplies, increases output of heavy machinery, and reduces " +
                "breakdowns and CR loss by %s.";
        String formatManufacturingCapacity = "%s increases output of supplies by %s, increases output of heavy machinery, and reduces " +
                "breakdowns and CR loss by %s.";
        String formatAssemblingCapacity = "%s increases output of supplies, increases output of heavy machinery by %s, and reduces " +
                "breakdowns and CR loss by %s.";
        String formatFullCapacity = "%s increases output of supplies by %s, increases output of heavy machinery by %s, and reduces " +
                "breakdowns and CR loss by %s.";

        if (manufacturingCapacity >= 1 && assemblingCapacity < 1) {
            tooltip.addPara(formatManufacturingCapacity, pad, textColor, highlightColor,
                    nanoforge, manufacturingBonus, qualityBonus);}
        if (assemblingCapacity >= 1 && manufacturingCapacity < 1) {
            tooltip.addPara(formatAssemblingCapacity, pad, textColor, highlightColor,
                    nanoforge, assemblingBonus, qualityBonus);}
        if (manufacturingCapacity >= 1 && assemblingCapacity >= 1) {
            tooltip.addPara(formatFullCapacity, pad, textColor, highlightColor,
                    nanoforge, manufacturingBonus, assemblingBonus, qualityBonus);}
        if (manufacturingCapacity < 1 && assemblingCapacity < 1) {
            tooltip.addPara(formatNoCapacity, pad, textColor, highlightColor,
                    nanoforge, qualityBonus);}

    }

    public static void addPristineNanoforgeNote(TooltipMakerAPI tooltip) {

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        Color textColor = Misc.getTextColor();
        Color highlightColor = Misc.getHighlightColor();
        float pad = 5f;

        int manufacturingCapacity = (int) Math.ceil(getForgingCapacity(fleet, MANUFACTURING));
        int assemblingCapacity = (int) Math.ceil(getForgingCapacity(fleet, ASSEMBLING));
        String manufacturingBonus = Misc.getWithDGS((int) Math.ceil(PRISTINE_NANOFORGE_MANUFACTURING_BONUS * manufacturingCapacity));
        String assemblingBonus = Misc.getWithDGS((int) Math.ceil(PRISTINE_NANOFORGE_ASSEMBLING_BONUS * assemblingCapacity));
        String qualityBonus = ((int)(100-((float) PRISTINE_NANOFORGE_QUALITY_BONUS*100)) + "%");
        String nanoforge = "Pristine Nanoforge";
        String formatNoCapacity = "%s increases output of supplies, increases output of heavy machinery, and reduces " +
                "breakdowns and CR loss by %s.";
        String formatManufacturingCapacity = "%s increases output of supplies by %s, increases output of heavy machinery, and reduces " +
                "breakdowns and CR loss by %s.";
        String formatAssemblingCapacity = "%s increases output of supplies, increases output of heavy machinery by %s, and reduces " +
                "breakdowns and CR loss by %s.";
        String formatFullCapacity = "%s increases output of supplies by %s, increases output of heavy machinery by %s, and reduces " +
                "breakdowns and CR loss by %s.";

        if (manufacturingCapacity >= 1 && assemblingCapacity < 1) {
            tooltip.addPara(formatManufacturingCapacity, pad, textColor, highlightColor,
                    nanoforge, manufacturingBonus, qualityBonus);}
        if (assemblingCapacity >= 1 && manufacturingCapacity < 1) {
            tooltip.addPara(formatAssemblingCapacity, pad, textColor, highlightColor,
                    nanoforge, assemblingBonus, qualityBonus);}
        if (manufacturingCapacity >= 1 && assemblingCapacity >= 1) {
            tooltip.addPara(formatFullCapacity, pad, textColor, highlightColor,
                    nanoforge, manufacturingBonus, assemblingBonus, qualityBonus);}
        if (manufacturingCapacity < 1 && assemblingCapacity < 1) {
            tooltip.addPara(formatNoCapacity, pad, textColor, highlightColor,
                    nanoforge, qualityBonus);}

    }

    public static void addCatalyticCoreNote(TooltipMakerAPI tooltip) {

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        Color textColor = Misc.getTextColor();
        Color highlightColor = Misc.getHighlightColor();
        float pad = 5f;

        int fleetCapacity = (int) Math.ceil(getForgingCapacity(fleet, REFINING));
        String refiningBonus = Misc.getWithDGS((int) Math.ceil(CATALYTIC_CORE_REFINING_BONUS * fleetCapacity));
        String specialItem = "Catalytic Core";

        String formatNoCapacity = specialItem + " increases output of metals and transplutonics.";
        String formatCapacity = specialItem + " increases output of metals and transplutonics by " + refiningBonus + ".";

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

        int fleetCapacity = (int) Math.ceil(getForgingCapacity(fleet, CENTRIFUGING));
        String centrifugingBonus = Misc.getWithDGS((int) Math.ceil(CATALYTIC_CORE_REFINING_BONUS * fleetCapacity));
        String specialItem = "Synchrotron Core";

        String formatNoCapacity = specialItem + " increases output of fuel.";
        String formatCapacity = specialItem + " increases output of fuel by " + centrifugingBonus + ".";

        if (fleetCapacity > 0 ) {
            tooltip.addPara(formatCapacity, pad, textColor, highlightColor, specialItem, centrifugingBonus);}
        else {
            tooltip.addPara(formatNoCapacity, pad, textColor, highlightColor, specialItem);}

    }

}
