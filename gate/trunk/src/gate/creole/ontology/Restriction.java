/**
 * 
 */
package gate.creole.ontology;

/**
 * @author niraj This interface defines a restriction in the ontology.
 *         The restriction is specified on a property.
 */
public interface Restriction extends AnonymousClass {

    /**
     *  Return the property on which the restriction is specified
     */
    public RDFProperty getOnPropertyValue();
    
    /**
     * Sets the property on which the restriction is specified
     * @param property
     */
    public void setOnPropertyValue(RDFProperty property);
    
}