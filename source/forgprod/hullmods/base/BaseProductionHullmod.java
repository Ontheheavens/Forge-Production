package forgprod.hullmods.base;

import org.lwjgl.input.Keyboard;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import forgprod.abilities.conversion.support.ProductionConstants;
import forgprod.abilities.conversion.support.ProductionType;
import forgprod.abilities.modules.FleetwideModuleManager;
import forgprod.abilities.modules.dataholders.ProductionCapacity;
import forgprod.abilities.modules.dataholders.ProductionModule;
import forgprod.hullmods.checks.HullmodStateChecks;
import forgprod.hullmods.tooltip.TooltipCapacitySection;
import forgprod.hullmods.tooltip.TooltipModuleSection;
import forgprod.settings.SettingsHolder;

import static forgprod.abilities.conversion.support.ProductionConstants.SHIPSIZE_CAPACITY;
import static forgprod.hullmods.checks.HullmodStateChecks.*;

/**
 * @author Ontheheavens
 * @since 05.12.2022
 */

public class BaseProductionHullmod extends BaseHullMod {

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        int shipCapacity = SHIPSIZE_CAPACITY.get(hullSize);
        int crewIncrease = SettingsHolder.CREW_REQ_PER_CAPACITY * shipCapacity;
        int supplyIncrease = SettingsHolder.SUPPLY_REQ_PER_CAPACITY * shipCapacity;
        stats.getMaxCombatReadiness().modifyFlat("Production logistics", -SettingsHolder.MAXIMUM_CR_DECREASE);
        stats.getSuppliesPerMonth().modifyFlat("Production logistics", supplyIncrease);
        stats.getMinCrewMod().modifyFlat("Production logistics", crewIncrease);
    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        int crewPerCapacity = SettingsHolder.CREW_REQ_PER_CAPACITY;
        int supplyPerCapacity = SettingsHolder.SUPPLY_REQ_PER_CAPACITY;
        int cruiserCapacity = SHIPSIZE_CAPACITY.get(ShipAPI.HullSize.CRUISER);
        int capitalCapacity = SHIPSIZE_CAPACITY.get(ShipAPI.HullSize.CAPITAL_SHIP);
        int combatReadinessDecrease = (int) (SettingsHolder.MAXIMUM_CR_DECREASE * 100f);
        if (index == 0) return (crewPerCapacity * cruiserCapacity) + "/" + (crewPerCapacity * capitalCapacity);
        if (index == 1) return (supplyPerCapacity * cruiserCapacity) + "/" + (supplyPerCapacity * capitalCapacity);
        if (index == 2) return combatReadinessDecrease + "%";
        return null;
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, ShipAPI.HullSize hullSize, ShipAPI ship,
                                          float width, boolean isForModSpec) {
        boolean expanded = Keyboard.isKeyDown(Keyboard.getKeyIndex("F1"));
        if (!expanded && ship != null) {
            String key = "F1";
            tooltip.addPara("Press %s for more info.", 10,
                    Misc.getGrayColor(), Misc.getHighlightColor(), key);
        }
        tooltip.addSectionHeading(ProductionConstants.HULLMOD_NAMES.get(this.spec.getId()), Alignment.MID, 8f);
        TooltipModuleSection.addModulePanel(tooltip, ship, this.spec.getId(), width, expanded);
        if (ship != null && isApplicableToShip(ship) && expanded) {
            addCapacitySection(tooltip, ship);
        }
    }

    public void addCapacitySection(TooltipMakerAPI tooltip, ShipAPI ship) {}

    public void addCapacityPanels(TooltipMakerAPI tooltip, ShipAPI ship, boolean hasSecondary,
                                  ProductionType primaryType, ProductionType secondaryType) {
        tooltip.addSectionHeading("Production capacities", Alignment.MID, 8f);
        FleetMemberAPI member = ship.getFleetMember();
        FleetwideModuleManager manager = FleetwideModuleManager.getInstance();
        ProductionCapacity primaryCapacity = manager.getSpecificCapacity(member, 0);
        boolean installed = ship.getVariant().hasHullMod(this.spec.getId());
        TooltipCapacitySection.addCapacityPanel(tooltip, ship, primaryType, primaryCapacity, installed);
        if (hasSecondary && secondaryType != null) {
            ProductionCapacity secondaryCapacity = manager.getSpecificCapacity(member, 1);
            TooltipCapacitySection.addCapacityPanel(tooltip, ship, secondaryType, secondaryCapacity, installed);
        }
    }

    protected boolean addAvailableCapacities(TooltipMakerAPI tooltip, ShipAPI ship) {
        CampaignFleetAPI fleet = HullmodStateChecks.getFleetOfShip(ship);
        if (fleet == null) {
            return false;
        }
        FleetwideModuleManager manager = FleetwideModuleManager.getInstance();
        ProductionModule module = manager.getSpecificModule(ship.getFleetMember());
        if (module == null) {
            return false;
        } else {
            boolean hasSecondary = module.getModuleCapacities().size() > 1;
            ProductionType primary = module.getModuleCapacities().get(0).getProductionType();
            if (primary == null) return false;
            ProductionType secondary = null;
            if (hasSecondary) {
                secondary = module.getModuleCapacities().get(1).getProductionType();
            }
            this.addCapacityPanels(tooltip, ship, hasSecondary, primary, secondary);
            return true;
        }
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return (isForgeHullmodInstallValid(ship) && !hasOtherModules(ship, this.spec.getId()));
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {
        if (ship != null && !isValidHullsize(ship.getHullSpec())) {
            return "Can only be installed on cruiser and capital hulls";
        }
        if (ship != null && isModule(ship)) {
            return "Can not be installed on modules";
        }
        if (hasOtherModules(ship, this.spec.getId())) {
            return "Can only install one Production Forge per hull";
        }
        return null;
    }

}
