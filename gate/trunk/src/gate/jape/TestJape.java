/*
 *  TestJape.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 23/Feb/00
 *
 *  $Id$
 */

package gate.jape;

import java.util.*;
import java.io.*;
import java.text.*;
import junit.framework.*;

import gate.*;
import gate.util.*;
import gate.annotation.*;
import gate.creole.tokeniser.*;
import gate.creole.gazetteer.*;
import gate.creole.*;



/** Tests for the Corpus classes
  */
public class TestJape extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestJape(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
    //Out.println("TestJape.setUp()");
  } // setUp

  /** Test using the large "combined" grammar from the gate/resources
    * tree.
    */
  public void _testCombined() throws IOException, GateException {
    DoTestBigGrammar("AveShort");

    /*
    Corpus c = Factory.newCorpus("TestJape corpus");
    c.add(
      Factory.newDocument(Files.getResourceAsString("texts/doc0.html"))
    );

    //add some annotations on the first (only) document in corpus c
    Document doc = (Document) c.first();
    AnnotationSet defaultAS = doc.getAnnotations();
    FeatureMap feat = Factory.newFeatureMap();
    defaultAS.add(new Long( 2), new Long( 4), "A",feat);
    defaultAS.add(new Long( 4), new Long(6), "B",feat);
    defaultAS.add(new Long(6), new Long(8), "C",feat);
    defaultAS.add(new Long(8), new Long(10), "C",feat);

    // run the parser test
    Gate.init();
    Batch batch = null;
    batch = new Batch("jape/combined/", "main.jape");

    // test the transducers
    batch.transduce(c);
    //Out.println(batch.getTransducer());

    // check the results
    doc = (Document)c.first();
    */
  } // testCombined()

  /** Batch run */
  public void testBatch()
  throws JapeException, ResourceInstantiationException, IOException
  {
    Corpus c = Factory.newCorpus("TestJape corpus");
    c.add(
      Factory.newDocument(Files.getGateResourceAsString("texts/doc0.html"))
    );
    //add some annotations on the first (only) document in corpus c
    Document doc = (Document)c.first();
    AnnotationSet defaultAS = doc.getAnnotations();

    try {
      FeatureMap feat = Factory.newFeatureMap();
      // defaultAS.add(new Long( 0), new Long( 2), "A",feat);
      defaultAS.add(new Long( 2), new Long( 4), "A",feat);
      // defaultAS.add(new Long( 4), new Long( 6), "A",feat);
      // defaultAS.add(new Long( 6), new Long( 8), "A",feat);
      defaultAS.add(new Long( 4), new Long(6), "B",feat);
      // defaultAS.add(new Long(10), new Long(12), "B",feat);
      // defaultAS.add(new Long(12), new Long(14), "B",feat);
      // defaultAS.add(new Long(14), new Long(16), "B",feat);
      // defaultAS.add(new Long(16), new Long(18), "B",feat);
      defaultAS.add(new Long(6), new Long(8), "C",feat);
      defaultAS.add(new Long(8), new Long(10), "C",feat);
      // defaultAS.add(new Long(22), new Long(24), "C",feat);
      // defaultAS.add(new Long(24), new Long(26), "C",feat);
    } catch(gate.util.InvalidOffsetException ioe) {
      ioe.printStackTrace(Err.getPrintWriter());
    }
/*
    // run the parser test
    Batch batch = null;
    // String japeFileName = "/gate/jape/Test11.jape";
    String japeFileName = Files.getResourcePath() + "/jape/TestABC.jape";
    // String japeFileName = "/gate/jape/Country.jape";
    InputStream japeFileStream = Files.getResourceAsStream(japeFileName);
    if(japeFileStream == null)
      throw new JapeException("couldn't open " + japeFileName);
*/
    Batch batch = new Batch(TestJape.class.getResource(
              Files.getResourcePath() + "/jape/TestABC.jape"), "UTF-8");
    // test code: print the first line of the jape stream
    // Out.println(
    //   new BufferedReader(new InputStreamReader(japeFileStream)).readLine()
    // );

    // test the transducers
    batch.transduce(c);
    // check the results
    doc = (Document)c.first();
    // defaultAS = doc.getAnnotations();
    // Out.println(defaultAS);
  } // testBatch()

  public void DoTestBigGrammar(String textName) throws GateException {
    long startCorpusLoad = 0, startCorpusTokenization = 0,
         startGazeteerLoad = 0, startLookup = 0,
         startJapeFileOpen = 0, startCorpusTransduce = 0,
         endProcess = 0;
    Out.print("Procesing " + textName + "...\n" +
                     "Started at: " + (new Date()) + "\n");
    startCorpusLoad = System.currentTimeMillis();
    Out.print("Loading corpus... ");
    Corpus corpus = Factory.newCorpus("Jape Corpus");
    try {
    corpus.add(Factory.newDocument(
        Files.getGateResourceAsString("jape/InputTexts/" + textName)));
    } catch(IOException ioe) {
      ioe.printStackTrace(Err.getPrintWriter());
    }

    if(corpus.isEmpty()) {
      Err.println("Missing corpus !");
      return;
    }

    //tokenize all documents
    gate.creole.tokeniser.DefaultTokeniser tokeniser = null;
    try {
      //create a default tokeniser
      FeatureMap params = Factory.newFeatureMap();
      tokeniser = (DefaultTokeniser) Factory.createResource(
                            "gate.creole.tokeniser.DefaultTokeniser", params);
      /*Files.getResourceAsStream("creole/tokeniser/DefaultTokeniser.rules"));*/
    } catch(ResourceInstantiationException re) {
      re.printStackTrace(Err.getPrintWriter());
    }
    startCorpusTokenization = System.currentTimeMillis();
    Out.print(": " +
                       (startCorpusTokenization - startCorpusLoad) +
                       "ms\n");

    Out.print("Tokenizing the corpus... ");
    int progress = 0;
    int docCnt = corpus.size();
    Iterator docIter = corpus.iterator();
    Document currentDoc;
    while(docIter.hasNext()){
      currentDoc = (Document)docIter.next();
      tokeniser.setDocument(currentDoc);
      //use the default anotation set
      tokeniser.setAnnotationSetName(null);
      tokeniser.run();
    }

    startJapeFileOpen = System.currentTimeMillis();
    Out.print(": " + (startJapeFileOpen - startCorpusTokenization) +
                     "ms\n");

    //Do gazeteer lookup
    gate.creole.gazetteer.DefaultGazetteer gazeteer = null;
    startGazeteerLoad = startLookup = System.currentTimeMillis();
    Out.print("Loading gazeteer lists...");
    try {
      //create a default gazetteer
      FeatureMap params = Factory.newFeatureMap();
      gazeteer = (DefaultGazetteer) Factory.createResource(
                            "gate.creole.gazetteer.DefaultGazetteer", params);
      gazeteer.init();
      startLookup = System.currentTimeMillis();
      Out.print(": " +
                         (startLookup - startGazeteerLoad) +
                         "ms\n");

      Out.print("Doing gazeteer lookup... ");
      docIter = corpus.iterator();
      while(docIter.hasNext()){
        currentDoc = (Document)docIter.next();
        gazeteer.setDocument(currentDoc);
        gazeteer.run();
      }
    } catch(ResourceInstantiationException re) {
      Err.println("Cannot read the gazeteer lists!" +
                         "\nAre the Gate resources in place?\n" + re);
    }

    startJapeFileOpen = System.currentTimeMillis();
    Out.print(": " + (startJapeFileOpen - startLookup) +
                     "ms\n");


    //do the jape stuff
    Gate.init();


    try {
      Out.print("Opening Jape grammar... ");
      Batch batch = new Batch(TestJape.class.getResource(
        Files.getResourcePath() + "/jape/combined/main.jape"), "UTF-8");
      /*
      Batch batch = new Batch("jape/combined/", "brian-soc-loc1.jape");
      Batch batch =
        new Batch("z:/gate/src/gate/resources/jape/combined/main.jape");
      Batch batch = new Batch("jape/", "Country.jape");
      */
      startCorpusTransduce = (new Date()).getTime();
      Out.print(": " + (startCorpusTransduce - startJapeFileOpen) +
                       "ms\n");
      Out.print("Transducing the corpus... ");
      batch.transduce(corpus);
      endProcess = System.currentTimeMillis();
      Out.print(": " + (endProcess - startCorpusTransduce) + "ms\n");
    } catch(JapeException je) {
      je.printStackTrace(Err.getPrintWriter());
    }
  }


  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestJape.class);
  } // suite

  //main method for running this test as a standalone test
  public static void main(String[] args) {
    for(int i = 0; i < 6; i++){
    System.gc();
    Out.println("Run " + i + "   ==============");
      try{
        TestJape testJape = new TestJape("Test Jape");
        testJape.setUp();
        if(args.length < 1) testJape.DoTestBigGrammar("AveShort");
       else testJape.DoTestBigGrammar(args[0]);
      } catch(Exception e) {
        e.printStackTrace(Err.getPrintWriter());
      }
    }
  }
} // class TestJape





//// OLD VERSION:
////
////  //Title:        TestJape.java (Java Annotation Patterns Engine)
////  //Version:      $Id$
////  //Copyright:    Copyright (c) 1998
////  //Author:       Hamish Cunningham
////  //Company:      NLP Group, DCS, Univ. of Sheffield
////  //Description:  Test class for JAPE.
////
//// package gate.jape;
////
////  import gate.jape.parser.*;
////  import gate.*;
////  import gate.annotation.*;
////  import gate.util.*;
////  // import sheffield.creole.tokeniser.*;
////  import java.util.Enumeration;
////  import java.io.*;
////  import com.objectspace.jgl.*;
////
////  /**
////    * A test harness for JAPE. Uses the Sheffield Tokeniser, and must be run
////    * from the gate2 directory.
////    * @author Hamish Cunningham
////    */
//// public class TestJape {
////
////    static public void main(String[] args) {
////      // initialise GATE
////      Gate.init();
////
////      // turn debug output on/off
////      //Debug.setDebug(true);
////      //Debug.setDebug(BasicPatternElement.class, true);
////
////      // create a collection and run the tokeniser
////      Corpus coll = tokenise();
////
////      // test compiler output
////      testCompilerOutput(coll);
////
////      // got anything?
////      try {
////        //Debug.pr(this,
////        //  "TestJape.main: annots are: " +
////        //  coll.firstDocument().getAnnotations().toString()
////        //);
////        Debug.pr(TestJape.class, "TestJape.main: first annotation is: ");
////        JdmAnnotation annot = coll.firstDocument().getAnnotations().nth(0);
////        Debug.pr(TestJape.class, "TestJape.main: " + annot.toString());
////
////      } catch(Exception e) { e.printStackTrace(); }
////
////      // run the backend test
////      testBackEnd(coll);
////
////      // run the parser test
////      Transducer[] transducers = testParser(coll);
////
////      // test the transducers from the parser
////      testTransducers(transducers, coll);
////
////  try {
////  Out.println("TestJape: " + coll.firstDocument().selectAnnotations(
////    "number", new FeatureMap()));
////    coll.sync();
////  } catch(Exception e) { e.printStackTrace(); }
////
////      Out.println("\n\nWow! We reached the end without crashing!!!\n");
////      System.exit(0);
////    } // main
////
////
////    /**
////      * Create a collection and put a tokenised doc in it.
////      */
////    static public Corpus tokenise() {
////      // get a collection and document (delete old doc first if exists)
////      File testDoc1Source =
////        new File("jape/testdocs" + File.separator + "JapeTestDoc.txt");
////      File collectionName = new File("TestJapeCollection");
////      FeatureMap cattrs = new FeatureMap();
////      Corpus collection = null;
////      Document testDoc1 = null;
////      try {
////        if(collectionName.exists())
////          collection = new Corpus(collectionName.getAbsolutePath());
////        else
////          collection = new Corpus(
////            collectionName.getAbsolutePath(),
////            cattrs
////          );
////        collection.removeDocument("JapeTestDoc.txt");
////        AnnotationSet cannot_set0 = new AnnotationSetImpl(doc)();
////        testDoc1 = collection.createDocument(
////          testDoc1Source.getAbsolutePath(), null, cannot_set0, cattrs
////        );
////      } catch(JdmException e) {
////        e.printStackTrace();
////      }
////      // Tokenise the document
////  /*
////      Tokeniser tokeniser = new Tokeniser(testDoc1, Tokeniser.HMM);
////      try { tokeniser.hmmTokenSequence(); }
////      catch(sheffield.creole.tokeniser.ParseException ex) {
////        ex.printStackTrace();
////        return null;
////      } catch (CreoleException ex) {
////        ex.printStackTrace();
////        return null;
////      }
////  */
////
////      // return the result
////      return collection;
////    } //tokenise
////
////
////    /**
////      * Must be run from the gate2 directory.
////      * Parse jape/grammars/Test1.cpsl,
////      * then tokenise jape/testdocs/JapeTestDoc.txt
////      * in collection TestJapeCollection, and run the patterns.
////      */
////    static public Transducer[] testParser(Corpus coll) {
////      Transducer[] transducers = new Transducer[2];
////
////      // parse the Test1.cpsl grammar
////      try {
////        ParseCpsl cpslParser =
////          new ParseCpsl("jape/grammars" + File.separator + "Test1.cpsl");
////        if(cpslParser != null)
////          transducers[0] = cpslParser.MultiPhaseTransducer();
////      } catch(IOException e) {
////        e.printStackTrace();
////      } catch(gate.jape.parser.ParseException ee) {
////        ee.printStackTrace();
////      }
////
////      // parse the Test2.cpsl grammar
////      try {
////        File testGrammar2 =
////          new File("jape/grammars" + File.separator + "Test2.cpsl");
////        ParseCpsl cpslParser = new ParseCpsl(new FileReader(testGrammar2));
////        transducers[1] = cpslParser.SinglePhaseTransducer();
////      } catch(FileNotFoundException e) {
////        e.printStackTrace();
////      } catch(gate.jape.parser.ParseException ee) {
////        ee.printStackTrace();
////      }
////
////      return transducers;
////    } // testParser
////
////
////    /** Test the backend classes (that are normally constructed by the parser -
////      * in this case we construct them manually).
////      * This example is based on this rule:
////      * <PRE>
////      *   Rule: Numbers
////      *   ( {Token.kind == "otherNum" } )*:numberList
////      *   -->
////      *   :numberList{ ... see gate2/jape/grammars/ExampleRhs.txt ... }
////      * </PRE>
////      */
////    public static void testBackEnd(Corpus coll) {
////      Constraint constraint = new Constraint("Token");
////      try { constraint.addAttribute(new JdmAttribute("kind", "otherNum"));
////      } catch(Exception e) { e.printStackTrace(); }
////
////      BasicPatternElement bpe = new BasicPatternElement();
////      bpe.addConstraint(constraint);
////      ConstraintGroup cg1 = new ConstraintGroup();
////      cg1.addPatternElement(bpe);
////
////      ComplexPatternElement cpe = new ComplexPatternElement(
////        cg1, JapeConstants.KLEENE_PLUS, "numberList"
////      );
////
////      ConstraintGroup cg2 = new ConstraintGroup();
////      cg2.addPatternElement(cpe);
////
////      LeftHandSide lhs = new LeftHandSide(cg2);
////      try {
////        lhs.addBinding("numberList", cpe, new HashSet(), false);
////      } catch(JapeException e) {
////        e.printStackTrace();
////      }
////
////      StringBuffer rhsString = new StringBuffer();
////      try {
////        File f = new File("jape/grammars/ExampleRhs.txt");
////        FileReader fr = new FileReader(f);
////        while(fr.ready())
////          rhsString.append((char) fr.read());
////      } catch(IOException e) {
////        e.printStackTrace();
////      }
////      RightHandSide rhs = new RightHandSide("TestPhase", "Numbers", lhs);
////      rhs.addBlock("numberList", rhsString.toString());
////      try {
////        rhs.createActionClass();
////      } catch(JapeException e) {
////        e.printStackTrace();
////      }
////      Debug.pr(TestJape.class, "TestJape.main: loaded the action class");
////
////      Rule rule =
////        new Rule("numbers", 0, JapeConstants.DEFAULT_PRIORITY, lhs, rhs);
////      SinglePhaseTransducer transducer =
////        new SinglePhaseTransducer("numberGrammar");
////      transducer.addRule(rule);
////      try {
////        Debug.pr(TestJape.class, "TestJape.main: trying the transducer");
////        transducer.transduce(coll.firstDocument());
////        Debug.pr(
////          TestJape.class, "TestJape.main: " + coll.firstDocument().toString()
////        );
////      } catch(JdmException e) {
////        e.printStackTrace();
////      } catch(JapeException e) {
////        e.printStackTrace();
////      }
////
////      // delete temp files created for Rule RHS actions
////  ///    RightHandSide.cleanUp();
////      Debug.pr(TestJape.class, "TestJape.main: testBackEnd done");
////    } // testBackEnd
////
////    static public void testTransducers(
////      Transducer[] transducers, Corpus coll
////    ) {
////      for(int i=0; i<transducers.length; i++) {
////        Transducer t = transducers[i];
////        try {
////          Debug.pr(
////            TestJape.class, "TestJape.main: trying transducer " + t.getName()
////          );
////          t.transduce(coll.firstDocument());
////        } catch(JdmException e) {
////          e.printStackTrace();
////        } catch(JapeException e) {
////          e.printStackTrace();
////        }
////      } // for
////
////    } // testTransducers
////
////    /** If Test4.ser exists, try running. */
////    static public void testCompilerOutput(Corpus coll) {
////      Debug.pr(TestJape.class, "testing compiler");
////      File f = new File("jape/grammars/Test4.ser");
////      if(! f.exists())
////        Out.println("Test4.ser not found");
////
////      MultiPhaseTransducer t = null;
////      try {
////        FileInputStream fis = new FileInputStream(f.getPath());
////        ObjectInputStream ois = new ObjectInputStream(fis);
////        t = (MultiPhaseTransducer) ois.readObject();
////        ois.close();
////      } catch (Exception ex) {
////        Err.println(
////          "Can't read from " + f.getName() + ": " + ex.toString()
////        );
////      }
////      try { t.transduce(coll.firstDocument()); }
////      catch(Exception e) {
////        Err.println("error transducing: " + e.toString());
////      }
////
////    } // testCompilerOutput
////
////} // class TestJape
