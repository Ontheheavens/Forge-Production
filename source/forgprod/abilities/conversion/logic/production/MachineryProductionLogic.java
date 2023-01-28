package forgprod.abilities.conversion.logic.production;

import com.fs.starfarer.api.impl.campaign.ids.Commodities;

import forgprod.abilities.conversion.logic.base.ProductionLogic;
import forgprod.abilities.conversion.support.ProductionType;
import forgprod.abilities.conversion.support.ConversionVariables;

import static forgprod.abilities.conversion.support.ProductionConstants.*;

/**
 * @author Ontheheavens
 * @since 04.12.2022
 */

public class MachineryProductionLogic extends ProductionLogic {

    private static MachineryProductionLogic logicInstance;

    protected MachineryProductionLogic() {
        productionType = ProductionType.MACHINERY_PRODUCTION;
        hasSecondaryInput = true;
        cyclePrimaryInputAmount = METALS_INPUT_MACHINERY;
        cycleSecondaryInputAmount = TRANSPLUTONICS_INPUT_MACHINERY;
        cycleOutputAmount = MACHINERY_OUTPUT;
        cycleMachineryUse = MACHINERY_USAGE.get(productionType);
        primaryInputType = Commodities.METALS;
        secondaryInputType = Commodities.RARE_METALS;
        outputType = Commodities.HEAVY_MACHINERY;
    }

    public static MachineryProductionLogic getInstance() {
        if (logicInstance == null) {
            logicInstance = new MachineryProductionLogic();
        }
        return logicInstance;
    }

    @Override
    protected void setReportFlags(float amountProduced) {
        ConversionVariables.machineryDailyFlag = true;
        ConversionVariables.totalMachineryProduced += amountProduced;
    }

}
