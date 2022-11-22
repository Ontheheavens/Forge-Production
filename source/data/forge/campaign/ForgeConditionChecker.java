package data.forge.campaign;

import java.util.List;
import java.util.ArrayList;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Items;

import data.forge.abilities.conversion.support.ForgeConversionGeneral;
import data.forge.abilities.conversion.support.ForgeTypes;
import data.forge.hullmods.support.ForgeHullmodsGeneral;
import data.forge.plugins.ForgeSettings;

import static data.forge.plugins.ForgeSettings.BASE_BREAKDOWN_CHANCE;
import static data.forge.plugins.ForgeSettings.FUEL_PRODUCED;

public class ForgeConditionChecker {

    public static boolean getMachineryBreakdownChance() {
        return (Math.random()<(BASE_BREAKDOWN_CHANCE * ForgeConditionChecker.getForgingQuality()));
    }

    public static boolean areFuelTanksFull(CampaignFleetAPI fleet) {
       return fleet.getCargo().getFreeFuelSpace() < (FUEL_PRODUCED + ForgeConditionChecker.getSynchrotronCoreBonus());
    }

    public static boolean hasMinimumMachinery() {
        return ForgeConversionGeneral.getMachineryAvailability() >= 0.1f;
    }

    public static int getPlayerCargo(String commodity) {
        if (Global.getSector().getPlayerFleet() == null) {
            return 0;
        }
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        return Math.round(fleet.getCargo().getCommodityQuantity(commodity));
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
            return ForgeSettings.PRISTINE_NANOFORGE_QUALITY_BONUS;
        }
        if (hasSpecialItem(Items.CORRUPTED_NANOFORGE)) {
            return ForgeSettings.CORRUPTED_NANOFORGE_QUALITY_BONUS;
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
            return ForgeSettings.PRISTINE_NANOFORGE_MANUFACTURING_BONUS;
        }
        if (hasSpecialItem(Items.CORRUPTED_NANOFORGE)) {
            return ForgeSettings.CORRUPTED_NANOFORGE_MANUFACTURING_BONUS;
        }
        return 0f;
    }

    public static float getNanoforgeAssemblingBonus() {
        if (hasSpecialItem(Items.PRISTINE_NANOFORGE)) {
            return ForgeSettings.PRISTINE_NANOFORGE_ASSEMBLING_BONUS;
        }
        if (hasSpecialItem(Items.CORRUPTED_NANOFORGE)) {
            return ForgeSettings.CORRUPTED_NANOFORGE_ASSEMBLING_BONUS;
        }
        return 0f;
    }

    public static boolean hasAnyForgingCapacity (CampaignFleetAPI fleet) {
        return  ForgeConversionGeneral.getForgingCapacityWithCR(fleet, ForgeTypes.REFINERY) >= 1 ||
                ForgeConversionGeneral.getForgingCapacityWithCR(fleet, ForgeTypes.CENTRIFUGE) >= 1 ||
                ForgeConversionGeneral.getForgingCapacityWithCR(fleet, ForgeTypes.MANUFACTURE) >= 1 ||
                ForgeConversionGeneral.getForgingCapacityWithCR(fleet, ForgeTypes.ASSEMBLY) >= 1;
    }


    public static boolean hasActiveForgeShips() {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) {
            return false;
        }
        for (FleetMemberAPI member : playerFleet.getMembersWithFightersCopy()) {
            if (!isValidHullsize(member) || !isOperational(member)) {
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
            if (!isValidHullsize(member)) {
                continue;
            }
            if (isForgeShip(member) && !isOperational(member)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOperational(FleetMemberAPI member) {
        return !member.getRepairTracker().isMothballed() &&
               !member.getRepairTracker().isSuspendRepairs() &&
                hasMinimumCR(member);
    }

    public static boolean isOperational(ShipAPI ship) {
        return !ship.getFleetMember().getRepairTracker().isMothballed() &&
               !ship.getFleetMember().getRepairTracker().isSuspendRepairs() &&
                hasMinimumCR(ship);
    }

    public static boolean hasMinimumCR(FleetMemberAPI member) {
        return member.getRepairTracker().getCR() > 0.1f;
    }

    public static boolean hasMinimumCR(ShipAPI ship) {
        return ship.getFleetMember().getRepairTracker().getCR() > 0.1f;
    }

    public static boolean isValidHullsize(FleetMemberAPI member) {
        return isSize(member, ShipAPI.HullSize.CRUISER) ||
               isSize(member, ShipAPI.HullSize.CAPITAL_SHIP);
    }

    public static boolean isValidHullsize(ShipAPI ship) {
        return (ship.getHullSize() == ShipAPI.HullSize.CRUISER ||
                ship.getHullSize() == ShipAPI.HullSize.CAPITAL_SHIP);
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
