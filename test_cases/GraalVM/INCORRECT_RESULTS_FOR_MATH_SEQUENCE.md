# Incorrect results for `java.lang.Math` sequence

*Java version:*
```
java version "1.8.0_192"
Java(TM) SE Runtime Environment (build 1.8.0_192-b12)
Java HotSpot(TM) 64-Bit Server VM (build 25.192-b12-jvmci-0.53, mixed mode)
```

*Graal VM version:*
```
vm-1.0.0-rc11
```

This bug was found using the generated file *LotsOfMath1.class* and finally minimized to [IncorrectResultsForMathSequence.class](IncorrectResultsForMathSequence.class).
Once again it seems to stem from issues with `java.lang.Math` predictions / assumptions.

It seems to be related to a `Math.scalb` call as well as some default values for fields and the corresponding branch predictions.
The file may be reproduced using the *ASM* code generation in [IncorrectResultsForMathScalb.java](IncorrectResultsForMathScalb.java) since
decompiling the bytecode may yield source code that does not reproduce the bug.

*Fixed in [[GR-13332]](https://github.com/oracle/graal/commit/b73552c09a549445f981eaf1f83b51bb5552ada4)*
