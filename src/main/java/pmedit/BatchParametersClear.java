package pmedit;

import java.awt.Frame;

public class BatchParametersClear extends BatchParametersEdit {

	/**
	 * @wbp.parser.constructor
	 */
	public BatchParametersClear( BatchOperationParameters params) {
		this( params, null);
	}
	
	public BatchParametersClear(BatchOperationParameters params, Frame owner) {
		super( params, owner);
		defaultMetadataPane.disableEdit();
		setMessage("Select fields to be cleared below");
	}

}
