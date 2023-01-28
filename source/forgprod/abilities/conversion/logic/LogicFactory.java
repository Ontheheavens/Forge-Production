package forgprod.abilities.conversion.logic;

import forgprod.abilities.conversion.logic.base.ProductionLogic;
import forgprod.abilities.conversion.logic.production.*;
import forgprod.abilities.conversion.support.ProductionType;

/**
 * @author Ontheheavens
 * @since 26.12.2022
 */

public class LogicFactory {

    public static ProductionLogic getLogic(ProductionType type) {
        switch (type) {
            case METALS_PRODUCTION:
                return MetalsProductionLogic.getInstance();
            case TRANSPLUTONICS_PRODUCTION:
                return TransplutonicsProductionLogic.getInstance();
            case FUEL_PRODUCTION:
                return FuelProductionLogic.getInstance();
            case SUPPLIES_PRODUCTION:
                return SuppliesProductionLogic.getInstance();
            case MACHINERY_PRODUCTION:
                return MachineryProductionLogic.getInstance();
            case HULL_PARTS_PRODUCTION:
                return HullPartsProductionLogic.getInstance();
            default:
                return new ProductionLogic() {};
        }
    }

}
