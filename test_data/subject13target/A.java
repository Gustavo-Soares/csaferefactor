public class A {
	public int m(boolean b) {
		int x = 42;
		try {
			x = n(b, x);
		} catch (Exception e) {
			return x;
		}
		return x;
	}

	public int n(boolean b, int x) throws Exception {
		// from
		if (b) {
			x = 23;
			throw new Exception();
		}
		// to
		return x;
	}

	public int test() {
		return m(true);
	}
}