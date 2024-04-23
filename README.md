This project is a compiler for a programming language called KnightCode.The capabilities of KnightCode are as follows:

1) Support for the data types INTEGER and STRING
2) Supports comments
3) Performs basic operations (addition, subtraction, multiplication, and division)
4) Reads input from users
5) Supports IF-THEN-ELSE and WHILE loops (conditionally)
6) Prints information to the command line

We utilize the ANTLR: antlr-4.13.1-complete.jar and ASM Bytecode Library: asm-9.6.jar

With a grammar file called KnightCode.g4 ANTLR generates the parser, lexer, and BaseVisitor.

To run, build the grammar first in the command line with:
ant build-grammar
ant compile-grammar
ant compile

To begin using the compiler for a KnightCode program you run the kcc.java file in command line. To do so, input 2 arguments: the name of the file you wish to compile (argument 1) and the name/location you wish the output file to be in (argument 2). An example will be shown below:
java compiler/kcc tests/program3.kc output/program3
This compiles program3 and outputs it as program3 in the output directory.
To run the outputted class file that was made use:
java output/(PROGRAM_NAME)
In our case from the example:
java output/program3
