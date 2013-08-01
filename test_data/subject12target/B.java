public class B extends A {
	public int getI() {
		return 42;
	}

	public int test() {
		A a = new B();
		a.setI(23);
		return a.getI();
	}
}