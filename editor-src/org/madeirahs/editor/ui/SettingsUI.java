/*
 *  The MHS-Collections Project editor is intended for use by Historical Society members
 *  to edit, review and upload artifact information.
 *  Copyright © 2012-  Madeira Historical Society (developed by Brian Groenke)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.madeirahs.editor.ui;

import java.awt.*;
import java.awt.event.*;
import java.util.prefs.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.madeirahs.editor.main.*;
import org.madeirahs.editor.main.Settings.UpdateInterval;

public class SettingsUI extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8875214273801086854L;

	private static final int TEXT_FIELD_LENGTH = 30, SHORT_TEXT_FIELD_LENGTH = 7;
	private static final String ALWAYS = "Always", DAILY = "Daily", WEEKLY = "Weekly", MONTHLY = "Monthly";
	/**
	 * Indices of values are assumed by code in this class.  If the order of the array is altered, value fetching/storing
	 * must be updated accordingly.
	 */
	private static final String[] INTERVAL_OPTS = new String[] {ALWAYS,DAILY,WEEKLY,MONTHLY};

	JTextField usr, limitNum;
	JCheckBox update, limit;
	JComboBox interval;
	Box root;

	public SettingsUI(Frame parent, String title) {
		super(parent, title);
		root = Box.createVerticalBox();
		JLabel usrl = new JLabel("Username");
		JPanel usrlp = new JPanel();
		((FlowLayout)usrlp.getLayout()).setAlignment(FlowLayout.LEFT);
		usrlp.add(usrl);
		root.add(usrlp);
		JPanel usrp = new JPanel();
		usr = new JTextField(TEXT_FIELD_LENGTH);
		usrp.add(usr);
		root.add(usrp);

		Box updateOpts = Box.createHorizontalBox();
		update = new JCheckBox("Check for updates on start");
		update.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				interval.setEnabled(update.isSelected());
			}
			
		});
		updateOpts.add(update);
		interval = new JComboBox(INTERVAL_OPTS);
		updateOpts.add(Box.createHorizontalGlue());
		updateOpts.add(interval);
		root.add(updateOpts);

		Box archiveOpts = Box.createHorizontalBox();
		limit = new JCheckBox("Limit backups kept in archives");
		limit.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				limitNum.setEnabled(limit.isSelected());
			}

		});
		archiveOpts.add(limit);
		archiveOpts.add(Box.createHorizontalGlue());
		archiveOpts.add(new JLabel("to "));
		limitNum = new JTextField(SHORT_TEXT_FIELD_LENGTH);
		JPanel lnp = new JPanel();
		lnp.add(limitNum);
		archiveOpts.add(lnp);
		root.add(archiveOpts);

		root.setBorder(new EmptyBorder(5,5,5,5));
		setContentPane(root);
		syncOptions();
		pack();
		setLocationRelativeTo(parent);
		addWindowListener(new DialogClosing());
		validate();
	}

	/**
	 * Sets all option components to the current corresponding Settings class values.
	 * 
	 * N.B: interval box index values are hard-coded.  If this is changed, you must also update this method.
	 */
	private void syncOptions() {
		usr.setText((Settings.usr != null) ? Settings.usr:"");
		update.setSelected(Settings.updateCheck);
		UpdateInterval intrv = Settings.UpdateInterval.getByMillis(Settings.interval);
		switch(intrv) {
		case DAILY:
			interval.setSelectedIndex(1); // assumed indices
			break;
		case WEEKLY:
			interval.setSelectedIndex(2);
			break;
		case MONTHLY:
			interval.setSelectedIndex(3);
			break;
		default:
			interval.setSelectedIndex(0);
		}
		interval.setEnabled(update.isSelected());
		limit.setSelected(Settings.archiveLimit >= 0);
		limitNum.setText((Settings.archiveLimit < 0) ? "0":String.valueOf(Settings.archiveLimit));
		limitNum.setEnabled(limit.isSelected());
	}

	/**
	 * Stores all values to Preferences when dialog is closed.
	 * 
	 * N.B: interval box index values are hard-coded.  If this is changed, you must also update the windowClosing method.
	 * @author Brian Groenke
	 *
	 */
	private class DialogClosing extends WindowAdapter {

		@Override
		public void windowClosing(WindowEvent wine) {
			Preferences p = Settings.prefs();
			p.put(Settings.USER_KEY, usr.getText());
			p.putBoolean(Settings.UPDATE_CHECK_KEY, update.isSelected());
			try {
				int newLimit = Integer.parseInt(limitNum.getText());
				if(!limit.isSelected())
					newLimit = -1;
				p.putInt(Settings.ARCHIVE_LIMIT_KEY, newLimit);
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(null, "Illegal backup limit value.  Changes will not persist.", "Illegal Entry", JOptionPane.WARNING_MESSAGE);
			}
			int index = interval.getSelectedIndex();
			switch(index) {
			case 1:
				p.putLong(Settings.UPDATE_INTERVAL_KEY, UpdateInterval.DAILY.getMillis());
				break;
			case 2:
				p.putLong(Settings.UPDATE_INTERVAL_KEY, UpdateInterval.WEEKLY.getMillis());
				break;
			case 3:
				p.putLong(Settings.UPDATE_INTERVAL_KEY, UpdateInterval.MONTHLY.getMillis());
				break;
			default:
				p.putLong(Settings.UPDATE_INTERVAL_KEY, UpdateInterval.ALWAYS.getMillis());
				break;
			}
			
			Settings.sync();
			Settings.save();
		}
	}

}
