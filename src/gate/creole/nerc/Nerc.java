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
      Map listeners = new HashMap();
      listeners.put("gate.event.StatusListener", new StatusListener(){
        public void statusChanged(String text){
          fireStatusChanged(text);
        }
      });

      ResourceData rData;

      //tokeniser
      fireStatusChanged("Creating a tokeniser");
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
                                                           params, features,
                                                           listeners);
      this.add(tokeniser);
      tokeniser.getFeatures().put("gate.NAME", "Tokeniser " + System.currentTimeMillis());
      fireProgressChanged(10);

      //gazetteer
      fireStatusChanged("Creating a gazetteer");
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

      listeners.put("gate.event.ProgressListener",
                    new CustomProgressListener(11, 50));

      gazetteer = (DefaultGazetteer)Factory.createResource(rData.getClassName(),
                                                           params, features,
                                                           listeners);
      this.add(gazetteer);
      gazetteer.getFeatures().put("gate.NAME", "Gazetteer " + System.currentTimeMillis());
      fireProgressChanged(50);

      //transducer
      fireStatusChanged("Creating a Jape transducer");
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
      listeners.put("gate.event.ProgressListener",
                    new CustomProgressListener(11, 50));
      transducer = (Transducer)Factory.createResource(rData.getClassName(),
                                                      params, features,
                                                      listeners);
      fireProgressChanged(100);
      fireProcessFinished();
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
  public synchronized void removeProgressListener(ProgressListener l) {
    if (progressListeners != null && progressListeners.contains(l)) {
      Vector v = (Vector) progressListeners.clone();
      v.removeElement(l);
      progressListeners = v;
    }
  }
  public synchronized void addProgressListener(ProgressListener l) {
    Vector v = progressListeners == null ? new Vector(2) : (Vector) progressListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      progressListeners = v;
    }
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
  private transient Vector progressListeners;
  private transient Vector statusListeners;
  protected void fireProgressChanged(int e) {
    if (progressListeners != null) {
      Vector listeners = progressListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((ProgressListener) listeners.elementAt(i)).progressChanged(e);
      }
    }
  }
  protected void fireProcessFinished() {
    if (progressListeners != null) {
      Vector listeners = progressListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((ProgressListener) listeners.elementAt(i)).processFinished();
      }
    }
  }
  public synchronized void removeStatusListener(StatusListener l) {
    if (statusListeners != null && statusListeners.contains(l)) {
      Vector v = (Vector) statusListeners.clone();
      v.removeElement(l);
      statusListeners = v;
    }
  }
  public synchronized void addStatusListener(StatusListener l) {
    Vector v = statusListeners == null ? new Vector(2) : (Vector) statusListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      statusListeners = v;
    }
  }
  protected void fireStatusChanged(String e) {
    if (statusListeners != null) {
      Vector listeners = statusListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((StatusListener) listeners.elementAt(i)).statusChanged(e);
      }
    }
  }

  class CustomProgressListener implements ProgressListener{
    CustomProgressListener(int start, int end){
      this.start = start;
      this.end = end;
    }
    public void progressChanged(int i){
      fireProgressChanged(start + (end - start) * i / 100);
    }

    public void processFinished(){
      fireProgressChanged(end);
    }

    int start;
    int end;
  }
} // class Nerc
