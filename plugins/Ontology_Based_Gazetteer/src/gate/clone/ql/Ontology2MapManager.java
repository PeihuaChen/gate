/**
 * 
 */
package gate.clone.ql;

import java.util.HashMap;
import java.util.Map;

import gate.clone.ql.utils.Ontology2Map;
import gate.creole.ontology.Ontology;

/**
 * @author danica damljanovic
 * 
 */
public class Ontology2MapManager {
  private Ontology2MapManager() {
  }

  private static Ontology2MapManager myInstance;
  private static Map ontology2Map = new HashMap();

  public static Ontology2MapManager getInstance() {
    if(myInstance == null) myInstance = new Ontology2MapManager();
    return myInstance;
  }

  public void addOntologyToIndex(Ontology ontology) {
    Ontology2Map os = new Ontology2Map(ontology);
    ontology2Map.put(ontology.getURL().toString(), os);
  }

  public Ontology2Map getOntology2Map(String ontologyURL) {
    return (Ontology2Map)ontology2Map.get(ontologyURL);
  }
}
