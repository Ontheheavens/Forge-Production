package data.campaign.abilities;

import java.awt.Color;

import com.fs.starfarer.api.campaign.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberViewAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.plugins.FPPlugin;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class ForgeProduction extends BaseToggleAbility {

	public static final Color CONTRAIL_COLOR = new Color(255, 97, 27, 80);

    private final boolean playSFX = Global.getSettings().getBoolean("yrex_forge_play_sfx");

    @Override
    protected String getActivationText() {
    return null;
    }

    @Override
    protected void activateImpl() { }

    @Override
    public boolean showActiveIndicator() { return isActive(); }

    @Override
    protected void applyEffect(float amount, float level) {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;

        if(!isActive()) return;

        fleet.getStats().getDetectedRangeMod().modifyPercent(getModId(), FPPlugin.SENSOR_PROFILE_INCREASE_PERCENT, "Forge Production");

        for (FleetMemberViewAPI view : getFleet().getViews()) {
            view.getContrailColor().shift("forge_production", CONTRAIL_COLOR, getActivationDays(), 2, 1f);
            view.getContrailWidthMult().shift("forge_production", 6, getActivationDays(), 2, 1f);
        }


    }

    //    if(playSFX) Global.getSoundPlayer().playSound("ui_cargo_metals", 1f, 1f, fleet.getLocation(), ZERO);

    protected List<FleetMemberAPI> getforgeShipsInFleet() {

        List<String> forgeHullmod = new ArrayList<>();

        {
            forgeHullmod.add("yrex_FuelForge");
            forgeHullmod.add("yrex_SuppliesForge");
            forgeHullmod.add("yrex_LobsterPot");
            forgeHullmod.add("yrex_RefineryForge");
        }

        List<FleetMemberAPI> membersWithHullMod = new ArrayList<>();

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        for (FleetMemberAPI forgeShip : playerFleet.getFleetData().getMembersListCopy())
            if (!Collections.disjoint(forgeShip.getVariant().getHullMods(), forgeHullmod))
             membersWithHullMod.add(forgeShip);
        
        return membersWithHullMod;
      }

      

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        //Color gray = Misc.getGrayColor();
        Color highlight = Misc.getHighlightColor();

        String status = " (off)";
        if (turnedOn) {
                status = " (on)";
        }

        LabelAPI title = tooltip.addTitle(spec.getName() + status);
        title.highlightLast(status);
        title.setHighlightColor(highlight);

        float pad = 10f;
        tooltip.addPara("Control forge production of your fleet.", pad);

            tooltip.addPara("The following ships in your fleet have forging capabilities:", pad);

            List<FleetMemberAPI> forgeShips = getforgeShipsInFleet();

            float currPad = 3.0F;
            String indent = "    ";
            for (FleetMemberAPI ship : forgeShips) {
                
                LabelAPI label = tooltip.addPara(indent + " %s",
                        currPad,
                        Misc.getHighlightColor(),
                        ship.getShipName() + " [" + ship.getHullSpec().getHullNameWithDashClass() + "]");
                
                currPad = 0.0F;
              } 

        addIncompatibleToTooltip(tooltip, expanded);
    }

    @Override
    public boolean hasTooltip() { return true; }


    @Override
    public boolean isUsable() {
        //return isActive();
        return true;
    }
    
    @Override
    protected void deactivateImpl() { cleanupImpl(); }

    @Override
    protected void cleanupImpl() {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;
        
        fleet.getStats().getDetectedRangeMod().unmodify(getModId());
    }
}