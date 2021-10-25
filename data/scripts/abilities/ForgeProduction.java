package data.scripts.abilities;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import org.lazywizard.lazylib.MathUtils;

public class ForgeProduction extends BaseToggleAbility {

    // Here: Declared constants for production

    private static boolean useAllowedByListener = false;

    private final boolean showNotification = Global.getSettings().getBoolean("forge_show_notification");

    private final float heavyMachineryCost = Global.getSettings().getFloat("forge_heavy_machinery_to_allow_refining");

    private final float oreCost = Global.getSettings().getFloat("forge_ore_to_refine");
    private final float metalProduced = Global.getSettings().getFloat("forge_metal_produced");

    private final float transplutonicOreCost = Global.getSettings().getFloat("forge_transplutonic_ore_to_refine");
    private final float transplutonicsProduced = Global.getSettings().getFloat("forge_transplutonics_produced");

    private final float combatReadinessCost = Global.getSettings().getFloat("forge_combat_readiness_decay_when_producing");

    private final IntervalUtil productionInterval = new IntervalUtil(0.9f,1.1f);

    private static final Color METAL_COLOR = new Color(205, 205, 205, 255);
    private static final Color WARNING_COLOR = new Color(255, 100, 100, 255);

    // Here: Temporary variables

    public static float forgingEquipmentQuality = 1f; // Base 1f = 100% (lower is better), intended to be improved by Nanoforges

    public static float totalDailyOreExpenditure = 0f;
    public static float totalDailyMetalProduction = 0f;
    public static float totalDailyMachineBreakage = 0f;

    public static boolean hasBreakdownHappened = false;
    public static boolean oreWasRefined = false;

    // Here: Ship forging capacity factors

    private static final Map<ShipAPI.HullSize, Integer> shipSizeEffect = new HashMap<>();
    static {

        shipSizeEffect.put(ShipAPI.HullSize.DEFAULT, 0);
        shipSizeEffect.put(ShipAPI.HullSize.FIGHTER, 0);
        shipSizeEffect.put(ShipAPI.HullSize.FRIGATE, 0);
        shipSizeEffect.put(ShipAPI.HullSize.DESTROYER, 2);
        shipSizeEffect.put(ShipAPI.HullSize.CRUISER, 4);
        shipSizeEffect.put(ShipAPI.HullSize.CAPITAL_SHIP, 8);

    }

    // Here: Inherited methods

    protected void activateImpl() { }

    protected void applyEffect(float amount, float level) {

        CampaignFleetAPI fleet = getFleet();

        if (fleet == null) return;
        if (!isActive()) return;

        float days = Global.getSector().getClock().convertToDays(amount);

        productionInterval.advance(days);

        if (productionInterval.intervalElapsed()) {

            float oreAvailable = fleet.getCargo().getCommodityQuantity(Commodities.ORE);

            if (oreAvailable > oreCost) {

                refineOre();

            }

            if (oreWasRefined) {

                applyRefiningCRMalus(fleet);

            }

            if (showNotification) {

                final String ores = String.valueOf((int) (totalDailyOreExpenditure));
                final String metals = String.valueOf((int) (totalDailyMetalProduction));

                final boolean breakdownReport = hasBreakdownHappened;
                final String machineryBreakdowns = String.valueOf((int) (totalDailyMachineBreakage));

                Global.getSector().getIntelManager().addIntel(new BaseIntelPlugin() {

                    // Add booleans for different conversion types. If no types were performed, no report.

                    @Override
                    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {

                        String metalProductionLine = ores + " ores were refined into " + metals + " metals";
                        String machineryBreakdownsLine = machineryBreakdowns + " heavy machinery broke down";

                        Color highlightColor = Misc.getHighlightColor();

                        if (oreWasRefined) {
                            info.addPara(metalProductionLine, 2, METAL_COLOR, highlightColor, ores, metals);
                        }

                        if (breakdownReport) {
                            info.addPara(machineryBreakdownsLine, 2, WARNING_COLOR, highlightColor, machineryBreakdowns);
                        }

                        // Jaghaimo: Die, you served your purpose
                        Global.getSector().getIntelManager().removeIntel(this);
                    }
                }
                );

                totalDailyOreExpenditure = 0f;
                totalDailyMetalProduction = 0f;
                totalDailyMachineBreakage = 0f;

                hasBreakdownHappened = false;
                oreWasRefined = false;

            }
        }
    }

    protected void deactivateImpl() { cleanupImpl(); }

    protected void cleanupImpl() { }

    @Override
    public boolean isUsable() {
        if (!isActivateCooldown &&
                getProgressFraction() > 0 && getProgressFraction() < 1 &&
                getDeactivationDays() > 0) return false;

        if (!useAllowedByListener)
            return false;

        return super.isUsable();
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {

        Color highlight = Misc.getHighlightColor();

        String status = " (off)";
        if (turnedOn) {
            status = " (on)";
        }

        LabelAPI title = tooltip.addTitle(spec.getName() + status);
        title.highlightLast(status);
        title.setHighlightColor(highlight);

        float pad = 10f;
        tooltip.addPara("Control forge production of your fleet.", pad);

    }

    //Here: Custom methods

    public static void setUseAllowedByListener (boolean useAllowed) {
        useAllowedByListener = useAllowed;
    }

    public static void setNanoforgeQuality (float quality) {
        forgingEquipmentQuality = quality;
    }

    private int getRefiningCapacity(CampaignFleetAPI fleet) {
        int totalRefiningCapacity = 0;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if ((member.getRepairTracker().isSuspendRepairs() || member.getRepairTracker().isMothballed()))
                continue;
            if (member.getVariant().hasHullMod("forge_refinery_module"))
                totalRefiningCapacity += Math.round(shipSizeEffect.get(member.getVariant().getHullSize()) * (member.getRepairTracker().getCR() / 0.7f));
        }
        return totalRefiningCapacity;
    }

    private void applyRefiningCRMalus(CampaignFleetAPI fleet) {
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy()) {
            if ((member.getRepairTracker().isSuspendRepairs() || member.getRepairTracker().isMothballed()))
                continue;
            if (member.getVariant().hasHullMod("forge_refinery_module"))
                member.getRepairTracker().applyCREvent(-(combatReadinessCost * (member.getRepairTracker().getCR() / 0.7f)), "Produced goods");
        }
    }

    private void refineOre() {

        CampaignFleetAPI fleet = getFleet();

        float heavyMachineryAvailable = fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY);
        if (heavyMachineryAvailable <= heavyMachineryCost) return;

        float heavyMachineryInRefining = heavyMachineryAvailable/heavyMachineryCost;
        float oreAvailable = fleet.getCargo().getCommodityQuantity(Commodities.ORE);

        float RefiningCapacity = getRefiningCapacity(fleet);

        if (oreAvailable > oreCost) {

            float oreInRefining = oreAvailable/oreCost;

            int refiningCycles = (int) (Math.min(RefiningCapacity, Math.min(heavyMachineryInRefining,oreInRefining)));

            // Math.random returns number between 0 and 1. If returned is !< breakdownChance, expression is false and breakdown won't happen.
            // So, 20% chance of breakdown.

            float baseBreakdownChance = 0.2f;
            boolean willHeavyMachineryBreakdown = (Math.random()<(baseBreakdownChance*forgingEquipmentQuality));

            int randomHeavyMachineryBreakdowns = MathUtils.getRandomNumberInRange(0, Math.round(refiningCycles*forgingEquipmentQuality));

            float dailyOreSpent = oreCost * refiningCycles;
            float dailyMetalProduced = metalProduced * refiningCycles;
            float dailyHeavyMachineryBroken = heavyMachineryInRefining * randomHeavyMachineryBreakdowns;

            fleet.getCargo().removeCommodity(Commodities.ORE, dailyOreSpent);
            fleet.getCargo().addCommodity(Commodities.METALS, dailyMetalProduced);
            if(willHeavyMachineryBreakdown)
                fleet.getCargo().removeCommodity(Commodities.HEAVY_MACHINERY, dailyHeavyMachineryBroken);

            float baseCommodityQuantity = 0f;

            totalDailyOreExpenditure = baseCommodityQuantity + dailyOreSpent;
            totalDailyMetalProduction = baseCommodityQuantity + dailyMetalProduced;
            totalDailyMachineBreakage = baseCommodityQuantity + dailyHeavyMachineryBroken;

            hasBreakdownHappened = willHeavyMachineryBreakdown;
            oreWasRefined = true;

        }

    }

}