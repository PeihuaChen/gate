/**
 * 
 */
package gate.creole.ontology;

/**
 * @author niraj
 *
 */
public interface AllValuesFromRestriction extends Restriction {

    /**
     * Returns the resource which is set as a restricted value.
     * @return
     */
    public OResource getHasValue();
    
    
    /**
     * Sets the resource as a restricted value.
     * @param resource
     */
    public void setHasValue(OResource resource);
}
