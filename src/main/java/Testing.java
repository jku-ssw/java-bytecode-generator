import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class Testing {

    public static void main(String[] args) throws NotFoundException {

        //MATH BORDER VALUES

        Math.addExact(Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2); //  Int.MIN_VALUE < sum < Int.MAX_VALUE    !!! => second bit of one of these two has to be 0
        Math.addExact(Long.MAX_VALUE / 2, Long.MAX_VALUE / 2); // sum < Long.MAX_VALUE   !!! !! => second bit of one of these two has to be 0

        Math.decrementExact(Integer.MIN_VALUE + 1); // > Integer.MIN_VALUE
        Math.decrementExact(Long.MIN_VALUE + 1); // > Long.MIN_VALUE

        Math.incrementExact(Integer.MAX_VALUE - 1); // < Integer.MAX_VALUE

//        Math.multiplyExact(Integer.MAX_VALUE, Integer.MAX_VALUE);   !!!

//        Math.multiplyExact(Long.MAX_VALUE, Long.MAX_VALUE);     !!!

        Math.negateExact(Integer.MIN_VALUE + 1); // < Integer.MIN_VALUE
        Math.negateExact(Long.MIN_VALUE + 1); // < Integer.MIN_VALUE

        Math.subtractExact(Integer.MIN_VALUE / 2, Integer.MIN_VALUE / 2);   //!!!!!  same as with add
        Math.subtractExact(Long.MIN_VALUE / 2, Long.MIN_VALUE / 2);   //!!!!!

        Math.toIntExact(2147483647L); // <= Integer.MAX_VALUE

        CtClass mathClazz = ClassPool.getDefault().get("java.lang.Math");
        CtMethod[] methods = mathClazz.getDeclaredMethods();
        for (CtMethod m : methods) System.out.println(m.getLongName());


        //4x sache mit second bit
        //

        //System.out.println(Math.addExact(1073741823, 1073741823));

        //System.out.println( 1<<30);
        System.out.println(Integer.MAX_VALUE & 1073741824);
        System.out.println((1 << 30) == Integer.MAX_VALUE);

        System.out.println((1073741824) & 1073741824);

        System.out.println(Long.MAX_VALUE & (1 << 62));

        System.out.println((-800481230 & 1 << 30) == 0);
        System.out.println((-1784877658 & 1 << 30) == 0);

//        if (((-800481230 & 1 << 30) == 0) || ((-1784877658 & 1 << 30) == 0)) {
//           System.out.println(Math.addExact(-800481230L, -1784877658L));
//        }
//        System.out.println(Integer.MIN_VALUE);

//        int b = -1979675448;
//        System.out.println(b > 0 && (b & 1073741824) == 0 || (b & 1073741824) == 0);
//        if (true && (b < 0 && b > 0 || b > 0 && b < 0 || b > 0 && (b & 1073741824) == 0 || (b & 1073741824) == 0)) {
//            Math.addExact(b, b);
//        }
//
//        if (4531613991445078650L < 0L && 6612443817826994177L > 0L || 4531613991445078650L > 0L && 6612443817826994177L < 0L ||
//                4531613991445078650L > 0L && (0L == 0L || 4611686018427387904L == 0L)) {
//            Math.addExact(4531613991445078650L, 6612443817826994177L);
//        }
//
//        if ((1354486592 < 0 && 820388835 > 0) || (1354486592 > 0 && 820388835 < 0) ||
//                (1354486592 > 0 &&
//                (((1354486592 & 1L << 30) == 0) || ((820388835 & 1L << 30) == 0))) || (1354486592 < 0 &&
//                (((1354486592 & 1L << 30) != 0) || ((820388835 & 1L << 30) != 0)))) {
//            Math.addExact(1354486592, 820388835);
//        }

        System.out.println(Integer.MAX_VALUE + Integer.MAX_VALUE);
       // 1892581158
        System.out.println(-1657399502 & (1073741824 + 1));
       // Math.addExact(-1657399502, -1657399502);


        Math.multiplyExact(-936792317, 101413799);
        Math.multiplyExact(2092751282, 272419494);

       // Math.multiplyHigh(-3177196481250713142L, -4330236363057485497L);

    }
}


