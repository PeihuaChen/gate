/*
 *  TestDocument.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 21/Jan/00
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

/** Tests for the Document classes
  */
public class TestDocument extends TestCase
{

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestDocument(String name) { super(name); setUp();}

  /** Base of the test server URL */
  protected static String testServer = null;

  /** Name of test document 1 */
  protected String testDocument1;

  /** Fixture set up */
  public void setUp() {

    try{
      Gate.init();
      testServer = Gate.getUrl().toExternalForm();
    } catch (GateException e){
      e.printStackTrace(Err.getPrintWriter());
    }

    testDocument1 = "tests/html/test2.htm";
  } // setUp

  /** Get the name of the test server */
  public static String getTestServerName() {
    if(testServer != null) return testServer;
    else{
      try { testServer = Gate.getUrl().toExternalForm(); }
      catch(Exception e) { }
      return testServer;
    }
  }

  /** Test ordering */
  public void testCompareTo() throws Exception{
    Document doc1 = null;
    Document doc2 = null;
    Document doc3 = null;


    doc1 = Factory.newDocument(new URL(testServer + "tests/def"));
    doc2 = Factory.newDocument(new URL(testServer + "tests/defg"));
    doc3 = Factory.newDocument(new URL(testServer + "tests/abc"));

    assertTrue(doc1.compareTo(doc2) < 0);
    assertTrue(doc1.compareTo(doc1) == 0);
    assertTrue(doc1.compareTo(doc3) > 0);

  } // testCompareTo()

  /** Test loading of the original document content */

  public void testOriginalContentPreserving() throws Exception {
    Document doc = null;
    FeatureMap params;
    String encoding = "UTF-8";
    String origContent;

    // test the default value of preserve content flag
    params = Factory.newFeatureMap();
    params.put(Document.DOCUMENT_URL_PARAMETER_NAME, new URL(testServer + testDocument1));
    params.put(Document.DOCUMENT_ENCODING_PARAMETER_NAME, encoding);
    doc =
      (Document) Factory.createResource("gate.corpora.DocumentImpl", params);

    origContent = (String) doc.getFeatures().get(
      GateConstants.ORIGINAL_DOCUMENT_CONTENT_FEATURE_NAME);

    assertNull(
      "The original content should not be preserved without demand.",
      origContent);

    params = Factory.newFeatureMap();
    params.put(Document.DOCUMENT_URL_PARAMETER_NAME,
      new URL(testServer + testDocument1));
    params.put(Document.DOCUMENT_ENCODING_PARAMETER_NAME, encoding);
    params.put(Document.DOCUMENT_PRESERVE_CONTENT_PARAMETER_NAME, new Boolean(true));
    doc =
      (Document) Factory.createResource("gate.corpora.DocumentImpl", params);

    origContent = (String) doc.getFeatures().get(
      GateConstants.ORIGINAL_DOCUMENT_CONTENT_FEATURE_NAME);

    assertNotNull("The original content is not preserved on demand.",
              origContent);

    assertTrue("The original content size is zerro.", origContent.length()>0);
  } // testOriginalContentPreserving()

  /** A comprehensive test */
  public void testLotsOfThings() {

    // check that the test URL is available
    URL u = null;
    try{
      u = new URL(testServer + testDocument1);
    } catch (Exception e){
      e.printStackTrace(Err.getPrintWriter());
    }

    // get some text out of the test URL
    BufferedReader uReader = null;
    try {
      uReader = new BufferedReader(new InputStreamReader(u.openStream()));
      assertEquals(uReader.readLine(), "<HTML>");
    } catch(UnknownHostException e) { // no network connection
      return;
    } catch(IOException e) {
      fail(e.toString());
    }
    /*
    Document doc = new TextualDocument(testServer + testDocument1);
    AnnotationGraph ag = new AnnotationGraphImpl();

    Tokeniser t = ...   doc.getContent()
    tokenise doc using java stream tokeniser

    add several thousand token annotation
    select a subset
    */
  } // testLotsOfThings

  /** The reason this is method begins with verify and not with test is that it
   *  gets called by various other test methods. It is somehow a utility test
   *  method. It should be called on all gate documents having annotation sets.
   */
  public static void verifyNodeIdConsistency(gate.Document doc)throws Exception{
      if (doc == null) return;
      Map offests2NodeId = new HashMap();
      // Test the default annotation set
      AnnotationSet annotSet = doc.getAnnotations();
      verifyNodeIdConsistency(annotSet,offests2NodeId, doc);
      // Test all named annotation sets
      if (doc.getNamedAnnotationSets() != null){
        Iterator namedAnnotSetsIter =
                              doc.getNamedAnnotationSets().values().iterator();
        while(namedAnnotSetsIter.hasNext()){
         verifyNodeIdConsistency((gate.AnnotationSet) namedAnnotSetsIter.next(),
                                                                 offests2NodeId,
                                                                 doc);
        }// End while
      }// End if
      // Test suceeded. The map is not needed anymore.
      offests2NodeId = null;
  }// verifyNodeIdConsistency();

  /** This metod runs the test over an annotation Set. It is called from her
   *  older sister. Se above.
   *  @param annotSet is the annotation set being tested.
   *  @param offests2NodeId is the Map used to test the consistency.
   *  @param doc is used in composing the assert error messsage.
   */
  public static void verifyNodeIdConsistency(gate.AnnotationSet annotSet,
                                             Map  offests2NodeId,
                                             gate.Document doc)
                                                              throws Exception{

      if (annotSet == null || offests2NodeId == null) return;

      Iterator iter = annotSet.iterator();
      while(iter.hasNext()){
        Annotation annot = (Annotation) iter.next();
        String annotSetName = (annotSet.getName() == null)? "Default":
                                                          annotSet.getName();
        // check the Start node
        if (offests2NodeId.containsKey(annot.getStartNode().getOffset())){
             assertEquals("Found two different node IDs for the same offset( "+
             annot.getStartNode().getOffset()+ " ).\n" +
             "START NODE is buggy for annotation(" + annot +
             ") from annotation set " + annotSetName + " of GATE document :" +
             doc.getSourceUrl(),
             annot.getStartNode().getId(),
             (Integer) offests2NodeId.get(annot.getStartNode().getOffset()));
        }// End if
        // Check the End node
        if (offests2NodeId.containsKey(annot.getEndNode().getOffset())){
             assertEquals("Found two different node IDs for the same offset("+
             annot.getEndNode().getOffset()+ ").\n" +
             "END NODE is buggy for annotation(" + annot+ ") from annotation"+
             " set " + annotSetName +" of GATE document :" + doc.getSourceUrl(),
             annot.getEndNode().getId(),
             (Integer) offests2NodeId.get(annot.getEndNode().getOffset()));
        }// End if
        offests2NodeId.put(annot.getStartNode().getOffset(),
                                                  annot.getStartNode().getId());
        offests2NodeId.put(annot.getEndNode().getOffset(),
                                                    annot.getEndNode().getId());
    }// End while
  }//verifyNodeIdConsistency();

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestDocument.class);
  } // suite

} // class TestDocument
