/*
 *  TestCoref.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 02/01/2002
 *
 *  $Id$
 */

package gate.creole.coref;

import junit.framework.*;

import gate.*;
import gate.util.*;
import gate.corpora.*;
import gate.creole.*;
import gate.creole.tokeniser.*;
import gate.creole.gazetteer.*;
import gate.creole.splitter.*;
import gate.creole.orthomatcher.*;
import gate.creole.coref.*;

public class TestCoref extends TestCase {

  public TestCoref(String name) {
    super(name);
  }

  public static void main(String[] args) {

    try{
      Gate.init();
      TestCoref testCoref = new TestCoref("");

      testCoref.setUp();
      testCoref.useCase01();
      testCoref.tearDown();

    } catch(Exception e) {
      e.printStackTrace();
    }
  } // main


  /** Test suite routine for the test runner */
  public static Test suite() {
    return new TestSuite(TestCoref.class);
  } // suite

  /** Fixture set up */
  public void setUp() throws Exception {
  }

  public void tearDown() throws Exception {
  } // tearDown


  private void runANNIE(Document doc) throws Exception {
System.out.println("starting ANNIE modules...");
    DefaultTokeniser englishTokeniser = (DefaultTokeniser)Factory.createResource("gate.creole.tokeniser.DefaultTokeniser");
    DefaultGazetteer gazeteer = (DefaultGazetteer)Factory.createResource("gate.creole.gazetteer.DefaultGazetteer");
    SentenceSplitter split = (SentenceSplitter)Factory.createResource("gate.creole.splitter.SentenceSplitter");
    POSTagger tag = (POSTagger)Factory.createResource("gate.creole.POSTagger");
    ANNIETransducer neTransducer = (ANNIETransducer)Factory.createResource("gate.creole.ANNIETransducer");
    OrthoMatcher orthoMatcher = (OrthoMatcher)Factory.createResource("gate.creole.orthomatcher.OrthoMatcher");

    englishTokeniser.init();
    gazeteer.init();
    split.init();
    tag.init();
    neTransducer.init();
    orthoMatcher.init();

    englishTokeniser.setDocument(doc);
    gazeteer.setDocument(doc);
    split.setDocument(doc);
    tag.setDocument(doc);
    neTransducer.setDocument(doc);
    orthoMatcher.setDocument(doc);

    englishTokeniser.execute();
    gazeteer.execute();
    split.execute();
    tag.execute();
    neTransducer.execute();
    orthoMatcher.execute();

  }


  private Document loadDocument(String url)
    throws Exception {

    FeatureMap params = Factory.newFeatureMap(); // params list for new doc
    // set the source URL parameter to a "file:..." URL string
    params.clear();
    params.put("sourceUrl", url);

    // create the document
    Document doc = (Document) Factory.createResource("gate.corpora.DocumentImpl", params);

    return doc;
  }


  /** Test suite routine for the test runner */
  public void useCase01()
    throws Exception{
System.out.println("starting use case 01...");
    Document doc = loadDocument("file:/E:/Gate2/data/gatecorpora/ace/aps/npaper/clean/9801.35.sgm");
    runANNIE(doc);

    Coreferencer corefMain = (Coreferencer)Factory.createResource("gate.creole.coref.Coreferencer");
    corefMain.init();
    corefMain.setDocument(doc);
System.out.println("starting COREF...");
    corefMain.execute();
System.out.println("case 01 finished...");
    return;
  } // suite

}
