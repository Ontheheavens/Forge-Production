package data.scripts.abilities.conversion;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

import data.scripts.campaign.ForgeConditionChecker;

import static data.scripts.plugins.ForgeSettings.*;
import static data.scripts.abilities.conversion.ForgeConversionVariables.*;
import static data.scripts.hullmods.ForgeHullmodsGeneral.shipSizeEffect;

public class ForgeRefiningLogic {

    // Here: Custom methods

    public static int getRefiningCapacity(CampaignFleetAPI fleet) {
        int totalRefiningCapacity = 0;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if ((!ForgeConditionChecker.isOperational(member)))
                continue;
            if (member.getVariant().hasHullMod("forge_refinery_module"))
                totalRefiningCapacity += (int) Math.ceil(shipSizeEffect.get(member.getVariant().getHullSize()) *
                        (member.getRepairTracker().getCR() / 0.7f));
        }
        return totalRefiningCapacity;
    }

    public static void applyRefiningCRMalus(CampaignFleetAPI fleet) {
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if ((!ForgeConditionChecker.isOperational(member)))
                continue;
            if (member.getVariant().hasHullMod("forge_refinery_module"))
                member.getRepairTracker().applyCREvent(-(((float) CR_PRODUCTION_DECAY * ForgeConditionChecker.getForgingQuality()) *
                        (member.getRepairTracker().getCR() / 0.7f)), "Refined metal");
        }
    }

    // Here: Conversion methods

    public static void refineOre(CampaignFleetAPI fleet) {

        float heavyMachineryAvailable = fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY);
        if (heavyMachineryAvailable <= HEAVY_MACHINERY_REFINING_USAGE) return;

        float heavyMachineryInRefining = heavyMachineryAvailable / HEAVY_MACHINERY_REFINING_USAGE;
        float oreAvailable = fleet.getCargo().getCommodityQuantity(Commodities.ORE);

        float RefiningCapacity = getRefiningCapacity(fleet);

        if (oreAvailable > ORE_TO_REFINE) {

            float oreInRefining = oreAvailable/ ORE_TO_REFINE;

            int refiningCycles = (int) (Math.min(RefiningCapacity, Math.min(heavyMachineryInRefining,oreInRefining)));

            boolean willHeavyMachineryBreakdown = (Math.random()<((float) BASE_BREAKDOWN_CHANCE * ForgeConditionChecker.getForgingQuality()));
            int MachineryInRefining = (int) Math.ceil((refiningCycles * HEAVY_MACHINERY_REFINING_USAGE));
            int machineryBroken = 0;

            for (int machineryInCheck = 0; machineryInCheck < MachineryInRefining; machineryInCheck++) {
                if (Math.random()<((float) BREAKDOWN_SEVERITY * ForgeConditionChecker.getForgingQuality())) machineryBroken++;
            }

            float dailyOreSpent = ORE_TO_REFINE * refiningCycles;
            float dailyMetalProduced = (METAL_PRODUCED + ForgeConditionChecker.getCatalyticCoreBonus()) * refiningCycles;
            float dailyHeavyMachineryBroken = machineryBroken;

            fleet.getCargo().removeCommodity(Commodities.ORE, dailyOreSpent);
            fleet.getCargo().addCommodity(Commodities.METALS, dailyMetalProduced);
            if(willHeavyMachineryBreakdown) {
                fleet.getCargo().removeCommodity(Commodities.HEAVY_MACHINERY, dailyHeavyMachineryBroken);
            }

            totalOreExpenditure +=  dailyOreSpent;
            totalMetalProduction +=  dailyMetalProduced;
            if(willHeavyMachineryBreakdown) {
                totalMachineryBreakage +=  dailyHeavyMachineryBroken;
            }

            if (willHeavyMachineryBreakdown && dailyHeavyMachineryBroken > 0) {
                breakdownReport = true;
            }

            if (dailyMetalProduced > 0) {
                oreWasRefined = true;
                oreRefiningReport = true;
            }

        }

    }

    public static void refineTransplutonicOre(CampaignFleetAPI fleet) {

        float heavyMachineryAvailable = fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY);
        if (heavyMachineryAvailable <= HEAVY_MACHINERY_REFINING_USAGE) return;

        float heavyMachineryInRefining = heavyMachineryAvailable / HEAVY_MACHINERY_REFINING_USAGE;
        float transplutonicOreAvailable = fleet.getCargo().getCommodityQuantity(Commodities.RARE_ORE);

        float refiningCapacity = getRefiningCapacity(fleet);

        if (transplutonicOreAvailable > TRANSPLUTONIC_ORE_TO_REFINE) {

            float transplutonicOreInRefining = transplutonicOreAvailable / TRANSPLUTONIC_ORE_TO_REFINE;

            int refiningCycles = (int) (Math.min(refiningCapacity, Math.min(heavyMachineryInRefining, transplutonicOreInRefining)));

            boolean willHeavyMachineryBreakdown = (Math.random()<((float) BASE_BREAKDOWN_CHANCE * ForgeConditionChecker.getForgingQuality()));
            int machineryInRefining = (int) Math.ceil((refiningCycles * HEAVY_MACHINERY_REFINING_USAGE));
            int machineryBroken = 0;

            for (int machineryInCheck = 0; machineryInCheck < machineryInRefining; machineryInCheck++) {
                if (Math.random()<((float) BREAKDOWN_SEVERITY * ForgeConditionChecker.getForgingQuality())) machineryBroken++;
            }

            float dailyTransplutonicOreSpent = TRANSPLUTONIC_ORE_TO_REFINE * refiningCycles;
            float dailyTransplutonicsProduced = (TRANSPLUTONICS_PRODUCED + ForgeConditionChecker.getCatalyticCoreBonus()) * refiningCycles;
            float dailyHeavyMachineryBroken = machineryBroken;

            fleet.getCargo().removeCommodity(Commodities.RARE_ORE, dailyTransplutonicOreSpent);
            fleet.getCargo().addCommodity(Commodities.RARE_METALS, dailyTransplutonicsProduced);
            if(willHeavyMachineryBreakdown) {
                fleet.getCargo().removeCommodity(Commodities.HEAVY_MACHINERY, dailyHeavyMachineryBroken);
            }

            totalTransplutonicOreExpenditure += dailyTransplutonicOreSpent;
            totalTransplutonicsProduction +=  dailyTransplutonicsProduced;
            if(willHeavyMachineryBreakdown) {
                totalMachineryBreakage +=  dailyHeavyMachineryBroken;
            }

            if (willHeavyMachineryBreakdown && dailyHeavyMachineryBroken > 0) {
                breakdownReport = true;
            }

            if (dailyTransplutonicsProduced > 0) {
                transplutonicsWereRefined = true;
                transplutonicsRefiningReport = true;
            }

        }
    }

}
