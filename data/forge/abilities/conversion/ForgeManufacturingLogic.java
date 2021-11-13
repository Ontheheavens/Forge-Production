package data.forge.abilities.conversion;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import data.forge.campaign.ForgeConditionChecker;

import static data.forge.abilities.conversion.ForgeConversionVariables.*;
import static data.forge.plugins.ForgeSettings.*;

public class ForgeManufacturingLogic {

    public static void startManufacturing(CampaignFleetAPI fleet) {

        if (ForgeConversionGeneral.getForgingCapacity(fleet, ForgeTypes.MANUFACTURING) <= 1) return;
        if (ForgeConditionChecker.getPlayerCargo(Commodities.HEAVY_MACHINERY) <= HEAVY_MACHINERY_MANUFACTURING_USAGE) return;

        if (ForgeConditionChecker.getPlayerCargo(Commodities.METALS) > METAL_TO_MANUFACTURE &&
                ForgeConditionChecker.getPlayerCargo(Commodities.RARE_METALS) > TRANSPLUTONICS_TO_MANUFACTURE) {
            manufactureSupplies(fleet);
        }

        if (suppliesWereManufactured) {
            ForgeConversionGeneral.applyForgingCRMalus(fleet, ForgeTypes.MANUFACTURING);
        }

    }

    public static void manufactureSupplies(CampaignFleetAPI fleet) {

        float machineryAvailableForManufacturing = ForgeConditionChecker.getPlayerCargo(Commodities.HEAVY_MACHINERY) / HEAVY_MACHINERY_MANUFACTURING_USAGE;

        float manufacturingCapacity = ForgeConversionGeneral.getForgingCapacity(fleet, ForgeTypes.MANUFACTURING);

        float metalInManufacturing = ForgeConditionChecker.getPlayerCargo(Commodities.METALS) / METAL_TO_MANUFACTURE;
        float transplutonicsInManufacturing = ForgeConditionChecker.getPlayerCargo(Commodities.RARE_METALS) / TRANSPLUTONICS_TO_MANUFACTURE;

        int manufacturingCycles = (int) (Math.min(manufacturingCapacity,
                Math.min(machineryAvailableForManufacturing, Math.min(metalInManufacturing, transplutonicsInManufacturing))));

        boolean willHeavyMachineryBreakdown = ForgeConditionChecker.getMachineryBreakdownChance();
        int MachineryInManufacturing = (int) Math.ceil((manufacturingCycles * HEAVY_MACHINERY_MANUFACTURING_USAGE));
        int machineryBroken = 0;

        for (int machineryInCheck = 0; machineryInCheck < MachineryInManufacturing; machineryInCheck++) {
            if (Math.random() < ((float) BREAKDOWN_SEVERITY * ForgeConditionChecker.getForgingQuality()))
                machineryBroken++;
        }

        float dailyMetalSpent = METAL_TO_MANUFACTURE * manufacturingCycles;
        float dailyTransplutonicsSpent = TRANSPLUTONICS_TO_MANUFACTURE * manufacturingCycles;
        float dailySuppliesProduced = (SUPPLIES_PRODUCED + ForgeConditionChecker.getNanoforgeManufacturingBonus()) * manufacturingCycles;
        float dailyHeavyMachineryBroken = machineryBroken;

        fleet.getCargo().removeCommodity(Commodities.METALS, dailyMetalSpent);
        fleet.getCargo().removeCommodity(Commodities.RARE_METALS, dailyTransplutonicsSpent);
        fleet.getCargo().addCommodity(Commodities.SUPPLIES, dailySuppliesProduced);
        if (willHeavyMachineryBreakdown) {
            fleet.getCargo().removeCommodity(Commodities.HEAVY_MACHINERY, dailyHeavyMachineryBroken);
        }

        totalSuppliesProduction += dailySuppliesProduced;
        if (willHeavyMachineryBreakdown) {
            totalMachineryBreakage += dailyHeavyMachineryBroken;
        }

        if (willHeavyMachineryBreakdown && dailyHeavyMachineryBroken > 0) {
            breakdownReport = true;
        }

        if (dailySuppliesProduced > 0) {
            suppliesWereManufactured = true;
            suppliesManufacturingReport = true;
        }

    }

}
