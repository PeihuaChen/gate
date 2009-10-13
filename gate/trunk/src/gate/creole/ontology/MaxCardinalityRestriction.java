/*
 * MaxCardinalityRestriction.java
 *
 * $Id$
 * 
 */
package gate.creole.ontology;

/**
 * A MaxCardinalityRestriction.
 *
 * @author Niraj Aswani
 *
 */
public interface MaxCardinalityRestriction extends Restriction {

    /**
     * This method returns the maximum cardinality value allowed for this value.
     * @return
     */
    public String getValue();
    
    /**
     * This method returns the datatype associated to the restriction.
     * @return
     */
    public DataType getDataType();
    
  /**
   * Sets the cardinality value.
   * @param value
   * @param dataType
   * @throws InvalidValueException
   */
  public void setValue(String value, DataType dataType) throws InvalidValueException;
    
}
