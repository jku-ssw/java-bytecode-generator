//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

public class IncorrectResultsForMathSequence {
  public static void main(String[] var0) {
    float var1 = (float)Math.IEEEremainder(0.0D, 0.0D);
    var1 = Math.ulp(var1);
    var1 = Math.copySign(0.0F, var1);
    System.out.println(var1);
  }
}
