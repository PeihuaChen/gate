package gate.creole.morph;

import java.util.ArrayList;
import java.util.regex.*;
import java.util.Iterator;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class CompiledRules {

  private ArrayList patterns;
  private int pointer;

  public CompiledRules() {
    patterns = new ArrayList();
    pointer = 0;
  }

  public boolean add(String pattern, String function) {
    try {
      Pattern p = Pattern.compile(pattern.trim());
      MyPattern mp = new MyPattern(p, function);
      patterns.add(mp);
      return true;
    } catch(PatternSyntaxException pse) {
      return false;
    }
  }

  public boolean hasNext() {
    if(pointer < patterns.size()) {
      return true;
    } else {
      return false;
    }
  }

  public MyPattern getNext() {
    if(hasNext()) {
      MyPattern mp = (MyPattern) (patterns.get(pointer));
      pointer++;
      return mp;
    } else {
      return null;
    }
  }

  public void resetPointer() {
    this.pointer = 0;
  }
}