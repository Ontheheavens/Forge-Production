package data.scripts.abilities;

import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility;

public class ForgeProduction extends BaseToggleAbility {

    protected void activateImpl() { }

    protected void applyEffect(float amount, float level) { }

    protected void deactivateImpl() { cleanupImpl(); }

    protected void cleanupImpl() { }

}