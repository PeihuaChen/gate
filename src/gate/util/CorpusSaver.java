/*
 *  CorpusSaver.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 22/Nov/2001
 *
 *  $Id$
 */

package gate.util;

import java.util.*;
import java.io.*;

import gate.*;
import gate.creole.*;
import gate.util.*;
import gate.persist.*;
import java.net.*;
import java.text.NumberFormat;

import gate.creole.tokeniser.*;
import gate.creole.gazetteer.*;
import gate.creole.splitter.*;
import gate.creole.orthomatcher.*;
import gate.creole.annotransfer.*;
import gate.creole.annotdelete.*;

public class CorpusSaver {

  private static final boolean DEBUG = true;

  public CorpusSaver() {
  }

  public void init() {
    File path = new File(dsPath);
    try {
     ds = Factory.openDataStore("gate.persist.SerialDataStore",
                                path.toURL().toString());
    } catch (Exception ex) {
      throw new gate.util.GateRuntimeException(ex.getMessage());
    }

    try {
      Corpus corpus = Factory.newCorpus("bnc");
      LanguageResource lr = ds.adopt(corpus, null);
      ds.sync(lr);
      theCorpus = (Corpus) lr;
    } catch (Exception ex) {
      throw new GateRuntimeException(ex.getMessage());
    }

    if (processMode)
      initPRs();

  }

  public void initPRs() {
    try {
      FeatureMap params = Factory.newFeatureMap();

      //create a default tokeniser
      Out.prln("Loading tokeniser <P>");
//      String rulesURL = this.configs.getProperty("tokeniserRulesURL");
//      if (rulesURL != null && !rulesURL.equals(""))
//        params.put("tokeniserRulesURL", rulesURL);
//      String grammarsURL = this.configs.getProperty("tokeniserGrammarURL");
//      if (grammarsURL != null && !grammarsURL.equals(""))
//        params.put("transducerGrammarURL", grammarsURL);
      //the annots are put in temp, as they are going to be transfered to the
      //new set
      params.put(DefaultTokeniser.DEF_TOK_ANNOT_SET_PARAMETER_NAME, "temp");
      tokeniser = (DefaultTokeniser) Factory.createResource(
                      "gate.creole.tokeniser.DefaultTokeniser", params);

      //create a default gazetteer
      Out.prln("Loading gazetteer <P>");
      params.clear();
//      String listsURL = this.configs.getProperty("gazetteerListsURL");
//      if (listsURL != null && !listsURL.equals(""))
//        params.put("listsURL", listsURL);
//      String caseSensitive = this.configs.getProperty("gazetteerCaseSensitive");
//      if (caseSensitive != null && !caseSensitive.equals(""))
//        params.put("caseSensitive", new Boolean(caseSensitive));
      params.put(DefaultGazetteer.DEF_GAZ_ANNOT_SET_PARAMETER_NAME, "temp");
      gazetteer = (DefaultGazetteer) Factory.createResource(
                      "gate.creole.gazetteer.DefaultGazetteer", params);

      //create the Annotation set transfer
      Out.prln("Loading annotation set transfer <P>");
      params.clear();
      params.put("inputASName", "temp");
      params.put("outputASName", annotSetName);
      //transfer only the annotations under the body tag (BNC spesific)
      setTransfer = (AnnotationSetTransfer) Factory.createResource(
                      "gate.creole.annotransfer.AnnotationSetTransfer", params);

      //create a splitter
      Out.prln("Loading sentence splitter <P>");
      params.clear();
//      listsURL = this.configs.getProperty("splitterGazetteerURL");
//      if (listsURL != null && !listsURL.equals(""))
//        params.put("gazetteerListsURL", listsURL);
//      grammarsURL = this.configs.getProperty("splitterGrammarURL");
//      if (grammarsURL != null && !grammarsURL.equals(""))
//        params.put("transducerURL", grammarsURL);
      params.put(SentenceSplitter.SPLIT_INPUT_AS_PARAMETER_NAME, annotSetName);
      params.put(SentenceSplitter.SPLIT_OUTPUT_AS_PARAMETER_NAME, annotSetName);
      splitter = (SentenceSplitter) Factory.createResource(
                      "gate.creole.splitter.SentenceSplitter", params);

      //create a tagger
      Out.prln("Loading POS tagger <P>");
      params.clear();
//      String lexiconURL = this.configs.getProperty("taggerLexiconURL");
//      if (lexiconURL != null && !lexiconURL.equals(""))
//        params.put("lexiconURL", lexiconURL);
//      rulesURL = this.configs.getProperty("taggerRulesURL");
//      if (rulesURL != null && !rulesURL.equals(""))
//        params.put("rulesURL", rulesURL);
      params.put(POSTagger.TAG_INPUT_AS_PARAMETER_NAME, annotSetName);
      params.put(POSTagger.TAG_OUTPUT_AS_PARAMETER_NAME, annotSetName);
      tagger = (POSTagger) Factory.createResource(
                      "gate.creole.POSTagger", params);

      //create a grammar
      Out.prln("Loading grammars for transducer <P>");
      params.clear();
//      String grammarURL = this.configs.getProperty("grammarURL");
//      if (grammarURL != null && !grammarURL.equals(""))
//        params.put("grammarURL", grammarURL);
      params.put(ANNIETransducer.TRANSD_INPUT_AS_PARAMETER_NAME, annotSetName);
      params.put(ANNIETransducer.TRANSD_OUTPUT_AS_PARAMETER_NAME, annotSetName);
      transducer = (ANNIETransducer) Factory.createResource(
                      "gate.creole.ANNIETransducer", params);

      //create an orthomatcher
      Out.prln("Loading orthomatcher <P>");
      params.clear();
      params.put(OrthoMatcher.OM_ANN_SET_PARAMETER_NAME, annotSetName);
      orthomatcher = (OrthoMatcher) Factory.createResource(
                      "gate.creole.orthomatcher.OrthoMatcher", params);

      Out.prln("Loading document reset PR <P>");
      params.clear();
      annotDeletePR = (AnnotationDeletePR) Factory.createResource(
                    "gate.creole.annotdelete.AnnotationDeletePR", params);
    } catch (ResourceInstantiationException ex) {
      throw new GateRuntimeException("Corpus Benchmark Tool:"+ex.getMessage());
    }
  }//initPRs

  public void execute() {
    execute(startDir);
    try {
      ds.sync(theCorpus);
      Factory.deleteResource(theCorpus);
      if(ds !=null)
        ds.close();
    } catch (Exception ex) {
      throw new GateRuntimeException(ex.getMessage());
    }
  }

  public void execute(File dir) {
    if (dir == null || ds == null)
      return;
    //first set the current directory to be the given one
    currDir = dir;
    Out.prln("Processing directory: " + currDir);

    ArrayList files = new ArrayList();
    ArrayList dirs = new ArrayList();
    File[] dirArray = currDir.listFiles();
    for (int i = 0; i < dirArray.length; i++) {
      if (dirArray[i].isDirectory())
        dirs.add(dirArray[i]);
      else if (dirArray[i].isFile())
        files.add(dirArray[i]);
    }

    saveFiles(files);

    //if no more subdirs left, return
    if (dirs.isEmpty())
      return;

    //there are more subdirectories to traverse, so iterate through
    for (int j = 0; j < dirs.size(); j++)
      execute((File) dirs.get(j));

  }//execute(dir)


  public static void main(String[] args) throws GateException {
    Gate.init();

    CorpusSaver corpusSaver1 = new CorpusSaver();

    if(args.length < 2)
      throw new GateException("usage: [-process] source_directory datastore_path");
    int i = 0;
    while (i < args.length && args[i].startsWith("-")) {
      if(args[i].equals("-process")) {
        Out.prln("ANNIE processing the corpus enabled. <P>");
        corpusSaver1.setProcessMode(true);
      }
      i++; //just ignore the option, which we do not recognise
    }//while

    String dirName = args[i];
    File dir = new File(dirName);
    if (!dir.isDirectory())
      throw new GateRuntimeException("Corpus directory should be "
                                     + "provided as a parameter");

    if(i+1 >= args.length)
      throw new GateRuntimeException("Datastore path not provided");

    String storagePath = args[i+1];
    File storage = new File(storagePath);
    if (!storage.isDirectory())
      throw new GateRuntimeException("Please provide path to an existing "
                                     + "GATE serial datastore");
    corpusSaver1.setDSPath(storagePath);

    corpusSaver1.init();
    corpusSaver1.setStartDir(dir);
    double timeBefore = System.currentTimeMillis();
    corpusSaver1.execute();
    double timeAfter = System.currentTimeMillis();
    Out.prln("BNC saved in " +
      NumberFormat.getInstance().format((timeAfter-timeBefore)/1000)
      + " seconds");

  }

  public void setStartDir(File newDir) {
    startDir = newDir;
  }

  public void setProcessMode(boolean mode) {
    processMode = mode;
  }

  public void setDSPath(String path){
    dsPath = path;
  }

  protected void saveFiles(List files) {
    if (files==null || files.isEmpty() || theCorpus == null || ds == null)
      return;

    for(int i=0; i<files.size(); i++) {
      try {
        Document doc = Factory.newDocument(((File)files.get(i)).toURL());
        doc.setName(Files.getLastPathComponent(((File)files.get(i)).toURL().toString()));
        Out.prln("Storing document: " + doc.getName());
        //first process it with ANNIE if in process mode
        if (processMode)
          processDocument(doc);
        //then store it in the DS and add to corpus
        LanguageResource lr = ds.adopt(doc, null);
        theCorpus.add(lr);
        theCorpus.unloadDocument((Document)lr);
        Factory.deleteResource(doc);
        if (lr != doc)
          Factory.deleteResource(lr);
      } catch (Exception ex) {
        throw new GateRuntimeException(ex.getClass() + " " + ex.getMessage());
      }
    }//for
  }//saveFiles

  protected void processDocument(Document doc) {
    try {
      tokeniser.setDocument(doc);
      tokeniser.execute();

      gazetteer.setDocument(doc);
      gazetteer.execute();

      setTransfer.setDocument(doc);
      String tagName = "text";
      AnnotationSet body = doc.getAnnotations(
                    GateConstants.ORIGINAL_MARKUPS_ANNOT_SET_NAME).get(tagName);
      if (body == null || body.isEmpty())
        tagName = "stext";
      body = doc.getAnnotations(
                    GateConstants.ORIGINAL_MARKUPS_ANNOT_SET_NAME).get(tagName);
      if (body == null || body.isEmpty())
        tagName = "body";
      setTransfer.setTextTagName(tagName);
      setTransfer.execute();

      splitter.setDocument(doc);
      splitter.execute();

      tagger.setDocument(doc);
      tagger.execute();

      transducer.setDocument(doc);
      transducer.execute();

      orthomatcher.setDocument(doc);
      orthomatcher.execute();

      annotDeletePR.setDocument(doc);
      List annotTypes = new ArrayList();
      annotTypes.add(ANNIEConstants.TOKEN_ANNOTATION_TYPE);
      annotTypes.add(ANNIEConstants.SPACE_TOKEN_ANNOTATION_TYPE);
      annotTypes.add("Unknown");
      annotTypes.add("TempIdentifier");
      annotTypes.add("Temp");
      annotTypes.add(ANNIEConstants.LOOKUP_ANNOTATION_TYPE);
      annotTypes.add("Split");
      annotDeletePR.setAnnotationTypes(annotTypes);
      annotDeletePR.execute();
    } catch (gate.creole.ExecutionException ex) {
      throw new GateRuntimeException("Corpus generation error: " +
                                     ex.getMessage());
    }
  }


  /**
   * The directory from which we should generate/evaluate the corpus
   */
  private File startDir;
  private File currDir;

  private DataStore ds;
  private Corpus theCorpus;
  private String annotSetName = "NE";
  private String dsPath = "d:\\bnc";

  private DefaultTokeniser tokeniser;
  private DefaultGazetteer gazetteer;
  private SentenceSplitter splitter;
  private POSTagger tagger;
  private ANNIETransducer transducer;
  private OrthoMatcher orthomatcher;
  private AnnotationSetTransfer setTransfer;
  private AnnotationDeletePR annotDeletePR;

  private boolean processMode = false;
}