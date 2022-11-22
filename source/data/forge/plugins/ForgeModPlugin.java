package data.forge.plugins;

import java.io.IOException;
import org.json.JSONException;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

public class ForgeModPlugin extends BaseModPlugin {

    public static final String FORGE_SETTINGS = "forge_production_settings.ini";
    public static final String FORGE_PRODUCTION_ABILITY = "forge_production";

    @Override
    public void onApplicationLoad() throws JSONException, IOException {
        ForgeDataSupplier.loadSettings(FORGE_SETTINGS);
    }

    @Override
    public void onGameLoad(boolean newGame) {

        if(!Global.getSector().getPlayerFleet().hasAbility(FORGE_PRODUCTION_ABILITY)) {
        Global.getSector().getCharacterData().addAbility(FORGE_PRODUCTION_ABILITY);
        }
    }

}
