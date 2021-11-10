package data.scripts.abilities.conversion;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

import data.scripts.campaign.ForgeConditionChecker;

import static data.scripts.plugins.ForgeSettings.*;
import static data.scripts.abilities.conversion.ForgeConversionVariables.*;
import static data.scripts.hullmods.ForgeHullmodsGeneral.shipSizeEffect;

public class ForgeCentrifugingLogic {

    // Here: Custom methods

    public static int getCentrifugingCapacity(CampaignFleetAPI fleet) {
        int totalCentrifugingCapacity = 0;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if ((!ForgeConditionChecker.isOperational(member)))
                continue;
            if (member.getVariant().hasHullMod("forge_centrifuge_module"))
                totalCentrifugingCapacity += (int) Math.ceil(shipSizeEffect.get(member.getVariant().getHullSize()) *
                        (member.getRepairTracker().getCR() / 0.7f));
        }
        return totalCentrifugingCapacity;
    }

    public static void applyCentrifugingCRMalus(CampaignFleetAPI fleet) {
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if ((!ForgeConditionChecker.isOperational(member)))
                continue;
            if (member.getVariant().hasHullMod("forge_centrifuge_module"))
                member.getRepairTracker().applyCREvent(-(((float) CR_PRODUCTION_DECAY * ForgeConditionChecker.getForgingQuality()) *
                        (member.getRepairTracker().getCR() / 0.7f)), "Centrifuged fuel");
        }
    }

    public static int getPossibleCentrifugingCycles(CampaignFleetAPI fleet) {

        int fuelInTanks = (int) fleet.getCargo().getCommodityQuantity(Commodities.FUEL);
        int maxTanksCapacity = (int) fleet.getCargo().getMaxFuel();
        int availableTanksCapacity = maxTanksCapacity - fuelInTanks;

        return (int) Math.floor(availableTanksCapacity / (FUEL_PRODUCED + ForgeConditionChecker.getSynchrotronCoreBonus()));
    }

    // Here: Conversion methods

    public static void centrifugeFuel(CampaignFleetAPI fleet) {

        float heavyMachineryAvailable = fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY);
        if (heavyMachineryAvailable <= HEAVY_MACHINERY_CENTRIFUGING_USAGE) return;

        float heavyMachineryInCentrifuging = heavyMachineryAvailable / HEAVY_MACHINERY_CENTRIFUGING_USAGE;
        float volatilesAvailable = fleet.getCargo().getCommodityQuantity(Commodities.VOLATILES);

        float centrifugingCapacity = getCentrifugingCapacity(fleet);

        int possibleCentrifugingCycles = getPossibleCentrifugingCycles(fleet);

        if (volatilesAvailable > VOLATILES_TO_CENTRIFUGE && possibleCentrifugingCycles >= 1) {

            float volatilesInCentrifuging = volatilesAvailable / VOLATILES_TO_CENTRIFUGE;

            int initialCentrifugingCycles = (int) Math.min(centrifugingCapacity, Math.min(heavyMachineryInCentrifuging, volatilesInCentrifuging));

            int centrifugingCycles = Math.min(initialCentrifugingCycles, possibleCentrifugingCycles);

            boolean willHeavyMachineryBreakdown = (Math.random()<((float) BASE_BREAKDOWN_CHANCE * ForgeConditionChecker.getForgingQuality()));
            int machineryInCentrifuging = (int) Math.ceil((centrifugingCycles * HEAVY_MACHINERY_CENTRIFUGING_USAGE));
            int machineryBroken = 0;

            for (int machineryInCheck = 0; machineryInCheck < machineryInCentrifuging; machineryInCheck++) {
                if (Math.random()<((float) BREAKDOWN_SEVERITY * ForgeConditionChecker.getForgingQuality())) machineryBroken++;
            }

            float dailyVolatilesSpent = VOLATILES_TO_CENTRIFUGE * centrifugingCycles;
            float dailyFuelProduced = (FUEL_PRODUCED + ForgeConditionChecker.getSynchrotronCoreBonus()) * centrifugingCycles;
            float dailyHeavyMachineryBroken = machineryBroken;

            fleet.getCargo().removeCommodity(Commodities.VOLATILES, dailyVolatilesSpent);
            fleet.getCargo().addCommodity(Commodities.FUEL, dailyFuelProduced);
            if(willHeavyMachineryBreakdown) {
                fleet.getCargo().removeCommodity(Commodities.HEAVY_MACHINERY, dailyHeavyMachineryBroken);
            }

            totalVolatilesExpenditure +=  dailyVolatilesSpent;
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

}
