package gate.creole.tokeniser;

/**
 * Title:        Gate2
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      University Of Sheffield
 * @author Hamish, Kalina, Valy, Cristi
 * @version 1.0
 */
import java.util.*;
import java.io.*;
import java.net.*;
import java.beans.*;
import java.lang.reflect.*;
import junit.framework.*;

import gate.*;
import gate.util.*;
import gate.corpora.TestDocument;

public class TestTokeniser extends TestCase{

  public TestTokeniser(String name) {
    super(name);
  }

  /** Fixture set up */
  public void setUp() throws Exception {
  }

  public void tearDown() throws Exception {
  } // tearDown

  /** Test the default tokeniser */
  public void testDefaultTokeniser() throws Exception {
    //get a document
    Document doc = Factory.newDocument(
      new URL(TestDocument.getTestServerName() + "tests/doc0.html")
    );
    //create a default tokeniser
    DefaultTokeniser tokeniser = new DefaultTokeniser();
    AnnotationSet tokeniserAS = doc.getAnnotations("TokeniserAS");
    tokeniser.tokenise(doc, tokeniserAS, false);
    assert(!tokeniserAS.isEmpty());
  }

  /**Tests a custom tokeniser. It uses the default tokeniser but loads it as if
    *it were a custom one
    */
  public void testCustomTokeniser() throws Exception {
    //get a document
    Document doc = Factory.newDocument(
      new URL(TestDocument.getTestServerName() + "tests/doc0.html")
    );
    //create a tokeniser
    DefaultTokeniser tokeniser = new DefaultTokeniser(
    Files.getResourceAsStream(Files.getResourcePath() +
                              "/creole/tokeniser/DefaultTokeniser.rules"));
    AnnotationSet tokeniserAS = doc.getAnnotations("TokeniserAS");
    tokeniser.tokenise(doc, tokeniserAS, false);
    assert(!tokeniserAS.isEmpty());
  }

  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestTokeniser.class);
  } // suite

  public static void main(String[] args) {
    try{
      Gate.init();
      TestTokeniser testTokeniser1 = new TestTokeniser("");
      testTokeniser1.setUp();
      testTokeniser1.testDefaultTokeniser();
      testTokeniser1.tearDown();
      testTokeniser1.setUp();
      testTokeniser1.testCustomTokeniser();
      testTokeniser1.tearDown();
    }catch(Exception e){
      e.printStackTrace();
    }
  }
}