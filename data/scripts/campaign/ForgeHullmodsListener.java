package data.scripts.campaign;

import com.fs.starfarer.api.EveryFrameScript;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import data.scripts.abilities.ForgeProduction;

import java.util.HashSet;
import java.util.Set;

public class ForgeHullmodsListener implements EveryFrameScript {

    // Here: Declared constants

    public static final Set<String> ALL_FORGE_HULLMODS = new HashSet<>();

    static {
        ALL_FORGE_HULLMODS.add("forge_refinery_module");
    }

    // Here: Inherited methods

    public boolean isDone() {
        return false;
    }

    public boolean runWhilePaused() {
        return false;
    }

    public void advance(float amount) {

        ForgeProduction.setUseAllowedByListener(hasActiveForgeShips());

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        int corruptedNanoforgesQuantity = Math.round(playerFleet.getCargo().getQuantity(CargoAPI.CargoItemType.SPECIAL, new SpecialItemData(Items.CORRUPTED_NANOFORGE, null)));
        int pristineNanoforgesQuantity = Math.round(playerFleet.getCargo().getQuantity(CargoAPI.CargoItemType.SPECIAL, new SpecialItemData(Items.PRISTINE_NANOFORGE, null)));

        if ((corruptedNanoforgesQuantity >= 1) && (pristineNanoforgesQuantity < 1) ) {
            ForgeProduction.setNanoforgeQuality( 0.8f );
        }

        if ((pristineNanoforgesQuantity >= 1)) {
            ForgeProduction.setNanoforgeQuality( 0.5f );
        }

        if ((corruptedNanoforgesQuantity < 1) && (pristineNanoforgesQuantity < 1) ) {
            ForgeProduction.setNanoforgeQuality( 1f );
        }

    }

    //Here: Custom methods

    public static boolean hasActiveForgeShips() {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) {
            return false;
        }

        for (FleetMemberAPI member : playerFleet.getMembersWithFightersCopy()) {
            if (!isValid(member) || !isOperational(member)) {
                continue;
            }
            if (isForgeShip(member)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOperational(FleetMemberAPI member) {
        // Forge Hullmods are inactive on mothballed or unrepaired ships
        return !member.getRepairTracker().isMothballed() || !member.getRepairTracker().isSuspendRepairs();
    }

    public static boolean isValid(FleetMemberAPI member) {
        // Forge Hullmods are only applicable to destroyers, cruisers or capitals
        return isSize(member, ShipAPI.HullSize.DESTROYER) ||
               isSize(member, ShipAPI.HullSize.CRUISER) ||
               isSize(member, ShipAPI.HullSize.CAPITAL_SHIP);
    }

    public static boolean isForgeShip(FleetMemberAPI member) {
        for (String forgeHullmod : ALL_FORGE_HULLMODS) {
            if (member.getVariant().getHullMods().contains(forgeHullmod)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSize(FleetMemberAPI member, ShipAPI.HullSize hullSize) {
        return member.getHullSpec().getHullSize().equals(hullSize);
    }

}
