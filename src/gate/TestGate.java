/*
 *	TestGate.java
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
 *	Hamish Cunningham, 21/Jan/00
 *
 *	$Id$
 */

package gate;

import java.util.*;
import junit.framework.*;

import gate.annotation.*;
import gate.corpora.*;
import gate.creole.*;
import gate.util.*;
import gate.db.*;
import gate.jape.*;
import gate.fsm.*;
import gate.xml.*;
import gate.email.*;
import gate.html.*;
import gate.sgml.*;


/** Top-level entry point for GATE test suite.
  * "main" will run the JUnit test runner interface.
  * Use a "-t" flag to run the textual UI test runner (useful for
  * debugging, as there's less confusion to do with threads and
  * class loaders!).
  */
public class TestGate
{
  /**
    *  This field is "final static" because it brings in
    *  the advantage of dead code elimination
    *  When DEBUG is set on false the code that it guardes will be eliminated
    *  by the compiler. This will spead up the progam a little bit.
    */
  private static final boolean DEBUG = false;

  /** Main routine. */
  public static void main(String[] args) throws Exception {

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
  public static Test suite() throws Exception {
    // inialise the library.
    // normally we would also call initCreoleRegister, but that's
    // done in TestCreole
    Gate.init();

    TestSuite suite = new TestSuite();
    suite.addTest(TestCreole.suite());
    suite.addTest(TestXSchema.suite()); //*
    suite.addTest(TestFiles.suite());
    suite.addTest(TestXml.suite());
    suite.addTest(TestHtml.suite());
    suite.addTest(TestSgml.suite());
    suite.addTest(TestEmail.suite());
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
    suite.addTest(TestDB.suite());      //*/

    return suite;
  } // suite

} // class TestGate
