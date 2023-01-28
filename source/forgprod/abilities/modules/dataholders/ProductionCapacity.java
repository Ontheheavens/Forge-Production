package forgprod.abilities.modules.dataholders;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import forgprod.abilities.conversion.logic.LogicFactory;
import forgprod.abilities.conversion.logic.base.ProductionLogic;
import forgprod.abilities.conversion.support.checks.FleetwideProductionChecks;
import forgprod.abilities.conversion.support.ProductionConstants;
import forgprod.abilities.conversion.support.ProductionType;
import forgprod.abilities.conversion.support.checks.ModuleConditionChecks;
import forgprod.settings.SettingsHolder;

/**
 * Info about specific production capacity of specific ship and tools to mutate it.
 * Serves as a basic building block for object-conceived fleet production logic.
 * @author Ontheheavens
 * @since 02.12.2022
 */

public class ProductionCapacity {

    private final ProductionModule parentModule;
    private final ProductionType productionType;
    private boolean isToggledOn;
    private final int baseCapacityCycles;
    private final float baseMachineryUse;

    protected ProductionCapacity(ProductionModule parentModule,
                                 ProductionType productionType,
                                 ShipAPI.HullSize shipSize) {
        this.parentModule = parentModule;
        this.isToggledOn = true;
        this.productionType = productionType;
        this.baseCapacityCycles = ProductionConstants.SHIPSIZE_CAPACITY.get(shipSize) * SettingsHolder.GRANULARITY;
        this.baseMachineryUse = ProductionConstants.MACHINERY_USAGE.get(productionType) * this.baseCapacityCycles;
    }

    public ProductionModule getParentModule() {
        return this.parentModule;
    }

    public ProductionType getProductionType() {
        return this.productionType;
    }

    public ProductionLogic getLogic() {
        return LogicFactory.getLogic(productionType);
    }

    public boolean isActive() {
        if (!parentShipOperational()) {
            return false;
        }
        return this.isToggledOn;
    }

    public boolean parentShipOperational() {
        return ModuleConditionChecks.isOperational(this.parentModule.getParentFleetMember());
    }

    public boolean isToggledOn() {
        return isToggledOn;
    }

    public void setToggledOn(boolean toggledOn) {
        this.isToggledOn = toggledOn;
    }

    // Needed to calculate fleetwide machinery availability.
    public float getMachineryUse() {
        return this.baseMachineryUse / this.getParentActiveCapacitiesAmount();
    }

    public float getCurrentThroughput() {
        if (!this.isActive()) {
            return 0f;
        }
        CampaignFleetAPI fleet = getParentFleet();
        if (!parentFleetHasMinimumMachinery()) {
            return 0f;
        }
        return ((float) getModifiedCapacity(fleet)) / this.baseCapacityCycles;
    }

    public boolean parentFleetHasMinimumMachinery() {
        CampaignFleetAPI fleet = getParentFleet();
        return FleetwideProductionChecks.hasMinimumMachinery(fleet);
    }

    private CampaignFleetAPI getParentFleet() {
        return this.getParentModule().getParentFleetMember().getFleetData().getFleet();
    }

    // Anything after the dot is intended to be discarded.
    // Lorefully rationalized as inefficient use of productive capacities when machinery is in short supply.
    // Assumes granularity and base production values are not too low so as to not permit less-than-one input cases.
    public int getModifiedCapacity(CampaignFleetAPI fleet) {
        if (this.getParentActiveCapacitiesAmount() <=0) {
            return 0;
        }
        int capacityShare = this.baseCapacityCycles / this.getParentActiveCapacitiesAmount();
        return (int) (capacityShare * FleetwideProductionChecks.getMachineryAvailability(fleet));
    }

    public int getParentActiveCapacitiesAmount() {
        int amount = 0;
        for (ProductionCapacity sibling : this.parentModule.getModuleCapacities()) {
            if (sibling.isActive()) {
                ++amount;
            }
        }
        return amount;
    }

}
