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

package gate.creole;

import gate.creole.*;
import gate.*;
import gate.util.*;
import gate.jape.*;

import java.net.*;
import gate.event.*;
import java.util.*;
import java.io.*;

/**
 * A cascaded multi-phase transducer using the Jape language which is a
 * variant of the CPSL language.
 */
public class Transducer extends AbstractProcessingResource {

  /**
   * Default constructor. Does nothing apart from calling the default
   * constructor from the super class. The actual object initialisation is done
   * via the {@link #init} method.
   */
  public Transducer() {
//    if (! Main.batchMode){
      //fire events if not in batch mode
      sListener = new StatusListener(){
        public void statusChanged(String text){
          fireStatusChanged(text);
        }
      };

      pListener = new ProgressListener(){
        public void progressChanged(int value){
          fireProgressChanged(value);
        }

        public void processFinished(){
          fireProcessFinished();
        }
      };
//    }
  }

  /*
  private void writeObject(ObjectOutputStream oos) throws IOException {
    Out.prln("writing transducer");
    oos.defaultWriteObject();
    Out.prln("finished writing transducer");
  } // writeObject
  */

  /**
   * This method is the one responsible for initialising the transducer. It
   * assumes that all the needed parameters have been already set using the
   * appropiate setXXX() methods.
   *@return a reference to <b>this</b>
   */
  public Resource init() throws ResourceInstantiationException {
    if(grammarURL != null && encoding != null){
      try{
        if(sListener != null){
          batch = new Batch(grammarURL, encoding, sListener);
          }else{
            batch = new Batch(grammarURL, encoding);
          }
      }catch(JapeException je){
        throw new ResourceInstantiationException(je);
      }
    } else
      throw new ResourceInstantiationException (
        "Both the URL (was " + grammarURL + ") and the encoding (was " +
        encoding + ") are needed to create a JapeTransducer!"
      );

      if(pListener != null) batch.addProgressListener(pListener);

    return this;
  }

  /**
   * Implementation of the run() method from {@link java.lang.Runnable}.
   * This method is responsible for doing all the processing of the input
   * document.
   */
  public void execute() throws ExecutionException{
    if(document == null) throw new ExecutionException("No document provided!");
    if(inputASName != null && inputASName.equals("")) inputASName = null;
    if(outputASName != null && outputASName.equals("")) outputASName = null;
    try{
      batch.transduce(document,
                      inputASName == null ?
                        document.getAnnotations() :
                        document.getAnnotations(inputASName),
                      outputASName == null ?
                        document.getAnnotations() :
                        document.getAnnotations(outputASName));
    }catch(JapeException je){
      throw new ExecutionException(je);
    }
  }

  /**
   * Sets the grammar to be used for building this transducer.
   * @param newGrammarURL an URL to a file containing a Jape grammar.
   */
  public void setGrammarURL(java.net.URL newGrammarURL) {
    grammarURL = newGrammarURL;
  }

  /**
   * Gets the URL to the grammar used to build this transducer.
   * @return a {@link java.net.URL} pointing to the grammar file.
   */
  public java.net.URL getGrammarURL() {
    return grammarURL;
  }

  /**
   * Sets the document to be processed through this transducer.
   * @param a {@link gate.Document} to be processed.
   */
  public void setDocument(gate.Document newDocument) {
    document = newDocument;
  }

  /**
   * Gets the document currently set as target for this transducer.
   * @return a {@link gate.Document}
   */
  public gate.Document getDocument() {
    return document;
  }

  /**
   *
   * Sets the encoding to be used for reding the input file(s) forming the Jape
   * grammar. Note that if the input grammar is a multi-file one than the same
   * encoding will be used for reding all the files. Multi file grammars with
   * different encoding across the composing files are not supported!
   * @param newEncoding a {link String} representing the encoding.
   */
  public void setEncoding(String newEncoding) {
    encoding = newEncoding;
  }

  /**
   * Gets the encoding used for reding the grammar file(s).
   */
  public String getEncoding() {
    return encoding;
  }

  /**
   * Sets the {@link gate.AnnotationSet} to be used as input for the transducer.
   * @param newInputAS a {@link gate.AnnotationSet}
   */
  public void setInputASName(String newInputASName) {
    inputASName = newInputASName;
  }

  /**
   * Gets the {@link gate.AnnotationSet} used as input by this transducer.
   * @return a {@link gate.AnnotationSet}
   */
  public String getInputASName() {
    return inputASName;
  }

  /**
   * Sets the {@link gate.AnnotationSet} to be used as output by the transducer.
   * @param newOutputAS a {@link gate.AnnotationSet}
   */
  public void setOutputASName(String newOutputASName) {
    outputASName = newOutputASName;
  }

  /**
   * Gets the {@link gate.AnnotationSet} used as output by this transducer.
   * @return a {@link gate.AnnotationSet}
   */
  public String getOutputASName() {
    return outputASName;
  }
  public synchronized void removeStatusListener(StatusListener l) {
    if (statusListeners != null && statusListeners.contains(l)) {
      Vector v = (Vector) statusListeners.clone();
      v.removeElement(l);
      statusListeners = v;
    }
  }

  public synchronized void addStatusListener(StatusListener l) {
    Vector v =
      statusListeners == null
      ? new Vector(2)
      : (Vector) statusListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      statusListeners = v;
    }
  }

  /**
   * The URL to the jape file used as grammar by this transducer.
   */
  private java.net.URL grammarURL;

  /**
   * The {@link gate.Document} curently set as target for this transducer.
   */
  private gate.Document document;

  /**
   * The actual JapeTransducer used for processing the document(s).
   */
  private Batch batch;

  /**
   * The encoding used for reding the grammar file(s).
   */
  private String encoding;

  /**
   * The {@link gate.AnnotationSet} used as input for the transducer.
   */
  private String inputASName;

  /**
   * The {@link gate.AnnotationSet} used as output by the transducer.
   */
  private String outputASName;

  private StatusListener sListener;
  private ProgressListener pListener;
  private transient Vector statusListeners;
  private transient Vector progressListeners;

  /**
   * If the transducer uses the Appelt match style then this option will be
   * used to decide whether the longest match or the shortest will be used.
   */
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
}