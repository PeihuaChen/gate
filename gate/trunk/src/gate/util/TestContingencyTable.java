package gate.util;

import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.Gate;

import java.util.ArrayList;
import java.net.URL;
import java.net.URI;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestContingencyTable extends TestCase{

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestContingencyTable.class);
  } // suite

  
  @Override
  protected void setUp() throws Exception {
    // TODO Auto-generated method stub
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    // TODO Auto-generated method stub
    super.tearDown();
  }
 
  public void test(){
    String type = "sent";
    String feature = "Op";
        
    Document doc1 = null;
    Document doc2 = null;
    Document doc3 = null;
    Document doc4 = null;
        
    try {
      Gate.init();
          
      URI uri1 = new URI("http://gate.ac.uk/tests/iaa/beijing-opera.xml");
      URI uri2 = new URI("http://gate.ac.uk/tests/iaa/beijing-opera.xml");
      URI uri3 = new URI("http://gate.ac.uk/tests/iaa/in-outlook-09-aug-2001.xml");
      URI uri4 = new URI("http://gate.ac.uk/tests/iaa/in-outlook-09-aug-2001.xml");
      
      doc1 = Factory.newDocument(uri1.toURL());
      doc2 = Factory.newDocument(uri2.toURL());
      doc3 = Factory.newDocument(uri3.toURL());
      doc4 = Factory.newDocument(uri4.toURL());
        
    } catch (Exception e) {
      e.printStackTrace();
    }
    
    if(doc1!=null && doc2!=null && doc3!=null && doc4!=null){
      AnnotationSet as1 = doc1.getAnnotations("ann1");
      AnnotationSet as2 = doc2.getAnnotations("ann2");

      ContingencyTable myContingencyTable1 = new ContingencyTable(as1, as2, type, feature);
      assertEquals(myContingencyTable1.getObservedAgreement(), new Float(0.7777778).floatValue());
      assertEquals(myContingencyTable1.getKappaCohen(), new Float(0.6086957).floatValue());
      assertEquals(myContingencyTable1.getKappaPi(), new Float(0.59550565).floatValue());
      
      AnnotationSet as3 = doc3.getAnnotations("ann1");
      AnnotationSet as4 = doc4.getAnnotations("ann2");
       
      ContingencyTable myContingencyTable2 = new ContingencyTable(as3, as4, type, feature);
      assertEquals(myContingencyTable2.getObservedAgreement(), new Float(0.96875).floatValue());
      assertEquals(myContingencyTable2.getKappaCohen(), new Float(0.3263158).floatValue());
      assertEquals(myContingencyTable2.getKappaPi(), new Float(0.3227513).floatValue());
       
      ArrayList<ContingencyTable> tablesList = new ArrayList<ContingencyTable>();
      tablesList.add(myContingencyTable1);
      tablesList.add(myContingencyTable2);
      ContingencyTable myNewContingencyTable = new ContingencyTable(tablesList);
      assertEquals(myNewContingencyTable.getObservedAgreement(), new Float(0.94520545).floatValue());
      assertEquals(myNewContingencyTable.getKappaCohen(), new Float(0.7784521).floatValue());
      assertEquals(myNewContingencyTable.getKappaPi(), new Float(0.7778622).floatValue());
       
    } else {
      System.out.println("Failed to create docs from URLs.");
    }
  }
}
