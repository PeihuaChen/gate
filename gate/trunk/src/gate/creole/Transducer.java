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
public class Transducer extends AbstractLanguageAnalyser {

  public static final String
    TRANSD_DOCUMENT_PARAMETER_NAME = "document";

  public static final String
    TRANSD_INPUT_AS_PARAMETER_NAME = "inputASName";

  public static final String
    TRANSD_OUTPUT_AS_PARAMETER_NAME = "outputASName";

  public static final String
    TRANSD_ENCODING_PARAMETER_NAME = "encoding";

  public static final String
    TRANSD_GRAMMAR_URL_PARAMETER_NAME = "grammarURL";

  /**
   * Default constructor. Does nothing apart from calling the default
   * constructor from the super class. The actual object initialisation is done
   * via the {@link #init} method.
   */
  public Transducer() {
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
        fireProgressChanged(0);
        batch = new Batch(grammarURL, encoding, new InternalStatusListener());
        fireProcessFinished();
      }catch(JapeException je){
        throw new ResourceInstantiationException(je);
      }
    } else
      throw new ResourceInstantiationException (
        "Both the URL (was " + grammarURL + ") and the encoding (was " +
        encoding + ") are needed to create a JapeTransducer!"
      );

      batch.addProgressListener(new IntervalProgressListener(0, 100));

    return this;
  }

  /**
   * Implementation of the run() method from {@link java.lang.Runnable}.
   * This method is responsible for doing all the processing of the input
   * document.
   */
  public void execute() throws ExecutionException{
    interrupted = false;
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
   * Notifies all the PRs in this controller that they should stop their
   * execution as soon as possible.
   */
  public synchronized void interrupt(){
    interrupted = true;
    batch.interrupt();
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

  /**
   * The URL to the jape file used as grammar by this transducer.
   */
  private java.net.URL grammarURL;


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
}