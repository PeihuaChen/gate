/*
 *  AnnotationImpl.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, Jan/00
 *
 *  $Id$
 */

package gate.annotation;

import java.util.*;

import gate.*;
import gate.util.*;
import gate.corpora.*;

/** Provides an implementation for the interface gate.Annotation
 *
 */
public class AnnotationImpl extends AbstractFeatureBearer
                            implements Annotation, FeatureBearer, Comparable {

  /** Debug flag
   */
  private static final boolean DEBUG = false;
  /** Freeze the serialization UID. */
  static final long serialVersionUID = -5658993256574857725L;

  /** Constructor. Package access - annotations have to be constructed via
   * AnnotationSets.
   *
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


  /** The ID of the annotation.
   */
  public Integer getId() {
    return id;
  } // getId()

  /** The type of the annotation (corresponds to TIPSTER "name").
   */
  public String getType() {
    return type;
  } // getType()

  /** The features, or content of this arc (corresponds to TIPSTER
   * "attributes", and to LDC "label", which is the simplest case).
   */
  public FeatureMap getFeatures() {
    return features;
  } // getFeatures()

  /** Set the feature set
   */
  public void setFeatures(FeatureMap features) { this.features = features; }

  /** The start node.
   */
  public Node getStartNode() {
    return start;
  } // getStartNode()

  /** The end node.
   */
  public Node getEndNode() {
    return end;
  } // getEndNode()

  /** String representation of hte annotation
   */
  public String toString() {
    return "AnnotationImpl: id=" + id + "; type=" + type +
           "; features=" + features + "; start=" + start +
           "; end=" + end + System.getProperty("line.separator");
  } // toString()

  /** Ordering
   */
  public int compareTo(Object o) throws ClassCastException {
    Annotation other = (Annotation) o;
    return id.compareTo(other.getId());
  } // compareTo

  /** When equals called on two annotations returns true, is REQUIRED that the
    * value hashCode for each annotation to be the same. It is not required
    * that when equals return false, the values to be different. For speed, it
    * would be beneficial to happen that way.
    */

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

  /** Returns true if two annotation are Equals.
   *  Two Annotation are equals if their offsets, types, id and features are the
   *  same.
   */
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

    // If their types are not equals then return false
    if((id == null) ^ (other.getId() == null))
      return false;
    if((id != null )&& (!id.equals(other.getId())))
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


  /** This verifies if <b>this</b> annotation is compatible with another one.
    * Compatible means that they hit the same possition and the FeatureMap of
    * <b>this</b> is incuded into aAnnot FeatureMap.
    * @param aAnnot a gate Annotation.
    * @return <code>true</code> if aAnnot is compatible with <b>this</> and
    * <code>false</code> otherwise.
    */
  public boolean isCompatible(Annotation aAnnot){
    if (aAnnot == null) return false;

    if (coextensive(aAnnot)){
      if (aAnnot.getFeatures() == null) return true;
      if (aAnnot.getFeatures().subsumes(this.getFeatures()))
        return true;
    }// End if
    return false;
  }//isCompatible

  /** This method verifies if two annotation and are partially compatible.
    * Partially compatible means that they overlap and the FeatureMap of
    * <b>this</b> is incuded into FeatureMap of aAnnot.
    * @param aAnnot a gate Annotation.
    * @return <code>true</code> if <b>this</b> is partially compatible with
    * aAnnot and <code>false</code> otherwise.
    */
  public boolean isPartiallyCompatible(Annotation aAnnot){
    if (aAnnot == null) return false;

    if (overlaps(aAnnot)){
      if (aAnnot.getFeatures() == null) return true;
      if (aAnnot.getFeatures().subsumes(this.getFeatures()))
        return true;
    }// End if
    return false;
  }//isPartiallyCompatible

  /**
    *  Two Annotation are coextensive if their offsets are the
    *  same.
    *  @param anAnnot A Gate annotation.
    *  @return <code>true</code> if two annotation hit the same possition and
    *  <code>false</code> otherwise
    */
  public boolean coextensive(Annotation anAnnot){
    // If their start offset is not the same then return false
    if((anAnnot.getStartNode() == null) ^ (this.getStartNode() == null))
      return false;

    if(anAnnot.getStartNode() != null){
      if((anAnnot.getStartNode().getOffset() == null) ^
         (this.getStartNode().getOffset() == null))
        return false;
      if(anAnnot.getStartNode().getOffset() != null &&
        (!anAnnot.getStartNode().getOffset().equals(
                            this.getStartNode().getOffset())))
        return false;
    }// End if

    // If their end offset is not the same then return false
    if((anAnnot.getEndNode() == null) ^ (this.getEndNode() == null))
      return false;

    if(anAnnot.getEndNode() != null){
      if((anAnnot.getEndNode().getOffset() == null) ^
         (this.getEndNode().getOffset() == null))
        return false;
      if(anAnnot.getEndNode().getOffset() != null &&
        (!anAnnot.getEndNode().getOffset().equals(
              this.getEndNode().getOffset())))
        return false;
    }// End if

    // If we are here, then the annotations hit the same position.
    return true;
  }//coextensive

  /** This method tells if <b>this</b> overlaps aAnnot.
    * @param aAnnot a gate Annotation.
    * @return <code>true</code> if they overlap and <code>false</code> false if
    * they don't.
    */
  public boolean overlaps(Annotation aAnnot){
    if (aAnnot == null) return false;
    if (aAnnot.getStartNode() == null ||
        aAnnot.getEndNode() == null ||
        aAnnot.getStartNode().getOffset() == null ||
        aAnnot.getEndNode().getOffset() == null) return false;

    if ( (aAnnot.getEndNode().getOffset().longValue() ==
          aAnnot.getStartNode().getOffset().longValue()) &&
          this.getStartNode().getOffset().longValue() <=
          aAnnot.getStartNode().getOffset().longValue() &&
          aAnnot.getEndNode().getOffset().longValue() <=
          this.getEndNode().getOffset().longValue()
       ) return true;


    if ( aAnnot.getEndNode().getOffset().longValue() <=
         this.getStartNode().getOffset().longValue())
      return false;

    if ( aAnnot.getStartNode().getOffset().longValue() >=
         this.getEndNode().getOffset().longValue())
      return false;

    return true;
  }//overlaps

  /**
   * The id of this annotation (for persitency resons)
   *
   */
  Integer id;
  /**
   * The type of the annotation
   *
   */
  String type;
  /**
   * The features of the annotation
   *
   */
  FeatureMap features;
  /**
   * The start node
   */
  Node start;

  /**
   *  The end node
   */
  Node end;

  /** @link dependency */
  /*#AnnotationImpl lnkAnnotationImpl;*/
} // class AnnotationImpl
