package gate.creole.morph;

import java.util.HashMap;
import java.util.Iterator;

/**
 * <p>Title: Storage.java </p>
 * <p>Description: This class is used as the storage in the system, where
 * all the declared variables and their appropriate values are stored </p>
 */
public class Storage {

  /**
   * Stores variable name as the key, and its variable values as values of these
   * keys
   */
  private HashMap variables;

  /**
   * Constructor
   */
  public Storage() {
    variables = new HashMap();
  }

  /**
   * Adds the variable name and its value into the hashTable
   * @param varName name of the variable
   * @param varValue value for the variable
   * @return true if variable doesnot exist, false otherwise
   */
  public boolean add(String varName, String varValue) {
    if(variables.containsKey(varName)) {
      return false;
    } else {

      // before storing varValue try to find if it is
      // a Character Range
      // a Character Set
      // a Sting Set

      variables.put(varName,varValue);
      return true;
    }
  }

  /**
   * This method looks into the hashtable and searches for the value of the
   * given variable
   * @param varName
   * @return value of the variable if variable found in the table,null otherwise
   */
  public String get(String varName) {
    String varValue = (String)(variables.get(varName));
    return varValue;
  }

  /**
   * This method checks for the existance of the variable into the hashtable
   * @param varName
   * @return true if variable exists, false otherwise
   */
  public boolean isExist(String varName) {
    if(variables.containsKey(varName)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Update the variable with the new value. If variable doesnot exist, add it
   * to the hashtable
   * @param varName name of the variable to be updated, or added
   * @param varValue value of the variable
   */
  public void update(String varName,String varValue) {
    variables.put(varName,varValue);

  }

  /**
   * This method returns names of all the variables available in the hashtable
   * @return array of Strings - names of the variables
   */
  public String [] getVarNames() {
    Iterator iter = variables.keySet().iterator();
    String [] varNames = new String[variables.size()];
    int i=0;
    while(iter.hasNext()) {
      varNames[i] = (String)(iter.next());
      i++;
    }
    return varNames;
  }
}