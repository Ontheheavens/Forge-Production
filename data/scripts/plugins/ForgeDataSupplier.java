package data.scripts.plugins;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;

public class ForgeDataSupplier {

    public static void loadSettings(String fileName) throws IOException, JSONException {

            JSONObject settings = Global.getSettings().loadJSON(fileName);

            // Here: Miscellaneous Settings
            ForgeSettings.SENSOR_PROFILE_INCREASE = settings.getInt("forge_sensor_profile_increase");
            ForgeSettings.SHIP_LIST_SIZE = settings.getInt("forge_ship_list_size");

            // Here: Notification Settings
            ForgeSettings.PLAY_SOUND_NOTIFICATION = settings.getBoolean("forge_play_sound_notification");
            ForgeSettings.SHOW_NOTIFICATION = settings.getBoolean("forge_show_notification");
            ForgeSettings.NOTIFICATION_INTERVAL = settings.getInt("forge_notification_interval");

            // Here: Refining Settings
            ForgeSettings.ORE_TO_REFINE = settings.getInt("forge_ore_to_refine");
            ForgeSettings.METAL_PRODUCED = settings.getInt("forge_metal_produced");
            ForgeSettings.TRANSPLUTONIC_ORE_TO_REFINE = settings.getInt("forge_transplutonic_ore_to_refine");
            ForgeSettings.TRANSPLUTONICS_PRODUCED = settings.getInt("forge_transplutonics_produced");
            ForgeSettings.HEAVY_MACHINERY_REFINING_USAGE = settings.getInt("forge_heavy_machinery_refining_usage");

            // Here: Centrifuging Settings
            ForgeSettings.VOLATILES_TO_CENTRIFUGE = settings.getInt("forge_volatiles_to_centrifuge");
            ForgeSettings.FUEL_PRODUCED = settings.getInt("forge_fuel_produced");
            ForgeSettings.HEAVY_MACHINERY_CENTRIFUGING_USAGE = settings.getInt("forge_heavy_machinery_centrifuging_usage");

            // Here: Machinery Breakdown Settings
            ForgeSettings.BASE_BREAKDOWN_CHANCE = settings.getDouble("forge_base_heavy_machinery_breakdown_chance");
            ForgeSettings.BREAKDOWN_SEVERITY = settings.getDouble("forge_heavy_machinery_breakdown_severity");

            // Here: CR  Settings
            ForgeSettings.CR_PRODUCTION_DECAY = settings.getDouble("forge_combat_readiness_decay_when_producing");

            // Here: Special Item Settings
            ForgeSettings.CORRUPTED_NANOFORGE_QUALITY_BONUS = settings.getDouble("forge_corrupted_nanoforge_quality_bonus");
            ForgeSettings.PRISTINE_NANOFORGE_QUALITY_BONUS = settings.getDouble("forge_pristine_nanoforge_quality_bonus");
            ForgeSettings.CATALYTIC_CORE_REFINING_BONUS = settings.getInt("forge_catalytic_core_refining_bonus");
            ForgeSettings.SYNCHROTRON_CORE_CENTRIFUGING_BONUS = settings.getInt("forge_synchrotron_core_centrifuging_bonus");

    }

}
