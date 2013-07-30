

public class C extends B {
	public static void main(String[] args) {
		new C().m();
	}

	public int  m() { return super.k(); }
	
	public int test() {
		return m();
	}
}
