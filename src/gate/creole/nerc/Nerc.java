/*
 *  Nerc.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, converted from Valy's code, 9/Mar/2001
 *
 *  $Id$
 */

package gate.creole.nerc;

import java.util.*;
import java.io.*;


import gate.*;
import gate.creole.*;
import gate.creole.gazetteer.*;
import gate.creole.tokeniser.*;
import gate.util.*;
import gate.event.*;


/** NERC stands for Named-Entity Recognition Component. This class wraps
  * various of GATE's builtin CREOLE components to form an NE recogniser.
  */
public class Nerc extends SerialController {
  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {
    //init super object
    super.init();

    try{
      //create all the componets
      FeatureMap params;
      FeatureMap features;
      ResourceData rData;

      //tokeniser
      params = Factory.newFeatureMap();
      rData = (ResourceData)Gate.getCreoleRegister().get(
              "gate.creole.tokeniser.DefaultTokeniser");
      params.putAll(rData.getParameterList().getInitimeDefaults());
      if(tokeniserRulesURL != null) params.put("rulesURL",
                                               tokeniserRulesURL);
      params.put("encoding", encoding);
      if(DEBUG) Out.prln("Parameters for the tokeniser: \n" + params);
      features = Factory.newFeatureMap();
      features.put("gate.HIDDEN", "true");
      tokeniser = (DefaultTokeniser)Factory.createResource(rData.getClassName(),
                                                           params, features);
      this.add(tokeniser);
      tokeniser.getFeatures().put("gate.NAME", "Tokeniser " + System.currentTimeMillis());

      //gazetteer
      params = Factory.newFeatureMap();
      rData = (ResourceData)Gate.getCreoleRegister().get(
              "gate.creole.gazetteer.DefaultGazetteer");
      params.putAll(rData.getParameterList().getInitimeDefaults());
      if(gazetteerListsURL != null) params.put("listsURL",
                                               tokeniserRulesURL);
      params.put("encoding", encoding);
      if(DEBUG) Out.prln("Parameters for the gazetteer: \n" + params);
      features = Factory.newFeatureMap();
      features.put("gate.HIDDEN", "true");
      gazetteer = (DefaultGazetteer)Factory.createResource(rData.getClassName(),
                                                           params, features);
      this.add(gazetteer);
      gazetteer.getFeatures().put("gate.NAME", "Gazetteer " + System.currentTimeMillis());

      //transducer
      params = Factory.newFeatureMap();
      rData = (ResourceData)Gate.getCreoleRegister().get(
              "gate.creole.Transducer");
      params.putAll(rData.getParameterList().getInitimeDefaults());
      if(japeGrammarURL != null) params.put("grammarURL",
                                            japeGrammarURL);
      params.put("encoding", encoding);
      if(DEBUG) Out.prln("Parameters for the transducer: \n" + params);
      features = Factory.newFeatureMap();
      features.put("gate.HIDDEN", "true");
      transducer = (Transducer)Factory.createResource(rData.getClassName(),
                                                      params, features);
      this.add(transducer);
      transducer.getFeatures().put("gate.NAME", "Transducer " + System.currentTimeMillis());
    }catch(ParameterException pe){
      throw new ResourceInstantiationException(pe);
    }
    return this;
  } // init()

  public void run(){
    try{
      FeatureMap params;
      try{
        params = Factory.newFeatureMap();
        params.put("document", document);
        params.put("annotationSetName", "nercAS");
        Factory.setResourceParameters(tokeniser, params);
        Factory.setResourceParameters(gazetteer, params);

        params = Factory.newFeatureMap();
        params.put("document", document);
        params.put("inputASName", "nercAS");
        params.put("outputASName", "entities");
        Factory.setResourceParameters(transducer, params);
      }catch(Exception e){
        throw new ExecutionException("Couldn't set parameters: " + e);
      }
      tokeniser.run();
      tokeniser.check();
      gazetteer.run();
      gazetteer.check();
      transducer.run();
      transducer.check();
      EntitySet entitySet = new EntitySet(document.getSourceUrl().getFile(),
                                          document,
                                          document.getAnnotations("nercAS"));
      document.getFeatures().put("entitySet", entitySet);
    }catch(ExecutionException ee){
      executionException = ee;
    }
  }

  public void setTokeniserRulesURL(java.net.URL newTokeniserRulesURL) {
    tokeniserRulesURL = newTokeniserRulesURL;
  }
  public java.net.URL getTokeniserRulesURL() {
    return tokeniserRulesURL;
  }
  public void setGazetteerListsURL(java.net.URL newGazetteerListsURL) {
    gazetteerListsURL = newGazetteerListsURL;
  }
  public java.net.URL getGazetteerListsURL() {
    return gazetteerListsURL;
  }
  public void setJapeGrammarURL(java.net.URL newJapeGrammarURL) {
    japeGrammarURL = newJapeGrammarURL;
  }
  public java.net.URL getJapeGrammarURL() {
    return japeGrammarURL;
  }
  public void setEncoding(String newEncoding) {
    encoding = newEncoding;
  }
  public String getEncoding() {
    return encoding;
  }
  public void setAnnotationSetName(String newAnnotationSetName) {
    annotationSetName = newAnnotationSetName;
  }
  public String getAnnotationSetName() {
    return annotationSetName;
  }
  public void setDocument(gate.corpora.DocumentImpl newDocument) {
    document = newDocument;
  }

  public gate.corpora.DocumentImpl getDocument() {
    return document;
  }



  /** XXX */
  protected DefaultTokeniser tokeniser;

  /** XXX */
  protected DefaultGazetteer gazetteer;

  /** XXX */
  protected Transducer transducer;


  private static final boolean DEBUG = false;
  private java.net.URL tokeniserRulesURL;
  private java.net.URL gazetteerListsURL;
  private java.net.URL japeGrammarURL;
  private String encoding;
  private String annotationSetName;
  private gate.corpora.DocumentImpl document;
} // class Nerc
