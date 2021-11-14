package data.forge.abilities.conversion;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import data.forge.campaign.ForgeConditionChecker;

import static data.forge.abilities.conversion.ForgeConversionVariables.*;
import static data.forge.plugins.ForgeSettings.*;

public class ForgeAssemblingLogic {

    public static void startAssembling(CampaignFleetAPI fleet) {

        if (ForgeConversionGeneral.getForgingCapacity(fleet, ForgeTypes.ASSEMBLING) <= 1) return;
        if (ForgeConditionChecker.getPlayerCargo(Commodities.HEAVY_MACHINERY) <= HEAVY_MACHINERY_ASSEMBLING_USAGE) return;

        if (ForgeConditionChecker.getPlayerCargo(Commodities.METALS) > METAL_TO_ASSEMBLE &&
                ForgeConditionChecker.getPlayerCargo(Commodities.RARE_METALS) > TRANSPLUTONICS_TO_ASSEMBLE) {
            assembleMachinery(fleet);
        }

        if (heavyMachineryWasAssembled) {
            ForgeConversionGeneral.applyForgingCRMalus(fleet, ForgeTypes.ASSEMBLING);
        }

    }

    public static void assembleMachinery(CampaignFleetAPI fleet) {

        float machineryAvailableForAssembling = ForgeConditionChecker.getPlayerCargo(Commodities.HEAVY_MACHINERY) / HEAVY_MACHINERY_ASSEMBLING_USAGE;

        float assemblingCapacity = ForgeConversionGeneral.getForgingCapacity(fleet, ForgeTypes.ASSEMBLING);

        float metalInAssembling = ForgeConditionChecker.getPlayerCargo(Commodities.METALS) / METAL_TO_ASSEMBLE;
        float transplutonicsInAssembling = ForgeConditionChecker.getPlayerCargo(Commodities.RARE_METALS) / TRANSPLUTONICS_TO_ASSEMBLE;

        int assemblingCycles = (int) Math.floor(Math.min(assemblingCapacity,
                Math.min(machineryAvailableForAssembling, Math.min(metalInAssembling, transplutonicsInAssembling))));

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
