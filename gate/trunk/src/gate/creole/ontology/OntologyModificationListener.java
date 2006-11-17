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
    /**
     * This method is invoked whenever the respective ontology is modified
     * @param ome provides information about the source of the event, affected resource and the
     * type of modification.
     */
    public void ontologyModified(OntologyModificationEvent ome);
}