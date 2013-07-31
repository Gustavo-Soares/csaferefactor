public class B extends A {

	// try to pushdown this method to C
	public void m() {
		super.k();
	}

	@Override
	public void k() {
		System.out.println(42);
	}

}
