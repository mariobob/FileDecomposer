package decomposer;

import javax.swing.*;

import java.awt.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static decomposer.DecomposerConstants.*;

/**
 * All the necessary GUI components and functions for composing are in this
 * panel. This is one of the tabs from the main window, where the other one is
 * {@linkplain DecomposerPanel}.
 *
 * @author Mario Bobic
 */
public class ComposerPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	
	private DefaultListModel<File> listModel = new DefaultListModel<>();
	private JList<File> filesList = new JList<>(listModel);
	
	private Long totalSize;
	private Long pieceSize;
	private Integer pieces;
	
	private JTextField totalSizeTf;
	
	private JButton openBtn;
	private JButton composeBtn;
	
	private JFileChooser chooser = new JFileChooser();

	private JProgressBar progressBar;

	/** Indicates the user decision to continue composing
	 * if a piece has been tampered with */
	private boolean continueComposing = false;
	
	/**
	 * Constructs and initializes this panel with GUI components.
	 */
	public ComposerPanel() {
		/* Set this panel's style. */
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		/* Create and add physically independent panels. */
		JPanel center = new JPanel(new BorderLayout());
		JPanel lower = new JPanel(new GridLayout(0, 1, 0, 10));
		
		add(center, BorderLayout.CENTER);
		add(lower, BorderLayout.PAGE_END);
		
		/* Style the central panel and add the files list to it. */
		center.setBorder(BorderFactory.createTitledBorder("Files"));
		center.add(new JScrollPane(filesList));
		
		/* Create a specially designed panel to fit the progress and the button. */
		JPanel progressPanel = new JPanel(new BorderLayout(10, 0));
		lower.add(progressPanel);
		
		/* Create and add a progress bar to this panel. */
		JLabel progressLabel = new JLabel("Progress:");
		progressPanel.add(progressLabel, BorderLayout.LINE_START);
		
		progressBar = new JProgressBar();
		progressPanel.add(progressBar, BorderLayout.CENTER);
		
		/* Add an open file button to this panel. */
		openBtn = new JButton("Open files");
		openBtn.addActionListener((e) -> {
			loadFiles();
		});
		progressPanel.add(openBtn, BorderLayout.LINE_END);
		
		/* Create and add the info and buttons panel. */
		JPanel lowerInfo = new JPanel(new BorderLayout(9, 0));
		JPanel lowerBtns = new JPanel(new GridLayout(1, 0));
		
		lower.add(lowerInfo);
		lower.add(lowerBtns);
		
		/* Add total size info. */
		JLabel totalSizeLbl = new JLabel("Total size:", SwingConstants.RIGHT);
		totalSizeTf = new JTextField("0 B");
		totalSizeTf.setEditable(false);
		lowerInfo.add(totalSizeLbl, BorderLayout.LINE_START);
		lowerInfo.add(totalSizeTf, BorderLayout.CENTER);

		/* Create and add a compose button. */
		composeBtn = new JButton("Compose");
		lowerBtns.add(composeBtn);
		
		composeBtn.addActionListener((e) -> {
			compose();
		});
	}

	/**
	 * Prepare the composing by disabling GUI components, and creating and
	 * executing the {@linkplain SwingWorker} task.
	 */
	protected void compose() {
		/* Reset the progress. */
		progressBar.setValue(0);
		
		/* Disable GUI components. */
		composeBtn.setEnabled(false);
		openBtn.setEnabled(false);
		
		/* Execute the task on a working thread and listen for progress change. */
		ComposeWorker worker = new ComposeWorker();

		worker.addPropertyChangeListener((evt) -> {
			if ("progress".equals(evt.getPropertyName())) {
				progressBar.setValue((Integer) evt.getNewValue());
			}
		});
		worker.execute();
	}

	/**
	 * Creates a dialog for the user to select files to be composed.
	 */
	protected void loadFiles() {
		/* File chooser settings */
		chooser.setDialogTitle("Open files");
		chooser.setMultiSelectionEnabled(true);
		
		/* After the window has closed, get the selected file
		 * and store this file's info to class variables. */
		int retVal = chooser.showOpenDialog(this);
		
		if (retVal == JFileChooser.APPROVE_OPTION) {
			/* Get selected files and clear the previously selected. */
			File[] files = chooser.getSelectedFiles();
			listModel.clear();
			
			/* Used for setting the total size text field. */
			totalSize = 0L;
			
			/* Add newly selected files to the list model. */
			for (File file : files) {
				listModel.addElement(file);
				totalSize += file.length();
			}
			
			/* Correct the total size by subtracting the implant size for all pieces. */
			totalSize -= IMPLANT_LENGTH * files.length;
			
			/* Set the number of pieces and the piece size. */
			if ((pieces = files.length) != 0) {
				pieceSize = getPieceLength(files[0]);
			}

			/* Set the total size text field if it is not negative.
			 * The total size may be negative if the user has selected files
			 * that have a total size smaller than the total implant size. */
			if (totalSize > 0) {
				totalSizeTf.setText(DecomposerPanel.humanReadableByteCount(totalSize));
			} else {
				totalSizeTf.setText("");
			}
		}
	}
	
	private static long getPieceLength(File piece) {
		return piece.length() - IMPLANT_LENGTH;
	}
	
	/**
	 * A working thread for literally composing several small pieces to one
	 * large file.
	 *
	 * @author Mario Bobic
	 */
	private class ComposeWorker extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() throws Exception {
			/* Make all possible checks. */
			if (listModel.isEmpty()) {
				showError("No files selected");
				return null;
			}
			
			if (totalSizeTf.getText().isEmpty()) {
				showError("Incorrect files selected");
				return null;
			}
			
			/* Get one of the pieces. */
			File piece = listModel.get(0);
			
			/* Parse the original file name. */
			String pieceName = piece.getName();
			int endIndex = pieceName.lastIndexOf('-'); // OVO JE LOŠE IMPLEMENTIRANO!!!!!!!!!!!!!!!!!!
			String outputName = pieceName.substring(0, endIndex);
			
			/* Create a new file with the parsed output name. */
			File outputDir = piece.getParentFile();
			File outputFile = new File(outputDir, outputName);
			
			if (outputFile.exists()) {
				showError("File " + outputName + " already exists");
				return null;
			}
			
			/* If program makes it to this point, composing is good to go. */
			FileOutputStream out = new FileOutputStream(outputFile);
			
			/* If piece size is less than the standard loader size,
			 * file will be loaded and saved piece by piece. */
			int loaderSize = getLoaderSize();
			
			/* Total number of bytes loaded.
			 * User for merging the last piece and tracking the progress. */
			long totalLoaded = 0;
			
			/* Merge all pieces of file together.
			 * Every iteration merges one piece to the whole file. */
			for (int i = 0; i < pieces; i++) {
				File pieceFile = listModel.get(i);
				
				/* If this is the last piece */
				if (i == pieces - 1) {
					pieceSize = getPieceLength(pieceFile);
					loaderSize = getLoaderSize();
				}
				
				/* Reading from piece starts. */
				try (FileInputStream in = new FileInputStream(pieceFile)) {
					/* Check if this piece is valid. */
					checkImplant(in, i);
					
					byte[] buff = new byte[loaderSize];
					int len;
					long iterationLoaded = 0;
					while ((len = in.read(buff)) > 0) {
						out.write(buff, 0, len);
						iterationLoaded += len;
						totalLoaded += len;
						setProgress((int) (100 * totalLoaded / totalSize));
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
					out.close();
					return null;
				} catch (ArrayStoreException e) {
					showError("Decomposed pieces have been modified!");
					out.close();
					return null;
				}
			}
			
			out.close();
			return null;
		}
		
		/**
		 * Simply re-enables every disabled GUI component.
		 */
		@Override
		protected void done() {
			/* Re-enable GUI components */
			composeBtn.setEnabled(true);
			openBtn.setEnabled(true);
			
			/* Reset the implant warning value */
			continueComposing = false;
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
		 * Check if this piece has a valid implant, given by the {@code i}
		 * param. If the implant is incorrect or can not be found, an
		 * {@linkplain ArrayStoreException} is thrown.
		 * 
		 * @param out FileOutputStream of this piece
		 * @param i this piece's ordinal number
		 * @throws IOException if an I/O exception occurs
		 * @throws ArrayStoreException if the implant is invalid
		 */
		private void checkImplant(FileInputStream in, int i) throws IOException, ArrayStoreException {
			ByteBuffer b = ByteBuffer.allocate(IMPLANT_LENGTH);
			b.putInt(i);
			byte[] result = b.array();
			
			byte[] readings = new byte[result.length];
			in.read(readings);

			/* If arrays are not of the same contents, ask the user what to do.
			 * Continue with composing or stop the process?
			 * Note that the user is asked this only once upon composing. */
			if (!Arrays.equals(result, readings)) {
				if (continueComposing) return;
				
				continueComposing = showImplantWarning();
				if (!continueComposing) {
					throw new ArrayStoreException();
				}
			}
		}

		/**
		 * Shows an error message with the desired text.
		 * 
		 * @param message desired text
		 */
		protected void showError(String message) {
			JOptionPane.showMessageDialog(ComposerPanel.this, message, "Error", JOptionPane.ERROR_MESSAGE);
		}
		
		/**
		 * Shows a warning that one or more piece files have been tampered with
		 * and prompts the user to continue with composing or to cancel.
		 * This dialog is shown only once, no matter how much pieces are tampered with.
		 * 
		 * @return the user's decision to continue or not
		 */
		private boolean showImplantWarning() {
			int retVal = JOptionPane.showConfirmDialog(ComposerPanel.this, "Someone has tampered with the decomposed pieces. Continue?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			return retVal == JOptionPane.YES_OPTION ? true : false;
		}
	}
	
}
