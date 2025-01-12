package pmedit.ui;

import pmedit.BatchOperationParameters;

import java.awt.*;

public class BatchParametersClear extends BatchParametersEdit {

    public BatchParametersClear(BatchOperationParameters params) {
        this(params, null);
    }

    public BatchParametersClear(BatchOperationParameters params, Frame owner) {
        super(params, owner);
        defaultMetadataPane.disableEdit();
        setMessage("Select fields to be cleared below");
    }

}
