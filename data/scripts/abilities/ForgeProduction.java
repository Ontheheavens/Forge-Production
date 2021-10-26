package data.scripts.abilities;

import java.awt.Color;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;

import data.scripts.campaign.ForgeHullmodsListener;
import org.lazywizard.lazylib.MathUtils;

public class ForgeProduction extends BaseToggleAbility {

    // Here: Declared constants for production

    private static boolean useAllowedByListener = false;

    private final boolean showNotification = Global.getSettings().getBoolean("forge_show_notification");

    private final float heavyMachineryOreCost = Global.getSettings().getFloat("forge_heavy_machinery_to_allow_ore_refining");
    private final float heavyMachineryTransplutonicOreCost = Global.getSettings().getFloat("forge_heavy_machinery_to_allow_transplutonic_ore_refining");

    private final float oreCost = Global.getSettings().getFloat("forge_ore_to_refine");
    private final float metalProduced = Global.getSettings().getFloat("forge_metal_produced");

    private final float transplutonicOreCost = Global.getSettings().getFloat("forge_transplutonic_ore_to_refine");
    private final float transplutonicsProduced = Global.getSettings().getFloat("forge_transplutonics_produced");

    private final float combatReadinessCost = Global.getSettings().getFloat("forge_combat_readiness_decay_when_producing");

    private final IntervalUtil productionInterval = new IntervalUtil(1f,1f);

    private static final Color METAL_COLOR = new Color(205, 205, 205, 255);
    private static final Color TRANSPLUTONIC_COLOR = new Color(120, 120, 200, 255);
    private static final Color WARNING_COLOR = new Color(255, 100, 100, 255);

    // Here: Temporary variables

    public static float forgingEquipmentQuality = 1f; // Base 1f = 100% (lower is better), intended to be improved by Nanoforges

    public static float totalDailyOreExpenditure = 0f;
    public static float totalDailyMetalProduction = 0f;

    public static float totalDailyTransplutonicOreExpenditure = 0f;
    public static float totalDailyTransplutonicsProduction = 0f;

    public static float totalDailyMachineBreakage = 0f;

    public static boolean hasBreakdownHappened = false;
    public static boolean oreWasRefined = false;
    public static boolean transplutonicsWereRefined = false;

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

            float transplutonicOreAvailable = fleet.getCargo().getCommodityQuantity(Commodities.RARE_ORE);

            if (transplutonicOreAvailable > transplutonicOreCost) {
                refineTransplutonicOre();
            }

            if (oreWasRefined || transplutonicsWereRefined) {

                applyRefiningCRMalus(fleet);

            }

            if (showNotification && ((oreWasRefined || transplutonicsWereRefined))) {

                final String ore = String.valueOf((int) (totalDailyOreExpenditure));
                final String metal = String.valueOf((int) (totalDailyMetalProduction));

                final String transplutonicOre = String.valueOf((int) (totalDailyTransplutonicOreExpenditure));
                final String transplutonics = String.valueOf((int) (totalDailyTransplutonicsProduction));

                final boolean breakdownReport = hasBreakdownHappened;
                final String machineryBreakdowns = String.valueOf((int) (totalDailyMachineBreakage));

                Global.getSector().getIntelManager().addIntel(new BaseIntelPlugin() {

                    @Override public String getIcon() {
                        return "forge_production_report";
                    }

                    @Override
                    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {

                        Color titleColor = getTitleColor(mode);

                        String metalProductionLine = ore + " units of ore were refined into " + metal + " units of metal";
                        String transplutonicsProductionLine = transplutonicOre + " units of transplutonic ore were refined into " + transplutonics + " units of transplutonics";
                        String machineryBreakdownsLine = machineryBreakdowns + " heavy machinery broke down";

                        Color highlightColor = Misc.getHighlightColor();

                        info.addPara("Forge Production - Report", titleColor, 0f);

                        if (oreWasRefined) {
                            info.addPara(metalProductionLine, 2, METAL_COLOR, highlightColor, ore, metal);
                        }

                        if (transplutonicsWereRefined) {
                            info.addPara(transplutonicsProductionLine, 2, TRANSPLUTONIC_COLOR, highlightColor, transplutonicOre, transplutonics);
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

                totalDailyTransplutonicOreExpenditure = 0f;
                totalDailyTransplutonicsProduction = 0f;

                totalDailyMachineBreakage = 0f;

                hasBreakdownHappened = false;
                oreWasRefined = false;
                transplutonicsWereRefined = false;

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

        tooltip.addPara("The following ships in your fleet have forging capabilities:", pad);

        List<FleetMemberAPI> forgeShips = getForgeShipsInFleet();

        float currPad = 3.0F;
        String indent = "    ";
        for (FleetMemberAPI member : forgeShips) {

            LabelAPI label = tooltip.addPara(indent + " %s",
                    currPad,
                    Misc.getHighlightColor(),
                    member.getShipName() + " [" + member.getHullSpec().getHullNameWithDashClass() + "]" + " - " + getForgeHullmodOfForgeShip(member));

            currPad = 0.0F;
        }

        addIncompatibleToTooltip(tooltip, expanded);

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

    protected List<FleetMemberAPI> getForgeShipsInFleet() {

        List<String> forgeHullMods = new ArrayList<>();

        {
            forgeHullMods.add("forge_refinery_module");
        }

        List<FleetMemberAPI> membersWithHullMod = new ArrayList<>();

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        for (FleetMemberAPI forgeShip : playerFleet.getFleetData().getMembersListCopy())
            if (!Collections.disjoint(forgeShip.getVariant().getHullMods(), forgeHullMods))
                membersWithHullMod.add(forgeShip);

        return membersWithHullMod;
    }

    protected String getForgeHullmodOfForgeShip (FleetMemberAPI member) {

         if (member.getVariant().hasHullMod("forge_refinery_module")) {
             return "Forge Refinery Module";
         }

         // Expand later

        return null;
    }

    // Here: Conversion methods

    private void refineOre() {

        CampaignFleetAPI fleet = getFleet();

        float heavyMachineryAvailable = fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY);
        if (heavyMachineryAvailable <= heavyMachineryOreCost) return;

        float heavyMachineryInRefining = heavyMachineryAvailable/ heavyMachineryOreCost;
        float oreAvailable = fleet.getCargo().getCommodityQuantity(Commodities.ORE);

        float RefiningCapacity = getRefiningCapacity(fleet);

        if (oreAvailable > oreCost) {

            float oreInRefining = oreAvailable/oreCost;

            int refiningCycles = (int) (Math.min(RefiningCapacity, Math.min(heavyMachineryInRefining,oreInRefining)));

            // Math.random returns number between 0 and 1. If returned is !< breakdownChance, expression is false and breakdown won't happen.
            // So, 20% chance of breakdown.

            float baseBreakdownChance = 0.1f;
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

    private void refineTransplutonicOre() {

        CampaignFleetAPI fleet = getFleet();

        float heavyMachineryAvailable = fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY);
        if (heavyMachineryAvailable <= heavyMachineryTransplutonicOreCost) return;

        float heavyMachineryInRefining = heavyMachineryAvailable/ heavyMachineryTransplutonicOreCost;
        float transplutonicOreAvailable = fleet.getCargo().getCommodityQuantity(Commodities.RARE_ORE);

        float RefiningCapacity = getRefiningCapacity(fleet);

        if (transplutonicOreAvailable > transplutonicOreCost) {

            float transplutonicOreInRefining = transplutonicOreAvailable / transplutonicOreCost;

            int refiningCycles = (int) (Math.min(RefiningCapacity, Math.min(heavyMachineryInRefining, transplutonicOreInRefining)));

            // Math.random returns number between 0 and 1. If returned is !< breakdownChance, expression is false and breakdown won't happen.
            // So, 20% chance of breakdown.

            float baseBreakdownChance = 0.1f;
            boolean willHeavyMachineryBreakdown = (Math.random() < (baseBreakdownChance * forgingEquipmentQuality));

            int randomHeavyMachineryBreakdowns = MathUtils.getRandomNumberInRange(0, Math.round(refiningCycles * forgingEquipmentQuality));

            float dailyTransplutonicOreSpent = transplutonicOreCost * refiningCycles;
            float dailyTransplutonicsProduced = transplutonicsProduced * refiningCycles;
            float dailyHeavyMachineryBroken = heavyMachineryInRefining * randomHeavyMachineryBreakdowns;

            fleet.getCargo().removeCommodity(Commodities.RARE_ORE, dailyTransplutonicOreSpent);
            fleet.getCargo().addCommodity(Commodities.RARE_METALS, dailyTransplutonicsProduced);
            if(willHeavyMachineryBreakdown)
                fleet.getCargo().removeCommodity(Commodities.HEAVY_MACHINERY, dailyHeavyMachineryBroken);

            float baseCommodityQuantity = 0f;

            totalDailyTransplutonicOreExpenditure = baseCommodityQuantity + dailyTransplutonicOreSpent;
            totalDailyTransplutonicsProduction = baseCommodityQuantity + dailyTransplutonicsProduced;
            totalDailyMachineBreakage = baseCommodityQuantity + dailyHeavyMachineryBroken;

            hasBreakdownHappened = willHeavyMachineryBreakdown;
            transplutonicsWereRefined = true;

        }
    }

}