package forgprod.settings;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;

/**
 * Loads values from custom .ini file. Thanks to Schaf-Unschaf for the idea of this one.
 * @author Ontheheavens
 * @since 30.11.2021
 */
public class SettingsDataSupplier {

    public static void loadSettings(String fileName) throws IOException, JSONException {

            JSONObject settings = Global.getSettings().loadJSON(fileName);

            // Here: Installation Settings.

            SettingsHolder.CREW_REQ_PER_CAPACITY = settings.getInt("crew_requirement_per_capacity");
            SettingsHolder.SUPPLY_REQ_PER_CAPACITY = settings.getInt("supply_increase_per_capacity");

            // Here: Miscellaneous Settings
            SettingsHolder.INTERACTION_CALL_HOTKEY = settings.getString("interaction_call_hotkey");

            SettingsHolder.ENABLE_SLOW_MOVE_PENALTY = settings.getBoolean("enable_slow_move_penalty");
            SettingsHolder.ENABLE_DETECT_AT_RANGE_PENALTY = settings.getBoolean("enable_sensor_penalty");

            SettingsHolder.ENABLE_BURN_ABILITIES_INCOMPATIBILITY = settings.getBoolean("enable_burn_abilities_incompatibility");
            SettingsHolder.ENABLE_GO_DARK_INCOMPATIBILITY = settings.getBoolean("enable_go_dark_incompatibility");

            SettingsHolder.SENSOR_PROFILE_INCREASE = settings.getInt("sensor_profile_increase");
            SettingsHolder.SHIP_LIST_SIZE = settings.getInt("ship_list_size");

            SettingsHolder.NOTIFICATION_INTERVAL = settings.getInt("notification_interval");

            // Base capacity by ship size.
            SettingsHolder.BASE_CAPACITY_FRIGATE = settings.getInt("base_capacity_frigate");
            SettingsHolder.BASE_CAPACITY_DESTROYER = settings.getInt("base_capacity_destroyer");
            SettingsHolder.BASE_CAPACITY_CRUISER = settings.getInt("base_capacity_cruiser");
            SettingsHolder.BASE_CAPACITY_CAPITAL = settings.getInt("base_capacity_capital");

            SettingsHolder.GRANULARITY = settings.getInt("granularity");

            // Metals Production values.
            SettingsHolder.BASE_ORE_INPUT = settings.getInt("base_ore_input");
            SettingsHolder.BASE_METAL_OUTPUT = settings.getInt("base_metals_output");
            SettingsHolder.BASE_MACHINERY_USE_METALS = settings.getInt("base_machinery_use_metals");

            // Transplutonics Production values.
            SettingsHolder.BASE_TRANSPLUTONIC_ORE_INPUT = settings.getInt("base_transplutonic_ore_input");
            SettingsHolder.BASE_TRANSPLUTONICS_OUTPUT = settings.getInt("base_transplutonics_output");
            SettingsHolder.BASE_MACHINERY_USE_TRANSPLUTONICS = settings.getInt("base_machinery_use_transplutonics");

            // Fuel Production values.
            SettingsHolder.BASE_VOLATILES_INPUT = settings.getInt("base_volatiles_input");
            SettingsHolder.BASE_FUEL_OUTPUT = settings.getInt("base_fuel_output");
            SettingsHolder.BASE_MACHINERY_USE_FUEL = settings.getInt("base_machinery_use_fuel");

            // Supplies Production values.
            SettingsHolder.BASE_METALS_INPUT_SUPPLIES = settings.getInt("base_metals_input_supplies");
            SettingsHolder.BASE_TRANSPLUTONICS_INPUT_SUPPLIES = settings.getInt("base_transplutonics_input_supplies");
            SettingsHolder.BASE_SUPPLIES_OUTPUT = settings.getInt("base_supplies_output");
            SettingsHolder.BASE_MACHINERY_USE_SUPPLIES = settings.getInt("base_machinery_use_supplies");

            // Machinery Production values.
            SettingsHolder.BASE_METAL_INPUT_MACHINERY = settings.getInt("base_metals_input_machinery");
            SettingsHolder.BASE_TRANSPLUTONICS_INPUT_MACHINERY = settings.getInt("base_transplutonics_input_machinery");
            SettingsHolder.BASE_MACHINERY_OUTPUT = settings.getInt("base_machinery_output");
            SettingsHolder.BASE_MACHINERY_USE_MACHINERY = settings.getInt("base_machinery_use_machinery");

            // Hull Parts Production values.
            SettingsHolder.BASE_METAL_INPUT_HULL_PARTS = settings.getInt("base_metals_input_hull_parts");
            SettingsHolder.BASE_TRANSPLUTONICS_INPUT_HULL_PARTS= settings.getInt("base_transplutonics_input_hull_parts");
            SettingsHolder.BASE_SUPPLIES_INPUT_HULL_PARTS = settings.getInt("base_supplies_input_hull_parts");
            SettingsHolder.BASE_MACHINERY_INPUT_HULL_PARTS = settings.getInt("base_machinery_input_hull_parts");
            SettingsHolder.BASE_HULL_PARTS_OUTPUT = settings.getInt("base_hull_parts_output");
            SettingsHolder.BASE_MACHINERY_USE_HULL_PARTS = settings.getInt("base_machinery_use_hull_parts");

            SettingsHolder.HULL_COST_MULTIPLIER = Float.parseFloat(settings.getString("hull_cost_multiplier"));

            // Machinery Breakdown values.
            SettingsHolder.BASE_BREAKDOWN_CHANCE = Float.parseFloat(settings.getString("machinery_breakdown_chance"));
            SettingsHolder.BREAKDOWN_SEVERITY = Float.parseFloat(settings.getString("machinery_breakdown_severity"));

            SettingsHolder.DAILY_CR_DECREASE = Float.parseFloat(settings.getString("daily_cr_decrease"));

            // Here: Special Item Settings

            SettingsHolder.CATALYTIC_CORE_OUTPUT_BONUS = settings.getInt("catalytic_core_output_bonus");
            SettingsHolder.SYNCHROTRON_CORE_OUTPUT_BONUS = settings.getInt("synchrotron_core_output_bonus");
            SettingsHolder.CORRUPTED_NANOFORGE_OUTPUT_BONUS = settings.getInt("corrupted_nanoforge_output_bonus");
            SettingsHolder.PRISTINE_NANOFORGE_OUTPUT_BONUS = settings.getInt("pristine_nanoforge_output_bonus");

    }

}
