/*
 *  CorpusBenchmarkTool.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 24/Oct/2001
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
import gate.creole.tokeniser.*;
import gate.creole.gazetteer.*;
import gate.creole.splitter.*;
import gate.creole.orthomatcher.*;
import gate.annotation.*;

public class CorpusBenchmarkTool {
  private static final String MARKED_DIR_NAME = "marked";
  private static final String CLEAN_DIR_NAME = "clean";
  private static final String CVS_DIR_NAME = "Cvs";
  private static final String PROCESSED_DIR_NAME = "processed";

  private static final boolean DEBUG = true;

  public CorpusBenchmarkTool() {}

  public void initPRs() {
    try {
      //create a default tokeniser
      FeatureMap params = Factory.newFeatureMap();
      tokeniser = (DefaultTokeniser) Factory.createResource(
                      "gate.creole.tokeniser.DefaultTokeniser", params);

      //create a default gazetteer
      gazetteer = (DefaultGazetteer) Factory.createResource(
                      "gate.creole.gazetteer.DefaultGazetteer", params);
      //create a splitter
      splitter = (SentenceSplitter) Factory.createResource(
                      "gate.creole.splitter.SentenceSplitter", params);
      //create a tagger
      tagger = (POSTagger) Factory.createResource(
                      "gate.creole.POSTagger", params);
      //create a grammar
      transducer = (ANNIETransducer) Factory.createResource(
                      "gate.creole.ANNIETransducer", params);
      //create an orthomatcher
      orthomatcher = (OrthoMatcher) Factory.createResource(
                      "gate.creole.orthomatcher.OrthoMatcher", params);
    } catch (ResourceInstantiationException ex) {
      throw new GateRuntimeException("Corpus Benchmark Tool:"+ex.getMessage());
    }
  }//initPRs

  public void execute() {
    execute(startDir);
  }

  public void execute(File dir) {
    if (dir == null)
      return;
    //first set the current directory to be the given one
    currDir = dir;
    Out.prln("Processing directory: " + currDir);

    File processedDir = null;
    File cleanDir = null;
    File markedDir = null;

    ArrayList subDirs = new ArrayList();
    File[] dirArray = currDir.listFiles();
    for (int i = 0; i < dirArray.length; i++) {
      if (dirArray[i].isFile() || dirArray[i].getName().equals(CVS_DIR_NAME))
        continue;
      if (dirArray[i].getName().equals(CLEAN_DIR_NAME))
        cleanDir = dirArray[i];
      else if (dirArray[i].getName().equals(MARKED_DIR_NAME))
        markedDir = dirArray[i];
      else if (dirArray[i].getName().equals(PROCESSED_DIR_NAME))
        processedDir = dirArray[i];
      else
        subDirs.add(dirArray[i]);
    }

    if (this.isGenerateMode)
      generateCorpus(cleanDir, processedDir);
    else
      evaluateCorpus(cleanDir, processedDir, markedDir);

    //if no more subdirs left, return
    if (subDirs.isEmpty())
      return;

    //there are more subdirectories to traverse, so iterate through
    for (int j = 0; j < subDirs.size(); j++)
      execute((File) subDirs.get(j));

  }//execute(dir)


  public static void main(String[] args) throws GateException {
    Out.prln("Please wait while GATE tools are initialised.");
    // initialise GATE
    Gate.init();

    CorpusBenchmarkTool corpusTool = new CorpusBenchmarkTool();
    corpusTool.initPRs();

    // check we have a directory name or list of files
    List inputFiles = null;
    if(args.length < 1) throw new GateException(usage);
    int i = 0;
    if(args[0].equals("-generate")) {
      Out.prln("Generating the corpus...");
      corpusTool.setGenerateMode(true);
      i++;
    }
    String dirName = args[i];
    File dir = new File(dirName);
    if (!dir.isDirectory())
      throw new GateException(usage);

    corpusTool.setStartDirectory(dir);
    corpusTool.execute();

    Out.prln("Finished!");

    System.exit(0);

  }//main

  public void setGenerateMode(boolean mode) {
    isGenerateMode = mode;
  }//setGenerateMode

  public boolean getGenerateMode() {
    return isGenerateMode;
  }//getGenerateMode

  public boolean isGenerateMode() {
    return isGenerateMode == true;
  }//isGenerateMode

  public File getStartDirectory() {
    return startDir;
  }//getStartDirectory

  public void setStartDirectory(File dir) {
    startDir = dir;
  }//setStartDirectory

  protected void generateCorpus(File fileDir, File outputDir) {
    //1. check if we have input files
    if (fileDir == null)
      return;
    //2. create the output directory or clean it up if needed
    File outDir = outputDir;
    if (outputDir == null) {
      outDir = new File(currDir, PROCESSED_DIR_NAME);
      outDir.mkdir();
    } else {
      // get rid of the directory, coz datastore wants it clean
      if (Files.rmdir(outDir))
        outDir.mkdir(); // create an empty dir of same name
      else
        Out.prln("cannot delete old output directory: " + outDir);
    }

    //create the datastore and process each document
    try {
      SerialDataStore sds = new SerialDataStore(outDir.toURL().toString());
      sds.create();
      sds.open();

      File[] files = fileDir.listFiles();
      for (int i=0; i < files.length; i++) {
        if (!files[i].isFile())
          continue;
        // create a document
        if (DEBUG)
          Out.prln("Processing and storing document: " + files[i].toURL());

        FeatureMap params = Factory.newFeatureMap();
        params.put("sourceUrl", files[i].toURL());
        params.put("encoding", "");

        // create the document
        Document doc = (Document) Factory.createResource(
          "gate.corpora.DocumentImpl", params
        );

        doc.setName(files[i].getName());
        if (doc == null)
          continue;
        processDocument(doc);
        LanguageResource lr = sds.adopt(doc, null);
        sds.sync(lr);
      }//for
      sds.close();
    } catch (java.net.MalformedURLException ex) {
      throw new GateRuntimeException("CorpusBenchmark: " + ex.getMessage());
    } catch (PersistenceException ex1) {
      throw new GateRuntimeException("CorpusBenchmark: " + ex1.getMessage());
    } catch (ResourceInstantiationException ex2) {
      throw new GateRuntimeException("CorpusBenchmark: " + ex2.getMessage());
    } catch (gate.security.SecurityException ex3) {
      throw new GateRuntimeException("CorpusBenchmark: " + ex3.getMessage());
    }

  }//generateCorpus

  protected void evaluateCorpus(File fileDir,
                    File processedDir, File markedDir) {
    //1. check if we have input files and the processed Dir
    if (fileDir == null || processedDir == null)
      return;

    //looked for marked texts only if the directory exists
    boolean processMarked = markedDir != null && markedDir.exists();

    //open the datastore and process each document
    try {
      //open the data store
      DataStore sds = Factory.openDataStore
                      ("gate.persist.SerialDataStore", processedDir.toURL());

      List lrIDs = sds.getLrIds("gate.corpora.DocumentImpl");
      for (int i=0; i < lrIDs.size(); i++) {
        String docID = (String) lrIDs.get(i);

        //read the stored document
        FeatureMap features = Factory.newFeatureMap();
        features.put(DataStore.DATASTORE_FEATURE_NAME, sds);
        features.put(DataStore.LR_ID_FEATURE_NAME, docID);
        Document doc = (Document) Factory.createResource(
                                    "gate.corpora.DocumentImpl",
                                    features);

        if (DEBUG)
          Out.prln("\t Evaluating NE on: " + doc.getName());

        File cleanDocFile = new File(fileDir, doc.getName());
        Document cleanDoc = null;
        //try reading the original document from clean
        if (! cleanDocFile.exists()) {
          Out.prln("Warning: Cannot find original document " +
                   doc.getName() + " in " + fileDir);
        } else {
          FeatureMap params = Factory.newFeatureMap();
          params.put("sourceUrl", cleanDocFile.toURL());
          params.put("encoding", "");

          // create the document
          cleanDoc = (Document) Factory.createResource(
                                  "gate.corpora.DocumentImpl", params);
          cleanDoc.setName(doc.getName());
        }

        //try finding the marked document
        StringBuffer docName = new StringBuffer(doc.getName());
        docName.replace(
          doc.getName().lastIndexOf("."),
          docName.length(),
          ".xml");
        File markedDocFile = new File(markedDir, docName.toString());
        Document markedDoc = null;
        if (! processMarked || ! markedDocFile.exists()) {
          Out.prln("Warning: Cannot find human-annotated document " +
                   markedDocFile + " in " + markedDir);
        } else {
          FeatureMap params = Factory.newFeatureMap();
          params.put("sourceUrl", markedDocFile.toURL());
          params.put("encoding", "");

          // create the document
          //CHANGE TO markedDoc
          cleanDoc = (Document) Factory.createResource(
                                   "gate.corpora.DocumentImpl", params);
          cleanDoc.setName(doc.getName());
        }

        evaluateDocuments(doc, cleanDoc, markedDoc);

      }//for loop through saved docs
      sds.close();
    } catch (java.net.MalformedURLException ex) {
      throw new GateRuntimeException("CorpusBenchmark: " + ex.getMessage());
    } catch (PersistenceException ex1) {
      throw new GateRuntimeException("CorpusBenchmark: " + ex1.getMessage());
    } catch (ResourceInstantiationException ex2) {
      throw new GateRuntimeException("CorpusBenchmark: " + ex2.getMessage());
    }

  }//evaluateCorpus

  protected void processDocument(Document doc) {
    try {
      tokeniser.setDocument(doc);
      tokeniser.execute();

      gazetteer.setDocument(doc);
      gazetteer.execute();

      splitter.setDocument(doc);
      splitter.execute();

      tagger.setDocument(doc);
      tagger.execute();

      transducer.setDocument(doc);
      transducer.execute();

      orthomatcher.setDocument(doc);
      orthomatcher.execute();
    } catch (gate.creole.ExecutionException ex) {
      throw new GateRuntimeException("Corpus generation error: " +
                                     ex.getMessage());
    }
  }

  protected void evaluateDocuments(Document persDoc,
                    Document cleanDoc, Document markedDoc)
                        throws ResourceInstantiationException {
    if (cleanDoc == null && markedDoc == null)
      return;

    if (cleanDoc != null) {

      processDocument(cleanDoc);

      AnnotationDiff annotDiff=measureDocs(persDoc, cleanDoc, "Organization");

      if (annotDiff.getFMeasureAverage() != 1.0) {

        AnnotationDiff annotDiff1 = measureDocs(markedDoc, persDoc, "Organization");
        AnnotationDiff annotDiff2 = measureDocs(markedDoc, cleanDoc, "Organization");

        //check the precision first
        if (annotDiff.getPrecisionAverage() != 1.0) {
          Out.prln("\t\t Precision different from that of stored grammar for: " +
                  cleanDoc.getName() + " is " + annotDiff.getPrecisionAverage());
          if (annotDiff1 != null &&
              annotDiff2!= null &&
              annotDiff1.getPrecisionAverage()<annotDiff2.getPrecisionAverage()
             )
            Out.prln("\t\t\t Precision increase on human-marked from " +
                     annotDiff1.getPrecisionAverage() + " to " +
                     annotDiff2.getPrecisionAverage());
          else if (annotDiff1 != null && annotDiff2 != null)
            Out.prln("\t\t\t Precision decrease on human-marked from " +
                     annotDiff1.getPrecisionAverage() + " to " +
                     annotDiff2.getPrecisionAverage());
        }//if precision

        //check the recall now
        if (annotDiff.getRecallAverage() != 1.0) {
          Out.prln("\t\t Recall different from that of stored grammar for: " +
                  cleanDoc.getName() + " is " + annotDiff.getRecallAverage());
          if (annotDiff1 != null &&
              annotDiff2!= null &&
              annotDiff1.getRecallAverage()<annotDiff2.getRecallAverage()
             )
            Out.prln("\t\t\t Recall increase on human-marked from " +
                     annotDiff1.getRecallAverage() + " to " +
                     annotDiff2.getRecallAverage());
          else if (annotDiff1 != null && annotDiff2 != null)
            Out.prln("\t\t\t Recall decrease on human-marked from " +
                     annotDiff1.getRecallAverage() + " to " +
                     annotDiff2.getRecallAverage());

        }//if recall

      }//if the f-measure is not 1.0

    }//if clean doc can be processed

  }

  protected AnnotationDiff measureDocs(
    Document keyDoc, Document respDoc, String annotType)
      throws ResourceInstantiationException {

    if (keyDoc == null || respDoc == null)
      return null;

    // create the annotation schema needed for AnnotationDiff
    AnnotationSchema annotationSchema = new AnnotationSchema();

    // organization type
    annotationSchema.setAnnotationName(annotType);
    // create an annotation diff
    FeatureMap parameters = Factory.newFeatureMap();
    parameters.put("keyDocument",keyDoc);
    parameters.put("responseDocument",respDoc);
    parameters.put("annotationSchema",annotationSchema);
    parameters.put("keyAnnotationSetName",null);
    parameters.put("responseAnnotationSetName",null);
    //for a start, do not compare the features of the annotations
    parameters.put("keyFeatureNamesSet", new HashSet());

    // Create Annotation Diff visual resource
    AnnotationDiff annotDiff = (AnnotationDiff)
          Factory.createResource("gate.annotation.AnnotationDiff",parameters);

    return annotDiff;
  }

  /**
   * The directory from which we should generate/evaluate the corpus
   */
  private File startDir;
  private File currDir;

  private DefaultTokeniser tokeniser;
  private DefaultGazetteer gazetteer;
  private SentenceSplitter splitter;
  private POSTagger tagger;
  private ANNIETransducer transducer;
  private OrthoMatcher orthomatcher;

  /**
   * If true, the corpus tool will generate the corpus, otherwise it'll
   * run in evaluate mode
   */
  private boolean isGenerateMode = false;

  /** String to print when wrong command-line args */
  private static String usage =
    "usage: CorpusBenchmarkTool [-generate] directory-name";

}