package forgprod.abilities.conversion.support.checks;

import com.fs.starfarer.api.fleet.FleetMemberAPI;

import forgprod.abilities.modules.FleetwideModuleManager;
import forgprod.abilities.modules.dataholders.ProductionModule;
import forgprod.settings.SettingsHolder;

public class ModuleConditionChecks {

    public static boolean isOperational(FleetMemberAPI member) {
        return !member.getRepairTracker().isMothballed() &&
               !member.getRepairTracker().isSuspendRepairs() &&
                hasMinimumCR(member);
    }

    public static boolean hasMinimumCR(FleetMemberAPI member) {
        return member.getRepairTracker().getCR() > SettingsHolder.MINIMUM_CR_PERCENT;
    }

    public static boolean hasActiveModule(FleetMemberAPI member) {
        ProductionModule module = FleetwideModuleManager.getInstance().getModuleIndex().get(member);
        if (module != null) {
            return module.hasActiveCapacities();
        } else return false;
    }

}
