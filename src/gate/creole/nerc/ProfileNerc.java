/*
 *  ProfileNerc.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian Ursu, 21/05/01
 *
 *  $Id$
 */

package gate.creole.nerc;

import java.util.*;
import java.net.*;
import java.io.*;
import java.text.*;

import gate.*;
import gate.creole.*;
import gate.gui.*;
import gate.util.*;

/** This class is used to profile nerc.*/
public class ProfileNerc {


  /** Nerc, the component to be profiled*/
  protected Nerc nerc = null;
  /** Where the results go*/
  protected static PrintWriter printWriter  = null;

  /** Default constructor*/
  public ProfileNerc() {
    formatter = NumberFormat.getInstance();
    formatter.setMaximumFractionDigits(2);
  }//ProfileNerc()

  /** Main takes at l */
  public static void main(String[] args) {

    Set documentsToBeRun = null;
    ProfileNerc profileNerc = null;
    File file = null;

    try{
      if(args.length < 2) {
          System.err.println("USAGE : ProfileNerc arg0 arg1 ... argN" +
                              "(must be at least 2 args)");
      }else {
        Gate.setLocalWebServer(false);
        Gate.setNetConnected(false);
        Gate.init();

        profileNerc = new ProfileNerc();
        documentsToBeRun = new HashSet();
        for (int i=1; i<args.length; i++){
            file = new File(args[i]);
            documentsToBeRun.add(file);
        }// End for
        printWriter = new PrintWriter(new FileWriter(new File(args[0])));
        printWriter.println("<?xml version=\"1.0\" ?>");
        printWriter.println("<?xml:stylesheet type=\"text/xsl\" href=\"nerc.xsl\"?>");
        printWriter.println("<results>");
        profileNerc.initNerc();
//        System.exit(1);
        profileNerc.profile(documentsToBeRun);
        printWriter.println(" <avgSpeed>");
        printWriter.println("  <avgSpeedSize>" +
               formatter.format(avgSpSize/documentsToBeRun.size()) + "</avgSpeedSize>");
        printWriter.println("  <avgSpeedTokens>" +
          formatter.format(avgSpTokens/documentsToBeRun.size()) + "</avgSpeedTokens>");
        printWriter.println("  <avgSpeedSpTokens>" +
 formatter.format(avgSpSpaceTokens/documentsToBeRun.size()) + "</avgSpeedSpTokens>");
        printWriter.println("  <avgSpeedlookUps>" +
 formatter.format(avgSpLookUp/documentsToBeRun.size()) + "</avgSpeedlookUps>");

        printWriter.println(" </avgSpeed>");
        printWriter.println("</results>");
        printWriter.flush();
        printWriter.close();
        System.err.println("Done...");
        System.exit(1);
      }// End if
    }catch (IOException ioe){
      ioe.printStackTrace(System.err);
    }catch (GateException ge){
      ge.printStackTrace(System.err);
    }// End try
  }// main()

  protected void profile(Set files){
    Iterator it = files.iterator();
    while (it.hasNext()){
      File file = (File) it.next();
      printWriter.println(" <result>");
      process(file);
      printWriter.println(" </result>");
    }// End while
  }// profile()

  protected void initNerc(){
    try{
      System.err.println("Nerc system is initialising...");
      long startTime = System.currentTimeMillis();
      nerc = (Nerc) Factory.createResource("gate.creole.nerc.Nerc");
      long endTime = System.currentTimeMillis();
      printWriter.println(" <nercInit>"+ formatter.format((endTime - startTime)/1000) + "</nercInit>");
      // Size was calculated with JProbe
      printWriter.println(" <nercSize>"+ "68" + "</nercSize>");
    }catch(ResourceInstantiationException rie){
      rie.printStackTrace(System.err);
    }// End try
  }// initNerc()

  protected void process(File  file){
    try{
      FeatureMap params = Factory.newFeatureMap();
      Document doc = null;
      //get the document
      System.err.println("Building document: " +
                                file.toURL().getFile() + "...");

      params.put("sourceUrl",file.toURL());
      params.put("markupAware",new Boolean(true));
      params.put("encoding","");

      doc = (Document)Factory.createResource("gate.corpora.DocumentImpl",
                                                    params);

      nerc.setDocument(doc);
      long startTime = System.currentTimeMillis();
      nerc.run();
      long endTime = System.currentTimeMillis();
      // runningTime calculated in seconds
      long runningTime = (endTime - startTime);
      nerc.check();
      System.err.println("Collecting results from " +
                         doc.getSourceUrl().getFile() + "...");

      int tokens = ((AnnotationSet)doc.getNamedAnnotationSets().
                                            get("nercAS")).get("Token").size();
      int spaceTokens = ((AnnotationSet)doc.getNamedAnnotationSets().
                                        get("nercAS")).get("SpaceToken").size();
      int lookUps = ((AnnotationSet)doc.getNamedAnnotationSets().
                                            get("nercAS")).get("Lookup").size();
      long size = doc.getContent().size().longValue();

      printWriter.println("  <document>"+doc.getSourceUrl().getFile()+"</document>");
      printWriter.println("  <size>" + formatter.format((float)size/1024) + "</size>");
      printWriter.println("  <tokens>" + tokens + "</tokens>");
      printWriter.println("  <spaceTokens>" + spaceTokens + "</spaceTokens>");
      printWriter.println("  <lookUps>" + lookUps + "</lookUps>");
      printWriter.println("  <nercRunningTime>" + formatter.format((float)runningTime/1000) +
                                                   "</nercRunningTime>");
      printWriter.println("  <speedPerSize>" + formatter.format(((float)size/1024)/(runningTime/1000)) +
                                                     "</speedPerSize>");
      avgSpSize += ((float)size/1024)/(runningTime/1000);
      printWriter.println("  <speedPerTokens>" +formatter.format (((float)tokens)/(runningTime/1000)) +
                                            "</speedPerTokens>");
      avgSpTokens += ((float)tokens)/(runningTime/1000);
      printWriter.println("  <speedPerSpaceTokens>" +
                        formatter.format(((float)spaceTokens)/(runningTime/1000)) +
                                    "</speedPerSpaceTokens>");
      avgSpSpaceTokens += ((float)spaceTokens)/(runningTime/1000);
      printWriter.println("  <speedPerLookUp>" +
              formatter.format(((float)lookUps)/(runningTime/1000)) + "</speedPerLookUp>");
      avgSpLookUp += ((float)lookUps)/(runningTime/1000);
      // free some mem
      doc = null;
    }catch(java.net.MalformedURLException jmue){
      System.err.println("Malformed exception ");
    }catch(gate.creole.ResourceInstantiationException ioe){
                System.err.println("Couldn't create file.");
    }catch(gate.creole.ExecutionException ee){
      ee.printStackTrace(System.err);
    }// End try
 }// process()

 static NumberFormat formatter = null;
 static float avgSpSize = 0;
 static float avgSpTokens = 0;
 static float avgSpSpaceTokens = 0;
 static float avgSpLookUp = 0;
}// End class ProfileNerc