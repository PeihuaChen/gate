/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 01 Feb 2000
 *
 *  $Id$
 */


package gate.creole.splitter;

import gate.*;
import gate.util.*;
import gate.event.*;
import gate.creole.tokeniser.*;
import gate.creole.gazetteer.*;
import gate.creole.*;
import gate.creole.nerc.Nerc;

import java.util.*;
/**
 * A sentence splitter. This is module similar to a
 * {@link gate.creole.nerc.Nerc} in the fact that it conatins a tokeniser, a
 * gazetteer and a Jape grammar. This class is used so we can have a different
 * entry in the creole.xml file describing the default resources and to add
 * some minor processing after running the components in order to extract the
 * results in a usable form.
 */
public class SentenceSplitter extends AbstractProcessingResource{

  public Resource init()throws ResourceInstantiationException{
    //create all the componets
    FeatureMap params;
    FeatureMap features;
    Map listeners = new HashMap();
    listeners.put("gate.event.StatusListener", new StatusListener(){
      public void statusChanged(String text){
        fireStatusChanged(text);
      }
    });

    //gazetteer
    fireStatusChanged("Creating the gazetteer");
    params = Factory.newFeatureMap();
    if(gazetteerListsURL != null) params.put("listsURL",
                                             gazetteerListsURL);
    params.put("encoding", encoding);
    features = Factory.newFeatureMap();
    Gate.setHiddenAttribute(features, true);

    listeners.put("gate.event.ProgressListener",
                  new CustomProgressListener(0, 10));

    gazetteer = (DefaultGazetteer)Factory.createResource(
                    "gate.creole.gazetteer.DefaultGazetteer",
                    params, features, listeners);
    gazetteer.setName("Gazetteer " + System.currentTimeMillis());
    fireProgressChanged(10);

    //transducer
    fireStatusChanged("Creating the JAPE transducer");
    params = Factory.newFeatureMap();
    if(transducerURL != null) params.put("grammarURL", transducerURL);
    params.put("encoding", encoding);
    features = Factory.newFeatureMap();
    Gate.setHiddenAttribute(features, true);

    listeners.put("gate.event.ProgressListener",
                  new CustomProgressListener(11, 100));

    transducer = (Transducer)Factory.createResource(
                    "gate.creole.Transducer",
                    params, features, listeners);
    transducer.setName("Transducer " + System.currentTimeMillis());
    fireProgressChanged(100);
    fireProcessFinished();

    return this;
  }

  public void run(){
    try{
      //set the runtime parameters
      FeatureMap params;
      if(inputASName != null && inputASName.equals("")) inputASName = null;
      if(outputASName != null && outputASName.equals("")) outputASName = null;
      try{
        fireProgressChanged(0);
        params = Factory.newFeatureMap();
        params.put("document", document);
        params.put("annotationSetName", inputASName);
        Factory.setResourceRuntimeParameters(gazetteer, params);

        params = Factory.newFeatureMap();
        params.put("document", document);
        params.put("inputASName", inputASName);
        params.put("outputASName", inputASName);
        Factory.setResourceRuntimeParameters(transducer, params);
      }catch(Exception e){
        throw new ExecutionException(e);
      }
      fireProgressChanged(5);

      //run the gazetteer
      ProgressListener pListener = new CustomProgressListener(5, 10);
      StatusListener sListener = new StatusListener(){
        public void statusChanged(String text){
          fireStatusChanged(text);
        }
      };
      gazetteer.addProgressListener(pListener);
      gazetteer.addStatusListener(sListener);
      gazetteer.run();
      gazetteer.check();
      gazetteer.removeProgressListener(pListener);
      gazetteer.removeStatusListener(sListener);

      //run the transducer
      pListener = new CustomProgressListener(11, 90);
      transducer.addProgressListener(pListener);
      transducer.addStatusListener(sListener);
      transducer.run();
      transducer.check();
      transducer.removeProgressListener(pListener);
      transducer.removeStatusListener(sListener);

      //copy the results to the output set
      if(!inputASName.equals(outputASName)){
        AnnotationSet inputAS = (inputASName == null) ?
                                document.getAnnotations() :
                                document.getAnnotations(inputASName);

        AnnotationSet outputAS = (outputASName == null) ?
                                 document.getAnnotations() :
                                 document.getAnnotations(outputASName);
        outputAS.addAll(inputAS.get("Sentence"));
      }

      fireProcessFinished();
    }catch(ExecutionException ee){
      executionException = ee;
    }catch(Exception e){
      executionException = new ExecutionException(e);
    }
  }


  public void setTransducerURL(java.net.URL newTransducerURL) {
    transducerURL = newTransducerURL;
  }
  public java.net.URL getTransducerURL() {
    return transducerURL;
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

  DefaultGazetteer gazetteer;
  Transducer transducer;
  private java.net.URL transducerURL;
  private transient Vector statusListeners;
  private transient Vector progressListeners;
  private String encoding;
  private java.net.URL gazetteerListsURL;
  private gate.Document document;
  protected void fireStatusChanged(String e) {
    if (statusListeners != null) {
      Vector listeners = statusListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((StatusListener) listeners.elementAt(i)).statusChanged(e);
      }
    }
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
  public void setEncoding(String newEncoding) {
    encoding = newEncoding;
  }
  public String getEncoding() {
    return encoding;
  }
  public void setGazetteerListsURL(java.net.URL newGazetteerListsURL) {
    gazetteerListsURL = newGazetteerListsURL;
  }
  public java.net.URL getGazetteerListsURL() {
    return gazetteerListsURL;
  }
  public void setDocument(gate.Document newDocument) {
    document = newDocument;
  }
  public gate.Document getDocument() {
    return document;
  }
  public void setInputASName(String newInputASName) {
    inputASName = newInputASName;
  }
  public String getInputASName() {
    return inputASName;
  }
  public void setOutputASName(String newOutputASName) {
    outputASName = newOutputASName;
  }
  public String getOutputASName() {
    return outputASName;
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



  private static final boolean DEBUG = false;
  private String inputASName;
  private String outputASName;
}//public class SentenceSplitter extends Nerc