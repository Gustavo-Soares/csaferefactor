public class D {

	static class C extends A {

		static int x = 23;

		public int m() {
			return C.x;
		}
	}

	public int test() {
		return new C().m();
	}

}
