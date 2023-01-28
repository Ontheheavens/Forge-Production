package forgprod.abilities.interaction.panel;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;

import forgprod.abilities.interaction.panel.components.Footer;
import forgprod.abilities.interaction.panel.components.Header;
import forgprod.abilities.interaction.panel.components.Sidebar;
import forgprod.abilities.interaction.panel.components.tabs.ModulesTab;
import forgprod.abilities.interaction.panel.listeners.ButtonListener;

/**
 * @author Ontheheavens
 * @since 29.12.2022
 */

public class ControlPanelAssembly {

    private static List<UIComponentAPI> components;

    private static PanelConstants.ShownTab shownTab;

    private static CustomPanelAPI displayedTabInstance;

    private static final float CONTENT_WIDTH = PanelConstants.PANEL_WIDTH - PanelConstants.PANEL_CONTENT_OFFSET;

    private static void clearPanel(CustomPanelAPI panel) {
        if (components != null) {
            for (UIComponentAPI component : components) {
                if (component == null) continue;
                panel.removeComponent(component);
            }
            components = null;
        }
        clearTab(panel);
        ModulesTab.setModuleCard(null);
        ButtonListener.clearIndex();
    }

    public static void setShownTab(PanelConstants.ShownTab tab) {
        ControlPanelAssembly.shownTab = tab;
    }

    public static PanelConstants.ShownTab getShownTab() {
        return shownTab;
    }

    private static void clearTab(CustomPanelAPI panel) {
        if (displayedTabInstance != null) {
            panel.removeComponent(displayedTabInstance);
        }
        displayedTabInstance = null;
    }

    public static void renderTab(CustomPanelAPI panel, CustomUIPanelPlugin plugin) {
        clearTab(panel);
        switch (ControlPanelAssembly.shownTab) {
            case MODULES:
                displayedTabInstance = ModulesTab.create(panel, CONTENT_WIDTH, plugin);
                break;
            case CAPACITIES:
                break;
        }
    }

    public static void renderPanel(CustomPanelAPI panel, CustomUIPanelPlugin plugin) {
        clearPanel(panel);
        addComponents(panel, plugin);
    }

    private static void addComponents(CustomPanelAPI panel, CustomUIPanelPlugin plugin) {
        components = new ArrayList<>();
        components.add(Header.create(panel, CONTENT_WIDTH));
        ControlPanelAssembly.renderTab(panel, plugin);
        components.add(Sidebar.create(panel, plugin));
        components.add(Footer.create(panel, CONTENT_WIDTH));
    }

}
