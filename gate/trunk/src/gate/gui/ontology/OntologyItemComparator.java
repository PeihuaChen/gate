/*
 *  OntologyItemComparator.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: OntologyItemComparator.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */

package gate.gui.ontology;

import gate.creole.ontology.OResource;
import java.util.Comparator;

/**
 * A Comparator that sorts the resources in ontology based on their URIs
 * 
 * @author niraj
 * 
 */
public class OntologyItemComparator implements Comparator {
  public OntologyItemComparator() {
  }

  public int compare(Object obj, Object obj1) {
    if(obj == null) return obj1 != null ? -1 : 0;
    if(obj1 == null) return obj != null ? 1 : 0;
    if((obj instanceof OResource) && (obj1 instanceof OResource)) {
      String s = ((OResource)obj).getURI().toString();
      String s1 = ((OResource)obj1).getURI().toString();
      if(s == null) return s1 != null ? -1 : 0;
      if(s1 == null)
        return s != null ? 1 : 0;
      else return s.compareTo(s1);
    }
    else {
      return 0;
    }
  }
}
