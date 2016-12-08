package decomposer;

/**
 * A collection of constants generally used for composing and decomposing files.
 *
 * @author Mario Bobic
 */
public interface DecomposerConstants {

	/** Default file extension */
	public static final String FILE_EXTENSION = ".decomposed";
	/** Default implant length used for storing recovery information into the pieces */
	public static final int IMPLANT_LENGTH = Integer.SIZE / 8;
	
	/** Default setting for the number of pieces */
	public static final int OPTIMAL_NUM_PIECES = 10;
	/** Minimal number of pieces that the user can select */
	public static final int MIN_PIECES = 1;
	/** Maximal number of pieces that the user can select */
	public static final int MAX_PIECES = 100;
	
	/** Standard loader size used for loading bytes of data */
	public static final int STD_LOADER_SIZE = 4096;

}
