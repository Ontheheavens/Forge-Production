package forgprod.abilities;

import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import forgprod.abilities.conversion.ProductionEventManager;
import forgprod.abilities.conversion.support.checks.FleetwideProductionChecks;
import forgprod.abilities.effects.AbilityEffects;
import forgprod.abilities.interaction.wrappers.ControlInteractionPlugin;
import forgprod.abilities.modules.FleetwideModuleManager;
import forgprod.abilities.tooltip.TooltipAssembly;
import forgprod.settings.SettingsHolder;

@SuppressWarnings("unused")
public class ForgeProductionAbility extends BaseToggleAbility {

    private ProductionEventManager productionManager;

    protected void activateImpl() {
        productionManager = ProductionEventManager.getInstance();
        FleetwideModuleManager.getInstance().refreshIndexes(getFleet());
        interruptIncompatible();
    }

    @Override
    protected String getActivationText() {
        return "Commencing forge production";
    }

    protected void applyEffect(float amount, float level) {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;
        if (!isActive()) return;
        if (!checkUsableCapacities()) {
            deactivate();
            return;
        }
        disableIncompatible();
        if (SettingsHolder.ENABLE_DETECT_AT_RANGE_PENALTY) {
            fleet.getStats().getDetectedRangeMod().modifyPercent(getModId(), SettingsHolder.SENSOR_PROFILE_INCREASE, "Forge production");
        }
        if (SettingsHolder.ENABLE_SLOW_MOVE_PENALTY) {
            fleet.goSlowOneFrame();
        }
        AbilityEffects.shiftFleetContrails(fleet, getModId(), level);
        productionManager.advance(fleet, amount);
    }

    @Override
    protected void deactivateImpl() {
        cleanupImpl();
    }

    @Override
    protected void cleanupImpl() {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;
        if (SettingsHolder.ENABLE_DETECT_AT_RANGE_PENALTY) {
            fleet.getStats().getDetectedRangeMod().unmodify(getModId());
        }
        productionManager.clearTracker();
        productionManager = null;
    }

    private boolean checkUsableCapacities() {
        FleetwideModuleManager.getInstance().refreshIndexes(getFleet());
        boolean hasShips = FleetwideProductionChecks.hasActiveModules();
        boolean hasMachinery = FleetwideProductionChecks.hasMinimumMachinery(this.getFleet());
        return hasShips && hasMachinery;
    }

    public boolean checkUsableAbilities() {
        for (AbilityPlugin other : this.getFleet().getAbilities().values()) {
            if (other == this) continue;
            if (!isCompatible(other) && other.isActiveOrInProgress()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isUsable() {
        if (Keyboard.isKeyDown(Keyboard.getKeyIndex(SettingsHolder.INTERACTION_CALL_HOTKEY))) {
            return true;
        }
        if (!isActivateCooldown &&
                getProgressFraction() > 0 && getProgressFraction() < 1 &&
                getDeactivationDays() > 0) return false;

        if (!checkUsableCapacities() || !checkUsableAbilities())
            return false;

        return super.isUsable();
    }

    @Override
    public void pressButton() {
        if (Keyboard.isKeyDown(Keyboard.getKeyIndex(SettingsHolder.INTERACTION_CALL_HOTKEY))) {
            FleetwideModuleManager.getInstance().refreshIndexes(this.getFleet());
            Global.getSector().getCampaignUI().showInteractionDialog(new ControlInteractionPlugin(), null);
            return;
        }
        super.pressButton();
    }

    @Override
    public boolean showActiveIndicator() { return isActive(); }

    @Override
    public boolean hasTooltip() { return true; }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        TooltipAssembly.assembleTooltip(this.getFleet(), tooltip, this.getSpec().getName(),
                this.isActive(), this.turnedOn, expanded, this);
        addIncompatibleToTooltip(tooltip, expanded);
        tooltip.addPara("Ctrl+left-click to open control panel", Misc.getGrayColor(), 4f);
    }

    @Override
    protected void addIncompatibleToTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        addIncompatibleToTooltip(tooltip, "Incompatible with the following abilities:",
                "Expand tooltip to view production and conflicting abilities",
                expanded);
    }

    @Override
    public boolean isCompatible(AbilityPlugin other) {
        boolean burnIncompat = SettingsHolder.ENABLE_BURN_ABILITIES_INCOMPATIBILITY;
        boolean darkIncompat = SettingsHolder.ENABLE_GO_DARK_INCOMPATIBILITY;
        if (burnIncompat && darkIncompat) {
            return !other.getId().equals(Abilities.SUSTAINED_BURN) &&
                   !other.getId().equals(Abilities.EMERGENCY_BURN) &&
                   !other.getId().equals(Abilities.GO_DARK);
        }
        if (burnIncompat) {
            return !other.getId().equals(Abilities.SUSTAINED_BURN) && !other.getId().equals(Abilities.EMERGENCY_BURN);
        }
        if (darkIncompat) {
            return !other.getId().equals(Abilities.GO_DARK);
        }
        return true;
    }

    @Override
    public List<AbilityPlugin> getInterruptedList() {
        List<AbilityPlugin> result = new ArrayList<>();
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return result;
        for (AbilityPlugin curr : fleet.getAbilities().values()) {
            if (curr == this) continue;
            if (!isCompatible(curr)) {
                result.add(curr);
            }
        }
        Collections.sort(result, new Comparator<AbilityPlugin>() {
            public int compare(AbilityPlugin o1, AbilityPlugin o2) {
                return o1.getSpec().getSortOrder() - o2.getSpec().getSortOrder();
            }
        });
        return result;
    }

}