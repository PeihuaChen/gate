/*
 *	TestGate.java
 *
 *	Hamish Cunningham, 21/Jan/00
 *
 *	$Id$
 */

package gate;
import java.util.*;
import junit.framework.*;
import gate.util.*;

/** Top-level entry point for GATE test suite.
  * "main" will run the JUnit test runner interface.
  */
public class TestGate
{

  /** Main routine. */
  public static void main(String[] args) {
    String a[] = new String[1];
    a[0] = "gate.TestGate";
    junit.ui.LoadingTestRunner.main(a);
  } // main

  /** GATE test suite. Every test case class has to be
    * registered here.
    */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(TestDocument.suite());  
    suite.addTest(gate.annotation.TestAnnotation.suite());
    return suite;
  } // suite

} // class TestGate
