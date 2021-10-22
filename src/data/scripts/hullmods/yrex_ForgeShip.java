package data.scripts.hullmods;

import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

//import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
public class yrex_ForgeShip extends BaseHullMod {    

//	   private static Logger log = Global.getLogger(yrex_ForgeShip.class);  
    
    private static final Set<String> BLOCKED_HULLMODS = new HashSet<>();
    static
    {
        // These hullmods will automatically be removed
        // This prevents unexplained hullmod blocking
        
    }
    private String ID, ERROR="yrex_IncompatibleHullmodWarning";
    
//    OTH: Here, I replaced Creature's old relative cargo malus with new absolute cargo malus taken from vanilla Expanded Cargo Holds hullmod.
//
//    private static final float CARGO_MALUS = 60f;

    private static Map mag = new HashMap();
    static {
        mag.put(HullSize.CRUISER, 200f);
        mag.put(HullSize.CAPITAL_SHIP, 400f);
    }

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		float mod = (Float) mag.get(hullSize);
		stats.getCargoMod().modifyFlat(id, -mod);
	}

//    @Override
//    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
//    	stats.getCargoMod().modifyMult(id, 1f - CARGO_MALUS * 0.01f);
//        ID=id;
//    }
        
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id){        
        for (String tmp : BLOCKED_HULLMODS) {
            if (ship.getVariant().getHullMods().contains(tmp)) {                
                ship.getVariant().removeMod(tmp);      
                ship.getVariant().addMod(ERROR);
            }
        }

//		boolean isApplicable = (ship.getParentStation() == null);
//		log.info("after effects parent station: " + ship.getParentStation() + "|" + isApplicable);
//        if(!isApplicable) {                
//                ship.getVariant().removeMod(this.spec.getId());      
//                ship.getVariant().addMod(ERROR);
//        }
    }
    
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
		if (index == 1) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue();
		if (index == 2) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue();
		return "1";
    }
	
//	public boolean isApplicableToShip(ShipAPI ship) {
//		log.info("check cargo: " + ship.getHullSpec().getCargo() + "|" + (ship.getHullSpec().getCargo() > 0));
//		
//		
//		if(ship.getHullSpec().getCargo() > 0) return true;
//		return false;
//		
//		//if (ship.getMutableStats().getCargoMod().computeEffective(ship.getHullSpec().getCargo()) < CARGO_REQ) return false;
//		
//		if(ship == null) return false;
//		
//		boolean isApplicable = (ship.getParentStation() == null);
//		
//		
//		log.info("check parent station: " + ship.getParentStation() + "|" + isApplicable);
//		log.info("ship.isStationModule(): " + ship.isStationModule());
//		
//		return isApplicable;		

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return (ship.getHullSize() == HullSize.CAPITAL_SHIP || ship.getHullSize() == HullSize.CRUISER) &&
                (ship.getHullSpec().getCargo() > 0);
    }

    public String getUnapplicableReason(ShipAPI ship) {
		if (ship != null && ship.getHullSize() != HullSize.CAPITAL_SHIP && ship.getHullSize() != HullSize.CRUISER) {
			return "Can only be installed on cruisers and capital ships";
		}
		if (ship.getHullSpec().getCargo() == 0) {
			return "Can not be installed on modules";
		}
		
		return null;
	}
//	public String getUnapplicableReason(ShipAPI ship) {
//		return "Can not be installed on modules";
//	}
}
