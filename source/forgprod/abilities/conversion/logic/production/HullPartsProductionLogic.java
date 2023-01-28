package forgprod.abilities.conversion.logic.production;

import java.util.List;
import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Items;

import forgprod.abilities.conversion.logic.base.ProductionLogic;
import forgprod.abilities.conversion.support.ConversionVariables;
import forgprod.abilities.conversion.support.ProductionType;
import forgprod.abilities.modules.FleetwideModuleManager;
import forgprod.settings.SettingsHolder;

import static forgprod.abilities.conversion.support.ProductionConstants.*;
import static forgprod.abilities.conversion.support.checks.ItemBonusesChecks.hasSpecialItem;
import static forgprod.settings.SettingsHolder.*;

/**
 * Modified version of production logic, supports four input types.
 * @author Ontheheavens
 * @since 16.01.2023
 */

public class HullPartsProductionLogic extends ProductionLogic {

    private static HullPartsProductionLogic logicInstance;
    protected float cycleTertiaryInputAmount;
    protected float cycleQuaternaryInputAmount;
    protected String tertiaryInputType;
    protected String quaternaryInputType;

    protected HullPartsProductionLogic() {
        productionType = ProductionType.HULL_PARTS_PRODUCTION;
        hasSecondaryInput = true;
        cyclePrimaryInputAmount = METALS_INPUT_HULL_PARTS;
        cycleSecondaryInputAmount = TRANSPLUTONICS_INPUT_HULL_PARTS;
        cycleTertiaryInputAmount = SUPPLIES_INPUT_HULL_PARTS;
        cycleQuaternaryInputAmount = MACHINERY_INPUT_HULL_PARTS;
        cycleOutputAmount = HULL_PARTS_OUTPUT;
        cycleMachineryUse = MACHINERY_USAGE.get(productionType);
        primaryInputType = Commodities.METALS;
        secondaryInputType = Commodities.RARE_METALS;
        tertiaryInputType = Commodities.SUPPLIES;
        quaternaryInputType = Commodities.HEAVY_MACHINERY;
        outputType = null;
    }

    public static HullPartsProductionLogic getInstance() {
        if (logicInstance == null) {
            logicInstance = new HullPartsProductionLogic();
        }
        return logicInstance;
    }

    public String getTertiaryInputType() {
        return tertiaryInputType;
    }

    public String getQuaternaryInputType() {
        return quaternaryInputType;
    }

    public float getCycleTertiaryInputAmount() {
        return cycleTertiaryInputAmount;
    }

    public float getCycleQuaternaryInputAmount() {
        return cycleQuaternaryInputAmount;
    }

    @Override
    public void initializeProduction(CampaignFleetAPI fleet) {
        if (isPlayerFleetRosterFull() || !hasOperationalSalvageRigs()) {
            return;
        }
        super.initializeProduction(fleet);
    }

    public boolean isPlayerFleetRosterFull() {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        return playerFleet.getFleetData().getNumMembers() >= Global.getSettings().getMaxShipsInFleet();
    }

    public boolean hasOperationalSalvageRigs() {
        return getActiveSalvageRigsNumber() >= 1;
    }

    public int getActiveSalvageRigsNumber() {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        List<FleetMemberAPI> membersList = playerFleet.getFleetData().getMembersListCopy();
        int count = 0;
        for (FleetMemberAPI member : membersList) {
            if (!member.getHullSpec().getBaseHullId().equals("crig")) {
                continue;
            }
            boolean mothballed = member.getRepairTracker().isMothballed();
            boolean hasMinCR = member.getRepairTracker().getCR() >= 0.2f;
            if (!mothballed || hasMinCR) {
                count += 1;
            }
        }
        return count;
    }

    @Override
    protected boolean hasMinimumCargo(CampaignFleetAPI fleet) {
        boolean hasPrimaryAndSecondaryInputs = (getAvailablePrimaryInputs(fleet) > cyclePrimaryInputAmount &&
                getAvailableSecondaryInputs(fleet) > cycleSecondaryInputAmount);
        boolean hasTertiaryAndQuaternaryInputs = (getAvailableTertiaryInputs(fleet) > cycleTertiaryInputAmount &&
                getAvailableQuaternaryInputs(fleet) > cycleQuaternaryInputAmount);
        return hasPrimaryAndSecondaryInputs && hasTertiaryAndQuaternaryInputs;
    }

    protected float getAvailableTertiaryInputs(CampaignFleetAPI fleet) {
        return fleet.getCargo().getCommodityQuantity(tertiaryInputType);
    }

    protected float getAvailableQuaternaryInputs(CampaignFleetAPI fleet) {
        return fleet.getCargo().getCommodityQuantity(quaternaryInputType);
    }

    @Override
    protected int getPossibleCapacityByCargo(CampaignFleetAPI fleet) {
        float primaryAndSecondaryInputs = Math.min((getAvailablePrimaryInputs(fleet) / cyclePrimaryInputAmount),
                (getAvailableSecondaryInputs(fleet) / cycleSecondaryInputAmount));
        float tertiaryAndQuaternaryInputs = Math.min((getAvailableTertiaryInputs(fleet) / cycleTertiaryInputAmount),
                (getAvailableQuaternaryInputs(fleet) / cycleQuaternaryInputAmount));
        return (int) Math.min(primaryAndSecondaryInputs, tertiaryAndQuaternaryInputs);
    }

    public int getPossibleAssemblingCapacity() {
        float capacityFromOneRig = SettingsHolder.HULL_ASSEMBLING_CAPACITY_PER_RIG * SettingsHolder.GRANULARITY;
        float totalRigsCapacity = this.getActiveSalvageRigsNumber() * capacityFromOneRig;
        return (int) totalRigsCapacity;
    }

    // Hull Parts Production does not receive item bonuses.
    @Override
    public float getCycleOutputBonus(CampaignFleetAPI fleet) {
        return 0f;
    }

    // Should be maximum of 0.8f, so you still get one d-mod and planetary production has more quality.
    public float getHullQuality() {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        float quality = 0.2f; // Base quality for autoforge parts production.
        if (hasOperationalSalvageRigs()) {
            quality += 0.1f; // Quality level of assembling hulls with Rigs.
        }
        if (hasSpecialItem(playerFleet, Items.PRISTINE_NANOFORGE)) {
            quality += PRISTINE_NANOFORGE_BREAKDOWN_DECREASE;
        } else if (hasSpecialItem(playerFleet, Items.CORRUPTED_NANOFORGE)) {
            quality += CORRUPTED_NANOFORGE_BREAKDOWN_DECREASE;
        }
        return quality;
    }

    public float computeDesignatedHullCostInParts() {
        ShipVariantAPI designatedVariant = FleetwideModuleManager.getInstance().getDesignatedVariant();
        return computeHullCostInParts(designatedVariant);
    }

    public float computeHullCostInParts(ShipVariantAPI variant) {
        float designValue = getHullPrice(variant);
        float singlePartValue = getSingleHullPartValue();
        if (variant.getHullSpec().hasTag(Factions.DERELICT)) {
            designValue = designValue * 0.6f;
        } else {
            designValue = designValue * 1.8f;
        }
        if (variant.getHullSpec().getShieldType() != ShieldAPI.ShieldType.NONE) {
            designValue = designValue * 1.2f;
        }
        designValue = designValue * SettingsHolder.HULL_COST_MULTIPLIER;
        return (float) Math.ceil(designValue / singlePartValue);
    }

    public float getHullPrice(ShipVariantAPI designatedVariant) {
        FleetMemberAPI dummyMember = Global.getFactory().createFleetMember(FleetMemberType.SHIP, designatedVariant);
        return dummyMember.getBaseBuyValue();
    }

    private float getSingleHullPartValue() {
        float metalsValue = BASE_METAL_INPUT_HULL_PARTS * getCommodityBaseValue(Commodities.METALS);
        float transplutonicsValue = BASE_TRANSPLUTONICS_INPUT_HULL_PARTS * getCommodityBaseValue(Commodities.RARE_METALS);
        float suppliesValue = BASE_SUPPLIES_INPUT_HULL_PARTS * getCommodityBaseValue(Commodities.SUPPLIES);
        float machineryValue = BASE_MACHINERY_INPUT_HULL_PARTS * getCommodityBaseValue(Commodities.HEAVY_MACHINERY);
        return metalsValue + transplutonicsValue + suppliesValue + machineryValue;
    }

    private float getCommodityBaseValue(String commodityId) {
        return Global.getSettings().getCommoditySpec(commodityId).getBasePrice();
    }

    private boolean attemptHullAssembly() {
        FleetwideModuleManager manager = FleetwideModuleManager.getInstance();
        float hullCost = computeDesignatedHullCostInParts();
        if (hullCost <= manager.getAccumulatedHullParts()) {
            manager.updateAccumulatedHullParts(-hullCost);
            FleetDataAPI fleetData = Global.getSector().getPlayerFleet().getFleetData();
            FleetMemberAPI newHull = Global.getFactory().createFleetMember(FleetMemberType.SHIP, manager.getDesignatedVariant());
            this.addAssembledHullToFleet(newHull, fleetData);
            return true;
        }
        return false;
    }

    private void addAssembledHullToFleet(FleetMemberAPI newHull, FleetDataAPI fleetData) {
        FleetEncounterContext.prepareShipForRecovery(newHull, true, true,
                true, 1f, 1f, new Random());
        newHull.getRepairTracker().setCR(0.4f);
        float averageDMods = DefaultFleetInflater.getAverageDmodsForQuality(getHullQuality());
        Random itemGenRandom = new Random();
        int addDMods = DefaultFleetInflater.getNumDModsToAdd(newHull.getVariant(), averageDMods, itemGenRandom);
        if (addDMods > 0) {
            DModManager.setDHull(newHull.getVariant());
            DModManager.addDMods(newHull, true, addDMods, itemGenRandom);
        }
        fleetData.addFleetMember(newHull);
    }

    @Override
    protected void commenceProduction(CampaignFleetAPI fleet) {
        int cycles = Math.min(getMinimalCapacity(fleet), getPossibleAssemblingCapacity());
        float dailyPrimaryInputsSpent = cyclePrimaryInputAmount * cycles;
        float dailySecondaryInputsSpent = cycleSecondaryInputAmount * cycles;
        float dailyTertiaryInputsSpent = cycleTertiaryInputAmount * cycles;
        float dailyQuaternaryInputsSpent = cycleQuaternaryInputAmount * cycles;
        float dailyOutputProduced = cycleOutputAmount * cycles;
        float dailyMachineryUsed = cycleMachineryUse * cycles;
        CargoAPI cargo = fleet.getCargo();
        cargo.removeCommodity(primaryInputType, dailyPrimaryInputsSpent);
        cargo.removeCommodity(secondaryInputType, dailySecondaryInputsSpent);
        cargo.removeCommodity(tertiaryInputType, dailyTertiaryInputsSpent);
        cargo.removeCommodity(quaternaryInputType, dailyQuaternaryInputsSpent);
        FleetwideModuleManager manager = FleetwideModuleManager.getInstance();
        manager.updateAccumulatedHullParts(dailyOutputProduced);
        float partsUpdatedAmount;
        int hullsAssembled = 0;
        do {
            if (attemptHullAssembly()) {
                hullsAssembled++;
            }
            partsUpdatedAmount = manager.getAccumulatedHullParts();
        } while (partsUpdatedAmount > computeDesignatedHullCostInParts());
        this.setReportFlags(hullsAssembled);
        this.addMachineryUsed(dailyMachineryUsed);
        this.setModuleFlags();
    }

    @Override
    protected void setReportFlags(float amountProduced) {
        ConversionVariables.hullPartsDailyFlag = true;
        ConversionVariables.totalHullsProduced += amountProduced;
    }

}
