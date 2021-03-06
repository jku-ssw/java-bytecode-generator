# Java-Bytecode-Generator

[![Build Status](https://travis-ci.org/jku-ssw/java-bytecode-generator.svg?branch=master)](https://travis-ci.org/jku-ssw/java-bytecode-generator)

The *Java Bytecode Generator* can create random Java class files refined with different 
options in order to test compilers or other tools that work with Java bytecode.

## Usage
The generator can be executed by running the main method in [JBGenerator](src/main/java/at/jku/ssw/java/bytecode/generator/JBGenerator.java)
or by using the provided Gradle wrapper:
```
./gradlew run
```

## Options
If executed from the command line, the following options are available:

| Argument  | Description                                                                             |
|-----------|-----------------------------------------------------------------------------------------|
| -h        | Lists all options and how to use them                                                   |
| -l        | The maximum number of iterations for program-generation                                 |
| -f        | The probability to generate fields                                                      |
| -lv       | The probability to generate local variables                                             |
| -ga       | The probability for assigning values to fields                                          |
| -la       | The probability for assigning values to variables                                       |
| -m        | The probability to generate methods                                                     |
| -mc       | The probability to generate method-calls                                                |
| -ml       | The maximum number of iterations for method-generation                                  |
| -mp       | The maximum number of parameters a method can have                                      |
| -mo       | The probability for overloading methods                                                 |
| -p        | The probability to generate print-statements                                            |
| -jlm      | The probability to call methods of java.lang.Math                                       |
| -cf       | the Probability to generate control flow statements                                     |
| -cl       | The maximum number of iterations for control-block generation                           |
| -cd       | The maximum depth, to which control-flow-statements can be nested                    |
| -mli      | The maximum number of iterations for while-, doWhile- or for-loops                      |
| -while    | The probability to generate while-loops                                                 |
| -for      | The probability to generate for-loops                                                   |
| -doWhile  | The probability to generate doWhile-loops                                               |
| -if       | The probability to generate an if-statement                                             |
| -ibf      | The maximum branching-factor for if-statements                                          |
| -os       | The probability to generate statements using operators                                  |
| -as       | The probability to generate statements using arithmetic operators                       |
| -ls       | The probability to generate statements using logical operators                          |
| -bs       | The probability to generate statements using bitwise operators                          |
| -als      | The probability to generate statements using arithmetic and logical operators           |
| -abs      | The probability to generate statements using arithmetic and bitwise operators           |
| -lbs      | The probability to generate statements using logical and bitwise operators              |
| -albs     | The probability to generate statements using arithmetic, logical and bitwise operators  |
| -mops     | The maximum number of operators in a statement. Not exactly true for combined operators |
| -snippet  | The probability to insert predefined code snippets                                      |
