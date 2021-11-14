package data.forge.plugins;

import java.io.IOException;
import java.math.BigDecimal;

import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;

public class ForgeDataSupplier {

    public static void loadSettings(String fileName) throws IOException, JSONException {

            JSONObject settings = Global.getSettings().loadJSON(fileName);

            // Here: Miscellaneous Settings
            ForgeSettings.ENABLE_PENALTIES = settings.getBoolean("forge_enable_ability_penalties");
            ForgeSettings.SENSOR_PROFILE_INCREASE = settings.getInt("forge_sensor_profile_increase");
            ForgeSettings.SHIP_LIST_SIZE = settings.getInt("forge_ship_list_size");

            // Here: Notification Settings
            ForgeSettings.PLAY_SOUND_NOTIFICATION = settings.getBoolean("forge_play_sound_notification");
            ForgeSettings.SHOW_NOTIFICATION = settings.getBoolean("forge_show_notification");
            ForgeSettings.NOTIFICATION_INTERVAL = settings.getInt("forge_notification_interval");

            // Here: Capacity Settings
            ForgeSettings.CAPACITY_CRUISER = settings.getInt("forge_capacity_cruiser");
            ForgeSettings.CAPACITY_CAPITAL = settings.getInt("forge_capacity_capital");

            // Here: Refining Settings
            ForgeSettings.ORE_TO_REFINE = Float.parseFloat(settings.getString("forge_ore_to_refine"));
            ForgeSettings.METAL_PRODUCED = Float.parseFloat(settings.getString("forge_metal_produced"));
            ForgeSettings.TRANSPLUTONIC_ORE_TO_REFINE = Float.parseFloat(settings.getString("forge_transplutonic_ore_to_refine"));
            ForgeSettings.TRANSPLUTONICS_PRODUCED = Float.parseFloat(settings.getString("forge_transplutonics_produced"));
            ForgeSettings.HEAVY_MACHINERY_REFINING_USAGE = Float.parseFloat(settings.getString("forge_heavy_machinery_refining_usage"));

            // Here: Centrifuging Settings
            ForgeSettings.VOLATILES_TO_CENTRIFUGE = Float.parseFloat(settings.getString("forge_volatiles_to_centrifuge"));
            ForgeSettings.FUEL_PRODUCED = Float.parseFloat(settings.getString("forge_fuel_produced"));
            ForgeSettings.HEAVY_MACHINERY_CENTRIFUGING_USAGE = Float.parseFloat(settings.getString("forge_heavy_machinery_centrifuging_usage"));

            // Here: Manufacturing Settings
            ForgeSettings.METAL_TO_MANUFACTURE = Float.parseFloat(settings.getString("forge_metal_to_manufacture"));
            ForgeSettings.TRANSPLUTONICS_TO_MANUFACTURE = Float.parseFloat(settings.getString("forge_transplutonics_to_manufacture"));
            ForgeSettings.SUPPLIES_PRODUCED = Float.parseFloat(settings.getString("forge_supplies_produced"));
            ForgeSettings.HEAVY_MACHINERY_MANUFACTURING_USAGE = Float.parseFloat(settings.getString("forge_heavy_machinery_manufacturing_usage"));

            // Here: Assembling Settings
            ForgeSettings.METAL_TO_ASSEMBLE = Float.parseFloat(settings.getString("forge_metal_to_assemble"));
            ForgeSettings.TRANSPLUTONICS_TO_ASSEMBLE = Float.parseFloat(settings.getString("forge_transplutonics_to_assemble"));
            ForgeSettings.HEAVY_MACHINERY_PRODUCED = Float.parseFloat(settings.getString("forge_heavy_machinery_produced"));
            ForgeSettings.HEAVY_MACHINERY_ASSEMBLING_USAGE = Float.parseFloat(settings.getString("forge_heavy_machinery_assembling_usage"));

            // Here: Machinery Breakdown Settings
            ForgeSettings.BASE_BREAKDOWN_CHANCE = Float.parseFloat(settings.getString("forge_base_heavy_machinery_breakdown_chance"));
            ForgeSettings.BREAKDOWN_SEVERITY = Float.parseFloat(settings.getString("forge_heavy_machinery_breakdown_severity"));

            // Here: CR  Settings
            ForgeSettings.CR_PRODUCTION_DECAY = Float.parseFloat(settings.getString("forge_combat_readiness_decay_when_producing"));

            // Here: Special Item Settings
            ForgeSettings.CORRUPTED_NANOFORGE_QUALITY_BONUS = Float.parseFloat(settings.getString("forge_corrupted_nanoforge_quality_bonus"));
            ForgeSettings.PRISTINE_NANOFORGE_QUALITY_BONUS = Float.parseFloat(settings.getString("forge_pristine_nanoforge_quality_bonus"));
            ForgeSettings.CATALYTIC_CORE_REFINING_BONUS = Float.parseFloat(settings.getString("forge_catalytic_core_refining_bonus"));
            ForgeSettings.SYNCHROTRON_CORE_CENTRIFUGING_BONUS = Float.parseFloat(settings.getString("forge_synchrotron_core_centrifuging_bonus"));
            ForgeSettings.CORRUPTED_NANOFORGE_MANUFACTURING_BONUS = Float.parseFloat(settings.getString("forge_corrupted_nanoforge_manufacturing_bonus"));
            ForgeSettings.PRISTINE_NANOFORGE_MANUFACTURING_BONUS = Float.parseFloat(settings.getString("forge_pristine_nanoforge_manufacturing_bonus"));
            ForgeSettings.CORRUPTED_NANOFORGE_ASSEMBLING_BONUS = Float.parseFloat(settings.getString("forge_corrupted_nanoforge_assembling_bonus"));
            ForgeSettings.PRISTINE_NANOFORGE_ASSEMBLING_BONUS = Float.parseFloat(settings.getString("forge_pristine_nanoforge_assembling_bonus"));

    }

}
