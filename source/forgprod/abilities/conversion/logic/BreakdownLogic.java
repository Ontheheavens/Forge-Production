package forgprod.abilities.conversion.logic;

import java.util.Map;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

import forgprod.abilities.conversion.support.ConversionVariables;
import forgprod.abilities.conversion.support.checks.FleetwideProductionChecks;
import forgprod.abilities.modules.FleetwideModuleManager;
import forgprod.abilities.modules.dataholders.ProductionModule;
import forgprod.settings.SettingsHolder;

import static forgprod.abilities.conversion.support.checks.ItemBonusesChecks.getNanoforgeBreakdownDecrease;

/**
 * Collects amount of daily used machinery from all production types and handles breakdowns.
 * Singleton-pattern class.
 * @author Ontheheavens
 * @since 03.12.2022
 */

public class BreakdownLogic {

    private static BreakdownLogic logicInstance;
    private float totalDailyMachineryUsed;

    public static BreakdownLogic getInstance() {
        if (logicInstance == null) {
            logicInstance = new BreakdownLogic();
        }
        return logicInstance;
    }

    public void addToTotalDailyMachineryUsed(float dailyMachineryUsedByType) {
        this.totalDailyMachineryUsed += dailyMachineryUsedByType;
    }

    // Needs to be called after every other production type has been finished.
    // Two types of breakdowns are intended: the usual machinery breakdown, and fatal accident.
    // First means lost machinery, second means you also lose crew.
    // Second can't happen without the first.
    public void calculateBreakdowns(CampaignFleetAPI fleet) {
        float baseBreakdownChance = FleetwideProductionChecks.getBreakdownChance(fleet);
        boolean breakdownHappened = Math.random() < baseBreakdownChance;
        if (breakdownHappened && totalDailyMachineryUsed > 10) {
            float minimalBreakdown = FleetwideProductionChecks.getMinimalBreakdownSeverity(fleet);
            float maximalBreakdown = FleetwideProductionChecks.getMaximalBreakdownSeverity(fleet);
            float finalBreakdownSeverity = (float) (minimalBreakdown + Math.random() * (maximalBreakdown - minimalBreakdown));
            float dailyMachineryBroken = totalDailyMachineryUsed * finalBreakdownSeverity;

            boolean fatalAccidentHappened = Math.random() < (FleetwideProductionChecks.getFatalAccidentChance(fleet));
            if (fatalAccidentHappened && totalDailyMachineryUsed > 50) {
                calculateFatalAccidents(fleet);
            }
            fleet.getCargo().removeCommodity(Commodities.HEAVY_MACHINERY, dailyMachineryBroken);
            ConversionVariables.totalMachineryBroke += dailyMachineryBroken;
        }
        totalDailyMachineryUsed = 0f;
    }

    private void calculateFatalAccidents(CampaignFleetAPI fleet) {
        float totalCrewEmployed = 0;
        Map<FleetMemberAPI, ProductionModule> moduleIndex = FleetwideModuleManager.getInstance().getModuleIndex();
        for (ProductionModule module : moduleIndex.values()) {
            if (module.hadProducedToday()) {
                totalCrewEmployed += module.getParentFleetMember().getCrewComposition().getCrew();
            }
        }
        float baseAccidentSeverity = (SettingsHolder.BREAKDOWN_SEVERITY * getNanoforgeBreakdownDecrease(fleet)) / 2;
        float finalAccidentSeverity = (float) (baseAccidentSeverity * Math.random());
        float dailyCrewmenLost = totalCrewEmployed * finalAccidentSeverity;
        if (dailyCrewmenLost < 1f) {
            dailyCrewmenLost = 1f;
        }
        fleet.getCargo().removeCommodity(Commodities.CREW, dailyCrewmenLost);
        ConversionVariables.totalCrewmenLost += dailyCrewmenLost;
    }

}
