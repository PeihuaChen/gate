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
    // TODO: do the escaping correctly!
    String value = this.value;
    value = value.replace("\"", "\\\"");
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

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Literal other = (Literal) obj;
    // if both are Literals, they are only equal if the dataTypes are the same
    // and if the languages are the same and if the values are the same
    if ((this.value == null) && (other.value == null)) {
      return true;
    }
    if(!this.dataType.equals(other.dataType) ||
       !this.language.equals(other.language) ||
       !this.value.equals(other.value)) {
      return false;
    }
    return true;
  }

}
