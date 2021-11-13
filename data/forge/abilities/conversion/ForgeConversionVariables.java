package data.forge.abilities.conversion;

public class ForgeConversionVariables {

    // Here: Production report variables

    public static float totalMetalProduction = 0f;
    public static float totalTransplutonicsProduction = 0f;
    public static float totalFuelProduction = 0f;
    public static float totalSuppliesProduction = 0f;
    public static float totalHeavyMachineryProduction = 0f;

    public static float totalMachineryBreakage = 0f;

    public static boolean breakdownReport = false;

    // Here: Daily floating/sound notification variables

    public static boolean oreWasRefined = false;
    public static boolean transplutonicsWereRefined = false;
    public static boolean fuelWasCentrifuged = false;
    public static boolean suppliesWereManufactured = false;
    public static boolean heavyMachineryWasAssembled = false;

    public static boolean goodsWereForged () {
        return  oreWasRefined ||
                transplutonicsWereRefined ||
                fuelWasCentrifuged ||
                suppliesWereManufactured ||
                heavyMachineryWasAssembled;
    }

    public static void clearGoodsStatus () {
        oreWasRefined = false;
        transplutonicsWereRefined = false;
        fuelWasCentrifuged = false;
        suppliesWereManufactured = false;
        heavyMachineryWasAssembled = false;
    }

    // Here: Detailed intel report variables

    public static boolean oreRefiningReport = false;
    public static boolean transplutonicsRefiningReport = false;
    public static boolean fuelCentrifugingReport = false;
    public static boolean suppliesManufacturingReport = false;
    public static boolean heavyMachineryAssemblingReport = false;

    public static boolean goodsProducedReport () {
        return  oreRefiningReport ||
                transplutonicsRefiningReport ||
                fuelCentrifugingReport ||
                suppliesManufacturingReport ||
                heavyMachineryAssemblingReport;
    }

}
