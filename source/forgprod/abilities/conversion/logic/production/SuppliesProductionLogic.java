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

public class SuppliesProductionLogic extends ProductionLogic {

    private static SuppliesProductionLogic logicInstance;

    protected SuppliesProductionLogic() {
        productionType = ProductionType.SUPPLIES_PRODUCTION;
        hasSecondaryInput = true;
        cyclePrimaryInputAmount = METALS_INPUT_SUPPLIES;
        cycleSecondaryInputAmount = TRANSPLUTONICS_INPUT_SUPPLIES;
        cycleOutputAmount = SUPPLIES_OUTPUT;
        cycleMachineryUse = MACHINERY_USAGE.get(productionType);
        primaryInputType = Commodities.METALS;
        secondaryInputType = Commodities.RARE_METALS;
        outputType = Commodities.SUPPLIES;
    }

    public static SuppliesProductionLogic getInstance() {
        if (logicInstance == null) {
            logicInstance = new SuppliesProductionLogic();
        }
        return logicInstance;
    }

    @Override
    protected void setReportFlags(float amountProduced) {
        ConversionVariables.suppliesDailyFlag = true;
        ConversionVariables.totalSuppliesProduced += amountProduced;
    }

}
