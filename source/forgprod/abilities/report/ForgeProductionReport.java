package forgprod.abilities.report;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.util.Misc;

import forgprod.abilities.conversion.ProductionEventManager;
import forgprod.abilities.conversion.support.ConversionVariables;
import forgprod.abilities.modules.FleetwideModuleManager;

public class ForgeProductionReport extends BaseIntelPlugin {

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color titleColor = getTitleColor(mode);
        int counter = ProductionEventManager.getInstance().getNotificationCounter();
        if (counter > 1) {
            info.addPara("Forge Production - " + counter + " Days Report", titleColor, 0f);
        } else if (counter == 1) {
            info.addPara("Forge Production - Daily Report", titleColor, 0f);
        } else {
            info.addPara("Forge Production - Report", titleColor, 0f);
        }
        info.addSpacer(2.5f);
        addBulletPoints(info, mode);
        ConversionVariables.clearReportVariables();
        Global.getSector().getIntelManager().removeIntel(this);
    }

    @Override public String getIcon() {
        return Global.getSettings().getSpriteName("forgprod_intel", "forge_production_report");
    }

    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
        Color highlightColor = Misc.getHighlightColor();
        Color negativeHighlight = Misc.getNegativeHighlightColor();
        List<String> values = new ArrayList<>();
        String metalsValue = getValueWithFormat(ConversionVariables.totalMetalsProduced);
        String transplutonicsValue = getValueWithFormat(ConversionVariables.totalTransplutonicsProduced);
        String fuelValue = getValueWithFormat(ConversionVariables.totalFuelProduced);
        String suppliesValue = getValueWithFormat(ConversionVariables.totalSuppliesProduced);
        String machineryValue = getValueWithFormat(ConversionVariables.totalMachineryProduced);
        String hullsValue = getValueWithFormat(ConversionVariables.totalHullsProduced);
        String machineryBreakdownsValue = getValueWithFormat(ConversionVariables.totalMachineryBroke);
        String casualtiesValue = getValueWithFormat(ConversionVariables.totalCrewmenLost);
        values.add(metalsValue);
        values.add(transplutonicsValue);
        values.add(fuelValue);
        values.add(suppliesValue);
        values.add(machineryValue);
        values.add(hullsValue);
        values.add(machineryBreakdownsValue);
        values.add(casualtiesValue);
        float longestValueWidth = 0f;
        for (String value : values) {
            float valueWidth = info.computeStringWidth(value);
            if (valueWidth > longestValueWidth) {
                longestValueWidth = valueWidth;
            }
        }
        String metalsProductionLine = "Metals";
        String transplutonicsProductionLine = "Transplutonics";
        String fuelProductionLine = "Fuel";
        String suppliesProductionLine = "Supplies";
        String machineryProductionLine = "Machinery";
        ShipHullSpecAPI producedSpec = FleetwideModuleManager.getInstance().getDesignatedVariant().getHullSpec();
        String producedHullName = producedSpec.getHullNameWithDashClass();
        String hullsProductionLine;
        if (ConversionVariables.totalHullsProduced > 1f) {
            hullsProductionLine = producedHullName + " Hulls";
        } else {
            hullsProductionLine = producedHullName + " Hull";
        }
        String machineryBreakdownsLine = "Breakdowns";
        String casualtiesLine = "Casualties";
        String iconName;
        if (ConversionVariables.totalMetalsProduced >= 1) {
            iconName = Global.getSettings().getCommoditySpec(Commodities.METALS).getIconName();
            ForgeProductionReport.addReportLine(info, iconName, metalsValue, highlightColor,
                    metalsProductionLine, longestValueWidth);
        }
        if (ConversionVariables.totalTransplutonicsProduced >= 1) {
            iconName = Global.getSettings().getCommoditySpec(Commodities.RARE_METALS).getIconName();
            ForgeProductionReport.addReportLine(info, iconName, transplutonicsValue, highlightColor,
                    transplutonicsProductionLine, longestValueWidth);
        }
        if (ConversionVariables.totalFuelProduced >= 1) {
            iconName = Global.getSettings().getCommoditySpec(Commodities.FUEL).getIconName();
            ForgeProductionReport.addReportLine(info, iconName, fuelValue, highlightColor,
                    fuelProductionLine, longestValueWidth);
        }
        if (ConversionVariables.totalSuppliesProduced >= 1) {
            iconName = Global.getSettings().getCommoditySpec(Commodities.SUPPLIES).getIconName();
            ForgeProductionReport.addReportLine(info, iconName, suppliesValue, highlightColor,
                    suppliesProductionLine, longestValueWidth);
        }
        if (ConversionVariables.totalMachineryProduced >= 1) {
            iconName = Global.getSettings().getCommoditySpec(Commodities.HEAVY_MACHINERY).getIconName();
            ForgeProductionReport.addReportLine(info, iconName, machineryValue, highlightColor,
                    machineryProductionLine, longestValueWidth);
        }
        if (ConversionVariables.totalHullsProduced >= 1) {
            iconName = Global.getSettings().getSpriteName("forgprod_ui", "hull_parts_symbolic");
            ForgeProductionReport.addReportLine(info, iconName, hullsValue, highlightColor,
                    hullsProductionLine, longestValueWidth);
        }
        if (ConversionVariables.totalMachineryBroke >= 1) {
            iconName = Global.getSettings().getSpriteName("forgprod_ui", "production_breakdowns");
            ForgeProductionReport.addReportLine(info, iconName, machineryBreakdownsValue, negativeHighlight,
                    machineryBreakdownsLine, longestValueWidth);
        }
        if (ConversionVariables.totalCrewmenLost >= 1) {
            iconName = Global.getSettings().getSpriteName("forgprod_ui", "production_casualties");
            ForgeProductionReport.addReportLine(info, iconName, casualtiesValue, negativeHighlight,
                    casualtiesLine, longestValueWidth);
        }
    }

    private static String getValueWithFormat(float value) {
        if (value > 99f) {
            return Misc.getWithDGS((int) (value));
        }
        return Misc.getRoundedValueMaxOneAfterDecimal((value));
    }

    private static void addReportLine(TooltipMakerAPI info, String iconSprite, String value,
                                      Color valueColor, String description, float offsetWidth) {
        TooltipMakerAPI reportLine = info.beginImageWithText(null, 2f);
        reportLine.setTextWidthOverride(250f);
        reportLine.addPara("-", 0f);
        UIComponentAPI bulletAnchor = reportLine.getPrev();
        bulletAnchor.getPosition().setXAlignOffset(-374f);
        reportLine.addImage(iconSprite, 18f, 18f, 0f);
        UIComponentAPI icon = reportLine.getPrev();
        icon.getPosition().rightOfMid(bulletAnchor, 2f);
        icon.getPosition().setXAlignOffset(-244f);
        icon.getPosition().setYAlignOffset(0f);
        LabelAPI valueLabel = reportLine.addPara(value + "×", valueColor, 0f);
        valueLabel.getPosition().rightOfMid(bulletAnchor, -460f + offsetWidth);
        valueLabel.getPosition().setYAlignOffset(2f);
        valueLabel.setAlignment(Alignment.RMID);
        LabelAPI descriptionLabel = reportLine.addPara(description, 0f);
        descriptionLabel.getPosition().rightOfMid(bulletAnchor, -(207f - offsetWidth));
        descriptionLabel.getPosition().setYAlignOffset(1f);
        reportLine.addSpacer(-50f);
        info.addImageWithText(2f);
    }

}
