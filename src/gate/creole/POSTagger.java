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
      //prepare the input for HepTag
      //define a comparator for annotations by start offset
      Comparator offsetComparator = new Comparator(){
        public int compare(Object o1,
                     Object o2){
          Annotation a1 = (Annotation)o1;
          Annotation a2 = (Annotation)o2;
          return a1.getStartNode().getOffset().compareTo(
                  a2.getStartNode().getOffset());
        }
      };
      AnnotationSet as = inputAS.get("Sentence");
      if(as != null && as.size() > 0){
        List sentences = new ArrayList(as);
        Collections.sort(sentences, offsetComparator);
        Iterator sentIter = sentences.iterator();
        while(sentIter.hasNext()){
          Annotation sentenceAnn = (Annotation)sentIter.next();
          AnnotationSet rangeSet = inputAS.get(
                                    sentenceAnn.getStartNode().getOffset(),
                                    sentenceAnn.getEndNode().getOffset());
          if(rangeSet == null) continue;
          AnnotationSet tokensSet = rangeSet.get("Token");
          if(tokensSet == null) continue;
          List tokens = new ArrayList(tokensSet);
          Collections.sort(tokens, offsetComparator);
          List sentence = new ArrayList();
          Iterator tokIter = tokens.iterator();
          //contains pairs (startOffset, endOffset)
          List locations = new ArrayList();
          //where will the next write in locations take place
          int i = 0;
          while(tokIter.hasNext()){
            Annotation token = (Annotation)tokIter.next();
            Long start = token.getStartNode().getOffset();
            Long end = token.getEndNode().getOffset();
            String text = document.getContent().getContent(start, end).
                                   toString();
            if(text.length() > 3 && text.endsWith("n't")){
              Long start1 = start;
              Long end1 = new Long(end.longValue() - 3);
              Long start2 = end1;
              Long end2 = end;
              sentence.add(text.substring(0, text.length() -3));
              locations.add(new Object[]{start1, end1});
              i++;
              sentence.add("n't");
              locations.add(new Object[]{start2, end2});
              i++;
            }else{
              sentence.add(text);
              locations.add(new Object[]{start, end});
              i++;
            }
          }//while(tokIter.hasNext())

          //run the POSTagger over this sentence
          List sentences4tagger = new ArrayList();
          sentences4tagger.add(sentence);
          List taggerResults = tagger.runTagger(sentences4tagger);
          //add the results to the output annotation set
          //we only get one sentence
          List sentenceFromTagger = (List)taggerResults.get(0);
          if(sentenceFromTagger.size() != locations.size()){
            throw new GateRuntimeException(
              "POS Tagger malfunction: the output size (" +
              sentenceFromTagger.size() +
              ") is different from the input size (" +
              locations.size() + ")!");
          }
          FeatureMap fm;
          for(i = 0; i< locations.size(); i++){
            fm = Factory.newFeatureMap();
            String category = ((String[])sentenceFromTagger.get(i))[1];
            fm.put("category", category);
            outputAS.add((Long)((Object[])locations.get(i))[0],
                         (Long)((Object[])locations.get(i))[1],
                         "POS",fm);
          }//for(i = 0; i<= locations.size(); i++)
        }//while(sentIter.hasNext())
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