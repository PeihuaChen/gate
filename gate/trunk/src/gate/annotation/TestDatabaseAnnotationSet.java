/*
 *  TestDatabaseAnnotationSet.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 21/Oct/2001
 *
 *  $Id$
 */

package gate.annotation;

import java.util.*;
import java.io.*;
import junit.framework.*;
import java.net.*;

import gate.*;
import gate.util.*;
import gate.corpora.*;

/** Tests for the DatabaseAnnotationSet class
  */
public class TestDatabaseAnnotationSet extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestDatabaseAnnotationSet(String name) { super(name); }

  /** A document */
  protected Document doc1;

  /** An annotation set */
  protected AnnotationSet basicAS;

  /** An empty feature map */
  protected FeatureMap emptyFeatureMap;

  /** Fixture set up */
  public void setUp() throws Exception
  {
    String server = TestDocument.getTestServerName();
    assertNotNull(server);
    FeatureMap params = Factory.newFeatureMap();
    params.put("sourceUrl", Gate.getUrl("tests/doc0.html"));
    params.put("markupAware", "false");
    doc1 = (Document)Factory.createResource("gate.corpora.DocumentImpl",
                                                    params);

    emptyFeatureMap = new SimpleFeatureMapImpl();

    basicAS = new DatabaseAnnotationSetImpl(doc1);
    FeatureMap fm = new SimpleFeatureMapImpl();

    basicAS.get("T");          // to trigger type indexing
    basicAS.get(new Long(0));  // trigger offset index (though add will too)

    basicAS.add(new Long(10), new Long(20), "T1", fm);    // 0
    basicAS.add(new Long(10), new Long(20), "T2", fm);    // 1
    basicAS.add(new Long(10), new Long(20), "T3", fm);    // 2
    basicAS.add(new Long(10), new Long(20), "T1", fm);    // 3

    fm = new SimpleFeatureMapImpl();
    fm.put("pos", "NN");
    fm.put("author", "hamish");
    fm.put("version", new Integer(1));

    basicAS.add(new Long(10), new Long(20), "T1", fm);    // 4
    basicAS.add(new Long(15), new Long(40), "T1", fm);    // 5
    basicAS.add(new Long(15), new Long(40), "T3", fm);    // 6
    basicAS.add(new Long(15), new Long(40), "T1", fm);    // 7

    fm = new SimpleFeatureMapImpl();
    fm.put("pos", "JJ");
    fm.put("author", "the devil himself");
    fm.put("version", new Long(44));
    fm.put("created", "monday");

    basicAS.add(new Long(15), new Long(40), "T3", fm);    // 8
    basicAS.add(new Long(15), new Long(40), "T1", fm);    // 9
    basicAS.add(new Long(15), new Long(40), "T1", fm);    // 10

    // Out.println(basicAS);
  } // setUp


  /** Test remove */
  public void testRemove() {
    AnnotationSet asBuf = basicAS.get("T1");
    assertEquals(7, asBuf.size());
    asBuf = basicAS.get(new Long(9));
    assertEquals(5, asBuf.size());

    basicAS.remove(basicAS.get(new Integer(0)));

    assertEquals(10, basicAS.size());
    assertEquals(10, ((DatabaseAnnotationSetImpl) basicAS).annotsById.size());

    asBuf = basicAS.get("T1");
    assertEquals(6, asBuf.size());

    asBuf = basicAS.get(new Long(9));
    assertEquals(4, asBuf.size());
    assertEquals(null, basicAS.get(new Integer(0)));
    basicAS.remove(basicAS.get(new Integer(8)));
    assertEquals(9, basicAS.size());
    basicAS.removeAll(basicAS);
    assertEquals(null, basicAS.get());
    assertEquals(null, basicAS.get("T1"));
    assertEquals(null, basicAS.get(new Integer(0)));
  } // testRemove()

  public void testRemoveInexistant() throws Exception{
    basicAS.add(new Long(0), new Long(10), "Foo", emptyFeatureMap);
    Annotation ann = (Annotation)basicAS.get("Foo").iterator().next();
    basicAS.remove(ann);
    //the second remove should do nothing...
    basicAS.remove(ann);
  }

  /** Test iterator remove */
  public void testIteratorRemove() {
    AnnotationSet asBuf = basicAS.get("T1");
    assertEquals(7, asBuf.size());
    asBuf = basicAS.get(new Long(9));
    assertEquals(5, asBuf.size());

    // remove annotation with id 0; this is returned last by the
    // iterator
    Iterator iter = basicAS.iterator();
    while(iter.hasNext())
      iter.next();
    iter.remove();

    assertEquals(10, basicAS.size());
    assertEquals(10, ((DatabaseAnnotationSetImpl) basicAS).annotsById.size());
    asBuf = basicAS.get("T1");
    assertEquals(6, asBuf.size());
    asBuf = basicAS.get(new Long(9));
    assertEquals(4, asBuf.size());
    assertEquals(null, basicAS.get(new Integer(0)));
    basicAS.remove(basicAS.get(new Integer(8)));

  } // testIteratorRemove()

  /** Test iterator */
  public void testIterator() {
    Iterator iter = basicAS.iterator();
    Annotation[] annots = new Annotation[basicAS.size()];
    int i = 0;

    while(iter.hasNext()) {
      Annotation a = (Annotation) iter.next();
      annots[i++] = a;

      assert(basicAS.contains(a));
      iter.remove();
      assert(!basicAS.contains(a));
    } // while

    i = 0;
    while(i < annots.length) {
      basicAS.add(annots[i++]);
      assertEquals(i, basicAS.size());
    } // while

    AnnotationSet asBuf = basicAS.get("T1");
    assertEquals(7, asBuf.size());
    asBuf = basicAS.get(new Long(9));
    assertEquals(5, asBuf.size());

    AnnotationSet testAS = new DatabaseAnnotationSetImpl(doc1, "test");
    testAS.add(basicAS.get(new Integer(1)));
    testAS.add(basicAS.get(new Integer(4)));
    testAS.add(basicAS.get(new Integer(5)));
    testAS.add(basicAS.get(new Integer(0)));
    Annotation ann = testAS.get(new Integer(0));
    FeatureMap features = ann.getFeatures();
    features.put("test", "test value");

    Annotation ann1 = testAS.get(new Integer(4));
    FeatureMap features1 = ann1.getFeatures();
    features1.remove("pos");

    FeatureMap newFeatures = Factory.newFeatureMap();
    newFeatures.put("my test", "my value");
    Annotation ann2 = testAS.get(new Integer(5));
    ann2.setFeatures(newFeatures);
    if (DEBUG) Out.prln("ann 2 features: " + ann2.getFeatures());

    testAS.remove(basicAS.get(new Integer(0)));
    if (DEBUG) Out.prln("Test AS is : " + testAS);

    AnnotationSet fromTransientSet = new DatabaseAnnotationSetImpl(basicAS);
    ann = fromTransientSet.get(new Integer(0));
    features = ann.getFeatures();
    features.put("test", "test value");

    ann1 = fromTransientSet.get(new Integer(4));
    features1 = ann1.getFeatures();
    features1.remove("pos");

    newFeatures = Factory.newFeatureMap();
    newFeatures.put("my test", "my value");
    ann2 = fromTransientSet.get(new Integer(5));
    ann2.setFeatures(newFeatures);

    if (DEBUG) Out.prln("From transient set is : " + fromTransientSet);

  } // testIterator

  /** Test Set methods */
  public void testSetMethods() throws Exception {
    basicAS.clear();
    setUp();

    Annotation a = basicAS.get(new Integer(6));
    assert(basicAS.contains(a));

    Annotation[] annotArray =
      (Annotation[]) basicAS.toArray(new Annotation[0]);
    Object[] annotObjectArray = basicAS.toArray();
    assertEquals(11, annotArray.length);
    assertEquals(11, annotObjectArray.length);

    SortedSet sortedAnnots = new TreeSet(basicAS);
    annotArray = (Annotation[]) sortedAnnots.toArray(new Annotation[0]);
    for(int i = 0; i<11; i++)
      assert( annotArray[i].getId().equals(new Integer(i)) );

    Annotation a1 = basicAS.get(new Integer(3));
    Annotation a2 = basicAS.get(new Integer(4));
    Set a1a2 = new HashSet();
    a1a2.add(a1);
    a1a2.add(a2);
    assert(basicAS.contains(a1));
    assert(basicAS.containsAll(a1a2));
    basicAS.removeAll(a1a2);

    assertEquals(9, basicAS.size());
    assert(! basicAS.contains(a1));
    assert(! basicAS.containsAll(a1a2));

    basicAS.addAll(a1a2);
    assert(basicAS.contains(a2));
    assert(basicAS.containsAll(a1a2));

    assert(basicAS.retainAll(a1a2));
    assert(basicAS.equals(a1a2));

    basicAS.clear();
    assert(basicAS.isEmpty());

  } // testSetMethods()

  /** Test AnnotationSetImpl */
  public void testAnnotationSet() throws Exception {
    // constuct an empty AS
    FeatureMap params = Factory.newFeatureMap();
    params.put("sourceUrl", Gate.getUrl("tests/doc0.html"));
    params.put("markupAware", "false");
    Document doc = (Document)Factory.createResource("gate.corpora.DocumentImpl",
                                                    params);

    AnnotationSet as = new DatabaseAnnotationSetImpl(doc);
    assertEquals(as.size(), 0);

    // add some annotations
    FeatureMap fm1 = Factory.newFeatureMap();
    fm1.put("test", "my-value");
    FeatureMap fm2 = Factory.newFeatureMap();
    fm2.put("test", "my-value-different");
    FeatureMap fm3 = Factory.newFeatureMap();
    fm3.put("another test", "different my-value");

    Integer newId;
    newId =
      as.add(new Long(0), new Long(10), "Token", fm1);
    assertEquals(newId.intValue(), 0);
    newId =
      as.add(new Long(11), new Long(12), "Token", fm2);
    assertEquals(newId.intValue(), 1);
    assertEquals(as.size(), 2);
    assert(! as.isEmpty());
    newId =
      as.add(new Long(15), new Long(22), "Syntax", fm1);

    // get by ID; remove; add(object)
    Annotation a = as.get(new Integer(1));
    as.remove(a);
    assertEquals(as.size(), 2);
    as.add(a);
    assertEquals(as.size(), 3);

    // iterate over the annotations
    Iterator iter = as.iterator();
    while(iter.hasNext()) {
      a = (Annotation) iter.next();
      if(a.getId().intValue() != 2)
        assertEquals(a.getType(), "Token");
      assertEquals(a.getFeatures().size(), 1);
    }

    // add some more
    newId =
      as.add(new Long(0), new Long(12), "Syntax", fm3);
    assertEquals(newId.intValue(), 3);
    newId =
      as.add(new Long(14), new Long(22), "Syntax", fm1);
    assertEquals(newId.intValue(), 4);
    assertEquals(as.size(), 5);
    newId =
      as.add(new Long(15), new Long(22), "Syntax", new SimpleFeatureMapImpl());

    //get by feature names
    HashSet hs = new HashSet();
    hs.add("test");
    AnnotationSet fnSet = as.get("Token", hs);
    assertEquals(fnSet.size(), 2);
    //now try without a concrete type, just features
    //we'll get some Syntax ones now too
    fnSet = as.get(null, hs);
    assertEquals(fnSet.size(), 4);


    // indexing by type
    ((DatabaseAnnotationSetImpl) as).indexByType();
    AnnotationSet tokenAnnots = as.get("Token");
    assertEquals(tokenAnnots.size(), 2);

    // indexing by position
    AnnotationSet annotsAfter10 = as.get(new Long(15));
    if(annotsAfter10 == null)
      fail("no annots found after offset 10");
    assertEquals(annotsAfter10.size(), 2);

  } // testAnnotationSet
  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestAnnotation.class);
  } // suite


  public static void main(String[] args){

    try{
      Gate.init();
      TestDatabaseAnnotationSet testAnnot = new TestDatabaseAnnotationSet("");
      Out.prln("test set up");
      testAnnot.setUp();
      Out.prln("testIterator");
      testAnnot.testIterator();
      Out.prln("testAnnotationSet");
      testAnnot.testAnnotationSet();
      Out.prln("testRemove");
      testAnnot.testRemove();
      Out.prln("testInexistant");
      testAnnot.testRemoveInexistant();
      Out.prln("testSetMethods");
      testAnnot.testSetMethods();
      testAnnot.tearDown();
    }catch(Throwable t){
      t.printStackTrace();
    }
  }
} // class TestAnnotation

