/*
 *  AnnotationImpl.java
 *
 *  Copyright (c) 2000-2001, The University of Sheffield.
 *  
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June1991.
 *  
 *  A copy of this licence is included in the distribution in the file
 *  licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *  
 *	Valentin Tablan, Jan/00
 *
 *	$Id$
 */

package gate.annotation;

import gate.*;
import gate.util.*;

/** Provides an implementation for the interface gate.Annotation
  */
public class AnnotationImpl
  implements Annotation, FeatureBearer, Comparable {

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Constructor. Package access - annotations have to be constructed via
    * AnnotationSets.
    * @param id The id of the new annotation;
    * @param start The node from where the annotation will depart;
    * @param end The node where trhe annotation ends;
    * @param type The type of the new annotation;
    * @param features The features of the annotation.
    */
  AnnotationImpl(
    Integer id, Node start, Node end, String type, FeatureMap features
  ) {
    this.id       = id;
    this.start    = start;
    this.end      = end;
    this.type     = type;
    this.features = features;
  } // AnnotationImpl


  /** The ID of the annotation. */
  public Integer getId() {
    return id;
  } // getId()

  /** The type of the annotation (corresponds to TIPSTER "name"). */
  public String getType() {
    return type;
  } // getType()

  /** The features, or content of this arc (corresponds to TIPSTER
    * "attributes", and to LDC "label", which is the simplest case).
    */
  public FeatureMap getFeatures() {
    return features;
  } // getFeatures()

  /** Set the feature set */
  public void setFeatures(FeatureMap features) { this.features = features; }

  /** The start node. */
  public Node getStartNode() {
    return start;
  } // getStartNode()

  /** The end node. */
  public Node getEndNode() {
    return end;
  } // getEndNode()

  /** String representation of hte annotation */
  public String toString() {
    return "AnnotationImpl: id=" + id + "; type=" + type +
           "; features=" + features + "; start=" + start +
           "; end=" + end + System.getProperty("line.separator");
  } // toString()

  /** Ordering */
  public int compareTo(Object o) throws ClassCastException {
    Annotation other = (Annotation) o;
    return id.compareTo(other.getId());
  } // compareTo

  Integer id;
  String type;
  FeatureMap features;
  Node start, end;

} // class AnnotationImpl
