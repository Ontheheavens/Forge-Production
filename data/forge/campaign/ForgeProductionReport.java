package data.forge.campaign;

import java.awt.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;

import data.forge.abilities.conversion.ForgeConversionVariables;
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

        addBulletPoints(info, mode);

        clearTemporaryVariables();

        Global.getSector().getIntelManager().removeIntel(this);

    }

    @Override public String getIcon() {

        return Global.getSettings().getSpriteName("intel", "forge_production_report");

    }

    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {

        float pad = 3f;

        Color textColor = Misc.getTextColor();

        bullet(info);

        String metalValue = Misc.getWithDGS((int) (ForgeConversionVariables.totalMetalProduction));
        String transplutonicsValue = Misc.getWithDGS((int) (ForgeConversionVariables.totalTransplutonicsProduction));
        String fuelValue = Misc.getWithDGS((int) (ForgeConversionVariables.totalFuelProduction));
        String suppliesValue = Misc.getWithDGS((int) (ForgeConversionVariables.totalSuppliesProduction));
        String heavyMachineryValue = Misc.getWithDGS((int) (ForgeConversionVariables.totalHeavyMachineryProduction));

        String machineryBreakdownsValue = Misc.getWithDGS((int) (ForgeConversionVariables.totalMachineryBreakage));

        String metalProductionLine =  metalValue + " metal produced";
        String transplutonicsProductionLine = transplutonicsValue + " transplutonics produced";
        String fuelProductionLine = fuelValue + " fuel produced";
        String suppliesProductionLine = suppliesValue + " supplies produced";
        String heavyMachineryProductionLine = heavyMachineryValue + " heavy machinery produced";
        String machineryBreakdownsLine = machineryBreakdownsValue + " heavy machinery broke down";

        if (ForgeConversionVariables.oreRefiningReport) {
            info.addPara(metalProductionLine, pad, textColor, Misc.getHighlightColor(),
                    metalValue);
        }

        if (ForgeConversionVariables.transplutonicsRefiningReport) {
            info.addPara(transplutonicsProductionLine, pad, textColor, Misc.getHighlightColor(),
                    transplutonicsValue);
        }

        if (ForgeConversionVariables.fuelCentrifugingReport) {
            info.addPara(fuelProductionLine, pad, textColor, Misc.getHighlightColor(),
                    fuelValue);
        }

        if (ForgeConversionVariables.suppliesManufacturingReport) {
            info.addPara(suppliesProductionLine, pad, textColor, Misc.getHighlightColor(),
                    suppliesValue);
        }

        if (ForgeConversionVariables.heavyMachineryAssemblingReport) {
            info.addPara(heavyMachineryProductionLine, pad, textColor, Misc.getHighlightColor(),
                    heavyMachineryValue);
        }

        if (ForgeConversionVariables.breakdownReport) {
            info.addPara(machineryBreakdownsLine, pad, textColor, Misc.getHighlightColor(),
                    machineryBreakdownsValue);
        }

        unindent(info);
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
