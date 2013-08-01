public class A {
	public int k() {
		final int i = 23;
		return new Object() {
			int j = 42;

			public int k() {
				return i;
			}
		}.k();
	}

	public int test() {
		return new A().k();
	}
}
