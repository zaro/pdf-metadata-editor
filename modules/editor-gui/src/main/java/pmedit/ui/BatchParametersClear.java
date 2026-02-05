package pmedit.ui;

import pmedit.BatchOperationParameters;
import pmedit.preset.PresetValues;

import java.awt.*;
import java.awt.event.ActionListener;

public class BatchParametersClear extends BatchParametersEdit {

    public BatchParametersClear(BatchOperationParameters params) {
        this(params, null);
    }

    public BatchParametersClear(BatchOperationParameters params, Frame owner) {
        super(params, owner);
        defaultMetadataPane.disableEdit();
        setTitle("Batch clear parameters");
        setMessage("Select fields to be cleared below");

        presetSelector.removeActionListeners();
        presetSelector.addActionListener(e -> {
            if (e.getSource() instanceof PresetSelector.PresetActionData actionData) {
                if (actionData.isOnLoad()) {
                    PresetValues values = actionData.getPresetValues();
                    values.metadata = null;
                    defaultMetadataPane.onLoadPreset(values);
                } else if (actionData.isOnSave()) {
                    defaultMetadataPane.onSavePreset(actionData.getPresetValues());
                }
            }
        });
    }

}
