package data.forge.plugins;

public class ForgeSettings {

        // Here: Miscellaneous Settings
        public static boolean ENABLE_PENALTIES = true;
        public static float SENSOR_PROFILE_INCREASE = 100;  // Unused for now
        public static int SHIP_LIST_SIZE = 5;

        // Here: Notification  Settings
        public static boolean PLAY_SOUND_NOTIFICATION = true;
        public static boolean SHOW_NOTIFICATION = true;
        public static int NOTIFICATION_INTERVAL = 3;

        // Here: Capacity Settings
        public static int CAPACITY_CRUISER = 40;
        public static int CAPACITY_CAPITAL = 60;

        // Here: Refining  Settings
        public static float ORE_TO_REFINE = 2f;
        public static float METAL_PRODUCED = 1.2f;
        public static float TRANSPLUTONIC_ORE_TO_REFINE = 1f;
        public static float TRANSPLUTONICS_PRODUCED = 0.6f;
        public static float HEAVY_MACHINERY_REFINING_USAGE = 0.6f;

        // Here: Centrifuging Settings
        public static float VOLATILES_TO_CENTRIFUGE = 0.1f;
        public static float FUEL_PRODUCED = 1.2f;
        public static float HEAVY_MACHINERY_CENTRIFUGING_USAGE = 0.8f;

        // Here: Manufacturing Settings
        public static float METAL_TO_MANUFACTURE = 0.8f;
        public static float TRANSPLUTONICS_TO_MANUFACTURE = 0.2f;
        public static float SUPPLIES_PRODUCED = 0.4f;
        public static float HEAVY_MACHINERY_MANUFACTURING_USAGE = 1f;

        // Here: Assembling Settings
        public static float METAL_TO_ASSEMBLE = 1.2f;
        public static float TRANSPLUTONICS_TO_ASSEMBLE = 0.1f;
        public static float HEAVY_MACHINERY_PRODUCED = 0.2f;
        public static float HEAVY_MACHINERY_ASSEMBLING_USAGE = 1.6f;

        // Here: Machinery Breakdown  Settings
        public static float BASE_BREAKDOWN_CHANCE = 0.3f;
        public static float BREAKDOWN_SEVERITY = 0.05f;

        // Here: CR  Settings
        public static float CR_PRODUCTION_DECAY = 0.02f;

        // Here: Special Item Settings
        public static float CORRUPTED_NANOFORGE_QUALITY_BONUS = 0.8f;
        public static float PRISTINE_NANOFORGE_QUALITY_BONUS = 0.5f;
        public static float CATALYTIC_CORE_REFINING_BONUS = 0.2f;
        public static float SYNCHROTRON_CORE_CENTRIFUGING_BONUS = 0.4f;
        public static float CORRUPTED_NANOFORGE_MANUFACTURING_BONUS = 0.1f;
        public static float PRISTINE_NANOFORGE_MANUFACTURING_BONUS = 0.2f;
        public static float CORRUPTED_NANOFORGE_ASSEMBLING_BONUS = 0.05f;
        public static float PRISTINE_NANOFORGE_ASSEMBLING_BONUS = 0.1f;

}
