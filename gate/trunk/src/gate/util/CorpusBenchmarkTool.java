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
      Out.prln("Loading tokeniser <P>");
      FeatureMap params = Factory.newFeatureMap();
      params.put("annotationSetName", annotSetName);
      tokeniser = (DefaultTokeniser) Factory.createResource(
                      "gate.creole.tokeniser.DefaultTokeniser", params);

      //create a default gazetteer
      Out.prln("Loading gazetteer <P>");
      gazetteer = (DefaultGazetteer) Factory.createResource(
                      "gate.creole.gazetteer.DefaultGazetteer", params);

      //create a splitter
      Out.prln("Loading sentence splitter <P>");
      params.clear();
      params.put("inputASName", annotSetName);
      params.put("outputASName", annotSetName);
      splitter = (SentenceSplitter) Factory.createResource(
                      "gate.creole.splitter.SentenceSplitter", params);

      //create a tagger
      Out.prln("Loading POS tagger <P>");
      tagger = (POSTagger) Factory.createResource(
                      "gate.creole.POSTagger", params);

      //create a grammar
      Out.prln("Loading grammars for transducer <P>");
      transducer = (ANNIETransducer) Factory.createResource(
                      "gate.creole.ANNIETransducer", params);

      //create an orthomatcher
      Out.prln("Loading orthomatcher <P>");
      params.clear();
      params.put("annotationSetName", annotSetName);
      orthomatcher = (OrthoMatcher) Factory.createResource(
                      "gate.creole.orthomatcher.OrthoMatcher", params);
    } catch (ResourceInstantiationException ex) {
      throw new GateRuntimeException("Corpus Benchmark Tool:"+ex.getMessage());
    }
  }//initPRs

  public void execute() {
    execute(startDir);
  }

  public void init() {
    //we only initialise the PRs if they are going to be used
    //for processing unprocessed documents
    if (!this.isMarkedStored)
      initPRs();

    annotTypes = new ArrayList();
    annotTypes.add("Organization");
    annotTypes.add("Person");
    annotTypes.add("Date");
    annotTypes.add("Location");
    annotTypes.add("Address");
    annotTypes.add("Money");
    annotTypes.add("Percent");
  }

  public void execute(File dir) {
    if (dir == null)
      return;
    //first set the current directory to be the given one
    currDir = dir;
    Out.prln("Processing directory: " + currDir + "<P>");

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
    Out.prln("<HTML>");
    Out.prln("<HEAD>");
    Out.prln("<TITLE> Corpus benchmark tool: ran with args " +
            args.toString() + " on " +
            new Date() + "</TITLE> </HEAD>");
    Out.prln("<BODY>");
    Out.prln("Please wait while GATE tools are initialised. <P>");
    // initialise GATE
    Gate.init();

    CorpusBenchmarkTool corpusTool = new CorpusBenchmarkTool();

    List inputFiles = null;
    if(args.length < 1) throw new GateException(usage);
    int i = 0;
    while (i < args.length && args[i].startsWith("-")) {
      if(args[i].equals("-generate")) {
        Out.prln("Generating the corpus... <P>");
        corpusTool.setGenerateMode(true);
      } else if (args[i].equals("-marked_clean")) {
        Out.prln("Evaluating current grammars against human-annotated...<P>");
        corpusTool.setMarkedClean(true);
      } else if (args[i].equals("-marked_stored")) {
        Out.prln("Evaluating stored documents against human-annotated...<P>");
        corpusTool.setMarkedStored(true);
      } else if (args[i].equals("-verbose")) {
        Out.prln("Running in verbose mode. Will generate annotation " +
          "information when precision/recall are lower than " +
          corpusTool.getThreshold() +"<P>");
        corpusTool.setVerboseMode(true);
      }
      i++; //just ignore the option, which we do not recognise
    }//while

    String dirName = args[i];
    File dir = new File(dirName);
    if (!dir.isDirectory())
      throw new GateException(usage);

    corpusTool.init();

    corpusTool.setStartDirectory(dir);
    corpusTool.execute();

    //if we're not generating the corpus, then print the precision and recall
    //statistics for the processed corpus
    if (! corpusTool.getGenerateMode())
      corpusTool.printStatistics();

    Out.prln("Finished! <P>");
    Out.prln("</BODY>");
    Out.prln("</HTML>");

    System.exit(0);

  }//main

  public void setGenerateMode(boolean mode) {
    isGenerateMode = mode;
  }//setGenerateMode

  public boolean getGenerateMode() {
    return isGenerateMode;
  }//getGenerateMode

  public boolean getVerboseMode() {
    return isVerboseMode;
  }//getVerboseMode

  public void setVerboseMode(boolean mode) {
    isVerboseMode = mode;
  }//setVerboseMode

  public void setMarkedStored(boolean mode) {
    isMarkedStored = mode;
  }//

  public boolean getMarkedStored() {
    return isMarkedStored;
  }//

  public void setMarkedClean(boolean mode) {
    isMarkedClean = mode;
  }//

  public boolean getMarkedClean() {
    return isMarkedClean;
  }//

  /**
   * Returns the average precision over the entire set of processed documents.
   * <P>
   * If the tool has been evaluating the original documents against the
   * previously-stored automatically annotated ones, then the precision
   * will be the average precision on those two sets. <P>
   * If the tool was run in -marked mode, i.e., was evaluating the stored
   * automatically processed ones against the human-annotated ones, then
   * the precision will be the average precision on those two sets of documents.
   */
  public double getPrecisionAverage() {
    return precisionSum/docNumber;
  }

  /**
   * Returns the average recall over the entire set of processed documents.
   * <P>
   * If the tool has been evaluating the original documents against the
   * previously-stored automatically annotated ones, then the recall
   * will be the average recall on those two sets. <P>
   * If the tool was run in -marked mode, i.e., was evaluating the stored
   * automatically processed ones against the human-annotated ones, then
   * the recall will be the average recall on those two sets of documents.
   */
  public double getRecallAverage() {
    return recallSum/docNumber;
  }

  public boolean isGenerateMode() {
    return isGenerateMode == true;
  }//isGenerateMode

  public double getThreshold() {
    return threshold;
  }

  public void setThreshold(double newValue) {
    threshold = newValue;
  }

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
    } else {
      // get rid of the directory, coz datastore wants it clean
      if (!Files.rmdir(outDir))
        Out.prln("cannot delete old output directory: " + outDir);
    }
    outDir.mkdir();

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
        Out.prln("Processing and storing document: " + files[i].toURL() +"<P>");

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
    if (fileDir == null || !fileDir.exists())
      return;
    if (processedDir == null || !processedDir.exists())
      //if the user wants evaluation of marked and stored that's not possible
      if (isMarkedStored) {
        Out.prln("Cannot evaluate because no processed documents exist.");
        return;
      }
      else
        isMarkedClean = true;

    //looked for marked texts only if the directory exists
    boolean processMarked = markedDir != null && markedDir.exists();
    if (!processMarked && (isMarkedStored || isMarkedClean)) {
        Out.prln("Cannot evaluate because no human-annotated documents exist.");
        return;
    }

    if (isMarkedStored) {
      evaluateMarkedStored(markedDir, processedDir);
      return;
    } else if (isMarkedClean) {
      evaluateMarkedClean(markedDir, fileDir);
      return;
    }

    Document persDoc = null;
    Document cleanDoc = null;
    Document markedDoc = null;

    //open the datastore and process each document
    try {
      //open the data store
      DataStore sds = Factory.openDataStore
                      ("gate.persist.SerialDataStore",
                       processedDir.toURL().toExternalForm());

      List lrIDs = sds.getLrIds("gate.corpora.DocumentImpl");
      for (int i=0; i < lrIDs.size(); i++) {
        String docID = (String) lrIDs.get(i);

        //read the stored document
        FeatureMap features = Factory.newFeatureMap();
        features.put(DataStore.DATASTORE_FEATURE_NAME, sds);
        features.put(DataStore.LR_ID_FEATURE_NAME, docID);
        persDoc = (Document) Factory.createResource(
                                    "gate.corpora.DocumentImpl",
                                    features);

        Out.prln("<H2>" + persDoc.getName() + "</H2>");

        File cleanDocFile = new File(fileDir, persDoc.getName());
        //try reading the original document from clean
        if (! cleanDocFile.exists()) {
          Out.prln("Warning: Cannot find original document " +
                   persDoc.getName() + " in " + fileDir);
        } else {
          FeatureMap params = Factory.newFeatureMap();
          params.put("sourceUrl", cleanDocFile.toURL());
          params.put("encoding", "");

          // create the document
          cleanDoc = (Document) Factory.createResource(
                                  "gate.corpora.DocumentImpl", params);
          cleanDoc.setName(persDoc.getName());
        }

        //try finding the marked document
        StringBuffer docName = new StringBuffer(persDoc.getName());
        docName.replace(
          persDoc.getName().lastIndexOf("."),
          docName.length(),
          ".xml");
        File markedDocFile = new File(markedDir, docName.toString());
        if (! processMarked || ! markedDocFile.exists()) {
          Out.prln("Warning: Cannot find human-annotated document " +
                   markedDocFile + " in " + markedDir);
        } else {
          FeatureMap params = Factory.newFeatureMap();
          params.put("sourceUrl", markedDocFile.toURL());
          params.put("encoding", "");

          // create the document
          markedDoc = (Document) Factory.createResource(
                                   "gate.corpora.DocumentImpl", params);
          markedDoc.setName(persDoc.getName());
        }

        evaluateDocuments(persDoc, cleanDoc, markedDoc);
        if (persDoc != null)
          Factory.deleteResource(persDoc);
        if (cleanDoc != null)
          Factory.deleteResource(cleanDoc);
        if (markedDoc != null)
          Factory.deleteResource(markedDoc);

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

  protected void evaluateMarkedStored(File markedDir, File storedDir) {
    Document persDoc = null;
    Document cleanDoc = null;
    Document markedDoc = null;

    //open the datastore and process each document
    try {
      //open the data store
      DataStore sds = Factory.openDataStore
                      ("gate.persist.SerialDataStore",
                       storedDir.toURL().toExternalForm());

      List lrIDs = sds.getLrIds("gate.corpora.DocumentImpl");
      for (int i=0; i < lrIDs.size(); i++) {
        String docID = (String) lrIDs.get(i);

        //read the stored document
        FeatureMap features = Factory.newFeatureMap();
        features.put(DataStore.DATASTORE_FEATURE_NAME, sds);
        features.put(DataStore.LR_ID_FEATURE_NAME, docID);
        persDoc = (Document) Factory.createResource(
                                    "gate.corpora.DocumentImpl",
                                    features);

        Out.prln("<H2>" + persDoc.getName() + "</H2>");

        //try finding the marked document
        StringBuffer docName = new StringBuffer(persDoc.getName());
        docName.replace(
          persDoc.getName().lastIndexOf("."),
          docName.length(),
          ".xml");
        File markedDocFile = new File(markedDir, docName.toString());
        if (! markedDocFile.exists()) {
          Out.prln("Warning: Cannot find human-annotated document " +
                   markedDocFile + " in " + markedDir);
        } else {
          FeatureMap params = Factory.newFeatureMap();
          params.put("sourceUrl", markedDocFile.toURL());
          params.put("encoding", "");

          // create the document
          markedDoc = (Document) Factory.createResource(
                                   "gate.corpora.DocumentImpl", params);
          markedDoc.setName(persDoc.getName());
        }

        evaluateDocuments(persDoc, cleanDoc, markedDoc);
        if (persDoc != null)
          Factory.deleteResource(persDoc);
        if (markedDoc != null)
          Factory.deleteResource(markedDoc);

      }//for loop through saved docs
      sds.close();

    } catch (java.net.MalformedURLException ex) {
      throw new GateRuntimeException("CorpusBenchmark: " + ex.getMessage());
    } catch (PersistenceException ex1) {
      throw new GateRuntimeException("CorpusBenchmark: " + ex1.getMessage());
    } catch (ResourceInstantiationException ex2) {
      throw new GateRuntimeException("CorpusBenchmark: " + ex2.getMessage());
    }

  }//evaluateMarkedStored


  protected void evaluateMarkedClean(File markedDir, File cleanDir) {
    Document persDoc = null;
    Document cleanDoc = null;
    Document markedDoc = null;

    File[] cleanDocs = cleanDir.listFiles();
    for (int i = 0; i< cleanDocs.length; i++) {
      if (!cleanDocs[i].isFile())
        continue;

      //try reading the original document from clean
      FeatureMap params = Factory.newFeatureMap();
      try {
        params.put("sourceUrl", cleanDocs[i].toURL());
      } catch (java.net.MalformedURLException ex) {
        Out.prln("Cannot create document from file: " +
          cleanDocs[i].getAbsolutePath());
        continue;
      }
      params.put("encoding", "");

      // create the document
      try {
        cleanDoc = (Document) Factory.createResource(
                              "gate.corpora.DocumentImpl", params, null,
                              null, cleanDocs[i].getName());
      } catch (gate.creole.ResourceInstantiationException ex) {
        Out.prln("Cannot create document from file: " +
          cleanDocs[i].getAbsolutePath());
        continue;
      }

      Out.prln("<TD>" + cleanDocs[i].getName() + "</TD>");

      //try finding the marked document
      StringBuffer docName = new StringBuffer(cleanDoc.getName());
      docName.replace(
        cleanDoc.getName().lastIndexOf("."),
        docName.length(),
        ".xml");
      File markedDocFile = new File(markedDir, docName.toString());
      if (! markedDocFile.exists()) {
        Out.prln("Warning: Cannot find human-annotated document " +
                 markedDocFile + " in " + markedDir);
        continue;
      } else {
        params = Factory.newFeatureMap();
        try {
          params.put("sourceUrl", markedDocFile.toURL());
        } catch (java.net.MalformedURLException ex) {
          Out.prln("Cannot create document from file: " +
            markedDocFile.getAbsolutePath());
          continue;
        }
        params.put("encoding", "");

        // create the document
        try {
          markedDoc = (Document) Factory.createResource(
                                 "gate.corpora.DocumentImpl", params,
                                 null, null, cleanDoc.getName());
        } catch (gate.creole.ResourceInstantiationException ex) {
          Out.prln("Cannot create document from file: " +
            markedDocFile.getAbsolutePath());
          continue;
        }

      }//if markedDoc exists

      try {
        evaluateDocuments(persDoc, cleanDoc, markedDoc);
      } catch (gate.creole.ResourceInstantiationException ex) {
        Out.prln("Evaluate failed on document: " + cleanDoc.getName());
      }
      if (persDoc != null)
        Factory.deleteResource(persDoc);
      if (cleanDoc != null)
        Factory.deleteResource(cleanDoc);
      if (markedDoc != null)
        Factory.deleteResource(markedDoc);

    }//for loop through clean docs


  }//evaluateMarkedClean

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

    //we've got no types to compare
    if (annotTypes == null || annotTypes.isEmpty())
      return;

    if (cleanDoc != null && !isMarkedStored) {

      processDocument(cleanDoc);

      if(!isMarkedClean)
        evaluateAllThree(persDoc, cleanDoc, markedDoc);
      else
        evaluateTwoDocs(markedDoc, cleanDoc);

    } else
      evaluateTwoDocs(markedDoc, persDoc);

  }

  protected void evaluateAllThree(Document persDoc,
                                  Document cleanDoc, Document markedDoc)
                                  throws ResourceInstantiationException {
    //first start the table and its header
    printTableHeader();
    for (int jj= 0; jj< annotTypes.size(); jj++) {
      String annotType = (String) annotTypes.get(jj);

      AnnotationDiff annotDiff=measureDocs(persDoc, cleanDoc, annotType);
      //we don't have this annotation type in this document
      if (annotDiff == null)
        continue;
      Out.prln("<TR>");

      //increase the number of processed documents
      docNumber++;
      //add precison and recall to the sums
      updateStatistics(annotDiff, annotType);

      Out.prln("<TD> Annotation type: " + annotType + "</TD>");

      AnnotationDiff annotDiff1 =
        measureDocs(markedDoc, persDoc, annotType);
      AnnotationDiff annotDiff2 =
        measureDocs(markedDoc, cleanDoc, annotType);

      Out.prln("<TD>" + annotDiff.getPrecisionAverage()
              + "</TD>");
      //check the precision first
      if (annotDiff.getPrecisionAverage() != 1.0) {
        if (annotDiff1 != null &&
            annotDiff2!= null &&
            annotDiff1.getPrecisionAverage()<annotDiff2.getPrecisionAverage()
           )
          Out.prln("<P> Precision increase on human-marked from " +
                   annotDiff1.getPrecisionAverage() + " to " +
                   annotDiff2.getPrecisionAverage() + "</P>");
        else if (annotDiff1 != null && annotDiff2 != null)
          Out.prln("<P> Precision decrease on human-marked from " +
                   annotDiff1.getPrecisionAverage() + " to " +
                   annotDiff2.getPrecisionAverage() + "</P>");
      }//if precision

      Out.prln("<TD>" + annotDiff.getRecallAverage() + "</TD>");
      //check the recall now
      if (annotDiff.getRecallAverage() != 1.0) {
        if (annotDiff1 != null &&
            annotDiff2!= null &&
            annotDiff1.getRecallAverage()<annotDiff2.getRecallAverage()
           )
          Out.prln("<P> Recall increase on human-marked from " +
                   annotDiff1.getRecallAverage() + " to " +
                   annotDiff2.getRecallAverage() + "</P>");
        else if (annotDiff1 != null && annotDiff2 != null)
          Out.prln("<P> Recall decrease on human-marked from " +
                   annotDiff1.getRecallAverage() + " to " +
                   annotDiff2.getRecallAverage() + "</P>");

      }//if recall

      Out.prln("</TR>");
    }//for loop through annotation types
    Out.prln("</TABLE>");

  }//evaluateAllThree

  protected void evaluateTwoDocs(Document keyDoc, Document respDoc)
        throws ResourceInstantiationException {

    //first start the table and its header
    printTableHeader();
    for (int jj= 0; jj< annotTypes.size(); jj++) {
      String annotType = (String) annotTypes.get(jj);

      AnnotationDiff annotDiff=measureDocs(keyDoc, respDoc, annotType);
      //we don't have this annotation type in this document
      if (annotDiff == null)
        continue;
      Out.prln("<TR>");

      //increase the number of processed documents
      docNumber++;
      //add precison and recall to the sums
      updateStatistics(annotDiff, annotType);

      Out.prln("<TD>" + annotType + "</TD>");

      Out.prln("<TD>" + annotDiff.getPrecisionAverage() + "</TD>");
      Out.prln("<TD>" + annotDiff.getRecallAverage() + "</TD>");
      //check the recall now
      if ( isVerboseMode
           &&
           ((annotDiff.getRecallAverage() < threshold
             ||
             annotDiff.getRecallAverage() < threshold)
           )
         )
        printAnnotations(annotDiff, keyDoc, respDoc);

      Out.prln("</TR>");
    }//for loop through annotation types
    Out.prln("</TABLE>");

  }//evaluateTwoDocs

  protected void printTableHeader() {
    Out.prln("<TABLE BORDER=1");
    if (isVerboseMode)
      Out.prln("<TR> <TD><B>Annotation Type</B></TD> <TD><B>Precision</B></TD> "
              + "<TD><B>Recall</B></TD> <TD><B>Annotations<B></TD>");
    else
      Out.prln("<TR> <TD><B>Annotation Type</B></TD> <TD><B>Precision</B></TD> "
              + "<TD><B>Recall</B></TD>");
  }

  protected void updateStatistics(AnnotationDiff annotDiff, String annotType){
      precisionSum += annotDiff.getPrecisionAverage();
      recallSum += annotDiff.getRecallAverage();
      Double oldPrecision = (Double) precisionByType.get(annotType);
      if (oldPrecision == null)
        precisionByType.put(annotType,
                            new Double(annotDiff.getPrecisionAverage()));
      else
        precisionByType.put(annotType,
                            new Double(oldPrecision.doubleValue() +
                                       annotDiff.getPrecisionAverage()));
      Integer precCount = (Integer) prCountByType.get(annotType);
      if (precCount == null)
        prCountByType.put(annotType, new Integer(1));
      else
        prCountByType.put(annotType, new Integer(precCount.intValue() + 1));


      Double oldRecall = (Double) recallByType.get(annotType);
      if (oldRecall == null)
        recallByType.put(annotType,
                         new Double(annotDiff.getRecallAverage()));
      else
        recallByType.put(annotType,
                         new Double(oldRecall.doubleValue() +
                                    annotDiff.getRecallAverage()));
      Integer recCount = (Integer) recCountByType.get(annotType);
      if (recCount == null)
        recCountByType.put(annotType, new Integer(1));
      else
        recCountByType.put(annotType, new Integer(recCount.intValue() + 1));

  }

  protected void printStatistics() {

    Out.prln("<H2> Statistics </H2>");
    Out.prln("<H3> Precision </H3>");
    if (precisionByType != null && !precisionByType.isEmpty()) {
      Iterator iter = precisionByType.keySet().iterator();
      while (iter.hasNext()) {
        String annotType = (String) iter.next();
        Out.prln(annotType + ": "
          + ((Double)precisionByType.get(annotType)).doubleValue()
              /
              ((Integer)prCountByType.get(annotType)).intValue()
          + "<P>");
      }//while
    }
    Out.prln("Overall precision: " + getPrecisionAverage() + "<P>");

    Out.prln("<H3> Recall </H3>");
    if (recallByType != null && !recallByType.isEmpty()) {
      Iterator iter = recallByType.keySet().iterator();
      while (iter.hasNext()) {
        String annotType = (String) iter.next();
        Out.prln(annotType + ": "
          + ((Double)recallByType.get(annotType)).doubleValue()
              /
              ((Integer)recCountByType.get(annotType)).intValue()
          + "<P>");
      }//while
    }

    Out.prln("Overall recall: " + getRecallAverage()
             + "<P>");
  }

  protected AnnotationDiff measureDocs(
    Document keyDoc, Document respDoc, String annotType)
      throws ResourceInstantiationException {

    if (keyDoc == null || respDoc == null)
      return null;

    if (annotSetName != null
        && keyDoc.getAnnotations(annotSetName).get(annotType) == null)
      return null;
    else if ((annotSetName == null || annotSetName.equals(""))
        && keyDoc.getAnnotations().get(annotType) == null)
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
    parameters.put("keyAnnotationSetName",annotSetName);
    parameters.put("responseAnnotationSetName",annotSetName);
    //for a start, do not compare the features of the annotations
    parameters.put("keyFeatureNamesSet", new HashSet());
    parameters.put("textMode", new Boolean(true));

    // Create Annotation Diff visual resource
    AnnotationDiff annotDiff = (AnnotationDiff)
          Factory.createResource("gate.annotation.AnnotationDiff",parameters);

    return annotDiff;
  }

  protected void printAnnotations(AnnotationDiff annotDiff,
                    Document keyDoc, Document respDoc) {
    Out.prln("<TD>");
    Out.pr("MISSING ANNOTATIONS in the automatic texts: ");
    Set missingSet =
      annotDiff.getAnnotationsOfType(AnnotationDiff.MISSING_TYPE);
    printAnnotations(missingSet, keyDoc);
    Out.prln("<BR>");

    Out.pr("SPURIOUS ANNOTATIONS in the automatic texts: ");
    Set spuriousSet =
      annotDiff.getAnnotationsOfType(AnnotationDiff.SPURIOUS_TYPE);
    printAnnotations(spuriousSet, respDoc);
    Out.prln("</BR>");

    Out.pr("PARTIALLY CORRECT ANNOTATIONS in the automatic texts: ");
    Set partialSet =
      annotDiff.getAnnotationsOfType(AnnotationDiff.PARTIALLY_CORRECT_TYPE);
    printAnnotations(partialSet, respDoc);
    Out.prln("</TD>");

  }

  protected void printAnnotations(Set set, Document doc) {
    if (set == null || set.isEmpty())
      return;

    Iterator iter = set.iterator();
    while (iter.hasNext()) {
      Annotation ann = (Annotation) iter.next();
      Out.prln(
        "<B>" +
        doc.getContent().toString().substring(
          ann.getStartNode().getOffset().intValue(),
          ann.getEndNode().getOffset().intValue()) +
        "</B>: <I>[" + ann.getStartNode().getOffset() +
        "," + ann.getEndNode().getOffset() + "]</I>"
//        + "; features" + ann.getFeatures()
        );
    }//while
  }

  /**
   * The directory from which we should generate/evaluate the corpus
   */
  private File startDir;
  private File currDir;
  private static List annotTypes;

  private DefaultTokeniser tokeniser;
  private DefaultGazetteer gazetteer;
  private SentenceSplitter splitter;
  private POSTagger tagger;
  private ANNIETransducer transducer;
  private OrthoMatcher orthomatcher;

  //collect the sum of all precisions and recalls of all docs
  //and the number of docs, so I can calculate the average for
  //the corpus at the end
  private double precisionSum = 0;
  private double recallSum = 0;
  private HashMap precisionByType = new HashMap();
  private HashMap prCountByType = new HashMap();
  private HashMap recallByType = new HashMap();
  private HashMap recCountByType = new HashMap();
  private int docNumber = 0;

  /**
   * If true, the corpus tool will generate the corpus, otherwise it'll
   * run in evaluate mode
   */
  private boolean isGenerateMode = false;
  private boolean isVerboseMode = false;

  /**
   * If true, the corpus tool will evaluate stored against the human-marked
   * documents
   */
  private boolean isMarkedStored = false;
  private boolean isMarkedClean = false;

  private String annotSetName = "Key";

  private double threshold = 0.5;

  /** String to print when wrong command-line args */
  private static String usage =
    "usage: CorpusBenchmarkTool [-generate|-marked_stored|-marked_clean] [-verbose] directory-name";

}