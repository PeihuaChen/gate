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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.ontotext.gate.gazetteer.TestHashGazetteer;

import gate.annotation.TestAnnotation;
import gate.annotation.TestAnnotationDiff;
import gate.config.TestConfig;
import gate.corpora.*;
import gate.creole.*;
import gate.creole.ir.TestIndex;
import gate.creole.morph.TestMorph;
import gate.creole.gazetteer.TestFlexibleGazetteer;
import gate.email.TestEmail;
import gate.html.TestHtml;
import gate.jape.TestJape;
import gate.persist.TestPersist;
import gate.security.TestSecurity;
import gate.sgml.TestSgml;
import gate.util.*;
import gate.wordnet.TestWordNet;
import gate.xml.TestXml;
import gate.creole.ml.maxent.*;
import gate.xml.TestRepositioningInfo;

import gnu.getopt.Getopt;

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

  /** Status flag for normal exit. */
  private static final int STATUS_NORMAL = 0;

  /** Status flag for error exit. */
  private static final int STATUS_ERROR = 1;

  private static final String
                defOracleDriver = "jdbc:oracle:thin:@derwent:1521:dbgate";
  private static final String
                saiOracleDriver = "jdbc:oracle:thin:GATEUSER/gate@192.168.128.7:1521:GATE04";
  private static final String
                defPSQLDriver = "jdbc:postgresql://redmires/gate";
  private static final String
                saiPSQLDriver = "jdbc:postgresql://sirma/gate";


  public static String oracleDriver = defOracleDriver;
  public static String psqlDriver = defPSQLDriver;

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
    * <LI>
    * <B>-i file</B> additional initialisation file (probably called
    *   <TT>gate.xml</TT>). Used for site-wide initialisation by the
    *   start-up scripts.
    * </UL>
    */
  public static void main(String[] args) throws Exception {
    boolean textMode = false;
    boolean autoloadingMode = false;

    // process command-line options
    Getopt g = new Getopt("GATE test suite", args, "tnNasi:");
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
        case 's':
          oracleDriver = saiOracleDriver;
          psqlDriver = saiPSQLDriver;
          break;
        // -i gate.xml site-wide init file
        case 'i':
          String optionString = g.getOptarg();
          URL u = null;
          File f = new File(optionString);
          try {
            u = f.toURL();
          } catch(MalformedURLException e) {
            Err.prln("Bad initialisation file: " + optionString);
            Err.prln(e);
            System.exit(STATUS_ERROR);
          }
          Gate.setSiteConfigFile(f);
          Out.prln(
            "Initialisation file " + optionString +
            " recorded for initialisation"
          );
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
      //get the config if set through a property
      String configFile = System.getProperty("gate.config");
      if(configFile != null && configFile.length() > 0){
        File f = new File(configFile);
        try {
          URL u = f.toURL();
        } catch(MalformedURLException e) {
          Err.prln("Bad initialisation file: " + configFile);
          Err.prln(e);
          System.exit(STATUS_ERROR);
        }
        Gate.setSiteConfigFile(f);
      }
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
      boolean allTests = false;
      if(! allTests){
        suite.addTest(TestJacl.suite());
      } else {
        suite.addTest(TestWordNet.suite());
        suite.addTest(TestIndex.suite());
        suite.addTest(TestPersist.suite());
        suite.addTest(TestBumpyStack.suite());
        suite.addTest(TestControllers.suite());
        suite.addTest(TestSecurity.suite());
        suite.addTest(TestAnnotationDiff.suite());
        suite.addTest(TestConfig.suite());
        suite.addTest(TestAnnotation.suite());
        suite.addTest(TestEmail.suite());
        suite.addTest(TestXml.suite());
        suite.addTest(TestHtml.suite());
        suite.addTest(TestSgml.suite());
        suite.addTest(TestXSchema.suite());
        suite.addTest(TestCreole.suite());
        suite.addTest(CookBook.suite());
        suite.addTest(TestFiles.suite());
        suite.addTest(TestJavac.suite());
        suite.addTest(TestReload.suite());
//        suite.addTest(TestJdk.suite());
        suite.addTest(TestJape.suite());
        suite.addTest(TestTemplate.suite());
        suite.addTest(TestJacl.suite());
        suite.addTest(TestDocument.suite());
        suite.addTest(TestRBTreeMap.suite());
        suite.addTest(TestCorpus.suite());
        suite.addTest(TestSerialCorpus.suite());
        suite.addTest(TestDiffer.suite());
//no longer needed as replaced by testPR
//        suite.addTest(TestTokeniser.suite());
//        suite.addTest(TestGazetteer.suite());
//        suite.addTest(TestSplitterTagger.suite());
        suite.addTest(TestFeatureMap.suite());
        suite.addTest(TestPR.suite());
        suite.addTest(TestMaxentWrapper.suite());

        //test ontotext gazetteer
        suite.addTest(TestHashGazetteer.suite());
        suite.addTest(TestRepositioningInfo.suite());
        suite.addTest(TestFlexibleGazetteer.suite());

      } // if(allTests)

    } catch(Exception e) {
      Out.prln("can't add tests! exception = " + e);
      throw(e);
    }

    return suite;
  } // suite

} // class TestGate
