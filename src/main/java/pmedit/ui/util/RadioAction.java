package pmedit.ui.util;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class RadioAction extends AbstractAction {
    private final ButtonGroup group;
    private boolean selected;
    private static final List<RadioAction> allActions = new ArrayList<>();

    public RadioAction(String name, ButtonGroup group, boolean initialState) {
        super(name);
        this.group = group;
        this.selected = initialState;
        allActions.add(this);
        updateState();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        if (this.selected != selected) {
            this.selected = selected;
            updateState();

            // Ensure mutual exclusion
            if (selected) {
                for (RadioAction action : allActions) {
                    if (action != this && action.isSelected()) {
                        action.setSelected(false);
                    }
                }
            }
        }
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