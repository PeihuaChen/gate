/**
 * 
 */
package gate.creole.ontology;

import java.util.EventListener;

/**
 * @author niraj
 * 
 */
public interface OntologyModificationListener extends EventListener {
  public void ontologyModified(OntologyModificationEvent ome);
}