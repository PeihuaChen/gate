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
public class POSTagger extends AbstractLanguageAnalyser {

  public static final String
    TAG_DOCUMENT_PARAMETER_NAME = "document";

  public static final String
    TAG_INPUT_AS_PARAMETER_NAME = "inputASName";

  public static final String
    TAG_LEXICON_URL_PARAMETER_NAME = "lexiconURL";

  public static final String
    TAG_RULES_URL_PARAMETER_NAME = "rulesURL";

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


  public void execute() throws ExecutionException{
    try{
      //check the parameters
      if(document == null) throw new GateRuntimeException(
        "No document to process!");
      if(inputASName != null && inputASName.equals("")) inputASName = null;
      AnnotationSet inputAS = (inputASName == null) ?
                              document.getAnnotations() :
                              document.getAnnotations(inputASName);


      AnnotationSet sentencesAS = inputAS.get(SENTENCE_ANNOTATION_TYPE);
      if(sentencesAS != null && sentencesAS.size() > 0){
        long startTime = System.currentTimeMillis();
        fireStatusChanged("POS tagging " + document.getName());
        fireProgressChanged(0);
        //prepare the input for HepTag
        List sentenceForTagger = new ArrayList();
        List sentencesForTagger = new ArrayList(1);
        sentencesForTagger.add(sentenceForTagger);

        //define a comparator for annotations by start offset
        Comparator offsetComparator = new OffsetComparator();

        //read all the tokens and all the sentences
        List sentencesList = new ArrayList(sentencesAS);
        Collections.sort(sentencesList, offsetComparator);
        List tokensList = new ArrayList(inputAS.get(TOKEN_ANNOTATION_TYPE));
        Collections.sort(tokensList, offsetComparator);

        Iterator sentencesIter = sentencesList.iterator();
        ListIterator tokensIter = tokensList.listIterator();

        List tokensInCurrentSentence = new ArrayList();
        Annotation currentToken = (Annotation)tokensIter.next();
        int sentIndex = 0;
        int sentCnt = sentencesAS.size();
        while(sentencesIter.hasNext()){
          Annotation currentSentence = (Annotation)sentencesIter.next();
          tokensInCurrentSentence.clear();
          sentenceForTagger.clear();
          while(currentToken != null
                &&
                currentToken.getEndNode().getOffset().compareTo(
                currentSentence.getEndNode().getOffset()) <= 0){
            tokensInCurrentSentence.add(currentToken);
            sentenceForTagger.add(currentToken.getFeatures().
                                  get(TOKEN_STRING_FEATURE_NAME));
            currentToken = (Annotation)(tokensIter.hasNext() ?
                                       tokensIter.next() : null);
          }
          //run the POS tagger
          List taggerResults = (List)tagger.runTagger(sentencesForTagger).get(0);
          //add the results
          //make sure no malfunction accured
          if(taggerResults.size() != tokensInCurrentSentence.size())
            throw new GateRuntimeException(
                "POS Tagger malfunction: the output size (" +
                taggerResults.size() +
                ") is different from the input size (" +
                tokensInCurrentSentence.size() + ")!");
          Iterator resIter = taggerResults.iterator();
          Iterator tokIter = tokensInCurrentSentence.iterator();
          while(resIter.hasNext()){
            ((Annotation)tokIter.next()).getFeatures().
              put(TOKEN_CATEGORY_FEATURE_NAME ,((String[])resIter.next())[1]);
          }
          fireProgressChanged(sentIndex++ * 100 / sentCnt);
        }//while(sentencesIter.hasNext())
        if(currentToken != null){
          //we have remaining tokens after the last sentence
          tokensInCurrentSentence.clear();
          sentenceForTagger.clear();
          while(currentToken != null){
            tokensInCurrentSentence.add(currentToken);
            sentenceForTagger.add(currentToken.getFeatures().
                                  get(TOKEN_STRING_FEATURE_NAME));
            currentToken = (Annotation)(tokensIter.hasNext() ?
                                        tokensIter.next() : null);
          }
          //run the POS tagger
          List taggerResults = (List)tagger.runTagger(sentencesForTagger).get(0);
          //add the results
          //make sure no malfunction accured
          if(taggerResults.size() != tokensInCurrentSentence.size())
            throw new GateRuntimeException(
                "POS Tagger malfunction: the output size (" +
                taggerResults.size() +
                ") is different from the input size (" +
                tokensInCurrentSentence.size() + ")!");
          Iterator resIter = taggerResults.iterator();
          Iterator tokIter = tokensInCurrentSentence.iterator();
          while(resIter.hasNext()){
            ((Annotation)tokIter.next()).getFeatures().
              put(TOKEN_CATEGORY_FEATURE_NAME ,((String[])resIter.next())[1]);
          }
        }//if(currentToken != null)
        fireProcessFinished();
        fireStatusChanged(
          document.getName() + " tagged in " +
          NumberFormat.getInstance().format(
          (double)(System.currentTimeMillis() - startTime) / 1000) +
          " seconds!");
      }else{
        throw new GateRuntimeException("No sentences to process!\n" +
                                       "Please run a sentence splitter first!");
      }

//OLD version
/*
      AnnotationSet as = inputAS.get(SENTENCE_ANNOTATION_TYPE);
      if(as != null && as.size() > 0){
        List sentences = new ArrayList(as);
        Collections.sort(sentences, offsetComparator);
        Iterator sentIter = sentences.iterator();
        int sentIndex = 0;
        int sentCnt = sentences.size();
        long startTime= System.currentTimeMillis();
        while(sentIter.hasNext()){
start = System.currentTimeMillis();
          Annotation sentenceAnn = (Annotation)sentIter.next();
          AnnotationSet rangeSet = inputAS.get(
                                    sentenceAnn.getStartNode().getOffset(),
                                    sentenceAnn.getEndNode().getOffset());
          if(rangeSet == null) continue;
          AnnotationSet tokensSet = rangeSet.get(TOKEN_ANNOTATION_TYPE);
          if(tokensSet == null) continue;
          List tokens = new ArrayList(tokensSet);
          Collections.sort(tokens, offsetComparator);

//          List tokens = (List)sentenceAnn.getFeatures().get("tokens");
          List sentence = new ArrayList(tokens.size());
          Iterator tokIter = tokens.iterator();
          while(tokIter.hasNext()){
            Annotation token = (Annotation)tokIter.next();
            String text = (String)token.getFeatures().get(TOKEN_STRING_FEATURE_NAME);
            sentence.add(text);
          }//while(tokIter.hasNext())

          //run the POSTagger over this sentence
          List sentences4tagger = new ArrayList(1);
          sentences4tagger.add(sentence);
prepTime += System.currentTimeMillis() - start;
start = System.currentTimeMillis();
          List taggerResults = tagger.runTagger(sentences4tagger);
posTime += System.currentTimeMillis() - start;
start = System.currentTimeMillis();
          //add the results to the output annotation set
          //we only get one sentence
          List sentenceFromTagger = (List)taggerResults.get(0);
          if(sentenceFromTagger.size() != sentence.size()){
            String taggerResult = "";
            for(int i = 0; i< sentenceFromTagger.size(); i++){
              taggerResult += ((String[])sentenceFromTagger.get(i))[1] + ", ";
            }
            throw new GateRuntimeException(
              "POS Tagger malfunction: the output size (" +
              sentenceFromTagger.size() +
              ") is different from the input size (" +
              sentence.size() + ")!" +
              "\n Input: " + sentence + "\nOutput: " + taggerResult);
          }
          for(int i = 0; i< sentence.size(); i++){
            String category = ((String[])sentenceFromTagger.get(i))[1];
            Annotation token = (Annotation)tokens.get(i);
            token.getFeatures().
              put(TOKEN_CATEGORY_FEATURE_NAME, category);
          }//for(i = 0; i<= sentence.size(); i++)
postTime += System.currentTimeMillis() - start;
          fireProgressChanged(sentIndex++ * 100 / sentCnt);
        }//while(sentIter.hasNext())
Out.prln("POS preparation time:" + prepTime);
Out.prln("POS execution time:" + posTime);
Out.prln("POS after execution time:" + postTime);
          fireProcessFinished();
          long endTime = System.currentTimeMillis();
          fireStatusChanged(document.getName() + " tagged in " +
                          NumberFormat.getInstance().format(
                          (double)(endTime - startTime) / 1000) + " seconds!");
      }else{
        throw new GateRuntimeException("No sentences to process!\n" +
                                       "Please run a sentence splitter first!");
      }//if(as != null && as.size() > 0)
*/
    }catch(Exception e){
      throw new ExecutionException(e);
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
  public void setInputASName(String newInputASName) {
    inputASName = newInputASName;
  }
  public String getInputASName() {
    return inputASName;
  }

  protected hepple.postag.POSTagger tagger;
  private java.net.URL lexiconURL;
  private java.net.URL rulesURL;
  private String inputASName;
}