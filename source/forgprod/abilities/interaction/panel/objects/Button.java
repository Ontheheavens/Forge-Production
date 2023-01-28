package forgprod.abilities.interaction.panel.objects;

import com.fs.starfarer.api.ui.ButtonAPI;

import forgprod.abilities.interaction.panel.listeners.ButtonListener;

/**
 * Container for ButtonAPI. Provides storage for custom parameters.
 * @author Ontheheavens
 * @since 29.12.2022
 */

public abstract class Button {

    public enum Type {
        STANDARD,
        MODULE,
        MODULE_MODE,
        TAB,
        BLUEPRINT,
        DISPLAY_TAG,
    }

    private final ButtonAPI instance;
    private final Type type;

    public Button(ButtonAPI button, Type type) {
        this.instance = button;
        this.type = type;
        ButtonListener.getIndex().add(this);
    }

    public Type getType() {
        return type;
    }

    public ButtonAPI getInner() {
        return instance;
    }

    public void applyEffect() {}

    public void check() {
        ButtonAPI instance = this.getInner();
        if (instance.isChecked()) {
            instance.setChecked(false);
           this.applyEffect();
        }
    }

}
