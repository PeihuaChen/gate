/*
 *	TestAnnotation.java
 *
 *	Hamish Cunningham, 7/Feb/00
 *
 *	$Id$
 */

package gate.annotation;

import java.util.*;
import junit.framework.*;
import gate.*;
import gate.util.*;

/** Tests for the Annotation classes
  */
public class TestAnnotation extends TestCase
{
  /** Construction */
  public TestAnnotation(String name) { super(name); }

//  /** Base of the test server URL */
//  protected String testServer;
//
  /** Name of test document 1 */
  protected Document testDocument1;

  /** Fixture set up */
  public void setUp() {
  } // setUp

  /** Test AnnotationSetImpl */
  public void testAnnotationSet() {
    AnnotationSet as = new AnnotationSetImpl(testDocument1);
    assertEquals(as.size(), 0);

    Integer newId;
    newId =
      as.add(new Long(0), new Long(10), "Token", new SimpleFeatureMapImpl());
    assertEquals(newId.intValue(), 0);
    newId =
      as.add(new Long(11), new Long(12), "Token", new SimpleFeatureMapImpl());
    assertEquals(newId.intValue(), 1);

    assertEquals(as.size(), 2);
    assert(! as.isEmpty());

    Iterator iter = as.iterator();
    while(iter.hasNext()) {
      Annotation a = (Annotation) iter.next();
      assertEquals(a.getType(), "Token");
      assertEquals(a.getFeatures().size(), 0);
    }

  } // testAnnotationSet

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestAnnotation.class);
  } // main

} // class TestAnnotation
