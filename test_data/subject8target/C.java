public class C extends B {

	public void test() {
		new C().m();
	}

	public int foo() {
		return 10;
	}

	public void m() {
		super.k();
	}
}