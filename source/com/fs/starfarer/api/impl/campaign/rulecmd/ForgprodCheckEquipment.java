package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;
import com.fs.starfarer.api.util.Misc;

/**
 * @author Ontheheavens
 * @since 14.01.2023
 */

@SuppressWarnings("unused")
public class ForgprodCheckEquipment extends BaseCommandPlugin {

    private static SectorEntityToken mothership;

    @Override
    public boolean execute(final String ruleId, final InteractionDialogAPI dialog,
                           final List<Misc.Token> params, final Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }
        mothership = dialog.getInteractionTarget();
        final String command = params.get(0).getString(memoryMap);
        if (command == null) {
            return false;
        }
        if ("playerHasEquipment".equals(command)) {
            return playerHasEquipment();
        }
        return false;
    }

    protected boolean playerHasEquipment() {
        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
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
        boolean hasCrew = hasCommodity(requiredRes, cargo, Commodities.CREW);
        boolean hasMachinery = hasCommodity(requiredRes, cargo, Commodities.HEAVY_MACHINERY);
        boolean hasCores = hasCommodity(requiredRes, cargo, Commodities.GAMMA_CORE);
        return hasCrew && hasMachinery && hasCores;
    }

    private static boolean hasCommodity(Map<String, Integer> requiredRes, CargoAPI cargo, String commodity) {
        return cargo.getCommodityQuantity(commodity) >= requiredRes.get(commodity);
    }

}
