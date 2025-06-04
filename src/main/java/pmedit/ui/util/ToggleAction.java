package pmedit.ui.util;

import javax.swing.*;
import java.awt.event.ActionEvent;

// Custom ToggleAction class
public class ToggleAction extends AbstractAction {
    protected boolean selected;

    public ToggleAction(String name, boolean initialState) {
        super(name);
        this.selected = initialState;
        updateState();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        updateState();
    }

    private void updateState() {
        putValue(Action.SELECTED_KEY, selected);
        firePropertyChange("selected", !selected, selected);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setSelected(!selected);
    }
}