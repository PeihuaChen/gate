/*
 *  Scratch.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 22/03/00
 *
 *  $Id$
 */


package gate.util;

import java.util.*;
import java.net.*;
import java.io.*;
import java.util.zip.*;

import gate.*;
import gate.creole.*;
import gate.creole.ir.*;
import gate.creole.tokeniser.*;
import gate.creole.gazetteer.*;
import gate.persist.*;
import gate.gui.*;

import org.xml.sax.*;
import javax.xml.parsers.*;
import org.w3c.www.mime.*;

/** A scratch pad for experimenting.
  */
public class Scratch
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  public static void main(String args[]) throws Exception {
//    Gate.init();
//
//    List classes = Tools.findSubclasses(gate.creole.ir.Search.class);
//    if(classes != null) for(int i = 0; i < classes.size(); i++){
//      Out.prln(classes.get(i).toString());
//    }
//    createIndex();
//    URL anURL = new URL("file:/z:/a/b/c/d.txt");
//    URL anotherURL = new URL("file:/z:/a/b/c/d.txt");
//    String relPath = gate.util.persistence.PersistenceManager.
//                     getRelativePath(anURL, anotherURL);
//    Out.prln("Context: " + anURL);
//    Out.prln("Target: " + anotherURL);
//    Out.prln("Relative path: " + relPath);
//    Out.prln("Result " + new URL(anURL, relPath));
//    javax.swing.text.FlowView fv;
//    javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
//    Map uidefaults  = (Map)javax.swing.UIManager.getDefaults();
//    List keys = new ArrayList(uidefaults.keySet());
//    Collections.sort(keys);
//    Iterator keyIter = keys.iterator();
//    while(keyIter.hasNext()){
//      Object key = keyIter.next();
//      System.out.println(key + " : " + uidefaults.get(key));
//    }

    // initialise the thing
//    Gate.setNetConnected(false);
//    Gate.setLocalWebServer(false);
//    Gate.init();

//    Scratch oneOfMe = new Scratch();
//    try{
//      oneOfMe.runNerc();
//    } catch (Exception e) {
//      e.printStackTrace(Out.getPrintWriter());
//    }


//    CreoleRegister reg = Gate.getCreoleRegister();
//System.out.println("Instances for " + reg.getLrInstances("gate.creole.AnnotationSchema"));
//System.out.println("Instances for " + reg.getAllInstances ("gate.creole.AnnotationSchema"));

//System.out.println("VRs for " + reg.getAnnotationVRs("Tree"));
//System.out.println("VRs for " + reg.getAnnotationVRs());

//System.out.println(reg.getLargeVRsForResource("gate.corpora.DocumentImpl"));

  } // main

  /** Example of using an exit-time hook. */
  public static void exitTimeHook() {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      public void run() {
        System.out.println("shutting down");
        System.out.flush();

        // create a File to store the state in
        File stateFile = new File("z:\\tmp", "GateGuiState.gzsr");

        // dump the state into the new File
        try {
          ObjectOutputStream oos = new ObjectOutputStream(
            new GZIPOutputStream(new FileOutputStream(stateFile))
          );
          System.out.println("writing main frame");
          System.out.flush();
          oos.writeObject(Main.getMainFrame());
          oos.close();
        } catch(Exception e) {
          System.out.println("Couldn't write to state file: " + e);
        }

        System.out.println("done");
        System.out.flush();
      }
    });
  } // exitTimeHook()

  /**
   * ***** <B>Failed</B> *****
   * attempt to serialise whole gui state - various swing components
   * don't like to be serialised :-(. might be worth trying again when
   * jdk1.4 arrives.
   */
  public static void dumpGuiState() {
    System.out.println("dumping gui state...");
    System.out.flush();

    // create a File to store the state in
    File stateFile = new File("z:\\tmp", "GateGuiState.gzsr");

    // dump the state into the new File
    try {
      ObjectOutputStream oos = new ObjectOutputStream(
        new GZIPOutputStream(new FileOutputStream(stateFile))
      );
      MainFrame mf = Main.getMainFrame();

      // wait for 1 sec
      long startTime = System.currentTimeMillis();
      long timeNow = System.currentTimeMillis();
      while(timeNow - startTime < 3000){
        try {
          Thread.sleep(150);
          timeNow = System.currentTimeMillis();
        } catch(InterruptedException ie) {}
      }

      System.out.println("writing main frame");
      System.out.flush();
      oos.writeObject(mf);
      oos.close();
    } catch(Exception e) {
      System.out.println("Couldn't write to state file: " + e);
    }

    System.out.println("...done gui dump");
    System.out.flush();
  } // dumpGuiState

  /**
   * Run NERC and print out the various stages (doesn't actually
   * use Nerc but the individual bits), and serialise then deserialise
   * the NERC system.
   */
  public void runNerc() throws Exception {
    long startTime = System.currentTimeMillis();

    Out.prln("gate init");
    Gate.setLocalWebServer(false);
    Gate.setNetConnected(false);
    Gate.init();

    Out.prln((System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
    Out.prln("creating resources");

    // a controller
    Controller c1 = (Controller) Factory.createResource(
      "gate.creole.SerialController",
      Factory.newFeatureMap()
    );
    c1.setName("Scratch controller");

    //get a document
    FeatureMap params = Factory.newFeatureMap();
    params.put(Document.DOCUMENT_URL_PARAMETER_NAME, Gate.getUrl("tests/doc0.html"));
    params.put(Document.DOCUMENT_MARKUP_AWARE_PARAMETER_NAME, "false");
    Document doc = (Document)Factory.createResource("gate.corpora.DocumentImpl",
                                                    params);

    //create a default tokeniser
    params = Factory.newFeatureMap();
    params.put(DefaultTokeniser.DEF_TOK_TOKRULES_URL_PARAMETER_NAME,
      "gate:/creole/tokeniser/DefaultTokeniser.rules");
    params.put(DefaultTokeniser.DEF_TOK_ENCODING_PARAMETER_NAME, "UTF-8");
    params.put(DefaultTokeniser.DEF_TOK_DOCUMENT_PARAMETER_NAME, doc);
    ProcessingResource tokeniser = (ProcessingResource) Factory.createResource(
      "gate.creole.tokeniser.DefaultTokeniser", params
    );

    //create a default gazetteer
    params = Factory.newFeatureMap();
    params.put(DefaultGazetteer.DEF_GAZ_DOCUMENT_PARAMETER_NAME, doc);
    params.put(DefaultGazetteer.DEF_GAZ_LISTS_URL_PARAMETER_NAME,
      "gate:/creole/gazeteer/default/lists.def");
    ProcessingResource gaz = (ProcessingResource) Factory.createResource(
      "gate.creole.gazetteer.DefaultGazetteer", params
    );

    //create a default transducer
    params = Factory.newFeatureMap();
    params.put(Transducer.TRANSD_DOCUMENT_PARAMETER_NAME, doc);
    //params.put("grammarURL", new File("z:\\tmp\\main.jape").toURL());
    ProcessingResource trans = (ProcessingResource) Factory.createResource(
      "gate.creole.Transducer", params
    );

    // get the controller to encapsulate the tok and gaz
    c1.getPRs().add(tokeniser);
    c1.getPRs().add(gaz);
    c1.getPRs().add(trans);

    Out.prln((System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
    Out.prln("dumping state");

    // create a File to store the state in
    File stateFile = new File("z:\\tmp", "SerialisedGateState.gzsr");

    // dump the state into the new File
    try {
      ObjectOutputStream oos = new ObjectOutputStream(
        new GZIPOutputStream(new FileOutputStream(stateFile))
      );
      oos.writeObject(new SessionState());
      oos.close();
    } catch(IOException e) {
      throw new GateException("Couldn't write to state file: " + e);
    }

    Out.prln(System.getProperty("user.home"));

    Out.prln((System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
    Out.prln("reinstating");

    try {
      FileInputStream fis = new FileInputStream(stateFile);
      GZIPInputStream zis = new GZIPInputStream(fis);
      ObjectInputStream ois = new ObjectInputStream(zis);
      SessionState state = (SessionState) ois.readObject();
      ois.close();
    } catch(IOException e) {
      throw
        new GateException("Couldn't read file "+stateFile+": "+e);
    } catch(ClassNotFoundException ee) {
      throw
        new GateException("Couldn't find class: "+ee);
    }

    Out.prln((System.currentTimeMillis() - startTime) / 1000.0 + " seconds");
    Out.prln("done");
  } // runNerc()


  /** Inner class for holding CR and DSR for serialisation experiments */
  class SessionState implements Serializable {
    SessionState() {
      cr = Gate.getCreoleRegister();
      dsr = Gate.getDataStoreRegister();
    }

    CreoleRegister cr;

    DataStoreRegister dsr;

    // other state from Gate? and elsewhere?
  } // SessionState

  /** Generate a random integer for file naming. */
  protected static int random() {
    return randomiser.nextInt(9999);
  } // random

  /**
   * Generates an index for a corpus in a datastore on Valy's computer in order
   * to have some test data.
   */
  public static void createIndex() throws Exception{
    String dsURLString = "file:///d:/temp/ds";
    String indexLocation = "d:/temp/ds.idx";

    Gate.init();

    //open the datastore
    SerialDataStore sds = (SerialDataStore)Factory.openDataStore(
                            "gate.persist.SerialDataStore", dsURLString);
    sds.open();
    List corporaIds = sds.getLrIds("gate.corpora.SerialCorpusImpl");
    IndexedCorpus corpus = (IndexedCorpus)
                           sds.getLr("gate.corpora.SerialCorpusImpl",

                                     corporaIds.get(0));
    DefaultIndexDefinition did = new DefaultIndexDefinition();
    did.setIrEngineClassName(gate.creole.ir.lucene.
                             LuceneIREngine.class.getName());

    did.setIndexLocation(indexLocation);
    did.addIndexField(new IndexField("body", new ContentPropertyReader(), false));

    corpus.setIndexDefinition(did);

    Out.prln("removing old index");
    corpus.getIndexManager().deleteIndex();
    Out.prln("building new index");
    corpus.getIndexManager().createIndex();
    Out.prln("optimising new index");
    corpus.getIndexManager().optimizeIndex();
    Out.prln("saving corpus");
    sds.sync(corpus);
    Out.prln("done!");
  }

  /**
   *
   * @param file a TXT file containing the text
   */
  public static void tokeniseFile(File file) throws Exception{
    //initialise GATE (only call it once!!)
    Gate.init();


    //create the document
    Document doc = Factory.newDocument(file.toURL());

    //create the tokeniser
    DefaultTokeniser tokeniser = (DefaultTokeniser)Factory.createResource(
      "gate.creole.tokeniser.DefaultTokeniser");

    //tokenise the document
    tokeniser.setParameterValue(DefaultTokeniser.DEF_TOK_DOCUMENT_PARAMETER_NAME, doc);
    tokeniser.execute();

    //extract data from document
    //we need tokens and spaces
    Set annotationTypes = new HashSet();
    annotationTypes.add(ANNIEConstants.TOKEN_ANNOTATION_TYPE);
    annotationTypes.add(ANNIEConstants.SPACE_TOKEN_ANNOTATION_TYPE);

    List tokenList = new ArrayList(doc.getAnnotations().get(annotationTypes));
    Collections.sort(tokenList, new OffsetComparator());

    //iterate through the tokens
    Iterator tokIter = tokenList.iterator();
    while(tokIter.hasNext()){
      Annotation anAnnotation = (Annotation)tokIter.next();
      System.out.println("Annotation: (" +
                        anAnnotation.getStartNode().getOffset().toString() +
                        ", " + anAnnotation.getEndNode().getOffset().toString() +
                        "[type: " + anAnnotation.getType() +
                         ", features: " + anAnnotation.getFeatures().toString()+
                         "]" );
    }
  }


  public static class ContentPropertyReader implements PropertyReader{
    public String getPropertyValue(gate.Document doc){
      return doc.getContent().toString();
    }
  }

  /** Random number generator */
  protected static Random randomiser = new Random();

} // class Scratch

