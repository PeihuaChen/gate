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
    if(gazetteerInst == null)  {
      throw new ResourceInstantiationException("No Gazetteer Provided!");
    }
    return this;
  }

  /**
   * This method runs the gazetteer. It assumes that all the needed
   * parameters are set. If they are not, an exception will be fired.
   */
  public void execute() throws ExecutionException {
    annotationMappings = new ArrayList<NodePosition>();
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
      // Where ChineseSplits occur, insert a single space
      if(currentToken.getType().equals(ANNIEConstants.SPACE_TOKEN_ANNOTATION_TYPE)
          && ((String)(currentToken.getFeatures().get(ANNIEConstants.TOKEN_KIND_FEATURE_NAME))).equals("ChineseSplit")) {

        // for chinese split startnode and end node are same
        long startOffset = currentToken.getStartNode().getOffset().longValue();

        // because we are adding a space in place of chinesesplit
        // the endoffset will become newStartOffset + 1
        long newStartOffset = startOffset - totalDeductedSpaces;
        long newEndOffset = newStartOffset + 1;
        NodePosition mapping = new NodePosition(startOffset, startOffset,
                newStartOffset, newEndOffset);

        // here is the addition of space in the document
        totalDeductedSpaces--;
        // Should these Splits actually be mappable from the temp document
        // back to the original one?  (AF)
        annotationMappings.add(mapping);
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
            
            // let us find the difference between the lengths of the
            // actual string and the newTokenValue
            long actualLength = endOffset - startOffset;
            long lengthDifference = actualLength - newTokenValue.length();
            
            // so lets find out the new startOffset and endOffset
            long newStartOffset = startOffset - totalDeductedSpaces;
            long newEndOffset = newStartOffset + newTokenValue.length();
            totalDeductedSpaces += lengthDifference;
            
            // and make the entry for this
            NodePosition mapping = new NodePosition(startOffset, endOffset,
                newStartOffset, newEndOffset);
            annotationMappings.add(mapping);
            
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
    
    // make sure the conversion table is in the right order
    Collections.sort(annotationMappings, new NodePositionComparator());

    fireStatusChanged("New Document to be processed with Gazetteer...");
    try {
      FeatureMap params = Factory.newFeatureMap();
      params.put("stringContent", newdocString.toString());
      if(document instanceof DocumentImpl) {
        params.put("encoding", ((DocumentImpl)document).getEncoding());
        params.put("markupAware", ((DocumentImpl)document).getMarkupAware());
      }
      
      FeatureMap features = Factory.newFeatureMap();
      tempDoc = (Document)Factory.createResource("gate.corpora.DocumentImpl",
              params, features);
      
      /* Mark the document with the locations of the input annotations so
       * that we can later eliminate Lookups that are out of scope.       */
      for (NodePosition mapping : annotationMappings) {
        tempDoc.getAnnotations().add(mapping.getNewStartOffset(), mapping.getNewEndOffset(), "Input", Factory.newFeatureMap());
      }
      
    }
    catch(ResourceInstantiationException rie) {
      throw new ExecutionException("Temporary document cannot be created", rie);
    } 
    catch(InvalidOffsetException e) {
      throw new ExecutionException("Temporary document cannot be created", e);
    }

    // lets create the gazetteer based on the provided gazetteer name
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
    AnnotationSet tempInputAS = tempDoc.getAnnotations().get("Input");
    //System.out.printf("temp Input size = %d\n", tempInputAS.size());

    for (Annotation currentLookup : Utils.inDocumentOrder(tempDoc.getAnnotations(outputAnnotationSetName))) {
      long tempStartOffset = currentLookup.getStartNode().getOffset().longValue();
      long tempEndOffset = currentLookup.getEndNode().getOffset().longValue();

      /* Ignore Lookups that are out of the range of the input annotations.
       */
      if (coveredByInput(tempStartOffset, tempEndOffset, tempInputAS)) {
        long originalStart = 0;
        long originalEnd = document.getContent().size() - 1L;
        
        int i = 0;
        
        for ( ; i < annotationMappings.size() ; i++) {
          /* Find the last mapping whose temp start offset is less than or equal 
           * to the temp lookup's start   */ 
          NodePosition mapping = annotationMappings.get(i);
          if (mapping.getNewStartOffset() <= tempStartOffset)  {
            originalStart = mapping.getOriginalStartOffset();
          }
          else {
            /* At this point, we are on the Token after the first one that 
             * matches the Lookup; the current one might also match, but we need to
             * back up to be sure.             */
            i--;
            break;
          }
        }
        
        for ( ; i < annotationMappings.size() ; i++) {
          /* Find the first mapping whose temp end offset is greater than or equal
           * to the temp lookup's end; typically this will be the same mapping as used for
           * for the start offset, but it could be a subsequent one.         */
          NodePosition mapping = annotationMappings.get(i);
          if (mapping.getNewEndOffset() >= tempEndOffset) {
            originalEnd = mapping.getOriginalEndOffset();
            addToOriginal(original, originalStart, originalEnd, tempStartOffset, tempEndOffset, currentLookup, tempDoc);
            break;
          }
        }
      }
    }

    // now remove the newDoc
    Factory.deleteResource(tempDoc);
    fireProcessFinished();
  } // END execute METHOD

  
  private void addToOriginal(AnnotationSet original, long originalStart, long originalEnd, 
      long tempStart, long tempEnd, Annotation tempLookup, Document tempDoc) throws ExecutionException {
    try {
      original.add(originalStart, originalEnd, tempLookup.getType(), tempLookup.getFeatures());
    } // This should no longer happen
    catch(InvalidOffsetException ioe) {
      // Better debugging info for when it does
      System.err.printf("temp %d, %d [%s]-> original %d, %d\n", tempStart, tempEnd, Utils.stringFor(tempDoc, tempLookup), 
          originalStart, originalEnd);
      throw new ExecutionException(ioe);
    }
  }

  /* Is this Lookup within the scope of the input annotations?  It might not be, if Token annotations
   * have been copied by AST only over the significant sections of the document.
   */
  private boolean coveredByInput(long tempStart, long tempEnd, AnnotationSet tempInputAS) {
    if (tempInputAS.getCovering("Input", tempStart, tempStart).isEmpty()) {
      return false;
    }
    // implied else
    if (tempInputAS.getCovering("Input", tempEnd, tempEnd).isEmpty()) {
      return false;
    }
    // implied else
    return true;
  }
  
  
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
  private ArrayList<NodePosition> annotationMappings;
}
