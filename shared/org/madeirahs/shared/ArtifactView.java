/*
 *  The MHS-Collections Project shared library is intended for use by both the applet
 *  and editor software in the interest of code consistency.
 *  Copyright © 2012-2013 Madeira Historical Society (developed by Brian Groenke)
 *
 *  This library is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.madeirahs.shared;

import java.awt.*;
import java.text.*;
import java.util.*;

import javax.swing.*;

import org.madeirahs.shared.provider.*;
import org.madeirahs.shared.time.*;
import org.madeirahs.shared.v3d.*;

/**
 * 
 * @author Brian Groenke
 * 
 */
public class ArtifactView extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4264988628541725443L;

	private static final String SHTML = "<html><font size=3>", EHTML = "</font></html>",
			TITLE = "<b>Title:</b> ", DONOR = "<b>Donor:</b> ",
			SUB_DATE = "<b>Date of Submission:</b> ",
			OBJ_DATE = "<b>Date of Object:</b> ", MEDIUM = "<b>Medium:</b> ",
			ACC_NUM = "<b>Accession #:</b> ", DESC = "<b>Description:</b>";

	public DataProvider provider;
	public Artifact elem;

	private V3DPanel vpan;
	private JLabel title, donor, subDate, objDate, medium, accNum, desc;
	private JTextArea descView;

	public ArtifactView(DataProvider provider) {
		this(new Artifact(new DateTime(Calendar.getInstance().getTime(),
				DateFormat.getDateInstance(DateFormat.MEDIUM)), new DateTime(
				Calendar.getInstance().getTime(),
				DateFormat.getDateInstance(DateFormat.MEDIUM)), "", "", "", "",
				""), provider);
	}

	public ArtifactView(Artifact a, DataProvider provider) {
		elem = a;
		this.provider = provider;
		setup();
	}

	public void setup() {
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		title = new JLabel(SHTML + TITLE + elem.title + EHTML);
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridy = 4;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.gridheight = 1;
		gbc.weighty = 1.0;
		add(title, gbc);
		donor = new JLabel(SHTML + DONOR + elem.donor + EHTML);
		gbc.gridy = 5;
		add(donor, gbc);
		subDate = new JLabel(SHTML + SUB_DATE + elem.subDate.toString() + EHTML);
		gbc.gridy = 6;
		add(subDate, gbc);
		objDate = new JLabel(SHTML + OBJ_DATE + elem.objDate.toString() + EHTML);
		gbc.gridy = 7;
		add(objDate, gbc);
		medium = new JLabel(SHTML + MEDIUM + elem.medium + EHTML);
		gbc.gridy = 8;
		add(medium, gbc);
		accNum = new JLabel(SHTML + ACC_NUM + elem.accNum + EHTML);
		gbc.gridy = 9;
		add(accNum, gbc);
		desc = new JLabel(SHTML + DESC + EHTML);
		//gbc.anchor = GridBagConstraints.PAGE_START;
		gbc.insets = new Insets(0, 50, 0, 0);
		gbc.gridy = 4;
		gbc.gridx = 2;
		add(desc, gbc);
		descView = new JTextArea();
		descView.setLineWrap(true);
		descView.setWrapStyleWord(true);
		descView.setEditable(false);
		descView.setText(elem.desc);
		descView.setFont(new Font("Times New Roman", Font.PLAIN, 13));
		JScrollPane scrollPane = new JScrollPane(descView);
		scrollPane.setPreferredSize(new Dimension(225, 100));
		// scrollPane.setBorder(null);
		scrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		descView.setCaretPosition(0);
		gbc.gridy = 5;
		gbc.gridheight = 4;
		gbc.gridx = 2;
		add(scrollPane, gbc);

		gbc.insets = null;
		gbc.gridy = 0;
		gbc.gridx = 0;
		gbc.gridwidth = 4;
		gbc.gridheight = 4;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(0, 0, 10, 0);
		vpan = new V3DPanel(elem, provider);
		vpan.setPreferredSize(new Dimension(500, 300));
		setBackground(Color.WHITE);
		add(vpan, gbc);
		validate();
	}

	/**
	 * Updates all of the components in the panel to match that of the
	 * associated Artifact.
	 * 
	 * @see #ArtifactView(Artifact, DataProvider)
	 */
	public void rebuild() {
		title.setText(SHTML + TITLE + elem.title + EHTML);
		donor.setText(SHTML + DONOR + elem.donor + EHTML);
		subDate.setText(SHTML + SUB_DATE + elem.subDate.toString() + EHTML);
		objDate.setText(SHTML + OBJ_DATE + elem.objDate.toString() + EHTML);
		medium.setText(SHTML + MEDIUM + elem.medium + EHTML);
		accNum.setText(SHTML + ACC_NUM + elem.accNum + EHTML);
		descView.setText(elem.desc);
		descView.setCaretPosition(0);
		validate();
	}

	public void updateV3D() {
		vpan.setImageDataFromArtifact(elem);
	}
}
