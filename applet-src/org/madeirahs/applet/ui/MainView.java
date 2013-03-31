/*
 *  The MHS-Collections Project applet contains the projects primary front-end
 *  code deployed on the website for use by end-users.
 *  Copyright © 2012-2013 Madeira Historical Society (developed by Brian Groenke)
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
 *  
 *  Note: This class was borrowed from the Groenke Commons Java API with full permission from the author.
 */

package org.madeirahs.applet.ui;

import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import org.madeirahs.applet.*;
import org.madeirahs.shared.*;
import org.madeirahs.shared.Artifact.StringField;
import org.madeirahs.shared.Artifact.TimeField;
import org.madeirahs.shared.database.*;
import org.madeirahs.shared.database.ArtifactSorter.Mode;
import org.madeirahs.shared.database.ArtifactSorter.Variable;
import org.madeirahs.shared.database.Database.TimeSearchFormat;
import org.madeirahs.shared.time.*;


public class MainView extends PageView {

	/**
	 * 
	 */
	private static final long serialVersionUID = -193890543949721611L;

	private static final String[] FIELDS = new String[] {"Title", "Donor", "Medium", "Object Date", "Submission Date"},
			SEARCH_FIELDS = new String[] {FIELDS[0], FIELDS[1], FIELDS[2], FIELDS[3], FIELDS[4], "Accession Number"},
			SORT_TYPE = new String[] {"First", "Last"};

	private static final int INSET = 10;
	private static final Font SEARCH_FONT = new Font("Times New Roman", Font.PLAIN, 13),
			SORT_FONT = new Font("Georgia", Font.PLAIN, 14);

	private CollectionsApplet context;
	private Artifact[] data, set;

	JPanel searchPanel, sortPanel;
	JScrollPane scrolls;
	Box main, dataList;

	JTextField search;
	JComboBox searchFields, sortType, sortBy;

	ArtifactSorter sorter = new ArtifactSorter();

	HashMap<JPanel, Artifact> selectionMap = new HashMap<JPanel, Artifact>();

	public MainView(CollectionsApplet context) {
		super();
		super.navMsg = NavBar.DEFAULT_NAV_MSG;
		this.context = context;
		setOpaque(false);
		setLayout(new BorderLayout());
		main = new Box(BoxLayout.Y_AXIS);
		main.setBorder(new EmptyBorder(INSET / 2, INSET, INSET, INSET));
		main.setOpaque(false);

		FlowLayout flowLeft = new FlowLayout();
		flowLeft.setAlignment(FlowLayout.LEFT);
		searchPanel = new JPanel(flowLeft);
		searchPanel.setOpaque(false);
		search = new JTextField(25);
		search.addCaretListener(new SearchFieldListener());
		search.setFont(SEARCH_FONT);
		//JButton searchBtn = new JButton("Search");
		searchFields = new JComboBox(SEARCH_FIELDS);
		searchPanel.add(search);
		searchPanel.add(searchFields);
		//searchPanel.add(searchBtn);

		sortPanel = new JPanel(flowLeft);
		sortPanel.setOpaque(false);
		JLabel sort1 = new JLabel("Sort by: ");
		sort1.setForeground(Color.WHITE);
		sort1.setFont(SORT_FONT);
		ActionListener sortListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				fillData(set, true);
			}

		};
		sortBy = new JComboBox(FIELDS);
		sortBy.addActionListener(sortListener);
		sortType = new JComboBox(SORT_TYPE);
		sortType.addActionListener(sortListener);
		sortPanel.add(sort1);
		sortPanel.add(sortBy);
		sortPanel.add(sortType);

		dataList = new Box(BoxLayout.Y_AXIS);
		dataList.setOpaque(false);

		scrolls = new JScrollPane(dataList);
		scrolls.setOpaque(false);
		scrolls.getViewport().setOpaque(false);
		scrolls.setViewportBorder(null);
		scrolls.getVerticalScrollBar().setUnitIncrement(15);
		scrolls.setBorder(new EmptyBorder(INSET, INSET, INSET, INSET));

		main.add(searchPanel);
		main.add(sortPanel);
		add(BorderLayout.NORTH, main);
		add(BorderLayout.CENTER, scrolls);
	}

	public void initData() {
		data = context.getDatabase().getData();
		set = data;
		fillData(data, true);
	}

	private void applySorting(Artifact[] data) {
		int fieldInd = sortBy.getSelectedIndex();
		int sortInd = sortType.getSelectedIndex();
		sorter.setVar(getSortingVar(fieldInd));
		sorter.setMode(getSortingMode(sortInd));
		Arrays.sort(data, sorter);
	}

	private void fillData(Artifact[] data, boolean sort) {

		if(sort)
			applySorting(data);

		dataList = new Box(BoxLayout.Y_AXIS);
		dataList.setOpaque(false);
		scrolls.setViewportView(dataList);

		for(int i = 0 ; i < data.length ; i++) {
			Artifact a = data[i];
			JPanel panel = new JPanel(new BorderLayout());
			Color panelColor = new Color(0, 0, 120, 180);
			panel.setBackground(panelColor);
			panel.setBorder(new MatteBorder(2,2,2,2,Color.BLACK));

			Font tfont = new Font("Times New Roman", Font.BOLD, 15);
			Font txtfont = new Font("Times New Roman", Font.PLAIN, 13);
			Color fontColor = Color.WHITE;
			Box west = new Box(BoxLayout.Y_AXIS);
			JLabel title = new JLabel(a.title);
			title.setFont(tfont);
			title.setForeground(fontColor);
			JLabel donor = new JLabel(a.donor);
			donor.setFont(txtfont);
			donor.setForeground(fontColor);
			JLabel accNum = new JLabel(a.accNum);
			accNum.setFont(txtfont);
			accNum.setForeground(fontColor);
			west.add(title);
			west.add(donor);
			west.add(accNum);
			Box east = new Box(BoxLayout.Y_AXIS);
			JLabel objDate = new JLabel(a.objDate.toString());
			objDate.setFont(txtfont);
			objDate.setForeground(fontColor);
			JLabel subDate = new JLabel(a.subDate.toString());
			subDate.setFont(txtfont);
			subDate.setForeground(fontColor);
			east.add(objDate);
			east.add(subDate);
			panel.add(BorderLayout.EAST, east);
			panel.add(BorderLayout.WEST, west);
			panel.addMouseListener(new ArtifactPanelListener(panelColor));
			panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel
					.getPreferredSize().height));
			dataList.add(panel);
			dataList.add(Box.createVerticalStrut(10));
			selectionMap.put(panel, a);
		}

		dataList.add(Box.createVerticalGlue());

		validate();
	}

	private void startSearch(Artifact[] data) {
		Database db = context.getDatabase();
		Artifact[] resArr = data;
		switch(searchFields.getSelectedIndex()) {
		case 0:
			Artifact a = Artifact.createGenericArtifact();
			a.title = search.getText();
			resArr = db.searchByField(StringField.TITLE, a);
			break;
		case 1:
			a = Artifact.createGenericArtifact();
			a.donor = search.getText();
			resArr = db.searchByField(StringField.DONOR, a);
			break;
		case 2:
			a = Artifact.createGenericArtifact();
			a.medium = search.getText();
			resArr = db.searchByField(StringField.MEDIUM, a);
			break;
		case 3:
			a = Artifact.createGenericArtifact();
			TimeSpec ts = parseStandardTimeString(search.getText());
			if(ts == null) {
				DateTime dt = new DateTime(Calendar.getInstance().getTime(), DateFormat.getDateInstance());
				dt.forcedValue = search.getText();
				a.objDate = dt;
				resArr = db.searchByFieldHybrid(TimeField.OBJECT_DATE, a);
			} else {
				a.objDate = ts;
				resArr = db.searchByField(TimeField.OBJECT_DATE, a);
			}
			break;
		case 4:
			a = Artifact.createGenericArtifact();
			ts = parseStandardTimeString(search.getText());
			if(ts == null) {
				DateTime dt = new DateTime(Calendar.getInstance().getTime(), DateFormat.getDateInstance());
				dt.forcedValue = search.getText();
				a.subDate = dt;
				resArr = db.searchByFieldHybrid(TimeField.SUBMISSION_DATE, a);
			} else {
				a.subDate = ts;
				resArr = db.searchByField(TimeField.SUBMISSION_DATE, a);
			}
			break;
		case 5:
			a = Artifact.createGenericArtifact();
			a.accNum = search.getText();
			resArr = db.searchByField(StringField.ACCESSION_NUMBER, a);
			break;
		}

		fillData(resArr, false);
	}
	
	private TimeSpec parseStandardTimeString(String str) {
		String[] pts = str.split("-");
		for(TimeSearchFormat tsf:TimeSearchFormat.values()) {
			Date time = tsf.parse(pts[0]);
			if(time == null)
				continue;
			DateTime dt = new DateTime(time, tsf.getFormat());
			if(pts.length > 1) {
				Date endTime = tsf.parse(pts[pts.length - 1]);
				if(!endTime.after(time))
					return dt;
				TimeFrame tf = new TimeFrame(time, endTime, tsf.getFormat());
				return tf;
			} else {
				return dt;
			}
		}
		return null;
	}

	private ArtifactSorter.Mode getSortingMode(int sortInd) {
		Mode mode = null;
		switch(sortInd) {
		case 0:
			mode = Mode.FORWARD;
			break;
		case 1:
			mode = Mode.REVERSE;
		}

		return mode;
	}

	private ArtifactSorter.Variable getSortingVar(int fieldInd) {
		Variable var = null;
		switch(fieldInd) {
		case 0:
			var = Variable.TITLE;
			break;
		case 1:
			var = Variable.DONOR;
			break;
		case 2:
			var = Variable.MEDIUM;
			break;
		case 3:
			var = Variable.OBJ_DATE;
			break;
		case 4:
			var = Variable.SUB_DATE;
		}
		return var;
	}

	private class ArtifactPanelListener implements MouseListener {

		Color pressed = new Color(255, 255, 150, 200),
				hover = new Color(Color.DARK_GRAY.getRed(), Color.DARK_GRAY.getBlue(), Color.DARK_GRAY.getGreen(), 210),
				hprev;

		boolean inside;

		ArtifactPanelListener(Color def) {
			hprev = def;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			e.getComponent().setBackground(pressed);
			context.repaint();
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			e.getComponent().setBackground(hprev);
			context.repaint();
			if(inside) {
				PageQueue pq = context.getPageQueue();
				PageView prev = pq.getCurrent();
				Artifact a = selectionMap.get(e.getComponent());
				PageView pv = new ArtifactPageView(new ArtifactView(a, context.getProvider()), context);
				pq.addNew(pv);
				context.updateView(prev);
				context.getNavBar().setMessage(pv.getCurrentNavMsg());
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			e.getComponent().setBackground(hover);
			inside = true;
			context.repaint();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			e.getComponent().setBackground(hprev);
			inside = false;
			e.getComponent().repaint();
			context.repaint();
		}

	}

	private class SearchFieldListener implements CaretListener {

		@Override
		public void caretUpdate(CaretEvent e) {
			if(search.getText() == null || search.getText().isEmpty()) {
				fillData(data, true);
				validate();
			} else {
				startSearch(data);
			}
		}
	}
}
