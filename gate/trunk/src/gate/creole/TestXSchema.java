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
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestXSchema(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp

  /** A test */
  public void testFromAndToXSchema() throws Exception {

    AnnotationSchema annotSchema = new AnnotationSchema();

    // Create an annoatationSchema from a URL.
    URL url = Gate.getUrl("tests/xml/POSSchema.xml");
    annotSchema.fromXSchema(url);

    String s = annotSchema.toXSchema();

    // write back the XSchema fom memory
    // File file = Files.writeTempFile(new ByteArrayInputStream(s.getBytes()));
    // load it again.
    //annotSchema.fromXSchema(file.toURL());
    annotSchema.fromXSchema(new ByteArrayInputStream(s.getBytes()));
  } // testFromAndToXSchema()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestXSchema.class);
  } // suite

} // class TestXSchema