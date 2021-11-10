package data.scripts.campaign;

import java.awt.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;

import data.scripts.abilities.conversion.ForgeConversionVariables;
import static data.scripts.plugins.ForgeSettings.NOTIFICATION_INTERVAL;

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

        String oreValue = Misc.getWithDGS((int) (ForgeConversionVariables.totalOreExpenditure));
        String metalValue = Misc.getWithDGS((int) (ForgeConversionVariables.totalMetalProduction));

        String transplutonicOreValue = Misc.getWithDGS((int) (ForgeConversionVariables.totalTransplutonicOreExpenditure));
        String transplutonicsValue = Misc.getWithDGS((int) (ForgeConversionVariables.totalTransplutonicsProduction));
        String volatilesValue = Misc.getWithDGS((int) (ForgeConversionVariables.totalVolatilesExpenditure));
        String fuelValue = Misc.getWithDGS((int) (ForgeConversionVariables.totalFuelProduction));

        String machineryBreakdownsValue = Misc.getWithDGS((int) (ForgeConversionVariables.totalMachineryBreakage));

        String metalProductionLine = oreValue + " ore refined into " + metalValue + " metal";
        String transplutonicsProductionLine = transplutonicOreValue + " transplutonic ore refined into " + transplutonicsValue + " transplutonics";
        String fuelProductionLine = volatilesValue + " volatiles centrifuged into " + fuelValue + " fuel";
        String machineryBreakdownsLine = machineryBreakdownsValue + " heavy machinery broke down";

        if (ForgeConversionVariables.oreRefiningReport) {
            info.addPara(metalProductionLine, pad, textColor, Misc.getHighlightColor(),
                    oreValue, metalValue);
        }

        if (ForgeConversionVariables.transplutonicsRefiningReport) {
            info.addPara(transplutonicsProductionLine, pad, textColor, Misc.getHighlightColor(),
                    transplutonicOreValue, transplutonicsValue);
        }

        if (ForgeConversionVariables.fuelCentrifugingReport) {
            info.addPara(fuelProductionLine, pad, textColor, Misc.getHighlightColor(),
                    volatilesValue, fuelValue);
        }

        if (ForgeConversionVariables.breakdownReport) {
            info.addPara(machineryBreakdownsLine, pad, textColor, Misc.getHighlightColor(),
                    machineryBreakdownsValue);
        }

        unindent(info);
    }

    //Here: Custom methods

    public void clearTemporaryVariables() {

        ForgeConversionVariables.totalOreExpenditure = 0f;
        ForgeConversionVariables.totalMetalProduction = 0f;

        ForgeConversionVariables.totalTransplutonicOreExpenditure = 0f;
        ForgeConversionVariables.totalTransplutonicsProduction = 0f;

        ForgeConversionVariables.totalVolatilesExpenditure = 0f;
        ForgeConversionVariables.totalFuelProduction = 0f;

        ForgeConversionVariables.totalMachineryBreakage = 0f;

        ForgeConversionVariables.oreRefiningReport = false;
        ForgeConversionVariables.transplutonicsRefiningReport = false;
        ForgeConversionVariables.fuelCentrifugingReport = false;
        ForgeConversionVariables.breakdownReport = false;

    }

}
