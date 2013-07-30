package csaferefactor.util;

/**
 * Hacks a given path for Java
 * 
 * @author Jeanderson Candido - <a href="mailto:jeandersonbc@gmail.com"
 *         target="new">jeandersonbc@gmail.com</a>
 * 
 */
public class OSPathHack {

	// by the moment, this shouldn't be instantiated
	private OSPathHack() {
	}

	/**
	 * Adapts the given path to a friendly OS path.
	 * 
	 * @param path
	 *            A string representing a path
	 * @return The adapted path
	 */
	public static String adapt(String path) {
		// nothing to be done if it doesn't have blank space
		if (!path.contains(" "))
			return path;
		String operatingSystemID = System.getProperty("os.name");
		if (operatingSystemID.toLowerCase().contains("windows")) {
			return getWindowsFriendlyPath(path);
		}
		return getUnixFriendlyPath(path);
	}

	private static String getUnixFriendlyPath(String path) {
		// TODO
		return path;
	}

	/**
	 * Converts a path to a DOS 8.3 notation (using tilde notation).
	 * <p>
	 * Example:
	 * <ol>
	 * <li><code>C:\Users\Neo\Programming stuff\Matrix</code></li>
	 * <li><code>C:\Users\Neo\Programming~1\Matrix</code></li>
	 * </ol>
	 * </p>
	 * 
	 * @param path
	 *            The String to be converted
	 * @return A friendly DOS 8.3 converted path
	 */
	private static String getWindowsFriendlyPath(String path) {
		/*
		 * FIXME Code will crash in the following case:
		 * 
		 * (Path 1) "C:\Program Other files\The wrong
		 * path\", (Path 2) "C:\Program Files\The right path\"
		 * 
		 * The right conversion would be "C:\Program~2\The~1\" since "Program~1"
		 * refers to Path 1.
		 */
		return path.replaceAll("\\s[\\w\\s]*/", "~1/");
	}

}
