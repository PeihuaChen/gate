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

  /** A test */
  public void testSomething() throws Exception {

    CreoleRegister reg = Gate.getCreoleRegister();

    assertEquals(reg.size(), 3);

    assert(((ResourceData) reg.get("Sheffield Unicode Tokeniser")).getName()
           .equals("Sheffield Unicode Tokeniser"));

    String docFormatName = "Sheffield XML Document Format";
    ResourceData xmlDocFormatRD = (ResourceData) reg.get(docFormatName);
    assert(xmlDocFormatRD.getName().equals(docFormatName));
    assert(xmlDocFormatRD.isAutoLoading());
    assert(xmlDocFormatRD.getJarFileName().equals("ShefDocumentFormats.jar"));
  } // testSomething()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestCreole.class);
  } // suite

} // class TestCreole
