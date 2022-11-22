package data.forge.abilities.conversion;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

import data.forge.abilities.conversion.support.ForgeConversionGeneral;
import data.forge.abilities.conversion.support.ForgeTypes;
import data.forge.campaign.ForgeConditionChecker;

import static data.forge.plugins.ForgeSettings.*;
import static data.forge.abilities.conversion.support.ForgeConversionVariables.*;

public class ForgeCentrifugingLogic {

    public static void startCentrifuging(CampaignFleetAPI fleet) {

        if (ForgeConversionGeneral.getForgingCapacityWithCR(fleet, ForgeTypes.CENTRIFUGE) <= 1) return;
        if (!ForgeConditionChecker.hasMinimumMachinery()) return;
        if (getPossibleCentrifugingCycles(fleet) < 1) return;

        if (ForgeConditionChecker.getPlayerCargo(Commodities.VOLATILES) > VOLATILES_TO_CENTRIFUGE) {
            centrifugeFuel(fleet);
        }

        if (fuelWasCentrifuged) {
            ForgeConversionGeneral.applyForgingCRMalus(fleet, ForgeTypes.CENTRIFUGE);
        }

    }

    public static int getPossibleCentrifugingCycles(CampaignFleetAPI fleet) {

        return (int) Math.floor(fleet.getCargo().getFreeFuelSpace() / (FUEL_PRODUCED + ForgeConditionChecker.getSynchrotronCoreBonus()));

    }

    public static void centrifugeFuel(CampaignFleetAPI fleet) {

        int machineryCentrifugingCycles = ForgeConversionGeneral.getMachineryUsageCycles(ForgeTypes.Conversion.CENTRIFUGING, ForgeTypes.CENTRIFUGE);

        int centrifugingCapacity = ForgeConversionGeneral.getForgingCapacityWithCR(fleet, ForgeTypes.CENTRIFUGE);

        int volatilesCentrifugingCycles = (int) Math.floor(ForgeConditionChecker.getPlayerCargo(Commodities.VOLATILES) / VOLATILES_TO_CENTRIFUGE);

        int initialCentrifugingCycles = Math.min(centrifugingCapacity, Math.min(machineryCentrifugingCycles, volatilesCentrifugingCycles));

        int centrifugingCycles = (int) Math.floor(Math.min(initialCentrifugingCycles, getPossibleCentrifugingCycles(fleet)));

        boolean willHeavyMachineryBreakdown = ForgeConditionChecker.getMachineryBreakdownChance();
        int machineryInCentrifuging = (int) Math.ceil((centrifugingCycles * HEAVY_MACHINERY_CENTRIFUGING_USAGE));
        int machineryBroken = 0;

        for (int machineryInCheck = 0; machineryInCheck < machineryInCentrifuging; machineryInCheck++) {
            if (Math.random()<(BREAKDOWN_SEVERITY * ForgeConditionChecker.getForgingQuality())) machineryBroken++;
        }

        int dailyVolatilesSpent = (int) Math.ceil(VOLATILES_TO_CENTRIFUGE * centrifugingCycles);
        int dailyFuelProduced = (int) Math.floor((FUEL_PRODUCED + ForgeConditionChecker.getSynchrotronCoreBonus()) * centrifugingCycles);
        int dailyHeavyMachineryBroken = machineryBroken;

        fleet.getCargo().removeCommodity(Commodities.VOLATILES, dailyVolatilesSpent);
        fleet.getCargo().addCommodity(Commodities.FUEL, dailyFuelProduced);
        if(willHeavyMachineryBreakdown) {
            fleet.getCargo().removeCommodity(Commodities.HEAVY_MACHINERY, dailyHeavyMachineryBroken);
        }

        totalFuelProduction +=  dailyFuelProduced;
        if(willHeavyMachineryBreakdown) {
            totalMachineryBreakage +=  dailyHeavyMachineryBroken;
        }

        if (willHeavyMachineryBreakdown && dailyHeavyMachineryBroken > 0) {
            breakdownReport = true;
        }

        if (centrifugingCycles >= 1 && dailyFuelProduced > 0) {
            fuelWasCentrifuged = true;
            fuelCentrifugingReport = true;
        }

    }

}
