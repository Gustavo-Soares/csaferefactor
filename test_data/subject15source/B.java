public class B extends A {
	protected int x = 42;

	public int test() {
		return new B().m();
	}
}