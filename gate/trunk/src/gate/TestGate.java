/*
 *  TestGate.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 21/Jan/00
 *
 *  $Id$
 */

package gate;

import java.util.*;
import junit.framework.*;
import gnu.getopt.*;

import gate.*;
import gate.annotation.*;
import gate.corpora.*;
import gate.creole.*;
import gate.creole.tokeniser.*;
import gate.creole.gazetteer.*;
import gate.db.*;
import gate.jape.*;
import gate.fsm.*;
import gate.xml.*;
import gate.email.*;
import gate.html.*;
import gate.sgml.*;
import gate.util.*;
import gate.config.*;
import gate.persist.*;


/** Top-level entry point for GATE test suite;
  * "main" will run the JUnit test runner interface.
  * <P>
  * Many tests require access to files; generally these files are located
  * on Web servers. In cases where there is no net connection, or the
  * Web servers are down, the test files are searched for in the file system
  * or Jar code base that the system has been loaded from. The search
  * order for test files is like this:
  * <UL>
  * <LI>
  * <A HREF=http://derwent.dcs.shef.ac.uk:80/gate.ac.uk/>
  * http://derwent.dcs.shef.ac.uk:80/gate.ac.uk/</A>
  * <LI>
  * <A HREF=http://gate.ac.uk:80/>http://gate.ac.uk:80/</A>
  * <LI>
  * <A HREF=http://localhost:80/gate.ac.uk/>http://localhost:80/gate.ac.uk/</A>
  * <LI>
  * the file system location that the classes came from, e.g.
  * <TT>z:\gate\classes</TT>, or <TT>jar:....gate.jar</TT>.
  * </UL>
  * This search order can be modified by parameters to the main
  * function (see below).
  */

public class TestGate {

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
    * (useful for
    * debugging, as there's less confusion to do with threads and
    * class loaders).
    * </UL>
    */
  public static void main(String[] args) throws Exception {
    boolean textMode = false;
    boolean autoloadingMode = false;

    // process command-line options
    Getopt g = new Getopt("GATE test suite", args, "tnNa");
    int c;
    while( (c = g.getopt()) != -1 )
      switch(c) {
        case 't':
          textMode = true;
          break;
        case 'n':
          Gate.setNetConnected(false);
          break;
        case 'N':
          Gate.setNetConnected(false);
          Gate.setLocalWebServer(false);
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
    String junitArgs[] = new String[2];
    junitArgs[0] = "-noloading";
    junitArgs[1] = "gate.TestGate";

    // use the next line if you're running with output to console in text mode:
    // junitArgs[1] = "-wait";

    // execute the JUnit test runner
    if(textMode) { // text runner mode
      junit.textui.TestRunner.main(junitArgs);
    } else if(autoloadingMode) { // autoloading mode
      junitArgs[0] = "gate.TestGate";
      junitArgs[1] = "";

      // NOTE: the DB tests fail under this one (doesn't load oracle driver,
      // even after the Class.forName call)
      Class clazz = null;
      clazz = Class.forName("oracle.jdbc.driver.OracleDriver");
      clazz = null;
      junit.swingui.TestRunner.main(junitArgs);

    } else { // by default us the single-run GUI version
      junit.swingui.TestRunner.main(junitArgs);
    }

  } // main

  /** GATE test suite. Every test case class has to be
    * registered here.
    */
  public static Test suite() throws Exception {
    // inialise the library. we re-throw any exceptions thrown by
    // init, after printing them out, because the junit gui doesn't
    // say anything more informative than "can't invoke suite" if there's
    // an exception here...
    try {
      Gate.init();
    } catch(GateException e) {
      Out.prln("can't initialise GATE library! exception = " + e);
      throw(e);
    }

    TestSuite suite = new TestSuite();

    try {
      ////////////////////////////////////////////////
      // Test bench
      ////////////////////////////////////////////////
      // set this true to run all tests; false to run the just one below
      boolean allTests = true;

      if(! allTests)
        suite.addTest(CookBook.suite());
      else {
        suite.addTest(TestControllers.suite());
        suite.addTest(TestPersist.suite());
        suite.addTest(TestAnnotationDiff.suite());
        suite.addTest(TestConfig.suite());
        suite.addTest(TestBumpyStack.suite());
        suite.addTest(TestAnnotation.suite());
        suite.addTest(TestEmail.suite());
        suite.addTest(TestXml.suite());
        suite.addTest(TestHtml.suite());
        suite.addTest(TestSgml.suite());
        suite.addTest(TestXSchema.suite());
        suite.addTest(TestCreole.suite());
        suite.addTest(CookBook.suite());
        suite.addTest(TestFiles.suite());
        suite.addTest(TestJdk.suite());
        suite.addTest(TestJape.suite());
        suite.addTest(TestFSM.suite());
        suite.addTest(TestTemplate.suite());
        suite.addTest(TestJacl.suite());
        suite.addTest(TestDocument.suite());
        suite.addTest(TestRBTreeMap.suite());
        suite.addTest(TestCorpus.suite());
        suite.addTest(TestDB.suite());
        suite.addTest(TestTokeniser.suite());
        suite.addTest(TestGazetteer.suite());
        suite.addTest(TestSplitterTagger.suite());
      } // if(allTests)

    } catch(Exception e) {
      Out.prln("can't add tests! exception = " + e);
      throw(e);
    }

    return suite;
  } // suite

} // class TestGate
