/*
 *  AnnotationDiff.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU, 27/Oct/2000
 *
 *  $Id$
 */

package gate.annotation;

import java.util.*;

import gate.util.*;
import gate.*;
import gate.creole.*;

/**
  * This class compare two annotation sets on annotation type given by the
  * AnnotationSchema object. It also deals with graphic representation.
  */
public class AnnotationDiff implements VisualResource{
  /** This document contains the key annotation set which is taken as reference
   *  in comparison*/
  private Document keyDocument = null;

  /** This document contains the response annotation set which is being
    * compared against the key annotation set.
    */
  private Document responseDocument = null;

  /** The annotation schema object used to get the annotation name
    */
  private AnnotationSchema annotationSchema = null;

  /** The Precision value (see NLP Information Extraction)*/
  private Double precision = null;
  /** The Recall value (see NLP Information Extraction)*/
  private Double recall = null;

  /** Sets the key Document containing the annotation set taken as refference */
  public void setKeyDocument(Document aKeyDocument) {
    keyDocument = aKeyDocument;
  }// setKeyDocument

  /** Sets the precision field*/
  public void setPrecision(Double aPrecision){
    precision = aPrecision;
  }// setPrecission

  /** Gets the precision field*/
  public Double getPrecision(){
    return precision;
  }// getPrecision

  /** Sets the Recall field*/
  public void setRecall(Double aRecall){
    recall = aRecall;
  }// setRecall
  /** Gets the recall*/
  public Double getRecall(){
    return recall;
  }// getRecall
  /**
    * Gets the keyDocument
    */
  public Document getKeyDocument(){
    return keyDocument;
  }// getKeyDocument
  /**
    * Sets the response Document(containing the annotation Set being compared)
    */
  public void setResponseDocument(Document aResponseDocument) {
    responseDocument = aResponseDocument;
  }//setResponseDocument

  /**
    * Sets the annotation type being compared. This type is found in annotation
    * Schema object as parameter.
    */
  public void setAnnotationSchema(AnnotationSchema anAnnotationSchema) {
    annotationSchema = anAnnotationSchema;
  } // setAnnotationType

  /** Returns the annotation schema object */
  public AnnotationSchema getAnnotationSchema(){
    return annotationSchema;
  }// AnnotationSchema

  /**
    * This method does the diff, P&R calculation and so on.
    */
  public Resource init() {
    // do the diff, P&R calculation and so on
    AnnotationSet keyAnnotSet = null;
    AnnotationSet responseAnnotSet = null;
    Set diffSet  = null;

    // Get the key AnnotationSet from the keyDocument
    keyAnnotSet = keyDocument.getAnnotations(
                              annotationSchema.getAnnotationName());
    // Get the response AnnotationSet from the resonseDocument
    responseAnnotSet = responseDocument.getAnnotations(
                                        annotationSchema.getAnnotationName());
    // Calculate the diff Set. This set will be used later at graphic
    // visualisation.
    diffSet = doDiff(keyAnnotSet, responseAnnotSet);

    return this;
  } // init()

  /** This method does the AnnotationSet diff and creates a set with
    * diffSetElement objects.
    */
  protected Set doDiff( AnnotationSet aKeyAnnotSet,
                        AnnotationSet aResponseAnnotSet){
    return null;
  }// doDiff

  public void setFeatures(FeatureMap aFeatureMap){
  }// setFeatures

  public FeatureMap getFeatures(){
    return null;
  }// getFeatures
} // class AnnotationDiff

class DiffSetElement {

  private Annotation leftAnnotation = null;
  private Annotation rightAnnotation = null;

  public DiffSetElement(){
  }// DiffSetElement

  public DiffSetElement( Annotation aLeftAnnotation,
                         Annotation aRightAnnotation){
    leftAnnotation = aLeftAnnotation;
    rightAnnotation = aRightAnnotation;
  }// DiffSetElement

  /** Sets the left annotation*/
  public void setLeftAnnotation(Annotation aLeftAnnotation){
    leftAnnotation = aLeftAnnotation;
  }// setLeftAnnot

  /** Gets the left annotation*/
  public Annotation getLeftAnnotation(){
    return leftAnnotation;
  }// getLeftAnnotation

  /** Sets the right annotation*/
  public void setRightAnnotation(Annotation aRightAnnotation){
    rightAnnotation = aRightAnnotation;
  }// setRightAnnot

  /** Gets the right annotation*/
  public Annotation getRightAnnotation(){
    return rightAnnotation;
  }// getRightAnnotation
}// classs DiffSetElement
