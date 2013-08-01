/**
 * 
 */
package csaferefactor.exception;

import csaferefactor.runnable.SafeRefactorThread;

/**
 * In case that a {@link SafeRefactorThread} attempts to check behavioral
 * changes in a compilation unit that doesn't have a declared package
 * 
 * @author Jeanderson Candido - <a href="mailto:jeandersonbc@gmail.com"
 *         target="new">jeandersonbc@gmail.com</a>
 * 
 */
public class PackageNotFoundException extends Exception {

	private static final long serialVersionUID = 5155608689348398588L;

	/**
	 * @param message
	 *            The message to be displayed
	 */
	public PackageNotFoundException(String message) {
		super(message);
	}

	/**
	 * Default constructor
	 */
	public PackageNotFoundException() {
		super("Source code is not in a package");
	}
}
