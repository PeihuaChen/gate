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
public class SentenceSplitter extends Nerc{

  public Resource init()throws ResourceInstantiationException {
    //create all the componets
    FeatureMap params;
    FeatureMap features;
    Map listeners = new HashMap();
    listeners.put("gate.event.StatusListener", new StatusListener(){
      public void statusChanged(String text){
        fireStatusChanged(text);
      }
    });

    //tokeniser
    fireStatusChanged("Creating a tokeniser");
    params = Factory.newFeatureMap();
//      rData = (ResourceData)Gate.getCreoleRegister().get(
//              "gate.creole.tokeniser.DefaultTokeniser");
//      params.putAll(rData.getParameterList().getInitimeDefaults());
    if(tokeniserRulesURL != null) params.put("rulesURL",
                                             tokeniserRulesURL);
    params.put("encoding", encoding);
    if(DEBUG) Out.prln("Parameters for the tokeniser: \n" + params);
    features = Factory.newFeatureMap();
    Gate.setHiddenAttribute(features, true);
    tokeniser = (DefaultTokeniser)Factory.createResource(
                    "gate.creole.tokeniser.DefaultTokeniser",
                    params, features, listeners);
    this.add(tokeniser);
    tokeniser.setName("Tokeniser " + System.currentTimeMillis());
    fireProgressChanged(10);

    //gazetteer
    fireStatusChanged("Creating a gazetteer");
    params = Factory.newFeatureMap();
    params.put("caseSensitive", new Boolean(false));
    if(gazetteerListsURL != null) params.put("listsURL",
                                             gazetteerListsURL);
    params.put("encoding", encoding);
    if(DEBUG) Out.prln("Parameters for the gazetteer: \n" + params);
    features = Factory.newFeatureMap();
    Gate.setHiddenAttribute(features, true);

    listeners.put("gate.event.ProgressListener",
                  new CustomProgressListener(11, 50));

    gazetteer = (DefaultGazetteer)Factory.createResource(
                    "gate.creole.gazetteer.DefaultGazetteer",
                    params, features, listeners);
    this.add(gazetteer);
    gazetteer.setName("Gazetteer " + System.currentTimeMillis());
    fireProgressChanged(50);

    //transducer
    fireStatusChanged("Creating a Jape transducer");
    params = Factory.newFeatureMap();
//      rData = (ResourceData)Gate.getCreoleRegister().get(
//              "gate.creole.Transducer");
//      params.putAll(rData.getParameterList().getInitimeDefaults());
    if(japeGrammarURL != null) params.put("grammarURL",
                                          japeGrammarURL);
    params.put("encoding", encoding);
    if(DEBUG) Out.prln("Parameters for the transducer: \n" + params);
    features = Factory.newFeatureMap();
    Gate.setHiddenAttribute(features, true);
    listeners.put("gate.event.ProgressListener",
                  new CustomProgressListener(11, 50));
    transducer = (Transducer)Factory.createResource("gate.creole.Transducer",
                                                    params, features,
                                                    listeners);
    fireProgressChanged(100);
    fireProcessFinished();
    this.add(transducer);
    transducer.setName("Transducer " + System.currentTimeMillis());
    return this;
  } // init()

  public void run(){
    try{
      super.runSystem();
      super.check();
      //copy the sentence annotations to the outputSet
      if(outputASName != null && outputASName.equals("")) outputASName = null;
      AnnotationSet outputAS = (outputASName == null) ?
                               document.getAnnotations() :
                               document.getAnnotations(outputASName);
      if(tempAnnotationSetName != null &&
         tempAnnotationSetName.equals("")) tempAnnotationSetName = null;
      AnnotationSet tempAS =  (tempAnnotationSetName == null) ?
                               document.getAnnotations() :
                               document.getAnnotations(tempAnnotationSetName);

      if(outputAS != tempAS){
        //we need to copy the sentence annotations.
        outputAS.addAll(tempAS.get("Sentence"));
      }
    }catch(ExecutionException ee){
      executionException = ee;
    }catch(Exception e){
      executionException = new ExecutionException(e);
    }
  }//run()

  public void run1(){
    try{
      super.runSystem();
      super.check();
      //create the sentence annotations
      if(outputASName != null && outputASName.equals("")) outputASName = null;
      AnnotationSet outputAS = (outputASName == null) ?
                               document.getAnnotations() :
                               document.getAnnotations(outputASName);

      if(tempAnnotationSetName != null &&
         tempAnnotationSetName.equals("")) tempAnnotationSetName = null;
      AnnotationSet tempAS =  (tempAnnotationSetName == null) ?
                               document.getAnnotations() :
                               document.getAnnotations(tempAnnotationSetName);

      //get a list of splitters
      ArrayList splitters = new ArrayList(tempAS.get("Split"));
      if(splitters.size() > 0){
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
        //sort the splitters by offset
        Collections.sort(splitters, offsetComparator);
        long startSentence = 0;
        long endSentence = startSentence;

        Iterator splitIter = splitters.iterator();
        while(splitIter.hasNext()){
          Annotation aSplitter = (Annotation)splitIter.next();
          endSentence = aSplitter.getEndNode().getOffset().longValue();
          //generate the new sentence annotation
          FeatureMap sentenceFeatures = Factory.newFeatureMap();

          //get a list of tokens
          AnnotationSet as = tempAS.get(new Long(startSentence),
                                        new Long(endSentence)).
                                        get("Token");
          if(as != null && as.size() > 0){
            ArrayList tokens = new ArrayList(as);
            //sort the tokens by offset
            Collections.sort(tokens, offsetComparator);
            sentenceFeatures.put("Tokens", tokens);
            try{
              outputAS.add(new Long(((Annotation)tokens.get(0)).
                                    getStartNode().getOffset().longValue()),
                           new Long(endSentence),
                           "Sentence", sentenceFeatures);
            }catch(InvalidOffsetException ioe){
              throw new ExecutionException(ioe);
            }
          }//if(as != null && as.size() > 0)

          startSentence = endSentence;
        }//while(splitIter.hasNext())
      }//if(splitters.size() > 0 && tokens.size() > 0)
    }catch(ExecutionException ee){
      executionException = ee;
    }catch(Exception e){
      executionException = new ExecutionException(e);
    }
  }//run()

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
  protected String outputASName;
  private static final boolean DEBUG = false;
}//public class SentenceSplitter extends Nerc