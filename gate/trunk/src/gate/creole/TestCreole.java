/*
 *	TestCreole.java
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
 *	Hamish Cunningham, 16/Mar/00
 *
 *	$Id$
 */

package gate.creole;

import java.util.*;
import java.io.*;
import java.net.*;
import junit.framework.*;

import gate.*;
import gate.util.*;

/** CREOLE test class
  */
public class TestCreole extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestCreole(String name) throws GateException { super(name); }

  /** Fixture set up */
  public void setUp() throws Exception {
    // Initialise the creole register
    Gate.init();
    Gate.initCreoleRegister();
  } // setUp

  /** Test resource discovery */
  public void testDiscovery() throws Exception {

    CreoleRegister reg = Gate.getCreoleRegister();
    if(DEBUG) {
      Iterator iter = reg.values().iterator();
      while(iter.hasNext()) Out.println(iter.next());
    }

    ResourceData rd = (ResourceData) reg.get("Sheffield Unicode Tokeniser");
    assertNotNull("couldn't find unicode tok in register of resources", rd);
    assert(rd.getName().equals("Sheffield Unicode Tokeniser"));

    String docFormatName = "Sheffield XML Document Format";
    ResourceData xmlDocFormatRD = (ResourceData) reg.get(docFormatName);
    assert(xmlDocFormatRD.getName().equals(docFormatName));
    assert(xmlDocFormatRD.isAutoLoading());
    assert(xmlDocFormatRD.getJarFileName().equals("ShefDocumentFormats.jar"));
  } // testDiscovery()

  /** Test resource metadata */
  public void testMetadata() throws Exception {

    // clear the register and the creole directory set
    CreoleRegister reg = Gate.getCreoleRegister();
    reg.clear();
    reg.getDirectories().clear();

    // find a URL for finding test files and add to the directory set
    URL testUrl = Gate.getUrl("tests/");
    reg.addDirectory(testUrl);

    reg.registerDirectories();
    if(DEBUG) {
      Iterator iter = reg.values().iterator();
      while(iter.hasNext()) Out.println(iter.next());
    }

    // get some res data from the register
    assert(reg.size() == 2);
    ResourceData pr1rd = (ResourceData) reg.get("Sheffield Test PR 1");
    ResourceData pr2rd = (ResourceData) reg.get("Sheffield Test PR 2");
    assert(pr1rd != null & pr2rd != null);

    // checks values of parameters of param0 in test pr 1
    assert(pr1rd.getClassName().equals("testpkg.TestPR1"));
    Iterator iter = pr1rd.getParameterListsSet().iterator();
    Iterator iter2 = null;
    Parameter param = null;
    while(iter.hasNext())
      iter2 = ((List) iter.next()).iterator();
    while(iter2.hasNext())
      param = (Parameter) iter2.next();
    assert(param.valueString.equals("param0"));
    assert(! param.optional);
    assert(param.runtime);
    assert(param.comment == null);
    assert(param.defaultValueString == null);
    assert(param.name.equals("HOOPY DOOPY"));

    reg.clear();
  } // testMetadata()

  /** Test resource loading */
  public void testLoading() throws Exception {

    // clear the register and the creole directory set
    CreoleRegister reg = Gate.getCreoleRegister();
    reg.clear();
    reg.getDirectories().clear();

    // find a URL for finding test files and add to the directory set
    URL testUrl = Gate.getUrl("tests/");
    reg.addDirectory(testUrl);

    reg.registerDirectories();
    if(DEBUG) {
      Iterator iter = reg.values().iterator();
      while(iter.hasNext()) Out.println(iter.next());
    }

    // get some res data from the register
    assert(reg.size() == 2);
    ResourceData pr1rd = (ResourceData) reg.get("Sheffield Test PR 1");
    ResourceData pr2rd = (ResourceData) reg.get("Sheffield Test PR 2");
    assert(pr1rd != null & pr2rd != null);

// try instantiation here

    reg.clear();
  } // testLoading()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestCreole.class);
  } // suite

} // class TestCreole
