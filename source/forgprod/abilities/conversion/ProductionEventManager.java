package forgprod.abilities.conversion;

import forgprod.abilities.conversion.logic.*;
import forgprod.abilities.conversion.logic.base.ProductionLogic;
import forgprod.abilities.conversion.support.ProductionType;
import forgprod.abilities.effects.AbilityEffects;
import forgprod.abilities.modules.FleetwideModuleManager;
import forgprod.abilities.report.ForgeProductionReport;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.util.IntervalUtil;

import forgprod.abilities.conversion.support.ConversionVariables;
import forgprod.settings.SettingsHolder;

/**
 * @author Ontheheavens
 * @since 23.11.2022
 */

public class ProductionEventManager {

    private static ProductionEventManager managerInstance;
    private Integer firstDay = null;
    private boolean firstDayDone = false;
    private int notificationCounter = 0;

    // Exactly one day, having a setting for this tracker is pointless,
    // since entire logic is based on the assumption of daily production cycle.
    private final IntervalUtil productionTracker = new IntervalUtil(1f, 1f);


    public static ProductionEventManager getInstance() {
        if (managerInstance == null) {
            managerInstance = new ProductionEventManager();
        }
        return managerInstance;
    }

    public int getNotificationCounter() {
        return notificationCounter;
    }

    // The idea here is to have production event happen precisely when the new day comes.
    // Since it is unreasonable to wait almost two full days if the ability button was pushed at the start of the day,
    // or, on the other hand, receive production after half a day if ability was toggled late,
    // the logic had to be made a bit more complicated.
    public void advance(CampaignFleetAPI fleet, float amount) {
        CampaignClockAPI clock = Global.getSector().getClock();
        int presentDay = clock.getDay();
        int hour = clock.getHour();
        // Four hours is the approximate time when day progress bar grows for one increment.
        // Therefore, ability user has about four hours window after start of the day to commence forging,
        // if he wants the day to be productive.
        if (firstDay == null && hour >= 4) {
            firstDay = presentDay;
            firstDayDone = true;
            productionTracker.setElapsed(0);
        } else if (firstDay == null) {
            firstDay = presentDay;
            productionTracker.setElapsed(0);
            firstDayDone = false;
        }
        // This is extra production cycle for the first productive day.
        if ((firstDay == presentDay) && !firstDayDone && (hour == 23)) {
            firstDayDone = true;
            startProductionEvent(fleet);
        }
        // Normal production cycle advancement, starts as soon as the next day after ability toggle comes,
        // continues until ability toggle-off.
        if (!(firstDay == presentDay)) {
            productionTracker.advance(Global.getSector().getClock().convertToDays(amount));
        }
        if (productionTracker.getElapsed() >= productionTracker.getIntervalDuration()) {
            productionTracker.setElapsed(0);
            startProductionEvent(fleet);
        }
    }

    private void startProductionEvent(CampaignFleetAPI fleet) {
        FleetwideModuleManager.getInstance().refreshIndexes(fleet);
        for (ProductionType type : ProductionType.values()) {
            ProductionLogic logic = LogicFactory.getLogic(type);
            logic.initializeProduction(fleet);
        }
        BreakdownLogic.getInstance().calculateBreakdowns(fleet);
        AbilityEffects.applyCRDecrease();
        if (ConversionVariables.hasGoodsDailyFlags()) {
            AbilityEffects.playProductionEffects(fleet);
            ConversionVariables.clearGoodsDailyFlags();
        }
        if (ConversionVariables.goodsProducedReport()) {
            notificationCounter += 1;
        }
        boolean notificationCounterElapsed = (notificationCounter >= SettingsHolder.NOTIFICATION_INTERVAL);
        if (notificationCounterElapsed) {
            addReportMessage();
            notificationCounter = 0;
        }
    }

    public void clearTracker() {
        productionTracker.setElapsed(0);
        firstDay = null;
        firstDayDone = false;
        // Immediate notification firing and cleanup so that variables do not carry over to the next ability usage.
        if (notificationCounter >= 1) {
            addReportMessage();
            notificationCounter = 0;
        }
    }

    private static void addReportMessage() {
        ForgeProductionReport intel = new ForgeProductionReport();
        Global.getSector().getCampaignUI().addMessage(intel);
    }

}
