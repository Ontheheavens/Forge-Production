package data.forge.abilities.conversion.support;

import data.forge.plugins.ForgeSettings;

import java.util.HashMap;
import java.util.Map;

public class ForgeTypes {

    public static final String REFINERY = "forge_refinery_module";
    public static final String CENTRIFUGE = "forge_centrifuge_module";
    public static final String MANUFACTURE = "forge_manufacture_module";
    public static final String ASSEMBLY = "forge_assembly_module";

    public enum Conversion {
        REFINING,
        CENTRIFUGING,
        MANUFACTURING,
        ASSEMBLING,
    }

    public static final Map<Conversion, Float> MACHINERY_USAGE = new HashMap<>();
    static {
        MACHINERY_USAGE.put(Conversion.REFINING, ForgeSettings.HEAVY_MACHINERY_REFINING_USAGE);
        MACHINERY_USAGE.put(Conversion.CENTRIFUGING, ForgeSettings.HEAVY_MACHINERY_CENTRIFUGING_USAGE);
        MACHINERY_USAGE.put(Conversion.MANUFACTURING, ForgeSettings.HEAVY_MACHINERY_MANUFACTURING_USAGE);
        MACHINERY_USAGE.put(Conversion.ASSEMBLING, ForgeSettings.HEAVY_MACHINERY_ASSEMBLING_USAGE);
    }

}
