/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 28 May 2002
 *
 *  $Id$
 */
package gate.ml;

import java.util.*;

import weka.core.*;


import gate.*;
import gate.util.*;
import gate.creole.ANNIEConstants;
/**
 * Extracts the POS category of the n-th word inside the instance.
 */
public class POSCategoryExtractor extends AbstractAttributeExtractor {

  public Attribute getAttribute() {
    List posCats = Arrays.asList(POS_CATEGORIES);
    FastVector values = new FastVector(POS_CATEGORIES.length);
    for(int i = 0; i < POS_CATEGORIES.length; i++)
      values.addElement(POS_CATEGORIES[i]);
    Attribute attribute = new Attribute("POS(" + position + ")", values);
    return attribute;
  }

  public Object getAttributeValue(Object data){
    if(position > 0) return getInsidePOSValue(data);
    else return getLeftContextPOS(data);
  }

  /**
   * This method will find POS category for tokens in the left context of the
   * target annotation (where position is negative).
   * @param data
   * @return
   */
  protected Object getLeftContextPOS(Object data){
    //the data is an annotation in this case.
    Annotation ann = (Annotation)data;
    Long previousOffset = dataCollector.previousOffset(
                                        ann.getStartNode().getOffset());
    //we start looking for Tokens going backwards from the annotation start.
    int skippedTokens = 0;
    while(previousOffset != null &&
          skippedTokens < -position){
      Set startingAnnots = dataCollector.getStartingAnnotations(previousOffset);
      if(startingAnnots != null && (!startingAnnots.isEmpty())){
        Iterator annIter = startingAnnots.iterator();
        while(annIter.hasNext()){
          Annotation annotation = (Annotation)annIter.next();
          if(annotation.getType().equals(ANNIEConstants.TOKEN_ANNOTATION_TYPE)){
            skippedTokens++;
            if(skippedTokens == -position){
              //the token we just skipped was the one we needed
              if(annotation.getFeatures() != null){
                String pos = (String)annotation.getFeatures().
                             get(ANNIEConstants.TOKEN_CATEGORY_FEATURE_NAME);
                if(posValues.contains(pos)) return pos;
                else{
                  Out.prln("Warning: unknown POS category: " + pos);
                }
              }
              return null;
            }
          }
        }
      }
      previousOffset = dataCollector.previousOffset(previousOffset);
    }
    //could not find the token
    return null;
  }

  /**
   * This method will find the POS category for tokens covered by the instance
   * annotation and tokens that are part of the right context.
   * @param data the instance annotation
   * @return the POS category as a string.
   */
  protected Object getInsidePOSValue(Object data){
    //the data is an annotation in this case.
    Annotation ann = (Annotation)data;
    Long endOffset = ann.getEndNode().getOffset();
    Long nextOffset = ann.getStartNode().getOffset();
    int skippedTokens = 0;
    while(nextOffset != null &&
          ((!ignoreRightContext) || (nextOffset.compareTo(endOffset) < 0)) &&
          skippedTokens < position){
      //advance offset skipping all tokens found
      Set startingAnnots = dataCollector.getStartingAnnotations(nextOffset);
      if(startingAnnots != null && (!startingAnnots.isEmpty())){
        Iterator annIter = startingAnnots.iterator();
        while(annIter.hasNext()){
          Annotation annotation = (Annotation)annIter.next();
          if(annotation.getType().equals(ANNIEConstants.TOKEN_ANNOTATION_TYPE)){
            skippedTokens++;
            if(skippedTokens == position){
              //the token we just skipped was the one we needed
              if(annotation.getFeatures() != null){
                String pos = (String)annotation.getFeatures().
                             get(ANNIEConstants.TOKEN_CATEGORY_FEATURE_NAME);
                if(posValues.contains(pos)) return pos;
                else{
                  Out.prln("Warning: unknown POS category: " + pos);
                }
              }
              return null;
            }
          }
        }
      }
      nextOffset = dataCollector.nextOffset(nextOffset);
    }
    //could not find the token
    return null;
  }

  /**
   * Sets the (1-based) location of the word inside the instance that this
   * extractor targets.
   * Negative positions mean tokens in the right context.
   * Position cannot be zero!
   * @param position an int value.
   */
  public void setPosition(int position){
    this.position = position;
  }

  public void setIgnoreRightContext(boolean ignoreRightContext) {
    this.ignoreRightContext = ignoreRightContext;
  }

  public boolean isIgnoreRightContext() {
    return ignoreRightContext;
  }

  /**
   * The 1-based position of the Token (for which the POS will gbe extracted)
   * inside the instance annotation.
   */
  protected int position;

  /**
   * Used internally for easy element-of tests
   */
  private List posValues = Arrays.asList(POS_CATEGORIES);

  static protected final String[] POS_CATEGORIES = new String[]
        {"NN", "NNP", "NNPS", "NNS", "NP", "NPS", "JJ", "JJR", "JJS",
         "JJSS", "RB", "RBR", "RBS", "VB", "VBD", "VBG", "VBN",
         "VBP", "VBZ", "FW", "CD", "CC", "DT", "EX", "IN", "LS",
         "MD", "PDT", "POS", "PP", "PRP", "PRP$", "PRPR$", "RP",
         "TO", "UH", "WDT", "WP", "WP$", "WRB", "SYM", "\"", "#",
         "$", "'", "(", ")", ",", "--", "-LRB-", ".", "''", ":" ,"::", "`"};

  private boolean ignoreRightContext = true;
}