package gate.creole.morph;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import junit.framework.*;

import gate.*;
import gate.creole.*;
import gate.creole.tokeniser.DefaultTokeniser;
import gate.util.*;
import gate.util.Files;
import gate.util.OffsetComparator;

/**
 * <p>Title: TestMorph </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2000</p>
 * <p>Company: University Of Sheffield</p>
 * @author not attributable
 * @version 1.0
 */

public class TestMorph
    extends TestCase {

  private Morph morpher;
  private Document verbDocumentToTest, verbDocumentWithAnswers,
      nounDocumentToTest, nounDocumentWithAnswers;
  private FeatureMap params;
  private DefaultTokeniser tokeniser;
  private int counter = 0;
  private int outOf = 0;

  public TestMorph(String dummy) {
    super(dummy);
  }

  /**
   * This method sets up the parameters for the files to be testes
   * It initialises the Tokenizer and sets up the other parameters for
   * the morph program
   */
  protected void setUp() {
    try{
      // creating documents
      verbDocumentToTest = Factory.newDocument(
        Gate.class.getResource(Files.getResourcePath() + 
        "/gate.ac.uk/tests/morph/verbTest.dat"));
      verbDocumentWithAnswers = Factory.newDocument(
              Gate.class.getResource(Files.getResourcePath() + 
              "/gate.ac.uk/tests/morph/verbAnswer.dat"));
      nounDocumentToTest = Factory.newDocument(
              Gate.class.getResource(Files.getResourcePath() + 
              "/gate.ac.uk/tests/morph/nounTest.dat"));
      nounDocumentWithAnswers = Factory.newDocument(
              Gate.class.getResource(Files.getResourcePath() + 
              "/gate.ac.uk/tests/morph/nounAnswer.dat"));
      // create the instance of (Morphological analyzer)
      morpher = (Morph)Factory.createResource("gate.creole.morph.Morph");
    }catch (ResourceInstantiationException rie) {
      throw new GateRuntimeException(rie);
//      fail("Resources cannot be created for the test and the answer file");
    }



    // set the parameters for the morpher, feature names
    morpher.setAffixFeatureName("affix");
    morpher.setRootFeatureName("root");


    try {
      // finally create the Tokenizer
      tokeniser = (DefaultTokeniser) Factory.createResource(
          "gate.creole.tokeniser.DefaultTokeniser");
    }
    catch (ResourceInstantiationException rie) {
      fail("Resources cannot be created fpr tokenizers");
    }

  }

  /**
   * Test the morpher on verbs, if their roots are identified correctly or not
   */
  public void testVerbs() {

    // run the tokenizer on the verbTestDocument
    tokeniser.setDocument(verbDocumentToTest);
    tokeniser.setAnnotationSetName("TokeniserAS");
    try {
      tokeniser.execute();
    }
    catch (ExecutionException ee) {
      fail("Error while executing Tokenizer on the test document");
    }

    // run the tokenizer on the verbAnswerDocument
    tokeniser.setDocument(verbDocumentWithAnswers);
    tokeniser.setAnnotationSetName("TokeniserAS");
    try {
      tokeniser.execute();
    }
    catch (ExecutionException ee) {
      fail("Error while executing Tokenizer on the test document");
    }

    // check both documents are processed correctly by tokeniser
    assertTrue(!verbDocumentToTest.getAnnotations("TokeniserAS").isEmpty());
    assertTrue(!verbDocumentWithAnswers.getAnnotations("TokeniserAS").isEmpty());


    // so we have finished running the tokenizer, now we need to test the
    // morph program to test the document
    morpher.setDocument(verbDocumentToTest);

    // compile the rules
    // and check that the resource is being created successfully
    try {
      ProcessingResource pr = (ProcessingResource) (morpher.init());
      assertTrue(pr != null);
    }
    catch (ResourceInstantiationException rie) {
      fail("Error occured while compiling rules for morphological analyser" +
           " using the default.rul file");
    }

    // now check if the tokenizer was run properly on the document
    AnnotationSet inputAs = verbDocumentToTest.getAnnotations("TokeniserAS");
    List queryTokens = new ArrayList(inputAs.get(ANNIEConstants.
                                                 TOKEN_ANNOTATION_TYPE));
    Comparator offsetComparator = new OffsetComparator();
    Collections.sort(queryTokens, offsetComparator);

    // same procedure with the answer document
    AnnotationSet inputAs1 = verbDocumentWithAnswers.getAnnotations(
        "TokeniserAS");
    List answerTokens = new ArrayList(inputAs1.get(ANNIEConstants.
        TOKEN_ANNOTATION_TYPE));
    Collections.sort(answerTokens, offsetComparator);

    // create iterator to get access to each and every individual token
    Iterator queryTokensIter = queryTokens.iterator();
    Iterator answerTokensIter = answerTokens.iterator();

    while (queryTokensIter.hasNext() && answerTokensIter.hasNext()) {

      // get the word to test
      Annotation currentQueryToken = (Annotation) queryTokensIter.next();
      String queryTokenValue = (String) (currentQueryToken.getFeatures().
                                         get(ANNIEConstants.
                                             TOKEN_STRING_FEATURE_NAME));

      // get the answer of this word
      Annotation currentAnswerToken = (Annotation) answerTokensIter.next();
      String answerTokenValue = (String) (currentAnswerToken.getFeatures().
                                          get(ANNIEConstants.
                                              TOKEN_STRING_FEATURE_NAME));

      // run the morpher
      String rootWord = morpher.findBaseWord(queryTokenValue);

      // compare it with the answerTokenValue
      assertEquals(rootWord, answerTokenValue);
    }
  }

  /**
   * Test the morpher on nouns, if their roots are identified correctly or not
   */
  public void testNouns() {

    // run the tokenizer on the nounTestDocument
    tokeniser.setDocument(nounDocumentToTest);
    tokeniser.setAnnotationSetName("TokeniserAS");
    try {
      tokeniser.execute();
    }
    catch (ExecutionException ee) {
      fail("Error while executing Tokenizer on the test document");
    }

    // run the tokenizer on the nounAnswerDocument
    tokeniser.setDocument(nounDocumentWithAnswers);
    tokeniser.setAnnotationSetName("TokeniserAS");
    try {
      tokeniser.execute();
    }
    catch (ExecutionException ee) {
      fail("Error while executing Tokenizer on the test document");
    }

    // check both documents are processed correctly by tokeniser
    assertTrue(!nounDocumentToTest.getAnnotations("TokeniserAS").isEmpty());
    assertTrue(!nounDocumentWithAnswers.getAnnotations("TokeniserAS").isEmpty());

    // so we have finished running the tokenizer
    // now we need to test the morph program

    // document to test
    morpher.setDocument(nounDocumentToTest);

    // compile the rules
    // and check that the resource is being created successfully
    try {
      ProcessingResource pr = (ProcessingResource) (morpher.init());
      assertTrue(pr != null);
    }
    catch (ResourceInstantiationException rie) {
      fail("Error occured while compiling rules for morphological analyser" +
           " using the default.rul file");
    }

    // now check if the tokenizer was run properly on the document
    AnnotationSet inputAs = nounDocumentToTest.getAnnotations("TokeniserAS");
    List queryTokens = new ArrayList(inputAs.get(ANNIEConstants.
                                                 TOKEN_ANNOTATION_TYPE));
    Comparator offsetComparator = new OffsetComparator();
    Collections.sort(queryTokens, offsetComparator);

    // same procedure with the answer document
    AnnotationSet inputAs1 = nounDocumentWithAnswers.getAnnotations(
        "TokeniserAS");
    List answerTokens = new ArrayList(inputAs1.get(ANNIEConstants.
        TOKEN_ANNOTATION_TYPE));
    Collections.sort(answerTokens, offsetComparator);

    // create iterator to get access to each and every individual token
    Iterator queryTokensIter = queryTokens.iterator();
    Iterator answerTokensIter = answerTokens.iterator();

    while (queryTokensIter.hasNext() && answerTokensIter.hasNext()) {

      // get the word to test
      Annotation currentQueryToken = (Annotation) queryTokensIter.next();
      String queryTokenValue = (String) (currentQueryToken.getFeatures().
                                         get(ANNIEConstants.
                                             TOKEN_STRING_FEATURE_NAME));

      // get the answer of this word
      Annotation currentAnswerToken = (Annotation) answerTokensIter.next();
      String answerTokenValue = (String) (currentAnswerToken.getFeatures().
                                          get(ANNIEConstants.
                                              TOKEN_STRING_FEATURE_NAME));

//      System.out.println(morpher+"  "+queryTokenValue);
      // run the morpher
      String rootWord = morpher.findBaseWord(queryTokenValue);

      // compare it with the answerTokenValue
      assertEquals(rootWord, answerTokenValue);
    }

  }

  public static Test suite() {
    return new TestSuite(TestMorph.class);
  }
}