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
  /**
    *  This field is "final static" because it brings in
    *  the advantage of dead code elimination
    *  When DEBUG is set on false the code that it guardes will be eliminated
    *  by the compiler. This will spead up the progam a little bit.
    */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestCreole(String name) throws GateException { super(name); }

  /** Fixture set up */
  public void setUp() throws Exception {
    // Initialise the creole register
    Gate.initCreoleRegister();
  } // setUp

  /** Test resource registration */
  public void testRegister() throws Exception {

    CreoleRegister reg = Gate.getCreoleRegister();
    if(DEBUG) {
      Iterator iter = reg.values().iterator();
      while(iter.hasNext())
        Out.println(iter.next());
    }

    assert(((ResourceData) reg.get("Sheffield Unicode Tokeniser")).getName()
           .equals("Sheffield Unicode Tokeniser"));

    String docFormatName = "Sheffield XML Document Format";
    ResourceData xmlDocFormatRD = (ResourceData) reg.get(docFormatName);
    assert(xmlDocFormatRD.getName().equals(docFormatName));
    assert(xmlDocFormatRD.isAutoLoading());
    assert(xmlDocFormatRD.getJarFileName().equals("ShefDocumentFormats.jar"));
  } // testRegister()

  /** Test resource loading */
  public void testLoading() throws Exception {

    CreoleRegister reg = Gate.getCreoleRegister();
    reg.clear();
    reg.getDirectories().clear();

    if (Gate.isGateHomeReachable())
      reg.addDirectory(
        new URL("http://derwent.dcs.shef.ac.uk/gate.ac.uk/tests/creole.xml")
      );
    else if (Gate.isGateAcUkReachable())
      reg.addDirectory(
        new URL("http://www.gate.ac.uk/tests/creole.xml")
      );
    else
      throw new GateException("Derwent and www.gate.ac.uk are not reachable");

    reg.registerDirectories();

    assert(reg.size() == 2);
    ResourceData pr1rd = (ResourceData) reg.get("Sheffield Test PR 1");
    ResourceData pr2rd = (ResourceData) reg.get("Sheffield Test PR 2");
    assert(pr1rd != null & pr2rd != null);

    CreoleLoader loader = Gate.getCreoleLoader();
    ProcessingResource pr1 = (ProcessingResource) loader.load(pr1rd);

    reg.clear();
  } // testLoading()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestCreole.class);
  } // suite

} // class TestCreole