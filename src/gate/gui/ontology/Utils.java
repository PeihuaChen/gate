package gate.gui.ontology;

import java.util.regex.Pattern;

public class Utils {
  public Utils() {
  }

  public static boolean isValidNameSpace(String s) {
    String s1 = new String(
            "[a-zA-Z]+(:)(/)+[a-zA-Z0-9]+((\\.)[a-zA-Z0-9]+)+((/)[a-zA-Z0-9]+)*(#)");
    Pattern pattern = Pattern.compile(s1);
    return pattern.matcher(s).matches();
  }

  public static boolean isValidOntologyResourceName(String s) {
    String s1 = new String("[a-zA-Z0-9_-]+");
    Pattern pattern = Pattern.compile(s1);
    return pattern.matcher(s).matches();
  }
}
