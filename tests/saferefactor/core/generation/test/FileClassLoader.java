package saferefactor.core.generation.test;
import java.io.File;
import java.io.FileFilter;
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

//	public void addJarFiles(String path) throws MalformedURLException {
//
//		File libDir = new File(path);
//
//		File[] jars = libDir.listFiles(new FileFilter() {
//			public boolean accept(File f) {
//				return f.getName().endsWith(".jar");
//			}
//		});
//
//		if (jars != null)
//			for (File jar : jars) {
//				URL jarfile = new URL("jar", "", "file:"
//						+ jar.getAbsolutePath() + "!/");
//				addURL(jarfile);
//			}
//
//	}
}
