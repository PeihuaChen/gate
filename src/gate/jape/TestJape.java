/*
 *	TestJape.java
 *
 *	Hamish Cunningham, 23/Feb/00
 *
 *	$Id$
 */

package gate.jape;

import java.util.*;
import java.io.*;
import java.text.*;
import junit.framework.*;

import gate.*;
import gate.util.*;
import gate.annotation.*;


/** Tests for the Corpus classes
  */
public class TestJape extends TestCase
{
  /** Construction */
  public TestJape(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
    //System.out.println("TestJape.setUp()");
  } // setUp

  /** Test using the large "combined" grammar from the gate/resources
    * tree.
    */
  public void _testCombined() throws IOException, GateException {
    DoTestBigGrammar("AveShort");
/*
    Corpus c = Transients.newCorpus("TestJape corpus");
    c.add(
      Transients.newDocument(Files.getResourceAsString("texts/doc0.html"))
    );

    //add some annotations on the first (only) document in corpus c
    Document doc = (Document) c.first();
    AnnotationSet defaultAS = doc.getAnnotations();
    FeatureMap feat = Transients.newFeatureMap();
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
    //System.out.println(batch.getTransducer());

    // check the results
    doc = (Document)c.first();
  */
  } // testCombined()

  /** Batch run */
  public void testBatch() throws JapeException, IOException {
    Corpus c = Transients.newCorpus("TestJape corpus");
    c.add(
      Transients.newDocument(Files.getResourceAsString("texts/doc0.html"))
    );
    //add some annotations on the first (only) document in corpus c
    Document doc = (Document)c.first();
    AnnotationSet defaultAS = doc.getAnnotations();
    try{
      FeatureMap feat = Transients.newFeatureMap();
//      defaultAS.add(new Long( 0), new Long( 2), "A",feat);
      defaultAS.add(new Long( 2), new Long( 4), "A",feat);
//      defaultAS.add(new Long( 4), new Long( 6), "A",feat);
//      defaultAS.add(new Long( 6), new Long( 8), "A",feat);
      defaultAS.add(new Long( 4), new Long(6), "B",feat);
//      defaultAS.add(new Long(10), new Long(12), "B",feat);
//      defaultAS.add(new Long(12), new Long(14), "B",feat);
//      defaultAS.add(new Long(14), new Long(16), "B",feat);
//      defaultAS.add(new Long(16), new Long(18), "B",feat);
      defaultAS.add(new Long(6), new Long(8), "C",feat);
      defaultAS.add(new Long(8), new Long(10), "C",feat);
//      defaultAS.add(new Long(22), new Long(24), "C",feat);
//      defaultAS.add(new Long(24), new Long(26), "C",feat);
    }catch(gate.util.InvalidOffsetException ioe){
      ioe.printStackTrace(System.err);
    }

    // run the parser test
    Batch batch = null;
//    String japeFileName = "/gate/jape/Test11.jape";
    String japeFileName = "jape/TestABC.jape";
//    String japeFileName = "/gate/jape/Country.jape";
    InputStream japeFileStream = Files.getResourceAsStream(japeFileName);
    if(japeFileStream == null)
      throw new JapeException("couldn't open " + japeFileName);
    batch = new Batch(japeFileStream);
    // test code: print the first line of the jape stream
    // System.out.println(
    //   new BufferedReader(new InputStreamReader(japeFileStream)).readLine()
    // );

    // test the transducers
    batch.transduce(c);
    // check the results
    doc = (Document)c.first();
//    defaultAS = doc.getAnnotations();
//    System.out.println(defaultAS);
  } // testBatch()

  public void DoTestBigGrammar(String textName){
    long startCorpusLoad = 0, startCorpusTokenization = 0,
         startJapeFileOpen = 0, startCorpusTransduce = 0,
         endProcess = 0;
    System.out.print("Procesing " + textName + "...\n" +
                     "Started at: " + (new Date()) + "\n");
    startCorpusLoad = (new Date()).getTime();
    System.out.print("Loading corpus... ");
    Corpus corpus = Transients.newCorpus("Jape Corpus");
    try{
    corpus.add(Transients.newDocument(
        Files.getResourceAsString("jape/InputTexts/" + textName)));
    }catch(IOException ioe){
      ioe.printStackTrace(System.err);
    }

    if(corpus.isEmpty()){
      System.err.println("Missing corpus !");
      return;
    }

    //tokenize all documents
    DefaultTokeniser tokeniser = new DefaultTokeniser();
    startCorpusTokenization = (new Date()).getTime();
    System.out.print(": " +
                       (startCorpusTokenization - startCorpusLoad) +
                       "ms\n");

    System.out.print("Tokenizing the corpus... ");
    int progress = 0;
    int docCnt = corpus.size();
    Iterator docIter = corpus.iterator();
    Document currentDoc;
    while(docIter.hasNext()){
      currentDoc = (Document)docIter.next();
      tokeniser.tokenise(currentDoc, false);
    }
    //do the jape stuff
    Gate.init();
    startJapeFileOpen = (new Date()).getTime();
    System.out.print(": " + (startJapeFileOpen - startCorpusTokenization) +
                     "ms\n");
    try{
      System.out.print("Opening Jape grammar... ");
//      Batch batch = new Batch("jape/combined/", "main.jape");
//      Batch batch = new Batch("z:/gate2/src/gate/resources/jape/combined/main.jape");
      Batch batch = new Batch("jape/", "Country.jape");
      startCorpusTransduce = (new Date()).getTime();
      System.out.print(": " + (startCorpusTransduce - startJapeFileOpen) +
                       "ms\n");
      System.out.print("Transducing the corpus... ");
      batch.transduce(corpus);
      endProcess = (new Date()).getTime();
      System.out.print(": " + (endProcess - startCorpusTransduce) + "ms\n");
    }catch(JapeException je){
      je.printStackTrace(System.err);
    }
  }


  public void tokenize(Document doc){
    String content = doc.getContent().toString();
    BreakIterator bi = BreakIterator.getWordInstance();
    bi.setText(content);
    int start = bi.first();
    FeatureMap fm;
    try{
      for (int end = bi.next();
           end != BreakIterator.DONE;
           start = end, end = bi.next())
      {
        if(!Character.isWhitespace(content.charAt(start))){
          fm = Transients.newFeatureMap();
          fm.put("string", content.substring(start, end));
          doc.getAnnotations().add(new Long(start),
                                   new Long(end),
                                   "Token", fm);
//System.out.println("Token: " + content.substring(start, end));
        }
      }//for
    }catch(InvalidOffsetException ioe){
    }
  }

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestJape.class);
  } // suite

  //main method for running this test as a standalone test
  public static void main(String[] args) {
    try{
      TestJape testJape = new TestJape("Test Jape");
      testJape.setUp();
      testJape._testCombined();
//      if(args.length < 1) testJape.DoTestBigGrammar("AveShort");
 //     else testJape.DoTestBigGrammar(args[0]);
    }catch(Exception e){
      e.printStackTrace(System.err);
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
////  System.out.println("TestJape: " + coll.firstDocument().selectAnnotations(
////    "number", new FeatureMap()));
////    coll.sync();
////  } catch(Exception e) { e.printStackTrace(); }
////
////      System.out.println("\n\nWow! We reached the end without crashing!!!\n");
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
////        System.out.println("Test4.ser not found");
////
////      MultiPhaseTransducer t = null;
////      try {
////        FileInputStream fis = new FileInputStream(f.getPath());
////        ObjectInputStream ois = new ObjectInputStream(fis);
////        t = (MultiPhaseTransducer) ois.readObject();
////        ois.close();
////      } catch (Exception ex) {
////      	System.err.println(
////          "Can't read from " + f.getName() + ": " + ex.toString()
////        );
////      }
////      try { t.transduce(coll.firstDocument()); }
////      catch(Exception e) {
////        System.err.println("error transducing: " + e.toString());
////      }
////
////    } // testCompilerOutput
////
////} // class TestJape
