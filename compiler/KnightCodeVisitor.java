 /**
* Visitor Code which is used over the BaseVisitor code. methods can be added whenever needed to implement new rules for compiling the grammar.
* @author Nicholas Newsom
* @version 1.0
* Assignment 5
* CS322 - Compiler Construction
* Spring 2024
**/
package compiler;

import lexparse.*;
import org.objectweb.asm.*;
import compiler.Utilities.*;
import java.util.*;

public class KnightCodeVisitor extends KnightCodeBaseVisitor<Object>{

    private ClassWriter cw;  //ClassWriter
	private MethodVisitor methVisitor; //global visitor
	private String programName; //output file name
    private Map<String, Variable> symbolTable; //map that stores variable name with corresponding variable object with some attributes
    private int memoryPointer;//where the compiler is being be pointed and shows where we're compiling
    /**
     * Constructor for KnightCodeVisitor
     * @param fileName the name of file being used
     */
    public KnightCodeVisitor(String fileName)
    {
        this.programName = fileName;    
    }
    /*
     * Method to print our symbol table
     */
    public void printSymbolTable()
    {
        System.out.println("SymbolTable: ");
        for (Map.Entry<String, Variable> entry : symbolTable.entrySet())
        {
            System.out.println("Key: " + entry.getKey() + " Value: " + entry.getValue().toString());
        }
    }
    /**
     * Method sets up the ClassWriter and starts the constructor for our compiler
     * @param name the name of program to create
     */
    public void beginClass(String name)
    {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC,programName, null, "java/lang/Object",null);
        {
			MethodVisitor mv=cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD, 0); //load the first local variable: this
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V",false);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitMaxs(1,1);
			mv.visitEnd();
		}
    }
    /**
     * ends main method and writes ClassWriter data to the output destination; with the name given
     */
    public void endClass()
    {
            methVisitor.visitInsn(Opcodes.RETURN);
            methVisitor.visitMaxs(0, 0);
            methVisitor.visitEnd();
    
            cw.visitEnd();    
            byte[] b = cw.toByteArray();
            Utilities.writeFile(b,this.fileName+".class");
    
            System.out.println("\nCompiled. Class File Generated");
    }
    @Override
    /**
     * * Calls beginClass, creating ClassWriter and constructor
     * @param ctx context of the file
     */
    public Object visitFile(KnightCodeParser.FileContext ctx)
    {
        beginClass(programName);
        return super.visitFile(ctx);
    }
    @Override
    /**
     * visits Declare for Variables, a symbol table hashmap will be made, stack memory pointer set to zero. compiler parses through tree with this
     */
    public Object visitDeclare(KnightCodeParser.DeclareContext ctx)
    {
        symbolTable = new HashMap<>();
        memoryPointer = 0;
        return super.visitDeclare(ctx);
    }
    @Override
    /**
     * * Once variables visited, name and type of each Variable will be used to instantiate a new Variable object in the symbol table
     * @param ctx context of the Variable
     * @return
     */
    public Object visitVariable(KnightCodeParser.VariableContext ctx)
    {
        System.out.println("Visiting: Variable");
        String datatype = ctx.vartype().getText();
        //if declared type != either STRING or INTEGER: print error, exit
        if (!type.equals("STRING") && !type.equals("INTEGER"))
        {
            System.err.println("Compilation ERROR: entered datatype not supported, string or integer is needed");
            System.exit(1);
        }
        // Creates the variable and adds it to symbol table
        String name = ctx.identifier().getText();
        Variable v = new Variable(name, datatype, memoryPointer++);
        symbolTable.put(name, v);

        printSymbolTable();
        return super.visitVariable(ctx);
    }
    @Override
    /**
     * Method that visits main and initializes the main method
     * @param ctx context of main body
     */
    public Object visitMain(KnightCodeParser.BodyContext ctx)
    {
        // Start MethodVisitor for main method of program
        methVisitor=cw.visitMethod(Opcodes.ACC_PUBLIC+Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        methVisitor.visitCode();
        return super.visitBody(ctx);
    }
    /**
     * checks if a string is a number or identifier within the symbol table and loads its stored value from the symbol table
     * @param str string with ID or value to be loaded
     */
    public void loadInteger(String string)
    {
        int memoryLocation;
        //If string is a key within symbol table
        if (symbolTable.containsKey(string))
        {
            Variable var = symbolTable.get(string);
            memoryLocation = var.getLocation();
            methVisitor.visitVarInsn(Opcodes.ILOAD, memoryLocation);
        }
        //If number, parse
        else 
        {
            methVisitor.visitLdcInsn(Integer.parseInt(string));
        }
    }
    @Override
    /**
     * when Setvar is visited: will define a previously declared variable
     * @param ctx context of the Setvar
     */
    public Object visitSetvar(KnightCodeParser.SetvarContext ctx)
    {
        //Name of variable to be created
        String varName = ctx.ID().getText(); 

        //Creates object for the variable
        Variable var = symbolTable.get(varName);
        
        // If the variable was not previously declared
        if (var == null)
        {
            System.err.println("ERROR: " + varName + "Variable does not exist");
            System.exit(1);
        }
        //Evaluates expressions before storing created variable
        else if(ctx.expr() != null)
        {
            evalExpr(ctx.expr());

            //Defines if it's an INTEGER
            if (var.getType().equals("INTEGER"))
            {
                System.out.println("Storing the Var " + varName);
                methVisitor.visitVarInsn(Opcodes.ISTORE, var.getLocation());
            }
            /*else if (var.getType().equals("STRING") && ctx.STRING() != null)
            {
                String string = removeParentheses(ctx.STRING().getText());
                methVisitor.visitLdcInsn(string);
                methVisitor.visitVarInsn(Opcodes.ASTORE, var.getLocation());
            } */
        }
        //Defines if it's a STRING
        else if (var.getType().equals("STRING") && ctx.STRING() != null)
        {
            String string = removeParentheses(ctx.STRING().getText());
            methVisitor.visitLdcInsn(string);
            methVisitor.visitVarInsn(Opcodes.ASTORE, var.getLocation());
        } 
        System.out.println("Variable Set.");
        printSymbolTable();
        return super.visitSetvar(ctx);
    }
    @Override
    /**
     *reads input from user and stores input in variable whose identifier follows the read call
     */
    public Object visitRead(KnightCodeParser.ReadContext ctx)
    {
        System.out.println("Entering Read");
        
        //Initializes the variable that will store the value inputted by the user so it can used later by the compiler 
        Variable var = symbolTable.get(ctx.ID().getText());
        int scanLocation = memoryPointer++;

        // Initializes the Scanner object
        methVisitor.visitTypeInsn(Opcodes.NEW, "java/util/Scanner"); // Creates Scanner and pushes it to the stack
        methVisitor.visitInsn(Opcodes.DUP); // Duplicates the Scanner reference for initialization and storage
        methVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
        methVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V", false); // Initializes Scanner
        methVisitor.visitVarInsn(Opcodes.ASTORE, scanLocation); // Stores Scanner

        //if variable is an INTEGER, reads input integer
        if (var.getType().equals("INTEGER"))
        {
            mainVisitor.visitVarInsn(Opcodes.ALOAD, scanLocation);
            mainVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "nextInt", "()I", false); // Scan.nextLong()
            mainVisitor.visitVarInsn(Opcodes.ISTORE, var.getLocation()); // Stores int value in a variable
        }
        //if variable is a STRING, reads input string
        else if (var.getType().equals("STRING"))
        {
            mainVisitor.visitVarInsn(Opcodes.ALOAD, scanLocation); // Loads scanner
            mainVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "nextLine", "()Ljava/lang/String;", false); // Scan.nextLine()
            mainVisitor.visitVarInsn(Opcodes.ASTORE, var.getLocation()); // Stores String value in a variable
        }
        return super.visitRead(ctx);
    }  
    /**
     * evaluates an expression depending on context type 
     * recurses until arrival at terminal, so it can be loaded and operations can be performed
     * @param ctx context of the expr 
     */
    public void evalulateExpr(KnightCodeParser.ExprContext ctx)
    {
        // If expr is a number, compiler reads and parses the text as an int and loads it into constant pool
        if (ctx instanceof KnightCodeParser.NumberContext)
        {
            int value = Integer.parseInt(ctx.getText());
            methVisitor.visitLdcInsn(value);
        }
        //Addition of 2 INTEGERS
        else if (ctx instanceof KnightCodeParser.AdditionContext)
        {
            for(KnightCodeParser.ExprContext expr : ((KnightCodeParser.AdditionContext)ctx).expr())
            {
                evaluateExpr(expr);
            }
            System.out.println("Adding");
            methVisitor.visitInsn(Opcodes.IADD);
        }
         //Subtraction of 2 INTEGERS
         else if (ctx instanceof KnightCodeParser.SubtractionContext)
        {
             for(KnightCodeParser.ExprContext expr : ((KnightCodeParser.SubtractionContext)ctx).expr())
            {
                evaluateExpr(expr);
            }
            System.out.println("Subtracting");
            methVisitor.visitInsn(Opcodes.ISUB);
        }
        //Multiplication of 2 INTEGERS
        else if (ctx instanceof KnightCodeParser.MultiplicationContext)
        {
            for(KnightCodeParser.ExprContext expr : ((KnightCodeParser.MultiplicationContext)ctx).expr())
            {
                evaluateExpr(expr);
            }
            System.out.println("Multiplying");
            methVisitor.visitInsn(Opcodes.IMUL);    
        }
        //Division of 2 INTEGERS
        else if (ctx instanceof KnightCodeParser.DivisionContext)
        {
            for(KnightCodeParser.ExprContext expr : ((KnightCodeParser.DivisionContext)ctx).expr())
            {
                evaluateExpr(expr);
            }
            System.out.println("Dividing");
            methVisitor.visitInsn(Opcodes.IDIV);
        }
        // If expr is a supported identifier 
        else if (ctx instanceof KnightCodeParser.IdContext)
        {
            String id = ctx.getText();
            Variable var = symbolTable.get(id);
            System.out.println("expr id " + id + "\nvar: " + var.toString());
            /*switch (id) {
                case "INTEGER":
                    mainVisitor.visitVarInsn(Opcodes.ILOAD, var.getLocation());
                    break;
                case "STRING":
                    mainVisitor.visitVarInsn(Opcodes.ALOAD, var.getLocation());
                    break;
            }*/

              // If datatype = INTEGER
            if (var.getType().equals("INTEGER"))
            {
                methVisitor.visitVarInsn(Opcodes.ILOAD, var.getLocation());
            }
            // If datatype = STRING
            else if (var.getType().equals("STRING"))
            {
                methVisitor.visitVarInsn(Opcodes.ALOAD, var.getLocation());
            } 
        }   
    }
    @Override
    /**
     * visits comparison and performs the comparison operation. if comparison is true: load one. if comparison is false: load 0
     * @param ctx context of the comparison
     */
    public Object visitComparison(KnightCodeParser.ComparisonContext ctx)
    {
        Label trueLabel = new Label();
        Label falseLabel = new Label();
        String operand = ctx.comp().getText();

        evalExpr(ctx.expr(0));
        evalExpr(ctx.expr(1));

        switch (operand) 
        {
            case ">":
                methVisitor.visitJumpInsn(Opcodes.IF_ICMPGT, trueLabel);
                break;
            case "<":
                methVisitor.visitJumpInsn(Opcodes.IF_ICMPLT, trueLabel);
                break;
            case "<>":
                methVisitor.visitJumpInsn(Opcodes.IF_ICMPNE, trueLabel);
                break;
            case "=":
                methVisitor.visitJumpInsn(Opcodes.IF_ICMPEQ, trueLabel);
                break;    
        }

        //If false, load 0, jump to end
        methVisitor.visitLdcInsn(0);
        methVisitor.visitJumpInsn(Opcodes.GOTO, falseLabel);
        //If true load 1 to stack
        methVisitor.visitLabel(trueLabel);
        methVisitor.visitLdcInsn(1);

        mainVisitor.visitLabel(falseLabel);

        return super.visitComparison(ctx);
    }
    @Override
    /**
     * handles logic for a IF-ELSE loop based on comparison using jumps
     */
    public Object visitDecision(KnightCodeParser.DecisionContext ctx)
    {    
        //labels for jumping
        Label trueLabel = new Label();
        Label falseLabel = new Label();
        //load the children to be compared, from left to right
        String num1 = ctx.getChild(1).getText();
        String num2 = ctx.getChild(3).getText();

        loadInteger(num1);
        loadInteger(num2);

        String operand = ctx.comp().getText();
        //based on the conditions, decides if it will jump to IF-THEN block
        switch (operand) 
        {
            case ">":
                System.out.println("GT");
                mainVisitor.visitJumpInsn(Opcodes.IF_ICMPGT, trueLabel);
                break;
            case "<":
                System.out.println("LT");
                mainVisitor.visitJumpInsn(Opcodes.IF_ICMPLT, trueLabel);
                break;
            case "<>":
                System.out.println("NE");
                mainVisitor.visitJumpInsn(Opcodes.IF_ICMPNE, trueLabel);
                break;
             case "=":
                System.out.println("EQ");  
                mainVisitor.visitJumpInsn(Opcodes.IF_ICMPEQ, trueLabel);
                break;
            default:
                System.err.println("ERROR: Unkown Comparison.");
                System.exit(1);
        }
        boolean hasElse = false; 
        int endLocation = 6; //used to find ENDIF
         //Loop to find ENDIF statement
         while (!ctx.getChild(endLocation).getText().equals("ENDIF"))
        {
            endLocation++;
        }  
        //Checks for ELSE
        for(int i = 0; i<ctx.children.size(); i++)
        {
            if(ctx.getChild(i).getText().equals("ELSE"))
            {
                hasElse = true;
                break;
            }
        }
        int elseLocation = 6; // least possible child index for ELSE
        //Handles ELSE block if needed
        if(hasElse)
        {
            //finds how many STAT are in the IF block
            while (!ctx.getChild(elseLocation).getText().equals("ELSE"))
            {
                elseLocation++;
            }  
            //visits all of the children within the ELSE block
            for(int i = elseLocation+1; i<ctx.getChildCount(); i++)
            {
                visit(ctx.getChild(i));
            }
        }
        //Jumps to end once loop executes (or hasElse = false) and comparison is false
        methVisitor.visitJumpInsn(Opcodes.GOTO, falseLabel);
        //when comparison is true
        methVisitor.visitLabel(trueLabel);

        //handles IF when there is an else
        if(hasElse)
        {
            //Starts at the first STAT of the THEN block, and visits the children until the location of the else is found
            for (int i = 5; i< elseLocation;i++)
            {
                visit(ctx.getChild(i));
            }
        }
        //Handles IF loop when there is no else
        else
        {
            //Starting at the first branch in the IF block, visits children until ENDIF TERMINAL is reached
            for (int i = 5; i< endLocation;i++)
            {
                visit(ctx.getChild(i));
            }
        }
        methVisitor.visitLabel(falseLabel);
        return null;
    }
    @Override
    /**
     * handles logic to perform while loops
     * @param ctx context of the loop
     */
    public Object visitLoop(KnightCodeParser.LoopContext ctx)
    {
        //labels used for jumping
        Label beginLoop = new Label();
        Label leaveLoop = new Label(); 
        
        //Begin loop
        methVisitor.visitLabel(beginLoop);
        //Load the children to be compared, from left to right
        String num1 = ctx.getChild(1).getText();
        String num2 = ctx.getChild(3).getText();
        loadInteger(num1);
        loadInteger(num2);

        //decides comparison
        String operand = ctx.comp().getText();
        //Handles whether or not it will jump to endLoop (when comp becomes false)
        switch (operand) 
        {
            case ">":
                System.out.println("LE");
                mainVisitor.visitJumpInsn(Opcodes.IF_ICMPLE, leaveLoop);
                break;
            case "<":
                System.out.println("GE");
                mainVisitor.visitJumpInsn(Opcodes.IF_ICMPGE, leaveLoop);
                break;
            case "<>":
                System.out.println("EQ");
                mainVisitor.visitJumpInsn(Opcodes.IF_ICMPEQ, leaveLoop);
                break;
            case "=":
                System.out.println("NEQ");  
                mainVisitor.visitJumpInsn(Opcodes.IF_ICMPNE, leaveLoop);
                break;
            default:
                System.err.println("ERROR: Unknown Comparison.");
                System.exit(1);
        }
        //Loop that runs STATs contained within the WHILE
        for(int i = 5; i<ctx.getChildCount(); i++)
        {
            visit(ctx.getChild(i));
        }
        //Jumps back to top if loop needs to execute
        methVisitor.visitJumpInsn(Opcodes.GOTO, beginLoop);
        
        methVisitor.visitLabel(leaveLoop);

        return null;
    }
    @Override
    /**
     * when print is visited: will print integer or a string
     * @param ctx context of the print
     */
    public Object visitPrint(KnightCodeParser.PrintContext ctx)
    {      
        mainVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        // If an ID is being printed: searches the stack and finds its location so it can be loaded and printed
        if(ctx.ID() != null)
        {   
            String varID = ctx.ID().getText();
            Variable var = symbolTable.get(varID);
            int varLocation = var.getLocation();
            if (var.getType().equals("INTEGER"))
            {
                methVisitor.visitVarInsn(Opcodes.ILOAD, varLocation);
                methVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
            }
            else
            {
                methVisitor.visitVarInsn(Opcodes.ALOAD, varLocation);
                methVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            }
        }

        //If a STRING is being printed:loads the string to the constant pool to be printed 
        else if(ctx.STRING()!=null)
        {
            String string = removeParentheses(ctx.STRING().getText());
            methVisitor.visitLdcInsn(string);
            methVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        }
        return super.visitPrint(ctx);
    }
}
 
