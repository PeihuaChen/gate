/*
 *  TestConfig.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 9/Nov/00
 *
 *  $Id$
 */

package gate.config;

import java.util.*;
import java.io.*;
import java.net.*;
import junit.framework.*;

import gate.*;
import gate.util.*;
import gate.creole.*;

/** CREOLE test class
  */
public class TestConfig extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestConfig(String name) throws GateException { super(name); }

  /** Fixture set up */
  public void setUp() throws Exception {
    CreoleRegister register = Gate.getCreoleRegister();
    register.registerDirectories(Gate.getUrl("tests"));
  } // setUp

  /** Put things back as they should be after running tests
    * (reinitialise the CREOLE register).
    */
  public void tearDown() throws Exception {
    CreoleRegister register = Gate.getCreoleRegister();
    register.clear();
    Gate.init();
  } // tearDown

  /** Test resource discovery */
  public void testConfigReading() throws Exception {
    ConfigDataProcessor configProcessor = new ConfigDataProcessor();

    // url of the builtin config data (for error messages)
    URL configUrl = Gate.getUrl("tests/gate.xml");

    // open a stream to the builtin config data file (tests version)
    InputStream configStream = null;
    try {
      configStream = configUrl.openStream();
    } catch(IOException e) {
      throw new GateException(
        "Couldn't open config data test file: " + configUrl + " " + e
      );
    }
    if (DEBUG)
      Out.prln("Parsing config file ... " + configStream + "from URL" + configUrl);
    configProcessor.parseConfigFile(configStream, configUrl);

    // check that we got the CREOLE dir entry; then remove it
    // so it doesn't get accessed in other tests
    CreoleRegister reg = Gate.getCreoleRegister();
    Set dirs = reg.getDirectories();
    assert(
      "CREOLE register doesn't contain URL from test gate.xml",
      dirs != null && ! dirs.isEmpty() &&
      dirs.contains(new URL("http://somewhere.on.the.net/creole/"))
    );

    // get a test system
    ResourceData controllerResData =
      (ResourceData) reg.get("gate.creole.SerialController");
    assertNotNull("no resdata for serial controller", controllerResData);
    ProcessingResource controller =
      (ProcessingResource) controllerResData.getInstantiations().pop();
    assertNotNull("no controller instance", controller);

    // try running the system
    controller.run();
    controller.check();
  } // testConfigReading()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestConfig.class);
  } // suite

} // class TestConfig
