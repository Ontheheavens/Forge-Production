package forgprod.hullmods;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import forgprod.abilities.conversion.support.ProductionType;
import forgprod.hullmods.base.BaseProductionHullmod;

/**
 * @author Ontheheavens
 * @since 06.12.2022
 */

@SuppressWarnings("unused")
public class RefiningModule extends BaseProductionHullmod {

    @Override
    public void addCapacitySection(TooltipMakerAPI tooltip, ShipAPI ship) {
        if (!super.addAvailableCapacities(tooltip, ship)) {
            super.addCapacityPanels(tooltip, ship, true, ProductionType.METALS_PRODUCTION,
                    ProductionType.TRANSPLUTONICS_PRODUCTION);
        }
    }

}
