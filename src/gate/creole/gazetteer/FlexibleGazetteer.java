/*
 * FlexibleGazetteer.java
 *
 * Copyright (c) 2004-2011, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * Niraj Aswani 02/2002
 * $Id$
 *
 */

package gate.creole.gazetteer;

import java.util.*;
import gate.util.*;
import gate.*;
import gate.corpora.DocumentImpl;
import gate.creole.*;

/**
 * <p>
 * Title: Flexible Gazetteer
 * </p>
 * <p>
 * The Flexible Gazetteer provides users with the flexibility to choose
 * </p>
 * <p>
 * their own customized input and an external Gazetteer. For example,
 * </p>
 * <p>
 * the user might want to replace words in the text with their base
 * </p>
 * <p>
 * forms (which is an output of the Morphological Analyser) or to
 * segment
 * </p>
 * <p>
 * a Chinese text (using the Chinese Tokeniser) before running the
 * </p>
 * <p>
 * Gazetteer on the Chinese text.
 * </p>
 * 
 * <p>
 * The Flexible Gazetteer performs lookup over a document based on the
 * </p>
 * <p>
 * values of an arbitrary feature of an arbitrary annotation type, by
 * </p>
 * <p>
 * using an externally provided gazetteer. It is important to use an
 * </p>
 * <p>
 * external gazetteer as this allows the use of any type of gazetteer
 * </p>
 * <p>
 * (e.g. an Ontological gazetteer).
 * </p>
 * 
 * @author niraj aswani
 * @version 1.0
 */

public class FlexibleGazetteer extends AbstractLanguageAnalyser
implements ProcessingResource {

  private static final long serialVersionUID = -1023682327651886920L;

  /**
   * Does the actual loading and parsing of the lists. This method must
   * be called before the gazetteer can be used
   */
  public Resource init() throws ResourceInstantiationException {
    // check for parameters
    if(gazetteerInst == null)
      throw new ResourceInstantiationException("No Gazetteer Provided!");

    this.changedNodes = new ArrayList<NodePosition>();
    return this;
  }

  /**
   * This method runs the gazetteer. It assumes that all the needed
   * parameters are set. If they are not, an exception will be fired.
   */
  public void execute() throws ExecutionException {
    changedNodes = new ArrayList<NodePosition>();
    fireProgressChanged(0);
    fireStatusChanged("Checking Document...");
    if(document == null) {
      throw new ExecutionException("No document to process!");
    }

    fireStatusChanged("Creating temporary Document...");
    StringBuffer newdocString = new StringBuffer(document.getContent().toString());
    Document tempDoc = null;

    if(inputFeatureNames == null || inputFeatureNames.size() == 0) {
      inputFeatureNames = new ArrayList<String>();
    }

    long totalDeductedSpaces = 0;
    fireStatusChanged("Replacing contents with the feature value...");
    outer: for (Annotation currentToken : Utils.inDocumentOrder(document.getAnnotations(inputAnnotationSetName))) {
      // check if it is a chinesesplit; if it is, replace no space character with a single space
      if(currentToken.getType().equals(ANNIEConstants.SPACE_TOKEN_ANNOTATION_TYPE)
          && ((String)(currentToken.getFeatures().get(ANNIEConstants.TOKEN_KIND_FEATURE_NAME))).equals("ChineseSplit")) {

        // for chinese split startnode and end node are same
        long startOffset = currentToken.getStartNode().getOffset().longValue();

        // because we are adding a space in place of chinesesplit
        // the endoffset will become newStartOffset + 1
        long newStartOffset = startOffset - totalDeductedSpaces;
        long newEndOffset = newStartOffset + 1;
        NodePosition newNode = new NodePosition(startOffset, startOffset,
                newStartOffset, newEndOffset, totalDeductedSpaces);

        // here is the addition of space in the document
        totalDeductedSpaces--;
        changedNodes.add(newNode);
        newdocString = newdocString.insert((int)newStartOffset, ' ');
        continue outer;
      } // chineseSplit if

      // search in the provided inputFeaturesNames
      // if the current annotation has a feature value that user
      // wants to paste on and replace the original string
      inner: for(String inputFeatureName : inputFeatureNames) {
        String[] keyVal = inputFeatureName .split("[.]");

        if(keyVal.length == 2) {
          // keyVal[0] = annotation type
          // keyVal[1] = feature name
          if(currentToken.getType().equals(keyVal[0]) && currentToken.getFeatures().containsKey(keyVal[1])) {
            String newTokenValue = currentToken.getFeatures().get(keyVal[1]).toString();
            
            // feature value found so we need to replace it
            // find the start and end offsets for this token
            long startOffset = currentToken.getStartNode().getOffset().longValue();
            long endOffset = currentToken.getEndNode().getOffset().longValue();
            
            // replacement code start
            long actualLength = endOffset - startOffset;
            // let us find the difference between the lengths of the
            // actual string and the newTokenValue
            long lengthDifference = actualLength - newTokenValue.length();
            
            // replacement code end
            
            // so lets find out the new startOffset and endOffset
            long newStartOffset = startOffset - totalDeductedSpaces;
            long newEndOffset = newStartOffset + newTokenValue.length();
            totalDeductedSpaces += lengthDifference;
            
            // and make the entry for this
            NodePosition newNode = new NodePosition(startOffset, endOffset,
                newStartOffset, newEndOffset, totalDeductedSpaces);
            changedNodes.add(newNode);
            
            // and finally replace the actual string in the document
            // with the new document
            newdocString = newdocString.replace((int)newStartOffset,
                (int)newStartOffset + (int)actualLength, // replacement code
                newTokenValue);
            break inner;
          }
        }
      } // END OF "inner" LOOP
    } // END OF "outer" LOOP

    fireStatusChanged("New Document to be processed with Gazetteer...");
    try {
      FeatureMap params = Factory.newFeatureMap();
      params.put("stringContent", newdocString.toString());
      if(document instanceof DocumentImpl) {
        params.put("encoding", ((DocumentImpl)document).getEncoding());
        params.put("markupAware", ((DocumentImpl)document).getMarkupAware());
      }
      
      FeatureMap features = Factory.newFeatureMap();
      // Gate.setHiddenAttribute(features, true);
      tempDoc = (Document)Factory.createResource("gate.corpora.DocumentImpl",
              params, features);
    }
    catch(ResourceInstantiationException rie) {
      throw new ExecutionException("Temporary document cannot be created");
    }

    // lets create the gazetteer based on the provided gazetteer name
    //FeatureMap params = Factory.newFeatureMap();
    gazetteerInst.setDocument(tempDoc);
    gazetteerInst.setAnnotationSetName(this.outputAnnotationSetName);

    fireStatusChanged("Executing Gazetteer...");
    try {
      gazetteerInst.execute();
    }
    finally {
      gazetteerInst.setDocument(null);
    }

    // now the tempDoc has been looked up, we need to shift the tokens
    // from this temp document to the original document
    fireStatusChanged("Transfering new tags to the original one...");
    AnnotationSet original = document.getAnnotations(outputAnnotationSetName);

    for (Annotation currentLookup : Utils.inDocumentOrder(tempDoc.getAnnotations(outputAnnotationSetName))) {
      long startOffset = currentLookup.getStartNode().getOffset().longValue();
      long endOffset = currentLookup.getEndNode().getOffset().longValue();

      long originalStart = 0;
      long originalEnd = tempDoc.getContent().size() - 1L;

      int i = 0;
      for(; i < changedNodes.size(); i++) {
        NodePosition np = changedNodes.get(i);

        // Find the last node whose temp start node is less than or equal 
        // to the temp lookup's start 
        if(np.getNewStartNode() <= startOffset) {
          originalStart = np.getOldStartNode();
        } 
        else {
          break;
        }
      }
      
      for(; i < changedNodes.size(); i++) {
        NodePosition np = changedNodes.get(i);

        // Find the first node whose temp end node is greater than or equal
        // to the temp lookup's end
        if(np.getNewEndNode() >= endOffset) {
          originalEnd = np.getOldEndNode();
          break;
        }
      }

      try { 
        original.add(originalStart, originalEnd, 
            currentLookup.getType(), currentLookup.getFeatures());
      } // This should no longer happen
      catch(InvalidOffsetException ioe) {
        throw new ExecutionException(ioe);
      }

    }

    // now remove the newDoc
    Factory.deleteResource(tempDoc);
    fireProcessFinished();
  } // END execute METHOD

  /**
   * Sets the document to work on
   * 
   * @param doc
   */
  public void setDocument(gate.Document doc) {
    this.document = doc;
  }

  /**
   * Returns the document set up by user to work on
   * 
   * @return a {@link Document}
   */
  public gate.Document getDocument() {
    return this.document;
  }

  /**
   * sets the outputAnnotationSetName
   * 
   * @param annName
   */
  public void setOutputAnnotationSetName(String annName) {
    this.outputAnnotationSetName = annName;
  }

  /**
   * Returns the outputAnnotationSetName
   * 
   * @return a {@link String} value.
   */
  public String getOutputAnnotationSetName() {
    return this.outputAnnotationSetName;
  }

  /**
   * sets the inputAnnotationSetName
   * 
   * @param annName
   */
  public void setInputAnnotationSetName(String annName) {
    this.inputAnnotationSetName = annName;
  }

  /**
   * Returns the inputAnnotationSetName
   * 
   * @return a {@link String} value.
   */
  public String getInputAnnotationSetName() {
    return this.inputAnnotationSetName;
  }

  /**
   * Feature names for example: Token.string, Token.root etc... Values
   * of these features should be used to replace the actual string of
   * these features. This method allows a user to set the name of such
   * features
   * 
   * @param inputs
   */
  public void setInputFeatureNames(java.util.List<String> inputs) {
    this.inputFeatureNames = inputs;
  }

  /**
   * Returns the feature names that are provided by the user to use
   * their values to replace their actual strings in the document
   * 
   * @return a {@link List} value.
   */
  public java.util.List<String> getInputFeatureNames() {
    return this.inputFeatureNames;
  }

  public Gazetteer getGazetteerInst() {
    return this.gazetteerInst;
  }

  public void setGazetteerInst(gate.creole.gazetteer.Gazetteer gazetteerInst) {
    this.gazetteerInst = gazetteerInst;
  }



  // Gazetteer Runtime parameters
  private gate.Document document;

  private java.lang.String outputAnnotationSetName;

  private java.lang.String inputAnnotationSetName;

  // Flexible Gazetteer parameter
  private Gazetteer gazetteerInst;

  private java.util.List<String> inputFeatureNames;

  // parameters required within the program
  private ArrayList<NodePosition> changedNodes;
}
