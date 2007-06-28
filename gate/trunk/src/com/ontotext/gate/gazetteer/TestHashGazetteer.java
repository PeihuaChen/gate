package com.ontotext.gate.gazetteer;
/*
 *  HashGazetteer.java
 *
 *  OntoText Lab.
 *
 *  borislav popov , 09/11/2001
 *
 *  $Id$
 */

import java.util.*;
import java.io.*;
import java.net.*;
import java.beans.*;
import java.lang.reflect.*;
import junit.framework.*;

import gate.*;
import gate.util.*;
import gate.creole.*;
import gate.corpora.TestDocument;

/**
 * Tests the HashGazetteer.
 */
public class TestHashGazetteer extends TestCase {

  private static final String GAZ_AS = "GazetteerAS";
  public TestHashGazetteer(String name) {
    super(name);
  }

  /** Fixture set up */
  public void setUp() throws Exception {
  }

  public void tearDown() throws Exception {
  } // tearDown

  /** Test the default tokeniser */
  public void testHashGazetteer() throws Exception {
    //get a document
    Document doc = Factory.newDocument(
      new URL(TestDocument.getTestServerName() + "tests/doc0.html")
    );

    //create a default gazetteer
    FeatureMap params = Factory.newFeatureMap();
    HashGazetteer gaz = (HashGazetteer) Factory.createResource(
                          "com.ontotext.gate.gazetteer.HashGazetteer", params);

    //runtime stuff
    gaz.setDocument(doc);
    gaz.setAnnotationSetName(GAZ_AS);
    gaz.execute();

//    dumpAnnotationSet(doc.getAnnotations(Gaz_AS));

    assertTrue("the Annotation set resulting of the execution of the OntoText "
            +"Natural Gazetteer is empty."
            ,!doc.getAnnotations(GAZ_AS).isEmpty());
    //check whether the annotations are as expected


//    assertTrue("Found in "+ doc.getSourceUrl().getFile()+ " "+
//      doc.getAnnotations(GAZ_AS).size() +
//      " Lookup annotations, instead of the expected 53.",
//      doc.getAnnotations(GAZ_AS).size()== 53);

/*very complex compare */
//    assertTrue("the Annotation set resulting from the OntoText Natural Gazetteer "
//        +"is not exactly the same as expected. Possible reasons: change in the test file "
//        +"doc0.html or malfunctioning of the gazetteer"
//        ,EqualAnnotationSets(doc.getAnnotations(GAZ_AS)));

  } // testHashGazetteer();

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestHashGazetteer.class);
  } // suite

  public static void main(String[] args) {
    try{
      Gate.init();
      TestHashGazetteer testGaz = new TestHashGazetteer("");
      testGaz.setUp();
      testGaz.testHashGazetteer();
      testGaz.tearDown();
    } catch(Exception e) {
      e.printStackTrace();
    }
  } // main


  /** dumps the annotation set to system ouput
   * @param marks an annotation set
   */
  private void dumpAnnotationSet(AnnotationSet marks) {
    if (marks != null) {
        Iterator<Annotation> iter = marks.iterator();
        while(iter.hasNext()) {
          Annotation lookup = iter.next();
          FeatureMap lookFeats = lookup.getFeatures();
          String majorStr = (String) lookFeats.get("majorType");
          String minorStr = (String) lookFeats.get("minorType");
          String position = " "+lookup.getStartNode().getOffset()+"-"+ lookup.getEndNode().getOffset();
          System.out.println(position+":"+majorStr + "." + minorStr + Strings.getNl());
        }
    } //if
  } // void dumpAnnotationSet(AnnotationSet set)

  /** Tests whether the annotation set has the same elements
   *  as statet in DESIRED_ANNOTATIONS
   *  @param marks an annotation set
   *  @return true if they match, false otherwise.
   */
  private boolean EqualAnnotationSets(AnnotationSet marks) {
    boolean areEqual = true;
    String currentMark = null;
    int index = 0;

    areEqual = areEqual && (marks.size() == DESIRED_ANNOTATIONS.length);

    if (marks != null) {
      Iterator<Annotation> iter = marks.iterator();

      while(iter.hasNext() & areEqual) {
        Annotation lookup = iter.next();
        FeatureMap lookFeats = lookup.getFeatures();
        String majorStr = (String) lookFeats.get("majorType");
        String minorStr = (String) lookFeats.get("minorType");
        String position = ""+lookup.getStartNode().getOffset()+"-"+ lookup.getEndNode().getOffset();

        currentMark = position+":"+majorStr + "." + minorStr;
        areEqual = areEqual && (currentMark.equals(DESIRED_ANNOTATIONS[index]));
        index++;
      }
    } else {
      areEqual = false;
    } // else


    return areEqual;
  } //  boolean testGazAnnotationSet(AnnotationSet marks) {

  private static String [] DESIRED_ANNOTATIONS =
  {
    "1067-1072:date_unit.null",

    "1033-1038:person_first.male",

    "1029-1032:title.male",

    "1014-1023:jobtitle.null",

    "1008-1013:jobtitle.null",

    "995-1003:jobtitle.null",

    "846-853:number.null",

    "814-822:date.month",

    "799-802:title.male",

    "765-768:org_ending.null",

    "765-768:cdg.null",

    "753-764:org_key.null",

    "738-741:org_ending.null",

    "738-741:cdg.null",

    "723-737:org_key.null",

    "713-722:organization.company",

    "696-701:cdg.null",

    "677-686:organization.company",

    "664-673:jobtitle.null",

    "658-663:jobtitle.null",

    "645-653:jobtitle.null",

    "636-641:date_unit.null",

    "614-616:stop.null",

    "603-613:organization.company",

    "582-587:cdg.null",

    "555-576:organization.company",

    "546-549:org_ending.null",

    "546-549:cdg.null",

    "529-538:jobtitle.null",

    "523-528:jobtitle.null",

    "510-518:jobtitle.null",

    "484-487:title.male",

    "465-473:jobtitle.null",

    "424-429:person_first.male",

    "414-420:person_first.male",

    "394-399:date_unit.null",

    "379-382:title.male",

    "350-373:jobtitle.null",

    "337-345:jobtitle.null",

    "320-325:person_first.male",

    "295-298:org_ending.null",

    "295-298:cdg.null",

    "274-277:location.province",

    "265-272:location.city",

    "182-189:cdg.null",

    "161-165:person_first.female",

    "100-115:title.civilian",

    "100-115:jobtitle.null",

    "87-95:title.civilian"
  }; // private static String [] DESIRED_ANNOTATIONS
} // TestHashGazetteer
