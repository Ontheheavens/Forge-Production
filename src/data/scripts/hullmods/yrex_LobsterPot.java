package data.scripts.hullmods;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import org.apache.log4j.Logger;
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

public class yrex_LobsterPot extends BaseHullMod {
	
//	private static Logger log = Global.getLogger(yrex_LobsterPot.class);   

	private static final Set<String> OTHER_FORGE_HULLMODS = new HashSet<>();
	static {
		// These hullmods will automatically be removed
		// This prevents unexplained hullmod blocking
		OTHER_FORGE_HULLMODS.add("yrex_FuelForge");
		OTHER_FORGE_HULLMODS.add("yrex_SuppliesForge");
		OTHER_FORGE_HULLMODS.add("yrex_RefineryForge");
    	OTHER_FORGE_HULLMODS.add("yrxp_ShipMall");
	}

    private static final Map<ShipAPI.HullSize, Integer> shipSizeEffect = new HashMap<>();
    static {
    	shipSizeEffect.put(ShipAPI.HullSize.DEFAULT, 10);
    	shipSizeEffect.put(ShipAPI.HullSize.CAPITAL_SHIP, 100);
        shipSizeEffect.put(ShipAPI.HullSize.CRUISER, 50);
        shipSizeEffect.put(ShipAPI.HullSize.DESTROYER, 30);
        shipSizeEffect.put(ShipAPI.HullSize.FRIGATE, 10);    
    }

	public static String DATA_KEY = "yrex_lobster_pot_ship_key";

	public static class LobsterPotData {
		Map<FleetMemberAPI, Integer> counter = new LinkedHashMap<FleetMemberAPI, Integer>();
		Map<FleetMemberAPI, Float> growthTracker = new LinkedHashMap<FleetMemberAPI, Float>();//Each time grown reaches 1.0, add 1 lobster.
	}

	private static final Color TEXT_COLOR = new Color(255, 255, 255, 80);
	private static final Color LOBSTER_COLOR = new Color(200, 255, 200, 255);
	private static final Color FOOD_COLOR = new Color(255, 255, 255, 255);
	private static final Vector2f ZERO = new Vector2f();
	private static final float CRRecoverBonus = 25.0f;

	private float foodCost = Global.getSettings().getFloat("yrex_lobster_food_to_lobster_cost");
	private float growthPerLobster = Global.getSettings().getFloat("yrex_lobster_growth_rate");
	private float spawningMonthFrom = Global.getSettings().getFloat("yrex_lobster_spawning_month_from");
	private float spawningMonthTo = Global.getSettings().getFloat("yrex_lobster_spawning_month_to");
	
	//	Global.getSector().getCampaignUI().getMessageDisplay().addMessage(
	//		"" + wing.getWingName() + ": blueprint already known");//,

	private boolean playSFX = Global.getSettings().getBoolean("yrex_forge_play_sfx");
	private boolean showNotification = Global.getSettings().getBoolean("yrex_forge_show_notification");

	private CombatEngineAPI engine;

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

		stats.getBaseCRRecoveryRatePercentPerDay().modifyMult(id, 1f + CRRecoverBonus * 0.01f);
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0)
			return "" + CRRecoverBonus + "%";
		if (index == 1)
			return "" + foodCost;
		if (index == 2)
			return shipSizeEffect.get(ShipAPI.HullSize.FRIGATE) + "/" + 
			shipSizeEffect.get(ShipAPI.HullSize.DESTROYER) + "/" + 
			shipSizeEffect.get(ShipAPI.HullSize.CRUISER) + "/" + 
			shipSizeEffect.get(ShipAPI.HullSize.CAPITAL_SHIP);
		return null;
	}

	public void advanceInCampaign(FleetMemberAPI member, float amount) {
		if (member.getFleetData() == null)
			return;
		if (member.getFleetData().getFleet() == null)
			return;
		if (!member.getFleetData().getFleet().isPlayerFleet())
			return;

		if (engine != Global.getCombatEngine()) {
			this.engine = Global.getCombatEngine();
		}

		CampaignFleetAPI fleet = member.getFleetData().getFleet();

		if (hasDoneToday(member)) {
			return;
		}
		// On next day
		
		
		float lobsters = fleet.getCargo().getCommodityQuantity(Commodities.LOBSTER);
		float food = fleet.getCargo().getCommodityQuantity(Commodities.FOOD);

		float maxLobstersThatCanBreed = shipSizeEffect.get(member.getVariant().getHullSize()) * (100/growthPerLobster);
		float maxLobstersThatNeedToEat = lobsters;
		float maxLobstersThatCanBeFed = food / foodCost;
		
		float lobstersToEat = Math.min(maxLobstersThatCanBreed, Math.min(maxLobstersThatNeedToEat, maxLobstersThatCanBeFed));
		float totalGrowth = lobstersToEat * growthPerLobster;
		
		boolean isSpawningSeason = false;
		if(Global.getSector().getClock().getMonth() >= spawningMonthFrom && Global.getSector().getClock().getMonth() <= spawningMonthTo) isSpawningSeason = true;
		
		if(isSpawningSeason) totalGrowth *= 10;
		
		int newLobsters = this.addGrowth(member, totalGrowth);
		
		newLobsters = (int) Math.min(shipSizeEffect.get(member.getVariant().getHullSize()), newLobsters);

		fleet.getCargo().removeCommodity(Commodities.FOOD, lobstersToEat * foodCost);
		fleet.getCargo().addCommodity(Commodities.LOBSTER, newLobsters);
		
		
		if(lobstersToEat > 0) {
			if (playSFX)
				Global.getSoundPlayer().playSound("ui_cargo_food_drop", 1f, 1f, fleet.getLocation(), ZERO);
	
			if (showNotification)			
				Global.getSector().getCampaignUI().addMessage("Lobsters have eaten " + (int) (lobstersToEat * foodCost) + " food", FOOD_COLOR);
		}
		
		if(newLobsters > 0) {
			if (playSFX)
				Global.getSoundPlayer().playSound("ui_cargo_luxury", 1f, 1f, fleet.getLocation(), ZERO);

			if (showNotification)			
				Global.getSector().getCampaignUI().addMessage((int) (newLobsters) + " new lobsters have been born.", LOBSTER_COLOR);
				
			fleet.addFloatingText("Lobsters have multiplied", Misc.setAlpha(TEXT_COLOR, 255), 0.5f);
		}
		

	}

	private boolean hasDoneToday(FleetMemberAPI member) {
		if(engine == null) {
			return true;
		}
		if(engine.getCustomData() == null) {
			return true;
		}

		LobsterPotData data = (LobsterPotData) engine.getCustomData().get(DATA_KEY);
		if (data == null) {
			data = new LobsterPotData();
			engine.getCustomData().put(DATA_KEY, data);
		}

		if (!data.counter.containsKey(member)) {
			data.counter.put(member, Global.getSector().getClock().getDay());
		}

		if (data.counter.get(member) == Global.getSector().getClock().getDay()) {
			return true;
		}
		data.counter.put(member, Global.getSector().getClock().getDay());

		return false;
	}
	
	private int addGrowth(FleetMemberAPI member, float growthAmount) {

		LobsterPotData data = (LobsterPotData) engine.getCustomData().get(DATA_KEY);
		if (data == null) {
			data = new LobsterPotData();
			engine.getCustomData().put(DATA_KEY, data);
		}

		if (!data.growthTracker.containsKey(member)) {
			data.growthTracker.put(member, 0f);
		}
		
		float currentGrowth = data.growthTracker.get(member);
		int numberOfNewLobsters = 0;
		
		currentGrowth += growthAmount;

		//New lobster is grown every 100 growth
		numberOfNewLobsters = (int) currentGrowth / 100;
		
		//Leave the remaining growth to the counter;
		currentGrowth = currentGrowth % 100;
		
		data.growthTracker.put(member, currentGrowth);

		return numberOfNewLobsters;
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
//		return !ship.getVariant().getHullMods().contains("unstable_injector") &&
//			   !ship.getVariant().getHullMods().contains("augmented_engines");
//		if (ship.getHullSize() == HullSize.FRIGATE)
//			return false;

		boolean isForgeShip = false;
		int forgeShipSlots = 0;
		if (ship.getVariant().hasHullMod("yrex_ForgeShip")) {
			isForgeShip = true;
			forgeShipSlots = 1;
		}

		if (!isForgeShip)
			return false;

		for (String tmp : OTHER_FORGE_HULLMODS) {
			if (ship.getVariant().hasHullMod(tmp)) {
				forgeShipSlots--;
			}
		}

		if (forgeShipSlots <= 0) {
			return false;
		}

		return true;
	}

	public String getUnapplicableReason(ShipAPI ship) {
//		if (ship.getHullSize() == HullSize.FRIGATE)
//			return "Cannot be installed on a frigate";

		boolean isForgeShip = false;
		int forgeShipSlots = 0;
		if (ship.getVariant().hasHullMod("yrex_ForgeShip")) {
			isForgeShip = true;
			forgeShipSlots = 1;
		}

		if (!isForgeShip)
			return "Must be installed on a common Forge Ship";

		for (String tmp : OTHER_FORGE_HULLMODS) {
			if (ship.getVariant().hasHullMod(tmp)) {
				forgeShipSlots--;
			}
		}

		if (forgeShipSlots <= 0) {
			return "This ship has no more space for new forges.";
		}

		return null;
	}
}
