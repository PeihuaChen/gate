/*
 *	CookBook.java
 *
 *	Hamish Cunningham, 16/Feb/2000
 *
 *	$Id$
 */

package gate;

import java.util.*;
import junit.framework.*;

import gate.*;


/**
<P>
This class provides examples of using the GATE APIs. It is set up as
part of the test suite (using the JUnit framework), so there's an easy
way to run the examples (viz.,
<A HREF=../gate/TestGate.html>gate.TestGate.main</A>, which will invoke the
JUnit test runner). Also, we can use JUnit's assert methods, e.g.
<PRE>
assert(corpus.isEmpty());
</PRE>
tests that a corpus object is empty, and creates a test failure report if
this is not the case.

<P>

**/
public class CookBook extends TestCase
{
  /** Constructing a corpus */
  public void testCorpusConstruction() {
    Corpus corpus = Transients.newCorpus("My example corpus");
    assert(corpus.isEmpty());
  } // constructCorpus




  /** Fixture set up */
  public void setUp() {
  } // setUp

  /** Construction */
  public CookBook(String name) { super(name); }

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(CookBook.class);
  } // suite

  
} // class CookBook
