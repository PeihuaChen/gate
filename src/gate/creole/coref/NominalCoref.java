/*
 *  NominalCoref.java
 *
 *  Copyright (c) 1998-2002, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  $Id$
 */

package gate.creole.coref;

import java.util.*;
import java.net.*;

import junit.framework.*;

import gate.*;
import gate.creole.*;
import gate.util.*;
import gate.annotation.*;

public class NominalCoref extends AbstractCoreferencer
    implements ProcessingResource, ANNIEConstants {

  public static final String COREF_DOCUMENT_PARAMETER_NAME = "document";

  public static final String COREF_ANN_SET_PARAMETER_NAME = "annotationSetName";

  /** --- */
  private static final boolean DEBUG = false;

  //annotation features
  private static final String PERSON_CATEGORY = "Person";
  private static final String JOBTITLE_CATEGORY = "JobTitle";

  //scope
  /** --- */
  private static AnnotationOffsetComparator ANNOTATION_OFFSET_COMPARATOR;
  /** --- */
  private String annotationSetName;
  /** --- */
  private AnnotationSet defaultAnnotations;
  /** --- */
  private HashMap anaphor2antecedent;

  static {
    ANNOTATION_OFFSET_COMPARATOR = new AnnotationOffsetComparator();
  }

  /** --- */
  public NominalCoref() {
    super("NOUN");
    this.anaphor2antecedent = new HashMap();
  }

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {
    return super.init();
  } // init()

  /**
   * Reinitialises the processing resource. After calling this method the
   * resource should be in the state it is after calling init.
   * If the resource depends on external resources (such as rules files) then
   * the resource will re-read those resources. If the data used to create
   * the resource has changed since the resource has been created then the
   * resource will change too after calling reInit().
  */
  public void reInit() throws ResourceInstantiationException {
    this.anaphor2antecedent = new HashMap();
    init();
  } // reInit()


  /** Set the document to run on. */
  public void setDocument(Document newDocument) {

    //0. precondition
//    Assert.assertNotNull(newDocument);

    super.setDocument(newDocument);
  }

  /** --- */
  public void setAnnotationSetName(String annotationSetName) {
    this.annotationSetName = annotationSetName;
  }

  /** --- */
  public String getAnnotationSetName() {
    return annotationSetName;
  }

  /**
   * This method runs the coreferencer. It assumes that all the needed parameters
   * are set. If they are not, an exception will be fired.
   */
  public void execute() throws ExecutionException{
      
    //0. preconditions
    if (null == this.document) {
      throw new ExecutionException("[coreference] Document is not set!");
    }

    //1. preprocess
    preprocess();

    // get Person entities
    //FeatureMap personConstraint = new SimpleFeatureMapImpl();
    //personConstraint.put(LOOKUP_MAJOR_TYPE_FEATURE_NAME,
    //                          PERSON_CATEGORY);
    HashSet personConstraint = new HashSet();
    personConstraint.add(PERSON_CATEGORY);
    AnnotationSet people =
        this.defaultAnnotations.get(personConstraint);

    // get all JobTitle entities
    //FeatureMap constraintJobTitle = new SimpleFeatureMapImpl();
    //constraintJobTitle.put(LOOKUP_MAJOR_TYPE_FEATURE_NAME, JOBTITLE_CATEGORY);
    HashSet jobTitleConstraint = new HashSet();
    jobTitleConstraint.add(JOBTITLE_CATEGORY);
    
    AnnotationSet jobTitles = 
        this.defaultAnnotations.get(jobTitleConstraint);

    // combine them into a list of nominals
    AnnotationSet nominals = people;
    if (null == nominals) {
      nominals = jobTitles;
    }
    else if (null != people) {
      nominals.addAll(jobTitles);
    }

    //6.do we have any nominals at all?
    if (null == nominals) {
      //do nothing
      return;
    }

    //7.sort them according to offset
    Object[] nominalArray = nominals.toArray();
    java.util.Arrays.sort(nominalArray,ANNOTATION_OFFSET_COMPARATOR);

    //8.cleanup - ease the GC
    nominals = jobTitles = people = null;

    HashMap anaphorToAntecedent = new HashMap();
    Annotation lastPerson = null;

    //10. process all nominals
    for (int i=0; i<nominalArray.length; i++) {
      Annotation nominal = (Annotation)nominalArray[i];

      if (nominal.getType().equals(PERSON_CATEGORY)) {
          // Add each Person entity to the beginning of the people list
          lastPerson = nominal;
      }
      else if (nominal.getType().equals(JOBTITLE_CATEGORY)) {
          // Associate this entity with the most recent Person
          if (lastPerson != null) {
              anaphor2antecedent.put(nominal, lastPerson);
          }
      }
    }

    generateCorefChains(anaphor2antecedent);
  }


  /** --- */
  public HashMap getResolvedAnaphora() {
    return this.anaphor2antecedent;
  }

  /** --- */
  private void preprocess() throws ExecutionException {

    //0.5 cleanup
    this.anaphor2antecedent.clear();

    //1.get all annotation in the input set
    if ( this.annotationSetName == null || this.annotationSetName.equals("")) {
      this.defaultAnnotations = this.document.getAnnotations();
    }
    else {
      this.defaultAnnotations = this.document.getAnnotations(annotationSetName);
    }

    //if none found, print warning and exit
    if (this.defaultAnnotations == null || this.defaultAnnotations.isEmpty()) {
      Err.prln("Coref Warning: No annotations found for processing!");
      return;
    }

    /*
    //5. initialise the quoted text fragments
    AnnotationSet sentQuotes = this.defaultAnnotations.get(QUOTED_TEXT_TYPE);

    //if none then return
    if (null == sentQuotes) {
      this.quotedText = new Quote[0];
    }
    else {
      this.quotedText = new Quote[sentQuotes.size()];

      Object[] quotesArray = sentQuotes.toArray();
      java.util.Arrays.sort(quotesArray,ANNOTATION_OFFSET_COMPARATOR);

      for (int i =0; i < quotesArray.length; i++) {
        this.quotedText[i] = new Quote((Annotation)quotesArray[i],i);
      }
    }
    */
  }

  /** --- */
  private static class AnnotationOffsetComparator implements Comparator {

    private int _getOffset(Object o) {

      if (o instanceof Annotation) {
        return ((Annotation)o).getEndNode().getOffset().intValue();
      }
      else if (o instanceof Node) {
        return ((Node)o).getOffset().intValue();
      }
      else {
        throw new IllegalArgumentException();
      }
    }

    public int compare(Object o1,Object o2) {

      //0. preconditions
      Assert.assertNotNull(o1);
      Assert.assertNotNull(o2);
      Assert.assertTrue(o1 instanceof Annotation ||
                        o1 instanceof Node);
      Assert.assertTrue(o2 instanceof Annotation ||
                        o2 instanceof Node);

      int offset1 = _getOffset(o1);
      int offset2 = _getOffset(o2);

      return offset1 - offset2;
    }
  }
}
