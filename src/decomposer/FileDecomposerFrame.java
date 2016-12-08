package decomposer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * This is the main class that provides view to {@linkplain Composer} and
 * {@linkplain Decomposer} panels. It also initializes the menu bar.
 *
 * @author Mario Bobic
 */
public class FileDecomposerFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private ComposerPanel composerPanel;
	private DecomposerPanel decomposerPanel;
	
	/**
	 * Constructs and initializes the window with GUI components.
	 */
	public FileDecomposerFrame() {
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setSize(640, 480);
		setMinimumSize(new Dimension(320, 240));
		setTitle("File Decomposer");
		
		initGUI();
		
		setLocationRelativeTo(null);
	}

	/**
	 * A helper GUI initializing method.
	 */
	private void initGUI() {
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		
		JPanel top = new JPanel(new GridLayout(0, 1));
		cp.add(top, BorderLayout.PAGE_START);
		
		top.add(createMenuBar());
		
		composerPanel = new ComposerPanel();
		decomposerPanel = new DecomposerPanel();

		JTabbedPane tabs = new JTabbedPane();
		tabs.add("Composer", composerPanel);
		tabs.add("Decomposer", decomposerPanel);
		tabs.setSelectedComponent(decomposerPanel);
		
		cp.add(tabs);
	}
	
	/**
	 * Creates and returns the menu bar to be added to the GUI.
	 * 
	 * @return the menu bar to be added to the GUI
	 */
	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		JMenu helpMenu = new JMenu("Help");
		
		menuBar.add(fileMenu);
		menuBar.add(helpMenu);
		
		fileMenu.setMnemonic(KeyEvent.VK_F);
		helpMenu.setMnemonic(KeyEvent.VK_H);
		
		JMenuItem newItem =
				createMenuItem(fileMenu, "New", KeyEvent.VK_N, "ctrl N", "Loads a file for decomposing");
		newItem.addActionListener((e) -> {
			decomposerPanel.loadFile();
		});
		
		JMenuItem openItem =
				createMenuItem(fileMenu, "Open", KeyEvent.VK_O, "ctrl O", "Loads decomposed files to be composed");
		openItem.addActionListener((e) -> {
			composerPanel.loadFiles();
		});
		
		JMenuItem composeItem =
				createMenuItem(fileMenu, "Compose", KeyEvent.VK_C, null, "Composes listed files with their default name");
		composeItem.addActionListener((e) -> {
			composerPanel.compose();
		});
		
		JMenuItem composeAsItem =
				createMenuItem(fileMenu, "Compose As...", -1, null, "Composes listed files with user specified name");
		composeAsItem.addActionListener((e) -> {
			JOptionPane.showMessageDialog(this, "This function has not been implemented yet", "JEBIGA", JOptionPane.INFORMATION_MESSAGE);
		});
		
		JMenuItem decomposeItem =
				createMenuItem(fileMenu, "Decompose", KeyEvent.VK_D, null, "Decomposes listed files with their default name");
		decomposeItem.addActionListener((e) -> {
			decomposerPanel.decompose();
		});
		
		JMenuItem decomposeAsItem =
				createMenuItem(fileMenu, "Decompose As...", -1, null, "Decomposes listed files with user specified name");
		decomposeAsItem.addActionListener((e) -> {
			decomposerPanel.saveFile();
		});
		
		fileMenu.addSeparator();
		
		JMenuItem exitItem =
				createMenuItem(fileMenu, "Exit", KeyEvent.VK_X, "ctrl X", "Exits the program");
		exitItem.addActionListener((e) -> {
			System.exit(0);
		});
		
		JMenuItem aboutItem =
				createMenuItem(helpMenu, "About File Decomposer", -1, null, "Shows information about this program");
		aboutItem.addActionListener((e) -> {
			showHelp();
		});
		
		return menuBar;
	}
	
	/**
	 * Creates a returns menu item based on the given parameters. The parent
	 * parameter and text must exist, while other parameters may be null or -1.
	 * 
	 * @param parent the parent to which this item will be added
	 * @param text text for this menu item
	 * @param mnemonic mnemonic for this menu item
	 * @param keyStrokeText shortcut key combination for this menu item
	 * @param tooltip tooltip message to be displayed for this menu item
	 * @return menu item based on the given parameters
	 */
	private JMenuItem createMenuItem(JMenu parent, String text, int mnemonic, String keyStrokeText, String tooltip) {
		JMenuItem item = new JMenuItem();
		
		item.setText(text);
		if (mnemonic != -1) {
			item.setMnemonic(mnemonic);
		}
		if (keyStrokeText != null) {
			KeyStroke accelerator = KeyStroke.getKeyStroke(keyStrokeText);
			item.setAccelerator(accelerator);
		}
		if (tooltip != null) {
			item.setToolTipText(tooltip);
		}
		
		parent.add(item);
		return item;
	}
	
	/**
	 * Shows the help dialog when the user clicks on the help menu item.
	 */
	private void showHelp() {
		JOptionPane.showMessageDialog(this, "Ma goni se u pičku materinu.", "About File Decomposer", JOptionPane.INFORMATION_MESSAGE);
	}
	
	/**
	 * Invokes the file decomposer frame and sets it as visible.
	 * 
	 * @param args not used
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new FileDecomposerFrame().setVisible(true);
		});
	}

}
