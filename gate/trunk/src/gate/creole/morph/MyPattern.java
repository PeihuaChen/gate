package gate.creole.morph;

import java.util.regex.Pattern;
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
  private String category;

  public MyPattern(Pattern pattern, String function, String category) {
    this.pattern = pattern;
    this.function = function;
    this.category = category;
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

  public String getCategory() {
    return category;
  }

  public boolean isSameCategory(String category) {
    if(this.category.equals("*")) {
       return true;
    } else {
      if (this.category.equals("verb")) {
        if (category.equals("VB") ||
            category.equals("VBD") ||
            category.equals("VBG") ||
            category.equals("VBN") ||
            category.equals("VBP") ||
            category.equals("VBZ")) {
          return true;
        }
        else {
          return false;
        }
      }
      else if (this.category.equals("noun")) {
        if (category.equals("NN") ||
            category.equals("NNP") ||
            category.equals("NNPS") ||
            category.equals("NNS") ||
            category.equals("NP") ||
            category.equals("NPS")) {
          return true;
        }
        else {
          return false;
        }
      } else {
        return false;
      }
    }
  }

  public void setCategory(String cat) {
    this.category = cat;
  }
}