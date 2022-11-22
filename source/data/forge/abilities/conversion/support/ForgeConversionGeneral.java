package data.forge.abilities.conversion.support;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import data.forge.campaign.ForgeConditionChecker;
import data.forge.plugins.ForgeSettings;

import static data.forge.hullmods.support.ForgeHullmodsGeneral.shipSizeEffect;
import static data.forge.plugins.ForgeSettings.CR_PRODUCTION_DECAY;

public class ForgeConversionGeneral {

    public static int getForgingCapacityWithCR(CampaignFleetAPI fleet, String forgeType) {
        if (Global.getSector().getPlayerFleet() == null) { return 0; }
        int totalForgingCapacity = 0;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (!ForgeConditionChecker.isOperational(member))
                continue;
            int shipBaseCapacity = shipSizeEffect.get(member.getVariant().getHullSize());
            if (member.getVariant().hasHullMod(forgeType))
                totalForgingCapacity += (int) Math.floor(shipBaseCapacity *
                        (member.getRepairTracker().getCR() / 0.7f));

        }
        return totalForgingCapacity;
    }

    public static void applyForgingCRMalus(CampaignFleetAPI fleet, String forgeType) {
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if ((!ForgeConditionChecker.isOperational(member)))
                continue;
            if (member.getVariant().hasHullMod(forgeType))
                member.getRepairTracker().applyCREvent(-((CR_PRODUCTION_DECAY * ForgeConditionChecker.getForgingQuality()) *
                        (member.getRepairTracker().getCR() / 0.7f)), "Forged goods");
        }
    }

    public static float getTotalFleetMachineryRequirement(CampaignFleetAPI fleet) {

        float requirementRefining = (ForgeSettings.HEAVY_MACHINERY_REFINING_USAGE * getForgingCapacityWithCR(fleet, ForgeTypes.REFINERY));
        float requirementCentrifuging = (ForgeSettings.HEAVY_MACHINERY_CENTRIFUGING_USAGE * getForgingCapacityWithCR(fleet, ForgeTypes.CENTRIFUGE));
        float requirementManufacturing = (ForgeSettings.HEAVY_MACHINERY_MANUFACTURING_USAGE * getForgingCapacityWithCR(fleet, ForgeTypes.MANUFACTURE));
        float requirementAssembling = (ForgeSettings.HEAVY_MACHINERY_ASSEMBLING_USAGE * getForgingCapacityWithCR(fleet, ForgeTypes.ASSEMBLY));

        return (requirementRefining + requirementCentrifuging + requirementManufacturing + requirementAssembling);
    }

    public static float getMachineryAvailability() {

        if (Global.getSector().getPlayerFleet() == null) {
            return 0f;
        }
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        int machineryInCargo = (int) Math.floor(ForgeConditionChecker.getPlayerCargo(Commodities.HEAVY_MACHINERY));
        float machineryRequiredTotal = getTotalFleetMachineryRequirement(fleet);

        float machineryAvailability = machineryInCargo / machineryRequiredTotal;

        if (machineryAvailability > 1f) {
            machineryAvailability = 1f;
        }

        return machineryAvailability;
    }

    public static float getConversionMachineryUsageTotal(CampaignFleetAPI fleet, ForgeTypes.Conversion type, String forgeType) {

        float requirementForConversion = (ForgeTypes.MACHINERY_USAGE.get(type) * getForgingCapacityWithCR(fleet, forgeType));
        float machineryAvailableTotal = getTotalFleetMachineryRequirement(fleet) * getMachineryAvailability();
        float machineryAvailableForOtherConversions = machineryAvailableTotal - (requirementForConversion * getMachineryAvailability());

        return machineryAvailableTotal - machineryAvailableForOtherConversions;
    }

    public static int getMachineryUsageCycles(ForgeTypes.Conversion type, String forgeType) {

        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();

        float machineryAvailable = ForgeConversionGeneral.getConversionMachineryUsageTotal(fleet, type, forgeType);

        return (int) Math.floor(machineryAvailable / ForgeTypes.MACHINERY_USAGE.get(type));
    }

}
