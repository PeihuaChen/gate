/*
 *	AnnotationDiff.java
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
  private Double Precision = null;
  /** The Recall value (see NLP Information Extraction)*/
  private Double Recall = null;

  /** Sets the key Document containing the annotation set taken as refference */
  public void setKeyDocument(Document aKeyDocument) {
    keyDocument = aKeyDocument;
  }// setKeyDocument

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

  /** Returns the annotation schema object
   */
  public AnnotationSchema getAnnotationSchema(){
    return annotationSchema;
  }// AnnotationSchema
  /**
    * This method does the diff, P&R calculation and so on.
    */
  public Resource init() {
    // do the diff, P&R calculation and so on
    return this;
  } // init()

  public void setFeatures(FeatureMap aFeatureMap){
  }// setFeatures

  public FeatureMap getFeatures(){
    return null;
  }// getFeatures
} // class AnnotationDiff