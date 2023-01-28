package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PersistentUIDataAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.loading.AbilitySpecAPI;
import com.fs.starfarer.api.util.Misc;

import forgprod.abilities.conversion.support.ProductionConstants;

/**
 * @author Ontheheavens
 * @since 15.01.2023
 */

@SuppressWarnings("unused")
public class ForgprodAbilityUnlock extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;
        TextPanelAPI text = dialog.getTextPanel();
        ForgprodAbilityUnlock.unlockAbility(text);
        ForgprodAbilityUnlock.unlockHullmods(text);
        return true;
    }

    private static void unlockAbility(TextPanelAPI text) {
        String abilityId = "forge_production";
        AbilitySpecAPI spec = Global.getSettings().getAbilitySpec(abilityId);
        boolean hadAbilityAlready = Global.getSector().getPlayerFleet().hasAbility(abilityId);
        if (hadAbilityAlready) {
            return;
        }
        Global.getSector().getCharacterData().addAbility(abilityId);
        PersistentUIDataAPI.AbilitySlotsAPI slots = Global.getSector().getUIData().getAbilitySlotsAPI();
        int currBarIndex = slots.getCurrBarIndex();
        OUTER:
        for (int i = 0; i < 5; i++) {
            slots.setCurrBarIndex(i);
            for (int j = 0; j < 10; j++) {
                PersistentUIDataAPI.AbilitySlotAPI slot = slots.getCurrSlotsCopy().get(j);
                if (slot.getAbilityId() == null) {
                    slot.setAbilityId(abilityId);
                    break OUTER;
                }
            }
        }
        slots.setCurrBarIndex(currBarIndex);
        text.addPara("You now possess %s ability - technical know-how needed to operate " +
                        "Pre-Collapse space manufacturing tech.",
                Misc.getHighlightColor(),
                "\"" + spec.getName() + "\"");
        text.addPara("These know-how, however, do not go as far as " +
                        "producing brand-new autoforge units yourself - the only practical option right now is to " +
                        "reassemble makeshift production modules using stock of crucial Domain-made components " +
                        "salvaged from derelict mothership.");
        AddRemoveCommodity.addAbilityGainText(abilityId, text);
    }

    private static void unlockHullmods(TextPanelAPI text) {
        Global.getSector().getCharacterData().getHullMods().add(ProductionConstants.REFINING_MODULE);
        Global.getSector().getCharacterData().getHullMods().add(ProductionConstants.FUEL_PRODUCTION_MODULE);
        Global.getSector().getCharacterData().getHullMods().add(ProductionConstants.HEAVY_INDUSTRY_MODULE);
        String name = Global.getSettings().getHullModSpec(ProductionConstants.HEAVY_INDUSTRY_MODULE).getDisplayName();
        text.setFontSmallInsignia();
        text.addParagraph("Gained modspec: " + name + "", Misc.getPositiveHighlightColor());
        text.highlightInLastPara(Misc.getHighlightColor(), name);
        text.setFontInsignia();
        Global.getSoundPlayer().playUISound("ui_acquired_hullmod", 1f, 1f);
    }

}
