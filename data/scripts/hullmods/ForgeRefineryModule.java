package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import data.scripts.campaign.ForgeShipEligibilityChecker;

public class ForgeRefineryModule extends BaseHullMod {

    public void advanceInCampaign(FleetMemberAPI member, float amount) {

        if (member.getFleetData() == null) return;
        if (member.getFleetData().getFleet() == null) return;
        if (!member.getFleetData().getFleet().isPlayerFleet()) return;
        if (!ForgeShipEligibilityChecker.isValid(member)) return;

        CampaignFleetAPI fleet = member.getFleetData().getFleet();

    }

}
