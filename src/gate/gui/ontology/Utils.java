/*
 *  Utils.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: Utils.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.gui.ontology;

import java.util.regex.Pattern;

/**
 * This class provides various static utility methods which are used by
 * the ontology editor.
 * 
 * @author niraj
 * 
 */
public class Utils {

  /**
   * Checks whether the provided name space is valid name space. In this
   * version, the namespace must match the following java regular
   * expression. <BR>
   * 
   * "[a-zA-Z]+(:)(/)+[a-zA-Z0-9\\-]+((\\.)[a-zA-Z0-9\\-]+)+((/)[a-zA-Z0-9\\.\\-_]+)*(#|/)"
   * 
   * @param s
   * @return
   */
  public static boolean isValidNameSpace(String s) {
    String s1 = new String(
            "[a-zA-Z]+(:)(/)+[a-zA-Z0-9\\-]+((\\.)[a-zA-Z0-9\\-]+)+((/)[a-zA-Z0-9\\.\\-_]+)*(#|/)");
    Pattern pattern = Pattern.compile(s1);
    return pattern.matcher(s).matches();
  }

  /**
   * Checks whether the provided resource name is a valid resource name
   * In this version, the resource name must match the following java
   * regular expression <BR>. "[a-zA-Z0-9_-]+"
   * 
   * @param s
   * @return
   */
  public static boolean isValidOntologyResourceName(String s) {
    String s1 = new String("[a-zA-Z0-9_-]+");
    Pattern pattern = Pattern.compile(s1);
    return pattern.matcher(s).matches();
  }
}
