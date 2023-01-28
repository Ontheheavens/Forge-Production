package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;

/**
 * @author Ontheheavens
 * @since 15.01.2023
 */

@SuppressWarnings("unused")
public class ForgprodRecoveryRewards extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;
        ForgprodRecoveryRewards.addItemRewards(dialog);
        return true;
    }

    private static void addItemRewards(InteractionDialogAPI dialog) {
        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
        Map<String, Pair<CargoItemType, Integer>> rewards = new HashMap<>();
        rewards.put(Commodities.HEAVY_MACHINERY, new Pair<>(CargoItemType.RESOURCES, 120));
        rewards.put(Commodities.METALS, new Pair<>(CargoItemType.RESOURCES, 260));
        rewards.put(Commodities.RARE_METALS, new Pair<>(CargoItemType.RESOURCES, 80));
        rewards.put(Items.CORRUPTED_NANOFORGE, new Pair<>(CargoItemType.SPECIAL, 1));
        for (String item : rewards.keySet()) {
            CargoItemType type = rewards.get(item).one;
            int quantity = rewards.get(item).two;
            if (type == CargoAPI.CargoItemType.RESOURCES) {
                cargo.addItems(type, item, quantity);
                AddRemoveCommodity.addCommodityGainText(item, quantity, dialog.getTextPanel());
            } else if (type == CargoAPI.CargoItemType.SPECIAL) {
                cargo.addItems(type, new SpecialItemData(item, null), quantity);
                ForgprodRecoveryRewards.addSpecialGainText(item, quantity, dialog.getTextPanel());
            }
        }
    }

    private static void addSpecialGainText(String itemId, int quantity, TextPanelAPI text) {
        SpecialItemSpecAPI spec = Global.getSettings().getSpecialItemSpec(itemId);
        text.setFontSmallInsignia();
        String name = spec.getName();
        text.addParagraph("Gained " + Misc.getWithDGS(quantity) + Strings.X + " " + name + "", Misc.getPositiveHighlightColor());
        text.highlightInLastPara(Misc.getHighlightColor(), Misc.getWithDGS(quantity) + Strings.X);
        text.setFontInsignia();
    }


}
