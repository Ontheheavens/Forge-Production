package forgprod.abilities.conversion.support.checks;

import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;

import forgprod.abilities.conversion.support.ProductionConstants;
import forgprod.abilities.conversion.support.ProductionType;
import forgprod.abilities.modules.FleetwideModuleManager;
import forgprod.abilities.modules.dataholders.ProductionCapacity;
import forgprod.abilities.modules.dataholders.ProductionModule;
import forgprod.settings.SettingsHolder;

import static forgprod.abilities.conversion.support.checks.ItemBonusesChecks.getNanoforgeBreakdownDecrease;

public class FleetwideProductionChecks {

    public static int getFleetCapacityForType(CampaignFleetAPI fleet, ProductionType type) {
        int fleetCapacity = 0;
        Set<ProductionCapacity> fleetCapacities = FleetwideModuleManager.getInstance().getCapacitiesIndex();
        if (fleetCapacities.isEmpty()) {
            return 0;
        }
        for (ProductionCapacity capacity : fleetCapacities) {
            if (capacity.getProductionType() == type) {
                if (!capacity.isActive()) { continue; }
                if (capacity.getCurrentThroughput() <= 0f) { continue; }
                fleetCapacity += capacity.getModifiedCapacity(fleet);
            }
        }
        return fleetCapacity;
    }

    public static int getTotalModuleVolume() {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        int result = 0;
        List<String> hullmods = new ArrayList<>(ProductionConstants.ALL_HULLMODS);
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (!Collections.disjoint(member.getVariant().getHullMods(), hullmods)) {
                ShipAPI.HullSize memberSize = member.getHullSpec().getHullSize();
                    int moduleSize = ProductionConstants.SHIPSIZE_CAPACITY.get(memberSize);
                    result += moduleSize;
            }
        }
        return result;
    }

    public static int getTotalModuleCount() {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        int result = 0;
        List<String> hullmods = new ArrayList<>(ProductionConstants.ALL_HULLMODS);
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (!Collections.disjoint(member.getVariant().getHullMods(), hullmods)) {
                result += 1;
            }
        }
        return result;
    }

    public static float getFleetMachineryRequirement() {
        float totalRequirement = 0;
        Set<ProductionCapacity> fleetCapacities = FleetwideModuleManager.getInstance().getCapacitiesIndex();
        for (ProductionCapacity capacity : fleetCapacities) {
            if (!capacity.isActive()) { continue; }
            totalRequirement += capacity.getMachineryUse();
        }
        return totalRequirement;
    }

    public static float getMachineryAvailability(CampaignFleetAPI fleet) {
        int machineryInCargo = (int) Math.floor(fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY));
        float machineryRequiredTotal = getFleetMachineryRequirement();
        float machineryAvailability = machineryInCargo / machineryRequiredTotal;
        if (machineryAvailability > 1f) { machineryAvailability = 1f; }
        return machineryAvailability;
    }

    public static float getBreakdownChance(CampaignFleetAPI fleet) {
        return SettingsHolder.BASE_BREAKDOWN_CHANCE * getNanoforgeBreakdownDecrease(fleet);
    }

    // Final accident chance is (getBreakdownChance * getFatalAccidentChance), or 0.045 by default.
    // This is because accidents chances are only calculated when breakdown happens.
    public static float getFatalAccidentChance(CampaignFleetAPI fleet) {
        return getBreakdownChance(fleet) / 2;
    }

    public static float getBaseBreakdownSeverity(CampaignFleetAPI fleet) {
        return SettingsHolder.BREAKDOWN_SEVERITY * getNanoforgeBreakdownDecrease(fleet);
    }

    private static float getBreakdownSeverityRange(float baseSeverity) {
        return baseSeverity / 2;
    }

    public static float getMinimalBreakdownSeverity(CampaignFleetAPI fleet) {
        float baseSeverity = getBaseBreakdownSeverity(fleet);
        float severityRange = getBreakdownSeverityRange(baseSeverity);
        return baseSeverity - severityRange;
    }

    public static float getMaximalBreakdownSeverity(CampaignFleetAPI fleet) {
        float baseSeverity = getBaseBreakdownSeverity(fleet);
        float severityRange = getBreakdownSeverityRange(baseSeverity);
        return baseSeverity + severityRange;
    }

    public static boolean hasMinimumMachinery(CampaignFleetAPI fleet) {
        return getMachineryAvailability(fleet) >= SettingsHolder.MINIMUM_MACHINERY_PERCENT;
    }

    public static boolean hasInstalledModules(CampaignFleetAPI fleet) {
        Map<FleetMemberAPI, ProductionModule> moduleIndex = FleetwideModuleManager.getInstance().getModuleIndex();
        if (moduleIndex.isEmpty()) {
            return false;
        }
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if (moduleIndex.get(member) != null) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasActiveModules() {
        Map<FleetMemberAPI, ProductionModule> moduleIndex = FleetwideModuleManager.getInstance().getModuleIndex();
        if (moduleIndex.isEmpty()) {
            return false;
        }
        for (ProductionModule module : moduleIndex.values()) {
            if (module.hasActiveCapacities()) {
                return true;
            }
        }
        return false;
    }

}
