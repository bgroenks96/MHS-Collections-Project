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
import java.net.*;

import javax.swing.*;

import org.madeirahs.applet.*;

public class NavBar extends Box {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7502260781158532688L;

	private static final int HEIGHT_DIV = 22;

	private static final Color BACKGROUND = new Color(0, 0, 0, 180);

	private final String back_img_name = "arrow_left.png", 
			forward_img_name = "arrow_right.png", fsel_img_name = "arrow_right_sel.png", bsel_img_name = "arrow_left_sel.png",
			help_img_name = "help.png", hsel_img_name = "help_sel.png";

	private ImageIcon back_img, forward_img, bsel_img, fsel_img, help_img, hsel_img;

	CollectionsApplet context;

	JButton back, forward, help;

	public NavBar(CollectionsApplet context) throws MalformedURLException {
		super(BoxLayout.X_AXIS);
		this.context = context;

		back_img = new ImageIcon(new URL(context.getResourceURL() + "/" + back_img_name));
		forward_img = new ImageIcon(new URL(context.getResourceURL() + "/" + forward_img_name));
		bsel_img = new ImageIcon(new URL(context.getResourceURL() + "/" + bsel_img_name));
		fsel_img = new ImageIcon(new URL(context.getResourceURL() + "/" + fsel_img_name));
		help_img = new ImageIcon(new URL(context.getResourceURL() + "/" + help_img_name));
		hsel_img = new ImageIcon(new URL(context.getResourceURL() + "/" + hsel_img_name));

		setOpaque(true);
		setPreferredSize(new Dimension(context.getWidth(), context.getHeight() / HEIGHT_DIV));
		NavButtonSelected dblistener = new NavButtonSelected();
		back = new JButton(back_img);
		back.setBorderPainted(false);
		back.setContentAreaFilled(false);
		back.addMouseListener(dblistener);
		back.addActionListener(new BackListener());
		forward = new JButton(forward_img);
		forward.setBorderPainted(false);
		forward.setContentAreaFilled(false);
		forward.addMouseListener(dblistener);
		forward.addActionListener(new ForwardListener());
		help = new JButton(help_img);
		help.setBorderPainted(false);
		help.setContentAreaFilled(false);
		help.addMouseListener(dblistener);
		add(Box.createHorizontalStrut(5));
		add(back);
		add(Box.createHorizontalStrut(5));
		add(forward);
		add(Box.createHorizontalGlue());
		JLabel label = new JLabel("Welcome to the Madeira Historical Society Artifact Database!");
		label.setFont(new Font("Century", Font.PLAIN, 12));
		label.setForeground(Color.WHITE);
		add(label);
		add(Box.createHorizontalGlue());
		add(help);
	}

	@Override
	public void paintComponent(Graphics g) {
		g.setColor(BACKGROUND);
		g.fillRect(0, 0, getWidth(), getHeight());
		super.paintComponent(g);
	}

	private class BackListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Container prev = context.getPageQueue().getCurrent();
			context.getPageQueue().back();
			context.updateView(prev);
		}

	}

	private class ForwardListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Container prev = context.getPageQueue().getCurrent();
			context.getPageQueue().forward();
			context.updateView(prev);
		}

	}

	private class NavButtonSelected extends MouseAdapter {

		@Override
		public void mousePressed(MouseEvent me) {
			JButton b = (JButton) me.getComponent();
			if(b == back)
				b.setIcon(bsel_img);
			else if(b == forward)
				b.setIcon(fsel_img);
			else if(b == help)
				b.setIcon(hsel_img);
			b.repaint();
		}

		@Override
		public void mouseReleased(MouseEvent me) {
			JButton b = (JButton) me.getComponent();
			if(b == back)
				b.setIcon(back_img);
			else if(b == forward)
				b.setIcon(forward_img);
			else if(b == help)
				b.setIcon(help_img);
			b.repaint();
		}
	}

}
