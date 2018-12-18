#Incorrect results for `Math.scalb`

*Java version:*
```
java version "1.8.0_192"
Java(TM) SE Runtime Environment (build 1.8.0_192-b12)
Java HotSpot(TM) 64-Bit Server VM (build 25.192-b12-jvmci-0.53, mixed mode)
```

This bug was found using the generated file [LotsOfMath87.class](LotsOfMath87.class) and finally minimized to [IncorrectResultsForMathScalb.class](IncorrectResultsForMathScalb.class).
It seems to be related to a `Math.scalb` call as well as some default values for fields and the corresponding branch predictions.
The file may be reproduced using the *ASM* code generation in [IncorrectResultsForMathScalb.java](IncorrectResultsForMathScalb.java) since
decompiling the bytecode may yield source code that does not reproduce the bug.
