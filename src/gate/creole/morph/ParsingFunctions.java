package gate.creole.morph;

import java.util.Stack;
/**
 * <p>Title: ParsingFunctions.java </p>
 * <p>Description: This class implements all static methods, which can be used
 * for various purposes, like converting rules defined by users into the regular
 * expressions, finding varilable type from its value type etc. </p>
 */
public class ParsingFunctions {

  /**
   * Default Constructor
   */
  public ParsingFunctions() {

  }

  /**
   * This method takes the value of the variable and tells the user
   * what type of value is from CharacterRange, CharacterSet, StringSet
   * @param varValue value for which to find the variable type
   * @return ERROR_CODE = -4, STRING_SET_CODE = 0, CHARACTER_RANGE_CODE = 1,
   * CHARACTER_SET_CODE = 2;
   */
  public static int findVariableType(String varValue) {
    // if the value starts with " it is string set
    // if the value starts with "[-" it is a character range
    // if the value starts with "[" it is a character set
    // otherwise error
    if(varValue==null) {
      return Codes.ERROR_CODE;
    }

    if(varValue.length()>=3 && varValue.charAt(0)=='\"'
       && (varValue.lastIndexOf('\"')==(varValue.length()-1))) {
      // for string set it should be greater than 3 because
      // it requires at least one character to make the string
      // first and the last character should be "
      return Codes.STRING_SET_CODE;

    } else if(varValue.length()>=6 && (((varValue.length()-3)%3)==0)
              && varValue.substring(0,2).equals("[-")
              && varValue.charAt(varValue.length()-1)==']') {
     // for the character range it should be greater than 6 because
     // three characters as "[-" and "]"
     // and finally to define the range character-character
     return Codes.CHARACTER_RANGE_CODE;

    } else if(varValue.length()>=3 && varValue.charAt(0)=='['
              && varValue.charAt(varValue.length()-1)==']') {
        // for the character set it should be greater than 3 characters because
        // it requires at least one character
        // first and the last character should be [ and ] respectively
        if(varValue.charAt(1)=='-') {
          return Codes.ERROR_CODE;
        } else {
          return Codes.CHARACTER_SET_CODE;
        }

    } else {
      // there are some errors
      return Codes.ERROR_CODE;
    }

  }

  /**
   * This method checks for the string if it is a valid integer value
   * @param value value to be checked for its type to be integer
   * @return if value is an integer returns true, false otherwise
   */
  public static boolean isInteger(String value) {
    try {
      int no = Integer.parseInt(value);
    } catch(NumberFormatException nfe) {
      return false;
    }
    return true;
  }

  /**
    * This method checks for the string if it is a valid integer value
    * @param value value to be checked for its type to be integer
    * @return if value is an integer returns true, false otherwise
    */
   public static boolean isBoolean(String value) {
     if(value.equals("false") || value.equals("true")) {
       return true;
     } else {
       return false;
     }
   }

   /**
    * This method convert the expression which has been entered by the user
    * in the .rul file (i.e. rules defined by the user), into the expression
    * which are recognized by the regular expression Patterns
    * @param line rule defined by the user
    * @param storage this method internally requires values of the used
    * variables to replace the them with their values in the expression
    * @return newly generated regular expression
    */
   public static String convertToRegExp(String line,Storage storage) {
     // replace all OR with |
     line = line.replaceAll("( OR )", "|");
     line = line.replaceAll("(\\[\\-)","[");

     // we will use the stack concept here
     // for every occurence of '{', or '(' we will add that into the stack
     // and for every occurence of '}' or ')' we will remove that element from
     // the stack
     // if the value found between the bracket is an integer value
     // we won't replace those brackets
     StringBuffer newExpr = new StringBuffer(line);
     Stack stack = new Stack();
     Stack bracketIndexes = new Stack();

     for (int i = 0; i < newExpr.length(); i++) {
       if (newExpr.charAt(i) == '{') {
         // add it to the stack
         stack.add("{");
         bracketIndexes.add(new Integer(i));

       }
       else if (newExpr.charAt(i) == '(') {
         // add it to the stack
         stack.add("(");
         bracketIndexes.add(new Integer(i));

       }
       else if (newExpr.charAt(i) == '[') {
         // add it to the stack
         stack.add("[");
         bracketIndexes.add(new Integer(i));

       }
       else if (newExpr.charAt(i) == '\"') {
         // before adding it to the stack, check if this is the closing one
         if (stack.isEmpty() || !(((String)(stack.get(stack.size() - 1))).equals("\""))) {
           // yes this is the opening one
           // add it to the stack
           stack.add("\"");
           bracketIndexes.add(new Integer(i));
         } else {
           // this is the closing one
           String bracket = (String)(stack.pop());
           int index = ((Integer)(bracketIndexes.pop())).intValue();
           newExpr.setCharAt(index, '(');
           newExpr.setCharAt(i, ')');
        }
       }
       else if (newExpr.charAt(i) == '}') {
         // remove the element from the stack
         // it must be '{', otherwise generate the error
         String bracket = (String) (stack.pop());
         int index = ((Integer)(bracketIndexes.pop())).intValue();
         if (!bracket.equals("{")) {
           return null;
         }

         // now check if the value between these brackets is integer, that means
         // we don't need to change the brackets, otherwise change them to
         // '(' and ')'
         if (isInteger(newExpr.substring(index + 1, i))) {
           // yes it is an integer
           // continue
           continue;
         }
         else {
           // no it is string
           newExpr.setCharAt(index, '(');
           newExpr.setCharAt(i, ')');
         }

       }
       else if (newExpr.charAt(i) == ')') {
         // remove the element from the stack
         // it must be ')', otherwise generate the error
         String bracket = (String) (stack.pop());
         int index = ( (Integer) (bracketIndexes.pop())).intValue();
         if (!bracket.equals("(")) {
           return null;
         }
         continue;
       }
       else if (newExpr.charAt(i) == ']') {
         // remove the element from the stack
         // it must be '[', otherwise generate the error
         String bracket = (String) (stack.pop());
         int index = ( (Integer) (bracketIndexes.pop())).intValue();
         if (!bracket.equals("[")) {
           return null;
         }
       }
     }
     // check if all the stacks are empty then and only then the written
     // expression is true, otherwise it is incorrect
     if(!stack.empty() || !bracketIndexes.empty()) {
       return null;
     }
     //System.out.println(line+"  "+newExpr);
     // now we need to replace the variables with their values
     // but how would we know which is the variable
     // so get the variable list and check if it is available in the expression
     String [] varNames = storage.getVarNames();
     for(int i=0;i<varNames.length;i++) {
       // check for the occurance of each varName in the expression
       int index = -1;
       String myString = "{[()]} ";
       while((index=newExpr.indexOf(varNames[i],index+1))!=-1) {
         //System.out.println(index + "  "+newExpr.length());
         // now check for the left and right characters
         if(index>0) {
           if(myString.indexOf(newExpr.charAt(index-1))==-1) {
             index = index +varNames[i].length()-1;
             // this is not the varilable
             continue;
           }
         }
         if((varNames[i].length()+index)<newExpr.length()) {
           if(myString.indexOf(newExpr.charAt(varNames[i].length()+index))==-1) {
             index = index +varNames[i].length()-1;
             // this is not the variable
             continue;
          }
         }

         // yes it is a variable
         String replaceWith = "("+(String)(storage.get(varNames[i]))+")";
         newExpr.replace(index,(varNames[i].length()+index),replaceWith);
         index = index + replaceWith.length();
       }
     }
     return new String(newExpr);
   }
}