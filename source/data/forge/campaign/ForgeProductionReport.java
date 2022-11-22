package data.forge.campaign;

import java.awt.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;

import data.forge.abilities.conversion.support.ForgeConversionVariables;
import data.forge.plugins.ForgeSettings;

import static data.forge.plugins.ForgeSettings.NOTIFICATION_INTERVAL;

public class ForgeProductionReport extends BaseIntelPlugin {

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {

        Color titleColor = getTitleColor(mode);

        if (NOTIFICATION_INTERVAL > 1) {
            info.addPara("Forge Production - " + NOTIFICATION_INTERVAL + " Days Report", titleColor, 0f);
        } else if (NOTIFICATION_INTERVAL == 1) {
            info.addPara("Forge Production - Daily Report", titleColor, 0f);
        }
        else {
            info.addPara("Forge Production - Report", titleColor, 0f);
        }

        info.addSpacer(2.5f);

        addBulletPoints(info, mode);

        clearTemporaryVariables();

        Global.getSector().getIntelManager().removeIntel(this);

    }

    @Override public String getIcon() {

        return Global.getSettings().getSpriteName("intel", "forge_production_report");

    }

    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {

        Color highlightColor = Misc.getHighlightColor();
        Color negativeHighlight = Misc.getNegativeHighlightColor();

        float pad = 10f;
        int gridSize = 0;
        float gridWidth = 190f;

        String metalValue = "+" + Misc.getWithDGS((int) (ForgeConversionVariables.totalMetalProduction));
        String transplutonicsValue = "+" + Misc.getWithDGS((int) (ForgeConversionVariables.totalTransplutonicsProduction));
        String fuelValue = "+" + Misc.getWithDGS((int) (ForgeConversionVariables.totalFuelProduction));
        String suppliesValue = "+" + Misc.getWithDGS((int) (ForgeConversionVariables.totalSuppliesProduction));
        String heavyMachineryValue = "+" + Misc.getWithDGS((int) (ForgeConversionVariables.totalHeavyMachineryProduction));

        String machineryBreakdownsValue = "-" + Misc.getWithDGS((int) (ForgeConversionVariables.totalMachineryBreakage));

        String indent = "    - ";

        String metalProductionLine = indent + "Metal:";
        String transplutonicsProductionLine = indent + "Transplutonics:";
        String fuelProductionLine = indent + "Fuel:";
        String suppliesProductionLine = indent + "Supplies:";
        String heavyMachineryProductionLine = indent + "Machinery:";
        String machineryBreakdownsLine = indent + "Breakdowns:";

        if (ForgeSettings.INVERTED_GRID_NOTIFICATION) {
            gridWidth = 220f;
            metalProductionLine =  "Metal produced";
            transplutonicsProductionLine = "Transplutonics produced";
            fuelProductionLine = "Fuel produced";
            suppliesProductionLine = "Supplies produced";
            heavyMachineryProductionLine = "Machinery produced";
            machineryBreakdownsLine = "Machinery broken";
        }

        info.setGridRowHeight(12.5f);

        if (!ForgeSettings.INVERTED_GRID_NOTIFICATION) {
            info.beginGrid(gridWidth, 1);
        }

        if (ForgeSettings.INVERTED_GRID_NOTIFICATION) {
            info.beginGridFlipped(gridWidth, 1, 40f, pad);
        }

        if (ForgeConversionVariables.oreRefiningReport) {
            info.addToGrid(0, gridSize++, metalProductionLine, metalValue, highlightColor);
        }

        if (ForgeConversionVariables.transplutonicsRefiningReport) {
            info.addToGrid(0, gridSize++, transplutonicsProductionLine, transplutonicsValue, highlightColor);
        }

        if (ForgeConversionVariables.fuelCentrifugingReport) {
            info.addToGrid(0, gridSize++, fuelProductionLine, fuelValue, highlightColor);
        }

        if (ForgeConversionVariables.suppliesManufacturingReport) {
            info.addToGrid(0, gridSize++, suppliesProductionLine, suppliesValue, highlightColor);
        }

        if (ForgeConversionVariables.heavyMachineryAssemblingReport) {
            info.addToGrid(0, gridSize++, heavyMachineryProductionLine, heavyMachineryValue, highlightColor);
        }

        if (ForgeConversionVariables.breakdownReport) {
            info.addToGrid(0, gridSize++, machineryBreakdownsLine, machineryBreakdownsValue, negativeHighlight);
        }
        info.addGrid(2f);
        info.resetGridRowHeight();
    }

    public void clearTemporaryVariables() {

        ForgeConversionVariables.totalMetalProduction = 0f;
        ForgeConversionVariables.totalTransplutonicsProduction = 0f;
        ForgeConversionVariables.totalFuelProduction = 0f;
        ForgeConversionVariables.totalSuppliesProduction = 0f;
        ForgeConversionVariables.totalHeavyMachineryProduction = 0f;
        ForgeConversionVariables.totalMachineryBreakage = 0f;

        ForgeConversionVariables.oreRefiningReport = false;
        ForgeConversionVariables.transplutonicsRefiningReport = false;
        ForgeConversionVariables.fuelCentrifugingReport = false;
        ForgeConversionVariables.suppliesManufacturingReport = false;
        ForgeConversionVariables.heavyMachineryAssemblingReport = false;
        ForgeConversionVariables.breakdownReport = false;

    }

}
