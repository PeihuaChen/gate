/*
 *  TestNamematch.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Oana Hamza, 25/January/01
 *
 *  $Id$
 */

package gate.creole.namematch;

import java.util.*;
import java.io.*;
import junit.framework.*;
import java.net.*;

import gate.*;
import gate.util.*;
import gate.corpora.*;

public class TestNamematch extends TestCase
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public TestNamematch(String name) { super(name); }

  /** Fixture set up */
  public void setUp() {
  } // setUp

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestNamematch.class);
  } // suite

  /** test the namatcher */
  public void testNamematch() throws Exception{
    //get a document
    Document doc = Factory.newDocument(
      new URL(TestDocument.getTestServerName() + "tests/matcher.txt"));
    //create a default tokeniser
    FeatureMap params = Factory.newFeatureMap();
    Namematch namematch = (Namematch) Factory.createResource(
                          "gate.creole.namematch.Namematch", params);

    AnnotationSet annotSetAS = doc.getAnnotations("AnnotationSetAS");

    Integer newId;
    FeatureMap fm = Factory.newFeatureMap();
    try {
      fm = Factory.newFeatureMap();
      fm.put("token","org");
      fm.put("country","USA");

      annotSetAS.add(new Long(255), new Long(272), "TTTT", fm);

      fm = Factory.newFeatureMap();
      fm.put("token","person");
      fm.put("country","USA");

      annotSetAS.add(new Long(273), new Long(291), "TTTT", fm);

      fm = Factory.newFeatureMap();
      fm.put("token","org");
      fm.put("country","USA");

      annotSetAS.add(new Long(292), new Long(304), "TTTT", fm);

      fm = Factory.newFeatureMap();
      fm.put("token","org");
      fm.put("country","USA");

      annotSetAS.add(new Long(305), new Long(324), "TTTT", fm);

      fm = Factory.newFeatureMap();
      fm.put("token","org");
      fm.put("country","USA");

      annotSetAS.add(new Long(325), new Long(335), "TTTT", fm);

      fm = Factory.newFeatureMap();
      fm.put("token","org");
      fm.put("country","USA");

      annotSetAS.add(new Long(336), new Long(346), "TTTT", fm);

      fm = Factory.newFeatureMap();
      fm.put("token","org");
      fm.put("country","USA");

      annotSetAS.add(new Long(347), new Long(362), "TTTT", fm);

      fm = Factory.newFeatureMap();
      fm.put("token","org");
      fm.put("country","USA");

      annotSetAS.add(new Long(383), new Long(386), "TTTT", fm);

      fm = Factory.newFeatureMap();
      fm.put("token","org");
      fm.put("country","USA");

      annotSetAS.add(new Long(387), new Long(391), "TTTT", fm);

      fm = Factory.newFeatureMap();
      fm.put("token","org");
      fm.put("country","USA");

      annotSetAS.add(new Long(392), new Long(419), "TTTT", fm);

    } catch (InvalidOffsetException ioe) {
      ioe.printStackTrace();
    }
    namematch.setDocument(doc);
    namematch.setAnnotationSet(annotSetAS);
    namematch.setType("TTTT");
    namematch.setTypeAttr("token");
    namematch.setIntCdgList(false);
    namematch.run();
    namematch.check();

    AnnotationSet annotSet = doc.getAnnotations("AnnotationSetAS");
    FeatureMap fm1;
    Annotation annot;
    Iterator i;

    // the annotation with Id 0
    annot = annotSet.get(new Integer(0));
    fm1 = annot.getFeatures();
    i = fm1.keySet().iterator();
    while (i.hasNext()) {
      String type = (String) i.next();
      if (type == "matches") {
        Vector vector = (Vector)fm1.get(type);
        for (int j=0; j< vector.size(); j++) {
          String value = (String)vector.get(j);
          assert(value.equals("3")||value.equals("5"));
        } // for
      } // if
    } // while

    // the annotation with Id 2
    annot = annotSet.get(new Integer(2));
    fm1 = annot.getFeatures();
    i = fm1.keySet().iterator();
    while (i.hasNext()) {
      String type = (String) i.next();
      if (type == "matches") {
        Vector vector = (Vector)fm1.get(type);
        for (int j=0; j< vector.size(); j++) {
          String value = (String)vector.get(j);
          assert(value.equals("4"));
        }// for
      } // if
    } // while

    // the annotation with Id 3
    annot = annotSet.get(new Integer(3));
    fm1 = annot.getFeatures();
    i = fm1.keySet().iterator();
    while (i.hasNext()) {
      String type = (String) i.next();
      if (type == "matches") {
        Vector vector = (Vector)fm1.get(type);
        for (int j=0; j< vector.size(); j++) {
          String value = (String)vector.get(j);
          assert(value.equals("0")||value.equals("5"));
        } // for
      } // if
    } // while

    // the annotation with Id 4
    annot = annotSet.get(new Integer(4));
    fm1 = annot.getFeatures();
    i = fm1.keySet().iterator();
    while (i.hasNext()) {
      String type = (String) i.next();
      if (type == "matches") {
        Vector vector = (Vector)fm1.get(type);
        for (int j=0; j< vector.size(); j++) {
          String value = (String)vector.get(j);
          assert(value.equals("2"));
        } // for
      } // if
    } // while

    // the annotation with Id 5
    annot = annotSet.get(new Integer(5));
    fm1 = annot.getFeatures();
    i = fm1.keySet().iterator();
    while (i.hasNext()) {
      String type = (String) i.next();
      if (type == "matches") {
        Vector vector = (Vector)fm1.get(type);
        for (int j=0; j< vector.size(); j++) {
          String value = (String)vector.get(j);
          assert(value.equals("0")||value.equals("3"));
        } // for
      } // if
    } // while

    // the annotation with Id 6
    annot = annotSet.get(new Integer(6));
    fm1 = annot.getFeatures();
    i = fm1.keySet().iterator();
    while (i.hasNext()) {
      String type = (String) i.next();
      if (type == "matches") {
        Vector vector = (Vector)fm1.get(type);
        for (int j=0; j< vector.size(); j++) {
          String value = (String)vector.get(j);
          assert(value.equals("8"));
        } // for
      }// if
    } // while

    // the annotation with Id 7
    annot = annotSet.get(new Integer(7));
    fm1 = annot.getFeatures();
    i = fm1.keySet().iterator();
    while (i.hasNext()) {
      String type = (String) i.next();
      if (type == "matches") {
        Vector vector = (Vector)fm1.get(type);
        for (int j=0; j< vector.size(); j++) {
          String value = (String)vector.get(j);
          assert(value.equals("9"));
        } // for
      }// if
    } // while

    // the annotation with Id 9
    annot = annotSet.get(new Integer(9));
    fm1 = annot.getFeatures();
    i = fm1.keySet().iterator();
    while (i.hasNext()) {
      String type = (String) i.next();
      if (type == "matches") {
        Vector vector = (Vector)fm1.get(type);
        for (int j=0; j< vector.size(); j++) {
          String value = (String)vector.get(j);
          assert(value.equals("7"));
        } // for
      }// if
    } // while
  }
  public static void main(String[] args) {

    TestNamematch test = new TestNamematch("");
    try {
      Gate.init();
      test.testNamematch();
    } catch (Exception e) {e.printStackTrace();}
  }

} // class TestNamematch
