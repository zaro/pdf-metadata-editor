package pmedit;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class BatchParametersEdit extends BatchParametersWindow {

	protected MetadataEditPane defaultMetadataPane;

	/**
	 * @wbp.parser.constructor
	 */
	public BatchParametersEdit(BatchOperationParameters parameters) {
		this(parameters, null);
	}

	/**
	 * Create the frame.
	 */
	public BatchParametersEdit(BatchOperationParameters parameters, final Frame owner) {
		super(parameters, owner);
	}
	
	protected JLabel lblSelectFieldsTo;
	protected void setMessage(String message){
		lblSelectFieldsTo.setText(message);
	}

	protected void createContentPane() {
		setTitle("Batch set parameters");
		setMinimumSize(new Dimension(640, 480));
		JPanel contentPane = new JPanel();
		setContentPane(contentPane);


		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{520,  0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 29, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.insets = new Insets(5, 5, 5, 5);
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		contentPane.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{520, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		lblSelectFieldsTo = new JLabel("The selected fields below will be set in all files");
		GridBagConstraints gbc_lblSelectFieldsTo = new GridBagConstraints();
		gbc_lblSelectFieldsTo.insets = new Insets(0, 0, 0, 5);
		gbc_lblSelectFieldsTo.fill = GridBagConstraints.VERTICAL;
		gbc_lblSelectFieldsTo.gridx = 0;
		gbc_lblSelectFieldsTo.gridy = 0;
		panel.add(lblSelectFieldsTo, gbc_lblSelectFieldsTo);
		
		JButton btnSelectAll = new JButton("Select all");
		btnSelectAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(parameters != null){
					parameters.metadata.setEnabled(true);
					defaultMetadataPane.fillFromMetadata(parameters.metadata);
				}
			}
		});
		GridBagConstraints gbc_btnSelectAll = new GridBagConstraints();
		gbc_btnSelectAll.insets = new Insets(0, 0, 0, 5);
		gbc_btnSelectAll.gridx = 1;
		gbc_btnSelectAll.gridy = 0;
		panel.add(btnSelectAll, gbc_btnSelectAll);
		
		JButton button = new JButton("Select none");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(parameters != null){
					parameters.metadata.setEnabled(false);
					defaultMetadataPane.fillFromMetadata(parameters.metadata);
				}
			}
		});
		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.gridx = 2;
		gbc_button.gridy = 0;
		panel.add(button, gbc_button);

		GridBagConstraints gbc_md = new GridBagConstraints();
		gbc_md.weightx = 1.0;
		gbc_md.weighty = 1.0;
		gbc_md.insets = new Insets(5, 5, 5, 5);
		gbc_md.anchor = GridBagConstraints.NORTH;
		gbc_md.fill = GridBagConstraints.BOTH;
		gbc_md.gridx = 0;
		gbc_md.gridy = 1;
		defaultMetadataPane = new MetadataEditPane();

		contentPane.add(defaultMetadataPane.tabbedaPane, gbc_md);

				
						
		JButton btnClose = new JButton("Close");
		GridBagConstraints gbc_btnClose = new GridBagConstraints();
		gbc_btnClose.insets = new Insets(0, 0, 5, 5);
		gbc_btnClose.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnClose.gridx = 0;
		gbc_btnClose.gridy = 2;
		contentPane.add(btnClose, gbc_btnClose);

		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				windowClosed();

			}
		});


		defaultMetadataPane.showEnabled(true);
		defaultMetadataPane.fillFromMetadata(parameters.metadata);
		contentPane.doLayout();
	}
	public void windowClosed() {
		defaultMetadataPane.copyToMetadata(parameters.metadata);
		super.windowClosed();
	}

}
