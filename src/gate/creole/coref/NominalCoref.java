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
  private static final String ORGANIZATION_CATEGORY = "Organization";
  private static final String LOOKUP_CATEGORY = "Lookup";
  private static final String ORGANIZATION_NOUN_CATEGORY = "organization_noun";
  

  //scope
  /** --- */
  //private static AnnotationOffsetComparator ANNOTATION_OFFSET_COMPARATOR;
  /** --- */
  private String annotationSetName;
  /** --- */
  private AnnotationSet defaultAnnotations;
  /** --- */
  private HashMap anaphor2antecedent;

    /*  static {
    ANNOTATION_OFFSET_COMPARATOR = new AnnotationOffsetComparator();
    }*/

  /** --- */
  public NominalCoref() {
    super("NOMINAL");
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

    HashMap anaphorToAntecedent = new HashMap();
    Object[] nominalArray;
      
    //0. preconditions
    if (null == this.document) {
      throw new ExecutionException("[coreference] Document is not set!");
    }

    //1. preprocess
    preprocess();

    Out.println("Total annotations: " + defaultAnnotations.size());
    

    // Get a sorted array of Tokens.
    Object[] tokens = defaultAnnotations.get(TOKEN_ANNOTATION_TYPE).toArray();
    java.util.Arrays.sort(tokens, new OffsetComparator());
    int currentToken = 0;

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

    FeatureMap orgNounConstraint = new SimpleFeatureMapImpl();
    orgNounConstraint.put(LOOKUP_MAJOR_TYPE_FEATURE_NAME,
                          ORGANIZATION_NOUN_CATEGORY);
    AnnotationSet orgNouns =
        this.defaultAnnotations.get(LOOKUP_CATEGORY, orgNounConstraint);

    HashSet orgConstraint = new HashSet();
    orgConstraint.add(ORGANIZATION_CATEGORY);

    AnnotationSet organizations =
        this.defaultAnnotations.get(orgConstraint);

    // combine them into a list of nominals
    Set nominals = new HashSet();
    if (people != null) {
        nominals.addAll(people);
    }
    if (jobTitles != null) {
        nominals.addAll(jobTitles);
    }
    if (orgNouns != null) {
        nominals.addAll(orgNouns);
    }
    if (organizations != null) {
        nominals.addAll(organizations);
    }

    Out.println("total nominals: " + nominals.size());

    // sort them according to offset
    nominalArray = nominals.toArray();
    java.util.Arrays.sort(nominalArray, new OffsetComparator());
    
    ArrayList previousPeople = new ArrayList();
    ArrayList previousOrgs = new ArrayList();
    
        
    // process all nominals
    for (int i=0; i<nominalArray.length; i++) {
        Annotation nominal = (Annotation)nominalArray[i];
        
        if (nominal.getType().equals(PERSON_CATEGORY)) {
            // Add each Person entity to the beginning of the people list
            previousPeople.add(0, nominal);
        }
        else if (nominal.getType().equals(JOBTITLE_CATEGORY)) {
            
            // Don't associate it if we have no people
            if (previousPeople.size() == 0) {
                Out.println("no people");
                continue;
            }

            // Don't associate it if it's part of a Person (eg President Bush)
            if (overlapsAnnotations(nominal, people)) {
                Out.println("overlapping annotation");
                continue;
            }
            
            // Don't associate it if it's proceeded by a generic marker
            Annotation previousToken =
                getPreviousAnnotation(nominal, tokens);
            String previousValue = (String) 
                previousToken.getFeatures().get(TOKEN_STRING_FEATURE_NAME);
            if (previousValue.equalsIgnoreCase("a") ||
                previousValue.equalsIgnoreCase("an") ||
                previousValue.equalsIgnoreCase("other") ||
                previousValue.equalsIgnoreCase("another")) {
                Out.println("indefinite");
                continue;
            }            

            // Look into the tokens to get some info about POS.
            Object[] jobTitleTokens =
                this.defaultAnnotations.get(TOKEN_ANNOTATION_TYPE,
                                         nominal.getStartNode().getOffset(),
                                         nominal.getEndNode().getOffset()).toArray();
            java.util.Arrays.sort(jobTitleTokens, new OffsetComparator());
            Annotation lastToken = (Annotation)
                jobTitleTokens[jobTitleTokens.length - 1];

            // Don't associate if the job title is not a singular noun
            if (! lastToken.getFeatures().get(TOKEN_CATEGORY_FEATURE_NAME)
                .equals("NN")) {
                Out.println("Not a singular noun");
                continue;
            }
            

            // Associate this entity with the most recent Person
            int personIndex = 0;
            
            Annotation previousPerson =
                (Annotation) previousPeople.get(personIndex);

            // Don't associate it if the previous person is a pronoun
            

            // Don't associate if the two nominals are note the same gender
            String personGender = (String) 
                previousPerson.getFeatures().get(PERSON_GENDER_FEATURE_NAME);
            String jobTitleGender = (String) 
                nominal.getFeatures().get(PERSON_GENDER_FEATURE_NAME);
            if (personGender != null && jobTitleGender != null) {
                if (! personGender.equals(jobTitleGender)) {
                    Out.println("wrong gender: " + personGender + " " +
                                jobTitleGender);
                    continue;
                }
            }

            
            anaphor2antecedent.put(nominal, previousPerson);
        }
        else if (nominal.getType().equals(ORGANIZATION_CATEGORY)) {
            // Add each Person entity to the beginning of the people list
            previousOrgs.add(0, nominal);
        }
        else if (nominal.getType().equals(LOOKUP_CATEGORY)) {
            // Associate this entity with the most recent Person
            if (previousOrgs.size() > 0) {
                anaphor2antecedent.put(nominal, previousOrgs.get(0));
            }
        }
    }

    generateCorefChains(anaphor2antecedent);
  }

  /**
   * This method specifies whether a given annotation overlaps any of a 
   * set of annotations. For instance, JobTitles occasionally are
   * part of Person annotations.
   * 
   */
  private boolean overlapsAnnotations(Annotation a,
                                      AnnotationSet annotations) {
      Iterator iter = annotations.iterator();
      while (iter.hasNext()) {
          Annotation current = (Annotation) iter.next();
          if (a.overlaps(current)) {
              return true;
          }
      }
      
      return false;
  }
                    
  /** 
   * This method returns the annotation before a given annotation.
   */
  private Annotation getPreviousAnnotation(Annotation a,
                                           Object[] annotations) {
      Annotation previous = null;
      Annotation current = null;

      for (int i=0;i<annotations.length;i++) {
          current = (Annotation) annotations[i];
          if (current.getStartNode().getOffset().longValue() >= 
              a.getStartNode().getOffset().longValue()) {
              return previous;
          }
          previous = current;
      }

      return null;
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

}
