public class A {
	public int m(boolean b) {
		int x = 42;
		try {
			// from
			if (b) {
				x = 23;
				throw new Exception();
			}
			// to
		} catch (Exception e) {
			return x;
		}
		return x;
	}

	public int test() {
		return m(true);
	}
}