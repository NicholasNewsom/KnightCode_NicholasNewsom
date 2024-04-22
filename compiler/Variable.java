* The class defines the Variable constructor, and has methods to obtain its attributes.
* such as Variable type, name, location, etc.
* @author Nicholas Newsom
* @version 1.0
* Assignment 5
* CS322 - Compiler Construction
* Spring 2024
**/
package compiler;

public class Variable 
{    
    public String varDataType = ""; // the datatype the var can hold
    public String name = ""; //the var name
    public int memoryLocation;//Location of variable on the stack
    /**
     * Variable constructor
     * @param varDatatype the datatype the var can hold
     * @param name the var name that will be stored
     * @param memoryLocation memory location on the stack the var is stored to
     */
    public Variable(String name, String varDataType, int memoryLocation)
    {
        this.variableType = varDataType;
        this.name = name;
        this.memoryLocation = memoryLocation;
    }
    /**
     * Returns the name of the var
     * @return name of variable
     */
    public String getName()
    {
        return name;
    }
    /**
     * Returns datatype of the var
     * @return var data type
     */
    public String getType()
    {
        return varDataType;
    }
    /**
     * Returns memory location on the stack of the var
     * @return memory location of var on the stack
     */
    public int getLocation()
    {
        return memoryLocation;
    }
    /**
     * prints name, type and location of the var
     * @return String containing var attributes
     */
    public String toString()
    {
        return "Variable Name: " + name + " Variable DataType: " + variableType + " Variable's Memory Location: " + memoryLocation;
    }
}
