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

package gate.creole.transducer;

import gate.creole.*;
import gate.*;
import gate.util.*;
import gate.jape.*;

import java.net.*;


public class JapeTransducer extends AbstractProcessingResource {

  public JapeTransducer() {
  }

  public Resource init() throws ResourceInstantiationException {
    if(grammarURL != null && encoding != null){
      try{
        batch = new Batch(grammarURL, encoding);
      }catch(JapeException je){
        throw new ResourceInstantiationException(je);
      }
    }else throw new ResourceInstantiationException(
          "Both the URL (was " + grammarURL + ") and the encoding (was " +
          encoding + ") are needed to create a JapeTransducer!");
    return this;
  }

  public void run(){
    try{
      if(document == null) throw new ParameterException("No document provided!");
      if(inputAS == null && outputAS == null){
         batch.transduce(document);
      }else{
        if(inputAS == null || outputAS == null){
          throw new ParameterException("Either both inputAS (was " + inputAS +
                                       ") and outputAS (was " + outputAS +
                                       ") or neither of them need to be set!");
        }
        batch.transduce(document, inputAS, outputAS);
      }
    }catch(Exception e){
      executionException = new ExecutionException(e);
    }
  }

  public void setGrammarURL(java.net.URL newGrammarURL) {
    grammarURL = newGrammarURL;
  }
  public java.net.URL getGrammarURL() {
    return grammarURL;
  }
  public void setDocument(gate.Document newDocument) {
    document = newDocument;
  }
  public gate.Document getDocument() {
    return document;
  }
  public void setEncoding(String newEncoding) {
    encoding = newEncoding;
  }
  public String getEncoding() {
    return encoding;
  }
  public void setInputAS(gate.AnnotationSet newInputAS) {
    inputAS = newInputAS;
  }
  public gate.AnnotationSet getInputAS() {
    return inputAS;
  }
  public void setOutputAS(gate.AnnotationSet newOutputAS) {
    outputAS = newOutputAS;
  }
  public gate.AnnotationSet getOutputAS() {
    return outputAS;
  }
  private java.net.URL grammarURL;
  private gate.Document document;
  private Batch batch;
  private String encoding;
  private gate.AnnotationSet inputAS;
  private gate.AnnotationSet outputAS;

}