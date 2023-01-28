package forgprod.abilities.conversion.support;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fs.starfarer.api.combat.ShipAPI;

import forgprod.settings.SettingsHolder;

import static forgprod.settings.SettingsHolder.*;

public class ProductionConstants {

    public static final String REFINING_MODULE = "forgprod_refining_module";
    public static final String FUEL_PRODUCTION_MODULE = "forgprod_fuel_production_module";
    public static final String HEAVY_INDUSTRY_MODULE = "forgprod_heavy_industry_module";

    public static float ORE_INPUT = ((float) BASE_ORE_INPUT / GRANULARITY);
    public static float METAL_OUTPUT = ((float) BASE_METAL_OUTPUT / GRANULARITY);

    public static float TRANSPLUTONIC_ORE_INPUT = ((float) BASE_TRANSPLUTONIC_ORE_INPUT / GRANULARITY);
    public static float TRANSPLUTONICS_OUTPUT = ((float) BASE_TRANSPLUTONICS_OUTPUT / GRANULARITY);

    public static float VOLATILES_INPUT = ((float) BASE_VOLATILES_INPUT / GRANULARITY);
    public static float FUEL_OUTPUT = ((float) BASE_FUEL_OUTPUT / GRANULARITY);

    public static float METALS_INPUT_SUPPLIES = ((float) BASE_METALS_INPUT_SUPPLIES / GRANULARITY);
    public static float TRANSPLUTONICS_INPUT_SUPPLIES = ((float) BASE_TRANSPLUTONICS_INPUT_SUPPLIES / GRANULARITY);
    public static float SUPPLIES_OUTPUT = ((float) BASE_SUPPLIES_OUTPUT / GRANULARITY);

    public static float METALS_INPUT_MACHINERY = ((float) BASE_METAL_INPUT_MACHINERY / GRANULARITY);
    public static float TRANSPLUTONICS_INPUT_MACHINERY = ((float) BASE_TRANSPLUTONICS_INPUT_MACHINERY / GRANULARITY);
    public static float MACHINERY_OUTPUT = ((float) BASE_MACHINERY_OUTPUT / GRANULARITY);

    public static float METALS_INPUT_HULL_PARTS = ((float) BASE_METAL_INPUT_HULL_PARTS / GRANULARITY);
    public static float TRANSPLUTONICS_INPUT_HULL_PARTS = ((float) BASE_TRANSPLUTONICS_INPUT_HULL_PARTS / GRANULARITY);
    public static float SUPPLIES_INPUT_HULL_PARTS = ((float) BASE_SUPPLIES_INPUT_HULL_PARTS / GRANULARITY);
    public static float MACHINERY_INPUT_HULL_PARTS = ((float) BASE_MACHINERY_INPUT_HULL_PARTS / GRANULARITY);
    public static float HULL_PARTS_OUTPUT = ((float) BASE_HULL_PARTS_OUTPUT / GRANULARITY);

    public static final String REFINING_MODULE_DESC = "Machinery and infrastructure needed for refining processes, " +
            "including dedicated purification complex and universal auto-smelter.";
    public static final String FUEL_PRODUCTION_MODULE_DESC = "High-tech isotope processing facility " +
            "and particle deceleration plant for small-scale starship fuel synthesis.";
    public static final String HEAVY_INDUSTRY_MODULE_DESC = "Multi-purpose autoforge array capable of " +
            "producing spaceship supplies, hull parts and heavy industrial equipment.";

    public static final Map<String, String> HULLMOD_DESCRIPTIONS = new HashMap<>();
    static {
        HULLMOD_DESCRIPTIONS.put(ProductionConstants.REFINING_MODULE, REFINING_MODULE_DESC);
        HULLMOD_DESCRIPTIONS.put(ProductionConstants.FUEL_PRODUCTION_MODULE, FUEL_PRODUCTION_MODULE_DESC);
        HULLMOD_DESCRIPTIONS.put(ProductionConstants.HEAVY_INDUSTRY_MODULE, HEAVY_INDUSTRY_MODULE_DESC);
    }

    public static final Map<ProductionType, String> HULLMOD_BY_TYPE = new HashMap<>();
    static {
        HULLMOD_BY_TYPE.put(ProductionType.METALS_PRODUCTION, REFINING_MODULE);
        HULLMOD_BY_TYPE.put(ProductionType.TRANSPLUTONICS_PRODUCTION, REFINING_MODULE);
        HULLMOD_BY_TYPE.put(ProductionType.FUEL_PRODUCTION, FUEL_PRODUCTION_MODULE);
        HULLMOD_BY_TYPE.put(ProductionType.SUPPLIES_PRODUCTION, HEAVY_INDUSTRY_MODULE);
        HULLMOD_BY_TYPE.put(ProductionType.MACHINERY_PRODUCTION, HEAVY_INDUSTRY_MODULE);
        HULLMOD_BY_TYPE.put(ProductionType.HULL_PARTS_PRODUCTION, HEAVY_INDUSTRY_MODULE);
    }

    public static final Map<ProductionType, Float> MACHINERY_USAGE = new HashMap<>();
    static {
        MACHINERY_USAGE.put(ProductionType.METALS_PRODUCTION, ((float)BASE_MACHINERY_USE_METALS / GRANULARITY));
        MACHINERY_USAGE.put(ProductionType.TRANSPLUTONICS_PRODUCTION, ((float)BASE_MACHINERY_USE_TRANSPLUTONICS / GRANULARITY));
        MACHINERY_USAGE.put(ProductionType.FUEL_PRODUCTION, ((float)BASE_MACHINERY_USE_FUEL / GRANULARITY));
        MACHINERY_USAGE.put(ProductionType.SUPPLIES_PRODUCTION, ((float)BASE_MACHINERY_USE_SUPPLIES / GRANULARITY));
        MACHINERY_USAGE.put(ProductionType.MACHINERY_PRODUCTION, ((float)BASE_MACHINERY_USE_MACHINERY / GRANULARITY));
        MACHINERY_USAGE.put(ProductionType.HULL_PARTS_PRODUCTION, ((float) BASE_MACHINERY_USE_HULL_PARTS / GRANULARITY));
    }

    public static final Set<String> ALL_HULLMODS = new HashSet<>();
    static {
        ALL_HULLMODS.add(ProductionConstants.REFINING_MODULE);
        ALL_HULLMODS.add(ProductionConstants.FUEL_PRODUCTION_MODULE);
        ALL_HULLMODS.add(ProductionConstants.HEAVY_INDUSTRY_MODULE);
    }

    public static final Map<String, String> HULLMOD_NAMES = new HashMap<>();
    static {
        HULLMOD_NAMES.put(ProductionConstants.REFINING_MODULE, "Refining Module");
        HULLMOD_NAMES.put(ProductionConstants.FUEL_PRODUCTION_MODULE, "Fuel Production Module");
        HULLMOD_NAMES.put(ProductionConstants.HEAVY_INDUSTRY_MODULE, "Heavy Industry Module");
    }

    public static final Map<ProductionType, String> PRODUCTION_NAMES = new HashMap<>();
    static {
        PRODUCTION_NAMES.put(ProductionType.METALS_PRODUCTION, "Metals");
        PRODUCTION_NAMES.put(ProductionType.TRANSPLUTONICS_PRODUCTION, "Transplutonics");
        PRODUCTION_NAMES.put(ProductionType.FUEL_PRODUCTION, "Fuel");
        PRODUCTION_NAMES.put(ProductionType.SUPPLIES_PRODUCTION, "Supplies");
        PRODUCTION_NAMES.put(ProductionType.MACHINERY_PRODUCTION, "Machinery");
        PRODUCTION_NAMES.put(ProductionType.HULL_PARTS_PRODUCTION, "Hull Parts");
    }

    public static Map<ShipAPI.HullSize, Integer> SHIPSIZE_CAPACITY = new HashMap<>();
    static {

        SHIPSIZE_CAPACITY.put(ShipAPI.HullSize.FRIGATE, SettingsHolder.BASE_CAPACITY_FRIGATE);
        SHIPSIZE_CAPACITY.put(ShipAPI.HullSize.DESTROYER, SettingsHolder.BASE_CAPACITY_DESTROYER);
        SHIPSIZE_CAPACITY.put(ShipAPI.HullSize.CRUISER, SettingsHolder.BASE_CAPACITY_CRUISER);
        SHIPSIZE_CAPACITY.put(ShipAPI.HullSize.CAPITAL_SHIP, SettingsHolder.BASE_CAPACITY_CAPITAL);

    }

}
