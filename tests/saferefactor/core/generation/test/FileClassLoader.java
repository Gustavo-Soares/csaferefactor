package saferefactor.core.generation.test;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class FileClassLoader extends URLClassLoader {

	public FileClassLoader(URL[] urls) {
		super(urls);

	}

	public void addClass(File f) throws MalformedURLException {

		URL jarfile = new URL("file://" + f.toString() + "/");

		addURL(jarfile);
	}

}
