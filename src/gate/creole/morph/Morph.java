package gate.creole.morph;


/*
 *  Morph.java
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
 *  Niraj Aswani, 13/10/2003
 *
 *  $Id$
 */


import java.util.*;
import gate.util.*;
import gate.creole.AbstractLanguageAnalyser;
import gate.Resource;
import gate.creole.ResourceInstantiationException;
import gate.creole.ExecutionException;
import gate.ProcessingResource;
import gate.AnnotationSet;
import gate.Annotation;
import gate.util.GateRuntimeException;

/**
 * <p>Title: Morph.java </p>
 * <p>Description: This class is a wrapper for {@link Interpreter}, the
 * Morphological Analyzer.</p>
 * @author Niraj Aswani
 * @version 1.0
 */
public class Morph
    extends AbstractLanguageAnalyser
    implements ProcessingResource {


  /** Document to be processed by the morpher, must be provided at Runtime. */
  private gate.Document document;

  /** File which cotains rules to be processed */
  private String rulesFile;

  /** Instance of BaseWord class - English Morpher */
  private Interpret interpret;

  /** Feature Name that should be displayed for the root word */
  private String rootFeatureName;

  /** Feature Name that should be displayed for the affix */
  private String affixFeatureName;

  /** The name of the annotation set used for input */
  private String annotationSetName;

  /** Boolean value that tells if parser should behave in caseSensitive mode */
  private Boolean caseSensitive;

  /** Default Constructor */
  public Morph() {
  }

  /**
   * This method creates the instance of the BaseWord - English Morpher and
   * returns the instance of current class with different attributes and
   * the instance of BaseWord class wrapped into it.
   * @return Resource
   * @throws ResourceInstantiationException
   */
  public Resource init() throws ResourceInstantiationException {
    interpret = new Interpret();
    if (rulesFile == null) {
      // no rule file is there, simply run the interpret to interpret it and
      throw new ResourceInstantiationException("\n\n No Rule File Provided");
    }

    // compile the rules
    interpret.init(rulesFile);

    return this;
  }

  /**
   * Method is executed after the init() method has finished its execution.
   * <BR>Method does the following operations:
   * <OL type="1">
   * <LI> creates the annotationSet
   * <LI> fetches word tokens from the document, one at a time
   * <LI> runs the morpher on each individual word token
   * <LI> finds the root and the affix for that word
   * <LI> adds them as features to the current token
   * @throws ExecutionException
   */
  public void execute() throws ExecutionException {
    // lets start the progress and initialize the progress counter
    fireProgressChanged(0);

    // If no document provided to process throw an exception
    if (document == null) {
      fireProcessFinished();
      throw new GateRuntimeException("No document to process!");
    }

    // get the annotationSet name provided by the user, or otherwise use the
    // default method
    AnnotationSet inputAs = (annotationSetName == null ||
        annotationSetName.length() == 0) ?
        document.getAnnotations() :
        document.getAnnotations(annotationSetName);

    // Morpher requires English tokenizer to be run before running the Morpher
    // Fetch tokens from the document
    AnnotationSet tokens = inputAs.get(TOKEN_ANNOTATION_TYPE);
    if (tokens == null || tokens.isEmpty()) {
      fireProcessFinished();
      throw new ExecutionException(
          "Please run the English Tokenizer first and then Morpher");
    }

    // create iterator to get access to each and every individual token
    Iterator tokensIter = tokens.iterator();

    // variables used to keep track on progress
    int tokenSize = tokens.size();
    int tokensProcessed = 0;
    int lastReport = 0;

    //lets process each token one at a time
    while (tokensIter.hasNext()) {
      Annotation currentToken = (Annotation) tokensIter.next();
      String tokenValue = (String) (currentToken.getFeatures().
                                    get(TOKEN_STRING_FEATURE_NAME));
      // run the Morpher
      if(!caseSensitive.booleanValue()) {
        tokenValue = tokenValue.toLowerCase();
      }

      String baseWord = interpret.runMorpher(tokenValue);
      String affixWord = interpret.getAffix();

      // no need to add affix feature if it is null
      if (affixWord != null) {
        currentToken.getFeatures().put(affixFeatureName, affixWord);
      }
      // add the root word as a feature
      currentToken.getFeatures().put(rootFeatureName, baseWord);

      // measure the progress and update every after 100 tokens
      tokensProcessed++;
      if(tokensProcessed - lastReport > 100){
        lastReport = tokensProcessed;
        fireProgressChanged(tokensProcessed * 100 /tokenSize);
      }
    }
    // process finished, acknowledge user about this.
    fireProcessFinished();
  }

  // getter and setter method
  /**
   * Sets the document to be processed
   * @param document - document to be processed
   */
  public void setDocument(gate.Document document) {
    this.document = document;
  }


  /**
   * This method should only be called after init()
   * @param word
   * @return the rootWord
   */
  public String findBaseWord(String word) {
    return interpret.runMorpher(word);
  }

  /**
   * This method should only be called after init()
   * @param word
   * @return the afix of the rootWord
   */
  public String findAffix(String word) {
    interpret.runMorpher(word);
    return interpret.getAffix();
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
  public void setRulesFile(String rulesFile) {
    this.rulesFile = rulesFile;
  }

  /**
   * Returns the document under process
   */
  public String getRulesFile() {
    return this.rulesFile;
  }

  /**
   * Returns the feature name that has been currently set to display the root
   * word
   */
  public String getRootFeatureName() {
    return rootFeatureName;
  }

  /**
   * Sets the feature name that should be displayed for the root word
   * @param rootFeatureName
   */
  public void setRootFeatureName(String rootFeatureName) {
    this.rootFeatureName = rootFeatureName;
  }

  /**
   * Returns the feature name that has been currently set to display the affix
   * word
   */
  public String getAffixFeatureName() {
    return affixFeatureName;
  }

  /**
   * Sets the feature name that should be displayed for the affix
   * @param affixFeatureName
   */
  public void setAffixFeatureName(String affixFeatureName) {
    this.affixFeatureName = affixFeatureName;
  }

  /**
   * Returns the name of the AnnotationSet that has been provided to create
   * the AnnotationSet
   */
  public String getAnnotationSetName() {
    return annotationSetName;
  }

  /**
   * Sets the AnnonationSet name, that is used to create the AnnotationSet
   * @param annotationSetName
   */
  public void setAnnotationSetName(String annotationSetName) {
    this.annotationSetName = annotationSetName;
  }

  /**
   * A method which returns if the parser is in caseSenstive mode
   * @return
   */
  public Boolean getCaseSensitive() {
    return this.caseSensitive;
  }

  /**
   * Sets the caseSensitive value, that is used to tell parser if it should
   * convert document to lowercase before parsing
   */
  public void setCaseSensitive(java.lang.Boolean value) {
    this.caseSensitive = value;
  }
}
