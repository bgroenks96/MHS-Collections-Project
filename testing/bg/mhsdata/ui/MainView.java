package bg.mhsdata.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class MainView extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2986033297099105133L;

	public MainView(LayoutManager layout) {
		super(layout);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		try {
			BufferedImage bi = ImageIO.read(Thread.currentThread()
					.getContextClassLoader()
					.getResource("bg/mhsdata/rsc/miller_house.jpg"));
			Image scaled = bi.getScaledInstance(getWidth(), getHeight(),
					Image.SCALE_SMOOTH);
			g.drawImage(scaled, 0, 0, getWidth(), getHeight(), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Font font = new Font("Times New Roman", Font.BOLD, 28);
		String str = "Madeira Historical Society Collections";
		FontMetrics metrics = g.getFontMetrics(font);
		int line = metrics.getHeight();
		int wt = metrics.stringWidth(str);
		g.setColor(new Color(0, 0, 150, 200));
		g.fill3DRect((getWidth() / 10) - 2, getHeight() / 10 - (line - 5),
				wt + 2, line + 2, true);
		g.setColor(Color.YELLOW);
		g.setFont(font);
		g.drawString(str, getWidth() / 10, getHeight() / 10);
	}

}
