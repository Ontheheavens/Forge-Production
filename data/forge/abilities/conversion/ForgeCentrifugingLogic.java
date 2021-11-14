package data.forge.abilities.conversion;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

import data.forge.campaign.ForgeConditionChecker;

import static data.forge.plugins.ForgeSettings.*;
import static data.forge.abilities.conversion.ForgeConversionVariables.*;

public class ForgeCentrifugingLogic {

    public static void startCentrifuging(CampaignFleetAPI fleet) {

        if (ForgeConversionGeneral.getForgingCapacity(fleet, ForgeTypes.CENTRIFUGING) <= 1) return;
        if (ForgeConditionChecker.getPlayerCargo(Commodities.HEAVY_MACHINERY) <= HEAVY_MACHINERY_CENTRIFUGING_USAGE) return;
        if (getPossibleCentrifugingCycles(fleet) < 1) return;

        if (ForgeConditionChecker.getPlayerCargo(Commodities.VOLATILES) > VOLATILES_TO_CENTRIFUGE) {
            centrifugeFuel(fleet);
        }

        if (fuelWasCentrifuged) {
            ForgeConversionGeneral.applyForgingCRMalus(fleet, ForgeTypes.CENTRIFUGING);
        }

    }

    public static int getPossibleCentrifugingCycles(CampaignFleetAPI fleet) {

        return (int) Math.floor(fleet.getCargo().getFreeFuelSpace() / (FUEL_PRODUCED + ForgeConditionChecker.getSynchrotronCoreBonus()));

    }

    public static void centrifugeFuel(CampaignFleetAPI fleet) {

        float machineryAvailableForCentrifuging = ForgeConditionChecker.getPlayerCargo(Commodities.HEAVY_MACHINERY) / HEAVY_MACHINERY_CENTRIFUGING_USAGE;

        float centrifugingCapacity = ForgeConversionGeneral.getForgingCapacity(fleet, ForgeTypes.CENTRIFUGING);

        float volatilesInCentrifuging = ForgeConditionChecker.getPlayerCargo(Commodities.VOLATILES) / VOLATILES_TO_CENTRIFUGE;

        int initialCentrifugingCycles = (int) Math.min(centrifugingCapacity, Math.min(machineryAvailableForCentrifuging, volatilesInCentrifuging));

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
