package gate.xml;

/**
 * <p>Title: Gate2</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2000</p>
 * <p>Company: University Of Sheffield</p>
 * @author Niraj Aswani
 * @version 1.0
 */

import junit.framework.*;
import gate.*;
import gate.creole.*;
import gate.corpora.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class TestRepositioningInfo
    extends TestCase {

  public TestRepositioningInfo(String dummy) {
    super(dummy);
  }

  /**
   * This method sets up the parameters for the files to be testes
   * It initialises the Tokenizer and sets up the other parameters for
   * the morph program
   */
  protected void setUp() {

    // initialise the rule file that will be used to find the base word
    //testFile = "gate:/creole/gazeteer/test-inline.xml";
    testFile = TestDocument.getTestServerName() + "tests/test-inline.xml";

    // creating documents
    try {
      FeatureMap params = Factory.newFeatureMap();
      params.put("sourceUrl",new URL(testFile));
      params.put("preserveOriginalContent", new Boolean("true"));
      params.put("collectRepositioningInfo", new Boolean("true"));
      doc = (Document) Factory.createResource("gate.corpora.DocumentImpl",params);
    }
    catch (MalformedURLException murle) {
      fail("Document cannot be created ");
    }
    catch (ResourceInstantiationException rie) {
      fail("Resources cannot be created for the test document");
    }
  }

  /** Fixture tear down - does nothing */
  public void tearDown() throws Exception {
    Factory.deleteResource(doc);
  } // tearDown


  public void testRepositioningInfo() throws Exception {

    // here we need to save the document to the file
      String encoding = ((DocumentImpl)doc).getEncoding();
      File outputFile = File.createTempFile("test-inline1","xml");
      OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(outputFile),encoding);
      writer.write(((gate.Document)doc).toXml(null, true));
      writer.flush();
      writer.close();
      InputStreamReader readerForSource = new InputStreamReader(new URL(testFile).openStream(),encoding);
      InputStreamReader readerForDesti = new InputStreamReader(new FileInputStream(outputFile),encoding);
      while(true) {
        int input1 = readerForSource.read();
        int input2 = readerForDesti.read();
        if(input1 < 0 || input2 < 0) {
          assertTrue(input1 < 0 && input2 < 0);
          readerForSource.close();
          readerForDesti.close();
          outputFile.delete();
          return;
        } else {
          assertEquals(input1,input2);
        }
      }
  }

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestRepositioningInfo.class);
  } // suite


  private String testFile = "";
  private String outputFile = "";
  private Document doc = null;

}