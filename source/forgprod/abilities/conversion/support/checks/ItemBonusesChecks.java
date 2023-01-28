package forgprod.abilities.conversion.support.checks;

import java.util.HashSet;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.campaign.SpecialItemSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Items;

import forgprod.settings.SettingsHolder;

/**
 * @author Ontheheavens
 * @since 04.12.2022
 */

public class ItemBonusesChecks {

    public static boolean hasSpecialItem(CampaignFleetAPI fleet, String specialItemType) {
        SpecialItemData specialItem = new SpecialItemData(specialItemType, null);
        float specialItemQuantity = fleet.getCargo().getQuantity(CargoAPI.CargoItemType.SPECIAL, specialItem);
        return ((int) Math.ceil(specialItemQuantity) >= 1);
    }

    public static boolean hasAnySpecialItem(CampaignFleetAPI fleet) {
        Set<String> items = new HashSet<>();
        items.add(Items.CATALYTIC_CORE);
        items.add(Items.SYNCHROTRON);
        items.add(Items.CORRUPTED_NANOFORGE);
        items.add(Items.PRISTINE_NANOFORGE);
        for (String item : items) {
            if (hasSpecialItem(fleet, item)) {
                return true;
            }
        }
        return false;
    }

    // Acts as a multiplier for base breakdown chance and breakdown severity.
    public static float getNanoforgeBreakdownDecrease(CampaignFleetAPI fleet) {
        if (hasSpecialItem(fleet, Items.PRISTINE_NANOFORGE)) {
            return (1 - SettingsHolder.PRISTINE_NANOFORGE_BREAKDOWN_DECREASE);
        }
        if (hasSpecialItem(fleet, Items.CORRUPTED_NANOFORGE)) {
            return (1 - SettingsHolder.CORRUPTED_NANOFORGE_BREAKDOWN_DECREASE);
        }
        return 1f;
    }

    private static SpecialItemSpecAPI getSpecialItemSpec(String itemId) {
        return Global.getSettings().getSpecialItemSpec(itemId);
    }

    public static SpecialItemSpecAPI getBestNanoforgeInCargoSpec(CampaignFleetAPI fleet) {
        if (hasSpecialItem(fleet, Items.PRISTINE_NANOFORGE)) {
            return getSpecialItemSpec(Items.PRISTINE_NANOFORGE);
        }
        if (hasSpecialItem(fleet, Items.CORRUPTED_NANOFORGE)) {
            return getSpecialItemSpec(Items.CORRUPTED_NANOFORGE);
        }
        return null;
    }

    public static float getRefiningBonus(CampaignFleetAPI fleet) {
        if (hasSpecialItem(fleet, Items.CATALYTIC_CORE)) {
            return ((float)SettingsHolder.CATALYTIC_CORE_OUTPUT_BONUS / SettingsHolder.GRANULARITY);
        }
        return 0f;
    }

    public static float getFuelProductionBonus(CampaignFleetAPI fleet) {
        if (hasSpecialItem(fleet, Items.SYNCHROTRON)) {
            return ((float) SettingsHolder.SYNCHROTRON_CORE_OUTPUT_BONUS / SettingsHolder.GRANULARITY);
        }
        return 0f;
    }

    public static float getHeavyIndustryBonus(CampaignFleetAPI fleet) {
        if (hasSpecialItem(fleet, Items.PRISTINE_NANOFORGE)) {
            return ((float) SettingsHolder.PRISTINE_NANOFORGE_OUTPUT_BONUS / SettingsHolder.GRANULARITY);
        }
        if (hasSpecialItem(fleet, Items.CORRUPTED_NANOFORGE)) {
            return ((float) SettingsHolder.CORRUPTED_NANOFORGE_OUTPUT_BONUS / SettingsHolder.GRANULARITY);
        }
        return 0f;
    }

}
