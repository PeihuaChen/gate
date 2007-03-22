/*
 *  OConstants.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id: OConstants.html,v 1.0 2007/03/09 16:13:01 niraj Exp $
 */
package gate.creole.ontology;

/**
 * This interface holds some constants used by several other intrfaces
 * and classes in the GATE ontology API.
 * 
 * @author Niraj Aswani
 */
public interface OConstants {
  /** denotes a direct closure(no transitivity) */
  public static final byte DIRECT_CLOSURE = 0;

  /** denotes atransitive closure */
  public static final byte TRANSITIVE_CLOSURE = 1;

  /**
   * denotes the rdf property
   */
  public static final byte RDF_PROPERTY = 0;

  /**
   * denotes the object property.
   */
  public static final byte OBJECT_PROPERTY = 1;

  /**
   * denotes the datatype property.
   */
  public static final byte DATATYPE_PROPERTY = 2;

  /**
   * denotes the symmetric property.
   */
  public static final byte SYMMETRIC_PROPERTY = 3;

  /**
   * denotes the transitive property.
   */
  public static final byte TRANSITIVE_PROPERTY = 4;

  /**
   * denotes the annotation property.
   */
  public static final byte ANNOTATION_PROPERTY = 5;

  /**
   * denotes the N3 ontology format
   */
  public static final byte ONTOLOGY_FORMAT_N3 = 0;

  /**
   * denotes the NTRIPLES ontology format
   */
  public static final byte ONTOLOGY_FORMAT_NTRIPLES = 1;

  /**
   * denotes the RDFXML ontology format
   */
  public static final byte ONTOLOGY_FORMAT_RDFXML = 2;

  /**
   * denotes the TURTLE ontology format
   */
  public static final byte ONTOLOGY_FORMAT_TURTLE = 3;

  /**
   * Name of the anonymouse class
   */
  public static final String ANONYMOUS_CLASS_NAME = "Anonymous";

  /**
   * denotes the addition of sub class event
   */
  public static final int SUB_CLASS_ADDED_EVENT = 0;

  /**
   * denotes the addition of super class event
   */
  public static final int SUPER_CLASS_ADDED_EVENT = 1;

  /**
   * denotes the removal of sub class event
   */
  public static final int SUB_CLASS_REMOVED_EVENT = 2;

  /**
   * denotes the removal of super class event
   */
  public static final int SUPER_CLASS_REMOVED_EVENT = 3;

  /**
   * denotes the event of two classes set as equivalent
   */
  public static final int EQUIVALENT_CLASS_EVENT = 4;

  /**
   * denotes the event when a comment on a resource is changed
   */
  public static final int COMMENT_CHANGED_EVENT = 5;

  /**
   * denotes the event when a label on a resource is changed
   */
  public static final int LABEL_CHANGED_EVENT = 6;

  /**
   * denotes the event when an annotation property is assigned to a
   * resource with some compatible value
   */
  public static final int ANNOTATION_PROPERTY_VALUE_ADDED_EVENT = 7;

  /**
   * denotes the event when a datatype property is assigned to a
   * resource with some compatible value
   */
  public static final int DATATYPE_PROPERTY_VALUE_ADDED_EVENT = 8;

  /**
   * denotes the event when an object property is assigned to a resource
   * with some compatible value
   */
  public static final int OBJECT_PROPERTY_VALUE_ADDED_EVENT = 9;

  /**
   * denotes the event when an rdf property is assigned to a resource
   * with some compatible value
   */
  public static final int RDF_PROPERTY_VALUE_ADDED_EVENT = 10;

  /**
   * denotes the event when an annotation property value is removed from
   * the resource
   */
  public static final int ANNOTATION_PROPERTY_VALUE_REMOVED_EVENT = 11;

  /**
   * denotes the event when a datatype property value is removed from
   * the resource
   */
  public static final int DATATYPE_PROPERTY_VALUE_REMOVED_EVENT = 12;

  /**
   * denotes the event when an object property value is removed from the
   * resource
   */
  public static final int OBJECT_PROPERTY_VALUE_REMOVED_EVENT = 13;

  /**
   * denotes the event when an rdf property value is removed from the
   * resource
   */
  public static final int RDF_PROPERTY_VALUE_REMOVED_EVENT = 14;

  /**
   * denotes the event when two instances are set to be different from
   * each other
   */
  public static final int DIFFERENT_INSTANCE_EVENT = 15;

  /**
   * denotes the event when two instances are set to be same instances
   */
  public static final int SAME_INSTANCE_EVENT = 16;

  /**
   * denotes the event when two properties are set to be equivalent
   */
  public static final int EQUIVALENT_PROPERTY_EVENT = 17;

  /**
   * denotes the event when a sub property is added to an existing
   * property
   */
  public static final int SUB_PROPERTY_ADDED_EVENT = 18;

  /**
   * denotes the event when a super property is added to an existing
   * property
   */
  public static final int SUPER_PROPERTY_ADDED_EVENT = 19;

  /**
   * denotes the event when a sub property is removed from an existing
   * property
   */
  public static final int SUB_PROPERTY_REMOVED_EVENT = 20;

  /**
   * denotes the event when a super property is removed from an existing
   * property
   */
  public static final int SUPER_PROPERTY_REMOVED_EVENT = 21;

}
