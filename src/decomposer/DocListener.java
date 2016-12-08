package decomposer;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * A functional interface used for listening for document events. This includes
 * the remove update, insert update and a more general changed update. All
 * updates fall back to the implementation of the changed update.
 *
 * @author Mario Bobic
 */
@FunctionalInterface
public interface DocListener extends DocumentListener {

	/**
	 * Invokes the {@code changedUpdate} method.
	 */
	default void removeUpdate(DocumentEvent e) { changedUpdate(e); }
	/**
	 * Invokes the {@code changedUpdate} method.
	 */
	default void insertUpdate(DocumentEvent e) { changedUpdate(e); }

	@Override
	void changedUpdate(DocumentEvent e);
}
