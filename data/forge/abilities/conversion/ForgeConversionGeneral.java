package data.forge.abilities.conversion;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import data.forge.campaign.ForgeConditionChecker;

import static data.forge.hullmods.ForgeHullmodsGeneral.shipSizeEffect;
import static data.forge.plugins.ForgeSettings.CR_PRODUCTION_DECAY;

public class ForgeConversionGeneral {

    public static int getForgingCapacity(CampaignFleetAPI fleet, String forgeType) {
        int totalForgingCapacity = 0;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if ((!ForgeConditionChecker.isOperational(member)))
                continue;
            int shipBaseCapacity = shipSizeEffect.get(member.getVariant().getHullSize());
            if (member.getVariant().hasHullMod(forgeType))
                totalForgingCapacity += (int) Math.ceil(shipBaseCapacity *
                        (member.getRepairTracker().getCR() / 0.7f));

        }
        return totalForgingCapacity;
    }

    public static void applyForgingCRMalus(CampaignFleetAPI fleet, String forgeType) {
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if ((!ForgeConditionChecker.isOperational(member)))
                continue;
            if (member.getVariant().hasHullMod(forgeType))
                member.getRepairTracker().applyCREvent(-(((float) CR_PRODUCTION_DECAY * ForgeConditionChecker.getForgingQuality()) *
                        (member.getRepairTracker().getCR() / 0.7f)), "Forged goods");
        }
    }

}
