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
import gate.creole.splitter.*;
import gate.util.*;
import gate.event.*;


/** NERC stands for Named-Entity Recognition Component. This class wraps
  * various of GATE's builtin CREOLE components to form an NE recogniser.
  */
public class Nerc extends AbstractProcessingResource {
  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {
    try{
      //init super object
      super.init();
      //create all the componets
      FeatureMap params;
      FeatureMap features;
      Map listeners = new HashMap();
      if (! Main.batchMode) {//fire events if not in batch mode
        listeners.put("gate.event.StatusListener", new StatusListener(){
          public void statusChanged(String text){
            fireStatusChanged(text);
          }
        });

      //tokeniser
        fireStatusChanged("Creating a tokeniser");
      } //if do events if we're not in batch
      params = Factory.newFeatureMap();
      if(tokeniserRulesURL != null) params.put("tokeniserRulesURL",
                                               tokeniserRulesURL);
      if(tokeniserPostprocessor != null) params.put("transducerGrammarURL",
                                               tokeniserPostprocessor);

      params.put("encoding", encoding);
      if(DEBUG) Out.prln("Parameters for the tokeniser: \n" + params);
      features = Factory.newFeatureMap();
      Gate.setHiddenAttribute(features, true);
      tokeniser = (DefaultTokeniser)Factory.createResource(
                    "gate.creole.tokeniser.DefaultTokeniser",
                    params, features, listeners);
      tokeniser.setName("Tokeniser " + System.currentTimeMillis());
      if (!Main.batchMode) {
        fireProgressChanged(10);

      //gazetteer
        fireStatusChanged("Creating a gazetteer");
      }
      params = Factory.newFeatureMap();
      if(gazetteerListsURL != null) params.put("listsURL",
                                               gazetteerListsURL);
      params.put("encoding", encoding);
      params.put("caseSensitive", caseSensitiveGazetteer);
      if(DEBUG) Out.prln("Parameters for the gazetteer: \n" + params);
      features = Factory.newFeatureMap();
      Gate.setHiddenAttribute(features, true);

      if (! Main.batchMode) //fire events if not in batch mode
        listeners.put("gate.event.ProgressListener",
                    new CustomProgressListener(11, 50));

      gazetteer = (DefaultGazetteer)Factory.createResource(
                      "gate.creole.gazetteer.DefaultGazetteer",
                      params, features, listeners);
      gazetteer.setName("Gazetteer " + System.currentTimeMillis());
      if (!Main.batchMode) {
        fireProgressChanged(50);

      //sentence spliter
        fireStatusChanged("Creating a sentence splitter");
      }
      params = Factory.newFeatureMap();
      if(splitterGazetteerURL != null) params.put("gazetteerListsURL",
                                               splitterGazetteerURL);
      if(splitterGrammarURL != null) params.put("transducerURL",
                                               splitterGrammarURL);

      params.put("encoding", encoding);
      if(DEBUG) Out.prln("Parameters for the sentence splitter: \n" + params);
      features = Factory.newFeatureMap();
      Gate.setHiddenAttribute(features, true);

      if (! Main.batchMode) //fire events if not in batch mode
        listeners.put("gate.event.ProgressListener",
                    new CustomProgressListener(50, 60));

      splitter = (SentenceSplitter)Factory.createResource(
                      "gate.creole.splitter.SentenceSplitter",
                      params, features, listeners);
      splitter.setName("Splitter " + System.currentTimeMillis());
      if (!Main.batchMode) {
        fireProgressChanged(60);

      //POS Tagger
        fireStatusChanged("Creating a POS tagger");
      }
      params = Factory.newFeatureMap();
      if(taggerLexiconURL != null) params.put("lexiconURL",
                                               taggerLexiconURL);
      if(taggerRulesURL != null) params.put("rulesURL",
                                               taggerRulesURL);

      if(DEBUG) Out.prln("Parameters for the POS tagger: \n" + params);
      features = Factory.newFeatureMap();
      Gate.setHiddenAttribute(features, true);

      if (! Main.batchMode) //fire events if not in batch mode
        listeners.put("gate.event.ProgressListener",
                    new CustomProgressListener(60, 65));

      tagger = (POSTagger)Factory.createResource(
                      "gate.creole.POSTagger",
                      params, features, listeners);
      tagger.setName("Tagger " + System.currentTimeMillis());
      if (!Main.batchMode) {
        fireProgressChanged(65);


      //transducer
        fireStatusChanged("Creating a Jape transducer");
      }
      params = Factory.newFeatureMap();
  //      rData = (ResourceData)Gate.getCreoleRegister().get(
  //              "gate.creole.Transducer");
  //      params.putAll(rData.getParameterList().getInitimeDefaults());
      if(japeGrammarURL != null) params.put("grammarURL",
                                            japeGrammarURL);
      params.put("encoding", encoding);
      if(DEBUG) Out.prln("Parameters for the transducer: \n" + params);
      features = Factory.newFeatureMap();
      Gate.setHiddenAttribute(features, true);
      if (! Main.batchMode) //fire events if not in batch mode
        listeners.put("gate.event.ProgressListener",
                    new CustomProgressListener(66, 100));
      transducer = (ANNIETransducer)Factory.createResource(
                                              "gate.creole.ANNIETransducer",
                                              params, features,
                                              listeners);
      if (! Main.batchMode) {//fire events if not in batch mode
        fireProgressChanged(100);
        fireProcessFinished();
      } //if
      transducer.setName("Transducer " + System.currentTimeMillis());
    }catch(ResourceInstantiationException rie){
      throw rie;
    }catch(Exception e){
      throw new ResourceInstantiationException(e);
    }
    return this;
  } // init()

  /**
   * Runs the group of processing resources over the current document
   */
  protected void runSystem() throws ExecutionException{
    FeatureMap params;
    if(tempAnnotationSetName.equals("")) tempAnnotationSetName = null;
    try{
      if (!Main.batchMode)
        fireProgressChanged(0);
      //tokeniser
      params = Factory.newFeatureMap();
      params.put("document", document);
      params.put("annotationSetName", tempAnnotationSetName);
      Factory.setResourceRuntimeParameters(tokeniser, params);

      //gazetteer
      params = Factory.newFeatureMap();
      params.put("document", document);
      params.put("annotationSetName", tempAnnotationSetName);
      Factory.setResourceRuntimeParameters(gazetteer, params);

      //sentence splitter
      params = Factory.newFeatureMap();
      params.put("document", document);
      params.put("inputASName", tempAnnotationSetName);
      params.put("outputASName", tempAnnotationSetName);
      Factory.setResourceRuntimeParameters(splitter, params);

      //POS tagger
      params = Factory.newFeatureMap();
      params.put("document", document);
      params.put("inputASName", tempAnnotationSetName);
      params.put("outputASName", tempAnnotationSetName);
      Factory.setResourceRuntimeParameters(tagger, params);

      //transducer
      params = Factory.newFeatureMap();
      params.put("document", document);
      params.put("inputASName", tempAnnotationSetName);
      params.put("outputASName", tempAnnotationSetName);
      Factory.setResourceRuntimeParameters(transducer, params);
    }catch(Exception e){
      throw new ExecutionException(e);
    }
    ProgressListener pListener = null;
    StatusListener sListener = null;
    if (!Main.batchMode) {
      fireProgressChanged(5);
      pListener = new CustomProgressListener(5, 15);
      sListener = new StatusListener(){
        public void statusChanged(String text){
          fireStatusChanged(text);
        }
      };

    //tokeniser
      tokeniser.addProgressListener(pListener);
      tokeniser.addStatusListener(sListener);
    }
    tokeniser.run();
    tokeniser.check();
    if (!Main.batchMode) {
      tokeniser.removeProgressListener(pListener);
      tokeniser.removeStatusListener(sListener);

    //gazetteer

      pListener = new CustomProgressListener(10, 20);
      gazetteer.addProgressListener(pListener);
      gazetteer.addStatusListener(sListener);
    }
    gazetteer.run();
    gazetteer.check();
    if (!Main.batchMode) {
      gazetteer.removeProgressListener(pListener);
      gazetteer.removeStatusListener(sListener);

    //sentence splitter
      pListener = new CustomProgressListener(20, 35);
      splitter.addProgressListener(pListener);
      splitter.addStatusListener(sListener);
    }
    splitter.run();
    splitter.check();
    if (!Main.batchMode) {
      splitter.removeProgressListener(pListener);
      splitter.removeStatusListener(sListener);

    //POS tagger
      pListener = new CustomProgressListener(35, 40);
      tagger.addProgressListener(pListener);
      tagger.addStatusListener(sListener);
    }
    tagger.run();
    tagger.check();
    if (!Main.batchMode) {
      tagger.removeProgressListener(pListener);
      tagger.removeStatusListener(sListener);

    //transducer
      pListener = new CustomProgressListener(40, 90);
      transducer.addProgressListener(pListener);
      transducer.addStatusListener(sListener);
    }
    transducer.run();
    transducer.check();
    if (!Main.batchMode) {
      transducer.removeProgressListener(pListener);
      transducer.removeStatusListener(sListener);
    }
  }//protected void runSystem() throws ExecutionException

  /**
   * reads the results created by the system run and packages them in one
   * entity set that is added to the document features
   */
  protected void createEntitySet(){
    EntitySet entitySet =
      new EntitySet(document.getSourceUrl().getFile(),
                    document,
                    document.getAnnotations(tempAnnotationSetName).
                    get(new HashSet(Arrays.asList(
                      new String[]{"Address", "Date", "Identifier",
                                   "Location", "Organization", "Person"}))));

    document.getFeatures().put("entitySet", entitySet);
  }//protected void createEntitySet(){

  public void run(){
    try{
      runSystem();
      createEntitySet();
      fireProgressChanged(100);
      fireProcessFinished();
    }catch(ExecutionException ee){
      executionException = ee;
    }catch(Exception e){
      executionException = new ExecutionException(e);
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
  public void setDocument(gate.Document newDocument) {
    document = newDocument;
  }

  public gate.Document getDocument() {
    return document;
  }


  /** The tokeniser used by this NERC */
  protected DefaultTokeniser tokeniser;

  /** The gazetteer used by this NERC */
  protected DefaultGazetteer gazetteer;

  /** The sentence splitter used by this NERC */
  protected SentenceSplitter splitter;

  /** The POS Tagger used by this NERC */
  protected POSTagger tagger;

  /** The Jape transducer used by this NERC */
  protected ANNIETransducer transducer;


  private static final boolean DEBUG = false;
  protected java.net.URL tokeniserRulesURL;
  protected java.net.URL gazetteerListsURL;
  protected java.net.URL japeGrammarURL;
  protected String encoding;
  protected gate.Document document;
  protected String tempAnnotationSetName = "nercAS";
  private Boolean caseSensitiveGazetteer;
  private java.net.URL splitterGazetteerURL;
  private java.net.URL splitterGrammarURL;
  private java.net.URL taggerRulesURL;
  private java.net.URL taggerLexiconURL;
  private java.net.URL tokeniserPostprocessor;

  public void setTempAnnotationSetName(String newTempAnnotationSetName) {
    tempAnnotationSetName = newTempAnnotationSetName;
  }
  public String getTempAnnotationSetName() {
    return tempAnnotationSetName;
  }
  public void setCaseSensitiveGazetteer(Boolean newCaseSensitiveGazetteer) {
    caseSensitiveGazetteer = newCaseSensitiveGazetteer;
  }
  public Boolean getCaseSensitiveGazetteer() {
    return caseSensitiveGazetteer;
  }
  public void setSplitterGazetteerURL(java.net.URL newSplitterGazetteerURL) {
    splitterGazetteerURL = newSplitterGazetteerURL;
  }
  public java.net.URL getSplitterGazetteerURL() {
    return splitterGazetteerURL;
  }
  public void setSplitterGrammarURL(java.net.URL newSplitterGrammarURL) {
    splitterGrammarURL = newSplitterGrammarURL;
  }
  public java.net.URL getSplitterGrammarURL() {
    return splitterGrammarURL;
  }
  public void setTaggerRulesURL(java.net.URL newTaggerRulesURL) {
    taggerRulesURL = newTaggerRulesURL;
  }
  public java.net.URL getTaggerRulesURL() {
    return taggerRulesURL;
  }
  public void setTaggerLexiconURL(java.net.URL newTaggerLexiconURL) {
    taggerLexiconURL = newTaggerLexiconURL;
  }
  public java.net.URL getTaggerLexiconURL() {
    return taggerLexiconURL;
  }
  public void setTokeniserPostprocessor(java.net.URL tokeniserPostprocessor) {
    this.tokeniserPostprocessor = tokeniserPostprocessor;
  }
  public java.net.URL getTokeniserPostprocessor() {
    return tokeniserPostprocessor;
  }

} // class Nerc
