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

public class MetalsProductionLogic extends ProductionLogic {

    private static MetalsProductionLogic logicInstance;

    protected MetalsProductionLogic() {
        productionType = ProductionType.METALS_PRODUCTION;
        hasSecondaryInput = false;
        cyclePrimaryInputAmount = ORE_INPUT;
        cycleOutputAmount = METAL_OUTPUT;
        cycleMachineryUse = MACHINERY_USAGE.get(productionType);
        primaryInputType = Commodities.ORE;
        outputType = Commodities.METALS;
    }

    public static MetalsProductionLogic getInstance() {
        if (logicInstance == null) {
            logicInstance = new MetalsProductionLogic();
        }
        return logicInstance;
    }

    @Override
    protected void setReportFlags(float amountProduced) {
        ConversionVariables.metalsDailyFlag = true;
        ConversionVariables.totalMetalsProduced += amountProduced;
    }

}
