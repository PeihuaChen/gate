package gate.creole.morph;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gate.creole.ResourceInstantiationException;

/**
 * <p>Title: Interpret.java </p>
 * <p>Description: This is the main class which which should be invoked
 * to load the rule file in the system and then to execute the program
 * to find the root word and the affix to it.</p>
 */
public class Interpret {

  /**
   * instance of the ReadFile class which reads the file and stores each line of
   * the given program in the arraylist which can be read using different
   * methods of the ReadFile class
   */
  private ReadFile file;

  /** Boolean variables to keep track on which section is being read */
  private boolean isDefineVarSession, isDefineRulesSession;

  /** Instance of Storage class, which is used store all the variables details*/
  private Storage variables;

  /** This variables keeps the record of available methods for the morphing */
  private Method[] methods;

  /** This varilable stores the compiles versions of rules */
  private CompiledRules rules;

  /** This variables holds the affix */
  private String affix;

  /**
   * Constructor
   */
  public Interpret() {

  }

  /**
   * It starts the actual program
   * @param ruleFileName
   */
  public void init(URL ruleFileURL) throws ResourceInstantiationException {
    variables = new Storage();
    prepareListOfMorphMethods();
    rules = new CompiledRules();
    file = new ReadFile(ruleFileURL);
    affix = null;
    isDefineRulesSession = false;
    isDefineVarSession = false;
    readProgram();
    interpretProgram();
  }

  /**
   * Once all the rules have been loaded in the system, now its time to start
   * the morpher, which will find out the base word rule
   * @param word input to the program
   * @return root word
   */
  public String runMorpher(String word, String category) {
    affix = null;
    rules.resetPointer();
    // do the pattern matching process with each and every available pattern
    // until the pattern is found which can accomodate the given word
    while (rules.hasNext()) {


      // load the pattern
      MyPattern myPatInst = rules.getNext();

      // first check if this pattern should be considered to match
      if(!myPatInst.isSameCategory(category)) {
        continue;
      }

      // proceed only iof the pattern is not null
      if (myPatInst != null) {

        // find the actual pattern
        Pattern pat = myPatInst.getPattern();
        Matcher m = pat.matcher(word);

        // check if this pattern can accomodate the given word
        if (m.matches()) {
          // yes it can, so find the name of the function which should be
          // called to find it's root word
          String function = myPatInst.getFunction();

          // call the appropriate function
          String methodName = getMethodName(function);
          String[] parameters = getParameterValues(function);

          // check all the available in built methods and run the
          // appropriate one
          for (int i = 0; i < methods.length; i++) {

            // preparing paramters for the comparision of two
            // methods
            int len = methodName.length();
            String currentMethod = methods[i].toString();
            int len1 = currentMethod.length();

            if (len < len1) {
              currentMethod = currentMethod.substring(len1 - len + 1, len1);
              if (currentMethod.trim().equals(methodName.trim())) {

                // yes two methods are equivalent
                // so call that method of MorphFunctions
                MorphFunctions morphInst = new MorphFunctions();

                // set the given word in that morph program
                morphInst.setInput(word);

                // and finally call the appropriate method to find the root
                // word and the affix
                if (methods[i].getName().equals("irreg_stem")) {
                  String answer = morphInst.irreg_stem(parameters[0],
                                                       parameters[1]);
                  this.affix = morphInst.getAffix();
                  return answer;
                }
                else if (methods[i].getName().equals("null_stem")) {
                  //return word;
                  String answer = morphInst.null_stem();
                  this.affix = morphInst.getAffix();
                  return answer;
                }
                else if (methods[i].getName().equals("semi_reg_stem")) {
                  String answer = morphInst.semi_reg_stem(
                                    Integer.parseInt(parameters[0]),
                                    parameters[1]);
                  this.affix = morphInst.getAffix();
                  return answer;
                }
                else if (methods[i].getName().equals("stem")) {
                  String answer = morphInst.stem(
                                    Integer.parseInt(parameters[0]),
                                    parameters[1],
                                    parameters[2]);
                  this.affix = morphInst.getAffix();
                  return answer;
                }
              }
            }
          }
        }
      }
    }
    // no rule matched so say no matching found
    affix = null;
    return word;

  }

  /**
   * This method is used to find the method definition
   * But it can recognize only String, boolean and int types
   * for Example: stem(2,"ed","d") ==>
   *              stem(int,java.lang.String,java.lang.String);
   * @param method
   * @return the definition of the method
   */
  private String getMethodName(String method) {
    // find the first index of '('
    int index = method.indexOf('(');
    String methodName = method.substring(0, index) + "(";

    // now get the parameter types
    String[] parameters =
        method.substring(index + 1, method.length() - 1).split(",");

    // find the approapriate type
    for (int i = 0; i < parameters.length; i++) {
      if (parameters[i].startsWith("\"") && parameters[i].endsWith("\"")) {
        methodName = methodName + "java.lang.String";
      }
      else if (ParsingFunctions.isBoolean(parameters[i])) {
        methodName = methodName + "boolean";
      }
      else if (ParsingFunctions.isInteger(parameters[i])) {
        methodName = methodName + "int";
      }
      if ( (i + 1) < parameters.length) {
        methodName = methodName + ",";
      }
    }
    methodName = methodName + ")";
    return methodName;
  }

  /**
   * This method finds the actual parameter values
   * @param method from which parameters are required to be found
   * @return parameter values
   */
  private String[] getParameterValues(String method) {
    // now first find the name of the method
    // their parameters and their types
    int index = method.indexOf("(");

    // now get the parameters
    String[] parameters =
        method.substring(index + 1, method.length() - 1).split(",");

    // process each parameter
    for (int i = 0; i < parameters.length; i++) {
      // we need to remove " from String
      if (parameters[i].startsWith("\"") && parameters[i].endsWith("\"")) {
        parameters[i] = parameters[i].substring(1, parameters[i].length() - 1);
        continue;
      }
    }
    return parameters;
  }

  /**
   * This method prepares the list of available methods in the MorphFunctions
   * class
   */
  private void prepareListOfMorphMethods()
      throws ResourceInstantiationException  {
    methods = MorphFunctions.class.getDeclaredMethods();
  }

  /**
   * read the program file
   */
  private void readProgram() throws ResourceInstantiationException {
    // read the program file
    boolean readStatus = file.read();

    // check if read was success
    if (!readStatus) {
      //not it wasn't so simply display the message and ask user to check it
      generateError("Some errors reading program file.. please check the" +
                         "program and try again");
    }
  }

  /**
   * This method reads each line of the program and interpret them
   */
  private void interpretProgram() throws ResourceInstantiationException  {
    // read each line and parse it
    while (file.hasNext()) {
      String currentLine = file.getNext();

      if (currentLine == null || currentLine.trim().length() == 0) {
        continue;
      }

      // remove all the leading spaces
      currentLine = currentLine.trim();

      /* if commandType is 0 ==> defineVars command
       * if commandType is 1 ==> defineRules command
       * if commandType is 2 ==> variable declaration
       * if commandType is 3 ==> rule declaration
       * otherwise // unknown generate error
       */
      int commandType = findCommandType(currentLine);
      switch (commandType) {
        case -1:
          //comment command
          continue;
        case 0:
          //defineVars command
          defineVarsCommand();
          break;
        case 1:
          //defineRules command
          defineRulesCommand();
          break;
        case 2:
          //variable declaration
          variableDeclarationCommand(currentLine);
          break;
        case 3:
          // rule declaration
          ruleDeclarationCommand(currentLine);
          break;
        default:
          generateError("Syntax Error at line " + file.getPointer()
                        + " : " + currentLine);
          break;
      }
    } // end while
  }

  /**
   * This method interprets the line and finds out the type of command and
   * returns the integer indicating the type of the command
   * @param line The program command to be interpreted
   * @return and <tt>int</tt> value
   */
  private int findCommandType(String line) {

    // check for the comment command
    if (line.substring(0, 2).equals("//") || line.charAt(0)=='#') {
      return -1;
    }
    else if (line.equals("defineVars")) {
      return 0;
    }
    else if (line.equals("defineRules")) {
      return 1;
    }
    else if (isDefineVarSession && line.split("==>").length == 2) {
      return 2;
    }
    else if (isDefineRulesSession &&
             /*(line.charAt(0) == '{' ||
              line.charAt(0) == '[' ||
              line.charAt(0) == '(' ||
              line.charAt(0) == '\"')*/ (line.charAt(0) == '<') &&
             line.split("==>").length == 2) {
      return 3;
    }
    else {
      return Codes.ERROR_CODE;
    }
  }

  /**
   * This method processes the command to define the variable section
   */
  private void defineVarsCommand()  throws ResourceInstantiationException {

    // variable section can only be defined once
    if (isDefineVarSession) {
      generateError("Variable Section already defined - " +
                    "see line " + file.getPointer());
    }
    else if (isDefineRulesSession) {
      generateError("Variable section must be declared before the Rule " +
                    "Section - see line " + file.getPointer());
    }
    else {
      isDefineVarSession = true;
    }
  }

  /**
   * This method processes the command to define the rule section
   */
  private void defineRulesCommand() throws ResourceInstantiationException  {
    if (isDefineRulesSession) {
      generateError("Rule Section already defined - see " +
                    "line " + file.getPointer());
    }
    else {
      isDefineVarSession = false;
      isDefineRulesSession = true;
    }
  }

  /**
   * This method processes the command to declare the variable
   * @param line
   */
  private void variableDeclarationCommand(String line)
      throws ResourceInstantiationException  {
    // ok so first find the variable name and the value for it
    String varName = (line.split("==>"))[0].trim();
    String varValue = (line.split("==>"))[1].trim();

    // find the type of variable it is
    int valueType = ParsingFunctions.findVariableType(varValue.trim());
    if (valueType == Codes.ERROR_CODE) {
      generateError(varName + " - Variable Syntax Error - see " +
                    "line" + file.getPointer() + " : " + line);
    }

    // based on the variable type create the instance
    Variable varInst = null;
    switch (valueType) {
      case Codes.CHARACTER_RANGE_CODE:
        varInst = new CharacterRange();
        break;
      case Codes.CHARACTER_SET_CODE:
        varInst = new CharacterSet();
        break;
      case Codes.STRING_SET_CODE:
        varInst = new StringSet();
        break;
    }

    // set the values in the variable
    if (!varInst.set(varName, varValue)) {
      generateError(varName + " - Syntax Error while assigning value to the " +
                    "variable - see line" + file.getPointer() + " : " + line);
    }

    // and finally add the variable in
    if (!variables.add(varName, varInst.getPattern())) {
      generateError(varName.trim() + " - Variable already defined - see " +
                    "line " + file.getPointer() + " : " + line);
    }

    varInst.resetPointer();
  }

  /**
   * This method processes the command to declare the rule
   * @param line
   */
  private void ruleDeclarationCommand(String line)
      throws ResourceInstantiationException  {
    // lets divide the rule into two parts
    // LHS and RHS.
    // LHS is a part which requires to be parsed and
    // RHS should be checked for the legal function name and valid arguments
    // we process RHS first and then the LHS
    int LHS = 0;
    int RHS = 1;
    String[] ruleParts = line.split("==>");
    if (ruleParts.length != 2) {
      generateError("Error in declaring rule at line : " +
                    file.getPointer() + " : " + line);
    }

    // now check if the method which has been called in this rule actually
    // available in the MorphFunction Class
    String methodCalled = ruleParts[1].trim();
    if (!isMethodAvailable(methodCalled)) {

      // no method is not available so print the syntax error
      generateError("Syntax error - method does not exists - see " +
                    "line " + file.getPointer() + " : " + line);
    }

    // so RHS part is Ok
    // now we need to check if LHS is written properly
    // and convert it to the pattern that is recognized by the java
    String category = "";
    // we need to find out the category
    int i = 1;
    for(;i<ruleParts[0].length();i++) {
      if(ruleParts[0].charAt(i) == '>')
        break;
      category = category + ruleParts[0].charAt(i);
    }
    if(i >= ruleParts[0].length()) {
      generateError("Syntax error - pattern not written properly - see " +
                    "line " + file.getPointer() + " : " + line);
    }

    ruleParts[0] = ruleParts[0].substring(i+1, ruleParts[0].length());
    String newPattern = ParsingFunctions.convertToRegExp(ruleParts[0],
                                                         variables);
    if (newPattern == null) {
      generateError("Syntax error - pattern not written properly - see " +
                    "line " + file.getPointer() + " : " + line);
    }

    // we need to compile this pattern and finally add into the compiledRules
    boolean result = rules.add(newPattern, ruleParts[1], category);
    if (!result) {
      // there was some error in the expression so generate the error
      generateError("Syntax error - pattern not declared properly - see" +
                    "line " + file.getPointer() + " : " + line);
    }
  }

  /**
   * This method takes a method signature and searches if the method
   * @param method
   * @return a <tt>boolean</tt> value.
   */
  private boolean isMethodAvailable(String method) {
    // now first find the name of the method
    // their parameters and their types
    int index = method.indexOf("(");
    if (index == -1 || index == 0 ||
        method.charAt(method.length() - 1) != ')') {
      return false;
    }

    String methodName = method.substring(0, index);
    // now get the parameters

    String[] parameters;
    int[] userMethodParams;

    String arguments = method.substring(index + 1, method.length() - 1);
    if (arguments == null || arguments.trim().length() == 0) {
      parameters = null;
      userMethodParams = null;
    }
    else {
      parameters = method.substring(index + 1, method.length() - 1).split(",");
      userMethodParams = new int[parameters.length];
    }

    // find the parameter types
    // here we define only three types of arguments
    // String, boolean and int
    if (parameters != null) {
      for (int i = 0; i < parameters.length; i++) {
        if (parameters[i].startsWith("\"") && parameters[i].endsWith("\"")) {
          userMethodParams[i] = 7;
          parameters[i] = "java.lang.String";
          continue;
        }
        else if (ParsingFunctions.isBoolean(parameters[i])) {
          userMethodParams[i] = 6;
          parameters[i] = "boolean";
        }
        else if (ParsingFunctions.isInteger(parameters[i])) {
          userMethodParams[i] = 2;
          parameters[i] = "int";
        }
        else {
          // type cannot be recognized so generate error
          return false;
        }
      }
    }

    // now parameters have been found, so check them with the available methods
    // in the morph function
    Outer:for (int i = 0; i < methods.length; i++) {
      if (methods[i].getName().equals(methodName)) {
        // yes method has found now check for the parameters compatibility
        Class[] methodParams = methods[i].getParameterTypes();
        // first check for the number of parameters
        if (methods[i].getName().equals("null_stem")) {
          return true;
        }
        if (methodParams.length == parameters.length) {
          // yes arity has matched
          // now set the precedence
          int[] paramPrecedence = new int[methodParams.length];

          // assign precedence
          for (int j = 0; j < methodParams.length; j++) {
            if (methodParams[j].getName().equals("java.lang.String"))
              paramPrecedence[j] = 7;
            else if (methodParams[j].getName().equals("boolean"))
              paramPrecedence[j] = 6;
            else if (methodParams[j].getName().equals("int"))
              paramPrecedence[j] = 2;
            else
              return false;
          }

          // if we are here that means all the type matched
          // so valid method declaration
          return true;
        }
      }
    }
    // if we are here that means method doesnot found
    return false;
  }

  /**
   * Generates the error and stop the execution
   * @param mess - message to be displayed as an error on the standard output
   */
  private void generateError(String mess)
      throws ResourceInstantiationException {
    System.out.println("\n\n" + mess);
    System.out.println("Program terminated...");
    throw new ResourceInstantiationException("\n\n"+mess);
  }

  /**
   * Main method
   * @param args
   */
  public static void main(String[] args)
      throws ResourceInstantiationException {
    if (args == null || args.length < 3) {
      System.out.println("Usage : Interpret <Rules fileName> <word> <POS>");
      System.exit( -1);
    }
    Interpret interpret = new Interpret();
    try{
      interpret.init(new URL((String)args[0]));
    }catch(MalformedURLException mue){
      throw new RuntimeException(mue);
    }
    String rootWord = interpret.runMorpher(args[1], args[2]);
    String affix = interpret.getAffix();
    System.out.println("Root : "+rootWord);
    System.out.println("affix : "+affix);
  }

  /**
   * This method tells what was the affix to the provided word
   * @return affix
   */
  public String getAffix() {
    return this.affix;
  }
}