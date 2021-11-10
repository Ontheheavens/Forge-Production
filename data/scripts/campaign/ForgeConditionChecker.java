package data.scripts.campaign;

import java.util.Set;
import java.util.HashSet;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Items;

import data.scripts.plugins.ForgeSettings;

public class ForgeConditionChecker {

    // Here: Declared constants

    public static final Set<String> ALL_FORGE_HULLMODS = new HashSet<>();
    static
    {
        ALL_FORGE_HULLMODS.add("forge_refinery_module");
        ALL_FORGE_HULLMODS.add("forge_centrifuge_module");
        ALL_FORGE_HULLMODS.add("forge_manufacture_module");
        ALL_FORGE_HULLMODS.add("forge_assembly_module");
    }

    //Here: Custom methods

    public static boolean hasSpecialItem(String specialItemType) {

        SpecialItemData specialItem = new SpecialItemData(specialItemType, null);
        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
        int specialItemQuantity = (int) Math.ceil(cargo.getQuantity(CargoAPI.CargoItemType.SPECIAL, specialItem));
        return specialItemQuantity > 0;

    }

    public static float getForgingQuality() {

        if (hasSpecialItem(Items.PRISTINE_NANOFORGE)) {
            return (float) ForgeSettings.PRISTINE_NANOFORGE_QUALITY_BONUS;
        }
        if (hasSpecialItem(Items.CORRUPTED_NANOFORGE)) {
            return (float) ForgeSettings.CORRUPTED_NANOFORGE_QUALITY_BONUS;
        }
        return 1f;
    }

    public static float getCatalyticCoreBonus() {

        if (hasSpecialItem(Items.CATALYTIC_CORE)) {
            return ForgeSettings.CATALYTIC_CORE_REFINING_BONUS;
        }
        return 0f;
    }

    public static float getSynchrotronCoreBonus() {

        if (hasSpecialItem(Items.SYNCHROTRON)) {
            return ForgeSettings.SYNCHROTRON_CORE_CENTRIFUGING_BONUS;
        }
        return 0f;
    }

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

    public static boolean hasInactiveForgeShips() {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) {
            return false;
        }

        for (FleetMemberAPI member : playerFleet.getMembersWithFightersCopy()) {
            if (!isValid(member)) {
                continue;
            }
            if (isForgeShip(member) && !isOperational(member)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOperational(FleetMemberAPI member) {
        // Forge Hullmods are inactive on mothballed or unrepaired ships
        return !member.getRepairTracker().isMothballed() && !member.getRepairTracker().isSuspendRepairs();
    }

    public static boolean isValid(FleetMemberAPI member) {
        // Forge Hullmods are only applicable to cruisers or capitals
        return isSize(member, ShipAPI.HullSize.CRUISER) ||
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
