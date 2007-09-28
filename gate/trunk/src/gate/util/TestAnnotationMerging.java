package gate.util;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.Gate;
import gate.GateConstants;
import java.util.HashMap;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestAnnotationMerging extends TestCase {
  /** The id of test case. */
  int caseN;

  /** Construction */
  public TestAnnotationMerging(String name) {
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
    return new TestSuite(TestAnnotationMerging.class);
  } // suite

  private Document loadDocument(String path, String name) throws Exception {
    Document doc = Factory.newDocument(Gate.getUrl(path), "UTF-8");
    doc.setName(name);
    return doc;
  }

  /** The test the AnnotationMerging. */
  public void testAnnotationMerging() throws Exception {

    Boolean savedSpaceSetting = Gate.getUserConfig().getBoolean(
            GateConstants.DOCUMENT_ADD_SPACE_ON_UNPACK_FEATURE_NAME);
    Gate.getUserConfig().put(
            GateConstants.DOCUMENT_ADD_SPACE_ON_UNPACK_FEATURE_NAME,
            Boolean.FALSE);
    try {

      //Gate.setGateHome(new File("C:\\svn\\gate"));
      //Gate.setUserConfigFile(new File("C:\\svn\\gate.xml"));
      //Gate.init();
      // Load the documents into a corpus
      Corpus data = Factory.newCorpus("data");
  
      int numDocs = 1; // Number of documents
      int numJudges = 3; // number of judges
      // Put the annotated document into a matrix for IAA
      String nameAnnSet = "Original markups";
      String nameAnnType = "";
      String nameAnnFeat = "";

      // Use the dataset of one document and three annotators
      data.add(loadDocument("tests/iaa/small/ann1.xml", "ann1.xml"));
      data.add(loadDocument("tests/iaa/small/ann2.xml", "ann2.xml"));
      data.add(loadDocument("tests/iaa/small/ann3.xml", "ann3.xml"));
      //ExtensionFileFilter fileFilter = new ExtensionFileFilter();
      //fileFilter.addExtension("xml");
      //data.populate(new File("C:\\yaoyong_h\\work\\iaa\\data\\smallData").toURL(), fileFilter, "UTF-8", false);
      
      numDocs = 1; // Number of documents
      numJudges = 3; // number of judges
      boolean isUsingMajority=false;
      nameAnnType = "SENT";
      nameAnnFeat = "senOp";
      caseN = 1;
      isUsingMajority=true;
      testWithfeat(numDocs, numJudges, nameAnnSet, nameAnnType, nameAnnFeat,
              data, isUsingMajority);
      
      caseN = 2;
      isUsingMajority=false;
      testWithfeat(numDocs, numJudges, nameAnnSet, nameAnnType, nameAnnFeat,
              data, isUsingMajority);

      
      nameAnnType = "OPINION_SRC";
      nameAnnFeat = "type";
      caseN = 3;
      isUsingMajority=true;
      testWithfeat(numDocs, numJudges, nameAnnSet, nameAnnType, nameAnnFeat,
              data, isUsingMajority);
      
      caseN = 4;
      isUsingMajority=false;
      testWithfeat(numDocs, numJudges, nameAnnSet, nameAnnType, nameAnnFeat,
              data, isUsingMajority);
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
          String nameAnnType, String nameAnnFeat, Corpus data, boolean isUsingMajority) {
    
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
    //Annotation merging
    boolean isTheSameInstances = true;
    for(int i=0; i<annArr2.length; ++i)
      if(!IaaCalculation.isSameInstancesForAnnotators(annArr2[i], 1)) {
        isTheSameInstances = false;
        break;
      }
    HashMap<Annotation,String>mergeInfor = new HashMap<Annotation,String>();
    if(isUsingMajority)
      AnnotationMerging.mergeAnnogationMajority(annArr2[0], nameAnnFeat, mergeInfor);
    else AnnotationMerging.mergeAnnogation(annArr2[0], nameAnnFeat, mergeInfor, 3, isTheSameInstances);
    int numAnns=0;
    if(isTheSameInstances) {
      for(Annotation ann:mergeInfor.keySet()) {
        if(ann.getFeatures().get(nameAnnFeat) != null)
          ++numAnns;
         
      }
    } else {
      numAnns = mergeInfor.size();
    }
    checkNumbers(numAnns);
  }

  /** Check the numbers. */
  private void checkNumbers(int numAnns) {
    switch(caseN) {
      case 1:
        assertEquals(numAnns, 14);
        break;
      case 2:
        assertEquals(numAnns, 5);
        break;
      case 3:
        assertEquals(numAnns, 6);
        break;
      case 4:
        assertEquals(numAnns, 6);
        break;
      default:
        System.out.println("The test case " + caseN + " is not defined yet.");
    }
  }

}
