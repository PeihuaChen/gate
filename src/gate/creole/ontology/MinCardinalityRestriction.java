/*
 * MinCardinalityRestriction.java
 *
 * $Id$
 * 
 */
package gate.creole.ontology;

/**
 * A MinCardinalityRestriction.
 *
 * @author Niraj Aswani
 *
 */
public interface MinCardinalityRestriction extends Restriction {

    /**
     * This method returns the mimimum cardinality value allowed for this value.
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
