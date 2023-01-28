package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.awt.*;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.util.Misc;

@SuppressWarnings("unused")
public class ForgprodShowRetrievalCost extends BaseCommandPlugin {

    @Override
    public boolean execute(final String ruleId, final InteractionDialogAPI dialog,
                           final List<Misc.Token> params, final Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }
        TextPanelAPI textPanel = dialog.getTextPanel();
        SectorEntityToken mothership = dialog.getInteractionTarget();
        this.showCost(textPanel, mothership);
        return true;
    }

    public void showCost(TextPanelAPI textPanel, SectorEntityToken mothership) {
        FactionAPI playerFaction = Global.getSector().getPlayerFaction();
        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();

        Color color = playerFaction.getColor();
        Color bad = Misc.getNegativeHighlightColor();
        Map<String, Integer> requiredRes = SalvageEntity.computeRequiredToSalvage(mothership);
        for (String commodityId : requiredRes.keySet()) {
            int cost = requiredRes.get(commodityId);
            if (commodityId.equals(Commodities.CREW)) {
                cost = Math.round(cost * 1.4f);
            } else {
                cost = Math.round(cost * 4.8f);
            }
            requiredRes.put(commodityId, cost);
        }
        requiredRes.put(Commodities.GAMMA_CORE, 3);
        String format = "Reporting officer notes that that engineering operation of this kind" +
                " will likely demand considerable %s in order to plan smooth uninstall of autoforging complexes " +
                "without causing irreparable damage to them in the process. The operation will also " +
                "require significant amounts of specialized %s.";
        textPanel.addPara(format, Misc.getTextColor(), Misc.getHighlightColor(),
                "processing power", "industrial equipment");
        ResourceCostPanelAPI cost = textPanel.addCostPanel("Crew, machinery & AI cores: required (available)",
                67f, color, playerFaction.getDarkUIColor());
        cost.setNumberOnlyMode(true);
        cost.setWithBorder(false);
        cost.setAlignment(Alignment.LMID);
        for (String commodityId : requiredRes.keySet()) {
            int required = requiredRes.get(commodityId);
            int available = (int) cargo.getCommodityQuantity(commodityId);
            Color curr = color;
            if (required > cargo.getQuantity(CargoAPI.CargoItemType.RESOURCES, commodityId)) {
                curr = bad;
            }
            cost.addCost(commodityId, "" + required + " (" + available + ")", curr);
        }
        cost.update();
    }
    
}