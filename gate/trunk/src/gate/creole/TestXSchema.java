/*
 *	TestXSchema.java
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
 *	Cristian URSU, 11/Octomber/2000
 *
 *	$Id$
 */

package gate.creole;

import java.util.*;
import java.io.*;
import java.net.*;

import junit.framework.*;

import gate.util.*;

/** Template test class - to add a new part of the test suite:
  * <UL>
  * <LI>
  * copy this class and change "Template" to the name of the new tests;
  * <LI>
  * add a line to TestGate.java in the suite method referencing your new
  * class;
  * <LI>
  * add test methods to this class.
  * </UL>
  */
public class TestXSchema extends TestCase
{
  /**
    *  This field is "final static" because it brings in
    *  the advantage of dead code elimination
    *  When DEBUG is set on false the code that it guardes will be eliminated
    *  by the compiler. This will speed up the progam a little bit.
    */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestXSchema(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp

  /** A test */
  public void testFromAndToXSchema() throws Exception {

   URL url = null;
   // init detects if Derwent or www.gate.ac.uk are reachable
   Gate.init();
   try{
     if (Gate.isGateHomeReachable())
      url =
   new URL ("http://derwent.dcs.shef.ac.uk/gate.ac.uk/tests/xml/POSSchema.xml");
     else if (Gate.isGateAcUkReachable())
              url = new URL("http://www.gate.ac.uk/tests/xml/POSSchema.xml");
          else
            throw new LazyProgrammerException(
                                "Derwent and www.gate.ak.uk are not reachable");
   } catch (Exception e){
      e.printStackTrace(Err.getPrintWriter());
   }

   AnnotationSchema annotSchema = new AnnotationSchema();
   // Create an annoatationSchema from a URL.
   annotSchema.fromXSchema(url);

   String s = annotSchema.toXSchema();
   // write back the XSchema fom memory
   File file = Files.writeTempFile(new ByteArrayInputStream(s.getBytes()));
   // load it again.
   annotSchema.fromXSchema(file.toURL());

  } // testFromAndToXSchema()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestXSchema.class);
  } // suite

} // class TestXSchema