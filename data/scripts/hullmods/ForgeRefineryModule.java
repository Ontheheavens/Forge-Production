package data.scripts.hullmods;

import java.util.Set;
import java.util.HashSet;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;

public class ForgeRefineryModule extends BaseHullMod {

    private static final Set<String> OTHER_FORGE_HULLMODS = new HashSet<>();
    static
    {

        OTHER_FORGE_HULLMODS.add("placeholder");

    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {

        boolean hasForgeModule = false;

        for (String forgeHullmod : OTHER_FORGE_HULLMODS) {
            if(ship.getVariant().hasHullMod(forgeHullmod)) {
                hasForgeModule = true;
            }
        }

        return (ship.getVariant().hasHullMod("forge_ship") && !hasForgeModule);

    }

    public String getUnapplicableReason(ShipAPI ship) {

        if(!(ship.getVariant().hasHullMod("forge_ship"))) return "Must be installed on a Forge Ship";

        boolean hasForgeModule = false;

        for (String forgeHullmod : OTHER_FORGE_HULLMODS) {
            if(ship.getVariant().hasHullMod(forgeHullmod)) {
                hasForgeModule = true;
            }
        }

        if (hasForgeModule) {
            return "This ship has no more space for new forges.";
        }

        return null;
    }

}
