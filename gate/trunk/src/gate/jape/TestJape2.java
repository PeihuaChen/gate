
//Title:        TestJape2.java (Java Annotation Patterns Engine)
//Version:      $Id$
//Copyright:    Copyright (c) 1998
//Author:       Hamish Cunningham
//Company:      NLP Group, DCS, Univ. of Sheffield
//Description:  Test class for JAPE.

package gate.jape;

import gate.jape.parser.*;
import gate.*;
import gate.annotation.*;
import gate.util.*;
import java.util.*;
import java.io.*;
import com.objectspace.jgl.*;

/**
  * Second test harness for JAPE.
  * Uses the Sheffield Tokeniser and Gazetteer, and must be run
  * from the gate2 directory.
  * @author Hamish Cunningham
  */
public class TestJape2 {

  /** How much noise to make. */
  static private boolean verbose = false;


  /** Take a list of text files and a collection name, and
    * call tokeniser/gazetteer/jape on them, creating the
    * collection.
    */
  static public void main(String[] args) {

    // turn debug output on/off
    //Debug.setDebug(true);
    //Debug.setDebug(AnnotationSet.class, true);
    //Debug.setDebug(BasicPatternElement.class, true);
    //Debug.setDebug(ComplexPatternElement.class, true);
    //Debug.setDebug(ConstraintGroup.class, true);
    //Debug.setDebug(SinglePhaseTransducer.class, true);

    // variables to parse the command line options into
    String collName = null;
    String japeName = null;
    Array fileNames = null;

    // process options
    for(int i=0; i<args.length; i++) {
      if(args[i].equals("-c") && ++i < args.length) // -c = coll name
        collName = args[i];
      else if(args[i].equals("-j") && ++i < args.length) // -j: .jape name
        japeName = args[i];
      else if(args[i].equals("-v")) // -v = verbose
        verbose = true;
      else { // a list of files
        fileNames = new Array();
        do {
          fileNames.add(args[i++]);
        } while(i < args.length);
      }
    } // for each arg

    // did they give valid options?
    message("checking options");
    if(collName == null || japeName == null || fileNames == null)
      usage("you must supply collection, transducer and file names");

    // create a collection and run the tokeniser
    message("creating coll, tokenising and gazetteering");
    Corpus coll = tokAndGaz(collName, fileNames);

    // run the parser test
    message("parsing the .jape file (or deserialising the .ser file)");
    Batch batch = null;
    try { batch = new Batch(japeName); }
    catch(JapeException e) {
      usage("can't create transducer " + e.getMessage());
    }
    /*Transducer transducer = parseJape(japeName);
    //System.out.println(transducer);
    if(transducer == null)
      System.exit(1);*/

    // test the transducers from the parser
    message("running the transducer");
    try { batch.transduce(coll); } catch(JapeException e) {
      usage("couldn't run transducer " + e.getMessage());
    }
    //runTransducer(transducer, coll);
    //System.out.println(transducer);

    message("done\n\r");
    System.exit(0);

  } // main


  /**
    * Create a collection and put tokenised and gazetteered docs in it.
    */
  static public Corpus tokAndGaz(String collName, Array fileNames) {

    // create or overwrite the collection
    Corpus collection = null;
    File collDir = new File(collName);
    collection = Transients.newCorpus(
      collDir.getAbsolutePath()
    );


    // add all the documents
    for(ArrayIterator i = fileNames.begin(); ! i.atEnd(); i.advance()) {
      String fname = (String) i.get();

      File f = new File(fname);
      FeatureMap attrs = Transients.newFeatureMap();
      Document doc = null;

      try {
        AnnotationSet annots = new AnnotationSetImpl(doc);
        collection.add(
          Transients.newDocument(f.getAbsolutePath())
        );
      } catch(IOException e) {
	      e.printStackTrace();
      }

/*
      // Tokenise the document
      Tokeniser tokeniser = new Tokeniser(doc, Tokeniser.HMM);
      try { tokeniser.hmmTokenSequence(); }
      catch(sheffield.creole.tokeniser.ParseException ex) {
	      ex.printStackTrace();
	      return null;
      } catch (CreoleException ex) {
	      ex.printStackTrace();
	      return null;
      }

      // Gazetteer the document
      gate.creole.Annotator gazetteer = new GazetteerAnnotator();
      gazetteer.annotate(doc, null);
*/
    } // for each doc name

    // return the annotated collection
    return collection;

  } //tokAndGaz


  /**
    * Must be run from the gate2 directory.
    * Parse the .jape file.
    */
/*  static public Transducer parseJape(String japeName) {
    Transducer transducer = null;

    if(japeName.endsWith(".ser")) { // it's compiled already
      message("deserialising " + japeName);
      File f = new File(japeName);
      if(! f.exists())
        System.out.println(japeName + " not found");

      try {
        FileInputStream fis = new FileInputStream(f.getPath());
        ObjectInputStream ois = new ObjectInputStream(fis);
        transducer = (Transducer) ois.readObject();
        ois.close();
      } catch (Exception ex) {
        System.err.println(
          "Can't read from " + f.getName() + ": " + ex.toString()
        );
      }
    } else { // parse it
      message("parsing " + japeName);
      try {
        ParseCpsl cpslParser = new ParseCpsl(japeName);
        transducer = cpslParser.MultiPhaseTransducer();
      } catch(IOException e) {
        e.printStackTrace();
      } catch(gate.jape.parser.ParseException ee) {
        System.err.println("Error parsing transducer: " + ee.getMessage());
      }
    }

    return transducer;
  } // parseJape


  static public void runTransducer(
    Transducer transducer, Corpus coll
  ) {

    try {
      Document doc = coll.firstDocument();
      do {
        message("doing document " + doc.getId());
        transducer.transduce(doc);
        // System.out.println(transducer.toString());
      } while( (doc = coll.nextDocument()) != null );
    } catch(JdmException e) {
      e.printStackTrace();
    } catch(JapeException e) {
      e.printStackTrace();
    }
  } // runTransducer
*/

  /** You got something wrong, dumbo. */
  public static void usage(String errorMessage) {
    String usageMessage =
      "usage: java gate.jape.TestJape2.main [-v] " +
        "-j JapePatternFile -c CollectionName FileName(s)";

    System.err.println(errorMessage);
    System.err.println(usageMessage);
    System.exit(1);

  } // usage


  /** Hello? Anybody there?? */
  public static void message(String mess) {
    if(verbose) System.out.println("TestJape2: " + mess);
  } // message

} // class TestJape2


// $Log$
// Revision 1.1  2000/02/23 13:46:12  hamish
// added
//
// Revision 1.1.1.1  1999/02/03 16:23:03  hamish
// added gate2
//
// Revision 1.9  1998/10/29 12:13:55  hamish
// reorganised to use Batch
//
// Revision 1.8  1998/10/01 16:06:41  hamish
// new appelt transduction style, replacing buggy version
//
// Revision 1.7  1998/09/26 09:19:21  hamish
// added cloning of PE macros
//
// Revision 1.6  1998/09/23 12:48:03  hamish
// negation added; noncontiguous BPEs disallowed
//
// Revision 1.5  1998/09/17 12:53:09  hamish
// fixed for new tok; new construction pattern
//
// Revision 1.4  1998/09/17 10:24:05  hamish
// added options support, and Appelt-style rule application
//
// Revision 1.3  1998/08/19 20:21:46  hamish
// new RHS assignment expression stuff added
//
// Revision 1.2  1998/08/18 14:37:45  hamish
// added some messages
//
// Revision 1.1  1998/08/18 12:43:11  hamish
// fixed SPT bug, not advancing newPosition
