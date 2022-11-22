package data.forge.abilities.conversion;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import data.forge.abilities.conversion.support.ForgeConversionGeneral;
import data.forge.abilities.conversion.support.ForgeTypes;
import data.forge.campaign.ForgeConditionChecker;

import static data.forge.abilities.conversion.support.ForgeConversionVariables.*;
import static data.forge.plugins.ForgeSettings.*;

public class ForgeManufacturingLogic {

    public static void startManufacturing(CampaignFleetAPI fleet) {

        if (ForgeConversionGeneral.getForgingCapacityWithCR(fleet, ForgeTypes.MANUFACTURE) <= 1) return;
        if (!ForgeConditionChecker.hasMinimumMachinery()) return;

        if (ForgeConditionChecker.getPlayerCargo(Commodities.METALS) > METAL_TO_MANUFACTURE &&
                ForgeConditionChecker.getPlayerCargo(Commodities.RARE_METALS) > TRANSPLUTONICS_TO_MANUFACTURE) {
            manufactureSupplies(fleet);
        }

        if (suppliesWereManufactured) {
            ForgeConversionGeneral.applyForgingCRMalus(fleet, ForgeTypes.MANUFACTURE);
        }

    }

    public static void manufactureSupplies(CampaignFleetAPI fleet) {

        int machineryManufacturingCycles = ForgeConversionGeneral.getMachineryUsageCycles(ForgeTypes.Conversion.MANUFACTURING, ForgeTypes.MANUFACTURE);

        int manufacturingCapacity = ForgeConversionGeneral.getForgingCapacityWithCR(fleet, ForgeTypes.MANUFACTURE);

        int metalManufacturingCycles = (int) Math.floor(ForgeConditionChecker.getPlayerCargo(Commodities.METALS) / METAL_TO_MANUFACTURE);
        int transplutonicsManufacturingCycles = (int) Math.floor(ForgeConditionChecker.getPlayerCargo(Commodities.RARE_METALS) / TRANSPLUTONICS_TO_MANUFACTURE);

        int manufacturingCycles = (int) Math.floor(Math.min(manufacturingCapacity,
                Math.min(machineryManufacturingCycles, Math.min(metalManufacturingCycles, transplutonicsManufacturingCycles))));

        boolean willHeavyMachineryBreakdown = ForgeConditionChecker.getMachineryBreakdownChance();
        int MachineryInManufacturing = (int) Math.ceil((manufacturingCycles * HEAVY_MACHINERY_MANUFACTURING_USAGE));
        int machineryBroken = 0;

        for (int machineryInCheck = 0; machineryInCheck < MachineryInManufacturing; machineryInCheck++) {
            if (Math.random() < (BREAKDOWN_SEVERITY * ForgeConditionChecker.getForgingQuality()))
                machineryBroken++;
        }

        int dailyMetalSpent = (int) Math.ceil(METAL_TO_MANUFACTURE * manufacturingCycles);
        int dailyTransplutonicsSpent = (int) Math.ceil(TRANSPLUTONICS_TO_MANUFACTURE * manufacturingCycles);
        int dailySuppliesProduced = (int) Math.floor((SUPPLIES_PRODUCED + ForgeConditionChecker.getNanoforgeManufacturingBonus()) * manufacturingCycles);
        int dailyHeavyMachineryBroken = machineryBroken;

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
