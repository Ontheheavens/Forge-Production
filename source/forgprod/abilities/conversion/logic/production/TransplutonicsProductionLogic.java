package forgprod.abilities.conversion.logic.production;

import com.fs.starfarer.api.impl.campaign.ids.Commodities;

import forgprod.abilities.conversion.logic.base.ProductionLogic;
import forgprod.abilities.conversion.support.ProductionType;
import forgprod.abilities.conversion.support.ConversionVariables;

import static forgprod.abilities.conversion.support.ProductionConstants.*;

/**
 * New iteration of commodity production logic.
 * Singleton-pattern class.
 * <p>
 *
 * @author Ontheheavens
 * @since 03.12.2022
 */

public class TransplutonicsProductionLogic extends ProductionLogic {

    private static TransplutonicsProductionLogic logicInstance;

    protected TransplutonicsProductionLogic() {
        productionType = ProductionType.TRANSPLUTONICS_PRODUCTION;
        hasSecondaryInput = false;
        cyclePrimaryInputAmount = TRANSPLUTONIC_ORE_INPUT;
        cycleOutputAmount = TRANSPLUTONICS_OUTPUT;
        cycleMachineryUse = MACHINERY_USAGE.get(productionType);
        primaryInputType = Commodities.RARE_ORE;
        outputType = Commodities.RARE_METALS;
    }

    public static TransplutonicsProductionLogic getInstance() {
        if (logicInstance == null) {
            logicInstance = new TransplutonicsProductionLogic();
        }
        return logicInstance;
    }

    @Override
    protected void setReportFlags(float amountProduced) {
        ConversionVariables.transplutonicsDailyFlag = true;
        ConversionVariables.totalTransplutonicsProduced += amountProduced;
    }

}
