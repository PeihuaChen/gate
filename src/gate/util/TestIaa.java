/*
 *
 *  Copyright (c) 1998-2007, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  $Id$
 */

package gate.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import junit.framework.*;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.annotation.AnnotationSetImpl;
import gate.gui.MainFrame;
import gate.GateConstants;

public class TestIaa extends TestCase {
  /** The id of test case. */
  int caseN;

  /** Construction */
  public TestIaa(String name) {
    super(name);
  }

  /** Fixture set up */
  public void setUp() {
  } // setUp

  /**
   * Put things back as they should be after running tests.
   */
  public void tearDown() throws Exception {
  } // tearDown

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestIaa.class);
  } // suite

  private Document loadDocument(String path, String name) throws Exception {
    Document doc = Factory.newDocument(Gate.getUrl(path), "UTF-8");
    doc.setName(name);
    return doc;
  }

  /** The test the IAA. */
  public void testIaa() throws Exception {

    Boolean savedSpaceSetting = Gate.getUserConfig().getBoolean(
            GateConstants.DOCUMENT_ADD_SPACE_ON_UNPACK_FEATURE_NAME);
    Gate.getUserConfig().put(
            GateConstants.DOCUMENT_ADD_SPACE_ON_UNPACK_FEATURE_NAME,
            Boolean.FALSE);
    try {

      // Load the documents into a corpus
      Corpus data = Factory.newCorpus("data");
      data
              .add(loadDocument("tests/iaa/twodocs/doc1-ann1.xml",
                      "doc1-ann1.xml"));
      data
              .add(loadDocument("tests/iaa/twodocs/doc1-ann3.xml",
                      "doc1-ann3.xml"));
      data
              .add(loadDocument("tests/iaa/twodocs/doc2-ann1.xml",
                      "doc2-ann1.xml"));
      data
              .add(loadDocument("tests/iaa/twodocs/doc2-ann2.xml",
                      "doc2-ann2.xml"));
      data
              .add(loadDocument("tests/iaa/twodocs/doc2-ann3.xml",
                      "doc2-ann3.xml"));

      boolean isUsingLabel = true;

      int numDocs = 2; // Number of documents
      int numJudges = 3; // number of judges
      // Put the annotated document into a matrix for IAA
      String nameAnnSet = "Original markups";
      String nameAnnType = "OPINION_SRC";
      String nameAnnFeat = "";
      // testNofeat(nameAnnSet, nameAnnType, nameAnnFeat, annArr2,
      // data);
      // Test with feature
      nameAnnSet = "Original markups";
      // nameAnnType = "OPINION_SRC";
      // nameAnnFeat = "type";
      caseN = 1;
      nameAnnType = "SENT";
      nameAnnFeat = "senOp";
      isUsingLabel = true;
      testWithfeat(numDocs, numJudges, nameAnnSet, nameAnnType, nameAnnFeat,
              data, isUsingLabel);

      caseN = 2;
      nameAnnType = "OPINION_SRC";
      nameAnnFeat = "type";
      isUsingLabel = false;
      testWithfeat(numDocs, numJudges, nameAnnSet, nameAnnType, nameAnnFeat,
              data, isUsingLabel);

      // Use another dataset
      data.clear();
      data.add(loadDocument("tests/iaa/small/ann1.xml", "ann1.xml"));
      data.add(loadDocument("tests/iaa/small/ann2.xml", "ann2.xml"));
      data.add(loadDocument("tests/iaa/small/ann3.xml", "ann3.xml"));

      numDocs = 1; // Number of documents
      numJudges = 3; // number of judges
      // Test with feature
      nameAnnSet = "Original markups";
      caseN = 3;
      nameAnnType = "SENT";
      nameAnnFeat = "senOp";
      isUsingLabel = true;
      testWithfeat(numDocs, numJudges, nameAnnSet, nameAnnType, nameAnnFeat,
              data, isUsingLabel);

      caseN = 4;
      nameAnnType = "OPINION_SRC";
      nameAnnFeat = "type";
      isUsingLabel = true;
      testWithfeat(numDocs, numJudges, nameAnnSet, nameAnnType, nameAnnFeat,
              data, isUsingLabel);
    }
    finally {
      Gate.getUserConfig().put(
              GateConstants.DOCUMENT_ADD_SPACE_ON_UNPACK_FEATURE_NAME,
              savedSpaceSetting);
    }

  }

  private int obtainAnnotatorId(String docName) {
    if(docName.contains("1.xml"))
      return 0;
    else if(docName.contains("2.xml"))
      return 1;
    else if(docName.contains("3.xml")) return 2;
    return -1;
  }

  private String obtainDocName(String docName) {
    return docName.substring(0, docName.indexOf(".xml") - 1);
  }

  /** The actual method for testing. */
  public void testWithfeat(int numDocs0, int numJudges, String nameAnnSet,
          String nameAnnType, String nameAnnFeat, Corpus data,
          boolean isUsingLabel) {
    AnnotationSet[][] annArr2 = new AnnotationSet[numDocs0][numJudges];
    int numDocs = data.size();
    // Get a document list as different annotation in different document
    // copy
    HashMap<String, Integer> docNameList = new HashMap<String, Integer>();
    int k = 0;
    for(int i = 0; i < data.size(); ++i) {
      String docName = obtainDocName(((Document)data.get(i)).getName());
      if(!docNameList.containsKey(docName)) {
        docNameList.put(docName, new Integer(k));
        ++k;
      }
    }

    for(int i = 0; i < numDocs; ++i) {
      Document doc = (Document)data.get(i);
      String docName = obtainDocName(doc.getName());
      int annotatorId = obtainAnnotatorId(doc.getName());
      if(annotatorId < 0 || !docNameList.containsKey(docName)) {
        System.out.println("Warning: annotator id must be bigger than 0!");
      }
      else {
        // Get the annotation
        annArr2[docNameList.get(docName).intValue()][annotatorId] = doc
                .getAnnotations(nameAnnSet).get(nameAnnType);
      }
    }
    // Get the Iaa computation
    // System.out.println("annSet="+nameAnnSet+",
    // annType="+nameAnnType+",
    // annFeat="+nameAnnFeat+".");
    // First collect labels for the feature
    ArrayList<String> labelsSet;
    labelsSet = IaaCalculation.collectLabels(annArr2, nameAnnFeat);
    String[] labelsArr = new String[labelsSet.size()]; // (String
    // [])labelsSet.toArray();
    for(int i = 0; i < labelsSet.size(); ++i) {
      labelsArr[i] = labelsSet.get(i);
    }
    IaaCalculation iaa = null;
    if(isUsingLabel)
      iaa = new IaaCalculation(nameAnnType, nameAnnFeat, labelsArr, annArr2, 0);
    else iaa = new IaaCalculation(nameAnnType, annArr2, 0);

    iaa.pairwiseIaaFmeasure();
    int[] nPwF = new int[4];
    nPwF[0] = (int)Math.ceil((double)iaa.fMeasureOverall.correct);
    nPwF[1] = (int)Math.ceil((double)iaa.fMeasureOverall.partialCor);
    nPwF[2] = (int)Math.ceil((double)iaa.fMeasureOverall.spurious);
    nPwF[3] = (int)Math.ceil((double)iaa.fMeasureOverall.missing);

    boolean isSuitable = true;
    for(int i = 0; i < annArr2.length; ++i)
      if(!IaaCalculation.isSameInstancesForAnnotators(annArr2[i], 0)) {
        isSuitable = false;
        break;
      }

    // Get a reference annotation set by merging all
    AnnotationSet[] refAnnsArr = new AnnotationSet[annArr2.length];
    boolean[] isMerged = new boolean[annArr2.length];
    for(int i = 0; i < annArr2.length; ++i)
      isMerged[i] = false;
    for(int iJ = 0; iJ < data.size(); ++iJ) {
      Document docC = (Document)data.get(iJ);
      String docName = obtainDocName(docC.getName());
      // if(!docNameList.containsKey(docName)) continue;
      int kk = docNameList.get(docName).intValue();
      if(!isMerged[kk]) {
        refAnnsArr[kk] = new AnnotationSetImpl(docC);
        HashMap<Annotation, String> mergeInfor = new HashMap<Annotation, String>();
        IaaCalculation.mergeAnnogation(annArr2[kk], nameAnnFeat, mergeInfor, 2);
        isMerged[kk] = true;
        // FeatureMap featM = Factory.newFeatureMap();
        AnnotationSet annsDoc = docC.getAnnotations("mergedAnns");
        for(Annotation ann : mergeInfor.keySet()) {
          refAnnsArr[kk].add(ann);
          // FeatureMap featM = ann.getFeatures();
          FeatureMap featM = Factory.newFeatureMap();
          featM.put(nameAnnFeat, ann.getFeatures().get(nameAnnFeat));
          featM.put("annotators", mergeInfor.get(ann));
          try {
            annsDoc.add(ann.getStartNode().getOffset(), ann.getEndNode()
                    .getOffset(), nameAnnType, featM);
          }
          catch(InvalidOffsetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      }
    }
    iaa.allwayIaaFmeasure(refAnnsArr);
    int[] nAwF = new int[4];
    nAwF[0] = (int)Math.ceil((double)iaa.fMeasureOverall.correct);
    nAwF[1] = (int)Math.ceil((double)iaa.fMeasureOverall.partialCor);
    nAwF[2] = (int)Math.ceil((double)iaa.fMeasureOverall.spurious);
    nAwF[3] = (int)Math.ceil((double)iaa.fMeasureOverall.missing);

    // Compute the kappa
    iaa.pairwiseIaaKappa();
    int[] nPwKa = new int[3];
    nPwKa[0] = (int)Math
            .ceil((double)iaa.contingencyOverall.observedAgreement * 100);
    nPwKa[1] = (int)Math.ceil((double)iaa.contingencyOverall.kappaCohen * 100);
    nPwKa[2] = (int)Math.ceil((double)iaa.contingencyOverall.kappaPi * 100);

    iaa.allwayIaaKappa();
    int[] nAwKa = new int[3];
    nAwKa[0] = (int)Math
            .ceil((double)iaa.contingencyOverall.observedAgreement * 100);
    nAwKa[1] = (int)Math.ceil((double)iaa.contingencyOverall.kappaDF * 100);
    nAwKa[2] = (int)Math.ceil((double)iaa.contingencyOverall.kappaSC * 100);

    checkNumbers(nPwF, nAwF, nPwKa, nAwKa, isSuitable);
  }

  /** Check the numbers. */
  private void checkNumbers(int[] nPwF, int[] nAwF, int[] nPwKa, int[] nAwKa,
          boolean isSuitable) {
    switch(caseN) {
      case 1:
        assertEquals(nPwF[0], 13);
        assertEquals(nPwF[1], 0);
        assertEquals(nPwF[2], 16);
        assertEquals(nPwF[3], 16);
        assertEquals(nAwF[0], 20);
        assertEquals(nAwF[1], 0);
        assertEquals(nAwF[2], 9);
        assertEquals(nAwF[3], 7);
        assertEquals(nPwKa[0], 39);
        assertEquals(nPwKa[1], 7);
        assertEquals(nPwKa[2], -3);
        assertEquals(nAwKa[0], 19);
        assertEquals(nAwKa[1], -32);
        assertEquals(nAwKa[2], 3);
        assertEquals(isSuitable, false);
        break;
      case 2:
        assertEquals(nPwF[0], 3);
        assertEquals(nPwF[1], 1);
        assertEquals(nPwF[2], 3);
        assertEquals(nPwF[3], 5);
        assertEquals(nAwF[0], 6);
        assertEquals(nAwF[1], 0);
        assertEquals(nAwF[2], 2);
        assertEquals(nAwF[3], 3);
        assertEquals(nPwKa[0], 27);
        assertEquals(nPwKa[1], -21);
        assertEquals(nPwKa[2], -63);
        assertEquals(nAwKa[0], 0);
        assertEquals(nAwKa[1], 0);
        assertEquals(nAwKa[2], -34);
        assertEquals(isSuitable, false);
        break;
      case 3:
        assertEquals(nPwF[0], 8);
        assertEquals(nPwF[1], 0);
        assertEquals(nPwF[2], 6);
        assertEquals(nPwF[3], 6);
        assertEquals(nAwF[0], 11);
        assertEquals(nAwF[1], 0);
        assertEquals(nAwF[2], 3);
        assertEquals(nAwF[3], 3);
        assertEquals(nPwKa[0], 58);
        assertEquals(nPwKa[1], 15);
        assertEquals(nPwKa[2], -7);
        assertEquals(nAwKa[0], 36);
        assertEquals(nAwKa[1], 15);
        assertEquals(nAwKa[2], 4);
        assertEquals(isSuitable, true);
        break;
      case 4:
        assertEquals(nPwF[0], 6);
        assertEquals(nPwF[1], 1);
        assertEquals(nPwF[2], 1);
        assertEquals(nPwF[3], 1);
        assertEquals(nAwF[0], 6);
        assertEquals(nAwF[1], 0);
        assertEquals(nAwF[2], 2);
        assertEquals(nAwF[3], 0);
        assertEquals(nPwKa[0], 100);
        assertEquals(nPwKa[1], 100);
        assertEquals(nPwKa[2], 100);
        assertEquals(nAwKa[0], 75);
        assertEquals(nAwKa[1], 46);
        assertEquals(nAwKa[2], 70);
        assertEquals(isSuitable, false);
        break;
      default:
        System.out.println("The test case " + caseN + " is not defined yet.");
    }
  }

}
