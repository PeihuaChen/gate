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

import gate.*;
import gate.creole.*;
import gate.util.*;
import gate.event.*;

import hepple.postag.*;

import java.util.*;
import java.io.*;
import java.net.URL;
import java.text.NumberFormat;
/**
 * This class is a wrapper for HepTag, Mark Hepple's POS tagger.
 */
public class POSTagger extends AbstractProcessingResource {

  public POSTagger() {
  }

  public Resource init()throws ResourceInstantiationException{
    if(lexiconURL == null){
      throw new ResourceInstantiationException(
        "NoURL provided for the lexicon!");
    }
    if(rulesURL == null){
      throw new ResourceInstantiationException(
        "No URL provided for the rules!");
    }
    try{
      tagger = new hepple.postag.POSTagger(lexiconURL,rulesURL);
    }catch(Exception e){
      throw new ResourceInstantiationException(e);
    }
    return this;
  }


  public void run(){
    try{
      //check the parameters
      if(document == null) throw new GateRuntimeException(
        "No document to process!");
      if(inputASName != null && inputASName.equals("")) inputASName = null;
      if(outputASName != null && outputASName.equals("")) outputASName = null;
      AnnotationSet inputAS = (inputASName == null) ?
                              document.getAnnotations() :
                              document.getAnnotations(inputASName);
      AnnotationSet outputAS = (outputASName == null) ?
                               document.getAnnotations() :
                               document.getAnnotations(outputASName);

      fireStatusChanged("POS tagging " + document.getName());
      fireProgressChanged(0);
      //prepare the input for HepTag
      //define a comparator for annotations by start offset
      Comparator offsetComparator = new OffsetComparator();
      AnnotationSet as = inputAS.get("Sentence");
      if(as != null && as.size() > 0){
        List sentences = new ArrayList(as);
        Collections.sort(sentences, offsetComparator);
        Iterator sentIter = sentences.iterator();
        int sentIndex = 0;
        int sentCnt = sentences.size();
        long startTime= System.currentTimeMillis();
        while(sentIter.hasNext()){
          Annotation sentenceAnn = (Annotation)sentIter.next();
//          AnnotationSet rangeSet = inputAS.get(
//                                    sentenceAnn.getStartNode().getOffset(),
//                                    sentenceAnn.getEndNode().getOffset());
//          if(rangeSet == null) continue;
//          AnnotationSet tokensSet = rangeSet.get("Token");
//          if(tokensSet == null) continue;
//          List tokens = new ArrayList(tokensSet);
//          Collections.sort(tokens, offsetComparator);
          List tokens = (List)sentenceAnn.getFeatures().get("tokens");
          List sentence = new ArrayList(tokens.size());
          Iterator tokIter = tokens.iterator();
          while(tokIter.hasNext()){
            Annotation token = (Annotation)tokIter.next();
            String text = (String)token.getFeatures().get("string");
            sentence.add(text);
          }//while(tokIter.hasNext())

          //run the POSTagger over this sentence
          List sentences4tagger = new ArrayList();
          sentences4tagger.add(sentence);
          List taggerResults = tagger.runTagger(sentences4tagger);
          //add the results to the output annotation set
          //we only get one sentence
          List sentenceFromTagger = (List)taggerResults.get(0);
          if(sentenceFromTagger.size() != sentence.size()){
            throw new GateRuntimeException(
              "POS Tagger malfunction: the output size (" +
              sentenceFromTagger.size() +
              ") is different from the input size (" +
              sentence.size() + ")!");
          }
          for(int i = 0; i< sentence.size(); i++){
            String category = ((String[])sentenceFromTagger.get(i))[1];
            Annotation token = (Annotation)tokens.get(i);
            token.getFeatures().put("category", category);
          }//for(i = 0; i<= sentence.size(); i++)
          fireProgressChanged(sentIndex++ * 100 / sentCnt);
        }//while(sentIter.hasNext())

          fireProcessFinished();
          long endTime = System.currentTimeMillis();
          fireStatusChanged(document.getName() + " tagged in " +
                          NumberFormat.getInstance().format(
                          (double)(endTime - startTime) / 1000) + " seconds!");
      }else{
        throw new GateRuntimeException("No sentences to process!\n" +
                                       "Please run a sentence splitter first!");
      }//if(as != null && as.size() > 0)
    }catch(Exception e){
      executionException = new ExecutionException(e);
    }
  }


  public void setLexiconURL(java.net.URL newLexiconURL) {
    lexiconURL = newLexiconURL;
  }
  public java.net.URL getLexiconURL() {
    return lexiconURL;
  }
  public void setRulesURL(java.net.URL newRulesURL) {
    rulesURL = newRulesURL;
  }
  public java.net.URL getRulesURL() {
    return rulesURL;
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

  protected hepple.postag.POSTagger tagger;
  private java.net.URL lexiconURL;
  private java.net.URL rulesURL;
  private gate.Document document;
  private String inputASName;
  private String outputASName;
  private transient Vector statusListeners;
  private transient Vector progressListeners;
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