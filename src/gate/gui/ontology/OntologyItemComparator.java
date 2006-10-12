package gate.gui.ontology;

import gate.creole.ontology.OntologyResource;
import java.util.Comparator;

public class OntologyItemComparator implements Comparator {
  public OntologyItemComparator() {
  }

  public int compare(Object obj, Object obj1) {
    if(obj == null) return obj1 != null ? -1 : 0;
    if(obj1 == null) return obj != null ? 1 : 0;
    if((obj instanceof OntologyResource) && (obj1 instanceof OntologyResource)) {
      String s = ((OntologyResource)obj).getName();
      String s1 = ((OntologyResource)obj1).getName();
      if(s == null) return s1 != null ? -1 : 0;
      if(s1 == null)
        return s != null ? 1 : 0;
      else return s.compareTo(s1);
    } else {
      return 0;
    }
  }
}
