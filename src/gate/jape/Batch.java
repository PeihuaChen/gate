/*
 *  Batch.java - transducer class
 *
 *  Copyright (c) 2000-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June1991.
 *
 *  A copy of this licence is included in the distribution in the file
 *  licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 * 
 *  Hamish Cunningham, 10/08/98
 *
 *  $Id$
 *
 *  DEVELOPER NOTES:
 *
 *  This is one that got away; the relation between constructors,
 *  initTransducer and parseTransducer are totally screwy and get worse
 *  every time I add something (e.g. support for resource loading).
 *  We should probably junk this whole thing and start again....
 */

package gate.jape;

import java.util.*;
import java.util.jar.*;
import java.io.*;
import com.objectspace.jgl.*;

import gate.annotation.*;
import gate.util.*;
import gate.*;
import gate.gui.*;

/** Batch processing of JAPE transducers against documents or collections.
  * Construction will parse or deserialise a transducer as required.
  */
public class Batch implements JapeConstants, java.io.Serializable,
                              ProcessProgressReporter, StatusReporter{
  /**
    *  This field is "final static" because it brings in
    *  the advantage of dead code elimination
    *  When DEBUG is set on false the code that it guardes will be eliminated
    *  by the compiler. This will spead up the progam a little bit.
    */
  private static final boolean DEBUG = false;

  /** The name of the transducer file, a .jape or .ser. */
  private String japeFileName;

  /** The JAPE transducer. */
  private Transducer transducer;

  /** A stream connected to the JAPE file (often null). */
  private InputStream japeStream = null;

  /** Create non-initialised instance (private, used in main). */
  private Batch() { }

  /** Create a fully initialised instance.
    * <P><CODE>japeFileName</CODE>: the name of a .jape or .ser transducer
    * file. This may be an absolute path, or may a .jar
    * that lives somewhere on the classpath.
    */
  public Batch(String japeFileName) throws JapeException {
    this.japeFileName = japeFileName;
    initTransducer();
  } // full init constructor

  public Batch(String japeFileName, StatusListener sListener) throws JapeException {
    this.japeFileName = japeFileName;
    this.addStatusListener(sListener);
    initTransducer();
  } // full init constructor


  /** Create a fully initialised instance from an InputStream connected
    * to the JAPE file.
    */
  public Batch(InputStream japeStream) throws JapeException {
    if(japeStream == null)
      throw new JapeException(
        "attempt to create a batch parser with null input stream"
      );
    this.japeFileName = "stream";
    this.japeStream = japeStream;
    initTransducer();
  } // full init constructor

  /** Create a fully initialised instance from a resource path and resource
    * name.
    */
  public Batch(String resPath, String resName) throws JapeException {
    fromResource = true;
    this.japeFileName = resName;
    this.resPath = resPath;
    initTransducer();
  } // full init constructor


  /** Get the transducer. */
  public Transducer getTransducer() { return transducer; }

  /** Instantiate transducer member as necessary. */
  private void initTransducer()
  throws JapeException {
    if(fromResource) {
      parseJape(resPath, japeFileName);
    } else if(japeFileName.endsWith(".ser") || japeFileName.endsWith(".SER"))
      deserialiseJape(new File(japeFileName));
    else if(japeFileName.endsWith(".jape") || japeFileName.endsWith(".JAPE"))
      parseJape();
    else if(japeFileName.endsWith(".jar") || japeFileName.endsWith(".JAR"))
      deserialiseJape();
    else if(japeFileName.equals("stream"))
      parseJape(japeStream);
    else
      throw new JapeException(
        "unknown file type (not .jape, .ser or .jar):" + japeFileName
      );
    if(transducer != null) transducer.addStatusListener(new StatusListener(){
      public void statusChanged(String text){
        fireStatusChangedEvent(text);
      }
    });
  } // getTransducer

  /** Parse a jape file and store the transducer. */
  private void parseJape() throws JapeException {
    try {
      gate.jape.parser.ParseCpsl parser =
        new gate.jape.parser.ParseCpsl(japeFileName);
      StatusListener listener = new StatusListener(){
        public void statusChanged(String text){
          fireStatusChangedEvent(text);
        }
      };
      parser.addStatusListener(listener);
      transducer = parser.MultiPhaseTransducer();
      transducer.removeStatusListener(listener);
    } catch (gate.jape.parser.ParseException e) {
      throw new
        JapeException("Batch: error parsing transducer: " + e.getMessage());
    } catch (java.io.IOException e) {
      throw new
        JapeException("Batch: couldn't open JAPE file: " + e.getMessage());
    }
  } // parseJape

  /** Parse a jape file from an InputStream and store the transducer. */
  private void parseJape(InputStream japeStream) throws JapeException {
    try {
      gate.jape.parser.ParseCpsl parser =
        new gate.jape.parser.ParseCpsl(japeFileName, japeStream);
      transducer = parser.MultiPhaseTransducer();
    } catch (gate.jape.parser.ParseException e) {
      throw new
        JapeException("Batch: error parsing transducer: " + e.getMessage());
    } catch (java.io.IOException e) {
      throw new
        JapeException("Batch: couldn't read JAPE stream: " + e.getMessage());
    }
  } // parseJape(InputStream) 

  /** Parse a jape file from a resource and store the transducer. */
  private void parseJape(String resPath, String resName) throws JapeException {
    try {
      gate.jape.parser.ParseCpsl parser =
        new gate.jape.parser.ParseCpsl(resPath, resName);
      transducer = parser.MultiPhaseTransducer();
    } catch (gate.jape.parser.ParseException e) {
      throw new
        JapeException("Batch: error parsing transducer: " + e.getMessage());
    } catch (java.io.IOException e) {
      throw new
        JapeException("Batch: couldn't read JAPE resource: " + e.getMessage());
    }
  } // parseJape(resPath, resName)


  /** Deserialise from a .ser file. */
  private void deserialiseJape(File japeFile) throws JapeException {
    // set up a file input stream
    FileInputStream japeInputStream = null;
    try {
      japeInputStream = new FileInputStream(japeFile.getPath());
    } catch (IOException e) {
      throw new JapeException(
        "Can't read from " + japeFile.getPath() + ": " + e.getMessage()
      );
    }

    // call the input stream deserialise method
    deserialiseJape(japeInputStream);
  } // deserialiseJape(File)

  /** Deserialise from a JAR file. */
  private void deserialiseJape() throws JapeException {
    // find the jar from CLASSPATH
    //SearchPath classPath =
    //  new SearchPath(System.getProperty("java.class.path"), ".");
    File jarFile = new File(japeFileName); //classPath.getFile(japeFileName);
    if(jarFile == null)
      throw new JapeException("Batch: can't find " + japeFileName);

    // get a byte array input stream with the .ser in out of the jar file
    JarFile jar = null;
    BufferedInputStream japeInputStream = null;
    try {
      jar = new JarFile(jarFile.getPath());
      japeInputStream = new BufferedInputStream(
        jar.getInputStream(jar.getJarEntry(jarNameToSerName(japeFileName)))
      );
    } catch(IOException e) {
      throw new JapeException("couldn't read jar file " + japeFileName);
    }


    // call the input stream deserialise method
    deserialiseJape(japeInputStream);
  } // deserialiseJape()

  /** Create a transducer from an object input stream (deserialisation). */
  private void deserialiseJape(InputStream japeInputStream)
  throws JapeException {
    try {
      ObjectInputStream ois = new ObjectInputStream(japeInputStream);
      transducer = (Transducer) ois.readObject();
      ois.close();
      japeInputStream.close(); // redundant?
    } catch (IOException e) {
      throw new JapeException(
        "Batch: can't deserialise InputStream (1): " + e.getMessage()
      );
    } catch (ClassNotFoundException e) {
      throw new JapeException(
        "Batch: can't deserialise InputStream (2): " + e.getMessage()
      );
    }
  } // deserialise(OIS)

  /** Create a .ser name from a .jar name. */
  private String jarNameToSerName(String jarName) {
    return jarName.substring(0, jarName.length() - 4) + ".ser";
  } // jarNameToSerName

  /** Process the given collection. */
  public void transduce(Corpus coll) throws JapeException {
    // for each doc run the transducer
    Iterator iter = coll.iterator();
    while(iter.hasNext()) {
      Document doc = (Document) iter.next();
//      transducer.transduce(doc);
      transduce(doc, doc.getAnnotations());
    }
  } // transduce(coll)

  /** Process a single document. */
  public void transduce(Document doc) throws JapeException {
    transducer.transduce(doc, doc.getAnnotations());
  } // transduce(doc)

  /** Process a single document. */
  public void transduce(Document doc, AnnotationSet annotations) throws JapeException {
    transducer.transduce(doc, annotations);
  } // transduce(doc)

  /** Process a single text. */
  public Document transduce(String text) throws JapeException {
    Document doc = null;
    try { doc = Transients.newDocument(text);
    } catch (IOException e) { throw new JapeException(e.toString()); }
    transducer.transduce(doc, doc.getAnnotations());
    return doc;
  } // transduce(text)

  /** Process a single file. */
  public Document transduce(File textFile) throws JapeException {
    String text = null;
    try {
      text = gate.util.Files.getString(textFile);
    } catch(IOException e) { throw new JapeException(e.toString()); }
    return transduce(text);
  } // transduce(textFile)

  /** Process a set of files. */
  public Corpus transduce(String[] textFileNames) throws JapeException {
    Corpus coll = Transients.newCorpus("JAPE batch corpus");
    Document doc = null;
    for(int i = 0; i < textFileNames.length; i++) {
      try {
        doc = Transients.newDocument(textFileNames[i]);
        doc.setFeatures(Transients.newFeatureMap());
        /*coll.createDocument(
          textFileNames[i],
          null, // the text - should get read from disk
          new AnnotationSetImpl(doc),
          Transients.newFeatureMap(),
          Document.COPIED
        );*/
      } catch(IOException e) { throw new JapeException(e.toString()); }
      transducer.transduce(doc, doc.getAnnotations());
    }
    return coll;
  } // transduce(textFileNames)

  /** This is where it all happens. This is <I>the</I> place to be. Take
    * your summer holidays here. Visit on Saturday nights. Buy a season
    * ticket from <CODE>www.programmer.gone.insane.com</CODE>.
    * <P>
    * Takes a .jape/.jar/.ser
    *  file name (-j option) which is assumed to hold a pattern
    * grammar for a multi-phase transducer, and a collection
    * name (-c option) or a list of files. As needed it then parses and
    * compiles the transducer, then transduces all the documents in the
    * collection and saves it to disk.
    */
  public static void main(String args[]) {
    // oh great bug in the sky give us this day our daily fuckup
    //gate.util.Debug.setDebug(true);
    //gate.util.Debug.setDebug(Rule.class, true);
    //gate.util.Debug.setDebug(LeftHandSide.class, true);
    //gate.util.Debug.setDebug(BasicPatternElement.class, true);
    //gate.util.Debug.setDebug(AnnotationSet.class, true);

    // The persistent name of the collection.
    String persCollName = null;;

    // The collection to process.
    Corpus collection = null;

    // create one of us
    Batch batch = new Batch();

    // process the options
    int i = 0;
    for( ; i<args.length; i++) {
      if(args[i].equals("-c") && ++i < args.length) // -c = coll name
        persCollName = args[i];
      else if(args[i].equals("-j") && ++i < args.length)// -j = transducer name
        batch.japeFileName = args[i];
      else if(args[i].equals("-v")) // -v = verbose
        batch.setVerbose(true);
      else if(args[i].startsWith("-"))
        batch.usage("unknown option " + args[i]);
      else
        break;
    } // for each arg

    // file name list
    String[] fileNames = null;
    if(args.length > i) {
      fileNames = new String[args.length - i];
      for(int j = 0; i<args.length; j++, i++)
        fileNames[j] = args[i];
    }

    // did they give valid options?
    if(batch.japeFileName == null)
      batch.usage("you must supply a transducer name");
    if(fileNames != null && persCollName != null)
      batch.usage("can't read a collection AND process a file list");

    // parse the transducer or bomb
    batch.message("parsing the transducer");
    try { batch.initTransducer(); }
    catch(JapeException e) {
      batch.usage("oops: " + e.toString());
    }

    Corpus coll = null;
    if(persCollName != null) { // we got a collection name, not a list of files
      // open the collection or bomb
      coll = null;
      batch.message("opening the collection");
      coll = Transients.newCorpus(persCollName);

      // transduce
      batch.message("calling transducer");
      try { batch.transduce(coll); }
      catch(JapeException e) {
        batch.usage("oops (1): " + e.toString());
      }

      // save to disk
      batch.message("saving the collection");
      batch.usage("couldn't sync coll ");

    // we got a list of files, not a collection
    } else {
      batch.message("transducing transient collection");
      try {
        coll = batch.transduce(fileNames);
      } catch(JapeException e) {
        batch.usage("oops (2): " + e.toString());
      }
    }

    // we won! we won! we can smash up all the computers now!
    batch.message("done");
//    System.exit(0);

  } // main

  /** Whether to print progress messages or not. */
  private boolean verbose = false;

  /** Set verbosity. */
  public void setVerbose(boolean turtleSoup) { verbose = turtleSoup; }

  /** You got something wrong, dumbo. */
  public void usage(String errorMessage) {
    String usageMessage =
      "usage: java gate.jape.Batch.main [-v] " +
        "-j japefile(.ser|.jape|.jar) " +
        "(-c CollectionName | filenames)";

    Err.println(errorMessage);
    Err.println(usageMessage);
//    System.exit(1);

  } // usage

  /** Hello? Anybody there?? */
  public void message(String mess) {
    if(verbose) Out.println("Batch: " + mess);
  } // message

  //StatusReporter Implementation
  public void addStatusListener(StatusListener listener){
    myStatusListeners.add(listener);
  }
  public void removeStatusListener(StatusListener listener){
    myStatusListeners.remove(listener);
  }
  protected void fireStatusChangedEvent(String text){
    Iterator listenersIter = myStatusListeners.iterator();
    while(listenersIter.hasNext())
      ((StatusListener)listenersIter.next()).statusChanged(text);
  }

  //ProcessProgressReporter implementation
  public void addProcessProgressListener(ProgressListener listener){
    transducer.addProcessProgressListener(listener);
  }
  public void removeProcessProgressListener(ProgressListener listener){
    transducer.removeProcessProgressListener(listener);
  }
  //ProcessProgressReporter implementation ends here

  /** Are we initialising from a resource? */
  private boolean fromResource = false;

  /** Path to the resources tree */
  private String resPath = null;

  private List myProgressListeners = new LinkedList();
  private List myStatusListeners = new LinkedList();

} // class Batch

// $Log$
// Revision 1.10  2000/10/10 15:36:35  oana
// Changed System.out in Out and System.err in Err;
// Added the DEBUG variable seted on false;
// Added in the header the licence;
//
// Revision 1.9  2000/07/18 12:09:33  valyt
// Removed muse from the gate tree
//
// Revision 1.8  2000/07/12 17:55:39  valyt
// *** empty log message ***
//
// Revision 1.7  2000/07/04 14:37:39  valyt
// Added some support for Jape-ing in a different annotations et than the default one;
// Changed the L&F for the JapeGUI to the System default
//
// Revision 1.6  2000/07/03 21:00:59  valyt
// Added StatusBar and ProgressBar support for tokenisation & Jape transduction
// (it looks great :) )
//
// Revision 1.5  2000/06/09 16:54:33  hamish
// support for grammars coming from resources
//
// Revision 1.4  2000/05/05 11:17:47  hamish
// use new parser constructor for streams
//
// Revision 1.3  2000/05/05 10:32:25  hamish
// added some error handling
//
// Revision 1.2  2000/05/03 18:06:39  hamish
// added construction from InputStream
//
// Revision 1.1  2000/02/23 13:46:04  hamish
// added
//
// Revision 1.1.1.1  1999/02/03 16:23:01  hamish
// added gate2
//
// Revision 1.8  1998/10/30 14:06:43  hamish
// added getTransducer
//
// Revision 1.7  1998/10/29 12:03:23  hamish
// completely rewrittern, to handle .ser and .jar as
// well as .jape, and to encapsulate a transducer to allow
// repeated applications; methods for documents, colls and files and
// strings added
//
// Revision 1.6  1998/08/19 20:21:37  hamish
// new RHS assignment expression stuff added
//
// Revision 1.5  1998/08/18 12:43:05  hamish
// fixed SPT bug, not advancing newPosition
//
// Revision 1.4  1998/08/17 10:26:18  hamish
// not much
//
// Revision 1.3  1998/08/12 19:05:41  hamish
// fixed multi-part CG bug; set reset to real reset and fixed multi-doc bug
//
// Revision 1.2  1998/08/12 15:39:31  hamish
// added padding toString methods
//
// Revision 1.1  1998/08/10 14:16:36  hamish
// fixed consumeblock bug and added batch.java