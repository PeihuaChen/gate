/*
 *	TestCorpus.java
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
  /** Construction */
  public TestCorpus(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp

  /** Corpus creation */
  public void testCreation() {
    Corpus c = new CorpusImpl("test corpus");
    assert(c.isEmpty());
    assert(c.getName().equals("test corpus"));

    c.setFeatures(new SimpleFeatureMapImpl());
    c.getFeatures().put("author", "hamish");
    c.getFeatures().put("date", new Integer(180200));
    assert(c.getFeatures().size() == 2);

    Corpus c2 = new CorpusImpl("test corpus2", new SimpleFeatureMapImpl());
    c2.getFeatures().put("author", "hamish");
    c2.getFeatures().put("author", "valy");
    assert(c2.getFeatures().size() == 1);
    assert(c2.getFeatures().get("author").equals("valy"));
  } // testCreation()

  /** Add some documents */
  public void testDocumentAddition() throws IOException { 
    Corpus c = new CorpusImpl("test corpus");
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
