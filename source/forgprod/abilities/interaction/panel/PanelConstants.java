package forgprod.abilities.interaction.panel;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ontheheavens
 * @since 29.12.2022
 */

public class PanelConstants {

    // On-the-spot color creation is to prevent edits to faction colors from affecting panel.
    public static final Color PLAYER_COLOR = new Color(170, 222, 255, 255);
    public static final Color DARK_PLAYER_COLOR = new Color(31, 94, 112, 175);
    public static final Color BRIGHT_PLAYER_COLOR = new Color(203, 245, 255, 255);

    public static final float PANEL_WIDTH = 1200f;
    public static final float PANEL_HEIGHT = 701f;
    public static final float HEADER_HEIGHT = 120f;
    public static final float PANEL_CONTENT_OFFSET = 8f;
    public static final float SPEC_LINE_ICON_OFFSET = -198f;

    public enum ShownTab {
        MODULES,
        CAPACITIES,
    }

    public static final Map<ShownTab, String> TAB_NAMES = new HashMap<>();
    static {
        TAB_NAMES.put(ShownTab.MODULES, "Modules");
        TAB_NAMES.put(ShownTab.CAPACITIES, "Capacities");
    }

}
