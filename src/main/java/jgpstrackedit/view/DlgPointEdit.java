package jgpstrackedit.view;

import jgpstrackedit.data.Point;
import jgpstrackedit.util.Parser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DlgPointEdit extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField textFieldIndex;
	private JTextField textFieldLatitude;
	private JTextField textFieldLongitude;
	private JTextField textFieldElevation;
	private JTextField textFieldTimestamp;
	private JTextField textFieldInformation;
	private JTextField textFieldLength;
	private final Point point;

	/**
	 * Create the dialog.
	 */
	public DlgPointEdit(Frame owner, int selectedPointIndex, Point selectedPoint) {
		super(owner, true);
		point = selectedPoint;
		initComponents();
		textFieldIndex.setText(""+selectedPointIndex);
		textFieldLatitude.setText(point.getLatitudeAsString());
		textFieldLongitude.setText(point.getLongitudeAsString());
		textFieldElevation.setText(point.getElevationAsString());
		textFieldTimestamp.setText(point.getTime());
		textFieldInformation.setText(point.getInformation());
		textFieldLength.setText(Parser.formatLength(point.getLength()));
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.setLocation(owner.getX() + (int)(owner.getWidth()/10), owner.getY() + (int)(owner.getHeight()/10));
	}

	private void initComponents() {
		setTitle("Edit Point");
		setBounds(100, 100, 450, 256);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		JLabel lblIndex = new JLabel("Index");
		lblIndex.setBounds(10, 11, 98, 14);
		contentPanel.add(lblIndex);
		
		textFieldIndex = new JTextField();
		textFieldIndex.setEditable(false);
		textFieldIndex.setBounds(154, 8, 86, 20);
		contentPanel.add(textFieldIndex);
		textFieldIndex.setColumns(10);

		JLabel lblNewLabel = new JLabel("Latitude");
		lblNewLabel.setBounds(10, 36, 113, 14);
		contentPanel.add(lblNewLabel);
		
		textFieldLatitude = new JTextField();
		textFieldLatitude.setEditable(false);
		textFieldLatitude.setBounds(154, 33, 171, 20);
		contentPanel.add(textFieldLatitude);
		textFieldLatitude.setColumns(10);

		JLabel lblLongitude = new JLabel("Longitude");
		lblLongitude.setBounds(10, 61, 113, 14);
		contentPanel.add(lblLongitude);
		
		textFieldLongitude = new JTextField();
		textFieldLongitude.setEditable(false);
		textFieldLongitude.setBounds(154, 58, 171, 20);
		contentPanel.add(textFieldLongitude);
		textFieldLongitude.setColumns(10);

		JLabel lblElevation = new JLabel("Elevation");
		lblElevation.setBounds(10, 86, 113, 14);
		contentPanel.add(lblElevation);
		
		textFieldElevation = new JTextField();
		textFieldElevation.setBounds(154, 83, 171, 20);
		contentPanel.add(textFieldElevation);
		textFieldElevation.setColumns(10);

		JLabel lblTimestamp = new JLabel("Timestamp");
		lblTimestamp.setBounds(10, 111, 123, 14);
		contentPanel.add(lblTimestamp);
		
		textFieldTimestamp = new JTextField();
		textFieldTimestamp.setBounds(154, 108, 278, 20);
		contentPanel.add(textFieldTimestamp);
		textFieldTimestamp.setColumns(10);

		JLabel lblInformation = new JLabel("Information");
		lblInformation.setBounds(10, 136, 123, 14);
		contentPanel.add(lblInformation);
		
		textFieldInformation = new JTextField();
		textFieldInformation.setBounds(154, 133, 278, 20);
		contentPanel.add(textFieldInformation);
		textFieldInformation.setColumns(10);

		JLabel lblLength = new JLabel("Length");
		lblLength.setBounds(10, 161, 113, 14);
		contentPanel.add(lblLength);
		
		textFieldLength = new JTextField();
		textFieldLength.setEditable(false);
		textFieldLength.setBounds(154, 158, 171, 20);
		contentPanel.add(textFieldLength);
		textFieldLength.setColumns(10);

		JLabel lblm = new JLabel("[m]");
		lblm.setBounds(335, 86, 46, 14);
		contentPanel.add(lblm);

		JLabel lblkm = new JLabel("[km]");
		lblkm.setBounds(335, 164, 46, 14);
		contentPanel.add(lblkm);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						okButtonActionPerformed(e);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						cancelButtonActionPerformed(e);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
	protected void okButtonActionPerformed(ActionEvent e) {
		point.setInformation(textFieldInformation.getText());
		point.setTime(textFieldTimestamp.getText());
		point.setElevation(textFieldElevation.getText());
		setVisible(false);
	}
	
	protected void cancelButtonActionPerformed(ActionEvent e) {
		setVisible(false);
	}
}
