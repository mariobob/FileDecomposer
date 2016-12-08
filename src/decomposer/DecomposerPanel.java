package decomposer;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import java.awt.*;
import java.awt.event.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;

import static decomposer.DecomposerConstants.*;

/**
 * All the necessary GUI components and functions for decomposing are in this
 * panel. This is one of the tabs from the main window, where the other one is
 * {@linkplain ComposerPanel}.
 *
 * @author Mario Bobic
 */
public class DecomposerPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private String fileParent;
	private String fileName;
	private Long fileLength;
	private Long pieceSize;
	private Integer pieces;
	
	private JTextField filePathTf;
	private JTextField fileNameTf;
	private JTextField fileSizeTf;
	private JTextField newPathTf;
	private JTextField newNameTf;
	private JTextField piecesTf;
	private JTextField pieceSizeTf;
	
	private JButton newBtn;
	private JButton decomposeBtn;
	
	private JFileChooser chooser = new JFileChooser();
	
	private JProgressBar progressBar;
	
	private MouseListener loadMouseListener = new MouseListener() {
		public void mouseReleased(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			loadFile();
		}
	};
	private MouseListener saveMouseListener = new MouseListener() {
		public void mouseReleased(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			saveFile();
		}
	};
	
	/**
	 * Constructs and initializes this panel with GUI components.
	 */
	public DecomposerPanel() {
		/* Set this panel's style. */
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		/* Create and add physically independent panels. */
		JPanel upper = new JPanel(new GridLayout(0, 1, 0, 10));
		JPanel lower = new JPanel(new GridLayout(0, 1, 0, 10));

		add(upper, BorderLayout.PAGE_START);
		add(lower, BorderLayout.PAGE_END);
		
		/* Create and add the file properties panel. */
		JPanel fileProperties = new JPanel(new BorderLayout(5, 0));
		fileProperties.setBorder(BorderFactory.createTitledBorder("File properties"));
		upper.add(fileProperties);
		
		/* Create the file properties labels. */
		JPanel filePropertiesLabels = new JPanel(new GridLayout(0, 1, 0, 5));
		JLabel filePathLbl = new JLabel("File path:", SwingConstants.RIGHT);
		JLabel fileNameLbl = new JLabel("File name:", SwingConstants.RIGHT);
		JLabel sizeLbl = new JLabel("File size:", SwingConstants.RIGHT);
		filePropertiesLabels.add(filePathLbl);
		filePropertiesLabels.add(fileNameLbl);
		filePropertiesLabels.add(sizeLbl);
		fileProperties.add(filePropertiesLabels, BorderLayout.LINE_START);
		
		/* Create the file properties text fields. */
		JPanel filePropertiesFields = new JPanel(new GridLayout(0, 1, 0, 5));
		filePathTf = new JTextField();
		fileNameTf = new JTextField();
		fileSizeTf = new JTextField("0 B");
		fileSizeTf.setEditable(false);
		filePropertiesFields.add(filePathTf);
		filePropertiesFields.add(fileNameTf);
		filePropertiesFields.add(fileSizeTf);
		fileProperties.add(filePropertiesFields, BorderLayout.CENTER);

		/* Make some text fields clickable - used for loading files. */
		filePathTf.addMouseListener(loadMouseListener);
		fileNameTf.addMouseListener(loadMouseListener);
		
		/* Listen for every update upon the file path text field
		 * and update the file parent variable. */
		DocListener filePathListener = (e) -> {
			fileParent = filePathTf.getText();
		};
		filePathTf.getDocument().addDocumentListener(filePathListener);
		
		/* Listen for every update upon the file name text field
		 * and update the file name variable. */
		DocListener fileNameListener = (e) -> {
			fileName = fileNameTf.getText();
		};
		fileNameTf.getDocument().addDocumentListener(fileNameListener);
		
		/* Create and add the file controls panel. */
		JPanel fileControls = new JPanel(new BorderLayout(5, 0));
		fileControls.setBorder(BorderFactory.createTitledBorder("File controls"));
		upper.add(fileControls);
		
		/* Create the file controls labels. */
		JPanel fileControlsLabels = new JPanel(new GridLayout(0, 1, 0, 5));
		JLabel newPathLbl = new JLabel("Decomposed file's path: ", SwingConstants.RIGHT);
		JLabel newNameLbl = new JLabel("Decomposed file's name: ", SwingConstants.RIGHT);
		JLabel piecesLbl = new JLabel("Pieces: ", SwingConstants.RIGHT);
		fileControlsLabels.add(newPathLbl);
		fileControlsLabels.add(newNameLbl);
		fileControlsLabels.add(piecesLbl);
		fileControls.add(fileControlsLabels, BorderLayout.LINE_START);
		
		/* Create the file controls text fields. */
		JPanel fileControlsFields = new JPanel(new GridLayout(0, 1, 0, 5));
		newPathTf = new JTextField();
		newNameTf = new JTextField();
		piecesTf = new JTextField();
		fileControlsFields.add(newPathTf);
		fileControlsFields.add(newNameTf);
		fileControlsFields.add(piecesTf);
		fileControls.add(fileControlsFields, BorderLayout.CENTER);
		
		/* Make some text fields clickable - used for saving new files. */
		newPathTf.addMouseListener(saveMouseListener);
		
		/* Listen for every update upon the pieces text field
		 * and update the pieces size text field. */
		DocListener piecesListener = (e) -> {
			try {
				pieces = Integer.parseInt(piecesTf.getText());
				if (pieces < MIN_PIECES || pieces > MAX_PIECES) throw new NumberFormatException();
				setPieces(fileLength, pieces);
			} catch (Exception exc) {
				pieces = -1;
				pieceSizeTf.setText("");
			}
		};
		piecesTf.getDocument().addDocumentListener(piecesListener);
		
		
		/* Create a specially designed panel to fit the progress and the button. */
		JPanel progressPanel = new JPanel(new BorderLayout(10, 0));
		lower.add(progressPanel);
		
		/* Create and add a progress bar to this panel. */
		JLabel progressLabel = new JLabel("Progress:");
		progressPanel.add(progressLabel, BorderLayout.LINE_START);
		
		progressBar = new JProgressBar();
		progressPanel.add(progressBar, BorderLayout.CENTER);
		
		/* Add a new file button to this panel. */
		newBtn = new JButton("New file");
		newBtn.addActionListener((e) -> {
			loadFile();
		});
		progressPanel.add(newBtn, BorderLayout.LINE_END);
		
		/* Create and add the info and buttons panel. */
		JPanel lowerInfo = new JPanel(new BorderLayout(5, 0));
		JPanel lowerBtns = new JPanel(new GridLayout(1, 0));
		
		lower.add(lowerInfo);
		lower.add(lowerBtns);
		
		/* Add piece size info. */
		JLabel pieceSizeLbl = new JLabel("Piece size:", SwingConstants.RIGHT);
		pieceSizeTf = new JTextField("0 B");
		pieceSizeTf.setEditable(false);
		lowerInfo.add(pieceSizeLbl, BorderLayout.LINE_START);
		lowerInfo.add(pieceSizeTf, BorderLayout.CENTER);
		
		/* This is what this program is all about. */
		decomposeBtn = new JButton("Decompose");
		lowerBtns.add(decomposeBtn);
		
		decomposeBtn.addActionListener((e) -> {
			decompose();
		});
	}
	
	/**
	 * Prepare the decomposing by disabling GUI components, some listeners and
	 * creating and executing the {@linkplain SwingWorker} task.
	 */
	protected void decompose() {
		/* Prepare fields for using. */
		fileParent = filePathTf.getText();
		fileName = fileNameTf.getText();

		/* Reset the progress. */
		progressBar.setValue(0);
		
		/* Disable GUI components. */
		decomposeBtn.setEnabled(false);
		newBtn.setEnabled(false);
		filePathTf.setEnabled(false);
		fileNameTf.setEnabled(false);
		newPathTf.setEnabled(false);
		newNameTf.setEnabled(false);
		piecesTf.setEnabled(false);
		
		filePathTf.removeMouseListener(loadMouseListener);
		fileNameTf.removeMouseListener(loadMouseListener);
		newPathTf.removeMouseListener(saveMouseListener);

		/* Execute the task on a working thread and listen for progress change. */
		DecomposeWorker worker = new DecomposeWorker();

		worker.addPropertyChangeListener((evt) -> {
			if ("progress".equals(evt.getPropertyName())) {
				progressBar.setValue((Integer) evt.getNewValue());
			}
		});
		worker.execute();
	}

	/**
	 * Creates a dialog for the user to select the file to be decomposed.
	 */
	protected void loadFile() {
		/* Reset the filter and set selectable to files only. */
		chooser.setDialogTitle("New file");
		chooser.resetChoosableFileFilters();
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		/* After the window has closed, get the selected file
		 * and store this file's info to class variables. */
		int retVal = chooser.showOpenDialog(this);
		
		if (retVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			fileParent = file.getParent();
			fileName = file.getName();
			fileLength = file.length();
			
			filePathTf.setText(fileParent);
			fileNameTf.setText(fileName);
			fileSizeTf.setText(humanReadableByteCount(fileLength));
			
			newPathTf.setText(fileParent);
			newNameTf.setText(fileName + FILE_EXTENSION);
			piecesTf.setText(Integer.toString(OPTIMAL_NUM_PIECES));
			
			setPieces(fileLength, OPTIMAL_NUM_PIECES);
		}
	}
	
	/**
	 * Creates a dialog for the user to select the desired save path.
	 */
	protected void saveFile() {
		/* Directory only view and select mode. */
		chooser.setDialogTitle("Save path");
		chooser.setFileFilter(new DirectoryFilter());
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		/* Only the desired new path is important. */
		int retVal = chooser.showOpenDialog(this);
		
		if (retVal == JFileChooser.APPROVE_OPTION) {
			File dir = chooser.getSelectedFile();
			newPathTf.setText(dir.toString());
		}
	}
	
	/**
	 * Sets the piece size based on the given file length and number of pieces.
	 * Number of pieces is {@linkplain DecomposerPanel#OPTIMAL_NUM_PIECES} by default,
	 * but may be manually input from the user.
	 * 
	 * @param fileLength length of the file in bytes
	 * @param numPieces number of pieces
	 */
	private void setPieces(long fileLength, int numPieces) {
		/* Every piece size is equal (except eventually the last one),
		 * all with the added number of bytes used to represent an int
		 * value in two's complement binary form.
		 * This is because the integer is used as an implant. */
		pieceSize = fileLength / numPieces + Integer.SIZE / 8;
		pieceSizeTf.setText(humanReadableByteCount(pieceSize));
	}
	
	/**
	 * Converts the number of bytes to a human readable byte count with binary
	 * prefixes.
	 * 
	 * @param bytes number of bytes
	 * @return human readable byte count with binary prefixes
	 */
	public static String humanReadableByteCount(long bytes) {
		/* Use the natural 1024 units and binary prefixes. */
		int unit = 1024;
		if (bytes < unit) return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = "kMGTPE".charAt(exp - 1) + "i";
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	/**
	 * File filter used to filter out directories only. This is used by the
	 * chooser upon saving the desired file path.
	 *
	 * @author Mario Bobic
	 */
	private static class DirectoryFilter extends FileFilter {
		@Override
		public boolean accept(File file) {
			if (file == null) {
				return false;
			}
			return file.isDirectory();
		}
		
		@Override
		public String getDescription() {
			return "Directories";
		}
	}
	
	/**
	 * A working thread for literally decomposing one large file to separate
	 * smaller files.
	 *
	 * @author Mario Bobic
	 */
	private class DecomposeWorker extends SwingWorker<Void, Void> {
		
		/**
		 * Creates new piece files in background.
		 */
		@Override
		protected Void doInBackground() throws Exception {
			/* Make all possible checks. */
			if (fileName.isEmpty()) {
				showError("Please specify an input file name and path");
				return null;
			}
			
			File file = Paths.get(fileParent, fileName).toFile();
			
			if (!file.exists()) {
				showError("File " + fileName + " doesn't exist");
				return null;
			}
			
			String outputParent = newPathTf.getText();
			String outputName = newNameTf.getText();
			
			if (outputParent.isEmpty() || outputName.isEmpty()) {
				showError("Output path and output name must not be empty");
				return null;
			}
			
			if (pieces == -1) {
				showError("Please enter a valid number of pieces between " + MIN_PIECES + " and " + MAX_PIECES);
				return null;
			}
			
			/* Set up the correct file name */
			if (!outputName.endsWith(FILE_EXTENSION)) {
				outputName += FILE_EXTENSION;
				newNameTf.setText(outputName);
			}
			
			/* If the user has manually entered the output directory,
			 * make sure that they exist before creating the file. */
			File outputDir = new File(outputParent);
			if (!outputDir.exists()) {
				outputDir.mkdirs();
			}
			
			/* If program makes it to this point, decomposing is good to go. */
			File inputFile = new File(fileParent, fileName);
			outputName = outputName.replace(FILE_EXTENSION, "");
			FileInputStream in = new FileInputStream(inputFile);
			
			/* If piece size is less than the standard loader size,
			 * file will be loaded and saved piece by piece. */
			int loaderSize = getLoaderSize();
			
			/* Total number of bytes loaded.
			 * User for creating the last piece and tracking the progress. */
			long totalLoaded = 0;
			
			/* Remembers the piece size because it may be changed during the
			 * writing of the last piece. */
			long rememberedPieceSize = pieceSize;
			
			/* Create the desired number of pieces.
			 * Every iteration creates one new piece file. */
			for (int i = 0; i < pieces; i++) {
				String index = (i < 10 ? "-0" : "-") + i;
				File newFile = new File(outputDir, outputName + index + FILE_EXTENSION);
				
				/* If this is the last piece */
				if (i == pieces - 1) {
					pieceSize = fileLength - totalLoaded;
					loaderSize = getLoaderSize();
				}
				
				/* Writing to file starts. */
				try (FileOutputStream out = new FileOutputStream(newFile)) {
					/* Information to be implanted into all the pieces. */
					putImplant(out, i);
					
					byte[] buff = new byte[loaderSize];
					int len;
					long iterationLoaded = 0;
					while ((len = in.read(buff)) > 0) {
						out.write(buff, 0, len);
						iterationLoaded += len;
						totalLoaded += len;
						setProgress((int) (100 * totalLoaded / fileLength));
						if (iterationLoaded == pieceSize) {
							/* This piece is done */
							break;
						} else if (pieceSize - iterationLoaded < loaderSize) {
							/* This will be the last iteration of this while-loop */
							buff = new byte[(int) (pieceSize - iterationLoaded)];
						}
					}
				} catch (IOException e) {
					showError("An unknown I/O error has occured!");
					in.close();
					return null;
				}
			}
			
			/* Restore the remembered piece size. */
			pieceSize = rememberedPieceSize;
			
			in.close();
			return null;
		}

		/**
		 * Simply re-enables every disabled GUI component.
		 */
		@Override
		protected void done() {
			/* Re-enable GUI components */
			decomposeBtn.setEnabled(true);
			newBtn.setEnabled(true);
			filePathTf.setEnabled(true);
			fileNameTf.setEnabled(true);
			newPathTf.setEnabled(true);
			newNameTf.setEnabled(true);
			piecesTf.setEnabled(true);
			
			/* Re-enable mouse listeners */
			filePathTf.addMouseListener(loadMouseListener);
			fileNameTf.addMouseListener(loadMouseListener);
			newPathTf.addMouseListener(saveMouseListener);
		}
		
		/**
		 * Returns the optimal byte array size used for loading from the input
		 * file. More formally, this method returns the smaller value between
		 * the standard loader size and piece size.
		 * 
		 * @return the smaller value between the standard loader size and piece size
		 */
		private int getLoaderSize() {
			return pieceSize < STD_LOADER_SIZE ? pieceSize.intValue() : STD_LOADER_SIZE;
		}
		
		/**
		 * Writes the implant out to the file output stream, with this piece's
		 * ordinal number.
		 * 
		 * @param out FileOutputStream of this piece
		 * @param i this piece's ordinal number
		 * @throws IOException if an I/O exception occurs
		 */
		private void putImplant(FileOutputStream out, int i) throws IOException {
			ByteBuffer b = ByteBuffer.allocate(IMPLANT_LENGTH);
			b.putInt(i);
			byte[] result = b.array();
			out.write(result);
		}
		
		/**
		 * Shows an error message with the desired text.
		 * 
		 * @param message desired text
		 */
		private void showError(String message) {
			JOptionPane.showMessageDialog(DecomposerPanel.this, message, "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

}
