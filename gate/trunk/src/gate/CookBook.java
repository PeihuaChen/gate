/*
 *	CookBook.java
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
 *	Hamish Cunningham, 16/Feb/2000
 *
 *	$Id$
 */

package gate;

import java.util.*;
import java.net.*;
import java.io.*;
import junit.framework.*;

import gate.*;
import gate.util.*;


/**
<P>
This class provides examples of using the GATE APIs.
Read this documentation along with a copy of the
<A HREF=CookBook.txt>source code</A>.

<P>
Rule 1: if it's not in the <TT>gate</TT> package, don't use it!
Contact the <A HREF=mailto:santa@north.pole>GATE development team</A>
and tell them what you're trying to do, and we'll try and make that
available at the top level. (The other packages are for internal use.)

<P>
The CookBook is set up as
part of the GATE test suite (using the JUnit framework), so there's an easy
way to run the examples (viz.,
<A HREF=../gate/TestGate.html>gate.TestGate.main</A>, which will invoke the
JUnit test runner). Also, we can use JUnit's assert methods, e.g.
<TT>assert(corpus.isEmpty());</TT>
tests that a corpus object is empty, and creates a test failure report if
this is not the case.

<P>
Programming to the GATE Java API involves manipulating the classes and
interfaces in the <A HREF=package-summary.html>gate package</A>. These are
mainly interfaces; the classes that do exist are mainly to do with getting
access to objects that implement the interfaces (without exposing those
implementations). In other words, it's an interface-based design.

<P>
Two classes take care of instantiating objects that implement the interfaces:
<A HREF=DataStore.html>DataStore</A> and <A HREF=Transients.html>Transients</A>.

<A HREF=DataStore.html>DataStore</A> allows the creation of objects that
are stored in databases
(NOT IMPLEMENTED YET!!!).

<A HREF=Transients.html>Transients</A> provides static methods that
construct new transient
objects, i.e. objects whose lifespan is bounded by the current invocation of
the program.

<P>
The <A HREF=Corpus.html>Corpus interface</A> represents collections of
<A HREF=Document.html>Documents</A> (and takes the place of the old TIPSTER
<TT>Collection</TT> class).

<P>
The
<A HREF=#testCorpusConstruction()>testCorpusConstruction</A> method gives
an example of how to create a new transient Corpus object.

<P>
The <A HREF=#testAddingDocuments()>testAddingDocuments</A> method gives
examples of adding documents to corpora.

<P>
The <A HREF=#testAddingAnnotations()>testAddingAnnotations</A> method gives
examples of adding annotations to documents.


<P>
The <A HREF=#testUsingFeatures()>testUsingFeatures</A> method gives
examples of using features. <A HREF=FeatureMap.html>The FeatureMap
interface</A> is a mechanism for associating arbitrary data with GATE
entities. Corpora, documents and annotations all share this
mechanism. Simple feature maps use Java's Map interface.


<H3>Other sources of examples</H3>

<P>
See also the other test classes, although note that they also use methods
that are not part of the public API (which is restricted to the <TT>gate</TT>
package. Test classes:
<A HREF=corpora/TestCorpus.html>TestCorpus</A>;
<A HREF=corpora/TestDocument.html>TestDocument</A>;
<A HREF=corpora/TestAnnotation.html>TestAnnotation</A>.

**/
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

  /** Constructing a corpus */
  public void testCorpusConstruction() {

    // corpus constructors require a name
    corpus = Transients.newCorpus("My example corpus");

    // the corpus interface inherits all the sorted set methods
    assert(corpus.isEmpty());

  } // testCorpusConstruction

  /** Adding documents to a corpus */
  public void testAddingDocuments() throws GateException {
    corpus = Transients.newCorpus("My example corpus");

    // document constructors may take a URL; if so you have
    // to deal with URL and net-related exceptions:
    URL u = null;
    u = Gate.getUrl("tests/doc0.html");

    // some set methods
    Iterator iter = corpus.iterator();
    while(iter.hasNext()) {
      Document doc = (Document) iter.next();
      assert(u.equals(doc.getSourceURL()));
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
    FeatureMap fm = Transients.newFeatureMap();
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
    corpus = Transients.newCorpus("My example corpus");

    doc1 = Transients.newDocument(Gate.getUrl("tests/doc0.html"));
    doc2 = Transients.newDocument(Gate.getUrl("tests/doc0.html"));
  } // setUp

  /** Construction */
  public CookBook(String name) { super(name); }

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(CookBook.class);
  } // suite


} // class CookBook