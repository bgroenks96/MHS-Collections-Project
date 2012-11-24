package bg.mhsdata.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class DesktopLauncher {

	static JFrame frame;
	static MainView view;

	public static void main(String[] args) throws InterruptedException,
			InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable() {

			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager
							.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}
				frame = new JFrame(
						"Madeira Historical Society Inventory UI Preview");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setSize(550, 400);
				frame.setLocationRelativeTo(null);

				view = new MainView(new BorderLayout());
				JPanel panel = new JPanel();
				GridBagLayout gbl = new GridBagLayout();
				panel.setLayout(gbl);
				view.add(BorderLayout.CENTER, panel);
				view.validate();
				GridBagConstraints c = new GridBagConstraints();
				c.fill = GridBagConstraints.NONE;
				c.anchor = GridBagConstraints.CENTER;
				c.gridx = 0;
				c.gridy = 0;
				c.insets = new Insets(10, 10, 10, 10);
				panel.add(new JTextField(25), c);
				c.gridx = 1;
				panel.add(new JComboBox(new String[] { "Fields Here" }), c);
				c.gridx = 0;
				c.gridy = 1;
				c.anchor = GridBagConstraints.EAST;
				c.ipadx = 100;
				panel.add(new JButton("Search"), c);
				view.add(
						BorderLayout.SOUTH,
						new JLabel(
								"<html><font color=\"white\">Copyright 2012 Madeira Historical Society - All rights reserved</font></html>"));
				panel.setOpaque(false);
				view.setEnabled(true);
				view.setOpaque(false);
				frame.setContentPane(view);
				frame.validate();
				frame.setVisible(true);
			}

		});
	}

}
