/*
 *  TestTemplate.java
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
 *  ___AUTHOR___, ___DATE___
 *
 *  $Id$
 */
package ___PACKAGE___;

import java.util.*;
import junit.framework.*;
import gate.*;

/** Top-level entry point for Template test suite.
  * "main" will run the JUnit test runner interface.
  * Use a "-t" flag to run the textual UI test runner (useful for
  * debugging, as there's less confusion to do with threads and
  * class loaders!).
  */
public class TestTemplate
{
  /** Main routine. */
  public static void main(String[] args) throws ClassNotFoundException {
    String a[] = new String[1];
    a[0] = "template.TestTemplate";
    // use the next line if you're running with output to console in text mode:
    // a[1] = "-wait";
    if(args.length > 0 && args[0].equals("-t")) // text runner mode
      junit.textui.TestRunner.main(a);
    else {

      // the GATE DB tests fail under this one (doesn't load oracle driver,
      // even after the Class.forName call)
      //Class c = null;
      //c = Class.forName("oracle.jdbc.driver.OracleDriver");
      //c = null;
      junit.swingui.TestRunner.main(a);
      // if there's a problem use this one:
      // junit.ui.TestRunner.main(a);
    }
  } // main

  /** Template test suite. Every test case class has to be
    * registered here.
    */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(Test___CLASSNAME___.suite());
    return suite;
  } // suite

} // class TestTemplate
