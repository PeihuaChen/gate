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

import gate.*;
import gate.util.*;

/** Annotation schemas test class.
  */
public class TestXSchema extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = true;

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

  /** Test creation of annotation schemas via gate.Factory */
  public void testFactoryCreation() throws Exception {

    FeatureMap parameters = Factory.newFeatureMap();
    AnnotationSchema schema = (AnnotationSchema)
      Factory.createResource("gate.creole.AnnotationSchema", parameters);

    if(DEBUG) Out.prln("schema features: " + schema.getFeatures());
///////////////////
/*

deal with the XML element properly; it should be processed relative
  to ResourceData.jarFileUrl minus the jarFile bit....

COULD do: make resource features have creoleDirectoryUrl;
"XML" element will be there too, and will be relative path from the URL;
then get init to construct URL to the XML source and read it.
BUT, doesn't this break the bean-style initialisation convention?
What is special about jarFile[Url] and xmlSourceUrl....? (Built
relative to dir URL)

make ASchema beans-style with proper init
then get it parsing the XML....
....and tested here

*/
//////////////////
  } // testFactoryCreation()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestXSchema.class);
  } // suite

} // class TestXSchema