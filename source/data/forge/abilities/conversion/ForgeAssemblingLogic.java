package data.forge.abilities.conversion;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import data.forge.abilities.conversion.support.ForgeConversionGeneral;
import data.forge.abilities.conversion.support.ForgeTypes;
import data.forge.campaign.ForgeConditionChecker;

import static data.forge.abilities.conversion.support.ForgeConversionVariables.*;
import static data.forge.plugins.ForgeSettings.*;

public class ForgeAssemblingLogic {

    public static void startAssembling(CampaignFleetAPI fleet) {

        if (ForgeConversionGeneral.getForgingCapacityWithCR(fleet, ForgeTypes.ASSEMBLY) <= 1) return;
        if (!ForgeConditionChecker.hasMinimumMachinery()) return;

        if (ForgeConditionChecker.getPlayerCargo(Commodities.METALS) > METAL_TO_ASSEMBLE &&
                ForgeConditionChecker.getPlayerCargo(Commodities.RARE_METALS) > TRANSPLUTONICS_TO_ASSEMBLE) {
            assembleMachinery(fleet);
        }

        if (heavyMachineryWasAssembled) {
            ForgeConversionGeneral.applyForgingCRMalus(fleet, ForgeTypes.ASSEMBLY);
        }

    }

    public static void assembleMachinery(CampaignFleetAPI fleet) {

        int machineryAssemblingCycles = ForgeConversionGeneral.getMachineryUsageCycles(ForgeTypes.Conversion.ASSEMBLING, ForgeTypes.ASSEMBLY);

        int assemblingCapacity = ForgeConversionGeneral.getForgingCapacityWithCR(fleet, ForgeTypes.ASSEMBLY);

        int metalAssemblingCycles = (int) Math.floor(ForgeConditionChecker.getPlayerCargo(Commodities.METALS) / METAL_TO_ASSEMBLE);
        int transplutonicsAssemblingCycles = (int) Math.floor(ForgeConditionChecker.getPlayerCargo(Commodities.RARE_METALS) / TRANSPLUTONICS_TO_ASSEMBLE);

        int assemblingCycles = (int) Math.floor(Math.min(assemblingCapacity,
                Math.min(machineryAssemblingCycles, Math.min(metalAssemblingCycles, transplutonicsAssemblingCycles))));

        boolean willHeavyMachineryBreakdown = ForgeConditionChecker.getMachineryBreakdownChance();
        int MachineryInAssembling = (int) Math.ceil((assemblingCapacity * HEAVY_MACHINERY_ASSEMBLING_USAGE));
        int machineryBroken = 0;

        for (int machineryInCheck = 0; machineryInCheck < MachineryInAssembling; machineryInCheck++) {
            if (Math.random() < (BREAKDOWN_SEVERITY * ForgeConditionChecker.getForgingQuality()))
                machineryBroken++;
        }

        int dailyMetalSpent = (int) Math.ceil(METAL_TO_ASSEMBLE * assemblingCycles);
        int dailyTransplutonicsSpent = (int) Math.ceil(TRANSPLUTONICS_TO_ASSEMBLE * assemblingCycles);
        int dailyHeavyMachineryProduced = (int) Math.floor((HEAVY_MACHINERY_PRODUCED + ForgeConditionChecker.getNanoforgeAssemblingBonus()) * assemblingCycles);
        int dailyHeavyMachineryBroken = machineryBroken;

        fleet.getCargo().removeCommodity(Commodities.METALS, dailyMetalSpent);
        fleet.getCargo().removeCommodity(Commodities.RARE_METALS, dailyTransplutonicsSpent);
        fleet.getCargo().addCommodity(Commodities.HEAVY_MACHINERY, dailyHeavyMachineryProduced);
        if (willHeavyMachineryBreakdown) {
            fleet.getCargo().removeCommodity(Commodities.HEAVY_MACHINERY, dailyHeavyMachineryBroken);
        }

        totalHeavyMachineryProduction += dailyHeavyMachineryProduced;
        if (willHeavyMachineryBreakdown) {
            totalMachineryBreakage += dailyHeavyMachineryBroken;
        }

        if (willHeavyMachineryBreakdown && dailyHeavyMachineryBroken > 0) {
            breakdownReport = true;
        }

        if (dailyHeavyMachineryProduced > 0) {
            heavyMachineryWasAssembled = true;
            heavyMachineryAssemblingReport = true;
        }

    }

}
