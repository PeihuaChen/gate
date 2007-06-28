/*
 *  Scratch.java
 *
 *  Copyright (c) 1998-2005, The University of Sheffield.
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

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.prefs.Preferences;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.*;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;

import gate.*;
import gate.creole.*;
import gate.creole.gazetteer.DefaultGazetteer;
import gate.creole.ir.*;
import gate.creole.tokeniser.DefaultTokeniser;
import gate.gui.MainFrame;
import gate.gui.OkCancelDialog;
import gate.gui.docview.AnnotationSetsView;
import gate.persist.SerialDataStore;
import gate.util.persistence.PersistenceManager;

/** A scratch pad for experimenting.
  */
public class Scratch
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  
  public static void docFromString(){
    try{
      Gate.init();
      SerialAnalyserController annie = (SerialAnalyserController)
        PersistenceManager.loadObjectFromFile(new File("d:/tmp/annie.gapp"));
      
      Corpus corpus = Factory.newCorpus("A Corpus");
      Document doc = Factory.newDocument("US President George W Bush has said he is seeking a $600m (£323m) boost in aid to nations hit by the Asian tsunami.");
      corpus.add(doc);
      annie.setCorpus(corpus);
      annie.execute();
      
      //get the annotations
      Iterator annIter = doc.getAnnotations().iterator();
      while(annIter.hasNext()){
        System.out.println(annIter.next());
      }
      
    }catch(Exception e){
      e.printStackTrace();
    }
  }
  
    
  public static void main(String args[]) throws Exception {   
    
    boolean value = OkCancelDialog.showDialog(null, new JLabel("OK?"), "Answer please!");
    System.out.println(value ? "Yes!" : "No!");
    
    if(true) return;
    final JFrame aFrame = new JFrame("Scratch");
    aFrame.addWindowListener(new WindowAdapter(){
      @Override
      public void windowClosing(WindowEvent e) {
        aFrame.dispose();
      }
      
    });
    aFrame.setSize(800, 600);
    aFrame.setVisible(true);
    Gate.init();
    MainFrame mf = MainFrame.getInstance();
    mf.setSize(800, 600);
    mf.setVisible(true);
    
    if(true) return;
    
    File file = new File("Z:/gate/bin");
    System.out.println("Canonical path: " + file.getCanonicalPath());
    System.out.println("URL: " + file.toURI().toURL());
    
    URL url = new URL("jar:file:/Z:/gate/bin/gate.jar!/gate/Gate.class");
    System.out.println(url);
    System.out.println("Path: " + url.getPath());
    System.out.println("File: " + url.getFile());
    System.out.println("Host: " + url.getHost());
    System.out.println("Proto: " + url.getProtocol());
    
    url = Thread.currentThread().getContextClassLoader().
      getResource("gate/Gate.class");
    System.out.println(url);
    System.out.println("Path: " + url.getPath());
    System.out.println("File: " + url.getFile());
    System.out.println("Host: " + url.getHost());
    System.out.println("Proto: " + url.getProtocol());
    
    Map defaultsMap = UIManager.getLookAndFeelDefaults();
    System.out.println(defaultsMap.keySet());

    
    //test for a bug reported by Luc Plamondon
    
    Gate.init();
    Document doc = Factory.newDocument("ala bala portocala");
    AnnotationSet set = doc.getAnnotations();
    Integer annId = 
      set.add(new Long(3), new Long(5), "FooBar", Factory.newFeatureMap());
    Annotation ann = set.get(annId);
    //remove the annotation 
    set.remove(ann);
    
    AnnotationSet resSet = set.get(new Long(0), new Long(10));
    
    //this set is empty so the bug was fixed.
    System.out.println(resSet);
    
    System.out.println("==============================================");
    
    
    Map listsMap = new HashMap();
    listsMap.put("blah", new ArrayList());
    List theList = (List)listsMap.get("blah");
    System.out.println(theList);
    theList.add("object");
    theList = (List)listsMap.get("blah");
    System.out.println(theList);
    
    
    
    File home = new File("z:/gate/plugins");
    File tok = new File(home, "ANNIE/resources/tokeniser/Default.rul");
    System.out.println(tok);
    
    Preferences prefRoot = Preferences.userNodeForPackage(AnnotationSetsView.class);
    System.out.println(prefRoot.keys().length);
    prefRoot.removeNode();
    prefRoot = Preferences.userNodeForPackage(AnnotationSetsView.class);
    System.out.println(prefRoot.keys().length);
    Color col = new Color(100, 101, 102, 103);
    int rgb = col.getRGB();
    int alpha = col.getAlpha();
    int rgba = rgb | (alpha << 24);
    Color col1 = new Color(rgba, true);
    System.out.println(col + " a: " + col.getAlpha());
    System.out.println(col1+ " a: " + col1.getAlpha());
    System.out.println(col.equals(col1));
//    Map defaultsMap = UIManager.getLookAndFeelDefaults();
//    System.out.println(defaultsMap.keySet());
    
    
//    double a = 16.99;
//    double b = 9.99;
//    double c = a - b;
//    System.out.println(c);

//    Runtime.getRuntime().exec(new String[]{"cmd",
//                                           "C:\\Program Files\\GATE 2.2\\bin\\gate.bat"},
//                              null,
//                              new File("C:\\Program Files\\GATE 2.2\\bin"));

//    Gate.init();
//    Document doc = Factory.newDocument("The quick brown fox jumped over the lazy dog");
//    AnnotationSet annSet1 = doc.getAnnotations("Set1");
//    annSet1.add(new Long(1), new Long(5), "Foo", Factory.newFeatureMap());
//
//    AnnotationSet annSet2 = doc.getAnnotations("Set2");
//    annSet2.add(new Long(1), new Long(5), "Bar", Factory.newFeatureMap());
//    annSet2.addAll(annSet1);
//
//    List annotations = new ArrayList(annSet2);
//    Collections.sort(annotations, new OffsetComparator());
//    Iterator annIter = annotations.iterator();
//    while(annIter.hasNext()){
//      Annotation ann =(Annotation)annIter.next();
//      System.out.print("Start node: ID = " + ann.getStartNode().getId());
//      System.out.println(" Offset = " + ann.getStartNode().getOffset());
//      System.out.print("End node: ID = " + ann.getEndNode().getId());
//      System.out.println(" Offset = " + ann.getEndNode().getOffset());
//
//    }
//    File tempFile = File.createTempFile("gaga", "");
//    tempFile.delete();
//    tempFile.mkdir();
//    tempFile.deleteOnExit();
//    File tempFile2 = File.createTempFile("fil", ".tmp", tempFile);
//    tempFile2.deleteOnExit();
//System.out.println(tempFile.getCanonicalPath());
//    Thread.sleep(100000);
//
//    Map charsets = java.nio.charset.Charset.availableCharsets();
//    Iterator namesIter = charsets.keySet().iterator();
//    while(namesIter.hasNext()){
//      String name = (String)namesIter.next();
//      System.out.println(name + " : " + charsets.get(name));
//    }
//    System.out.println(System.getProperty("file.encoding"));
//    System.out.println(java.nio.charset.Charset.forName(System.getProperty("file.encoding")).name());
//    System.out.println(new Character((char)0xa3));
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
    //params.put("grammarURL", new File("z:\\tmp\\main.jape").toURI().toURL());
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
    Document doc = Factory.newDocument(file.toURI().toURL());
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

    List<Annotation> tokenList = new ArrayList<Annotation>(doc.getAnnotations().get(annotationTypes));
    Collections.sort(tokenList, new OffsetComparator());

    //iterate through the tokens
    Iterator<Annotation> tokIter = tokenList.iterator();
    while(tokIter.hasNext()){
      Annotation anAnnotation = tokIter.next();
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


