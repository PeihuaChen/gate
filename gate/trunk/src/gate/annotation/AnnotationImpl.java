/*	AnnotationImpl.java
 *
 *	Valentin Tablan, Jan/00
 *
 *	$Id$
 */

package  gate.annotation;
import gate.*;
import gate.util.*;

/** Provides an implementation for the interface gate.Annotation
  */
public class AnnotationImpl implements Annotation, FeatureBearer
{
  /** Constructor. Builds a new annotation.
    * @param id The id of the new annotation;
    * @param start The node from where the annotation will depart;
    * @param end The node where trhe annotation ends;
    * @param type The type of the new annotation;
    * @param features The features of the annotation.
    */
  AnnotationImpl(
    long id, Node start, Node end, String type, FeatureMap features
  ) {
    this.id = id;
    this.start = start;
    this.end = end;
    this.type = type;
    this.features = features;
  } // AnnotationImpl

  /** The type of the annotation (corresponds to TIPSTER "name"). */
  public String getType() {
    return  type;
  }

  /** The features, or content of this arc (corresponds to TIPSTER
    * "attributes", and to LDC "label", which is the simplest case).
    */
  public gate.FeatureMap getFeatures() {
    return  features;
  }

  /** The start node. */
  public gate.Node getStartNode() {
    return  start;
  }

  /** The end node. */
  public Node getEndNode() {
    return end;
  }

  /**The id of the annotation.*/
  public long getId() {
      return id;
  }

  private String type = null;
  private FeatureMap features = null;
  private Node start, end;
  private AnnotationStereotype stereotype = null;
  private long id;
} // class AnnotationImpl

