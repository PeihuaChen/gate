////  
////  //Title:        TestJape.java (Java Annotation Patterns Engine)
////  //Version:      $Id$
////  //Copyright:    Copyright (c) 1998
////  //Author:       Hamish Cunningham
////  //Company:      NLP Group, DCS, Univ. of Sheffield
////  //Description:  Test class for JAPE.
////  
package gate.jape;
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
public class TestJape {
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
} // class TestJape
////  
////  
////  // $Log$
////  // Revision 1.1  2000/02/23 13:46:12  hamish
////  // added
////  //
////  // Revision 1.1.1.1  1999/02/03 16:23:02  hamish
////  // added gate2
////  //
////  // Revision 1.21  1998/10/30 15:31:08  kalina
////  // Made small changes to make compile under 1.2 and 1.1.x
////  //
////  // Revision 1.20  1998/09/18 16:54:19  hamish
////  // save/restore works except for attribute seq
////  //
////  // Revision 1.19  1998/09/17 16:48:34  hamish
////  // added macro defs and macro refs on LHS
////  //
////  // Revision 1.18  1998/09/17 12:53:07  hamish
////  // fixed for new tok; new construction pattern
////  //
////  // Revision 1.17  1998/08/19 20:21:45  hamish
////  // new RHS assignment expression stuff added
////  //
////  // Revision 1.16  1998/08/18 12:43:10  hamish
////  // fixed SPT bug, not advancing newPosition
////  //
////  // Revision 1.15  1998/08/10 14:16:42  hamish
////  // fixed consumeblock bug and added batch.java
////  //
////  // Revision 1.14  1998/08/07 16:39:18  hamish
////  // parses, transduces. time for a break
////  //
////  // Revision 1.13  1998/08/07 16:18:48  hamish
////  // parser pretty complete, with backend link done
////  //
////  // Revision 1.12  1998/08/07 12:01:48  hamish
////  // parser works; adding link to backend
////  //
////  // Revision 1.11  1998/08/05 21:58:08  hamish
////  // backend works on simple test
////  //
////  // Revision 1.10  1998/08/04 13:51:36  hamish
////  // moved creole packages from uk to sheffield
////  //
////  // Revision 1.9  1998/08/03 21:45:00  hamish
////  // moved parser classes to gate.jape.parser
////  //
////  // Revision 1.8  1998/08/03 19:51:29  hamish
////  // rollback added
////  //
////  // Revision 1.7  1998/07/31 16:50:20  mks
////  // RHS compilation works; it runs - and falls over...
////  //
////  // Revision 1.6  1998/07/31 13:12:29  mks
////  // done RHS stuff, not tested
////  //
////  // Revision 1.5  1998/07/30 12:16:17  mks
////  // reorganising
////  //
////  // Revision 1.4  1998/07/29 20:34:02  hamish
////  // resolved conflict
////  //
////  // Revision 1.3  1998/07/29 15:08:28  kalina
////  // not sure...
////  //
////  // Revision 1.2  1998/07/29 11:07:13  hamish
////  // first compiling version
////  //
////  // Revision 1.1.1.1  1998/07/28 16:37:46  hamish
////  // gate2 lives
