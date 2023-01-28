package forgprod.abilities.conversion.logic.production;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

import forgprod.abilities.conversion.logic.base.ProductionLogic;
import forgprod.abilities.conversion.support.ProductionType;
import forgprod.abilities.conversion.support.ConversionVariables;

import static forgprod.abilities.conversion.support.ProductionConstants.*;

/**
 * @author Ontheheavens
 * @since 04.12.2022
 */

public class FuelProductionLogic extends ProductionLogic {

    private static FuelProductionLogic logicInstance;

    protected FuelProductionLogic() {
        productionType = ProductionType.FUEL_PRODUCTION;
        hasSecondaryInput = false;
        cyclePrimaryInputAmount = VOLATILES_INPUT;
        cycleOutputAmount = FUEL_OUTPUT;
        cycleMachineryUse = MACHINERY_USAGE.get(productionType);
        primaryInputType = Commodities.VOLATILES;
        outputType = Commodities.FUEL;
    }

    public static FuelProductionLogic getInstance() {
        if (logicInstance == null) {
            logicInstance = new FuelProductionLogic();
        }
        return logicInstance;
    }

    @Override
    public void initializeProduction(CampaignFleetAPI fleet) {
        if (areFuelTanksFull(fleet)) {
            return;
        }
        super.initializeProduction(fleet);
    }

    public boolean areFuelTanksFull(CampaignFleetAPI fleet) {
        return fleet.getCargo().getFreeFuelSpace() < (cycleOutputAmount + getCycleOutputBonus(fleet));
    }

    @Override
    protected void setReportFlags(float amountProduced) {
        ConversionVariables.fuelDailyFlag = true;
        ConversionVariables.totalFuelProduced += amountProduced;
    }

}
