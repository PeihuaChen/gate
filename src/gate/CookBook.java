/*
 *  CookBook.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 16/Feb/2000
 *
 *  $Id$
 */

package gate;

import java.util.*;
import java.net.*;
import java.io.*;
import junit.framework.*;

import gate.*;
import gate.util.*;


/**
  * <P>
  * This class provides examples of using the GATE APIs.
  * Read this documentation along with a copy of the
  * <A HREF=http://gate.ac.uk/gate/doc/java2html/gate/CookBook.java.html>source
  * code</A>.
  *
  * <P>
  * The CookBook is set up as
  * part of the GATE test suite (using the
  * <A HREF="http://www.junit.org/>JUnit testing framework</A>), so there's
  * an easy way to run the examples (viz.,
  * <A HREF=../gate/TestGate.html>gate.TestGate</A>'s <TT>main</TT> method,
  * which will invoke the
  * JUnit test runner). Also, we can use JUnit's assert methods: e.g.
  * <TT>assert(corpus.isEmpty());</TT>
  * tests that a corpus object is empty, and creates a test failure report if
  * this is not the case. (To add a new test class to the suite, see the
  * <A HREF=../gate/util/TestTemplate.html>gate.util.TestTemplate</A> class.)
  *
  * <P>
  * Programming to the GATE Java API involves manipulating the classes and
  * interfaces in the <A HREF=package-summary.html>gate package</A>
  * (and to a lesser extent other packages). These are
  * often interfaces; classes there are often to do with getting
  * access to objects that implement the interfaces (without exposing those
  * implementations). In other words, there's a lot of interface-based design
  * around.
  *
  * <P>
  * For more details and for a conceptual view, see
  * <A HREF=http://gate.ac.uk/sale/tao/>Developing Language Processing
  * Components with GATE</A> (for which this class provides some of the
  * examples).
  *
  * <P>
  * The rest of this documentation refers to methods in the code that
  * provide examples of using the GATE API.
  *
  * <P>
  * The <A HREF=#testResourceCreation()>testResourceCreation</A> method gives
  * an example of creating a resource via
  * <A HREF=../gate/Factory.html>gate.Factory</A>.
  *
  * <P>
  * The <A HREF=Corpus.html>Corpus interface</A> represents collections of
  * <A HREF=Document.html>Documents</A> (and takes the place of the old TIPSTER
  * <TT>Collection</TT> class).
  *
  * <P>
  * The <A HREF=#testCorpusConstruction()>testCorpusConstruction</A> method
  * gives an example of how to create a new transient Corpus object.
  *
  * <P>
  * The <A HREF=#testAddingDocuments()>testAddingDocuments</A> method gives
  * examples of adding documents to corpora.
  *
  * <P>
  * The <A HREF=#testAddingAnnotations()>testAddingAnnotations</A> method gives
  * examples of adding annotations to documents.
  *
  *
  * <P>
  * The <A HREF=#testUsingFeatures()>testUsingFeatures</A> method gives
  * examples of using features. <A HREF=FeatureMap.html>The FeatureMap
  * interface</A> is a mechanism for associating arbitrary data with GATE
  * entities. Corpora, documents and annotations all share this
  * mechanism. Simple feature maps use Java's Map interface.
  *
  *
  * <H3>Other sources of examples</H3>
  *
  * <P>
  * See also the other test classes, although note that they also use methods
  * that are not part of the public API. Test classes include:
  * <A HREF=corpora/TestCreole.html>TestCreole</A>;
  * <A HREF=corpora/TestCorpus.html>TestCorpus</A>;
  * <A HREF=corpora/TestDocument.html>TestDocument</A>;
  * <A HREF=corpora/TestAnnotation.html>TestAnnotation</A>; anything
  * else starting "Test" - about 30 of them at the last count.
  */
public class CookBook extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** A corpus */
  Corpus corpus = null;

  /** A document */
  Document doc1 = null;

  /** Another document */
  Document doc2 = null;

  /** Constructing a resource */
  public void testResourceCreation() throws GateException {

    // before creating a resource we need a feature map to store
    // parameter values
    FeatureMap params = Factory.newFeatureMap();

    // to create a document we need a sourceUrlName parameter giving
    // the location of the source for the document content
    params.put("sourceUrl", Gate.getUrl("tests/doc0.html"));
    params.put("markupAware", new Boolean(true));
    Resource res = Factory.createResource("gate.corpora.DocumentImpl", params);

    // now we have a document
    assert(
      "should be document but the class is: " + res.getClass().getName(),
      res instanceof gate.Document
    );
    Document doc = (Document) res;
    AnnotationSet markupAnnotations = doc.getAnnotations("Original markups");
    //this is useless as doc.getAnnotations() will never return null!
    assertNotNull("no markup annotations on doc " + doc, markupAnnotations);
    int numMarkupAnnotations = markupAnnotations.size();
    if(DEBUG)
      Out.prln("annotations on doc after unpack= " + numMarkupAnnotations);
    assert(
      "wrong number annots on doc: " + doc + numMarkupAnnotations,
      numMarkupAnnotations == 27
    );

  } // testResourceCreation

  /** Constructing a corpus */
  public void testCorpusConstruction() throws GateException {

    // corpus constructors require a name
    corpus = Factory.newCorpus("My example corpus");

    // the corpus interface inherits all the sorted set methods
    assert(corpus.isEmpty());

  } // testCorpusConstruction

  /** Adding documents to a corpus */
  public void testAddingDocuments() throws GateException {

    corpus = Factory.newCorpus("My example corpus");

    // add a document or two....
    corpus.add(doc1);
    corpus.add(doc2);

    // iterate the corpus members and do some random tests
    Iterator iter = corpus.iterator();
    while(iter.hasNext()) {
      Document doc = (Document) iter.next();
      assert(
        "document url not as expected",
        doc.getSourceUrl().toExternalForm().endsWith("doc0.html") ||
          doc.getSourceUrl().toExternalForm().endsWith("test1.htm")
      );
    } // while

  } // testAddingDocuments

  /** Adding annotations to documents */
  public void testAddingAnnotations() {
    AnnotationSet as = doc1.getAnnotations();
    FeatureMap fm = doc1.getFeatures();
    Integer id;

    // during creation of annotations offsets are checked and an invalid
    // offset exception thrown if they are invalid
    try {
      id = as.add(new Long(10), new Long(20), "T1", fm);
    } catch (InvalidOffsetException e) {
      fail(e.toString());
    }
  } // testAddingAnnotations

  /** Using the FeatureMap interface */
  public void testUsingFeatures() {
    AnnotationSet as = doc1.getAnnotations();
    Integer id; // the id of new annotations

    // putting features on documents
    FeatureMap fm = Factory.newFeatureMap();
    doc1.setFeatures(fm);
    assert(fm.size() == 0);
    fm.put("author", "segovia");
    assert(fm.get("author").equals("segovia"));
    fm.put("author", "brendl"); // map puts overwrite existing values
    assert(fm.get("author").equals("brendl"));
    assert(fm.size() == 1);

  } // testUsingFeatures

  /** Fixture set up: initialise members before each test method */
  public void setUp() throws GateException, IOException {
    corpus = Factory.newCorpus("My example corpus");

    doc1 = Factory.newDocument(Gate.getUrl("tests/doc0.html"));
    doc2 = Factory.newDocument(Gate.getUrl("tests/html/test1.htm"));
  } // setUp

  /** Construction */
  public CookBook(String name) { super(name); }

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(CookBook.class);
  } // suite

} // class CookBook
