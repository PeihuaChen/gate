/*
 *	TestGate.java
 *
 *	Hamish Cunningham, 21/Jan/00
 *
 *	$Id$
 */

package gate;

import java.util.*;
import junit.framework.*;

import gate.annotation.*;
import gate.corpora.*;
import gate.util.*;
import gate.db.*;
import gate.jape.*;
import gate.fsm.*;
import gate.xml.*;
import gate.html.*;


/** Top-level entry point for GATE test suite.
  * "main" will run the JUnit test runner interface.
  * Use a "-t" flag to run the textual UI test runner (useful for
  * debugging, as there's less confusion to do with threads and
  * class loaders!).
  */
public class TestGate
{
  /** Main routine. */
  public static void main(String[] args) throws ClassNotFoundException {
    Gate.init();

    String a[] = new String[1];
    a[0] = "gate.TestGate";
    // use the next line if you're running with output to console in text mode:
    // a[1] = "-wait";
    if(args.length > 0 && args[0].equals("-t")) // text runner mode
      junit.textui.TestRunner.main(a);
    else {
      junit.ui.TestRunner.main(a);
      // the DB tests fail under this one (doesn't load oracle driver,
      // even after the Class.forName call)
      //Class c = null;
      //c = Class.forName("oracle.jdbc.driver.OracleDriver");
      //c = null;
      //junit.ui.LoadingTestRunner.main(a);
    }
  } // main

  /** GATE test suite. Every test case class has to be
    * registered here.
    */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(TestFiles.suite());
//    suite.addTest(TestFiles.suite());
    suite.addTest(TestXml.suite());
    suite.addTest(TestHtml.suite());
    suite.addTest(TestJdk.suite());
    suite.addTest(TestJape.suite());
    suite.addTest(TestFSM.suite());
    suite.addTest(TestTemplate.suite());
    suite.addTest(TestJacl.suite());
    suite.addTest(TestDocument.suite());
    suite.addTest(TestAnnotation.suite());
    suite.addTest(TestRBTreeMap.suite());
    suite.addTest(TestCorpus.suite());
    suite.addTest(CookBook.suite());
    suite.addTest(TestDB.suite());

    return suite;
  } // suite

} // class TestGate
