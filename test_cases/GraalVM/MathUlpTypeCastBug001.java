public class MathUlpTypeCastBug001 {
    byte ee;

    public static void main(String[] args) {
        MathUlpTypeCastBug001 o = new MathUlpTypeCastBug001();

        /* this evaluates to false when interpreted, but becomes true
        * when compiled with Graal */
        System.out.println((short) (Math.ulp(Math.log10(0.0D))) == (char) (~o.ee));
    }
}
