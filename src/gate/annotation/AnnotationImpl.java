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

import java.util.*;

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

  /** When equals called on two annotations returns true, is REQUIRED that the
    * value hashCode for each annotation to be the same. It is not required
    * that when equals return false, the values to be different. For speed, it
    * would be beneficial to happen that way.
    */
/*
  public int hashCode(){
    int hashCodeRes = 0;
    if (start != null && start.getOffset() != null)
       hashCodeRes ^= start.getOffset().hashCode();
    if (end != null && end.getOffset() != null)
      hashCodeRes ^= end.getOffset().hashCode();
    if(features != null)
      hashCodeRes ^= features.hashCode();
    return  hashCodeRes;
  }// hashCode
*/
  /** Returns true if two annotation are Equals.
   *  Two Annotation are equals if their offsets, types and features are the
   *  same.
   */
/*
  public boolean equals(Object obj){
    if(obj == null)
      return false;
    Annotation other;
    if(obj instanceof AnnotationImpl){
      other = (Annotation) obj;
    }else return false;

    // If their types are not equals then return false
    if((type == null) ^ (other.getType() == null))
      return false;
    if(type != null && (!type.equals(other.getType())))
      return false;

  // If their start offset is not the same then return false
    if((start == null) ^ (other.getStartNode() == null))
      return false;
    if(start != null){
      if((start.getOffset() == null) ^
         (other.getStartNode().getOffset() == null))
        return false;
      if(start.getOffset() != null &&
        (!start.getOffset().equals(other.getStartNode().getOffset())))
        return false;
    }

  // If their end offset is not the same then return false
    if((end == null) ^ (other.getEndNode() == null))
      return false;
    if(end != null){
      if((end.getOffset() == null) ^
         (other.getEndNode().getOffset() == null))
        return false;
      if(end.getOffset() != null &&
        (!end.getOffset().equals(other.getEndNode().getOffset())))
        return false;
    }

    // If their featureMaps are not equals then return false
    if((features == null) ^ (other.getFeatures() == null))
      return false;
    if(features != null && (!features.equals(other.getFeatures())))
      return false;
    return true;
  }// equals
*/
  /** This method compares two FeatureMaps
    * Returns true if their content is
    */
  private boolean compareFeatureMaps(FeatureMap oneFm, FeatureMap anotherFm){
    // If the two sets don't have the same size return false
    if (oneFm.size() != anotherFm.size())
      return false;
    // If they have the same size compare their elements
    Set oneFmKeySet = oneFm.keySet();
    Iterator oneFmKeySetIterator = oneFmKeySet.iterator();
    while (oneFmKeySetIterator.hasNext()){
      Object oneFmKey = oneFmKeySetIterator.next();
      if (!anotherFm.containsKey(oneFmKey))
        return false;
      if (!anotherFm.containsValue(oneFm.get(oneFmKey)))
        return false;
    }// end while
    return true;
  }// compareFeatureMaps

  Integer id;
  String type;
  FeatureMap features;
  Node start, end;

} // class AnnotationImpl
