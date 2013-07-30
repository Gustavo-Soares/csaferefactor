package csaferefactor.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

import junit.framework.Assert;

import org.junit.Test;

import csaferefactor.util.OSPathHack;

/**
 * Tests for {@link OSPathHack}
 * 
 * @author Jeanderson Candido - <a href="mailto:jeandersonbc@gmail.com"
 *         target="new">jeandersonbc@gmail.com</a>
 * 
 */
public class PathHackTest {

	@Test
	public void testWindowsPath() throws FileNotFoundException {
		Scanner in = new Scanner(new BufferedReader(new FileReader(
				"./test_data/WinPathTransf.txt")));

		// Conversion to a friendly DOS 8.3 name
		String[] expectedResults = { "C:/Users/Neo/Documents/My~1/workspace/",
				"C:/Users/Morpheus/Documents/How~1/examples/",
				"C:/Users/Agent~1/References/Understanding~1/Chapter~1/",
				"C:/Users/Trinity/Programming/workspace/" };

		for (int i = 0; in.hasNext() && i < expectedResults.length; i++) {
			String path = in.nextLine();
			String result = OSPathHack.adapt(path);
			Assert.assertEquals(expectedResults[i], result);
		}
		in.close();
	}
}
