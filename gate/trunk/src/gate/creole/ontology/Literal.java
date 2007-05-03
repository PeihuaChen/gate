/*
 *  Literal.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: Literal.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.creole.ontology;

import java.util.Locale;

/**
 * Literal represents a single value or a value with language used for
 * annotation properties, or a value with datatype used for the datatype
 * properties.
 * 
 * @author niraj
 */
public class Literal {
  /**
   * The actual value of the literal
   */
  private String value;

  /**
   * Specified language for the literal.
   */
  private Locale language;

  /**
   * Assigned Datatype to this instance of literal
   */
  private DataType dataType;

  /**
   * Constructor
   * 
   * @param value
   */
  public Literal(String value) {
    this.value = value;
  }

  /**
   * Constructor
   * 
   * @param value
   * @param language
   */
  public Literal(String value, Locale language) {
    this.value = value;
    this.language = language;
  }

  /**
   * Constructor
   * 
   * @param value
   * @param dataType
   * @throws InvalidValueException
   */
  public Literal(String value, DataType dataType) throws InvalidValueException {
    this.value = value;
    this.dataType = dataType;
    // lets check if the provided value is valid for the supplied
    // dataType
    if(!dataType.isValidValue(this.value)) {
      throw new InvalidValueException("The value :\"" + this.value
              + "\" is not compatible with the dataType \""
              + dataType.getXmlSchemaURI() + "\"");
    }
  }

  /**
   * Gets the assigned datatype. This may return null if user did not
   * use the Literal(String, Datatype) constructor to instantiate the
   * instance.
   * 
   * @return
   */
  public DataType getDataType() {
    return dataType;
  }

  /**
   * Returns the value associated with this instance of literal.
   * 
   * @return
   */
  public String getValue() {
    return value;
  }

  /**
   * Returns the language associated with this literal. This may return
   * null if use did not use the Literal(String, String) constructor to
   * instantiate the instance.
   * 
   * @return
   */
  public Locale getLanguage() {
    return language;
  }
  
}
