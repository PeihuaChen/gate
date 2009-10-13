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
 * @author Niraj Aswani
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
    this.language = OConstants.ENGLISH;
    this.dataType = DataType.getStringDataType();
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
    this.dataType = DataType.getStringDataType(); 
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
    this.language = OConstants.ENGLISH;
    this.dataType = dataType;
    // lets check if the provided value is valid for the supplied
    // dataType
    if(!dataType.isValidValue(this.value)) {
      throw new InvalidValueException("The value :\"" + this.value
              + "\" is not compatible with the dataType \""
              + dataType.getXmlSchemaURIString() + "\"");
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

  public String toString() {
    return value;
  }


  public String toTurtle() {
    // make an attempt to convert the string into turtle syntax:
    // quote it, escape embedded special characters like quotes,
    // if it is a string, add a language identifier if we have one,
    // if it is not a string, add a datatype uri if we have oen.
    // TODO: do the escaping correctly!
    value.replace("\"", "\\\"");
    value = "\""+value+"\"";
    if(dataType.isStringDataType()) {
      if(language != null) {
        value = value+"@"+language;
      } else {
        value = value+"^^<" + dataType.getXmlSchemaURIString() + ">";
      }
    } else {
      value = value+"^^<" + dataType.getXmlSchemaURIString() + ">";
    }
    return value;
  }

}
