/*
 *  TestAnnotation.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 7/Feb/00
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

/** Tests for the Annotation classes
  */
public class TestAnnotation extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestAnnotation(String name) { super(name); }

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

    basicAS = new AnnotationSetImpl(doc1);
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


  /** Test indexing by offset */
  public void testOffsetIndex() throws InvalidOffsetException {
    AnnotationSet as = new AnnotationSetImpl(doc1);
    AnnotationSet asBuf;
    Integer newId;
    FeatureMap fm = new SimpleFeatureMapImpl();
    Annotation a;
    Node startNode;
    Node endNode;

    newId = as.add(new Long(10), new Long(20), "T", fm);
    assertEquals(newId.intValue(), 11);
    a = as.get(newId);

    startNode = a.getStartNode();
    endNode = a.getEndNode();
    assertEquals(startNode.getId().intValue(), 4);
    assertEquals(endNode.getId().intValue(), 5);
    assertEquals(startNode.getOffset().longValue(), 10);
    assertEquals(endNode.getOffset().longValue(), 20);

    newId = as.add(new Long(10), new Long(30), "T", fm);
    assertEquals(newId.intValue(), 12);
    a = as.get(newId);

    startNode = a.getStartNode();
    endNode = a.getEndNode();
    assertEquals(startNode.getId().intValue(), 4);
    assertEquals(endNode.getId().intValue(), 6);
    assertEquals(startNode.getOffset().longValue(), 10);
    assertEquals(endNode.getOffset().longValue(), 30);

    asBuf = as.get(new Long(10));
    assertEquals(asBuf.size(), 2);

  } // testOffsetIndex()

  /** Test exception throwing */
  public void testExceptions() {
    AnnotationSet as = new AnnotationSetImpl(doc1);
    boolean threwUp = false;

    try {
      as.add(new Long(-1), new Long(1), "T", emptyFeatureMap);
    } catch (InvalidOffsetException e) {
      threwUp = true;
    }

    if(! threwUp) fail("Should have thrown InvalidOffsetException");
    threwUp = false;

    try {
      as.add(new Long(1), new Long(-1), "T", emptyFeatureMap);
    } catch (InvalidOffsetException e) {
      threwUp = true;
    }

    if(! threwUp) fail("Should have thrown InvalidOffsetException");
    threwUp = false;

    try {
      as.add(new Long(1), new Long(0), "T", emptyFeatureMap);
    } catch (InvalidOffsetException e) {
      threwUp = true;
    }

    if(! threwUp) fail("Should have thrown InvalidOffsetException");
    threwUp = false;

    try {
      as.add(null, new Long(1), "T", emptyFeatureMap);
    } catch (InvalidOffsetException e) {
      threwUp = true;
    }

    if(! threwUp) fail("Should have thrown InvalidOffsetException");
    threwUp = false;

    try {
      as.add(new Long(1), null, "T", emptyFeatureMap);
    } catch (InvalidOffsetException e) {
      threwUp = true;
    }

    if(! threwUp) fail("Should have thrown InvalidOffsetException");
    threwUp = false;

    try {
      as.add(new Long(999999), new Long(100000000), "T", emptyFeatureMap);
    } catch (InvalidOffsetException e) {
      threwUp = true;
    }

    /*
    // won't work until the doc size check is implemented
    if(! threwUp) fail("Should have thrown InvalidOffsetException");
    */
    threwUp = false;

  } // testExceptions()

  /** Test type index */
  public void testTypeIndex() throws Exception {
    FeatureMap params = Factory.newFeatureMap();
    params.put("sourceUrl", Gate.getUrl("tests/doc0.html"));
    params.put("markupAware", "false");
    Document doc = (Document)Factory.createResource("gate.corpora.DocumentImpl",
                                                    params);
    AnnotationSet as = new AnnotationSetImpl(doc);
    AnnotationSet asBuf;
    Integer newId;
    FeatureMap fm = new SimpleFeatureMapImpl();
    Annotation a;
    Node startNode;
    Node endNode;

    // to trigger type indexing
    as.get("T");
    as.add(new Long(10), new Long(20), "T1", fm);    // 0
    as.add(new Long(10), new Long(20), "T2", fm);    // 1
    as.add(new Long(10), new Long(20), "T3", fm);    // 2
    as.add(new Long(10), new Long(20), "T1", fm);    // 3
    as.add(new Long(10), new Long(20), "T1", fm);    // 4
    as.add(new Long(10), new Long(20), "T1", fm);    // 5
    as.add(new Long(10), new Long(20), "T3", fm);    // 6
    as.add(new Long(10), new Long(20), "T1", fm);    // 7
    as.add(new Long(10), new Long(20), "T3", fm);    // 8
    as.add(new Long(10), new Long(20), "T1", fm);    // 9
    as.add(new Long(10), new Long(20), "T1", fm);    // 10

    asBuf = as.get("T");
    assertEquals(null, asBuf);

    asBuf = as.get("T1");
    assertEquals(7, asBuf.size());
    asBuf = as.get("T2");
    assertEquals(1, asBuf.size());
    asBuf = as.get("T3");
    assertEquals(3, asBuf.size());

    // let's check that we've only got two nodes, what the ids are and so on;
    // first construct a sorted set of annotations
    SortedSet sortedAnnots = new TreeSet(as);

    // for checking the annotation id
    int idCounter = 0;
    Iterator iter = sortedAnnots.iterator();
    while(iter.hasNext()) {
      a = (Annotation) iter.next();

      // check annot ids
      assertEquals(idCounter++, a.getId().intValue());

      startNode = a.getStartNode();
      endNode = a.getEndNode();

      // start node id
      assertEquals(0,  startNode.getId().intValue());

      // start offset
      assertEquals(10, startNode.getOffset().longValue());

      // end id
      assertEquals(1,  endNode.getId().intValue());

      // end offset
      assertEquals(20, endNode.getOffset().longValue());
    }

  } // testTypeIndex()

  /** Test the annotations set add method that uses existing nodes */
  public void testAddWithNodes() throws Exception {
    FeatureMap params = Factory.newFeatureMap();
    params.put("sourceUrl", Gate.getUrl("tests/doc0.html"));
    params.put("markupAware", "false");
    Document doc = (Document)Factory.createResource("gate.corpora.DocumentImpl",
                                                    params);
    AnnotationSet as = new AnnotationSetImpl(doc);
    AnnotationSet asBuf;
    Integer newId;
    FeatureMap fm = new SimpleFeatureMapImpl();
    Annotation a;
    Node startNode;
    Node endNode;

    // to trigger type indexing
    as.get("T");
    newId = as.add(new Long(10), new Long(20), "T1", fm);    // 0
    a = as.get(newId);
    startNode = a.getStartNode();
    endNode = a.getEndNode();

    as.add(startNode, endNode, "T2", fm);    // 1
    as.add(startNode, endNode, "T3", fm);    // 2
    as.add(startNode, endNode, "T1", fm);    // 3
    as.add(startNode, endNode, "T1", fm);    // 4
    as.add(startNode, endNode, "T1", fm);    // 5
    as.add(startNode, endNode, "T3", fm);    // 6
    as.add(startNode, endNode, "T1", fm);    // 7
    as.add(startNode, endNode, "T3", fm);    // 8
    as.add(startNode, endNode, "T1", fm);    // 9
    as.add(startNode, endNode, "T1", fm);    // 10

    asBuf = as.get("T");
    assertEquals(null, asBuf);

    asBuf = as.get("T1");
    assertEquals(7, asBuf.size());
    asBuf = as.get("T2");
    assertEquals(1, asBuf.size());
    asBuf = as.get("T3");
    assertEquals(3, asBuf.size());

    // let's check that we've only got two nodes, what the ids are and so on;
    // first construct a sorted set of annotations
    SortedSet sortedAnnots = new TreeSet(as);

    // for checking the annotation id
    int idCounter = 0;
    Iterator iter = sortedAnnots.iterator();
    while(iter.hasNext()) {
      a = (Annotation) iter.next();
      // check annot ids
      assertEquals(idCounter++, a.getId().intValue());

      startNode = a.getStartNode();
      endNode = a.getEndNode();

      // start node id
      assertEquals(0,  startNode.getId().intValue());

      // start offset
      assertEquals(10, startNode.getOffset().longValue());

      // end id
      assertEquals(1,  endNode.getId().intValue());

      // end offset
      assertEquals(20, endNode.getOffset().longValue());
    }

  } // testAddWithNodes()

  /** Test complex get (with type, offset and feature contraints) */
  public void testComplexGet() throws InvalidOffsetException {
    AnnotationSet as = basicAS;
    AnnotationSet asBuf;
    Integer newId;
    FeatureMap fm = new SimpleFeatureMapImpl();
    Annotation a;
    Node startNode;
    Node endNode;

    FeatureMap constraints = new SimpleFeatureMapImpl();
    constraints.put("pos", "NN");

    //Out.println(basicAS);
    //Out.println(constraints);

    asBuf = basicAS.get("T1", constraints);
    assertEquals(3, asBuf.size());
    asBuf = basicAS.get("T3", constraints);
    assertEquals(1, asBuf.size());
    asBuf = basicAS.get("T1", constraints, new Long(12));
    assertEquals(2, asBuf.size());
    asBuf = basicAS.get("T1", constraints, new Long(10));
    assertEquals(1, asBuf.size());
    asBuf = basicAS.get("T1", constraints, new Long(11));
    assertEquals(2, asBuf.size());
    asBuf = basicAS.get("T1", constraints, new Long(9));
    assertEquals(1, asBuf.size());

    constraints.put("pos", "JJ");
    //Out.println(constraints);
    asBuf = basicAS.get("T1", constraints, new Long(0));
    assertEquals(null, asBuf);
    asBuf = basicAS.get("T1", constraints, new Long(14));
    assertEquals(2, asBuf.size());

    constraints.put("author", "valentin");
    asBuf = basicAS.get("T1", constraints, new Long(14));
    assertEquals(null, asBuf);

    constraints.put("author", "the devil himself");
    asBuf = basicAS.get("T1", constraints, new Long(14));
    assertEquals(2, asBuf.size());

    asBuf = basicAS.get("T1", constraints, new Long(5));
    assertEquals(null, asBuf);

    constraints.put("this feature isn't", "there at all");
    asBuf = basicAS.get("T1", constraints, new Long(14));
    assertEquals(null, asBuf);

  } // testComplexGet()

  /** Test remove */
  public void testRemove() {
    AnnotationSet asBuf = basicAS.get("T1");
    assertEquals(7, asBuf.size());
    asBuf = basicAS.get(new Long(9));
    assertEquals(5, asBuf.size());

    basicAS.remove(basicAS.get(new Integer(0)));

    assertEquals(10, basicAS.size());
    assertEquals(10, ((AnnotationSetImpl) basicAS).annotsById.size());

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
    assertEquals(10, ((AnnotationSetImpl) basicAS).annotsById.size());
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
  } // testIterator

  /** Test Set methods */
  public void testSetMethods() {
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

    AnnotationSet as = new AnnotationSetImpl(doc);
    assertEquals(as.size(), 0);

    // add some annotations
    Integer newId;
    newId =
      as.add(new Long(0), new Long(10), "Token", new SimpleFeatureMapImpl());
    assertEquals(newId.intValue(), 0);
    newId =
      as.add(new Long(11), new Long(12), "Token", new SimpleFeatureMapImpl());
    assertEquals(newId.intValue(), 1);
    assertEquals(as.size(), 2);
    assert(! as.isEmpty());
    newId =
      as.add(new Long(15), new Long(22), "Syntax", new SimpleFeatureMapImpl());

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
      assertEquals(a.getFeatures().size(), 0);
    }

    // add some more
    newId =
      as.add(new Long(0), new Long(12), "Syntax", new SimpleFeatureMapImpl());
    assertEquals(newId.intValue(), 3);
    newId =
      as.add(new Long(14), new Long(22), "Syntax", new SimpleFeatureMapImpl());
    assertEquals(newId.intValue(), 4);
    assertEquals(as.size(), 5);
    newId =
      as.add(new Long(15), new Long(22), "Syntax", new SimpleFeatureMapImpl());


    // indexing by type
    ((AnnotationSetImpl) as).indexByType();
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

  /** Test get with offset and no annotation starting at given offset */
  public void _testGap() throws InvalidOffsetException {
    AnnotationSet as = basicAS;
    as.clear();
    FeatureMap fm = Factory.newFeatureMap();
    fm.put("A", "B");
    as.add(new Long(0), new Long(10), "foo", fm);
    as.add(new Long(11), new Long(20), "foo", fm);
    as.add(new Long(10), new Long(11), "space", fm);

    //do the input selection (ignore spaces)
    Set input = new HashSet();
    input.add("foo");
    input.add("foofoo");
    AnnotationSet annotations = null;

    if(input.isEmpty()) annotations = as;
    else{
      Iterator typesIter = input.iterator();
      AnnotationSet ofOneType = null;

      while(typesIter.hasNext()){
        ofOneType = as.get((String)typesIter.next());

        if(ofOneType != null){
          //System.out.println("Adding " + ofOneType.getAllTypes());
          if(annotations == null) annotations = ofOneType;
          else annotations.addAll(ofOneType);
        }
      }
    }
    /* if(annotations == null) annotations = new AnnotationSetImpl(doc); */
    if (DEBUG)
      Out.println(
        "Actual input:" + annotations.getAllTypes() + "\n" + annotations
      );

    AnnotationSet res =
      annotations.get("foo", Factory.newFeatureMap(), new Long(10));

    if (DEBUG)
      Out.println(res);
    assert(!res.isEmpty());
  }

  /** Test Overlaps */
  public void testOverlapsAndCoextensive() throws InvalidOffsetException {
    Node node1 = new NodeImpl(new Integer(1),new Long(10));
    Node node2 = new NodeImpl(new Integer(2),new Long(20));
    Node node3 = new NodeImpl(new Integer(3),new Long(15));
    Node node4 = new NodeImpl(new Integer(4),new Long(15));
    Node node5 = new NodeImpl(new Integer(5),new Long(20));
    Node node6 = new NodeImpl(new Integer(6),new Long(30));

    FeatureMap fm1 = new SimpleFeatureMapImpl();
    fm1.put("color","red");
    fm1.put("Age",new Long(25));
    fm1.put(new Long(23), "Cristian");

    FeatureMap fm2 = new SimpleFeatureMapImpl();
    fm2.put("color","red");
    fm2.put("Age",new Long(25));
    fm2.put(new Long(23), "Cristian");

    FeatureMap fm4 = new SimpleFeatureMapImpl();
    fm4.put("color","red");
    fm4.put("Age",new Long(26));
    fm4.put(new Long(23), "Cristian");

    FeatureMap fm3 = new SimpleFeatureMapImpl();
    fm3.put("color","red");
    fm3.put("Age",new Long(25));
    fm3.put(new Long(23), "Cristian");
    fm3.put("best",new Boolean(true));

    // Start=10, End = 20
    Annotation annot1 = new AnnotationImpl(new Integer(1),
                                           node1,
                                           node2,
                                           "pos",
                                           null);
    // Start=20, End = 30
    Annotation annot2 = new AnnotationImpl (new Integer(2),
                                            node2,
                                            node6,
                                            "pos",
                                            null);
    // Start=20, End = 30
    Annotation annot3 = new AnnotationImpl (new Integer(3),
                                            node5,
                                            node6,
                                            "pos",
                                            null);
    // Start=20, End = 20
    Annotation annot4 = new AnnotationImpl (new Integer(4),
                                            node2,
                                            node5,
                                            "pos",
                                            null);
    // Start=10, End = 30
    Annotation annot5 = new AnnotationImpl (new Integer(5),
                                            node1,
                                            node6,
                                            "pos",
                                            null);
    // Start=10, End = 15
    Annotation annot6 = new AnnotationImpl (new Integer(6),
                                            node1,
                                            node4,
                                            "pos",
                                            null);
    // Start=null, End = null
    Annotation annot7 = new AnnotationImpl (new Integer(7),
                                            null,
                                            null,
                                            "pos",
                                            null);

    // MAP
    // annot1 -> Start=10, End = 20
    // annot2 -> Start=20, End = 30
    // annot3 -> Start=20, End = 30
    // annot4 -> Start=20, End = 20
    // annot5 -> Start=10, End = 30
    // annot6 -> Start=10, End = 15

    // Not overlaping situations
   assert("Those annotations does not overlap!",!annot1.overlaps(annot3));
   assert("Those annotations does not overlap!",!annot1.overlaps(annot2));
   assert("Those annotations does not overlap!",!annot2.overlaps(annot1));
   assert("Those annotations does not overlap!",!annot3.overlaps(annot1));
   assert("Those annotations does not overlap!",!annot4.overlaps(annot6));
   assert("Those annotations does not overlap!",!annot6.overlaps(annot4));

   assert("Those annotations does not overlap!",!annot6.overlaps(null));
   assert("Those annotations does not overlap!",!annot1.overlaps(annot7));

   // Overlaping situations
   assert("Those annotations does overlap!",annot4.overlaps(annot5));
   assert("Those annotations does overlap!",annot5.overlaps(annot4));
   assert("Those annotations does overlap!",annot1.overlaps(annot6));
   assert("Those annotations does overlap!",annot6.overlaps(annot1));
   assert("Those annotations does overlap!",annot2.overlaps(annot5));
   assert("Those annotations does overlap!",annot5.overlaps(annot2));

   // Not coextensive situations
   assert("Those annotations are not coextensive!",!annot1.coextensive(annot2));
   assert("Those annotations are not coextensive!",!annot2.coextensive(annot1));
   assert("Those annotations are not coextensive!",!annot4.coextensive(annot3));
   assert("Those annotations are not coextensive!",!annot3.coextensive(annot4));
   assert("Those annotations are not coextensive!",!annot4.coextensive(annot7));
   assert("Those annotations are not coextensive!",!annot5.coextensive(annot6));
   assert("Those annotations are not coextensive!",!annot6.coextensive(annot5));
   //Coextensive situations
   assert("Those annotations are coextensive!",annot2.coextensive(annot2));
   assert("Those annotations are coextensive!",annot2.coextensive(annot3));
   assert("Those annotations are coextensive!",annot3.coextensive(annot2));

  }//testOverlapsAndCoextensive

  /** Test Coextensive */
  public void testIsPartiallyCompatibleAndCompatible()
                                                throws InvalidOffsetException {
    Node node1 = new NodeImpl(new Integer(1),new Long(10));
    Node node2 = new NodeImpl(new Integer(2),new Long(20));
    Node node3 = new NodeImpl(new Integer(3),new Long(15));
    Node node4 = new NodeImpl(new Integer(4),new Long(15));
    Node node5 = new NodeImpl(new Integer(5),new Long(20));
    Node node6 = new NodeImpl(new Integer(6),new Long(30));

    FeatureMap fm1 = new SimpleFeatureMapImpl();
    fm1.put("color","red");
    fm1.put("Age",new Long(25));
    fm1.put(new Long(23), "Cristian");

    FeatureMap fm2 = new SimpleFeatureMapImpl();
    fm2.put("color","red");
    fm2.put("Age",new Long(25));
    fm2.put(new Long(23), "Cristian");

    FeatureMap fm4 = new SimpleFeatureMapImpl();
    fm4.put("color","red");
    fm4.put("Age",new Long(26));
    fm4.put(new Long(23), "Cristian");

    FeatureMap fm3 = new SimpleFeatureMapImpl();
    fm3.put("color","red");
    fm3.put("Age",new Long(25));
    fm3.put(new Long(23), "Cristian");
    fm3.put("best",new Boolean(true));

    // Start=10, End = 20
    Annotation annot1 = new AnnotationImpl(new Integer(1),
                                           node1,
                                           node2,
                                           "pos",
                                           fm1);
    // Start=20, End = 30
    Annotation annot2 = new AnnotationImpl (new Integer(2),
                                            node2,
                                            node6,
                                            "pos",
                                            fm2);
    // Start=20, End = 30
    Annotation annot3 = new AnnotationImpl (new Integer(3),
                                            node5,
                                            node6,
                                            "pos",
                                            fm3);
    // Start=20, End = 20
    Annotation annot4 = new AnnotationImpl (new Integer(4),
                                            node2,
                                            node5,
                                            "pos",
                                            fm4);
    // Start=10, End = 30
    Annotation annot5 = new AnnotationImpl (new Integer(5),
                                            node1,
                                            node6,
                                            "pos",
                                            fm3);
    // Start=10, End = 15
    Annotation annot6 = new AnnotationImpl (new Integer(6),
                                            node1,
                                            node4,
                                            "pos",
                                            fm1);
    // Start=null, End = null
    Annotation annot7 = new AnnotationImpl (new Integer(7),
                                            null,
                                            null,
                                            "pos",
                                            null);

// MAP
  /*
   annot1 -> Start=10, End = 20,{color="red",Age="25",23="Cristian"}
   annot2 -> Start=20, End = 30,{color="red",Age="25",23="Cristian"}
   annot3 -> Start=20, End = 30,{color="red",Age="25",23="Cristian",best="true"}
   annot4 -> Start=20, End = 20,{color="red",Age="26",23="Cristian"}
   annot5 -> Start=10, End = 30,{color="red",Age="25",23="Cristian",best="true"}
   annot6 -> Start=10, End = 15,{color="red",Age="25",23="Cristian"}
  */

  // Not compatible situations
  assert("Those annotations are not compatible!",!annot3.isCompatible(annot2));

  // Not partially compatible situations
  assert("Those annotations are not partially compatible!",
                                       !annot1.isPartiallyCompatible(annot2));
  assert("Those annotations are not partially compatible!",
                                       !annot1.isPartiallyCompatible(annot3));
  assert("Those annotations are not partially compatible!",
                                       !annot1.isPartiallyCompatible(annot4));
  assert("Those annotations are not partially compatible!",
                                       !annot4.isPartiallyCompatible(annot5));


  // Compatible situations
  assert("Those annotations are compatible!",annot2.isCompatible(annot3));
  assert("Those annotations are compatible!",annot4.isCompatible(annot4));

  // Partially compatible situations
  assert("Those annotations are partially compatible!",
                                        annot2.isPartiallyCompatible(annot3));
  assert("Those annotations are partially compatible!",
                                        annot2.isPartiallyCompatible(annot2));
  assert("Those annotations are partially compatible!",
                                        annot4.isPartiallyCompatible(annot4));
  assert("Those annotations are partially compatible!",
                                        annot1.isPartiallyCompatible(annot5));
  assert("Those annotations are partially compatible!",
                                        annot1.isPartiallyCompatible(annot6));
  assert("Those annotations are partially compatible!",
                                        annot3.isPartiallyCompatible(annot5));
  assert("Those annotations are partially compatible!",
                                        annot5.isPartiallyCompatible(annot3));
  assert("Those annotations are partially compatible!",
                                        annot6.isPartiallyCompatible(annot5));

  }// testIsPartiallyCompatibleAndCompatible

  public static void main(String[] args){

    try{
      Gate.init();
      TestAnnotation testAnnot = new TestAnnotation("");
      testAnnot.setUp();
      testAnnot.testIterator();
      testAnnot._testGap();
      testAnnot.tearDown();
      testAnnot.testOverlapsAndCoextensive();
      testAnnot.testIsPartiallyCompatibleAndCompatible();
    }catch(Throwable t){
      t.printStackTrace();
    }
  }
} // class TestAnnotation

