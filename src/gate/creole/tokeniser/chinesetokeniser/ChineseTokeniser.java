package gate.creole.tokeniser.chinesetokeniser;

/*
 *  ChineseTokeniser.java
 *
 * Copyright (c) 2000-2001, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 *  m2na2, 13/10/2003
 *
 *  $Id$
 */

import java.util.*;
import java.net.*;
import java.io.*;
import gate.creole.AbstractLanguageAnalyser;
import gate.ProcessingResource;
import gate.creole.tokeniser.SimpleTokeniser;
import gate.Resource;
import gate.creole.ResourceInstantiationException;
import gate.creole.ExecutionException;
import gate.util.GateRuntimeException;
import gate.FeatureMap;
import gate.Factory;
import gate.Gate;
import gate.Document;
import gate.AnnotationSet;
import gate.util.OffsetComparator;
import gate.Annotation;
import gate.util.InvalidOffsetException;

/**
 * <p>Title: ChineseTokeniser.java </p>
 * <p>Description: This class is a wrapper for segmenter.</p>
 * <p> Tokenises a Chinese document using the Chinesse segmenter</p>
 * @author Niraj Aswani
 * @version 1.0
 */
public class ChineseTokeniser
    extends AbstractLanguageAnalyser
    implements ProcessingResource {

  /** Instance of segmenter */
  private Segmenter segmenter;

  /** The name of the encoding used */
  private String encoding;

  /** The name of the sourceFile */
  private gate.Document document;

  /** Temporary document */
  private gate.Document tempDoc;

  /** Instance of Simple Tokenizer */
  private SimpleTokeniser tokeniser;

  /** Boolean value which says if tokeniser has to generate the spack tokens */
  private Boolean generateSpaceTokens;

  /** Rules for the simple tokeniser */
  private java.net.URL rulesURL;

  private String annotationSetName;

  private int charform;

  /** Default Constructor */
  public ChineseTokeniser() {

  }

  public Resource init() throws ResourceInstantiationException {
    fireProgressChanged(0);
    fireStatusChanged("Loading Data Files...");
    // check the encoging parameters
    if (encoding == null) {
      // setting the default parameter for encoding
      encoding = "UTF8";
    }
    else {
      if (encoding.equals("BIG5")) {
        charform = Segmenter.TRAD;
      }
      else if (encoding.equals("GBK")) {
        charform = Segmenter.SIMP;
      }
      else if (encoding.equals("UTF8")) {
        charform = Segmenter.BOTH;
      }
      else {
        // setting the default parameter for encoding
        encoding = "UTF8";
        charform = Segmenter.BOTH;
      }
    }

    if (rulesURL == null) {
      throw new ResourceInstantiationException(
          "No URL provided for the tokeniser rules");
    }
    // creating instance of segmenter
    segmenter = new Segmenter(charform, true);
    fireProcessFinished();

    // returning the current resource
    return this;
  }

  /** This method reInitialises the segmenter */
  public void reInit() throws ResourceInstantiationException {
    segmenter = new Segmenter(charform, true);
  }

  /**
   * This method gets executed whenever user clicks on the Run button
   * available in the GATE gui.  It runs the segmenter on the given document
   * and segments the text by addting spaces or space tokens with 0-length
   * character (depends on the value of generateSpaceTokens selected by the
   * user at run time).
   * @throws ExecutionException
   */
  public void execute() throws ExecutionException {
    // lets start the progress and initialize the progress counter
    fireProgressChanged(0);

    // If no document provided to process throw an exception
    if (document == null) {
      throw new GateRuntimeException("No document to process!");
    }

    // run the segmenter on this text
    String segmentedData = segmenter.segmentData(
        document.getContent().toString(),
        encoding);

    if(encoding.equals("UTF8")) {
      encoding = "UTF-8";
    }

    // now we need to create a temporary document
    // and copy all the contents of sourceDoc to this temporary document
    // so that we'll provide this document to the segmenter
    try {
      FeatureMap params = Factory.newFeatureMap();
      params.put("stringContent", segmentedData);
      FeatureMap features = Factory.newFeatureMap();

      // we need to hide the creation of new document on the GUI screen
      Gate.setHiddenAttribute(features, true);

      tempDoc = (Document) Factory.createResource("gate.corpora.DocumentImpl",
                                                  params, features);
    }
    catch (ResourceInstantiationException rie) {
      throw new ExecutionException("Temporary document cannot be created");
    }

    // and the get the marks where the spaces in the original document
    // were added
    ArrayList marks = segmenter.getMarks();

    // we need to run the Simple Tokenizer on this
    FeatureMap features = Factory.newFeatureMap();
    Gate.setHiddenAttribute(features, true);

    // set the parameters
    FeatureMap params = Factory.newFeatureMap();
    params.put("rulesURL", rulesURL);
    params.put("encoding", encoding);
    params.put("document", tempDoc);
    params.put("annotationSetName", annotationSetName);

    try {
      tokeniser = (gate.creole.tokeniser.SimpleTokeniser) Factory.
          createResource(
          "gate.creole.tokeniser.SimpleTokeniser", params, features);
    }
    catch (ResourceInstantiationException rie) {
      throw new ExecutionException(
          "Instance of SimpleTokeniser cannot be created");
    }

    // so now run the tokeniser
    tokeniser.execute();

    // so space tokens have been added, now we need to map all the newly added
    // features from tokeniser to the new document to the original one
    AnnotationSet anns;
    AnnotationSet original;

    if(annotationSetName == null || annotationSetName.length() == 0) {
      anns = tempDoc.getAnnotations();
      original = document.getAnnotations();
    } else {
      anns = tempDoc.getAnnotations(annotationSetName);
      original = document.getAnnotations(annotationSetName);
    }

    List tokens = new ArrayList(anns.get());
    Comparator offsetComparator = new OffsetComparator();
    Collections.sort(tokens, offsetComparator);
    Iterator tokenIter = tokens.iterator();


    // to make the process faster, lets copy all the marks into the long array
    long[] markValues = new long[marks.size()];
    for (int i = 0; i < marks.size(); i++) {
      markValues[i] = ( (Long) marks.get(i)).longValue();
    }
    Arrays.sort(markValues);

    // and finally transfer the annotations
    while (tokenIter.hasNext()) {
      Annotation currentToken = ( (Annotation) tokenIter.next());
      long startOffset =
          currentToken.getStartNode().getOffset().longValue();
      long endOffset =
          currentToken.getEndNode().getOffset().longValue();

      // search how many chinese splits are before the current annotation
      int index = Arrays.binarySearch(markValues, startOffset);
      if (index >= 0) {
        // it is a chinese split
        if (generateSpaceTokens.booleanValue()) {
          try {
            FeatureMap newFeatures = Factory.newFeatureMap();
            newFeatures.put("kind", "ChineseSplit");
            original.add(new Long(startOffset - index),
                         new Long(startOffset - index),
                         SPACE_TOKEN_ANNOTATION_TYPE, newFeatures);
          }
          catch (InvalidOffsetException ioe) {
            throw new ExecutionException("Offset Error");
          }
        }

      }
      else {
        index = Math.abs(index) - 1;

        // it is not a chinese split but some other token
        // lets add this annotation to the original document
        String annotSetName = currentToken.getType();
        FeatureMap newFeatureMap = currentToken.getFeatures();
        try {
          original.add(new Long(startOffset - index),
                       new Long(endOffset - index), annotSetName,
                       newFeatureMap);
        }
        catch (InvalidOffsetException ioe) {
          throw new ExecutionException(
              "Problem with the invalid offset while adding annotations" +
              "to the original document");
        }
      }
    }
    // and finally remove the temporary Document
    Factory.deleteResource(tempDoc);

    // process finished, acknowledge user about this.
    fireProcessFinished();
  }

  // getter and setter method
  /**
   * Sets the boolean parameter which states if segmenter should produce
   * the space tokens
   * @param inputFile
   */
  public void setGenerateSpaceTokens(Boolean value) {
    this.generateSpaceTokens = value;
  }

  /**
   * Gets the boolean parameter which states if segmenter should produce
   * the space tokens
   * @param inputFile
   */
  public Boolean getGenerateSpaceTokens() {
    return this.generateSpaceTokens;
  }

  /**
   * Sets the document to be processed
   * @param document - document to be processed
   */
  public void setDocument(gate.Document inputFile) {
    this.document = inputFile;
  }

  /**
   * Returns the document under process
   */
  public gate.Document getDocument() {
    return this.document;
  }

  /**
   * Sets the rule file to be processed
   * @param String - rule File name to be processed
   */
  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  /**
   * Returns the document under process
   */
  public String getEncoding() {
    return this.encoding;
  }

  /**
   * URL for the file, which contains rules to be given to the tokeniser
   * @param rules
   */
  public void setRulesURL(java.net.URL rules) {
    this.rulesURL = rules;
  }

  /**
   * Returns the URL of the file, which contains rules for the tokeniser
   * @return
   */
  public java.net.URL getRulesURL() {
    return rulesURL;
  }

  /**
   * AnnotationSet name
   * @param name Name of the annotation
   */
  public void setAnnotationSetName(String name) {
    this.annotationSetName = name;
  }

  /**
   * Returns the provided annotationset name
   * @return
   */
  public String getAnnotationSetName() {
    return this.annotationSetName;
  }
}