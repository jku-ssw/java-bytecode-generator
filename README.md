# Java-Bytecode-Generator

#Usage:
The JB-Generator can randomly create a new java program by running the class JBGenerator.java. 
see file [here](src/main/java/jb_generator/JBGenerator.java)
By running the program without options, default values for generation probabilities are used.

#Options:
##-h: 
Lists all options and how to use them
##-l: 
The maximum number of iterations for program-generation
##-f: 
The probability to generate fields
##-lv: 
The probability to generate local variables
##-ga: 
The probability for assigning values to fields
##-la: 
The probability for assigning values to variables
##-m:
The probability to generate methods
##-mc:
The probability to generate method-malls
##-ml:
The maximum number of iterations for method-generation
##-mp:
The maximum number of parameters a method can have
##-mo: 
The probability for overloading methods
##-p:
The probability to generate print-statements
##-jlm: 
The probability to call methods of java.lang.Math
##-cf:
the Probability to generate control flow statements
##-cl:
The maximum number of iterations for control-block generation
##-cd:
The maximum deepness, to which control-flow-statements can be nested
##-mli:
The maximum number of iterations for while-, doWhile- or for-loops
##-while:
The probability to generate while-loops
##-for:
The probability to generate for-loops
##-doWhile:
The probability to generate doWhile-loops
##-if:
The probability to generate an if-statement
##-ibf:
The maximum branching-factor for if-statements




