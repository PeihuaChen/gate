/*
 *  ProfilePRs.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 04/10/2001
 *
 *  $Id$
 */

package gate.creole;

import java.util.*;
import java.io.*;

import gate.*;
import gate.creole.*;
import gate.util.*;
import gate.creole.tokeniser.*;
import gate.creole.gazetteer.*;
import gate.creole.splitter.*;
import gate.creole.orthomatcher.*;
import java.text.NumberFormat;

/**
 * This class provides a main function that:
 * <UL>
 * <LI>
 * initialises the GATE library, and creates all PRs
 * <LI>
 * takes a directory name as argument
 * <LI>
 * for each .html file in that directory:
 * <BR>  create a GATE document from the file
 * <BR>  run the PRs on the document
 * <BR>  dump some statistics in the end
 * </UL>
 */
public class ProfilePRs {

  /** String to print when wrong command-line args */
  private static String usage =
    "usage: ProfilePRs [-dir directory-name | file(s)]";

  private static double totalDocLength = 0, totalTokTime = 0,
    totalGazTime = 0, totalSplitTime = 0, totalTagTime = 0,
    totalJapeTime = 0, totalMatcherTime = 0;
  private static int docs = 0;

  /** Main function */
  public static void main(String[] args) throws Exception {
    // say "hi"
    Out.prln("processing command line arguments");

    // check we have a directory name or list of files
    List inputFiles = null;
    if(args.length < 1) throw new GateException(usage);
    if(args[0].equals("-dir")) { // list all the files in the dir
      if(args.length < 2) throw new GateException(usage);
      File dir = new File(args[1]);
      File[] filesArray = dir.listFiles();
      if(filesArray == null)
        throw new GateException(
          dir.getPath() + " is not a directory; " + usage
        );
      inputFiles = Arrays.asList(filesArray);
    } else { // all args should be file names
      inputFiles = new ArrayList();
      for(int i = 0; i < args.length; i++)
        inputFiles.add(new File(args[i]));
    }

    // initialise GATE
    Out.prln("initialising GATE");
    Gate.init();
    //tell GATE we're in batch mode
//    gate.Main.batchMode = true;

    double timeBefore = 0, timeAfter = 0;

    // create some processing resources
    Out.prln("creating PRs");

    timeBefore = System.currentTimeMillis();
    //create a default tokeniser
    FeatureMap params = Factory.newFeatureMap();
    DefaultTokeniser tokeniser = (DefaultTokeniser) Factory.createResource(
                    "gate.creole.tokeniser.DefaultTokeniser", params);
    timeAfter = System.currentTimeMillis();
    Out.prln("Tokeniser initialised for (seconds): " + (timeAfter-timeBefore));

    //create a default gazetteer
    timeBefore = System.currentTimeMillis();
    params = Factory.newFeatureMap();
    DefaultGazetteer gaz = (DefaultGazetteer) Factory.createResource(
                          "gate.creole.gazetteer.DefaultGazetteer", params);
    timeAfter = System.currentTimeMillis();
    Out.prln("Gazetteer initialised for (seconds): " + (timeAfter-timeBefore));

    //create a splitter
    timeBefore = System.currentTimeMillis();
    params = Factory.newFeatureMap();
    SentenceSplitter splitter = (SentenceSplitter) Factory.createResource(
                          "gate.creole.splitter.SentenceSplitter", params);
    timeAfter = System.currentTimeMillis();
    Out.prln("Splitter initialised for (seconds): " + (timeAfter-timeBefore));

    //create a tagger
    timeBefore = System.currentTimeMillis();
    params = Factory.newFeatureMap();
    POSTagger tagger = (POSTagger) Factory.createResource(
                          "gate.creole.POSTagger", params);
    timeAfter = System.currentTimeMillis();
    Out.prln("Tagger initialised for (seconds): " + (timeAfter-timeBefore));

    //create a grammar
    timeBefore = System.currentTimeMillis();
    params = Factory.newFeatureMap();
    ANNIETransducer transducer = (ANNIETransducer) Factory.createResource(
                          "gate.creole.ANNIETransducer", params);
    timeAfter = System.currentTimeMillis();
    Out.prln("Grammars initialised for (seconds): " + (timeAfter-timeBefore));

    //create an orthomatcher
    timeBefore = System.currentTimeMillis();
    params = Factory.newFeatureMap();
    OrthoMatcher orthomatcher = (OrthoMatcher) Factory.createResource(
                          "gate.creole.orthomatcher.OrthoMatcher", params);
    timeAfter = System.currentTimeMillis();
    Out.prln("Orthomatcher initialised for (seconds): " + (timeAfter-timeBefore));


    // for each document
    //   create a gate doc
    //   set as the document for hte PRs
    //   run the PRs
    //   dump output from the doc
    //   delete the doc
    Out.prln("looping on input files list");
    Iterator filesIter = inputFiles.iterator();
    while(filesIter.hasNext()) {
      File inFile = (File) filesIter.next(); // the current file
      Out.prln("processing file " + inFile.getPath());

      // set the source URL parameter to a "file:..." URL string
      params.clear();
      params.put("sourceUrl", inFile.toURL().toExternalForm());
      params.put("encoding", "");

      // create the document
      Document doc = (Document) Factory.createResource(
        "gate.corpora.DocumentImpl", params
      );
      docs++;
      totalDocLength += doc.getContent().size().longValue();

      // set the document param on the PRs
      tokeniser.setDocument(doc);
      timeBefore = System.currentTimeMillis();
      tokeniser.execute();
      timeAfter = System.currentTimeMillis();
      totalTokTime += timeAfter - timeBefore;

      //run gazetteer
      gaz.setDocument(doc);
      timeBefore = System.currentTimeMillis();
      gaz.execute();
      timeAfter = System.currentTimeMillis();
      totalGazTime += timeAfter - timeBefore;

      //run splitter
      splitter.setDocument(doc);
      timeBefore = System.currentTimeMillis();
      splitter.execute();
      timeAfter = System.currentTimeMillis();
      totalSplitTime += timeAfter - timeBefore;

      //run the tagger
      tagger.setDocument(doc);
      timeBefore = System.currentTimeMillis();
      tagger.execute();
      timeAfter = System.currentTimeMillis();
      totalTagTime += timeAfter - timeBefore;

      //run the transducer
      transducer.setDocument(doc);
      timeBefore = System.currentTimeMillis();
      transducer.execute();
      timeAfter = System.currentTimeMillis();
      totalJapeTime += timeAfter - timeBefore;

      // run the orthomatcher
      orthomatcher.setDocument(doc);
      timeBefore = System.currentTimeMillis();
      orthomatcher.execute();
      timeAfter = System.currentTimeMillis();
      totalMatcherTime += timeAfter - timeBefore;

      // make the doc a candidate for garbage collection
      Factory.deleteResource(doc);

    } // input files loop

    totalTokTime = (double) totalTokTime/1000;
    totalGazTime = (double) totalGazTime/1000;
    totalSplitTime = (double) totalSplitTime/1000;
    totalTagTime = (double) totalTagTime/1000;
    totalJapeTime = (double) totalJapeTime/1000;
    totalMatcherTime = (double) totalMatcherTime/1000;
    Out.prln("Total tokeniser time: " +
      NumberFormat.getInstance().format(totalTokTime));
    Out.prln("Total gazetteer time: " +
      NumberFormat.getInstance().format(totalGazTime));
    Out.prln("Total splitter time: " +
      NumberFormat.getInstance().format(totalSplitTime));
    Out.prln("Total tagger time: " +
      NumberFormat.getInstance().format(totalTagTime));
    Out.prln("Total JAPE grammars time: " +
      NumberFormat.getInstance().format(totalJapeTime));
    Out.prln("Total orthomatcher time: " +
      NumberFormat.getInstance().format(totalMatcherTime));
    Out.print("Total processing time: ");
    Out.prln(totalTokTime + totalGazTime + totalSplitTime
             + totalTagTime + totalJapeTime + totalMatcherTime);

    totalDocLength = (double) totalDocLength/1024;
    Out.prln("Total KBytes processed: " +
      NumberFormat.getInstance().format(totalDocLength));

    Out.pr("Avg tokeniser speed: ");
    Out.prln(NumberFormat.getInstance().format(totalDocLength/totalTokTime)
             + " Kb/sec");
    Out.pr("Avg gazetteer speed: ");
    Out.prln(NumberFormat.getInstance().format(totalDocLength/totalGazTime)
             + " Kb/sec");
    Out.pr("Avg splitter speed: ");
    Out.prln(NumberFormat.getInstance().format(totalDocLength/totalSplitTime)
             + " Kb/sec");
    Out.pr("Avg tagger speed: ");
    Out.prln(NumberFormat.getInstance().format(totalDocLength/totalTagTime)
             + " Kb/sec");
    Out.pr("Avg JAPE grammars speed: ");
    Out.prln(NumberFormat.getInstance().format(totalDocLength/totalJapeTime)
             + " Kb/sec");
    Out.pr("Avg orthomatcher Speed: ");
    Out.prln(NumberFormat.getInstance().format(totalDocLength/totalMatcherTime)
             + " Kb/sec");

    Out.pr("Combined Speed: ");
    Out.prln(NumberFormat.getInstance().format(totalDocLength/
              (totalTokTime + totalGazTime + totalSplitTime
              + totalTagTime + totalJapeTime + totalMatcherTime)));


  } // main


} // class ProfilePRs
