package forgprod.abilities.modules;

import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import forgprod.abilities.conversion.support.ProductionConstants;
import forgprod.abilities.modules.dataholders.ProductionCapacity;
import forgprod.abilities.modules.dataholders.ProductionModule;

/**
 * Registry of all modules and production capacities possessed by the fleet.
 * To be used as an intermediary in production logic and by the control panel for purposes of mass toggling.
 * Singleton-pattern class, persists between sessions, holds info about production configurations.
 * <p>
 *
 * @author Ontheheavens
 * @since 30.11.2022
 */

public class FleetwideModuleManager {

    private static FleetwideModuleManager managerInstance;
    private Set<ProductionCapacity> capacitiesIndex;
    private Map<FleetMemberAPI, ProductionModule> moduleIndex;
    private float accumulatedHullParts;
    private ShipVariantAPI designatedVariant;

    private FleetwideModuleManager() {
        this.moduleIndex = new HashMap<>();
        this.capacitiesIndex = new HashSet<>();
        this.accumulatedHullParts = 0f;
        this.ensureDefaultVariant();
    }

    public static FleetwideModuleManager getInstance() {
        managerInstance = (FleetwideModuleManager) Global.getSector().getPersistentData().get("forgprod_index");
        if (managerInstance == null) {
            managerInstance = new FleetwideModuleManager();
            Global.getSector().getPersistentData().put("forgprod_index", managerInstance);
        }
        if (managerInstance.designatedVariant == null) {
            managerInstance.ensureDefaultVariant();
        }
        return managerInstance;
    }

    private void ensureDefaultVariant() {
        this.designatedVariant = Global.getSettings().getVariant("picket_Assault");
    }

    public Set<ProductionCapacity> getCapacitiesIndex() {
        return this.capacitiesIndex;
    }

    public Map<FleetMemberAPI, ProductionModule> getModuleIndex() {
        return moduleIndex;
    }

    public float getAccumulatedHullParts() {
        return accumulatedHullParts;
    }

    public void updateAccumulatedHullParts(float value) {
        this.accumulatedHullParts = accumulatedHullParts + value;
    }

    public ShipVariantAPI getDesignatedVariant() {
        return designatedVariant;
    }

    public void setDesignatedVariant(ShipVariantAPI designatedVariant) {
        this.designatedVariant = designatedVariant;
    }

    public void refreshIndexes(CampaignFleetAPI fleet) {
        if (fleet == null) return;
        this.cullCapacitiesIndex(fleet);
        this.cullModuleIndex(fleet);
        this.addModulesToIndex(fleet);
        Global.getSector().getPersistentData().put("forgprod_index", managerInstance);
    }

    private void addModulesToIndex(CampaignFleetAPI fleet) {
        List<String> hullmods = new ArrayList<>(ProductionConstants.ALL_HULLMODS);
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (!Collections.disjoint(member.getVariant().getHullMods(), hullmods)) {
                if (this.moduleIndex.get(member) == null) {
                    ProductionModule module = new ProductionModule(member, getInstalledHullmod(member));
                    moduleIndex.put(member, module);
                }
            }
        }
    }

    private void cullModuleIndex(CampaignFleetAPI fleet) {
        Iterator<FleetMemberAPI> members = moduleIndex.keySet().iterator();
        while (members.hasNext()) {
            FleetMemberAPI member = members.next();
            boolean notPresent = (member.getFleetData() == null)
                    || (member.getFleetData() != fleet.getFleetData());
            boolean noHullmod = !hasInstalledHullmod(member);
            if (notPresent || noHullmod) {
                ProductionModule module = moduleIndex.get(member);
                module.clearCapacities();
                members.remove();
            }
        }
    }

    private void cullCapacitiesIndex(CampaignFleetAPI fleet) {
        Iterator<ProductionCapacity> capacities = capacitiesIndex.iterator();
        while (capacities.hasNext()) {
            ProductionCapacity capacity = capacities.next();
            FleetMemberAPI capacityHolder = capacity.getParentModule().getParentFleetMember();
            boolean notPresent = (capacityHolder.getFleetData() == null)
                    || (capacityHolder.getFleetData() != fleet.getFleetData());
            boolean noHullmod = !hasInstalledHullmod(capacityHolder);
            if (capacity.getParentModule() == null || noHullmod || notPresent) {
                capacities.remove();
            }
        }
    }

    public ProductionModule getSpecificModule(FleetMemberAPI member) {
        return this.moduleIndex.get(member);
    }

    /**
     * @param index 0 for primary, 1 for secondary
     * @return can be null.
     */
    public ProductionCapacity getSpecificCapacity(FleetMemberAPI member, int index) {
        if (member == null) { return null; }
        ProductionModule module = getSpecificModule(member);
        if (module == null) { return null; }
        if ((index + 1) >  module.getModuleCapacities().size()) {
            return null;
        }
        return module.getModuleCapacities().get(index);
    }

    private String getInstalledHullmod(FleetMemberAPI member) {
        List<String> hullmods = new ArrayList<>(ProductionConstants.ALL_HULLMODS);
        for (String checkedHullmodId : member.getVariant().getHullMods()) {
            if (hullmods.contains(checkedHullmodId)) {
                return checkedHullmodId;
            }
        }
        return null;
    }

    private boolean hasInstalledHullmod(FleetMemberAPI member) {
        return member.getVariant().hasHullMod(moduleIndex.get(member).getHullmodId());
    }

}
