/*
 *  TestSerialCorpus.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 20/Oct/2001
 *
 *  $Id$
 */

package gate.corpora;

import java.util.*;
import java.net.*;
import java.io.*;
import junit.framework.*;

import gate.*;
import gate.util.*;
import gate.annotation.*;

/** Tests for the SerialCorpus classes
  */
public class TestSerialCorpus extends TestCase
{

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestSerialCorpus(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp

  /** Corpus creation */
  public void testCreation() throws Exception {
    Corpus c = new SerialCorpusImpl();
    c.setName("test corpus");

    assert(c.isEmpty());
    assert(c.getName().equals("test corpus"));

    c.setFeatures(new SimpleFeatureMapImpl());
    c.getFeatures().put("author", "hamish");
    c.getFeatures().put("date", new Integer(180200));
    assert(c.getFeatures().size() == 2);


  } // testCreation()

  /** Add some documents */
  public void testDocumentAddition() throws Exception {
    Corpus c = Factory.newCorpus("test corpus");
    Document d1 = Factory.newDocument("a document");
    Document d2 = Factory.newDocument("another document");
    d2.setSourceUrl(new URL("http://localhost/1"));
    d2.setSourceUrl(new URL("http://localhost/2"));
    assert(c.add(d1));
    assert(c.add(d2));
    assertEquals(2, c.size());

    Corpus c1 = new SerialCorpusImpl(c);
    Document d1_1 = (Document) c1.get(0);
    Document d2_1 = (Document) c1.get(1);
    assertEquals(d1, d1_1);
    assertEquals(d2, d2_1);

    Corpus c2 = new SerialCorpusImpl();
    c2.add(d1);
    c2.add(d2);
    Document d1_2 = (Document) c2.get(0);
    Document d2_2 = (Document) c2.get(1);
    assertEquals(d1, d1_2);
    assertEquals(d2, d2_2);

  } // testDocumentAddition()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestSerialCorpus.class);
  } // suite

  public static void main(String[] args){
    try{
      Gate.setLocalWebServer(false);
      Gate.setNetConnected(false);
      Gate.init();
      TestSerialCorpus test = new TestSerialCorpus("");
      test.setUp();
      test.testCreation();
      test.tearDown();

      test.setUp();
      test.testDocumentAddition();
      test.tearDown();

    }catch(Exception e){
      e.printStackTrace();
    }
  }

} // class TestCorpus
