package forgprod.abilities.interaction.panel.listeners;

import java.util.HashSet;
import java.util.Set;

import forgprod.abilities.interaction.panel.objects.Button;

/**
 * @author Ontheheavens
 * @since 29.12.2022
 */

public class ButtonListener {

    private static Set<Button> buttonsIndex;

    public static Set<Button> getIndex() {
        if (buttonsIndex == null) {
            buttonsIndex = new HashSet<>();
        }
        return buttonsIndex;
    }

    public static void clearIndex() {
        buttonsIndex = null;
    }

    public static void checkIndex() {
        if (buttonsIndex == null) return;
        for (Button button : buttonsIndex) {
            if (button !=null && button.getInner() != null) {
                button.check();
            }
        }
    }
    
}
