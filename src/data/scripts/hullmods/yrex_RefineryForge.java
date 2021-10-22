package data.scripts.hullmods;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.util.Misc;


public class yrex_RefineryForge extends BaseHullMod {

	
    private static final Set<String> OTHER_FORGE_HULLMODS = new HashSet<>();
    static
    {
        // These hullmods will automatically be removed
        // This prevents unexplained hullmod blocking
    	OTHER_FORGE_HULLMODS.add("yrex_FuelForge");
    	OTHER_FORGE_HULLMODS.add("yrex_SuppliesForge");
    	OTHER_FORGE_HULLMODS.add("yrex_LobsterPot");
    	OTHER_FORGE_HULLMODS.add("yrxp_ShipMall");
    }

    private static final Map<ShipAPI.HullSize, Integer> shipSizeEffect = new HashMap<>();
    static {
    	shipSizeEffect.put(ShipAPI.HullSize.DEFAULT, 1);
    	shipSizeEffect.put(ShipAPI.HullSize.CAPITAL_SHIP, 8);
        shipSizeEffect.put(ShipAPI.HullSize.CRUISER, 4);
        shipSizeEffect.put(ShipAPI.HullSize.DESTROYER, 2);
        shipSizeEffect.put(ShipAPI.HullSize.FRIGATE, 1);    
    }
    

	public static String DATA_KEY = "yrex_refinery_forge_ship_key";
	public static class RefineryForgeData {
		Map<FleetMemberAPI, Integer> counter = new LinkedHashMap<FleetMemberAPI, Integer>();
	}

	public static float totalDailyOreExpenditure = 0f;
	public static float totalDailyMetalProduction = 0f;
	public static float totalDailyMachineBreakage = 0f;

	private static final Color TEXT_COLOR = new Color(255, 255, 255, 80);
	private static final Color METAL_COLOR = new Color(205, 205, 205, 255);
	private static final Color TRANSPLUTONIC_COLOR = new Color(120, 120, 200, 255);
	private static final Color WARNING_COLOR = new Color(255, 100, 100, 255);
	
	
	
	private static final Vector2f ZERO = new Vector2f();
	private static final float REPAIR_RATE_BONUS = 50.0f;
	
    private final float metalCost = Global.getSettings().getFloat("yrex_forge_ore_to_metal_cost");
    private final float transplutonicCost = Global.getSettings().getFloat("yrex_forge_ore_to_transplutonic_cost");
    private final float heavyMachineryCost = Global.getSettings().getFloat("yrex_forge_heavy_machinery_to_refine_cost");
    

    private final float metalProduced = Global.getSettings().getFloat("yrex_forge_ore_to_metal_produced");
    private final float transplutonicProduced = Global.getSettings().getFloat("yrex_forge_ore_to_transplutonic_produced");
    private final int maxRefineTimes = Global.getSettings().getInt("yrex_forge_max_ore_refines_per_day");

    private final float crCost = Global.getSettings().getFloat("yrex_forge_refine_cr_cost_per_day");

    private final boolean playSFX = Global.getSettings().getBoolean("yrex_forge_play_sfx");
    private final boolean showNotification = Global.getSettings().getBoolean("yrex_forge_show_notification");

	private CombatEngineAPI engine;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getRepairRatePercentPerDay().modifyMult(id, 1f + REPAIR_RATE_BONUS * 0.01f);

	}
    
    public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + REPAIR_RATE_BONUS + "%";

		if (index == 1) return
			maxRefineTimes*shipSizeEffect.get(ShipAPI.HullSize.FRIGATE) + "/" +
			maxRefineTimes*shipSizeEffect.get(ShipAPI.HullSize.DESTROYER) + "/" +
			maxRefineTimes*shipSizeEffect.get(ShipAPI.HullSize.CRUISER) + "/" +
			maxRefineTimes*shipSizeEffect.get(ShipAPI.HullSize.CAPITAL_SHIP);
		if (index == 2) return "" + metalCost;
		if (index == 3) return "" + heavyMachineryCost;
		if (index == 4) return "" + metalProduced;
		if (index == 5) return "" + transplutonicCost;
		if (index == 6) return "" + heavyMachineryCost;
		if (index == 7) return "" + transplutonicProduced;
		return null;
    }
	
    public void advanceInCampaign(FleetMemberAPI member, float amount) {

		if (member.getFleetData() == null) return;
		if (member.getFleetData().getFleet() == null) return;
		if (!member.getFleetData().getFleet().isPlayerFleet()) return;

		if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }
		
		CampaignFleetAPI fleet = member.getFleetData().getFleet();
		
        if(hasDoneToday(member)) {
        	return;
        }

		//On next day
        
        float machines = fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY);
        if(machines <= heavyMachineryCost) {
            return;
        } 

        int refineTimes;
        float machineRefineTimes = machines/heavyMachineryCost;
        
        boolean hasProducedSomething = false;
        
        float metal_ore = fleet.getCargo().getCommodityQuantity(Commodities.ORE);
        if(metal_ore > metalCost) {

            float metalRefineTimes = metal_ore/metalCost; 
        	
            hasProducedSomething = true;
            
            refineTimes = (int) Math.ceil((Math.min(maxRefineTimes*shipSizeEffect.get(member.getVariant().getHullSize()),
					Math.min(machineRefineTimes,metalRefineTimes)) * member.getRepairTracker().getCR()));


			// Math.random returns number between 0 and 1. If returned is !< breakdownChance, expression is false and breakdown won't happen.
			// So, 10% chance of breakdown.

			float breakdownChance = 0.1f;
            boolean willHeavyMachineryBreakdown = (Math.random()<breakdownChance);

			// Reminder to expand on Breakdown mechanic later with Nanoforges.

            int randomHeavyMachineryBreakdowns = MathUtils.getRandomNumberInRange(0, refineTimes);

			float dailyOreSpent = metalCost * refineTimes;
			float dailyMetalProduced = metalProduced * refineTimes;
			float dailyMachinesBroken = heavyMachineryCost * randomHeavyMachineryBreakdowns;

            fleet.getCargo().removeCommodity(Commodities.ORE, dailyOreSpent);
            fleet.getCargo().addCommodity(Commodities.METALS, dailyMetalProduced);
			if(willHeavyMachineryBreakdown)
				fleet.getCargo().removeCommodity(Commodities.HEAVY_MACHINERY, dailyMachinesBroken);

			float baseCommodityQuantity = 0f;

			totalDailyOreExpenditure = baseCommodityQuantity + dailyOreSpent;
			totalDailyMetalProduction = baseCommodityQuantity + dailyMetalProduced;
			totalDailyMachineBreakage = baseCommodityQuantity + dailyMachinesBroken;

			if(showNotification) 
			{
				final String shipName = member.getShipName() + " [" + member.getHullSpec().getHullNameWithDashClass() + "]";
				final String ores = String.valueOf((int) (dailyOreSpent));
				final String metals = String.valueOf((int) (dailyMetalProduced));

				final boolean hasBreakdownHappened = willHeavyMachineryBreakdown;
				final String machineryBreakdowns = String.valueOf((int) (dailyMachinesBroken));

				Global.getSector().getIntelManager().addIntel(new BaseIntelPlugin() {

					@Override
					public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {

						String metalProductionLine = shipName + " refined " + ores + " ores into " + metals + " metals";
						String machineryBreakdownsLine = machineryBreakdowns + " heavy machinery broke down";

						Color highlightColor = Misc.getHighlightColor();

						info.addPara(metalProductionLine, 2, METAL_COLOR, highlightColor, shipName, ores, metals);

						if(hasBreakdownHappened)
						info.addPara(machineryBreakdownsLine, 2, WARNING_COLOR, highlightColor, machineryBreakdowns);

						// Jaghaimo: die, you served your purpose
						Global.getSector().getIntelManager().removeIntel(this);
					}
				}
				);
            }

        }

        float transplutonic_ore = fleet.getCargo().getCommodityQuantity(Commodities.RARE_ORE);
        if(transplutonic_ore > transplutonicCost) {

            float transplutonicRefineTimes = transplutonic_ore/transplutonicCost;

            refineTimes = (int) Math.ceil((Math.min(maxRefineTimes*shipSizeEffect.get(member.getVariant().getHullSize()), Math.min(machineRefineTimes,transplutonicRefineTimes)) * member.getRepairTracker().getCR()));
            
            boolean willHeavyMachineryBreakdown = (Math.random()<0.1);
            int randomHeavyMachineryBreakdowns = MathUtils.getRandomNumberInRange(0, refineTimes);

            fleet.getCargo().removeCommodity(Commodities.RARE_ORE, transplutonicCost * refineTimes);
            if(willHeavyMachineryBreakdown) fleet.getCargo().removeCommodity(Commodities.HEAVY_MACHINERY, heavyMachineryCost * randomHeavyMachineryBreakdowns);
            fleet.getCargo().addCommodity(Commodities.RARE_METALS, transplutonicProduced * refineTimes);

            hasProducedSomething = true;
            
            if(playSFX) Global.getSoundPlayer().playSound("ui_cargo_raremetals", 1f, 1f, fleet.getLocation(), ZERO);

            if(showNotification) {
				Global.getSector().getCampaignUI().addMessage((int) (transplutonicCost * refineTimes) + " transplutonic ores were refined into " + (int) (transplutonicProduced * refineTimes) + " transplutonics", TRANSPLUTONIC_COLOR);
				if(willHeavyMachineryBreakdown)
					Global.getSector().getCampaignUI().addMessage((int) (heavyMachineryCost * randomHeavyMachineryBreakdowns) + " heavy machines broke down", WARNING_COLOR);
            }
            
//            fleet.addFloatingText("Refined Transplutonics", Misc.setAlpha(TEXT_COLOR, 255), 0.5f);
        }

        if(!hasProducedSomething) return;
        
        fleet.addFloatingText("Refined Ores", Misc.setAlpha(TEXT_COLOR, 255), 0.5f);

    	member.getRepairTracker().applyCREvent(-crCost,  "Refined ores");
	}

	private boolean hasDoneToday(FleetMemberAPI member) {
		if(engine == null) {
			return true;
		}
		if(engine.getCustomData() == null) {
			return true;
		}

		RefineryForgeData data = (RefineryForgeData) engine.getCustomData().get(DATA_KEY);
		if (data == null) {
			data = new RefineryForgeData();
			engine.getCustomData().put(DATA_KEY, data);
		}
		
		if(!data.counter.containsKey(member)) {
			data.counter.put(member, Global.getSector().getClock().getDay());
		}
		
		if(data.counter.get(member) == Global.getSector().getClock().getDay()) {
			return true;
		}
		data.counter.put(member, Global.getSector().getClock().getDay());
		
		return false;
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {

		boolean isForgeShip = false;
		int forgeShipSlots = 0;
		if (ship.getVariant().hasHullMod("yrex_ForgeShip")) {
			isForgeShip = true;
			forgeShipSlots = 1;
		}
		
		if(!isForgeShip) return false;

        for (String forgeHullmod : OTHER_FORGE_HULLMODS) {
			if(ship.getVariant().hasHullMod(forgeHullmod)) {
				forgeShipSlots--;
			}
        }

		if(forgeShipSlots <= 0) {
			return false;
		}

		return true;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		
		boolean isForgeShip = false;
		int forgeShipSlots = 0;
		if (ship.getVariant().hasHullMod("yrex_ForgeShip")) {
			isForgeShip = true;
			forgeShipSlots = 1;
		}
		
		if(!isForgeShip) return "Must be installed on a common Forge Ship";

        for (String forgeHullmod : OTHER_FORGE_HULLMODS) {
			if(ship.getVariant().hasHullMod(forgeHullmod)) {
				forgeShipSlots--;
			}
        }
		
		if(forgeShipSlots <= 0) {
			return "This ship has no more space for new forges.";
		}
		
		return null;
	}
}











