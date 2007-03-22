/*
 *  DataType.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: DataType.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.creole.ontology;

/**
 * This class provides a list of datatypes, supported by the ontology API.
 * @author niraj
 */
public class DataType {
  /**
   * for each datatype, there exists a XML Schema URI in ontology which is used
   * to denote the specific datatype. For example to denote the boolean datatype
   * one would have to use "http://www.w3.org/2001/XMLSchema#boolean".
   * 
   */
  protected URI xmlSchemaURI;

  /**
   * Constructor
   * 
   * @param xmlSchemaURI
   *          for each datatype, there exists a XML Schema URI in ontology which
   *          is used to denote the specific datatype. For example to denote the
   *          boolean datatype one would have to use
   *          "http://www.w3.org/2001/XMLSchema#boolean".
   */
  public DataType(URI xmlSchemaURI) {
    this.xmlSchemaURI = xmlSchemaURI;
  }

  /**
   * denotes the "http://www.w3.org/2001/XMLSchema#boolean" datatype.
   */
  public static DataType getBooleanDataType() {
    try {
      return new BooleanDT(new URI("http://www.w3.org/2001/XMLSchema#boolean", false));
    } catch(InvalidURIException iue) {
      return null;
    }
  }

  /**
   * denotes the "http://www.w3.org/2001/XMLSchema#byte" datatype.
   */
  public static DataType getByteDataType() {
    try {
      return new ByteDT(new URI("http://www.w3.org/2001/XMLSchema#byte", false));
    } catch(InvalidURIException iue) {
      return null;
    }
  }

  /**
   * denotes the "http://www.w3.org/2001/XMLSchema#date" datatype.
   */
  public static DataType getDateDataType() {
    try {
      return new DataType(new URI("http://www.w3.org/2001/XMLSchema#date", false));
    } catch(InvalidURIException iue) {
      return null;
    }
  }

  /**
   * denotes the "http://www.w3.org/2001/XMLSchema#decimal" datatype.
   */
  public static DataType getDecimalDataType() {
    try {
      return new DoubleDT(new URI(
              "http://www.w3.org/2001/XMLSchema#decimal", false));
    } catch(InvalidURIException iue) {
      return null;
    }
  }

  /**
   * denotes the "http://www.w3.org/2001/XMLSchema#double" datatype.
   */
  public static DataType getDoubleDataType() {
    try {
      return new DoubleDT(
              new URI("http://www.w3.org/2001/XMLSchema#double", false));
    } catch(InvalidURIException iue) {
      return null;
    }
  }

  /**
   * denotes the "http://www.w3.org/2001/XMLSchema#duration" datatype.
   */
  public static DataType getDurationDataType() {
    try {
      return new LongDT(
              new URI("http://www.w3.org/2001/XMLSchema#duration", false));
    } catch(InvalidURIException iue) {
      return null;
    }
  }

  /**
   * denotes the "http://www.w3.org/2001/XMLSchema#float" datatype.
   */
  public static DataType getFloatDataType() {
    try {
      return new FloatDT(new URI("http://www.w3.org/2001/XMLSchema#float", false));
    } catch(InvalidURIException iue) {
      return null;
    }
  }

  /**
   * denotes the "http://www.w3.org/2001/XMLSchema#int" datatype.
   */
  public static DataType getIntDataType() {
    try {
      return new IntegerDT(new URI("http://www.w3.org/2001/XMLSchema#int", false));
    } catch(InvalidURIException iue) {
      return null;
    }
  }

  /**
   * denotes the "http://www.w3.org/2001/XMLSchema#integer" datatype.
   */
  public static DataType getIntegerDataType() {
    try {
      return new IntegerDT(new URI(
              "http://www.w3.org/2001/XMLSchema#integer", false));
    } catch(InvalidURIException iue) {
      return null;
    }
  }

  /**
   * denotes the "http://www.w3.org/2001/XMLSchema#long" datatype.
   */
  public static DataType getLongDataType() {
    try {
      return new LongDT(new URI("http://www.w3.org/2001/XMLSchema#long", false));
    } catch(InvalidURIException iue) {
      return null;
    }
  }

  /**
   * denotes the "http://www.w3.org/2001/XMLSchema#negativeInteger" datatype.
   */
  public static DataType getNegativeIntegerDataType() {
    try {
      return new NegativeIntegerDT(new URI(
              "http://www.w3.org/2001/XMLSchema#negativeInteger", false));
    } catch(InvalidURIException iue) {
      return null;
    }
  }

  /**
   * denotes the "http://www.w3.org/2001/XMLSchema#nonNegativeInteger" datatype.
   */
  public static DataType getNonNegativeIntegerDataType() {
    try {
      return new NonNegativeIntegerDT(new URI(
              "http://www.w3.org/2001/XMLSchema#nonNegativeInteger", false));
    } catch(InvalidURIException iue) {
      return null;
    }
  }

  /**
   * denotes the "http://www.w3.org/2001/XMLSchema#nonPositiveInteger" datatype.
   */
  public static DataType getNonPositiveIntegerDataType() {
    try {
      return new NegativeIntegerDT(new URI(
              "http://www.w3.org/2001/XMLSchema#nonPositiveInteger", false));
    } catch(InvalidURIException iue) {
      return null;
    }
  }

  /**
   * denotes the "http://www.w3.org/2001/XMLSchema#positiveInteger" datatype.
   */
  public static DataType getPositiveIntegerDataType() {
    try {
      return new NonNegativeIntegerDT(new URI(
              "http://www.w3.org/2001/XMLSchema#positiveInteger", false));
    } catch(InvalidURIException iue) {
      return null;
    }
  }

  /**
   * denotes the "http://www.w3.org/2001/XMLSchema#short" datatype.
   */
  public static DataType getShortDataType() {
    try {
      return new ShortDT(new URI("http://www.w3.org/2001/XMLSchema#short", false));
    } catch(InvalidURIException iue) {
      return null;
    }
  }

  /**
   * denotes the "http://www.w3.org/2001/XMLSchema#string" datatype.
   */
  public static DataType getStringDataType() {
    try {
      return new DataType(
              new URI("http://www.w3.org/2001/XMLSchema#string", false));
    } catch(InvalidURIException iue) {
      return null;
    }
  }

  /**
   * denotes the "http://www.w3.org/2001/XMLSchema#time" datatype.
   */
  public static DataType getTimeDataType() {
    try {
      return new DataType(new URI("http://www.w3.org/2001/XMLSchema#time", false));
    } catch(InvalidURIException iue) {
      return null;
    }
  }

  /**
   * denotes the "http://www.w3.org/2001/XMLSchema#unsignedByte" datatype.
   */
  public static DataType getUnsignedByteDataType() {
    try {
      return new UnsignedByteDT(new URI(
              "http://www.w3.org/2001/XMLSchema#unsignedByte", false));
    } catch(InvalidURIException iue) {
      return null;
    }
  }

  /**
   * denotes the "http://www.w3.org/2001/XMLSchema#unsignedInt" datatype.
   */
  public static DataType getUnsignedIntDataType() {
    try {
      return new NonNegativeIntegerDT(new URI(
              "http://www.w3.org/2001/XMLSchema#unsignedInt", false));
    } catch(InvalidURIException iue) {
      return null;
    }
  }

  /**
   * denotes the "http://www.w3.org/2001/XMLSchema#unsignedLong" datatype.
   */
  public static DataType getUnsignedLongDataType() {
    try {
      return new UnsignedLongDT(new URI(
              "http://www.w3.org/2001/XMLSchema#unsignedLong", false));
    } catch(InvalidURIException iue) {
      return null;
    }
  }

  /**
   * denotes the "http://www.w3.org/2001/XMLSchema#unsignedShort" datatype.
   */
  public static DataType getUnsignedShortDataType() {
    try {
      return new UnsignedShortDT(new URI(
              "http://www.w3.org/2001/XMLSchema#unsignedShort", false));
    } catch(InvalidURIException iue) {
      return null;
    }
  }

  public URI getXmlSchemaURI() {
    return xmlSchemaURI;
  }

  /**
   * Compares if the two objects are same.
   */
  public boolean equals(Object o) {
    if(o instanceof DataType) {
      DataType dt = (DataType)o;
      return this.xmlSchemaURI.getNameSpace().equals(dt.xmlSchemaURI.getNameSpace()) &&
              this.xmlSchemaURI.getResourceName().equals(dt.xmlSchemaURI.getResourceName());
    }
    return false;
  }

  /**
   * Checks whether the provided value is a valid value for the datatype (e.g.
   * if the datatype is integer, parsing a string value into integer causes the
   * exception or not.
   * 
   * @param value
   * @return true, if the provided value can be parsed correctly into the
   *         datatype, otherwise - false.
   */
  public boolean isValidValue(String value) {
    return true;
  }
}

/**
 * Boolean DataType 
 * @author niraj
 */
class BooleanDT extends DataType {
  public BooleanDT(URI xmlSchemaURI) {
    super(xmlSchemaURI);
  }

  /**
   * A Method to validate the boolean value
   */
  public boolean isValidValue(String value) {
    try {
      if((Boolean.parseBoolean(value)+"").equalsIgnoreCase(value)) return true;
      return false;
    } catch(Exception e) {
      return false;
    }
  }
}

/**
 * Byte DataType
 * @author niraj
 *
 */
class ByteDT extends DataType {
  public ByteDT(URI xmlSchemaURI) {
    super(xmlSchemaURI);
  }

  /**
   * Methods check if the value is valid for the datatype
   */
  public boolean isValidValue(String value) {
    try {
      if((Byte.parseByte(value)+"").equalsIgnoreCase(value)) return true;;
      return false;
    } catch(Exception e) {
      return false;
    }
  }
}

/**
 * Double Datatype
 * @author niraj
 *
 */
class DoubleDT extends DataType {
  public DoubleDT(URI xmlSchemaURI) {
    super(xmlSchemaURI);
  }

  /**
   * Methods check if the value is valid for the datatype
   */
  public boolean isValidValue(String value) {
    try {
      if((Double.parseDouble(value)+"").equalsIgnoreCase(value)) return true;
      return false;
    } catch(Exception e) {
      return false;
    }
  }
}

/**
 * Long Datatype
 * @author niraj
 *
 */
class LongDT extends DataType {
  public LongDT(URI xmlSchemaURI) {
    super(xmlSchemaURI);
  }

  /**
   * Methods check if the value is valid for the datatype
   */
  public boolean isValidValue(String value) {
    try {
      if((Long.parseLong(value)+"").equalsIgnoreCase(value)) return true;;
      return false;
    } catch(Exception e) {
      return false;
    }
  }
}

/**
 * Float Datatype
 * @author niraj
 *
 */
class FloatDT extends DataType {
  public FloatDT(URI xmlSchemaURI) {
    super(xmlSchemaURI);
  }

  /**
   * Methods check if the value is valid for the datatype
   */
  public boolean isValidValue(String value) {
    try {
      if((Float.parseFloat(value)+"").equalsIgnoreCase(value)) return true;;
      return false;
    } catch(Exception e) {
      return false;
    }
  }
}

/**
 * Integer Datatype
 * @author niraj
 *
 */
class IntegerDT extends DataType {
  public IntegerDT(URI xmlSchemaURI) {
    super(xmlSchemaURI);
  }

  /**
   * Methods check if the value is valid for the datatype
   */
  public boolean isValidValue(String value) {
    try {
      if((Integer.parseInt(value)+"").equalsIgnoreCase(value)) return true;;
      return false;
    } catch(Exception e) {
      return false;
    }
  }
}

/**
 * Negative Integer Datatype
 * @author niraj
 *
 */
class NegativeIntegerDT extends DataType {
  public NegativeIntegerDT(URI xmlSchemaURI) {
    super(xmlSchemaURI);
  }

  /**
   * Methods check if the value is valid for the datatype
   */
  public boolean isValidValue(String value) {
    try {
      int intVal = Integer.parseInt(value);
      if(!(intVal+"").equalsIgnoreCase(value)) return false;
      return intVal < 0;
    } catch(Exception e) {
      return false;
    }
  }
}

/**
 * NonNegativeInteger Datatype
 * @author niraj
 *
 */
class NonNegativeIntegerDT extends DataType {
  public NonNegativeIntegerDT(URI xmlSchemaURI) {
    super(xmlSchemaURI);
  }

  /**
   * Methods check if the value is valid for the datatype
   */
  public boolean isValidValue(String value) {
    try {
      int intVal = Integer.parseInt(value);
      if(!(intVal+"").equalsIgnoreCase(value)) return false;
      return intVal > -1;
    } catch(Exception e) {
      return false;
    }
  }
}

/**
 * Short Datatype
 * @author niraj
 *
 */
class ShortDT extends DataType {
  public ShortDT(URI xmlSchemaURI) {
    super(xmlSchemaURI);
  }

  /**
   * Methods check if the value is valid for the datatype
   */
  public boolean isValidValue(String value) {
    try {
      short intVal = Short.parseShort(value);
      if(!(intVal+"").equalsIgnoreCase(value)) return false;
      return true;
    } catch(Exception e) {
      return false;
    }
  }
}

/**
 * UnsignedByte Datatype
 * @author niraj
 *
 */
class UnsignedByteDT extends DataType {
  public UnsignedByteDT(URI xmlSchemaURI) {
    super(xmlSchemaURI);
  }

  /**
   * Methods check if the value is valid for the datatype
   */
  public boolean isValidValue(String value) {
    try {
      byte byteVal = Byte.parseByte(value);
      if(!(byteVal+"").equalsIgnoreCase(value)) return false;
      return byteVal > -1;
    } catch(Exception e) {
      return false;
    }
  }
}

/**
 * UnsignedLong Datatype
 * @author niraj
 *
 */
class UnsignedLongDT extends DataType {
  public UnsignedLongDT(URI xmlSchemaURI) {
    super(xmlSchemaURI);
  }

  /**
   * Methods check if the value is valid for the datatype
   */
  public boolean isValidValue(String value) {
    try {
      long longVal = Long.parseLong(value);
      if(!(longVal+"").equalsIgnoreCase(value)) return false;
      return longVal > -1;
    } catch(Exception e) {
      return false;
    }
  }
}

/**
 * UnsignedShort Datatype
 * @author niraj
 *
 */
class UnsignedShortDT extends DataType {
  public UnsignedShortDT(URI xmlSchemaURI) {
    super(xmlSchemaURI);
  }

  /**
   * Methods check if the value is valid for the datatype
   */
  public boolean isValidValue(String value) {
    try {
      short shortVal = Short.parseShort(value);
      if(!(shortVal+"").equalsIgnoreCase(value)) return false;
      return shortVal > -1;
    } catch(Exception e) {
      return false;
    }
  }
}
