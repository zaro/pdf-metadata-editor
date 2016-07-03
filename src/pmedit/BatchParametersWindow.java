package pmedit;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JPanel;

public abstract class BatchParametersWindow extends JDialog {

	public BatchOperationParameters parameters;
	Runnable onClose;

	public BatchParametersWindow(BatchOperationParameters parameters) {
		this(parameters, null);
	}

	/**
	 * Create the frame.
	 */
	public BatchParametersWindow(BatchOperationParameters parameters, final Frame owner) {
		super(owner, true);

		if (parameters != null) {
			this.parameters = parameters;
		} else {
			this.parameters = new BatchOperationParameters();
		}
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				BatchParametersWindow.this.windowClosed();
			}
		});
		createContentPane();
	}
	protected abstract void createContentPane();

	public void onCloseAction(Runnable newAction) {
		onClose = newAction;
	}

	public void windowClosed() {
		if(onClose != null){
			onClose.run();
		}		
	}

}