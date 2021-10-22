package data.plugins;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

public class FPPlugin extends BaseModPlugin {
    
    public static final String ABILITY_ID = "forge_production";

    public static float SENSOR_PROFILE_INCREASE_PERCENT = 20f;

    @Override
    public void afterGameSave() {
        Global.getSector().getCharacterData().addAbility(ABILITY_ID);
    }

    @Override
    public void beforeGameSave() {
        Global.getSector().getCharacterData().removeAbility(ABILITY_ID);
    }

    @Override
    public void onGameLoad(boolean newGame) {
        try {
            if(!Global.getSector().getPlayerFleet().hasAbility(ABILITY_ID)) {
                Global.getSector().getCharacterData().addAbility(ABILITY_ID);
            }

            SENSOR_PROFILE_INCREASE_PERCENT = Global.getSettings().getFloat("ForgeProductionSensorProfileIncreasePercent");

        } catch (Exception e) {
            StringBuilder stackTrace = new StringBuilder();

            for(int i = 0; i < e.getStackTrace().length; i++) {
                StackTraceElement ste = e.getStackTrace()[i];
                stackTrace.append("    ").append(ste.toString()).append(System.lineSeparator());
            }

            Global.getLogger(FPPlugin.class).error(e.getMessage() + System.lineSeparator() + stackTrace);
        }
    }
}
