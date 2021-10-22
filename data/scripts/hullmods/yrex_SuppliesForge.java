package data.scripts.hullmods;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.BuffManagerAPI.Buff;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberViewAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;


public class yrex_SuppliesForge extends BaseHullMod {

	
    private static final Set<String> OTHER_FORGE_HULLMODS = new HashSet<>();
    static
    {
        // These hullmods will automatically be removed
        // This prevents unexplained hullmod blocking
    	OTHER_FORGE_HULLMODS.add("yrex_RefineryForge");
    	OTHER_FORGE_HULLMODS.add("yrex_FuelForge");
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
    
	public static String MFS_DATA_KEY = "yrex_supplies_forge_ship_key";
	public static class MunitionsForgeData {
		Map<FleetMemberAPI, Integer> counter = new LinkedHashMap<FleetMemberAPI, Integer>();
	}

	private static final Color TEXT_COLOR = new Color(255, 255, 255, 80);
	private static final Color SUPPLIES_COLOR = new Color(255, 255, 150, 255);
	private static final Color WARNING_COLOR = new Color(255, 100, 100, 255);
	private static final Vector2f ZERO = new Vector2f();

	private static final float ROF_BONUS = 10.0f;
	private static final float AMMO_BONUS = 50.0f;
	
    private float metalCost = Global.getSettings().getFloat("yrex_forge_metal_to_supplies_cost");
    private float transplutonicCost = Global.getSettings().getFloat("yrex_forge_transplutonic_to_supplies_cost");
    private float heavyMachineryCost = Global.getSettings().getFloat("yrex_forge_heavy_machinery_to_supplies_cost");
    private float suppliesProduced = Global.getSettings().getFloat("yrex_forge_supplies_produced");
    
    private int maxRefineTimes = Global.getSettings().getInt("yrex_forge_max_supply_produce_per_day");

    private final float crCost = Global.getSettings().getFloat("yrex_forge_supplies_cr_cost_per_day");

    private boolean playSFX = Global.getSettings().getBoolean("yrex_forge_play_sfx");
    private boolean showNotification = Global.getSettings().getBoolean("yrex_forge_show_notification");

	private CombatEngineAPI engine;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getBallisticRoFMult().modifyMult(id, 1f + ROF_BONUS * 0.01f);
		stats.getEnergyRoFMult().modifyMult(id, 1f + ROF_BONUS * 0.01f);
		stats.getMissileRoFMult().modifyMult(id, 1f + ROF_BONUS * 0.01f);

		stats.getBallisticAmmoBonus().modifyMult(id, 1f + AMMO_BONUS * 0.01f);
		stats.getEnergyAmmoBonus().modifyMult(id, 1f + AMMO_BONUS * 0.01f);
		stats.getMissileAmmoBonus().modifyMult(id, 1f + AMMO_BONUS * 0.01f);
	}
    
    public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + AMMO_BONUS + "%";
		if (index == 1) return "" + ROF_BONUS + "%";
//		if (index == 2) return "" + maxRefineTimes;
		if (index == 2) return maxRefineTimes*shipSizeEffect.get(ShipAPI.HullSize.FRIGATE) + "/" + 
			maxRefineTimes*shipSizeEffect.get(ShipAPI.HullSize.DESTROYER) + "/" + 
			maxRefineTimes*shipSizeEffect.get(ShipAPI.HullSize.CRUISER) + "/" + 
			maxRefineTimes*shipSizeEffect.get(ShipAPI.HullSize.CAPITAL_SHIP);
		if (index == 3) return "" + metalCost;
		if (index == 4) return "" + transplutonicCost;
		if (index == 5) return "" + heavyMachineryCost;
		if (index == 6) return "" + suppliesProduced;
		if (index == 7) return "" + crCost + "%";
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

        if(hasDoneToday(member)) {;
        	return;
        }
		//On next day
        
        float machines = fleet.getCargo().getCommodityQuantity(Commodities.HEAVY_MACHINERY);
        if(machines <= heavyMachineryCost) {
        	//Cannot generate refined materials without enough heavy machinery
//        	fleet.addFloatingText("Full of Supplies", Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f);
            
            return;
        } 

        int refineTimes = 0;
        float machineRefineTimes = machines/heavyMachineryCost;
        

        float metals = fleet.getCargo().getCommodityQuantity(Commodities.METALS);
        float transplutonics = fleet.getCargo().getCommodityQuantity(Commodities.RARE_METALS);
        if(metals > metalCost && transplutonics > transplutonicCost) {

            float metalRefineTimes = metals/metalCost; 
            float transplutonicRefineTimes = transplutonics/transplutonicCost; 
            
            refineTimes = (int) Math.ceil((Math.min(maxRefineTimes*shipSizeEffect.get(member.getVariant().getHullSize()), Math.max(machineRefineTimes, Math.max(metalRefineTimes, transplutonicRefineTimes))) * member.getRepairTracker().getCR()));
            
            
            boolean willHeavyMachineryBreakdown = (Math.random()<0.1);
            int randomHeavyMachineryBreakdowns = MathUtils.getRandomNumberInRange(0, refineTimes);

            fleet.getCargo().removeCommodity(Commodities.METALS, metalCost * refineTimes);
            fleet.getCargo().removeCommodity(Commodities.RARE_METALS, transplutonicCost * refineTimes);
            if(willHeavyMachineryBreakdown) fleet.getCargo().removeCommodity(Commodities.HEAVY_MACHINERY, heavyMachineryCost * randomHeavyMachineryBreakdowns);
            fleet.getCargo().addCommodity(Commodities.SUPPLIES, suppliesProduced * refineTimes);
            
            if(playSFX) Global.getSoundPlayer().playSound("ui_cargo_handweapons", 1f, 1f, fleet.getLocation(), ZERO);
            
            if(showNotification) {
				Global.getSector().getCampaignUI().addMessage((int) (metalCost * refineTimes) + " metals and " + (int) (transplutonicCost * refineTimes) + " transplutonics were used to make " + (int) (suppliesProduced * refineTimes) + " supplies", SUPPLIES_COLOR);
				if(willHeavyMachineryBreakdown)
					Global.getSector().getCampaignUI().addMessage((int) (heavyMachineryCost * randomHeavyMachineryBreakdowns) + " heavy machines broke down", WARNING_COLOR);
            }
            
            fleet.addFloatingText("Produced Supplies", Misc.setAlpha(TEXT_COLOR, 255), 0.5f);

        	member.getRepairTracker().applyCREvent(-crCost,  "Produced supplies");
        }
		
	}
	private boolean hasDoneToday(FleetMemberAPI member) {
		if(engine == null) {
			return true;
		}
		if(engine.getCustomData() == null) {
			return true;
		}

		MunitionsForgeData data = (MunitionsForgeData) engine.getCustomData().get(MFS_DATA_KEY);
		if (data == null) {
			data = new MunitionsForgeData();
			engine.getCustomData().put(MFS_DATA_KEY, data);
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
//		return !ship.getVariant().getHullMods().contains("unstable_injector") &&
//			   !ship.getVariant().getHullMods().contains("augmented_engines");

		boolean isForgeShip = false;
		int forgeShipSlots = 0;
		if (ship.getVariant().hasHullMod("yrex_ForgeShip")) {
			isForgeShip = true;
			forgeShipSlots = 1;
		}
		
		if(!isForgeShip) return false;

        for (String tmp : OTHER_FORGE_HULLMODS) {
			if(ship.getVariant().hasHullMod(tmp)) {
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
		
		if(!isForgeShip) return "Must be installed into an Common Forge Ship";

        for (String tmp : OTHER_FORGE_HULLMODS) {
			if(ship.getVariant().hasHullMod(tmp)) {
				forgeShipSlots--;
			}
        }
		
		if(forgeShipSlots <= 0) {
			return "This ship has no more space for new forges.";
		}
		
		return null;
	}
}











