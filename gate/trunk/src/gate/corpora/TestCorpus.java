/*
 *	TestCorpus.java
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
 *	Hamish Cunningham, 18/Feb/00
 *
 *	$Id$
 */

package gate.corpora;

import java.util.*;
import java.net.*;
import java.io.*;
import junit.framework.*;

import gate.*;
import gate.util.*;
import gate.annotation.*;

/** Tests for the Corpus classes
  */
public class TestCorpus extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestCorpus(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp

  /** Corpus creation */
  public void testCreation() throws Exception {
    Corpus c = Factory.newCorpus("test corpus");
    assert(c.isEmpty());
    assert(c.getName().equals("test corpus"));

    c.setFeatures(new SimpleFeatureMapImpl());
    c.getFeatures().put("author", "hamish");
    c.getFeatures().put("date", new Integer(180200));
    assert(c.getFeatures().size() == 2);

    Corpus c2 = Factory.newCorpus("test corpus2");
    c2.getFeatures().put("author", "hamish");
    c2.getFeatures().put("author", "valy");
    assert(c2.getFeatures().size() == 1);
    assert(c2.getFeatures().get("author").equals("valy"));
  } // testCreation()

  /** Add some documents */
  public void testDocumentAddition() throws Exception {
    Corpus c = Factory.newCorpus("test corpus");
    Document d1 = new DocumentImpl();
    Document d2 = new DocumentImpl();
    assert(c.add(d1));
    assert(c.add(d2));
    assertEquals(2, c.size());
  } // testDocumentAddition()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestCorpus.class);
  } // suite

} // class TestCorpus