/*
 *  Coreferencer.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 18/Dec/2001
 *
 *  $Id$
 */

package gate.creole.coref;

import java.util.*;

import junit.framework.*;

import gate.*;
import gate.creole.*;
import gate.util.*;

public class Coreferencer extends AbstractLanguageAnalyser
                          implements ProcessingResource{

  /** --- */
  private static final boolean DEBUG = true;
  /** --- */
  private PronominalCoref pronominalModule;

  /** --- */
  public Coreferencer() {
    this.pronominalModule = new PronominalCoref();
  }


  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {

    Resource result = super.init();

    //load all submodules
    this.pronominalModule.init();

    return result;
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


  /** Set the document to run on. */
  public void setDocument(Document newDocument) {

    Assert.assertNotNull(newDocument);

    this.pronominalModule.setDocument(newDocument);
    super.setDocument(newDocument);
  }


  /** --- */
  public void setAnnotationSetName(String annotationSetName) {
    this.pronominalModule.setAnnotationSetName(annotationSetName);
  }

  /** --- */
  public String getAnnotationSetName() {
    return this.pronominalModule.getAnnotationSetName();
  }

  /**
   * This method runs the coreferencer. It assumes that all the needed parameters
   * are set. If they are not, an exception will be fired.
   */
  public void execute() throws ExecutionException {

    this.pronominalModule.execute();
    generateCorefChains();
  }

  /** --- */
  private void generateCorefChains() throws GateRuntimeException{

    //1. get the resolved corefs
    HashMap ana2ant = this.pronominalModule.getResolvedAnaphora();

    //2. get the outout annotation set
    String asName = getAnnotationSetName();
    AnnotationSet outputSet = null;

    if (null == asName || asName.equals("")) {
      outputSet = getDocument().getAnnotations();
    }
    else {
      outputSet = getDocument().getAnnotations(asName);
    }

    //3. generate new annotations
    Iterator it = ana2ant.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry currLink = (Map.Entry)it.next();
      Annotation anaphor = (Annotation)currLink.getKey();
      Annotation antecedent = (Annotation)currLink.getValue();

      if (DEBUG) {
        AnnotationSet corefSet = getDocument().getAnnotations("COREF");
        Long antOffset = new Long(0);

        if (null != antecedent) {
          antOffset = antecedent.getStartNode().getOffset();
        }

        FeatureMap features = new SimpleFeatureMapImpl();
        features.put("antecedent",antOffset);
        corefSet.add(anaphor.getStartNode(),anaphor.getEndNode(),"COREF",features);
      }

      //do we have antecedent?
      if (null == antecedent) {
        continue;
      }

      //get the ortho-matches of the antecedent
      List matches = (List)antecedent.getFeatures().get("matches");
      if (null == matches) {
        matches = new ArrayList();
        matches.add(antecedent.getId());
        antecedent.getFeatures().put("matches",matches);
        //check if the document has a list of matches
        //if yes, simply add the new list to it
        //if not, create it and add the list of matches to it
        if (document.getFeatures().containsKey(
            ANNIEConstants.DOCUMENT_COREF_FEATURE_NAME)) {
          Map matchesMap = (Map) document.getFeatures().get(
                                ANNIEConstants.DOCUMENT_COREF_FEATURE_NAME);
          List matchesList = (List) matchesMap.get(getAnnotationSetName());
          if (matchesList == null) {
            matchesList = new ArrayList();
            matchesMap.put(getAnnotationSetName(), matchesList);
          }
          matchesList.add(matches);
        } else {
          Map matchesMap = new HashMap();
            List matchesList = new ArrayList();
            matchesMap.put(getAnnotationSetName(), matchesList);
            matchesList.add(matches);
        }//if else
      }//if matches == null

      FeatureMap features = new SimpleFeatureMapImpl();
      features.put("ENTITY_MENTION_TYPE","PRONOUN");
      features.put("matches",matches);
      features.put("antecedent_offset",antecedent.getStartNode().getOffset());

      Integer annID = outputSet.add(anaphor.getStartNode(),
                                    anaphor.getEndNode(),
                                    antecedent.getType(),
                                    features);
      matches.add(annID);
    }
  }

}