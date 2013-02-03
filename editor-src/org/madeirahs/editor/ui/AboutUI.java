package org.madeirahs.editor.ui;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.jar.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;

import org.madeirahs.editor.main.*;

public class AboutUI extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2028779818842532250L;
	
	private static final String IMG_HEADER_PATH = "org/madeirahs/editor/ui/about_head.png", COPYRIGHT_PATH = "org/madeirahs/editor/copyright.txt";
	
	private static String copyright;
	
	static {
		Scanner sc = new Scanner(ClassLoader.getSystemClassLoader().getResourceAsStream(COPYRIGHT_PATH));
		StringBuilder sb = new StringBuilder();
		while(sc.hasNextLine())
			sb.append(sc.nextLine());
		copyright = sb.toString();
	}
	
	String[] pkgStrings;

	public AboutUI(Frame parent, String title) {
		super(parent, title);
		HeaderPanel head = new HeaderPanel();
		add(BorderLayout.NORTH, head);
		
		JTextArea crt = new JTextArea(5, 1);
		crt.setLineWrap(true);
		crt.setWrapStyleWord(true);
		crt.setOpaque(false);
		crt.setBackground(new Color(0,0,0,0));
		crt.setBorder(null);
		crt.setEditable(false);
		crt.setMaximumSize(new Dimension(head.getPreferredSize().width, 500));
		crt.setText(copyright);
		
		Box info = Box.createVerticalBox();
		info.setBorder(new EmptyBorder(10, 5, 10, 5));
		
		boolean pkgInfo = true;
		try {
			loadPackageInfo();
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error loading package information:\n"+e.toString(), "I/O Error", JOptionPane.ERROR_MESSAGE);
			JPanel pan = new JPanel();
			pan.add(new JLabel("<html><font color=\"red\">Unable to retrieve packge information</font></html>"));
			info.add(BorderLayout.SOUTH, pan);
			pkgInfo = false;
		}
		
		if(pkgInfo) {
			
			JPanel sh = new JPanel();
			sh.add(new JLabel("<html><b>Software Packages</b></html>"));
			info.add(sh);
			
			for(String s:pkgStrings) {
				JPanel p = new JPanel();
				((FlowLayout)p.getLayout()).setAlignment(FlowLayout.LEFT);
				JLabel label = new JLabel(s);
				label.setFont(new Font("Arial", Font.PLAIN, 12));
				p.add(label);
				info.add(p);
			}
			
			info.add(Box.createVerticalStrut(10));
		}
		
		info.add(crt);
		
		add(BorderLayout.CENTER, info);
		
		setSize(Math.max(head.getPreferredSize().width, info.getPreferredSize().width), 400);
		setLocationRelativeTo(parent);
		setResizable(false);
	}
	
	private void loadPackageInfo() throws IOException {
		File dir = AppSupport.BIN_DIR;
		File[] jars = dir.listFiles();
		jars = Arrays.copyOf(jars, jars.length + 1);
		jars[jars.length - 1] = new File(System.getProperty("user.dir") + File.separator + getLocalJarName(this.getClass()));
		ArrayList<String> strs = new ArrayList<String>();
		for(File f:jars) {
			if(!f.exists())
				continue;
			JarFile jar = new JarFile(f);
			Attributes attr = jar.getManifest().getMainAttributes();
			String st = attr.getValue(Attributes.Name.SPECIFICATION_TITLE);
			String it = attr.getValue(Attributes.Name.IMPLEMENTATION_TITLE);
			String sv = attr.getValue(Attributes.Name.SPECIFICATION_VERSION);
			String iv = attr.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
			String str = st + (st.equals(it) ? "":" (" + it + ")") + " - v."+ sv + ((iv.equals(sv)) ? "":"_"+iv);
			strs.add(str);
		}
		
		pkgStrings = strs.toArray(new String[strs.size()]);
	}
	
	/**
	 * Copied from org.madeirahs.editor.launch.InitLauncher
	 * @param cls
	 * @return
	 */
	private String getLocalJarName(Class<?> cls) {
		String cn = cls.getName();
		String rn = cn.replace('.', '/') + ".class";
		String path =
				ClassLoader.getSystemClassLoader().getResource(rn).getPath();
		int ind = path.lastIndexOf("!");
		if(ind < 0)
			return "nojar";
		String jpath = path.substring(0, ind);
		return jpath.substring(jpath.lastIndexOf("/") + 1, jpath.length()).replace("%20", " ");
	}
	
	private class HeaderPanel extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7664266457232483489L;
		
		BufferedImage img;
		int width, height;
		
		public HeaderPanel() {
			try {
				img = ImageIO.read(ClassLoader.getSystemClassLoader().getResource(IMG_HEADER_PATH));
				width = img.getWidth();
				height = img.getHeight();
				setPreferredSize(new Dimension(width, height));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void paintComponent(Graphics g) {
			g.drawImage(img, 0, 0, null);
		}
	}
}
