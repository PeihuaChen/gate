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
          //Gate.getUrl("tests/annotDiff/KeyDocument.xml")
          new URL("file:///Z:/gate2/src/gate/resources/gate.ac.uk/tests/annotDiff/keyDocument.xml")
       );
    gate.DocumentFormat keyDocFormat = gate.DocumentFormat.getDocumentFormat(
      keyDocument, keyDocument.getSourceUrl()
    );
    keyDocFormat.unpackMarkup (keyDocument,"DocumentContent");
    // Load the xml Response Document and unpack it
    gate.Document responseDocument =
        gate.Factory.newDocument(
            //Gate.getUrl("tests/annotDiff/ResponseDocument.xml")
            new URL("file:///Z:/gate2/src/gate/resources/gate.ac.uk/tests/annotDiff/responseDocument.xml")
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

    AnnotationDiff annotDiff = (AnnotationDiff)
      Factory.createResource("gate.annotation.AnnotationDiff",parameters);

    JFrame jFrame = new JFrame("AnnotationDiff GUI");
    JScrollPane tableScroll = new JScrollPane();
    tableScroll.getViewport().add(annotDiff.getGUI(),null);
    jFrame.getContentPane().add(tableScroll, BorderLayout.CENTER);
    jFrame.setSize(500,300);
    jFrame.setVisible(true);
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
