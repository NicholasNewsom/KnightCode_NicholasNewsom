/**
* Runs compiler with 2 command arguments: the name of the input .kcc file that you want to run, the name of the file that you want ouput, and the path to files must be included for both input and output.
* @author Nicholas Newsom
* @version 1.0
* Assignment 5
* CS322 - Compiler Construction
* Spring 2024
**/
package compiler;

import lexparse.*;
import java.io.IOException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.tree.*;
import org.antlr.v4.gui.Trees;

public class kcc 
{
     public static void main(String[] args) throws IOException
    {
        //if args are input incorrectly
        if (args.length < 2) 
        {
            System.err.println("Invalid args. Run: java compiler/kcc <pathToIp/input.kcc> <pathToOp/output>\nReplace pathToIp with input file directory and pathToOp with preferred output location");
            return;
        }    
        /*CommonTokenStream tokens; //input file tokens
        CharStream input; //input .kcc file
        KnightCodeParser parser; //input file parser
        KnightCodeLexer lexer; //input file lexer
        String output; //name for the output
        */
        try
        {
            CharStream input = CharStreams.fromFileName(args[0]);  //obain input
            KnightCodeLexer lexer = new KnightCodeLexer(input); //creates input file lexer
            CommonTokenStream tokens = new CommonTokenStream(lexer); //create input file token stream
            KnightCodeParser parser = new KnightCodeParser(tokens); //create input file parser
            String output = args[1]; //get output

            ParseTree tree = parser.file();  //set start location of parser
            Trees.inspect(tree, parser);  //displays parse tree
            
            KnightCodeVisitor visitor = new KnightCodeVisitor(output);
            
            visitor.visit(tree);
            visitor.endClass();

        }
        finally{}
        System.out.println("File Created");
    }
}

