/*
 *  TestBumpyStack.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 10/June/00
 *
 *  $Id$
 */

package gate.util;

import java.util.*;
import java.io.*;
import junit.framework.*;
import java.net.*;

import gate.*;
import gate.creole.*;

/** BumpyStack test class.
  */
public class TestBumpyStack extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestBumpyStack(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestBumpyStack.class);
  } // suite

  /** Test the bumpiness of the thing. */
  public void testBumpiness() throws Exception {
    WeakBumpyStack bumper = new WeakBumpyStack();

    String s1 = new String("s1");
    String s2 = new String("s2");
    String s3 = new String("s3");

    bumper.push(s3);
    bumper.push(s2);
    bumper.push(s1);

    assert(
      "managed to bump non-existent element",
      ! bumper.bump(new String("something"))
    );

    assert("stack wrong length (I): " + bumper.size(), bumper.size() == 3);
    assert("couldn't bump s2", bumper.bump(s2));
    assert("s2 not front of stack", ((String) bumper.pop()).equals("s2"));
    assert("stack wrong length (II)" + bumper.size(), bumper.size() == 2);
  } // testBumpiness()

  /**
   * Tests whether the CreoleRegisterImpl keeps unreacheable resourecs alive
   */
  public void testSelfCleaning() throws Exception {
    //force GC
    System.gc();

    //count instances
    Collection instances = ((ResourceData)
                           Gate.getCreoleRegister().
                           get("gate.corpora.DocumentImpl")).
                           getInstantiations();
    int docCnt = instances == null ? 0: instances.size();

    instances = ((ResourceData)
                  Gate.getCreoleRegister().
                  get("gate.corpora.CorpusImpl")).
                  getInstantiations();
    int corpusCnt = instances == null ? 0: instances.size();

    instances = ((ResourceData)
                  Gate.getCreoleRegister().
                  get("gate.creole.tokeniser.DefaultTokeniser")).
                  getInstantiations();
    int tokCnt = instances == null ? 0: instances.size();

    instances = ((ResourceData)
                  Gate.getCreoleRegister().
                  get("gate.creole.ANNIETransducer")).
                  getInstantiations();
    int japeCnt = instances == null ? 0: instances.size();

    instances = ((ResourceData)
                  Gate.getCreoleRegister().
                  get("gate.creole.SerialController")).
                  getInstantiations();
    int serctlCnt = instances == null ? 0: instances.size();

    //create some unreacheable resources
    //LRs

    Resource res = Factory.newCorpus("corpus1");
    res.getFeatures().put("large", new byte[500000]);
    res = Factory.newCorpus("corpus2");
    res.getFeatures().put("large", new byte[500000]);
    res = Factory.newCorpus("corpus3");
    res.getFeatures().put("large", new byte[500000]);

    res = Factory.newDocument("content");
    res.getFeatures().put("large", new byte[500000]);
    res = Factory.newDocument(Gate.getUrl("tests/doc0.html"));
    res.getFeatures().put("large", new byte[500000]);
    res = Factory.newDocument(Gate.getUrl("tests/doc0.html"));
    res.getFeatures().put("large", new byte[500000]);
    res = Factory.newDocument(Gate.getUrl("tests/doc0.html"));
    res.getFeatures().put("large", new byte[500000]);

    //PRs
    res = Factory.createResource("gate.creole.tokeniser.DefaultTokeniser");
    res.getFeatures().put("large", new byte[500000]);
    res = Factory.createResource("gate.creole.tokeniser.DefaultTokeniser");
    res.getFeatures().put("large", new byte[500000]);
    res = Factory.createResource("gate.creole.tokeniser.DefaultTokeniser");
    res.getFeatures().put("large", new byte[500000]);

    res = Factory.createResource("gate.creole.ANNIETransducer");
    res.getFeatures().put("large", new byte[500000]);

    //Controllers
    res = Factory.createResource("gate.creole.SerialController");
    res.getFeatures().put("large", new byte[500000]);
    res = Factory.createResource("gate.creole.SerialController");
    res.getFeatures().put("large", new byte[500000]);
    res = Factory.createResource("gate.creole.SerialController");
    res.getFeatures().put("large", new byte[500000]);

    res = null;


    //force GC
    Thread.currentThread().sleep(1000);
    System.gc();
    Thread.currentThread().sleep(1000);
    System.gc();

    //check instances count
    int newDocCnt = ((ResourceData)
                  Gate.getCreoleRegister().
                  get("gate.corpora.DocumentImpl")).
                  getInstantiations().size();

    int newCorpusCnt = ((ResourceData)
                  Gate.getCreoleRegister().
                  get("gate.corpora.CorpusImpl")).
                  getInstantiations().size();
    int newTokCnt = ((ResourceData)
                  Gate.getCreoleRegister().
                  get("gate.creole.tokeniser.DefaultTokeniser")).
                  getInstantiations().size();
    int newJapeCnt = ((ResourceData)
                  Gate.getCreoleRegister().
                  get("gate.creole.ANNIETransducer")).
                  getInstantiations().size();
    int newSerctlCnt = ((ResourceData)
                  Gate.getCreoleRegister().
                  get("gate.creole.SerialController")).
                  getInstantiations().size();

    String message =
          "\nDocs expected: " + docCnt + ", got: " + newDocCnt +
          "\nCorpora expected: " + corpusCnt + ", got: " + newCorpusCnt +
          "\nTokenisers expected: " + tokCnt + ", got: " + newTokCnt +
          "\nJapes expected: " + japeCnt + ", got: " + newJapeCnt +
          "\nSerCtls expected: " + serctlCnt + ", got: " + newSerctlCnt;

    assert(message, docCnt == newDocCnt &&
                    corpusCnt == newCorpusCnt &&
                    tokCnt == newTokCnt &&
                    japeCnt == newJapeCnt &&
                    serctlCnt == newSerctlCnt);
  }




} // class TestBumpyStack
