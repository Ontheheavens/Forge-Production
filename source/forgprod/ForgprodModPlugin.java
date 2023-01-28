package forgprod;

import org.json.JSONException;

import java.io.IOException;

import com.fs.starfarer.api.BaseModPlugin;

import forgprod.settings.SettingsDataSupplier;

@SuppressWarnings("unused")
public final class ForgprodModPlugin extends BaseModPlugin {

    public static final String FORGE_SETTINGS = "forge_production_settings.ini";

    @Override
    public void onApplicationLoad() throws JSONException, IOException {
        SettingsDataSupplier.loadSettings(FORGE_SETTINGS);
    }

}
