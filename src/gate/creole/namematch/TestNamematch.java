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

  /** test the namematcher */
  public void testNamematch() throws Exception{
    //get a document
    Document doc = Factory.newDocument(
      new URL(TestDocument.getTestServerName() + "tests/matcher.txt"));
    //create a namematcher
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

      annotSetAS.add(new Long(257), new Long(274), "TTTT", fm);

      fm = Factory.newFeatureMap();
      fm.put("token","person");
      fm.put("country","USA");

      annotSetAS.add(new Long(275), new Long(293), "TTTT", fm);

      fm = Factory.newFeatureMap();
      fm.put("token","org");
      fm.put("country","USA");

      annotSetAS.add(new Long(294), new Long(306), "TTTT", fm);

      fm = Factory.newFeatureMap();
      fm.put("token","org");
      fm.put("country","USA");

      annotSetAS.add(new Long(307), new Long(326), "TTTT", fm);

      fm = Factory.newFeatureMap();
      fm.put("token","org");
      fm.put("country","USA");

      annotSetAS.add(new Long(327), new Long(338), "TTTT", fm);

      fm = Factory.newFeatureMap();
      fm.put("token","org");
      fm.put("country","USA");

      annotSetAS.add(new Long(339), new Long(349), "TTTT", fm);

      fm = Factory.newFeatureMap();
      fm.put("token","org");
      fm.put("country","USA");

      annotSetAS.add(new Long(350), new Long(365), "TTTT", fm);

      fm = Factory.newFeatureMap();
      fm.put("token","org");
      fm.put("country","USA");

      annotSetAS.add(new Long(386), new Long(389), "TTTT", fm);

      fm = Factory.newFeatureMap();
      fm.put("token","org");
      fm.put("country","USA");

      annotSetAS.add(new Long(390), new Long(394), "TTTT", fm);

      fm = Factory.newFeatureMap();
      fm.put("token","org");
      fm.put("country","USA");

      annotSetAS.add(new Long(395), new Long(422), "TTTT", fm);

    } catch (InvalidOffsetException ioe) {
      ioe.printStackTrace();
    }
    namematch.setDocument(doc);
    namematch.setAnnotationSet(annotSetAS);
    namematch.setType("TTTT");
    namematch.setTypeAttr("token");

    // uses intern cdg list or extern cdg list
    namematch.setIntCdgList(true);
    // uses inter lists or extern lists
    namematch.setIntExtLists(false);
    namematch.run();
    namematch.check();

    // the vector with all the matches from the document
    Vector matches = namematch.getMatchesDocument();
    assert(matches.toString().equals("[[0, 3, 5], [2, 4], [6, 8], [7, 9]]"));

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
