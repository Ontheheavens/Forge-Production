package forgprod.abilities.conversion.support;

import java.util.HashSet;
import java.util.Set;

/**
 * Storage of temporary production flags and values.
 * <p>
 *
 * @author Ontheheavens
 * @since 30.11.2021
 */

public class ConversionVariables {

    // Here: Daily flags, used and cleared each time production event happens.
    // Set to true only if their specific production happened, otherwise stay false.
    // Used for applying CR malus to ships and displaying float notes/ playing sounds.

    public static boolean metalsDailyFlag = false;
    public static boolean transplutonicsDailyFlag = false;
    public static boolean fuelDailyFlag = false;
    public static boolean suppliesDailyFlag = false;
    public static boolean machineryDailyFlag = false;
    public static boolean hullPartsDailyFlag = false;

    public static boolean hasGoodsDailyFlags() {
        Set<Boolean> goodsDailyFlag = new HashSet<>();
        goodsDailyFlag.add(metalsDailyFlag);
        goodsDailyFlag.add(transplutonicsDailyFlag);
        goodsDailyFlag.add(fuelDailyFlag);
        goodsDailyFlag.add(suppliesDailyFlag);
        goodsDailyFlag.add(machineryDailyFlag);
        goodsDailyFlag.add(hullPartsDailyFlag);
        for (boolean produced : goodsDailyFlag) {
            if (produced) { return true; }
        }
        return false;
    }

    public static void clearGoodsDailyFlags() {
        metalsDailyFlag = false;
        transplutonicsDailyFlag = false;
        fuelDailyFlag = false;
        suppliesDailyFlag = false;
        machineryDailyFlag = false;
        hullPartsDailyFlag = false;
    }

    // Here: Production report variables, accrue each time production event happens.
    // Used for production report, cleared after report is displayed.

    public static float totalMetalsProduced = 0f;
    public static float totalTransplutonicsProduced = 0f;
    public static float totalFuelProduced = 0f;
    public static float totalSuppliesProduced = 0f;
    public static float totalMachineryProduced = 0f;
    public static float totalHullsProduced = 0f;
    public static float totalMachineryBroke = 0f;
    public static float totalCrewmenLost = 0f;

    public static boolean goodsProducedReport() {
        Set<Float> totalGoodsTally = new HashSet<>();
        totalGoodsTally.add(totalMetalsProduced);
        totalGoodsTally.add(totalTransplutonicsProduced);
        totalGoodsTally.add(totalFuelProduced);
        totalGoodsTally.add(totalSuppliesProduced);
        totalGoodsTally.add(totalMachineryProduced);
        totalGoodsTally.add(totalHullsProduced);
        totalGoodsTally.add(totalMachineryBroke);
        totalGoodsTally.add(totalCrewmenLost);
        for (float produced : totalGoodsTally) {
            if (produced >= 1f) {
                return true;
            }
        }
        return false;
    }

    public static void clearReportVariables() {
        totalMetalsProduced = 0f;
        totalTransplutonicsProduced = 0f;
        totalFuelProduced = 0f;
        totalSuppliesProduced = 0f;
        totalMachineryProduced = 0f;
        totalHullsProduced = 0f;
        totalMachineryBroke = 0f;
        totalCrewmenLost = 0f;
    }

}
