package gate.creole.morph;

import java.util.regex.Pattern;
import java.util.Comparator;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class MyPattern implements Comparable {

  private Pattern pattern;
  private String function;

  public MyPattern(Pattern pattern, String function) {
    this.pattern = pattern;
    this.function = function;
  }

  public int compareTo(Object obj) {
    // this is always to be added at the end
    return 1;
  }

  public Pattern getPattern() {
    return pattern;
  }

  public void setPattern(Pattern pattern) {
    this.pattern = pattern;
  }

  public String getFunction() {
    return function;
  }

  public void setFunction(String function) {
    this.function = function;
  }
}