/*
 *	TestCreole.java
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
  /** Construction */
  public TestCreole(String name) { super(name); }

  /** Fixture set up */
  public void setUp() throws Exception {  
    Gate.init();
    Gate.initCreoleRegister();
  } // setUp

  /** Test resource registration */
  public void testRegister() throws Exception {

    CreoleRegister reg = Gate.getCreoleRegister();

    assertEquals(reg.size(), 3);

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
    reg.addDirectory(new URL("file:/z:/gate2/build/creole.xml"));
    reg.registerDirectories();

    assert(reg.size() == 2);
    ResourceData pr1 = (ResourceData) reg.get("Sheffield Test PR 1");
    ResourceData pr2 = (ResourceData) reg.get("Sheffield Test PR 2");
    assert(pr1 != null & pr2 != null);

  } // testLoading()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestCreole.class);
  } // suite

} // class TestCreole
