/*
 *	TestJacl.java
 *
 *	Hamish Cunningham, 16/Mar/00
 *
 *	$Id$
 */

package gate.util;

import java.util.*;
import junit.framework.*;
import tcl.lang.*;

/** Tests for the Jacl class
  */
public class TestJacl extends TestCase
{
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
    interp.eval("findScripts");

    // get the result - should be a list of .tcl files
    TclObject result = interp.getResult();

    // check that the result looks right
    // (this may start to fail if we have packages other than gate
    // that contain tcl scripts...)
    assert(result.toString().startsWith("gate/"));

    // check that a known script is present
    assert(result.toString().indexOf("FindScripts.tcl") != -1);
  } // testCreation()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestJacl.class);
  } // suite

} // class TestJacl
