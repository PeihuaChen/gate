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
      Gate.setHiddenAttribute(features, true);
      tokeniser = (DefaultTokeniser)Factory.createResource(rData.getClassName(),
                                                           params, features,
                                                           listeners);
      this.add(tokeniser);
      tokeniser.setName("Tokeniser " + System.currentTimeMillis());
      fireProgressChanged(10);

      //gazetteer
      fireStatusChanged("Creating a gazetteer");
      params = Factory.newFeatureMap();
      rData = (ResourceData)Gate.getCreoleRegister().get(
              "gate.creole.gazetteer.DefaultGazetteer");
      params.putAll(rData.getParameterList().getInitimeDefaults());
      if(gazetteerListsURL != null) params.put("listsURL",
                                               gazetteerListsURL);
      params.put("encoding", encoding);
      if(DEBUG) Out.prln("Parameters for the gazetteer: \n" + params);
      features = Factory.newFeatureMap();
      Gate.setHiddenAttribute(features, true);

      listeners.put("gate.event.ProgressListener",
                    new CustomProgressListener(11, 50));

      gazetteer = (DefaultGazetteer)Factory.createResource(rData.getClassName(),
                                                           params, features,
                                                           listeners);
      this.add(gazetteer);
      gazetteer.setName("Gazetteer " + System.currentTimeMillis());
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
      Gate.setHiddenAttribute(features, true);
      listeners.put("gate.event.ProgressListener",
                    new CustomProgressListener(11, 50));
      transducer = (Transducer)Factory.createResource(rData.getClassName(),
                                                      params, features,
                                                      listeners);
      fireProgressChanged(100);
      fireProcessFinished();
      this.add(transducer);
      transducer.setName("Transducer " + System.currentTimeMillis());
    }catch(ParameterException pe){
      throw new ResourceInstantiationException(pe);
    }
    return this;
  } // init()

  public void run(){
    try{
      FeatureMap params;
      if(tempAnnotationSetName.equals("")) tempAnnotationSetName = null;
      try{
        fireProgressChanged(0);
        params = Factory.newFeatureMap();
        params.put("document", document);
        params.put("annotationSetName", tempAnnotationSetName);
        Factory.setResourceParameters(tokeniser, params);

        params = Factory.newFeatureMap();
        params.put("document", document);
        params.put("annotationSetName", tempAnnotationSetName);
        Factory.setResourceParameters(gazetteer, params);

        params = Factory.newFeatureMap();
        params.put("document", document);
        params.put("inputASName", tempAnnotationSetName);
        params.put("outputASName", tempAnnotationSetName);
        Factory.setResourceParameters(transducer, params);
      }catch(Exception e){
        throw new ExecutionException("Couldn't set parameters: " + e);
      }
      fireProgressChanged(5);
      ProgressListener pListener = new CustomProgressListener(5, 15);
      StatusListener sListener = new StatusListener(){
        public void statusChanged(String text){
          fireStatusChanged(text);
        }
      };

      tokeniser.addProgressListener(pListener);
      tokeniser.addStatusListener(sListener);
      tokeniser.run();
      tokeniser.check();
      tokeniser.removeProgressListener(pListener);
      tokeniser.removeStatusListener(sListener);

      pListener = new CustomProgressListener(15, 25);
      gazetteer.addProgressListener(pListener);
      gazetteer.addStatusListener(sListener);
      gazetteer.run();
      gazetteer.check();
      gazetteer.removeProgressListener(pListener);
      gazetteer.removeStatusListener(sListener);

      pListener = new CustomProgressListener(25, 90);
      transducer.addProgressListener(pListener);
      transducer.addStatusListener(sListener);
      transducer.run();
      transducer.check();
      transducer.removeProgressListener(pListener);
      transducer.removeStatusListener(sListener);


      EntitySet entitySet =
        new EntitySet(document.getSourceUrl().getFile(),
                      document,
                      document.getAnnotations(tempAnnotationSetName).
                      get(new HashSet(Arrays.asList(
                        new String[]{"Address", "Date", "Identifier",
                                     "Location", "Organization", "Person"}))));

      document.getFeatures().put("entitySet", entitySet);
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
  protected java.net.URL tokeniserRulesURL;
  protected java.net.URL gazetteerListsURL;
  protected java.net.URL japeGrammarURL;
  protected String encoding;
  protected gate.Document document;
  private transient Vector progressListeners;
  private transient Vector statusListeners;
  protected String tempAnnotationSetName = "nercAS";

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
  public void setTempAnnotationSetName(String newTempAnnotationSetName) {
    tempAnnotationSetName = newTempAnnotationSetName;
  }
  public String getTempAnnotationSetName() {
    return tempAnnotationSetName;
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
