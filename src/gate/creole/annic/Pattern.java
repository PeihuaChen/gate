/*
 *  Pattern.java
 *
 *  Niraj Aswani, 19/March/07
 *
 *  $Id: Pattern.html,v 1.0 2007/03/19 16:22:01 niraj Exp $
 */
package gate.creole.annic;

import java.util.ArrayList;

/**
 * Pattern is an extension of the HIT class. This provides information
 * about the underlying annotations as well as the information about its
 * left and right context.
 * 
 * @author niraj
 * 
 */
public class Pattern extends Hit {

  /**
   * serial version id
   */
  private static final long serialVersionUID = 3258126955659604530L;

  /**
   * Left context start offset
   */
  protected int leftContextStartOffset;

  /**
   * right context end offset
   */
  protected int rightContextEndOffset;

  /**
   * pattern text
   */
  protected String patternText;

  /**
   * annotations
   */
  protected ArrayList<PatternAnnotation> annotations;

  /**
   * Constructor
   * @param docID
   * @param patternText
   * @param startOffset
   * @param endOffset
   * @param leftContextStartOffset
   * @param rightContextEndOffset
   * @param annotations
   * @param queryString
   */
  public Pattern(String docID, String patternText, int startOffset,
          int endOffset, int leftContextStartOffset, int rightContextEndOffset,
          ArrayList<PatternAnnotation> annotations, String queryString) {
    super(docID, startOffset, endOffset, queryString);
    this.patternText = patternText;
    this.leftContextStartOffset = leftContextStartOffset;
    this.rightContextEndOffset = rightContextEndOffset;
    this.annotations = annotations;
  }

  /**
   * Returns the annotations lying between the start and the end offsets
   * 
   * @param start
   * @param end
   * @return
   */
  public ArrayList getPatternAnnotations(int startOffset, int endOffset) {
    ArrayList<PatternAnnotation> annots = new ArrayList<PatternAnnotation>();
    for(int i = 0; i < annotations.size(); i++) {
      PatternAnnotation ga1 = (PatternAnnotation)annotations.get(i);
      if(ga1.getStartOffset() >= startOffset && ga1.getEndOffset() <= endOffset) {
        annots.add(ga1);
      }
    }
    return annots;
  }

  /**
   * Converts the list of annotations to an array
   * @param annotations
   * @return
   */
  private PatternAnnotation[] toArray(ArrayList<PatternAnnotation> annotations) {
    PatternAnnotation anns[] = new PatternAnnotation[annotations.size()];
    for(int i = 0; i < annotations.size(); i++) {
      anns[i] = (PatternAnnotation)annotations.get(i);
    }
    return anns;
  }

  /**
   * Returns all annotations underlying the pattern
   * 
   * @return
   */
  public PatternAnnotation[] getPatternAnnotations() {
    return toArray(annotations);
  }

  /**
   * Returns the annotations with the given type
   * 
   * @param type
   * @return
   */
  public PatternAnnotation[] getPatternAnnotations(String type) {
    ArrayList<PatternAnnotation> annots = new ArrayList<PatternAnnotation>();
    for(int i = 0; i < annotations.size(); i++) {
      PatternAnnotation ga1 = (PatternAnnotation)annotations.get(i);
      if(ga1.getType().equals(type)) {
        annots.add(ga1);
      }
    }
    return toArray(annots);
  }

  /**
   * Returns the annotations with the given type and the feature
   * 
   * @param type
   * @param feature
   * @return
   */
  public PatternAnnotation[] getPatternAnnotations(String type, String feature) {
    ArrayList<PatternAnnotation> annots = new ArrayList<PatternAnnotation>();
    for(int i = 0; i < annotations.size(); i++) {
      PatternAnnotation ga1 = (PatternAnnotation)annotations.get(i);
      if(ga1.getType().equals(type)) {
        // check if this has the following feature
        java.util.HashMap<String, String> features = ga1.getFeatures();
        if(features != null && features.keySet().contains(feature)) {
          annots.add(ga1);
        }
      }
    }
    return toArray(annots);
  }

  /**
   * Returns the text of the pattern
   * 
   * @return
   */
  public String getPatternText() {
    return patternText;
  }

  /**
   * Returns the text of the pattern between the given offsets
   * 
   * @param startOffset
   * @param endOffset
   * @return
   */
  public String getPatternText(int startOffset, int endOffset) {
    return patternText.substring(startOffset - leftContextStartOffset,
            endOffset - leftContextStartOffset);
  }

  /**
   * Returns the start offset of the left context
   * 
   * @return
   */
  public int getLeftContextStartOffset() {
    return leftContextStartOffset;
  }

  /**
   * Returns the end offset of the right context
   * 
   * @return
   */
  public int getRightContextEndOffset() {
    return rightContextEndOffset;
  }
}
