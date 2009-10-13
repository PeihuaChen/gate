/**
 * 
 */
package gate.creole.ontology;

/**
 * A SomeValuesFromRestriction.
 *
 * @author Niraj Aswani
 *
 */
public interface SomeValuesFromRestriction extends Restriction {

  /**
   * Returns the resource which is set as a value
   * 
   * @return
   */
  public OResource getHasValue();

  /**
   * Sets the resource as a restricted value.
   * 
   * @param resource
   */
  public void setHasValue(OResource resource);

}
