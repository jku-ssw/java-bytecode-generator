#Test cases

This folder gathers test files that produced bugs on a selection of bytecode
related tools. Both the original file and the corresponding minimal test case
are displayed as well as a human-readable representation of the Java bytecode.

At the moment, the following tools are under evaluation:

* *[GraalVM](https://graalvm.org)*
  
  Collects bugs that occurred when ran with a custom Graal wrapper (c.f. CompileTheWorld) that first interprets a generated class and then compares this (expected) result with
  those that are derived when running the (Graal-)compiled version as well as a compiled and optimized version.
