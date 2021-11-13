package data.forge.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Items;

import data.forge.abilities.conversion.ForgeConversionGeneral;
import data.forge.abilities.conversion.ForgeTypes;
import data.forge.hullmods.ForgeHullmodsGeneral;
import data.forge.plugins.ForgeSettings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static data.forge.plugins.ForgeSettings.BASE_BREAKDOWN_CHANCE;
import static data.forge.plugins.ForgeSettings.FUEL_PRODUCED;

public class ForgeConditionChecker {

    public static boolean getMachineryBreakdownChance() {
        return (Math.random()<((float) BASE_BREAKDOWN_CHANCE * ForgeConditionChecker.getForgingQuality()));
    }

    public static boolean areFuelTanksFull(CampaignFleetAPI fleet) {
       return fleet.getCargo().getFreeFuelSpace() < (FUEL_PRODUCED + ForgeConditionChecker.getSynchrotronCoreBonus());
    }

    public static float getPlayerCargo(String commodity) {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        return fleet.getCargo().getCommodityQuantity(commodity);
    }

    public static boolean hasSpecialItem(String specialItemType) {
        SpecialItemData specialItem = new SpecialItemData(specialItemType, null);
        CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
        int specialItemQuantity = (int) Math.ceil(cargo.getQuantity(CargoAPI.CargoItemType.SPECIAL, specialItem));
        return specialItemQuantity > 0;
    }

    public static boolean hasAnySpecialItem() {
        List<String> specialItems = new ArrayList<>();
        {
            specialItems.add(Items.CORRUPTED_NANOFORGE);
            specialItems.add(Items.PRISTINE_NANOFORGE);
            specialItems.add(Items.CATALYTIC_CORE);
            specialItems.add(Items.SYNCHROTRON);
        }
        for(String specialItem : specialItems) {
            if(hasSpecialItem(specialItem)) {
                return true;
            }
        }
        return false;
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

    public static float getNanoforgeManufacturingBonus() {
        if (hasSpecialItem(Items.PRISTINE_NANOFORGE)) {
            return (float) ForgeSettings.PRISTINE_NANOFORGE_MANUFACTURING_BONUS;
        }
        if (hasSpecialItem(Items.CORRUPTED_NANOFORGE)) {
            return (float) ForgeSettings.CORRUPTED_NANOFORGE_MANUFACTURING_BONUS;
        }
        return 0f;
    }

    public static float getNanoforgeAssemblingBonus() {
        if (hasSpecialItem(Items.PRISTINE_NANOFORGE)) {
            return (float) ForgeSettings.PRISTINE_NANOFORGE_ASSEMBLING_BONUS;
        }
        if (hasSpecialItem(Items.CORRUPTED_NANOFORGE)) {
            return (float) ForgeSettings.CORRUPTED_NANOFORGE_ASSEMBLING_BONUS;
        }
        return 0f;
    }

    public static boolean hasAnyForgingCapacity (CampaignFleetAPI fleet) {
        return  ForgeConversionGeneral.getForgingCapacity(fleet, ForgeTypes.REFINING) >= 1 ||
                ForgeConversionGeneral.getForgingCapacity(fleet, ForgeTypes.CENTRIFUGING) >= 1 ||
                ForgeConversionGeneral.getForgingCapacity(fleet, ForgeTypes.MANUFACTURING) >= 1 ||
                ForgeConversionGeneral.getForgingCapacity(fleet, ForgeTypes.ASSEMBLING) >= 1;
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
        return !member.getRepairTracker().isMothballed() && !member.getRepairTracker().isSuspendRepairs();
    }

    public static boolean isValid(FleetMemberAPI member) {
        return isSize(member, ShipAPI.HullSize.CRUISER) ||
               isSize(member, ShipAPI.HullSize.CAPITAL_SHIP);
    }

    public static boolean isForgeShip(FleetMemberAPI member) {
        for (String forgeHullmod : ForgeHullmodsGeneral.ALL_FORGE_HULLMODS) {
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
