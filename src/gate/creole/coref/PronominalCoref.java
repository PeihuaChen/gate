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
import java.net.*;

import junit.framework.*;

import gate.*;
import gate.creole.*;
import gate.util.*;
import gate.annotation.*;

public class PronominalCoref extends AbstractLanguageAnalyser
                              implements ProcessingResource{

  //JAPE grammars
  private static final String QT_GRAMMAR_URL = "gate://gate/creole/coref/quoted.jape";

  //annotation types
  private static final String PERSON_TYPE = "Person";
  private static final String ORG_TYPE = "Organization";
  private static final String LOC_TYPE = "Location";
  private static final String TOKEN_TYPE = "Token";
  private static final String SENTENCE_TYPE = "Sentence";
  private static final String QUOTED_TEXT_TYPE = "Quoted Text";

  //annotation features
  private static final String PRP_CATEGORY = "PRP";
  private static final String PRP$_CATEGORY = "PRP$";
  private static final String TOKEN_CATEGORY = "category";
  private static final String TOKEN_STRING = "string";
  private static final String PERSON_GENDER = "gender";
  private static final String PERSON_ORTHO_COREF = "matches";

  //scope
  private static final int SENTENCES_IN_SCOPE = 3;

  private static AnnotationOffsetComparator ANNOTATION_OFFSET_COMPARATOR;

  private AnnotationSet defaultAnnotations;
  private Sentence[] textSentences;
  private Quote[] quotedText;
  private HashMap personGender;
  private Transducer qtTransducer;

  static {
    ANNOTATION_OFFSET_COMPARATOR = new AnnotationOffsetComparator();
  }

  public PronominalCoref() {

    this.personGender = new HashMap();
    this.qtTransducer = new gate.creole.Transducer();
  }

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {

    //0. preconditions
    Assert.assertNotNull(this.qtTransducer);

    //1. initialise quoted text transducer
    URL qtGrammarURL = null;
    try {
      qtGrammarURL = new URL(QT_GRAMMAR_URL);
    }
    catch(MalformedURLException mue) {
      throw new ResourceInstantiationException(mue);
    }
    this.qtTransducer.setGrammarURL(qtGrammarURL);
    this.qtTransducer.setEncoding("UTF-8");
    this.qtTransducer.init();

    //2. delegate
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

    if (null != this.qtTransducer) {
      this.qtTransducer.reInit();
    }

    init();
  } // reInit()

  /** Set the document to run on. */
  public void setDocument(Document newDocument) {

    //0. precondition
    Assert.assertNotNull(newDocument);

    //1. set doc for aggregated components
    this.qtTransducer.setDocument(newDocument);

    //2. delegate
    super.setDocument(newDocument);
  }

  /**
   * This method runs the coreferencer. It assumes that all the needed parameters
   * are set. If they are not, an exception will be fired.
   */
  public void execute() throws ExecutionException{

    //0. preconditions
    if(null == this.document) {
      throw new ExecutionException("[coreference] Document is not set!");
    }

    //1. preprocess
    preprocess();

    //2. remove corefs from previous run
    AnnotationSet corefSet = this.document.getAnnotations("COREF");
    if (false == corefSet.isEmpty()) {
      corefSet.clear();
    }

    //3.get personal pronouns
    FeatureMap constraintPRP = new SimpleFeatureMapImpl();
    constraintPRP.put(TOKEN_CATEGORY,PRP_CATEGORY);
    AnnotationSet personalPronouns = this.defaultAnnotations.get(TOKEN_TYPE,constraintPRP);

    //4.get possesive pronouns
    FeatureMap constraintPRP$ = new SimpleFeatureMapImpl();
    constraintPRP$.put(TOKEN_CATEGORY,PRP$_CATEGORY);
    AnnotationSet possesivePronouns = this.defaultAnnotations.get(TOKEN_TYPE,constraintPRP$);

    //5.combine them
    AnnotationSet pronouns = personalPronouns;
    if (null == personalPronouns) {
      pronouns = possesivePronouns;
    }
    else if (null != possesivePronouns) {
      pronouns.addAll(possesivePronouns);
    }

    //6.do we have pronouns at all?
    if (null == pronouns) {
      //do nothing
      return;
    }

    //7.sort them according to offset
    Object[] arrPronouns = pronouns.toArray();
    java.util.Arrays.sort(arrPronouns,ANNOTATION_OFFSET_COMPARATOR);

    //8.cleanup - ease the GC
    pronouns = personalPronouns = possesivePronouns = null;

    int prnSentIndex = 0;


    //10. process all pronouns
    for (int i=0; i< arrPronouns.length; i++) {
      Annotation currPronoun = (Annotation)arrPronouns[i];
      while (this.textSentences[prnSentIndex].getEndOffset().longValue() <
                                      currPronoun.getStartNode().getOffset().longValue()) {
        prnSentIndex++;
      }

      Sentence currSentence = this.textSentences[prnSentIndex];
      Assert.assertTrue(currSentence.getStartOffset().longValue() <= currPronoun.getStartNode().getOffset().longValue());
      Assert.assertTrue(currSentence.getEndOffset().longValue() >= currPronoun.getEndNode().getOffset().longValue());

      //11. find antecedent (if any) for pronoun
      Annotation antc = findAntecedent(currPronoun,prnSentIndex);

      //12.add to COREF annotation set
      corefSet = this.document.getAnnotations("COREF");
      Long antOffset = new Long(0);

      if (null!= antc) {
        antOffset = antc.getStartNode().getOffset();
      }

      //13. create coref annotation
      FeatureMap features = new SimpleFeatureMapImpl();
      features.put("antecedent",antOffset);
      corefSet.add(currPronoun.getStartNode(),currPronoun.getEndNode(),"COREF",features);
    }

    //done
  }

  private Annotation findAntecedent(Annotation currPronoun,int prnSentIndex) {

    //0. preconditions
    Assert.assertNotNull(currPronoun);
    Assert.assertTrue(prnSentIndex >= 0);
    Assert.assertTrue(currPronoun.getType().equals(TOKEN_TYPE));
    Assert.assertTrue(currPronoun.getFeatures().get(TOKEN_CATEGORY).equals(PRP_CATEGORY) ||
                      currPronoun.getFeatures().get(TOKEN_CATEGORY).equals(PRP$_CATEGORY));

    //1.
    String strPronoun = (String)currPronoun.getFeatures().get(TOKEN_STRING);

    Assert.assertNotNull(strPronoun);

    //2. delegate processing to the appropriate methods
    if (strPronoun.equalsIgnoreCase("HE") ||
        strPronoun.equalsIgnoreCase("HIM") ||
        strPronoun.equalsIgnoreCase("HIS")) {
      return _resolve$HE$HIM$HIS$(currPronoun,prnSentIndex);
    }
    else if (strPronoun.equalsIgnoreCase("SHE") ||
              strPronoun.equalsIgnoreCase("HER")) {
      return _resolve$SHE$HER$(currPronoun,prnSentIndex);
    }
    else if (strPronoun.equalsIgnoreCase("IT") ||
              strPronoun.equalsIgnoreCase("ITS")) {
      return _resolve$IT$ITS$(currPronoun,prnSentIndex);
    }
    else if (strPronoun.equalsIgnoreCase("I") ||
              strPronoun.equalsIgnoreCase("ME") ||
              strPronoun.equalsIgnoreCase("MY")) {
      return _resolve$I$ME$MY$(currPronoun,prnSentIndex);
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
    Assert.assertTrue(pronoun.getFeatures().get(TOKEN_CATEGORY).equals(PRP_CATEGORY) ||
                      pronoun.getFeatures().get(TOKEN_CATEGORY).equals(PRP$_CATEGORY));
    String pronounString = (String)pronoun.getFeatures().get(TOKEN_STRING);
    Assert.assertTrue(pronounString.equalsIgnoreCase("HE") ||
                      pronounString.equalsIgnoreCase("HIM") ||
                      pronounString.equalsIgnoreCase("HIS"));

    //1.
    boolean antecedentFound = false;
    int scopeFirstIndex = sentenceIndex - SENTENCES_IN_SCOPE;
    if (scopeFirstIndex < 0 ) scopeFirstIndex = 0;

    int currSentenceIndex = sentenceIndex;
    Annotation bestAntecedent = null;

    while (currSentenceIndex >= scopeFirstIndex || antecedentFound == false) {
      Sentence currSentence = this.textSentences[currSentenceIndex];
      AnnotationSet persons = currSentence.getPersons();

      Iterator it = persons.iterator();
      while (it.hasNext()) {
        Annotation currPerson = (Annotation)it.next();
        String gender = (String)this.personGender.get(currPerson);

        if (null == gender ||
            gender.equalsIgnoreCase("MALE") ||
            gender.equalsIgnoreCase("UNKNOWN")) {
          //hit
          antecedentFound = true;

          if (null == bestAntecedent) {
            bestAntecedent = currPerson;
          }
          else {
            bestAntecedent = _chooseAntecedent$HE$HIM$HIS$SHE$HER$(bestAntecedent,currPerson,pronoun);
          }
        }
      }

      if (0 == currSentenceIndex--)
        break;

    }
gate.util.Err.println("found antecedent for ["+pronounString+"] : " + bestAntecedent);
    return bestAntecedent;
  }


  private Annotation _resolve$SHE$HER$(Annotation pronoun, int sentenceIndex) {

    //0. preconditions
    Assert.assertTrue(pronoun.getType().equals(TOKEN_TYPE));
    Assert.assertTrue(pronoun.getFeatures().get(TOKEN_CATEGORY).equals(PRP_CATEGORY) ||
                      pronoun.getFeatures().get(TOKEN_CATEGORY).equals(PRP$_CATEGORY));
    String pronounString = (String)pronoun.getFeatures().get(TOKEN_STRING);
    Assert.assertTrue(pronounString.equalsIgnoreCase("SHE") ||
                      pronounString.equalsIgnoreCase("HER"));

    //1.
    boolean antecedentFound = false;
    int scopeFirstIndex = sentenceIndex - SENTENCES_IN_SCOPE;
    if (scopeFirstIndex < 0 ) scopeFirstIndex = 0;
    int currSentenceIndex = sentenceIndex;
    Annotation bestAntecedent = null;

    while (currSentenceIndex >= scopeFirstIndex || antecedentFound == false) {
      Sentence currSentence = this.textSentences[currSentenceIndex];
      AnnotationSet persons = currSentence.getPersons();

      Iterator it = persons.iterator();
      while (it.hasNext()) {
        Annotation currPerson = (Annotation)it.next();
        String gender = (String)this.personGender.get(currPerson);

        if (null == gender ||
            gender.equalsIgnoreCase("FEMALE") ||
            gender.equalsIgnoreCase("UNKNOWN")) {
          //hit
          antecedentFound = true;

          if (null == bestAntecedent) {
            bestAntecedent = currPerson;
          }
          else {
            bestAntecedent = _chooseAntecedent$HE$HIM$HIS$SHE$HER$(bestAntecedent,currPerson,pronoun);
          }
        }
      }

      if (0 == currSentenceIndex--)
        break;
    }

gate.util.Err.println("found antecedent for ["+pronounString+"] : " + bestAntecedent);
    return bestAntecedent;
  }


  private Annotation _resolve$IT$ITS$(Annotation pronoun, int sentenceIndex) {

    //0. preconditions
    Assert.assertTrue(pronoun.getType().equals(TOKEN_TYPE));
    Assert.assertTrue(pronoun.getFeatures().get(TOKEN_CATEGORY).equals(PRP_CATEGORY) ||
                      pronoun.getFeatures().get(TOKEN_CATEGORY).equals(PRP$_CATEGORY));
    String pronounString = (String)pronoun.getFeatures().get(TOKEN_STRING);
    Assert.assertTrue(pronounString.equalsIgnoreCase("IT") ||
                      pronounString.equalsIgnoreCase("ITS"));

    //1.
    int scopeFirstIndex = sentenceIndex - 1;
    if (scopeFirstIndex < 0 ) scopeFirstIndex = 0;

    int currSentenceIndex = sentenceIndex;
    Annotation bestAntecedent = null;

    while (currSentenceIndex >= scopeFirstIndex) {

      Sentence currSentence = this.textSentences[currSentenceIndex];
      AnnotationSet org = currSentence.getOrganizations();
      AnnotationSet loc = currSentence.getLocations();
      //combine them
      AnnotationSet org_loc = org;
      org_loc.addAll(loc);

      Iterator it = org_loc.iterator();
      while (it.hasNext()) {
        Annotation currOrgLoc = (Annotation)it.next();

        if (null == bestAntecedent) {
          //discard cataphoric references
          if (currOrgLoc.getStartNode().getOffset().longValue() <
                                          pronoun.getStartNode().getOffset().longValue()) {
            bestAntecedent = currOrgLoc;
          }
        }
        else {
          bestAntecedent = this._chooseAntecedent$IT$ITS$(bestAntecedent,currOrgLoc,pronoun);
        }
      }

      if (0 == currSentenceIndex--)
        break;
    }

gate.util.Err.println("found antecedent for ["+pronounString+"] : " + bestAntecedent);
    return bestAntecedent;

  }


  private Annotation _resolve$I$ME$MY$(Annotation pronoun, int sentenceIndex) {

    //0. preconditions
    Assert.assertTrue(pronoun.getType().equals(TOKEN_TYPE));
    Assert.assertTrue(pronoun.getFeatures().get(TOKEN_CATEGORY).equals(PRP_CATEGORY) ||
                      pronoun.getFeatures().get(TOKEN_CATEGORY).equals(PRP$_CATEGORY));
    String pronounString = (String)pronoun.getFeatures().get(TOKEN_STRING);
    Assert.assertTrue(pronounString.equalsIgnoreCase("I") ||
                      pronounString.equalsIgnoreCase("MY") ||
                      pronounString.equalsIgnoreCase("ME"));

    //1.
    Annotation bestAntecedent = null;

    int closestQuoteIndex = java.util.Arrays.binarySearch(this.quotedText,pronoun,ANNOTATION_OFFSET_COMPARATOR);
    //normalize index
    if (closestQuoteIndex < 0) {
      closestQuoteIndex = -closestQuoteIndex -1 -1;
    }

    //get closest Quote
    Quote quoteContext = this.quotedText[closestQuoteIndex];

    //assure that the pronoun is contained in the quoted text fragment
    //otherwise exit

    if (pronoun.getStartNode().getOffset().intValue() > quoteContext.getEndOffset().intValue() ||
        pronoun.getEndNode().getOffset().intValue() < quoteContext.getStartOffset().intValue()) {
      //oops, probably incorrect text - I/My/Me is not part of quoted text fragment
      //exit
System.out.println("Oops! ["+pronounString+"] not part of quoted fragment...");
      return null;
    }

    //get the Persons that precede/succeed the quoted fragment
    //the order is:
    //
    //[1]. if there exists a Person or pronoun in {he, she} following the quoted fragment but
    //in the same sentence, then use it
    //i.e.  ["PRN1(x)...", said X ...A, B, C ....]
    //
    //[2]. if there is a Person (NOT a pronoun) in the same sentence,
    // preceding the quote, then use it
    //i.e. . [A, B, C...X ..."PRN1(x) ..."...]
    //

    //try [1]
    //get the succeeding Persons/pronouns
    AnnotationSet succCandidates = quoteContext.getAntecedentCandidates(Quote.END_SENTENCE);
    if (false == succCandidates.isEmpty()) {
      //cool, we have candidates, pick up the one closest to the end quote
      Iterator it = succCandidates.iterator();

      while (it.hasNext()) {
        Annotation currCandidate = (Annotation)it.next();
        if (null == bestAntecedent || ANNOTATION_OFFSET_COMPARATOR.compare(bestAntecedent,currCandidate) > 0) {
          //wow, we have a candidate that is closer to the quote
          bestAntecedent = currCandidate;
        }
      }

      //that's all
      return bestAntecedent;
    }

    //try [2]
    //get the preceding Persons/pronouns
    AnnotationSet precCandidates = quoteContext.getAntecedentCandidates(Quote.START_SENTENCE);
    if (false == precCandidates.isEmpty()) {
      //cool, we have candidates, pick up the one closest to the end quote
      Iterator it = precCandidates.iterator();

      while (it.hasNext()) {
        Annotation currCandidate = (Annotation)it.next();
        if (null == bestAntecedent || ANNOTATION_OFFSET_COMPARATOR.compare(bestAntecedent,currCandidate) < 0) {
          //wow, we have a candidate that is closer to the quote
          bestAntecedent = currCandidate;
        }
      }

      //that's all
      return bestAntecedent;
    }

gate.util.Err.println("found antecedent for ["+pronounString+"] : " + bestAntecedent);
    return bestAntecedent;
  }

  private void preprocess() throws ExecutionException {

    //0.5 cleanup
    this.personGender.clear();

    //1.get all annotation in the default set
    this.defaultAnnotations = this.document.getAnnotations();

    //1.5 remove QT annotation sif left from previous execution
    AnnotationSet qtSet = this.defaultAnnotations.get(QUOTED_TEXT_TYPE);
    qtSet.clear();

    //1.6. run quoted text transducer to generate "Quoted Text" annotations
    this.qtTransducer.execute();

    //2.get all SENTENCE annotations
    AnnotationSet sentenceAnnotations = this.defaultAnnotations.get(SENTENCE_TYPE);

    this.textSentences = new Sentence[sentenceAnnotations.size()];
    Object[]  sentenceArray = sentenceAnnotations.toArray();

    java.util.Arrays.sort(sentenceArray,ANNOTATION_OFFSET_COMPARATOR);

    for (int i=0; i< sentenceArray.length; i++) {

      Annotation currSentence = (Annotation)sentenceArray[i];
      Long sentStartOffset = currSentence.getStartNode().getOffset();
      Long sentEndOffset = currSentence.getEndNode().getOffset();

      //2.1. get PERSOSNS in this sentence
      AnnotationSet sentPersons = this.defaultAnnotations.get(PERSON_TYPE,
                                                              sentStartOffset,
                                                              sentEndOffset);

      //2.2. get ORGANIZATIONS in this sentence
      AnnotationSet sentOrgs = this.defaultAnnotations.get(ORG_TYPE,
                                                              sentStartOffset,
                                                              sentEndOffset);

      //2.3. get LOCATION in this sentence
      AnnotationSet sentLocs = this.defaultAnnotations.get(LOC_TYPE,
                                                              sentStartOffset,
                                                              sentEndOffset);

      //2.5. create a Sentence for thei SENTENCE annotation
      this.textSentences[i] = new Sentence(i,
                                            0,
                                            sentStartOffset,
                                            sentEndOffset,
                                            sentPersons,
                                            sentOrgs,
                                            sentLocs
                                  );

      //2.6. for all PERSONs in the sentence - find their gender using the
      //orthographic coreferences if the gender of some entity is unknown
      Iterator itPersons = sentPersons.iterator();
      while (itPersons.hasNext()) {
        Annotation currPerson = (Annotation)itPersons.next();
        String gender = this.findPersonGender(currPerson);
        this.personGender.put(currPerson,gender);
      }
    }

    //3. get the quoted text fragments
    AnnotationSet sentQuotes = this.defaultAnnotations.get(QUOTED_TEXT_TYPE);
    this.quotedText = new Quote[sentQuotes.size()];

    Object[] quotesArray = sentQuotes.toArray();
    java.util.Arrays.sort(quotesArray,ANNOTATION_OFFSET_COMPARATOR);

    for (int i =0; i < quotesArray.length; i++) {
      this.quotedText[i] = new Quote((Annotation)quotesArray[i]);
    }
  }


  private String findPersonGender(Annotation person) {

    String result = (String)person.getFeatures().get(PERSON_GENDER);

    if (null==result) {
      //gender is unknown - try to find it from the ortho coreferences
      List orthoMatches = (List)person.getFeatures().get(PERSON_ORTHO_COREF);

      if (null != orthoMatches) {
        Iterator itMatches = orthoMatches.iterator();

        while (itMatches.hasNext()) {
          Integer correferringID = (Integer)itMatches.next();
          Annotation coreferringEntity = this.defaultAnnotations.get(correferringID);
          Assert.assertTrue(coreferringEntity.getType().equalsIgnoreCase(PERSON_TYPE));
          String correferringGender = (String)coreferringEntity.getFeatures().get(PERSON_GENDER);

          if (null != correferringGender) {
            result = correferringGender;
            break;
          }
        }
      }
    }

    return result;
  }


  private static class AnnotationOffsetComparator implements Comparator {

    private int _getOffset(Object o) {

      if (o instanceof Annotation) {
        return ((Annotation)o).getEndNode().getOffset().intValue();
      }
      else if (o instanceof Sentence) {
        return ((Sentence)o).getStartOffset().intValue();
      }
      else if (o instanceof Quote) {
        return ((Quote)o).getStartOffset().intValue();
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
                        o1 instanceof Sentence ||
                        o1 instanceof Quote ||
                        o1 instanceof Node);
      Assert.assertTrue(o2 instanceof Annotation ||
                        o2 instanceof Sentence ||
                        o2 instanceof Quote ||
                        o2 instanceof Node);

      int offset1 = _getOffset(o1);
      int offset2 = _getOffset(o2);

      return offset1 - offset2;
    }
  }


  private Annotation _chooseAntecedent$HE$HIM$HIS$SHE$HER$(Annotation ant1, Annotation ant2, Annotation pronoun) {

    //0. preconditions
    Assert.assertNotNull(ant1);
    Assert.assertNotNull(ant2);
    Assert.assertNotNull(pronoun);
    Assert.assertTrue(pronoun.getFeatures().get(TOKEN_CATEGORY).equals(PRP_CATEGORY) ||
                      pronoun.getFeatures().get(TOKEN_CATEGORY).equals(PRP$_CATEGORY));
    String pronounString = (String)pronoun.getFeatures().get(TOKEN_STRING);
    Assert.assertTrue(pronounString.equalsIgnoreCase("SHE") ||
                      pronounString.equalsIgnoreCase("HER") ||
                      pronounString.equalsIgnoreCase("HE") ||
                      pronounString.equalsIgnoreCase("HIM") ||
                      pronounString.equalsIgnoreCase("HIS"));

    if (pronounString.equalsIgnoreCase("HE") ||
        pronounString.equalsIgnoreCase("HIS")||
        pronounString.equalsIgnoreCase("HIM") ||
        pronounString.equalsIgnoreCase("SHE") ||
        pronounString.equalsIgnoreCase("HER")) {

      Long offset1 = ant1.getStartNode().getOffset();
      Long offset2 = ant2.getStartNode().getOffset();
      Long offsetPrn = pronoun.getStartNode().getOffset();

      long diff1 = offsetPrn.longValue() - offset1.longValue();
      long diff2 = offsetPrn.longValue() - offset2.longValue();
      Assert.assertTrue(diff1 != 0 && diff2 != 0);

      //get the one CLOSEST AND PRECEDING the pronoun
      if (diff1 > 0 && diff2 > 0) {
        //we have [...antecedentA...AntecedentB....pronoun...] ==> choose B
        if (diff1 < diff2)
          return ant1;
        else
          return ant2;
      }
      else if (diff1 < 0 && diff2 < 0){
        //we have [...pronoun ...antecedentA...AntecedentB.......] ==> choose A
        if (Math.abs(diff1) < Math.abs(diff2))
          return ant1;
        else
          return ant2;
      }
      else {
        Assert.assertTrue(Math.abs(diff1 + diff2) < Math.abs(diff1) + Math.abs(diff2));
        //we have [antecedentA...pronoun...AntecedentB] ==> choose A
        if (diff1 > 0)
          return ant1;
        else
          return ant2;
      }

    }
    else {
      throw new MethodNotImplementedException();
    }
  }


  private Annotation _chooseAntecedent$IT$ITS$(Annotation ant1, Annotation ant2, Annotation pronoun) {

    //0. preconditions
    Assert.assertNotNull(ant1);
    Assert.assertNotNull(ant2);
    Assert.assertNotNull(pronoun);
    Assert.assertTrue(pronoun.getFeatures().get(TOKEN_CATEGORY).equals(PRP_CATEGORY) ||
                      pronoun.getFeatures().get(TOKEN_CATEGORY).equals(PRP$_CATEGORY));
    String pronounString = (String)pronoun.getFeatures().get(TOKEN_STRING);

    Assert.assertTrue(pronounString.equalsIgnoreCase("IT") ||
                      pronounString.equalsIgnoreCase("ITS"));

    Long offset1 = ant1.getStartNode().getOffset();
    Long offset2 = ant2.getStartNode().getOffset();
    Long offsetPrn = pronoun.getStartNode().getOffset();
    long diff1 = offsetPrn.longValue() - offset1.longValue();
    long diff2 = offsetPrn.longValue() - offset2.longValue();
    Assert.assertTrue(diff1 != 0 && diff2 != 0);

    //get the one CLOSEST AND PRECEDING the pronoun
    if (diff1 > 0 && diff2 > 0) {
      //we have [...antecedentA...AntecedentB....pronoun...] ==> choose B
      if (diff1 < diff2)
        return ant1;
      else
        return ant2;
    }
    else if (diff1 > 0){
      Assert.assertTrue(Math.abs(diff1 + diff2) < Math.abs(diff1) + Math.abs(diff2));
      //we have [antecedentA...pronoun...AntecedentB] ==> choose A
      return ant1;
    }
    else if (diff2 > 0){
      Assert.assertTrue(Math.abs(diff1 + diff2) < Math.abs(diff1) + Math.abs(diff2));
      //we have [antecedentA...pronoun...AntecedentB] ==> choose A
      return ant2;
    }
    else {
      //both possible antecedents are BEHIND the anaophoric pronoun - i.e. we have either
      //cataphora, or nominal antecedent, or an antecedent that is further back in scope
      //in any case - discard the antecedents
      return null;
    }
  }


  private class Quote {

    public static final int END_SENTENCE = 1;
    public static final int START_SENTENCE = 2;
    public static final int PREV_SENTENCE = 3;

    private AnnotationSet precPersonsInSentence;
    private AnnotationSet succPersonsInSentence;
    private AnnotationSet precPersonsInContext;
    private Annotation quoteAnnotation;

    public Quote(Annotation quoteAnnotation) {

      this.quoteAnnotation = quoteAnnotation;
      init();
    }

    private void init() {

      //0.preconditions
      Assert.assertNotNull(textSentences);

      //1. generate the precPersons set

      //1.1 locate the sentece containing the opening quote marks
      int quoteStartPos = java.util.Arrays.binarySearch(textSentences,
                                                        this.quoteAnnotation.getStartNode(),
                                                        ANNOTATION_OFFSET_COMPARATOR);
      int startSentenceIndex = -quoteStartPos -1 -1; // blame Sun, not me

      //1.2. get the persons and restrict to these that precede the quote (i.e. not contained
      //in the quote)
      this.precPersonsInSentence = new AnnotationSetImpl(
                                              textSentences[startSentenceIndex].getPersons());
      Iterator itPersons = this.precPersonsInSentence.iterator();
      while (itPersons.hasNext()) {
        Annotation currPerson = (Annotation)itPersons.next();
        if (currPerson.getStartNode().getOffset().intValue() > getStartOffset().intValue()) {
          itPersons.remove();
        }
      }

      //2. generate the precPersonsInCOntext set
      //2.1. get the persons from the sentence precedeing the sentence containing the quote start
      if (startSentenceIndex > 0) {
        this.precPersonsInContext = new AnnotationSetImpl(
                                            textSentences[startSentenceIndex-1].getPersons());
      }

      //2. generate the succ  Persons set
      //2.1 locate the sentece containing the closing quote marks
      int quoteEndPos = java.util.Arrays.binarySearch(textSentences,
                                                        this.quoteAnnotation.getEndNode(),
                                                        ANNOTATION_OFFSET_COMPARATOR);
      int endSentenceIndex = -quoteEndPos -1 -1; // blame Sun, not me
      Sentence endSentence = textSentences[endSentenceIndex];

      //2.2. get the persons and restrict to these that succeed the quote (i.e. not contained
      //in the quoted fragment)
      this.succPersonsInSentence = new AnnotationSetImpl(endSentence.getPersons());
      itPersons = this.succPersonsInSentence.iterator();

      while (itPersons.hasNext()) {
        Annotation currPerson = (Annotation)itPersons.next();
        if (currPerson.getStartNode().getOffset().intValue() < this.getEndOffset().intValue()) {
          itPersons.remove();
        }
      }

      //2.3 now get all HE/SHE pronouns that follow the quote in the same sentence
      AnnotationSet sentPronouns = defaultAnnotations.getContained(this.getEndOffset(),
                                                                  endSentence.getEndOffset());
      FeatureMap restriction = new SimpleFeatureMapImpl();
      restriction.put(TOKEN_CATEGORY,PRP_CATEGORY);

      AnnotationSet personalPronouns = null;
      if (null != sentPronouns) {
        personalPronouns = sentPronouns.get(TOKEN_TYPE,restriction);

        if (null != personalPronouns) {
          //add to succPersons
          this.succPersonsInSentence.addAll(personalPronouns);
        }
      }
    }

    public Long getStartOffset() {
      return this.quoteAnnotation.getStartNode().getOffset();
    }

    public Long getEndOffset() {
      return this.quoteAnnotation.getEndNode().getOffset();
    }

    public AnnotationSet getAntecedentCandidates(int type) {

      switch(type) {

        case END_SENTENCE:
          return this.succPersonsInSentence;

        case START_SENTENCE:
          return this.precPersonsInSentence;

        case PREV_SENTENCE:
          return this.precPersonsInContext;

        default:
          throw new IllegalArgumentException();
      }
    }

  }


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