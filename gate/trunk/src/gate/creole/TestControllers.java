/*
 *  TestControllers.java
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

package gate.creole;

import java.util.*;
import java.io.*;
import java.net.*;
import junit.framework.*;

import gate.*;
import gate.creole.*;
import gate.util.*;

/** Tests for controller classes
  */
public class TestControllers extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** The CREOLE register */
  CreoleRegister reg;

  /** Construction */
  public TestControllers(String name) { super(name); }

  /** Fixture set up */
  public void setUp() throws GateException {
    // Initialise the GATE library and creole register
    Gate.init();

    // clear the register and the creole directory set
    reg = Gate.getCreoleRegister();
    reg.clear();
    reg.getDirectories().clear();

    // find a URL for finding test files and add to the directory set
    URL testUrl = Gate.getUrl("tests/");
    reg.addDirectory(testUrl);

    // register the test resources
    reg.registerDirectories();
  } // setUp

  /** Put things back as they should be after running tests
    * (reinitialise the CREOLE register).
    */
  public void tearDown() throws Exception {
    reg.clear();
    Gate.init();
  } // tearDown

  /** Serial controller test 1 */
  public void testSerial1() throws Exception {
    // a controller
    Controller c1 = (Controller) Factory.createResource(
      "gate.creole.SerialController",
      Factory.newFeatureMap()
    );
    assertNotNull("c1 controller is null", c1);

    // a couple of PRs
    ResourceData pr1rd = (ResourceData) reg.get("testpkg.TestPR1");
    ResourceData pr2rd = (ResourceData) reg.get("testpkg.TestPR2");
    assert("couldn't find PR1/PR2 res data", pr1rd != null && pr2rd != null);
    assert("wrong name on PR1", pr1rd.getName().equals("Sheffield Test PR 1"));
    ProcessingResource pr1 = (ProcessingResource)
      Factory.createResource("testpkg.TestPR1", Factory.newFeatureMap());
    ProcessingResource pr2 = (ProcessingResource)
      Factory.createResource("testpkg.TestPR2", Factory.newFeatureMap());
  } // testSerial1()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestControllers.class);
  } // suite

} // class TestControllers
