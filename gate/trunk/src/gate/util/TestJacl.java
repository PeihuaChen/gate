/*
 *  TestJacl.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *  
 *  Hamish Cunningham, 16/Mar/00
 *
 *  $Id$
 */

package gate.util;

import java.util.*;
import junit.framework.*;
import tcl.lang.*;

/** Tests for the Jacl class
  */
public class TestJacl extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestJacl(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp

  /** Jacl creation and use of GATE scripts */
  public void testCreation() throws TclException {

    // create and interpreter and load all the GATE scripts
    Jacl jacl = new Jacl();
    jacl.loadScripts();

    // try running a script (assumes we are run from within gate2
    // directory hierarchy)
    Interp interp = jacl.getInterp();
    interp.eval("GATE::findScripts");

    // get the result - should be a list of .tcl files
    TclObject result = interp.getResult();

    // check that the result looks right
    // (this may start to fail if we have packages other than gate
    // that contain tcl scripts...)
    assert(result.toString().startsWith("gate/"));

    // check that a known script is present
    assert(result.toString().indexOf("FindScripts.tcl") != -1);
  } // testCreation()


  /** Test the finding and listing methods */
  public void testListing() throws TclException {
    // create and interpreter and load all the GATE scripts
    Jacl jacl = new Jacl();

    // find the list of script files in the GATE source tree
    // (the parameter to findScripts causes a dir change before the search)
    List scriptPaths = jacl.findScripts(jacl.goToGateSrcScript);
    // Out.println("Scripts found: " + scriptPaths);

    // refresh Jacl.java's list of GATE scripts
    // Out.println("Updating Jacl.java....");
    // jacl.listGateScripts();

    // copy the scripts to the classes tree
    // Out.println("Doing copy....");
    jacl.copyGateScripts(scriptPaths);

    // load the scripts (as a test)
    // Out.println("Doing load....");
    jacl.loadScripts(scriptPaths);

    // tell the world
    // Out.println("Tcl scripts found, installed and loaded");
  } // testListing


  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestJacl.class);
  } // suite

} // class TestJacl
