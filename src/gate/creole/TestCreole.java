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

  /** Debug flag */
  private static final boolean debug = false;

  /** Fixture set up */
  public void setUp() throws Exception {
    Gate.init();
    Gate.initCreoleRegister();
  } // setUp

  /** Test resource registration */
  public void testRegister() throws Exception {

    CreoleRegister reg = Gate.getCreoleRegister();
    if(debug) {
      Iterator iter = reg.values().iterator();
      while(iter.hasNext())
        System.out.println(iter.next());
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
    reg.addDirectory(
      new URL("http://derwent.dcs.shef.ac.uk/tests/creole.xml")
    );
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
