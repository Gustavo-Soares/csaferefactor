import static java.lang.String.*;

public class A {
  static String valueOf(int i) { 
    return "42"; 
  }
  public int test() {
    return Integer.parseInt(valueOf(23));
  }
}