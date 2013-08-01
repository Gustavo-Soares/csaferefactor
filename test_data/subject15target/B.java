public class B extends A {
	protected int x = 42;

	public int m() {
		return x;
	}

	public int test() {
		return new B().m();
	}
}