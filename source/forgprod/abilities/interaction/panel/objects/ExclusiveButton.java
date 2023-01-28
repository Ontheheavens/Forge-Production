package forgprod.abilities.interaction.panel.objects;

import com.fs.starfarer.api.ui.ButtonAPI;

import forgprod.abilities.interaction.panel.listeners.ButtonListener;

/**
 * @author Ontheheavens
 * @since 31.12.2022
 */

public abstract class ExclusiveButton extends Button{

    private boolean checkedLastFrame;
    private boolean othersUnchecked;
    private boolean checkedAtCreation;

    public ExclusiveButton(ButtonAPI button, Type type) {
        super(button, type);
    }

    public ExclusiveButton(ButtonAPI button, Type type, boolean createChecked) {
        super(button, type);
        checkedAtCreation = createChecked;
    }

    public void affectOthersInGroup() {
        for (Button entry : ButtonListener.getIndex()) {
            if (entry.getType() != this.getType()) continue;
            if (entry == this) continue;
            entry.getInner().setChecked(false);
            entry.getInner().setEnabled(true);
            entry.getInner().unhighlight();
        }
    }

    @Override
    public void check() {
        ButtonAPI instance = this.getInner();
        if (!instance.isChecked()) {
            checkedLastFrame = false;
            othersUnchecked = false;
            return;
        }
        if (!othersUnchecked) {
            affectOthersInGroup();
            othersUnchecked = true;
        }
        if (checkedAtCreation) {
            checkedAtCreation = false;
            checkedLastFrame = true;
            return;
        }
        if (!checkedLastFrame) {
            this.applyEffect();
            checkedLastFrame = true;
        }
    }

}
