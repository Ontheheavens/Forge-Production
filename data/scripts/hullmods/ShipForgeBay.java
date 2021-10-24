package data.scripts.hullmods;

import java.util.Map;
import java.util.HashMap;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;

public class ShipForgeBay extends BaseHullMod {

    private static final Map<ShipAPI.HullSize, Integer> shipSizeEffect = new HashMap<>();
    static {
        shipSizeEffect.put(ShipAPI.HullSize.DEFAULT, 0);
        shipSizeEffect.put(ShipAPI.HullSize.CRUISER, 4);
        shipSizeEffect.put(ShipAPI.HullSize.CAPITAL_SHIP, 8);
    }

}
