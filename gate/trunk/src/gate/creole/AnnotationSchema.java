/*
 *  AnnotationSchema.java
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
 *  Cristian URSU, 27/Sept/2000
 *
 *  $Id$
 */


package gate.creole;

import java.util.*;

import gate.util.*;


/** This class handles all possible annotations together with their attributes,
  *  values and types
  */
public class AnnotationSchema {
  /**
    *  This field is "final static" because it brings in
    *  the advantage of dead code elimination
    *  When DEBUG is set on false the code that it guardes will be eliminated
    *  by the compiler. This will spead up the progam a little bit.
    */
  private static final boolean DEBUG = false;

  /** The name of the annotation */
  String annotationName = null;

  /** Schemas for the attributes */
  Set featureSchemaSet = null;


  /** A store of all AnnotationSchema (all AnnotationSchema must register
    * here on construction or loading)
    */
  static Map annotationSchemaMap = null;

  /**
    * Constructs an annotation schema given it's name.
    * Feature schema that it might contain is set on null
    */
  private AnnotationSchema(String anAnnotationName){
    this(anAnnotationName,null);
  }//AnnotationSchema

  /**
    * Constructs an AnnotationSchema object given it's name and a set of
    * FeatureSchema
    */
  private AnnotationSchema(String anAnnotationName,Set aFeatureSchemaSet){
    annotationName   = anAnnotationName;
    featureSchemaSet = aFeatureSchemaSet;
  }//AnnotationSchema


  /** Constructs and adds a new AnnotationSchema to the annotationSchemaMap
    * given the anAnnotationName.
    */
  static public void addSchema(String anAnnotationName){
    AnnotationSchema annotSchema
                            = new AnnotationSchema(anAnnotationName);
    if (annotationSchemaMap == null)
                    annotationSchemaMap = new HashMap();
    annotationSchemaMap.put(anAnnotationName, annotSchema);
  }//addSchema


  /** Constructs and adds a new AnnotationSchema to the annotationSchemaMap
    * given the anAnnotationName and a set of FeatureSchema
    */
  static public void addSchema(String anAnnotationName,Set aFeatureSchemaSet){
    AnnotationSchema annotSchema
                     = new AnnotationSchema(anAnnotationName,aFeatureSchemaSet);
    if (annotationSchemaMap == null)
                            annotationSchemaMap = new HashMap();
    annotationSchemaMap.put(anAnnotationName, annotSchema);
  }//addSchema


  /** Returns a read only map with all the AnnotationSchema objects */
  static public Map getSchemas() {
    return Collections.unmodifiableMap(annotationSchemaMap);
  }//getSchemas

  /**
   * Returns the value of annotationName field
   */
  public String getAnnotationName(){
    return annotationName;
  }//getAnnotationName

  /** Returns the set of FeatureSchema*/
  public Set getFeatureSchemas(){
    return featureSchemaSet;
  }//getAttributeSchemas

  /** Returns a FeatureSchema object from featureSchemaSet, given a
    * feature name.
    * It will return null if the feature name is not found.
    */
  public FeatureSchema getFeatureSchema(String featureName) {
    Iterator fsIterator = featureSchemaSet.iterator();
    while (fsIterator.hasNext()) {
      FeatureSchema fs = (FeatureSchema) fsIterator.next();
      if (fs.getFeatureName().equals(featureName) )
        return fs;
    }
    return null;
  }//getFeatureSchema

  /** Find a Schema by annotation name */
  static public AnnotationSchema getSchema(String anAnnotationName) {
    return (AnnotationSchema) annotationSchemaMap.get(anAnnotationName);
  }//getSchema(String)

}//AnnotationSchema
