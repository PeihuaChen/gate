/*
 *  PronominalCoref.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 30/Dec/2001
 *
 *  $Id$
 */

package gate.creole.coref;

import java.util.*;

import junit.framework.*;

import gate.*;
import gate.creole.*;
import gate.util.*;

public class PronominalCoref extends AbstractProcessingResource
                              implements ProcessingResource{


  //annotation types
  private static final String PERSON_TYPE = "Person";
  private static final String ORG_TYPE = "Organization";
  private static final String LOC_TYPE = "Location";
  private static final String TOKEN_TYPE = "Token";
  private static final String SENTENCE_TYPE = "Sentence";

  //annotation features
  private static final String PRP_CATEGORY = "PRP";
  private static final String PRPS_CATEGORY = "PRP$";
  private static final String TOKEN_CATEGORY = "category";
  private static final String TOKEN_STRING = "string";
  private static final String PERSON_GENDER = "gender";

  //scope
  private static final int SENTENCES_IN_SCOPE = 3;

//  private static final SentenceComparator SENTENCE_COMPARATOR;
  private static final AnnotationComparator ANNOTATION_COMPARATOR;

  private Document  doc;
  private AnnotationSet defaultAnnotations;
  private Sentence[] textSentences;

  static {
//    SENTENCE_COMPARATOR = new SentenceComparator();
    ANNOTATION_COMPARATOR = new AnnotationComparator();
  }

  public PronominalCoref() {
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
    init();
  } // reInit()

  /** Get the document we're running on. */
  public Document getDocument() {
    return this.doc;
  }

  /** Set the document to run on. */
  public void setDocument(Document newDocument) {
    Assert.assertNotNull(newDocument);
    this.doc = newDocument;
  }

  /**
   * This method runs the coreferencer. It assumes that all the needed parameters
   * are set. If they are not, an exception will be fired.
   */
  public void execute() throws ExecutionException{

    if(null == this.doc) {
      throw new ExecutionException("[coreference] Document is not set!");
    }

    preprocess();

    FeatureMap constraint = new SimpleFeatureMapImpl();
    constraint.put("category",PRP_CATEGORY);
    AnnotationSet personalPronouns = this.defaultAnnotations.get(TOKEN_TYPE,constraint);
    //sort them according to offset
    Object[] arrPersonalPronouns = personalPronouns.toArray();
    java.util.Arrays.sort(arrPersonalPronouns,ANNOTATION_COMPARATOR);

    int prnSentIndex = 0;

    for (int i=0; i< arrPersonalPronouns.length; i++) {
      Annotation currPronoun = (Annotation)arrPersonalPronouns[i];
      while (this.textSentences[prnSentIndex].getEndOffset().longValue() <
                                      currPronoun.getStartNode().getOffset().longValue()) {
        prnSentIndex++;
      }

      Sentence currSentence = this.textSentences[prnSentIndex];
      Assert.assertTrue(currSentence.getStartOffset().longValue() <= currPronoun.getStartNode().getOffset().longValue());
      Assert.assertTrue(currSentence.getEndOffset().longValue() >= currPronoun.getEndNode().getOffset().longValue());

      Annotation antc = findAntecedent(currPronoun,prnSentIndex);
    }

  }


  private Annotation findAntecedent(Annotation currPronoun,int prnSentIndex) {

    //0. preconditions
    Assert.assertNotNull(currPronoun);
    Assert.assertTrue(prnSentIndex >= 0);
    Assert.assertTrue(currPronoun.getType().equals(TOKEN_TYPE));
    Assert.assertTrue(currPronoun.getFeatures().get(TOKEN_CATEGORY).equals(PRP_CATEGORY) ||
                      currPronoun.getFeatures().get(TOKEN_CATEGORY).equals(PRPS_CATEGORY));

    String strPronoun = (String)currPronoun.getFeatures().get(TOKEN_STRING);

    if (strPronoun.equalsIgnoreCase("HE") ||
        strPronoun.equalsIgnoreCase("HIS")) {
      return _resolve$HE$HIM$HIS$(currPronoun,prnSentIndex);
    }
    else if (strPronoun.equalsIgnoreCase("SHE") ||
              strPronoun.equalsIgnoreCase("HER")) {
      return this._resolve$SHE$HER$(currPronoun,prnSentIndex);
    }
    else {
//      throw new MethodNotImplementedException();
      gate.util.Err.println("["+strPronoun+"] is not handled yet...");
      return null;
    }
  }


  private Annotation _resolve$HE$HIM$HIS$(Annotation pronoun, int sentenceIndex) {

    //0. preconditions
    Assert.assertTrue(pronoun.getType().equals(TOKEN_TYPE));
    Assert.assertTrue(pronoun.getFeatures().get(TOKEN_CATEGORY).equals(PRP_CATEGORY));
    String pronounString = (String)pronoun.getFeatures().get(TOKEN_STRING);
    Assert.assertTrue(pronounString.equalsIgnoreCase("HE") ||
                      pronounString.equalsIgnoreCase("HIM") ||
                      pronounString.equalsIgnoreCase("HIS"));

    //1.
    boolean antecedentFound = false;
    int scopeFirstIndex = sentenceIndex - SENTENCES_IN_SCOPE;
    int currSentenceIndex = sentenceIndex;
    Annotation bestAntecedent = null;

    while (currSentenceIndex >= scopeFirstIndex || antecedentFound == false) {
      Sentence currSentence = this.textSentences[currSentenceIndex];
      AnnotationSet persons = currSentence.getPersons();

      Iterator it = persons.iterator();
      while (it.hasNext()) {
        Annotation currPerson = (Annotation)it.next();
        String gender = (String)currPerson.getFeatures().get(PERSON_GENDER);

        if (null != gender && gender.equalsIgnoreCase("MALE")) {
          //hit
          antecedentFound = true;

          if (null == bestAntecedent ||
              currPerson.getStartNode().getOffset().longValue() >
                                  bestAntecedent.getStartNode().getOffset().longValue()) {
            bestAntecedent = currPerson;
          }
        }
      }
    }
gate.util.Err.println("found antecedent for ["+pronounString+"] : " + bestAntecedent);
    return bestAntecedent;
  }


  private Annotation _resolve$SHE$HER$(Annotation pronoun, int sentenceIndex) {

    //0. preconditions
    Assert.assertTrue(pronoun.getType().equals(TOKEN_TYPE));
    Assert.assertTrue(pronoun.getFeatures().get(TOKEN_CATEGORY).equals(PRP_CATEGORY));
    String pronounString = (String)pronoun.getFeatures().get(TOKEN_STRING);
    Assert.assertTrue(pronounString.equalsIgnoreCase("SHE") ||
                      pronounString.equalsIgnoreCase("HER"));

    //1.
    boolean antecedentFound = false;
    int scopeFirstIndex = sentenceIndex - SENTENCES_IN_SCOPE;
    int currSentenceIndex = sentenceIndex;
    Annotation bestAntecedent = null;

    while (currSentenceIndex >= scopeFirstIndex || antecedentFound == false) {
      Sentence currSentence = this.textSentences[currSentenceIndex];
      AnnotationSet persons = currSentence.getPersons();

      Iterator it = persons.iterator();
      while (it.hasNext()) {
        Annotation currPerson = (Annotation)it.next();
        String gender = (String)currPerson.getFeatures().get(PERSON_GENDER);

        if (null != gender && gender.equalsIgnoreCase("FEMALE")) {
          //hit
          antecedentFound = true;

          if (null == bestAntecedent ||
              currPerson.getStartNode().getOffset().longValue() >
                                  bestAntecedent.getStartNode().getOffset().longValue()) {
            bestAntecedent = currPerson;
          }
        }
      }
    }

gate.util.Err.println("found antecedent for ["+pronounString+"] : " + bestAntecedent);
    return bestAntecedent;
  }

  private Annotation _resolve$IT$ITS$(String pronoun) {
    throw new MethodNotImplementedException();
  }

  private void preprocess() {

    //get all annotation in the default set
    this.defaultAnnotations = this.doc.getAnnotations();

    //get all SENTENCE annotations
    AnnotationSet sentenceAnnotations = this.defaultAnnotations.get(SENTENCE_TYPE);

    this.textSentences = new Sentence[sentenceAnnotations.size()];
    Object[]  sentenceArray = sentenceAnnotations.toArray();

    java.util.Arrays.sort(sentenceArray,ANNOTATION_COMPARATOR);

    for (int i=0; i< sentenceArray.length; i++) {

      Annotation currSentence = (Annotation)sentenceArray[i];
      Long sentStartOffset = currSentence.getStartNode().getOffset();
      Long sentEndOffset = currSentence.getEndNode().getOffset();

      //get PERSOSNS in this sentence
      AnnotationSet sentPersons = this.defaultAnnotations.get(this.PERSON_TYPE,
                                                              sentStartOffset,
                                                              sentEndOffset);

      //get ORGANIZATIONS in this sentence
      AnnotationSet sentOrgs = this.defaultAnnotations.get(this.ORG_TYPE,
                                                              sentStartOffset,
                                                              sentEndOffset);

      //get LOCATION in this sentence
      AnnotationSet sentLocs = this.defaultAnnotations.get(this.LOC_TYPE,
                                                              sentStartOffset,
                                                              sentEndOffset);

      this.textSentences[i] = new Sentence(i,
                                            0,
                                            sentStartOffset,
                                            sentEndOffset,
                                            sentPersons,
                                            sentOrgs,
                                            sentLocs
                                  );

    }
  }

//  private int findSentenceNumber(Annotation ann) {
//    int index = Arrays.binarySearch(this.textSentences,ann.getStartNode().getOffset(),SENTENCE_COMPARATOR);
//  }

  private static class AnnotationComparator implements Comparator {

    public int compare(Object o1,Object o2) {

      Assert.assertNotNull(o1);
      Assert.assertNotNull(o2);
      Assert.assertTrue(o1 instanceof Annotation);
      Assert.assertTrue(o2 instanceof Annotation);

      Annotation a1 = (Annotation)o1;
      Annotation a2 = (Annotation)o2;

      return (a1.getEndNode().getOffset().intValue() - a2.getEndNode().getOffset().intValue());
    }
  }


/*  private static class SentenceComparator implements Comparator {

    public int compare(Object o1,Object o2) {

      Assert.assertNotNull(o1);
      Assert.assertNotNull(o2);
      Assert.assertTrue(o1 instanceof Sentence);
      Assert.assertTrue(o2 instanceof Long);

      Sentence s = (Sentence)o1;
      Long offset = (Long)o2;
      return s.getStartOffset().longValue() - offset.intValue();
    }
  }
*/

  private class Sentence {

    private int sentNumber;
    private int paraNumber;
    private Long startOffset;
    private Long endOffset;

    private AnnotationSet persons;
    private AnnotationSet organizations;
    private AnnotationSet locations;


    public Sentence(int sentNumber,
                    int paraNumber,
                    Long startOffset,
                    Long endOffset,
                    AnnotationSet persons,
                    AnnotationSet organizations,
                    AnnotationSet locations) {

      this.sentNumber = sentNumber;
      this.paraNumber = paraNumber;
      this.startOffset = startOffset;
      this.endOffset = endOffset;
      this.persons = persons;
      this.organizations = organizations;
      this.locations = locations;
    }

    public void addEntity(Annotation entAnnotation,String entType) {
    }

    public Long getStartOffset() {
      return this.startOffset;
    }

    public Long getEndOffset() {
      return this.endOffset;
    }

    public AnnotationSet getPersons() {
      return this.persons;
    }

    public AnnotationSet getOrganizations() {
      return this.organizations;
    }

    public AnnotationSet getLocations() {
      return this.locations;
    }
  }


}