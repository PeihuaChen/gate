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
import gnu.getopt.*;

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
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Main routine for the GATE test suite.
    * Command-line arguments:
    * <UL>
    * <LI>
    * <B>-a</B> means run the test runner in automatic class reload mode
    * <LI>
    * <B>-n</B> means assume there's no net connection
    * <LI>
    * <B>-t</B> means run the test runner in text mode
    * </UL>
    */
  public static void main(String[] args) throws Exception {
    boolean textMode = false;
    boolean autoloadingMode = false;

    // process command-line options
    Getopt g = new Getopt("GATE test suite", args, "tna");
    int c;
    while( (c = g.getopt()) != -1 )
      switch(c) {
        case 't':
          textMode = true;
          break;
        case 'n':
          Gate.setNetConnected(false);
          break;
        case 'a':
          autoloadingMode = true;
          break;
        case '?':
          // leave the warning to getopt
          return;
        default:
          Err.prln("getopt() returned " + c + "\n");
      } // switch

    // set up arguments for the JUnit test runner
    String junitArgs[] = new String[1];
    junitArgs[0] = "gate.TestGate";
    // use the next line if you're running with output to console in text mode:
    // junitArgs[1] = "-wait";

    // execute the JUnit test runner
    if(textMode) { // text runner mode
      junit.textui.TestRunner.main(junitArgs);
    } else if(autoloadingMode) { // autoloading mode
      // NOTE: the DB tests fail under this one (doesn't load oracle driver,
      // even after the Class.forName call)
      Class clazz = null;
      clazz = Class.forName("oracle.jdbc.driver.OracleDriver");
      clazz = null;
      junit.ui.LoadingTestRunner.main(junitArgs);
    } else { // by default us the single-run GUI version
      junit.ui.TestRunner.main(junitArgs);
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
    suite.addTest(TestCreole.suite());  //*
    suite.addTest(TestXSchema.suite());
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
