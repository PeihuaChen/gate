/*
 *  TestAnnotationDiff.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 11/Feb/2000
 *
 *  Cristian URSU, 06/Nov/2000
 *
 *  $Id$
 */

package gate.annotation;

import java.util.*;
import java.io.*;
import java.net.*;
import junit.framework.*;
import javax.swing.*;
import java.awt.*;

import gate.util.*;
import gate.gui.*;
import gate.creole.*;
import gate.*;


/**
  */
public class TestAnnotationDiff extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** The Precision value (see NLP Information Extraction)*/
  private Double precision = null;

  /** The Recall value (see NLP Information Extraction)*/
  private Double recall = null;


  /** Construction */
  public TestAnnotationDiff(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {

  } // setUp

  /** A test */
  public void testDiff() throws Exception {
    // Create a AnnotationSchema object from URL.
    ResourceData resData = (ResourceData)
      Gate.getCreoleRegister().get("gate.creole.AnnotationSchema");

    FeatureMap parameters = Factory.newFeatureMap();
    parameters.put("xmlFileUrl", Gate.getUrl("tests/xml/POSSchema.xml"));

    AnnotationSchema annotationSchema = (AnnotationSchema)
      Factory.createResource("gate.creole.AnnotationSchema", parameters);


    // Load the xml Key Document and unpack it
    gate.Document keyDocument =
       gate.Factory.newDocument(
          Gate.getUrl("tests/annotDiff/KeyDocument.xml")
//            new URL("file:///Z:/testAnnotDiff/key1.xml")
       );

    // Load the xml Response Document and unpack it
    gate.Document responseDocument =
        gate.Factory.newDocument(
            Gate.getUrl("tests/annotDiff/ResponseDocument.xml")
//            new URL("file:///Z:/testAnnotDiff/response1.xml")
        );

    AnnotationSet keyAnnotSet = null;
    AnnotationSet responseAnnotSet = null;
    Set diffSet  = null;
    // Get the key AnnotationSet from the keyDocument
    keyAnnotSet = keyDocument.getAnnotations("Original markups").get(
                              annotationSchema.getAnnotationName());
    // Get the response AnnotationSet from the resonseDocument
    responseAnnotSet = responseDocument.getAnnotations("Original markups").get(
                                        annotationSchema.getAnnotationName());

//*
    // Create an AnnotationDiff object.
    // Creole.xml must contain a entry for AnnotationDiff.
    // If not, you will get an exception (couldn't configure resource metadata)

    parameters = Factory.newFeatureMap();
    parameters.put("keyDocument",keyDocument);
    parameters.put("responseDocument",responseDocument);
    parameters.put("annotationSchema",annotationSchema);
    parameters.put("keyAnnotationSetName","Original markups");
    parameters.put("responseAnnotationSetName","Original markups");

    // Create Annotation Diff visual resource
    AnnotationDiff annotDiff = (AnnotationDiff)
          Factory.createResource("gate.annotation.AnnotationDiff",parameters);

//*/
//*
    assert("Precision strict changed.That's because of the key/response" +
            " document or" + " code implementation!",
                        0.16666666666666666 == annotDiff.getPrecisionStrict());
    assert("Recall strict changed.That's because of the key/response" +
    " document or" + " code implementation!",
                    0.18181818181818182 == annotDiff.getRecallStrict());


//*/
    // Display the component
/*
    JFrame jFrame = new JFrame("AnnotationDiff GUI");
    jFrame.getContentPane().add(annotDiff, BorderLayout.CENTER);
    jFrame.pack();
    jFrame.setVisible(true);
*/

  } // testDiff()



  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestAnnotationDiff.class);
  } // suite

  public static void main(String[] args) {
    try{
      Gate.init();
      TestAnnotationDiff testAnnotDiff = new TestAnnotationDiff("");

      testAnnotDiff.testDiff();
    }catch(Exception e){
      e.printStackTrace();
    }
  }// main

} // class TestAnnotationDiff
