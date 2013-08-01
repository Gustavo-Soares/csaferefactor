public class A {
	public int k() {
		final int j = 23;
		return new Object() {
			int j = 42;

			public int k() {
				return j;
			}
		}.k();
	}

	public int test() {
		return new A().k();
	}
}