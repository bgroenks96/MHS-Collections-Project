/*
 *  The MHS-Collections Project editor is intended for use by Historical Society members
 *  to edit, review and upload artifact information.
 *  Copyright (c) 2012-2016 Madeira Historical Society (developed by Brian Groenke)
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
import java.awt.image.*;
import java.io.*;
import java.net.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;

public class HelpUI extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8355846190461698480L;
	
	private static final String UI_ROOT = "org/madeirahs/editor/ui/", HELP_ARTIFACTS_LOC = UI_ROOT + "help_artifacts.html", HELP_NET_LOC = UI_ROOT + "help_net.rtf",
			HELP_GENERAL_LOC = UI_ROOT + "help_general.rtf", HELP_GPL_LOC = UI_ROOT + "gpl.html";
	
	private static final URL BLANK_PAGE = ClassLoader.getSystemClassLoader().getResource(UI_ROOT + "blank.html");

	private static double scalex, scaley;

	static {
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		scalex = d.width / 1920.0;
		scaley = d.height / 1080.0;
		if(scalex > 1)
			scalex = 1;
		if(scaley > 1)
			scaley = 1;
	}

	private Sidebar sidebar;
	private SidebarOption general, artifact, net, gpl, sel;
	private JTextPane infoView;

	public HelpUI(Frame parent, String title) {
		sidebar = new Sidebar();
		sidebar.setBorder(new EmptyBorder(10,10,10,10));
		infoView = new JTextPane();
		Dimension d1 = sidebar.getPreferredSize();
		infoView.setPreferredSize(new Dimension(d1.width * 3, d1.height - 5));
		infoView.setEditable(false);

		MouseAdapter ma = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent me) {
				SidebarOption sopt = (SidebarOption) me.getComponent();
				if(sel != null) {
					sel.setSelected(false);
					sel.repaint();
				}
				sel = sopt;
				sel.setSelected(true);
				sel.repaint();
				renderInfo();
			}
		};

		general = new SidebarOption("General Info", HELP_GENERAL_LOC);
		general.addMouseListener(ma);
		sidebar.add(general);
		
		sidebar.add(Box.createVerticalStrut(scy(10)));

		artifact = new SidebarOption("Artifacts", HELP_ARTIFACTS_LOC);
		artifact.addMouseListener(ma);
		sidebar.add(artifact);
		
		sidebar.add(Box.createVerticalStrut(scy(10)));

		net = new SidebarOption("Networking", HELP_NET_LOC);
		net.addMouseListener(ma);
		sidebar.add(net);
		
		sidebar.add(Box.createVerticalStrut(scy(10)));
		
		gpl = new SidebarOption("License", HELP_GPL_LOC);
		gpl.addMouseListener(ma);
		sidebar.add(gpl);
		
		general.setSelected(true);
		sel = general;

		sidebar.add(Box.createVerticalGlue());

		add(BorderLayout.WEST, sidebar);
		add(BorderLayout.CENTER, new JScrollPane(infoView));
		setResizable(false);
		pack();
		setLocationRelativeTo(parent);
		setTitle(title);
		
		renderInfo();
	}
	
	private void renderInfo() {
		if(sel.rsc == null) {
			try {
				infoView.setPage(BLANK_PAGE);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		
		try {
			infoView.setPage(sel.rsc);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int scx(int px) {
		return (int) Math.round(px * scalex);
	}

	private int scy(int py) {
		return (int) Math.round(py * scaley);
	}

	private class Sidebar extends Box {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4636294888266555489L;

		BufferedImage back;

		Sidebar() {
			super(BoxLayout.Y_AXIS);

			try {
				back = ImageIO.read(ClassLoader.getSystemClassLoader().getResource("org/madeirahs/editor/ui/help_sidebar.png"));
				scaleImage();
				setPreferredSize(new Dimension(back.getWidth(), back.getHeight()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void paintComponent(Graphics g) {
			g.drawImage(back, 0, 0, null);
			super.paintComponent(g);
		}

		private void scaleImage() {
			Image img = back.getScaledInstance(scx(back.getWidth()), scy(back.getHeight()), Image.SCALE_SMOOTH);
			back = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			Graphics g = back.getGraphics();
			g.drawImage(img, 0, 0, null);
			g.dispose();
		}
	}

	private class SidebarOption extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = -3208144624932369119L;

		private final Font font = new Font("Arial", Font.BOLD, 16);
		private final Color selColor = new Color(0, 0, 255, 100);
		private boolean selected = false;

		JLabel label;
		URL rsc;

		public SidebarOption(String labelTxt, String rsc) {
			label = new JLabel(labelTxt);
			label.setForeground(Color.WHITE);
			label.setFont(font);
			add(label);
			setOpaque(false);
			setMaximumSize(new Dimension(1000, label.getPreferredSize().height + 5));
			
			this.rsc = ClassLoader.getSystemClassLoader().getResource(rsc);
		}

		@Override
		public void paintComponent(Graphics g) {
			if(selected) {
				g.setColor(selColor);
				g.fillRoundRect(0, 0, getWidth(), getHeight(), getWidth() / 10, getHeight() / 10);
				label.setForeground(Color.YELLOW);
			} else
				label.setForeground(Color.WHITE);
			super.paintComponent(g);
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
		}

	}

}
