package forgprod.abilities.effects;

import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberViewAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;

import forgprod.abilities.conversion.support.ConversionVariables;
import forgprod.abilities.modules.FleetwideModuleManager;
import forgprod.abilities.modules.dataholders.ProductionModule;
import forgprod.abilities.conversion.support.checks.ModuleConditionChecks;
import forgprod.settings.SettingsHolder;

/**
 * @author Ontheheavens
 * @since 23.11.2022
 */
public class AbilityEffects {

    private static final Color CONTRAIL_COLOR = new Color(65, 45, 30, 255);

    public static void applyCRDecrease() {
        FleetwideModuleManager managerInstance = FleetwideModuleManager.getInstance();
        Set<ProductionModule> fleetModules = new HashSet<>(managerInstance.getModuleIndex().values());
        for (ProductionModule module : fleetModules) {
            if (module == null) {
                continue;
            }
            if (module.hadProducedToday()) {
                module.getParentFleetMember().getRepairTracker().applyCREvent(-SettingsHolder.DAILY_CR_DECREASE, "Forged goods");
                module.setProducedToday(false);
            }
        }
    }

    public static void shiftFleetContrails(CampaignFleetAPI fleet, String modId, float level) {
        for (FleetMemberViewAPI view : fleet.getViews()) {
            FleetMemberAPI member = view.getMember();
            if ((ModuleConditionChecks.hasActiveModule(member) && ModuleConditionChecks.isOperational(member))) {
                view.getContrailColor().shift(modId, CONTRAIL_COLOR, 1f, 1f, 0.5f * level);
                view.getContrailWidthMult().shift(modId, 2f, 1f, 1f, level);
                view.getContrailDurMult().shift(modId, 1.2f, 1f, 1f, level);
                view.getEngineGlowSizeMult().shift(modId, 1.5f, 1f, 1f, level);
                view.getEngineHeightMult().shift(modId, 1.5f, 1f, 1f, level);
                view.getEngineWidthMult().shift(modId, 2f, 1f, 1f, level);
            }
        }
    }

    public static void playProductionEffects (CampaignFleetAPI fleet) {

        List<Pair<Boolean, String>> refinedGoods = new ArrayList<>();
        refinedGoods.add(0, new Pair<>(ConversionVariables.metalsDailyFlag, "Produced metals"));
        refinedGoods.add(1,new Pair<>(ConversionVariables.transplutonicsDailyFlag, "Produced transplutonics"));
        refinedGoods.add(2,new Pair<>(ConversionVariables.fuelDailyFlag, "Produced fuel"));
        refinedGoods.add(3,new Pair<>(ConversionVariables.suppliesDailyFlag, "Produced supplies"));
        refinedGoods.add(4,new Pair<>(ConversionVariables.machineryDailyFlag, "Produced machinery"));
        refinedGoods.add(5, new Pair<>(ConversionVariables.hullPartsDailyFlag, "Produced hull parts"));

        int refinedCount = 0;
        String validMessage = "";
        for (Pair<Boolean, String> goodRefined : refinedGoods) {
            if (goodRefined.one) {
                ++refinedCount;
                validMessage = goodRefined.two;
            }
        }
        String resultMessage = "Produced goods";
        if (refinedCount <= 1) {
            resultMessage = validMessage;
        }
        fleet.addFloatingText(resultMessage, Misc.setAlpha(Misc.getTextColor(), 255), 0.5f);

        refinedGoods.set(0, new Pair<>(ConversionVariables.metalsDailyFlag, "ui_cargo_metals_drop"));
        refinedGoods.set(1, new Pair<>(ConversionVariables.transplutonicsDailyFlag, "ui_cargo_raremetals_drop"));
        refinedGoods.set(2, new Pair<>(ConversionVariables.fuelDailyFlag, "ui_cargo_fuel_drop"));
        refinedGoods.set(3, new Pair<>(ConversionVariables.suppliesDailyFlag, "ui_cargo_supplies_drop"));
        refinedGoods.set(4, new Pair<>(ConversionVariables.machineryDailyFlag, "ui_cargo_machinery_drop"));
        refinedGoods.set(5, new Pair<>(ConversionVariables.hullPartsDailyFlag, "ui_cargo_machinery_drop"));

        for (int index = 5; index >= 0; --index ) {
            Pair<Boolean, String> producedGood = refinedGoods.get(index);
            if (producedGood.one) {
                Global.getSoundPlayer().playSound(producedGood.two, 1f, 1f, fleet.getLocation(), new Vector2f());
                break;
            }
        }
    }

}
