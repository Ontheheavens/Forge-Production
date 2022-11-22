package data.forge.abilities.conversion;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

import data.forge.abilities.conversion.support.ForgeConversionGeneral;
import data.forge.abilities.conversion.support.ForgeTypes;
import data.forge.campaign.ForgeConditionChecker;
import static data.forge.plugins.ForgeSettings.*;
import static data.forge.abilities.conversion.support.ForgeConversionVariables.*;

public class ForgeRefiningLogic {

    public static void startRefining(CampaignFleetAPI fleet) {

        if (ForgeConversionGeneral.getForgingCapacityWithCR(fleet, ForgeTypes.REFINERY) <= 1) return;
        if (!ForgeConditionChecker.hasMinimumMachinery()) return;

        if (ForgeConditionChecker.getPlayerCargo(Commodities.ORE) > ORE_TO_REFINE) {
            refineOre(fleet);
        }

        if (ForgeConditionChecker.getPlayerCargo(Commodities.RARE_ORE) > TRANSPLUTONIC_ORE_TO_REFINE) {
            refineTransplutonicOre(fleet);
        }

        if (oreWasRefined || transplutonicsWereRefined) {
            ForgeConversionGeneral.applyForgingCRMalus(fleet, ForgeTypes.REFINERY);
        }

    }

    public static void refineOre(CampaignFleetAPI fleet) {

        int machineryRefiningCycles = ForgeConversionGeneral.getMachineryUsageCycles(ForgeTypes.Conversion.REFINING, ForgeTypes.REFINERY);

        int refiningCapacity = ForgeConversionGeneral.getForgingCapacityWithCR(fleet, ForgeTypes.REFINERY);

        int oreRefiningCycles = (int) Math.floor(ForgeConditionChecker.getPlayerCargo(Commodities.ORE)/ ORE_TO_REFINE);

        int refiningCycles = (int) Math.floor(Math.min(refiningCapacity, Math.min(machineryRefiningCycles, oreRefiningCycles)));

        boolean willHeavyMachineryBreakdown = ForgeConditionChecker.getMachineryBreakdownChance();
        int MachineryInRefining = (int) Math.ceil((refiningCycles * HEAVY_MACHINERY_REFINING_USAGE));
        int machineryBroken = 0;

        for (int machineryInCheck = 0; machineryInCheck < MachineryInRefining; machineryInCheck++) {
            if (Math.random()<(BREAKDOWN_SEVERITY * ForgeConditionChecker.getForgingQuality())) machineryBroken++;
        }

        int dailyOreSpent = (int) Math.ceil(ORE_TO_REFINE * refiningCycles);
        int dailyMetalProduced = (int) Math.floor((METAL_PRODUCED + ForgeConditionChecker.getCatalyticCoreBonus()) * refiningCycles);
        int dailyHeavyMachineryBroken = machineryBroken;

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

        int machineryRefiningCycles = ForgeConversionGeneral.getMachineryUsageCycles(ForgeTypes.Conversion.REFINING, ForgeTypes.REFINERY);

        int refiningCapacity = ForgeConversionGeneral.getForgingCapacityWithCR(fleet, ForgeTypes.REFINERY);

        int transplutonicOreRefiningCycles = (int) Math.floor(ForgeConditionChecker.getPlayerCargo(Commodities.RARE_ORE) / TRANSPLUTONIC_ORE_TO_REFINE);

        int refiningCycles = (int) Math.floor(Math.min(refiningCapacity, Math.min(machineryRefiningCycles, transplutonicOreRefiningCycles)));

        boolean willHeavyMachineryBreakdown = ForgeConditionChecker.getMachineryBreakdownChance();
        int machineryInRefining = (int) Math.ceil((refiningCycles * HEAVY_MACHINERY_REFINING_USAGE));
        int machineryBroken = 0;

        for (int machineryInCheck = 0; machineryInCheck < machineryInRefining; machineryInCheck++) {
            if (Math.random()<(BREAKDOWN_SEVERITY * ForgeConditionChecker.getForgingQuality())) machineryBroken++;
        }

        int dailyTransplutonicOreSpent = (int) Math.ceil(TRANSPLUTONIC_ORE_TO_REFINE * refiningCycles);
        int dailyTransplutonicsProduced = (int) Math.floor((TRANSPLUTONICS_PRODUCED + ForgeConditionChecker.getCatalyticCoreBonus()) * refiningCycles);
        int dailyHeavyMachineryBroken = machineryBroken;

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
