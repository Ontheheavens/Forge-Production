package data.forge.abilities.conversion;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

import data.forge.campaign.ForgeConditionChecker;
import static data.forge.plugins.ForgeSettings.*;
import static data.forge.abilities.conversion.ForgeConversionVariables.*;

public class ForgeRefiningLogic {

    public static void startRefining(CampaignFleetAPI fleet) {

        if (ForgeConversionGeneral.getForgingCapacity(fleet, ForgeTypes.REFINING) <= 1) return;
        if (ForgeConditionChecker.getPlayerCargo(Commodities.HEAVY_MACHINERY) <= HEAVY_MACHINERY_REFINING_USAGE) return;

        if (ForgeConditionChecker.getPlayerCargo(Commodities.ORE) > ORE_TO_REFINE) {
            refineOre(fleet);
        }

        if (ForgeConditionChecker.getPlayerCargo(Commodities.RARE_ORE) > TRANSPLUTONIC_ORE_TO_REFINE) {
            refineTransplutonicOre(fleet);
        }

        if (oreWasRefined || transplutonicsWereRefined) {
            ForgeConversionGeneral.applyForgingCRMalus(fleet, ForgeTypes.REFINING);
        }

    }

    public static void refineOre(CampaignFleetAPI fleet) {

        float machineryAvailableForRefining = ForgeConditionChecker.getPlayerCargo(Commodities.HEAVY_MACHINERY) / HEAVY_MACHINERY_REFINING_USAGE;
        float refiningCapacity = ForgeConversionGeneral.getForgingCapacity(fleet, ForgeTypes.REFINING);

        float oreInRefining = ForgeConditionChecker.getPlayerCargo(Commodities.ORE)/ ORE_TO_REFINE;

        int refiningCycles = (int) (Math.min(refiningCapacity, Math.min(machineryAvailableForRefining, oreInRefining)));

        boolean willHeavyMachineryBreakdown = ForgeConditionChecker.getMachineryBreakdownChance();
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

    public static void refineTransplutonicOre(CampaignFleetAPI fleet) {

        float machineryAvailableForRefining = ForgeConditionChecker.getPlayerCargo(Commodities.HEAVY_MACHINERY) / HEAVY_MACHINERY_REFINING_USAGE;

        float refiningCapacity = ForgeConversionGeneral.getForgingCapacity(fleet, ForgeTypes.REFINING);

        float transplutonicOreInRefining = ForgeConditionChecker.getPlayerCargo(Commodities.RARE_ORE) / TRANSPLUTONIC_ORE_TO_REFINE;

        int refiningCycles = (int) (Math.min(refiningCapacity, Math.min(machineryAvailableForRefining, transplutonicOreInRefining)));

        boolean willHeavyMachineryBreakdown = ForgeConditionChecker.getMachineryBreakdownChance();
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
