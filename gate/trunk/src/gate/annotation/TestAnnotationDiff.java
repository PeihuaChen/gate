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
    parameters.put("xmlFileUrl", resData.getXmlFileUrl());

    AnnotationSchema annotSchema = (AnnotationSchema)
      Factory.createResource("gate.creole.AnnotationSchema", parameters);

    URL url = Gate.getUrl("tests/xml/POSSchema.xml");
    annotSchema.fromXSchema(url);

    // Load the xml Key Document and unpack it
    gate.Document keyDocument =
       gate.Factory.newDocument(
          Gate.getUrl("tests/annotDiff/KeyDocument.xml")
       );
    gate.DocumentFormat keyDocFormat = gate.DocumentFormat.getDocumentFormat(
      keyDocument, keyDocument.getSourceUrl()
    );
    keyDocFormat.unpackMarkup (keyDocument,"DocumentContent");
    // Load the xml Response Document and unpack it
    gate.Document responseDocument =
        gate.Factory.newDocument(
            Gate.getUrl("tests/annotDiff/ResponseDocument.xml")
        );
    gate.DocumentFormat responseDocFormat =
        gate.DocumentFormat.getDocumentFormat(
                responseDocument, responseDocument.getSourceUrl()
        );
    responseDocFormat.unpackMarkup (responseDocument,"DocumentContent");

    // Create an AnnotationDiff object.
    parameters = Factory.newFeatureMap();
    parameters.put("keyDocument",keyDocument);
    parameters.put("responseDocument",responseDocument);
    parameters.put("annotationSchema",annotSchema);

    // Create Annotation Diff visual resource
    AnnotationDiff annotDiff = (AnnotationDiff)
          Factory.createResource("gate.annotation.AnnotationDiff",parameters);

    assert("Precision changed.That's because of the key/response document or" +
            " code implementation!",
            new Double(0.90).equals(annotDiff.getPrecision()));
    assert("Recall changed.That's because of the key/response document or" +
            " code implementation!",
            new Double(0.8181818181818182).equals(annotDiff.getRecall()));

    // Display the component
/*
    JFrame jFrame = new JFrame("AnnotationDiff GUI");
    jFrame.getContentPane().add(annotDiff.getGUI(), BorderLayout.CENTER);
    jFrame.pack();
    jFrame.setVisible(true);
*/

  } // testDiff()

  public void testAnnotationAreEquals() throws Exception {
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

    Annotation annot1 = new AnnotationImpl(new Integer(1),
                                           node1,
                                           node2,
                                           "word",
                                           null);
    Annotation annot2 = new AnnotationImpl (new Integer(2),
                                            node2,
                                            node6,
                                            "sentence",
                                            null);
    Annotation annot3 = new AnnotationImpl (new Integer(3),
                                            node5,
                                            node6,
                                            "sentence",
                                            null);

   AnnotationDiff annotDiff = new AnnotationDiff();

   // types and offsets not equals
   assert("Those annotations must not be equal!",
                                !annotDiff.areEqual(annot1,annot2));
   // Those two must be equals
   assert("Those annotations must be equal!",annotDiff.areEqual(annot2,annot3));

   annot2.setFeatures(fm1);
   annot3.setFeatures(fm2);
   assert("Those annotations must be equal!",annotDiff.areEqual(annot2,annot3));

   annot2.setFeatures(fm1);
   annot3.setFeatures(fm3);
   assert("Those annotations must NOT be equal!",
                          !annotDiff.areEqual(annot2,annot3));

   annot3.setFeatures(null);
   assert("Those annotations must NOT be equal!",
                      !annotDiff.areEqual(annot2,annot3));

    annot3.setFeatures(fm4);
    assert("Those annotations must not be equal!",
                                    !annotDiff.areEqual(annot2,annot3));
  }// testAnnotationEquals

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestAnnotationDiff.class);
  } // suite

  public static void main(String[] args) {
    try{
      Gate.init();
      TestAnnotationDiff testAnnotDiff = new TestAnnotationDiff("");
      testAnnotDiff.testDiff();
      testAnnotDiff.testAnnotationAreEquals();
    }catch(Exception e){
      e.printStackTrace();
    }
  }// main

} // class TestAnnotationDiff
