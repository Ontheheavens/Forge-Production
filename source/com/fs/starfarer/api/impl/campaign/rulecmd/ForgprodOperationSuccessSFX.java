package com.fs.starfarer.api.impl.campaign.rulecmd;

import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;

/**
 * @author Ontheheavens
 * @since 15.01.2023
 */

@SuppressWarnings("unused")
public class ForgprodOperationSuccessSFX extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) return false;
        Global.getSoundPlayer().playUISound("ui_scavenge_on", 1f, 1f);
        return true;
    }

}
