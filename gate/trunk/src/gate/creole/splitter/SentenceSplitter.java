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
                  new IntervalProgressListener(0, 10));

    gazetteer = (DefaultGazetteer)Factory.createResource(
                    "gate.creole.gazetteer.DefaultGazetteer",
                    params, features, listeners, null);
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
                  new IntervalProgressListener(11, 100));

    transducer = (Transducer)Factory.createResource(
                    "gate.creole.Transducer",
                    params, features, listeners, null);
    transducer.setName("Transducer " + System.currentTimeMillis());

    fireProgressChanged(100);
    fireProcessFinished();

    return this;
  }

  public void execute() throws ExecutionException{
    //set the runtime parameters
    FeatureMap params;
    if(inputASName != null && inputASName.equals("")) inputASName = null;
    if(outputASName != null && outputASName.equals("")) outputASName = null;
    try{
      fireProgressChanged(0);
      params = Factory.newFeatureMap();
      params.put("document", document);
      params.put("annotationSetName", inputASName);
      gazetteer.setParameterValues(params);

      params = Factory.newFeatureMap();
      params.put("document", document);
      params.put("inputASName", inputASName);
      params.put("outputASName", inputASName);
      transducer.setParameterValues(params);
    }catch(Exception e){
      throw new ExecutionException(e);
    }
    ProgressListener pListener = null;
    StatusListener sListener = null;
    fireProgressChanged(5);

    //run the gazetteer
    pListener = new IntervalProgressListener(5, 10);
    sListener = new StatusListener(){
      public void statusChanged(String text){
        fireStatusChanged(text);
      }
    };
    gazetteer.addProgressListener(pListener);
    gazetteer.addStatusListener(sListener);
    gazetteer.execute();
    gazetteer.removeProgressListener(pListener);
    gazetteer.removeStatusListener(sListener);

    //run the transducer
    pListener = new IntervalProgressListener(11, 90);
    transducer.addProgressListener(pListener);
    transducer.addStatusListener(sListener);
    transducer.execute();
    transducer.removeProgressListener(pListener);
    transducer.removeStatusListener(sListener);

    //get pointers to the annotation sets
    AnnotationSet inputAS = (inputASName == null) ?
                            document.getAnnotations() :
                            document.getAnnotations(inputASName);

    AnnotationSet outputAS = (outputASName == null) ?
                             document.getAnnotations() :
                             document.getAnnotations(outputASName);

    //copy the results to the output set if they are different
    if(inputAS != outputAS){
      outputAS.addAll(inputAS.get("Sentence"));
    }
    fireProcessFinished();
  }//execute()


  public void setTransducerURL(java.net.URL newTransducerURL) {
    transducerURL = newTransducerURL;
  }
  public java.net.URL getTransducerURL() {
    return transducerURL;
  }
  DefaultGazetteer gazetteer;
  Transducer transducer;
  private java.net.URL transducerURL;
  private String encoding;
  private java.net.URL gazetteerListsURL;
  private gate.Document document;


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



  private static final boolean DEBUG = false;
  private String inputASName;
  private String outputASName;
}//public class SentenceSplitter extends Nerc