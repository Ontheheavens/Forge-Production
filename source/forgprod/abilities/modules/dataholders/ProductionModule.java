package forgprod.abilities.modules.dataholders;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import forgprod.abilities.conversion.support.ProductionConstants;
import forgprod.abilities.conversion.support.ProductionType;
import forgprod.abilities.modules.FleetwideModuleManager;

/**
 * Storage of persistent data about forge ships and their modules.
 * @author Ontheheavens
 * @since 30.11.2022
 */

public class ProductionModule {

    private final String hullmodId;
    private FleetMemberAPI parentFleetMember;
    private List<ProductionCapacity> moduleCapacities;
    private ProductionMode capacitiesMode;

    public enum ProductionMode {
        PRIMARY,
        SECONDARY
    }

    private boolean producedToday;

    public ProductionModule(FleetMemberAPI fleetMember, String hullmodId) {
        this.hullmodId = hullmodId;
        this.parentFleetMember = fleetMember;
        this.capacitiesMode = ProductionMode.PRIMARY;
        this.populateCapacities(hullmodId, fleetMember);
    }

    public String getHullmodId() {
        return hullmodId;
    }

    public ProductionMode getCapacitiesMode() {
        return capacitiesMode;
    }

    public void setCapacitiesMode(ProductionMode capacitiesMode) {
        if (this.capacitiesMode == capacitiesMode) return;
        this.capacitiesMode = capacitiesMode;
        this.clearCapacities();
        this.populateCapacities(this.hullmodId, this.parentFleetMember);
    }

    public FleetMemberAPI getParentFleetMember() {
        return parentFleetMember;
    }

    public List<ProductionCapacity> getModuleCapacities() {
        return moduleCapacities;
    }

    public boolean hasSpecificActiveCapacity(ProductionType type) {
        for (ProductionCapacity capacity : this.getModuleCapacities()) {
            if (!capacity.isActive()) { continue; }
            if (capacity.getProductionType() == type) {
                return true;
            }
        }
        return false;
    }

    public boolean hasActiveCapacities() {
        for (ProductionCapacity capacity : this.getModuleCapacities()) {
            if (capacity.isActive()) {
                return true;
            }
        }
        return false;
    }

    public boolean hadProducedToday() {
        return producedToday;
    }

    public void setProducedToday(boolean producedToday) {
        this.producedToday = producedToday;
    }

    private void populateCapacities(String hullmodId, FleetMemberAPI fleetMember) {
        ShipAPI.HullSize shipSize = fleetMember.getHullSpec().getHullSize();
        ProductionCapacity firstCapacity = null;
        ProductionCapacity secondCapacity = null;
        this.moduleCapacities = new ArrayList<>();
        switch (hullmodId) {
            case ProductionConstants.REFINING_MODULE: {
                firstCapacity = new ProductionCapacity(this, ProductionType.METALS_PRODUCTION, shipSize);
                secondCapacity = new ProductionCapacity(this, ProductionType.TRANSPLUTONICS_PRODUCTION, shipSize);
                moduleCapacities.add(firstCapacity);
                moduleCapacities.add(secondCapacity);
                break;
            }
            case ProductionConstants.FUEL_PRODUCTION_MODULE: {
                firstCapacity = new ProductionCapacity(this, ProductionType.FUEL_PRODUCTION, shipSize);
                moduleCapacities.add(firstCapacity);
                break;
            }
            case ProductionConstants.HEAVY_INDUSTRY_MODULE: {
                if (this.capacitiesMode == ProductionMode.PRIMARY) {
                    firstCapacity = new ProductionCapacity(this, ProductionType.SUPPLIES_PRODUCTION, shipSize);
                    secondCapacity = new ProductionCapacity(this, ProductionType.MACHINERY_PRODUCTION, shipSize);
                    moduleCapacities.add(firstCapacity);
                    moduleCapacities.add(secondCapacity);
                } else {
                    firstCapacity = new ProductionCapacity(this, ProductionType.HULL_PARTS_PRODUCTION, shipSize);
                    moduleCapacities.add(firstCapacity);
                }
                break;
            }
        }
        FleetwideModuleManager.getInstance().getCapacitiesIndex().add(firstCapacity);
        if (secondCapacity != null) {
            FleetwideModuleManager.getInstance().getCapacitiesIndex().add(secondCapacity);
        }
    }

    // Does not need an iterator because input List is different from operated Set.
    public void clearCapacities() {
        for (ProductionCapacity capacity : this.moduleCapacities) {
            FleetwideModuleManager.getInstance().getCapacitiesIndex().remove(capacity);
        }
        this.moduleCapacities = null;
    }

}
