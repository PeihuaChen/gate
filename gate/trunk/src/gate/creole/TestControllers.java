/*
 *  TestControllers.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 16/Mar/00
 *
 *  $Id$
 */

package gate.creole;

import java.util.*;
import java.io.*;
import java.net.*;
import junit.framework.*;

import gate.*;
import gate.creole.*;
import gate.util.*;

/** Tests for controller classes
  */
public class TestControllers extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** The CREOLE register */
  CreoleRegister reg;

  /** Construction */
  public TestControllers(String name) { super(name); }

  /** Fixture set up */
  public void setUp() throws GateException {
    // Initialise the GATE library and get the creole register
    Gate.init();
    reg = Gate.getCreoleRegister();

  } // setUp

  /** Put things back as they should be after running tests
    * (reinitialise the CREOLE register).
    */
  public void tearDown() throws Exception {
    reg.clear();
    Gate.init();
  } // tearDown

  /** Serial controller test 1 */
  public void testSerial1() throws Exception {
    // a controller
    SerialController c1 = new SerialController();
    assertNotNull("c1 controller is null", c1);

    //get a document
    FeatureMap params = Factory.newFeatureMap();
    params.put("sourceUrl", Gate.getUrl("tests/doc0.html"));
    params.put("markupAware", "false");
    Document doc = (Document)Factory.createResource("gate.corpora.DocumentImpl",
                                                    params);

    if(DEBUG) {
      ResourceData docRd = (ResourceData) reg.get("gate.corpora.DocumentImpl");
      assertNotNull("Couldn't find document res data", docRd);
      Out.prln(docRd.getParameterList().getInitimeParameters());
    }

    //create a default tokeniser
    params = Factory.newFeatureMap();
    params.put("tokeniserRulesURL",
                "gate:/creole/tokeniser/DefaultTokeniser.rules");
    params.put("transducerGrammarURL",
                "gate:/creole/tokeniser/postprocess.jape");
    params.put("encoding", "UTF-8");
    params.put("document", doc);
    ProcessingResource tokeniser = (ProcessingResource) Factory.createResource(
      "gate.creole.tokeniser.DefaultTokeniser", params
    );

    //create a default gazetteer
    params = Factory.newFeatureMap();
    params.put("document", doc);
    params.put("listsURL", "gate:/creole/gazeteer/default/lists.def");
    ProcessingResource gaz = (ProcessingResource) Factory.createResource(
      "gate.creole.gazetteer.DefaultGazetteer", params
    );

    // get the controller to encapsulate the tok and gaz
    c1.add(tokeniser);
    c1.add(gaz);
    c1.execute();

    // check the resulting annotations
    if(DEBUG) {
      Out.prln(doc.getAnnotations());
      Out.prln(doc.getContent());
    }
    AnnotationSet annots = doc.getAnnotations();
    assertTrue("no annotations from doc!", !annots.isEmpty());
    Annotation a = annots.get(new Integer(580));
    assertNotNull("couldn't get annot with id 580", a);
//sorry, this is no way to write a test!
//    assert( // check offset - two values depending on whether saved with \r\n
//      "wrong value: " + a.getStartNode().getOffset(),
//      (a.getStartNode().getOffset().equals(new Long(1360)) ||
//      a.getStartNode().getOffset().equals(new Long(1367)))
//    );
//    assert( // check offset - two values depending on whether saved with \r\n
//      "wrong value: " + a.getEndNode().getOffset(),
//      a.getEndNode().getOffset().equals(new Long(1361)) ||
//      a.getEndNode().getOffset().equals(new Long(1442))
//    );
  } // testSerial1()

  /** Serial controller test 2 */
  public void testSerial2() throws Exception {
    // a controller
    Controller c1 = new SerialController();
    assertNotNull("c1 controller is null", c1);
/*
    // a couple of PRs
    ResourceData pr1rd = (ResourceData) reg.get("testpkg.TestPR1");
    ResourceData pr2rd = (ResourceData) reg.get("testpkg.TestPR2");
    assert("couldn't find PR1/PR2 res data", pr1rd != null && pr2rd != null);
    assert("wrong name on PR1", pr1rd.getName().equals("Sheffield Test PR 1"));
    ProcessingResource pr1 = (ProcessingResource)
      Factory.createResource("testpkg.TestPR1", Factory.newFeatureMap());
    ProcessingResource pr2 = (ProcessingResource)
      Factory.createResource("testpkg.TestPR2", Factory.newFeatureMap());

    // add the PRs to the controller and run it
    c1.add(pr1);
    c1.add(pr2);
    c1.run();
*/
  } // testSerial2()

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestControllers.class);
  } // suite

} // class TestControllers
