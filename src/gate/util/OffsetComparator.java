package gate.util;

import java.util.Comparator;
import gate.*;

/**
 * Compares annotations by start offset
 */
public class OffsetComparator implements Comparator {

  public int compare(Object o1, Object o2){
    Annotation a1 = (Annotation)o1;
    Annotation a2 = (Annotation)o2;
    return a1.getStartNode().getOffset().compareTo(
            a2.getStartNode().getOffset());
  }
}