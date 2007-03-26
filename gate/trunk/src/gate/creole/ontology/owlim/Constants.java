package gate.creole.ontology.owlim;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Constants {
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
   * Pattern representing OWL namespace
   */
  public static final Matcher OWL_PATTERN = Pattern.compile(
          "http://www.w3.org/2002/07/owl").matcher("");

  /**
   * denotes the pattern for XML Schema namespace
   */
  public static final Matcher XML_SCHEMA_PATTERN = Pattern.compile(
          "http://www.w3.org/2001/XMLSchema").matcher("");

  /**
   * denotes the pattern for RDF Schema Namespace
   */
  public static final Matcher RDF_SYNTAX_PATTERN = Pattern.compile(
          "http://www.w3.org/1999/02/22-rdf-syntax-ns").matcher("");

  /**
   * denotes the pattern for RDF Schema Namespace
   */
  public static final Matcher RDF_SCHEMA_PATTERN = Pattern.compile(
          "http://www.w3.org/2000/01/rdf-schema").matcher("");
}
