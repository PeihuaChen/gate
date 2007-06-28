/**
 * 
 */
package gate.creole.ontology;

/**
 * @author niraj
 *
 */
public interface CardinalityRestriction extends Restriction {

    /**
     * This method returns the cardinality value.
     * @return
     */
    public String getValue();
    
    /**
     * This method returns the datatype uri.
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
