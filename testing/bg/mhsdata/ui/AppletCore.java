package bg.mhsdata.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.net.ftp.FTPClient;

public class AppletCore extends JApplet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6414568438258153604L;

	MainView view;

	@Override
	public void init() {
		view = new MainView(new BorderLayout());
		setSize(600, 450);
		FTPClient ftp = new FTPClient();
		try {
			ftp.connect("017e76a.netsolhost.com");
			boolean login = ftp.login("brgftp%017e76a", "Jftp2012");
			if (login) {
				System.out.println("Logged in successfully");
				OutputStream os = ftp.storeFileStream("/folder/hello.txt");
				FileInputStream fis = new FileInputStream(
						"C:\\test_root\\hello.txt");
				byte[] bytes = new byte[1024];
				int len;
				while ((len = fis.read(bytes)) > 0) {
					os.write(bytes, 0, len);
					os.flush();
				}
				fis.close();
				os.close();
				ftp.completePendingCommand();
				InputStream is = ftp.retrieveFileStream("/folder/hello.txt");
				FileOutputStream fos = new FileOutputStream(
						"C:\\test_root\\downloaded.txt");
				bytes = new byte[1024];
				while ((len = is.read(bytes)) > 0) {
					fos.write(bytes, 0, len);
					fos.flush();
				}
				fos.close();
				is.close();
				ftp.completePendingCommand();
				// new
				// URL("ftp://brgftp%017e76a:Jftp2012@017e76a.netsolhost.com/hello.txt;type=i");
				boolean logout = ftp.logout();
				if (!logout) {
					System.out.println("Failed to logout successfully.");
				} else {
					System.out.println("Logged out successfully");
				}
				ftp.disconnect();
			}
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start() {
		SwingUtilities.invokeLater(new Runnable() {

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
				setContentPane(view);
				view.setOpaque(false);

				JPanel panel = new JPanel();
				GridBagLayout gbl = new GridBagLayout();
				panel.setLayout(gbl);
				view.add(BorderLayout.CENTER, panel);
				view.validate();
				GridBagConstraints c = new GridBagConstraints();
				c.fill = GridBagConstraints.NONE;
				c.anchor = GridBagConstraints.SOUTH;
				c.weighty = 20.0;
				c.gridx = 0;
				c.gridy = 0;
				c.insets = new Insets(10, 10, 10, 10);
				panel.add(new JTextField(25), c);
				c.gridx = 1;
				c.gridy = 0;
				panel.add(new JComboBox(new String[] { "Fields Here" }), c);
				c.gridx = 0;
				c.gridy = 1;
				c.weighty = 0.5;
				c.ipadx = 100;
				c.anchor = GridBagConstraints.SOUTH;
				panel.add(new JButton("Search"), c);

				view.add(
						BorderLayout.SOUTH,
						new JLabel(
								"<html><font color=\"white\">Copyright © 2012 Madeira Historical Society - All rights reserved</font></html>"));
				panel.setOpaque(false);
				view.validate();
			}

		});
	}

	@Override
	public void stop() {

	}

	@Override
	public void destroy() {

	}
}
