/*
// *  The MHS-Collections Project editor is intended for use by Historical Society members
 *  to edit, review and upload artifact information.
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
 */

package org.madeirahs.editor.ui;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import org.madeirahs.editor.main.*;
import org.madeirahs.editor.net.*;
import org.madeirahs.shared.*;
import org.madeirahs.shared.database.*;
import org.madeirahs.shared.provider.*;
import org.madeirahs.shared.time.*;
import org.madeirahs.shared.v3d.*;

/**
 * 
 * @author Brian Groenke
 * 
 */
public class MainUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5243250274005223523L;

	public static final int SAVED_STATE = 0xAF0, CHANGED_STATE = 0xAF1,
			BLANK_STATE = 0xAF2;
	public static final Font DEFAULT_LABEL_FONT = new Font("Helvetica",
			Font.PLAIN, 13);
	public static final Insets SPACE_BOTTOM = new Insets(0, 0, 10, 0),
			NO_SPACE = new Insets(0, 0, 0, 0);

	public static final String ARTIFACT_EXT = ".ser",
			ICON_LOC = AppSupport.JAR_PKG_UI + "/mhs_editor_icon2.png",
			TMP_FILES_PTH = System.getProperty("java.io.tmpdir")
			+ File.separator + "mhseditor" + File.separator;

	public static final FileProvider FILE_PROV = new FileProvider();

	static {
		new File(TMP_FILES_PTH).mkdir();
	}

	MainUI mui = this;
	JPanel rootPanel, viewPanel, fieldPanel;
	ArtifactView preview;
	JTextField titleField, subDateField, donorField, dateField, mediumField,
	numField;
	JTextArea descArea;
	JList fileList;
	JMenuBar menuBar;
	JMenu file, tools, server, help;
	JMenuItem exit, progDir, manage, load, savem, upload, sub, settings,
	reload, viewdir, newdoc, info, about;
	String title;

	private DataProvider provider = FILE_PROV;
	private Map<String, String> nameMap = new HashMap<String, String>();
	private String[] fromFtpCopy;
	private DropTarget fileDrop;
	private SaveAction save = new SaveAction();
	private int docState = BLANK_STATE;
	private boolean configActive, ftp;

	/**
	 * @param windowTitle
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws UnsupportedLookAndFeelException
	 * @throws ParseException
	 * @throws IOException
	 */
	public void createGUI(String windowTitle) throws ClassNotFoundException,
	InstantiationException, IllegalAccessException,
	UnsupportedLookAndFeelException, ParseException, IOException {

		/*
		boolean lfset = false;
		for (LookAndFeelInfo linf : UIManager.getInstalledLookAndFeels()) {
			if (linf.getClassName().toLowerCase().contains("nimbus")) {
				UIManager.setLookAndFeel(linf.getClassName());
				lfset = true;
				break;
			}
		}
		if (!lfset) {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		*/
		UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");

		setTitle(windowTitle);
		setIconImage(Toolkit.getDefaultToolkit().getImage(
				getClass().getClassLoader().getResource(ICON_LOC)));
		title = windowTitle;
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setAlwaysOnTop(false);
		addWindowListener(new ExitCheck());

		buildMenuBar();

		rootPanel = new JPanel(new BorderLayout());
		//rootPanel.setBackground(new Color(0xE6EBF0));

		buildGUI();

		setContentPane(rootPanel);
		pack();
		setLocationRelativeTo(null);
		validate();
		setVisible(true);
	}


	private void buildGUI() {
		fieldPanel = new JPanel(new GridBagLayout());
		fieldPanel.setOpaque(false);
		fieldPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
		GridBagConstraints c1 = new GridBagConstraints();
		viewPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c2 = new GridBagConstraints();
		viewPanel.setOpaque(false);
		viewPanel.setBorder(new EmptyBorder(5, 10, 5, 20));

		String actionKey = "save";
		rootPanel.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
		.put(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				InputEvent.CTRL_DOWN_MASK, false), actionKey);
		rootPanel.getActionMap().put(actionKey, save);

		JLabel title = new JLabel("Title");
		title.setFont(DEFAULT_LABEL_FONT);
		c1.anchor = GridBagConstraints.NORTHWEST;
		c1.weighty = 1.0;
		c1.gridx = 0;
		c1.gridy = 0;
		c1.fill = GridBagConstraints.NONE;
		c1.gridwidth = 1;
		fieldPanel.add(title, c1);
		// Title field
		titleField = new JTextField(30);
		TextEditor t1 = new TextEditor();
		titleField.getDocument().addUndoableEditListener(t1);
		titleField.addKeyListener(t1);
		// ----
		c1.gridy = 1;
		c1.gridwidth = 3;
		c1.insets = SPACE_BOTTOM;
		fieldPanel.add(titleField, c1);
		c1.gridy = 2;
		c1.gridwidth = 1;
		c1.insets = NO_SPACE;
		JLabel donor = new JLabel("Donor");
		donor.setFont(DEFAULT_LABEL_FONT);
		fieldPanel.add(donor, c1);
		// Donor field
		donorField = new JTextField(30);
		TextEditor t2 = new TextEditor();
		donorField.getDocument().addUndoableEditListener(t2);
		donorField.addKeyListener(t2);
		// ----
		c1.gridy = 3;
		c1.gridwidth = 3;
		c1.insets = SPACE_BOTTOM;
		fieldPanel.add(donorField, c1);
		JLabel subDate = new JLabel("Submission Date");
		subDate.setFont(DEFAULT_LABEL_FONT);
		c1.insets = NO_SPACE;
		c1.gridy = 4;
		c1.gridwidth = 1;
		fieldPanel.add(subDate, c1);
		// Submission Date Field
		subDateField = new JTextField(30);
		TextEditor t3 = new TextEditor();
		subDateField.getDocument().addUndoableEditListener(t3);
		subDateField.addKeyListener(t3);
		// ----
		c1.gridy = 5;
		c1.gridwidth = 3;
		c1.insets = SPACE_BOTTOM;
		fieldPanel.add(subDateField, c1);
		JLabel objDate = new JLabel("Date of Object");
		objDate.setFont(DEFAULT_LABEL_FONT);
		c1.insets = NO_SPACE;
		c1.gridy = 6;
		c1.gridwidth = 1;
		fieldPanel.add(objDate, c1);
		// Object Date Field
		dateField = new JTextField(30);
		TextEditor t4 = new TextEditor();
		dateField.getDocument().addUndoableEditListener(t4);
		dateField.addKeyListener(t4);
		// ----
		c1.gridy = 7;
		c1.gridwidth = 3;
		c1.insets = SPACE_BOTTOM;
		fieldPanel.add(dateField, c1);
		JLabel medium = new JLabel("Medium");
		medium.setFont(DEFAULT_LABEL_FONT);
		c1.insets = NO_SPACE;
		c1.gridy = 8;
		c1.gridwidth = 1;
		fieldPanel.add(medium, c1);
		// Medium field
		mediumField = new JTextField(30);
		TextEditor t5 = new TextEditor();
		mediumField.getDocument().addUndoableEditListener(t5);
		mediumField.addKeyListener(t5);
		// ----
		c1.gridy = 9;
		c1.gridwidth = 3;
		c1.insets = SPACE_BOTTOM;
		fieldPanel.add(mediumField, c1);
		JLabel num = new JLabel("Accession #");
		num.setFont(DEFAULT_LABEL_FONT);
		c1.insets = NO_SPACE;
		c1.gridy = 10;
		c1.gridwidth = 1;
		fieldPanel.add(num, c1);
		// Acc # Field
		numField = new JTextField(30);
		TextEditor t6 = new TextEditor();
		numField.getDocument().addUndoableEditListener(t6);
		numField.addKeyListener(t6);
		// ----
		c1.gridy = 11;
		c1.gridwidth = 3;
		c1.insets = SPACE_BOTTOM;
		fieldPanel.add(numField, c1);
		JLabel desc = new JLabel("Description");
		desc.setFont(DEFAULT_LABEL_FONT);
		c1.insets = NO_SPACE;
		c1.gridy = 12;
		c1.gridwidth = 1;
		fieldPanel.add(desc, c1);
		// Description area
		descArea = new JTextArea(8, 30);
		descArea.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		descArea.setBorder(new MatteBorder(1, 1, 1, 1, Color.BLACK));
		descArea.setLineWrap(true);
		descArea.setWrapStyleWord(true);
		TextEditor t7 = new TextEditor();
		descArea.getDocument().addUndoableEditListener(t7);
		descArea.addKeyListener(t7);
		// ----
		c1.gridy = 13;
		c1.gridheight = 4;
		c1.gridwidth = 3;
		c1.insets = SPACE_BOTTOM;
		fieldPanel.add(new JScrollPane(descArea), c1);
		
		JLabel rscs = new JLabel("Resources");
		rscs.setFont(DEFAULT_LABEL_FONT);
		c1.insets = NO_SPACE;
		c1.gridy = 17;
		c1.gridheight = 1;
		c1.gridwidth = 1;
		fieldPanel.add(rscs, c1);
		fileList = new JList();
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			fileDrop = new DropTarget(fileList, new ListFileListener());
		}
		fileList.addMouseListener(new ListFileListener());
		c1.gridy = 18;
		c1.gridheight = 3;
		c1.gridwidth = 3;
		JScrollPane flhldr = new JScrollPane(fileList);
		flhldr.setPreferredSize(new Dimension(
				descArea.getPreferredSize().width, (int) (descArea
						.getPreferredSize().height / 1.25f)));
		flhldr.setBorder(new MatteBorder(1, 1, 1, 1, Color.BLUE));
		fieldPanel.add(flhldr, c1);

		preview = new ArtifactView(FILE_PROV);
		preview.setPreferredSize(new Dimension(600, 450));
		c2.anchor = GridBagConstraints.CENTER;
		c2.gridheight = 3;
		viewPanel.add(preview, c2);

		rootPanel.add(BorderLayout.WEST, fieldPanel);
		rootPanel.add(BorderLayout.EAST, viewPanel);
		rootPanel.validate();
	}


	private void buildMenuBar() {
		menuBar = new JMenuBar();

		file = new JMenu("File");
		tools = new JMenu("Tools");
		server = new JMenu("Server");
		help = new JMenu("Help");

		newdoc = new JMenuItem("New");
		newdoc.addActionListener(new CreateNew());
		file.add(newdoc);

		load = new JMenuItem("Load");
		load.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				LoadUI lui = new LoadUI(mui);
				lui.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				lui.setSize(600, 400);
				lui.setLocationRelativeTo(mui);
				lui.setVisible(true);
			}

		});
		file.add(load);

		savem = new JMenuItem("Save");
		savem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				save.saveData();
				mui.setTitle(title);
			}
		});
		file.add(savem);
		
		upload = new JMenuItem("Upload");
		upload.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						if (preview.elem.accNum == null
								|| preview.elem.accNum.isEmpty()) {
							JOptionPane
							.showMessageDialog(mui,
									"Please enter an accession number before uploading (for naming purposes).");
							return;
						}
						Artifact a = new Artifact(preview.elem);
						a.filenames = nameMap.keySet().toArray(
								new String[nameMap.size()]);
						setFieldData(a);
						try {
							new UploadUI(FILE_PROV, mui, a, fromFtpCopy).start();
						} catch (IOException e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog(mui, "Failed to initialize upload service:\n"+e.toString(), "I/O Error", JOptionPane.ERROR_MESSAGE);
						}
					}

				}).start();
			}
		});
		file.add(upload);

		exit = new JMenuItem("Exit");
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				performExit();
			}
		});
		file.add(exit);

		manage = new JMenuItem("Manage");
		manage.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ManageUI manage = new ManageUI(mui, "Manage Server");
				manage.setVisible(true);
			}

		});
		server.add(manage);

		sub = new JMenuItem("View Submissions");
		sub.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new SubviewUI(mui, ServerFTP.getProvider()).setVisible(true);
			}

		});
		server.add(sub);

		reload = new JMenuItem("Rebuild 'ArtifactView'");
		reload.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				preview.removeAll();
				preview.setup();
				preview.rebuild();
			}

		});
		tools.add(reload);

		viewdir = new JMenuItem("View program files");
		viewdir.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean success = true;
				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().open(AppSupport.APP_DIR);
					} catch (IOException e1) {
						e1.printStackTrace();
						success = false;
					}
				} else {
					success = false;
				}

				if (!success) {
					JOptionPane.showMessageDialog(mui,
							"Unable to launch system file explorer.\nProgram directory location: "
									+ AppSupport.APP_DIR.getAbsolutePath(),
									"Desktop not supported", JOptionPane.ERROR_MESSAGE);
				}
			}

		});
		tools.add(viewdir);
		
		settings = new JMenuItem("Settings");
		settings.addActionListener(new ViewSettings());
		tools.add(settings);
		
		info = new JMenuItem("Info");
		info.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				HelpUI hui = new HelpUI(mui, "MHS Collections Editor Help and Information Center");
				hui.setVisible(true);
			}
		});
		help.add(info);
		
		about = new JMenuItem("About");
		about.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				AboutUI ui = new AboutUI(mui, "Software Information");
				ui.setVisible(true);
			}
			
		});
		help.add(about);

		menuBar.add(file);
		menuBar.add(Box.createHorizontalStrut(10));
		menuBar.add(tools);
		menuBar.add(Box.createHorizontalStrut(10));
		menuBar.add(server);
		menuBar.add(Box.createHorizontalStrut(10));
		menuBar.add(help);
		setJMenuBar(menuBar);
	}


	/**
	 * Updates the UI list with the current ArtifactView element's filename
	 * list.
	 */
	private void fillFileList() {

		preview.elem.filenames = new String[nameMap.size()];
		int i = 0;
		for (String s : nameMap.keySet()) {
			preview.elem.filenames[i] = nameMap.get(s);
			i++;
		}

		fileList.setListData(new Vector<String>(nameMap.keySet()));

		preview.updateV3D();
		preview.rebuild();
	}


	private void setFieldData(Artifact a) {
		a.fieldValues = new String[7];
		int i = 0;
		a.fieldValues[i++] = titleField.getText();
		a.fieldValues[i++] = donorField.getText();
		a.fieldValues[i++] = subDateField.getText();
		a.fieldValues[i++] = dateField.getText();
		a.fieldValues[i++] = mediumField.getText();
		a.fieldValues[i++] = numField.getText();
		a.fieldValues[i++] = descArea.getText();
	}


	public void performExit() {
		if (docState != CHANGED_STATE) {
			System.exit(0);
		} else {
			if(notifyUnsaved())
				System.exit(0);
		}
	}

	public void loadArtifactFromSystem(SavedArtifact saved) {
		
		if(docState == CHANGED_STATE && !notifyUnsaved())
			return;
		
		ftp = saved.loadedRemote;

		// Replace the old preview panel with a new one.
		viewPanel.remove(preview);
		GridBagConstraints pc = ((GridBagLayout) viewPanel.getLayout())
				.getConstraints(preview); // We need to recover the constraint
		// values before changing.
		Dimension size = preview.getPreferredSize(); // Recover the preferred
		// size
		MultiProvider multiprov = new MultiProvider(FILE_PROV,
				ServerFTP.getProvider());
		multiprov.setDefaultOutputProvider(ServerFTP.getProvider());
		provider = multiprov;
		preview = new ArtifactView(saved.element, provider);
		preview.setPreferredSize(size);
		viewPanel.add(preview, pc);
		// ---

		nameMap.clear();
		nameMap.putAll(saved.nameMap);
		for (String s : nameMap.keySet()) {
			File f = new File(nameMap.get(s));
			if (f.exists()) {
				FILE_PROV.putListing(s, f);
			}
		}

		int i = 0;
		titleField.setText(saved.element.fieldValues[i]);
		donorField.setText(saved.element.fieldValues[++i]);
		subDateField.setText(saved.element.fieldValues[++i]);
		dateField.setText(saved.element.fieldValues[++i]);
		mediumField.setText(saved.element.fieldValues[++i]);
		numField.setText(saved.element.fieldValues[++i]);
		descArea.setText(saved.element.fieldValues[++i]);
		fromFtpCopy = saved.element.filenames;
		fillFileList();
		preview.rebuild();
		preview.validate();
		rootPanel.validate();
	}


	/**
	 * Load an Artifact into the editor from the FTP server.
	 * 
	 * @param a
	 *            the LocalArtifact to load.
	 * @param prov
	 * 			  the FTPProvider to use
	 */
	public void loadArtifactFromServer(Artifact a, FTPProvider prov) {

		if(docState == CHANGED_STATE && !notifyUnsaved())
			return;
		
		ftp = true;
		MultiProvider multiprov = new MultiProvider(prov, FILE_PROV);
		multiprov.setDefaultOutputProvider(prov);
		provider = multiprov;

		// Replace the old preview panel with a new one.
		viewPanel.remove(preview);
		GridBagConstraints pc = ((GridBagLayout) viewPanel.getLayout())
				.getConstraints(preview); // We need to recover the constraint
		// values before changing.
		Dimension size = preview.getPreferredSize(); // Recover the preferred
		// size
		preview = new ArtifactView(a, provider);
		preview.setPreferredSize(size);
		viewPanel.add(preview, pc);
		// ---

		int i = 0;
		titleField.setText(a.fieldValues[i]);
		donorField.setText(a.fieldValues[++i]);
		subDateField.setText(a.fieldValues[++i]);
		dateField.setText(a.fieldValues[++i]);
		mediumField.setText(a.fieldValues[++i]);
		numField.setText(a.fieldValues[++i]);
		descArea.setText(a.fieldValues[++i]);
		fromFtpCopy = a.filenames;
		nameMap.clear();
		for (String s : a.filenames) {
			String[] pts = s.split("/");
			String name = pts[pts.length - 1];
			nameMap.put(name, s);
		}

		fillFileList();
		preview.rebuild();
		preview.validate();
		rootPanel.validate();
	}


	private boolean notifyUnsaved() {
		int reply = JOptionPane.showConfirmDialog(
				this,
				"Save changes to current document?",
				"Unsaved Data", JOptionPane.YES_NO_CANCEL_OPTION);
		switch(reply) {
		case JOptionPane.YES_OPTION:
			save.saveData();
		case JOptionPane.NO_OPTION:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Check doc status before exiting the program when the user closes the
	 * window.
	 * 
	 * @author Brian Groenke
	 * 
	 */
	class ExitCheck extends WindowAdapter {

		@Override
		public void windowClosing(WindowEvent we) {
			performExit();
		}

	}


	class ListFileListener extends MouseAdapter implements DropTargetListener {

		@Override
		public void dragEnter(DropTargetDragEvent dtde) {

		}

		@Override
		public void dragOver(DropTargetDragEvent dtde) {

		}

		@Override
		public void dropActionChanged(DropTargetDragEvent dtde) {

		}

		@Override
		public void dragExit(DropTargetEvent dte) {

		}

		@Override
		public void drop(DropTargetDropEvent dtde) {
			try {
				dtde.acceptDrop(DnDConstants.ACTION_COPY);
				List<File> droppedFiles = (List<File>) dtde.getTransferable()
						.getTransferData(DataFlavor.javaFileListFlavor);
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"imgs", "jpg", "jpeg", "png", "pdf");
				for (File file : droppedFiles) {
					if (filter.accept(file) && !file.isDirectory()) {
						nameMap.put(file.getName(), file.getPath());
						FILE_PROV.putListing(file.getName(), file);
					}
				}
				fillFileList();
			} catch (UnsupportedFlavorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
				JFileChooser jfc = new JFileChooser(
						System.getProperty("user.home"));
				jfc.setDialogTitle("Browse For Image Resources");
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"images (*.jpg,*.png,*.pdf)", "jpg", "jpeg", "png",
						"pdf");
				jfc.setFileFilter(filter);
				jfc.setMultiSelectionEnabled(true);
				int resp = jfc.showOpenDialog(mui);
				if (resp == JFileChooser.APPROVE_OPTION) {
					File[] files = jfc.getSelectedFiles();
					for (File f : files) {
						if (nameMap.keySet().contains(f.getName())) {
							JOptionPane
							.showMessageDialog(
									mui,
									f.getName()
									+ " already exists in the resource list.  Please remove it or use a different file name.");
							continue;
						}

						nameMap.put(f.getName(), f.getPath());
						FILE_PROV.putListing(f.getName(), f);
					}
					fillFileList();
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popup(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popup(e);
			}
		}

		private void popup(MouseEvent me) {
			JPopupMenu menu = new JPopupMenu();
			JMenuItem remv = new JMenuItem("Remove");
			remv.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					int count = 0;
					if ((count = fileList.getSelectedValues().length) > 0) {
						for (int i = 0; i < count; i++) {
							String name = (String) fileList.getSelectedValues()[i];
							nameMap.remove(name);
						}
						fillFileList();
					}
				}

			});
			JMenuItem config = new JMenuItem("Configure 3D");
			config.addActionListener(new V3DConfig());
			menu.add(config);
			menu.add(remv);
			menu.show(fileList, me.getX(), me.getY());
		}

	}


	/**
	 * Listener class responsible for handling undo/redo requests on text
	 * fields.
	 * 
	 * @author Brian Groenke
	 * 
	 */
	private class TextEditor implements UndoableEditListener, KeyListener {

		LinkedList<UndoableEdit> undoQueue = new LinkedList<UndoableEdit>();
		LinkedList<UndoableEdit> redoQueue = new LinkedList<UndoableEdit>();
		private final int EDIT_LIMIT = 50;

		@Override
		public void keyTyped(KeyEvent e) {
			//
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
				if (undoQueue.size() > 0 && undoQueue.peekLast().canUndo()) {
					UndoableEdit edit = undoQueue.pollLast();
					edit.undo();
					if (redoQueue.size() > EDIT_LIMIT) {
						redoQueue.removeFirst();
					}
					redoQueue.add(edit);
					updateElementData();
				}
			} else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Y) {
				if (redoQueue.size() > 0 && redoQueue.peekLast().canRedo()) {
					UndoableEdit edit = redoQueue.pollLast();
					edit.redo();
					if (undoQueue.size() > EDIT_LIMIT) {
						undoQueue.removeFirst();
					}
					undoQueue.add(edit);
					updateElementData();
				}
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			//
		}

		@Override
		public void undoableEditHappened(UndoableEditEvent e) {
			if (undoQueue.size() > EDIT_LIMIT) {
				undoQueue.removeFirst();
			}
			undoQueue.add(e.getEdit());
			redoQueue.clear();
			updateElementData();
		}

		/**
		 * 
		 */
		private void updateElementData() {
			JTextComponent field = null;
			try {
				field = (JTextComponent) mui.getFocusOwner();
			} catch (ClassCastException e) {
				return;
			}
			if (field == titleField) {
				preview.elem.title = field.getText();
			} else if (field == donorField) {
				preview.elem.donor = field.getText();
			} else if (field == mediumField) {
				preview.elem.medium = field.getText();
			} else if (field == numField) {
				preview.elem.accNum = field.getText();
			} else if (field == descArea) {
				preview.elem.desc = field.getText();
			} else if (field == dateField) {
				TimeSpec time = null;
				try {
					time = parseTime(field.getText());
				} catch (ParseException e) {
					return;
				}
				if (time != null) {
					preview.elem.objDate = time;
				}
			} else if (field == subDateField) {
				TimeSpec time = null;
				try {
					time = parseTime(field.getText());
				} catch (ParseException e) {
					return;
				}
				if (time != null) {
					preview.elem.subDate = time;
				}
			}
			preview.rebuild();
			docState = CHANGED_STATE;
			setTitle(title + " *Unsaved*");
		}

		/**
		 * <Internal Method> Used to determine the TimeSpec for time inputs. The
		 * returned TimeSpec will have the unparsed input value stored in its
		 * <code>syntaxString</code> field.
		 * 
		 * @param input
		 * @return
		 * @throws ParseException
		 */
		private TimeSpec parseTime(String input) throws ParseException {
			String[] strs = input.split("=");

			String literal = null;

			switch (strs.length) {
			case 2:
				literal = strs[1];
			case 1:
				String[] fpts = strs[0].split("\\|");
				if (fpts.length == 2) {
					SimpleDateFormat sdf = new SimpleDateFormat(fpts[0]);
					TimeSpec ts = _parseTime(fpts[1], sdf);
					if (ts != null) {
						ts.syntaxString = input;
						ts.forcedValue = literal;
					}
					return ts;
				} else if (fpts.length == 1) {
					DateFormat df = DateFormat
							.getDateInstance(DateFormat.MEDIUM);
					TimeSpec ts = _parseTime(fpts[0], df);
					if (ts != null) {
						ts.syntaxString = input;
						ts.forcedValue = literal;
					}
					return ts;
				}
			}

			return null;
		}

		/**
		 * <Internal method> Used to determine whether or not the specified date
		 * portion is a time range or specific time using the '-' character. The
		 * returned TimeSpec is of the corresponding type.
		 * 
		 * @param sect
		 * @param format
		 * @return the appropriate TimeSpec for this string.
		 * @throws ParseException
		 */
		private TimeSpec _parseTime(String sect, DateFormat format)
				throws ParseException {
			String[] ss = sect.split("-");
			TimeSpec spec = null;
			switch (ss.length) {
			case 1:
				Date d = format.parse(sect);
				if (d == null) {
					return null;
				} else {
					spec = new DateTime(d, format);
				}
			case 2:
				if (spec == null) {
					Date d1 = format.parse(ss[0]);
					Date d2 = format.parse(ss[1]);
					if (d1 == null && d2 == null) {
						return null;
					} else if (d1.before(d2)) {
						spec = new TimeFrame(d1, d2, format);
					}
				}
			default:
				return spec;
			}
		}
	}


	/**
	 * Configuration window for resources supported by V3D. Only one instance
	 * may be shown at a time.
	 * 
	 * @author Brian Groenke
	 * 
	 */
	class V3DConfig extends JDialog implements ActionListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1192661562580841841L;

		final String[] positions = new String[] { "Front", "Right", "Back",
				"Left", "Bottom", "Top" };
		private HashMap<String, JComboBox> map = new HashMap<String, JComboBox>();

		public V3DConfig() {
			super(mui, "3D Configuration");
		}

		/**
		 * Called to open a V3DConfig window upon occurrence of an ActionEvent;
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (!configActive) {
				if (nameMap.size() < 4 || nameMap.size() > 6) {
					JOptionPane.showMessageDialog(mui,
							"Invalid number of resources for V3D");
					return;
				}

				for (String s : nameMap.keySet()) {
					if (s.endsWith(".pdf")) {
						JOptionPane.showMessageDialog(mui,
								"V3D does not support PDF");
						return;
					}
				}
				setDefaultCloseOperation(DISPOSE_ON_CLOSE);
				addWindowFocusListener(new WindowFocusListener() {

					@Override
					public void windowGainedFocus(WindowEvent e) {

					}

					@Override
					public void windowLostFocus(WindowEvent e) {
						e.getWindow().requestFocus();
					}
				});

				JPanel root = new JPanel(new BorderLayout());
				root.setBackground(Color.WHITE);
				Box posbox = new Box(BoxLayout.Y_AXIS);
				posbox.setBorder(new EmptyBorder(10, 20, 20, 20));
				String[] names = new String[nameMap.keySet().size() + 1];
				int a = 0;
				names[a] = "";
				for (String s : nameMap.keySet()) {
					a++;
					names[a] = s;
				}

				for (int i = 0; i < positions.length; i++) {
					JPanel pan = new JPanel(new BorderLayout());
					pan.setOpaque(false);
					pan.add(BorderLayout.WEST, new JLabel(
							"<html><font face=\"Verdana\" size=5>"
									+ positions[i] + "</font></html>"));
					JComboBox jcb = new JComboBox(names);
					pan.add(BorderLayout.CENTER, Box.createHorizontalStrut(75));
					pan.add(BorderLayout.EAST, jcb);
					map.put(positions[i], jcb);
					posbox.add(pan);
					posbox.add(Box.createVerticalStrut(10));

					if (i > 3 && nameMap.size() < 5) {
						jcb.setEnabled(false);
					}
				}

				if (preview.elem.is3DSupported()) {
					V3DBundle bundle = preview.elem.bundle;
					String[] v3dnames = bundle.getFinalImageArray();

					outer: for (int i = 0; i < positions.length; i++) {
						switch (i) {
						case 4:
							if (bundle.getBundleType() == V3DBundle.BUNDLE_TYPE_LEVEL) {
								break outer;
							} else if (bundle.getBundleType() == V3DBundle.BUNDLE_TYPE_TOP) {
								continue outer;
							}
						case 5:
							if (bundle.getBundleType() != V3DBundle.BUNDLE_TYPE_TOP
							&& bundle.getBundleType() != V3DBundle.BUNDLE_TYPE_360) {
								break outer;
							}
						}
						
						for(String t:names) {
							if(t == null || t.isEmpty())
								continue;
							if(v3dnames[i].contains(t)) {
								map.get(positions[i]).setSelectedItem(t);
								break;
							}
						}
					}
				}

				JPanel buttons = new JPanel();
				buttons.setOpaque(false);
				JButton config = new JButton("Configure");
				config.addActionListener(new OnConfigure());
				JButton reset = new JButton("Reset");
				reset.addActionListener(new OnReset());
				reset.setPreferredSize(config.getMinimumSize());
				buttons.add(config);
				buttons.add(reset);

				root.add(BorderLayout.CENTER, posbox);
				root.add(BorderLayout.SOUTH, buttons);
				setContentPane(root);
				pack();
				setLocationRelativeTo(mui);
				setVisible(true);
				configActive = true;
			}
		}

		@Override
		public void dispose() {
			super.dispose();
			configActive = false;
		}

		class OnConfigure implements ActionListener {

			@Override
			public void actionPerformed(ActionEvent e) {
				String[] names = new String[positions.length];
				V3DBundle bundle = null;
				for (int i = 0; i < positions.length; i++) {
					if (nameMap.keySet().size() < i) {
						break;
					}
					String str = (String) map.get(positions[i])
							.getSelectedItem();
					names[i] = nameMap.get(str);
					switch (i) {
					case 0:
					case 1:
					case 2:
						break;
					case 3:
						bundle = new V3DBundle(names[0], names[1], names[2],
								names[3]);
						break;
					case 4:
						if (names[i] != null) {
							bundle.setBottomOnly(names[i]);
						}
						break;
					case 5:
						if (names[i] == null) {
							break;
						}

						if (names[i - 1] != null) {
							bundle.setTopBottom(names[i], names[i - 1]);
						} else {
							bundle.setTopOnly(names[i]);
						}
					}
				}
				
				System.out.println(bundle.getBundleType() == V3DBundle.BUNDLE_TYPE_TOP);

				try {
					preview.elem.configure3D(bundle);
					preview.rebuild();
					preview.updateV3D();
					preview.validate();
				} catch (V3DException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(null,
							"Error configuring V3D:\n" + e1.toString(),
							"V3DException", JOptionPane.ERROR_MESSAGE);
				} finally {
					dispose();
				}
			}

		}

		class OnReset implements ActionListener {

			@Override
			public void actionPerformed(ActionEvent e) {
				for (JComboBox jcb : map.values()) {
					jcb.setSelectedIndex(0);
				}
			}

		}
	}


	/**
	 * Action class responsible for responding to save actions. The
	 * ArtifactView's current artifact data is serialized to a file in the
	 * MHS-Editor/saves/ directory.
	 * 
	 * @author Brian Groenke
	 */
	private class SaveAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5457596547471110974L;

		protected SavedArtifact lsa;

		/**
		 * Action method overridden from AbstractAction that performs the save
		 * operation. Note that any currently existing files with the same name
		 * (same accession number) will be overwritten without warning.
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			saveData();
		}

		/**
		 * Saves the data entered in the current document.
		 * 
		 * @return true if successful, false otherwise.
		 */
		public boolean saveData() {
			if (preview.elem.accNum == null || preview.elem.accNum.isEmpty()) {
				JOptionPane
				.showMessageDialog(mui,
						"Please enter an accession number before saving (for naming purposes).");
				return false;
			}
			String ext = (ftp) ? "_remote" + ARTIFACT_EXT : ARTIFACT_EXT;
			File saveFile = new File(AppSupport.SAVE_DIR + File.separator
					+ preview.elem.accNum + ext);
			try {

				ObjectOutputStream oos = new ObjectOutputStream(
						new FileOutputStream(saveFile));
				/*
				 * Write a new LocalArtifact object containing all necessary
				 * local data. The field data should be passed in the order of
				 * which the fields are created in the GUI building code, so the
				 * loading process places them correctly.
				 */
				lsa = new SavedArtifact(preview.elem, nameMap, ftp,
						titleField.getText(), donorField.getText(),
						subDateField.getText(), dateField.getText(),
						mediumField.getText(), numField.getText(),
						descArea.getText());
				oos.writeObject(lsa);
				oos.close();
				docState = SAVED_STATE;
				mui.setTitle(title);
				return true;
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(mui,
						"Unable to save artifact data:\n" + e1.toString(),
						"Failed to complete save operation",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}

	}


	/**
	 * Clears data and rebuilds the current document from the ground up.
	 * 
	 * @author Brian Groenke
	 * 
	 */
	private class CreateNew implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			switch (docState) {
			case CHANGED_STATE:
				if(!notifyUnsaved())
					return;
			default:
				nameMap.clear();
				ftp = false;
				provider = FILE_PROV;

				/*
				 * Previous code - this works but deosn't clean up as nicely -
				 * and it uses instanceof (bad OO practice) for(Component
				 * c:fieldPanel.getComponents()) if(c instanceof JTextComponent)
				 * ((JTextComponent)c).setText(""); // Replace the old preview
				 * panel with a new one. viewPanel.remove(preview);
				 * GridBagConstraints pc = ((GridBagLayout)
				 * viewPanel.getLayout()) .getConstraints(preview); // We need
				 * to recover the constraint // values before changing.
				 * Dimension size = preview.getPreferredSize(); // Recover the
				 * preferred // size preview = new ArtifactView(FILE_PROV);
				 * preview.setPreferredSize(size); viewPanel.add(preview, pc);
				 * fillFileList();
				 */

				rootPanel.removeAll(); // get rid of EVERYTHING from the root
				buildGUI(); // rebuild the gui (not including the menu bar,
				// frame or root panel)
				fillFileList();
			}
		}
	}

	private class ViewSettings implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			SettingsUI ui = new SettingsUI(mui, "Settings");
			ui.setVisible(true);
		}
	}
}
