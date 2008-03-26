package gate.stanford;

import java.util.*;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.ling.TaggedWord;
import gate.*;
import gate.creole.ANNIEConstants;
import gate.util.OffsetComparator;

/**
 * The Stanford Parser itself takes as input a List of edu.stanford.nlp.ling.Word.
 * This data structure is constructed from a Sentence Annotation, using the enclosed
 * Token Annotations, and yields the required List, as well as methods for
 * converting the parser's output spans into GATE Annotation offsets.
 * 
 * @author Adam Funk
 *
 */
public class StanfordSentence {
  
  private Map<Integer, Long> startPosToOffset;
  private Map<Integer, Long> endPosToOffset;
  private Map<Integer, Annotation> startPosToToken;
  private List<Word>         words;
  private Long               sentenceStartOffset, sentenceEndOffset;
  private List<Annotation>   tokens;

  private static final String  POS_TAG_FEATURE    = ANNIEConstants.TOKEN_CATEGORY_FEATURE_NAME;
  private static final String  STRING_FEATURE     = ANNIEConstants.TOKEN_STRING_FEATURE_NAME;
  
  int nbrOfTokens, nbrOfMissingPosTags;
  
  
  /* This is probably dodgy, but I can't find an "unknown" tag 
   * in the Penn documentation.    */
  private static final String  UNKNOWN_TAG     = "NN";
  
  public StanfordSentence(Annotation sentence, String tokenType, 
    AnnotationSet inputAS, boolean usePosTags) {
    
    OffsetComparator  offsetComparator = new OffsetComparator();
    Annotation token;
    String tokenString;

    startPosToOffset = new HashMap<Integer, Long>();
    endPosToOffset   = new HashMap<Integer, Long>();
    startPosToToken  = new HashMap<Integer, Annotation>();
    
    sentenceStartOffset = sentence.getStartNode().getOffset();
    sentenceEndOffset   = sentence.getEndNode().getOffset();
   
    nbrOfTokens   = 0;
    nbrOfMissingPosTags = 0;
    
    tokens = new ArrayList<Annotation>(inputAS.getContained(sentenceStartOffset, sentenceEndOffset).get(tokenType));
    java.util.Collections.sort(tokens, offsetComparator);

    Iterator<Annotation> tokenIter = tokens.iterator();
    words = new ArrayList<Word>();
    int tokenNo = 0;

    while(tokenIter.hasNext()) {
      token = tokenIter.next();
      tokenString = (String) token.getFeatures().get(STRING_FEATURE);
      add(tokenNo, token);
      
      /* The FAQ says the parser will automatically use existing POS tags
       * if the List elements are of type TaggedWord.  
       * http://nlp.stanford.edu/software/parser-faq.shtml#f
       */
      
      if (usePosTags)  {
        words.add(new TaggedWord(tokenString, getPosTag(token)));
      }
      else {
        words.add(new Word(tokenString));
      }

      tokenNo++;
    }
    
    nbrOfTokens = tokenNo;

  }

  
  
  private String getPosTag(Annotation token)  {
    String pos = UNKNOWN_TAG;
    FeatureMap tokenFeatures = token.getFeatures();

    if (tokenFeatures.containsKey(POS_TAG_FEATURE)) {
      Object temp = tokenFeatures.get(POS_TAG_FEATURE);
      
      if (temp instanceof String) {
        pos = (String) temp;
      }
      else {
        nbrOfMissingPosTags++;
      }
      
    }
    else {
      nbrOfMissingPosTags++;
    }
    
    return pos;
  }
  


  private void add(int tokenNbr, Annotation token) {
    Long tokenStartOffset = token.getStartNode().getOffset();
    Long tokenEndOffset   = token.getEndNode().getOffset();
    Integer tokenNbrInt = new Integer(tokenNbr);

    startPosToOffset.put(tokenNbrInt, tokenStartOffset);
    endPosToOffset.put(new Integer(tokenNbr + 1), tokenEndOffset);
    startPosToToken.put(tokenNbrInt, token);
  }
  

  
  /* Explanation of the position conversion:
   * The output of the Stanford Parser specifies each constituent's span in terms of 
   * token boundaries re-numbered within each sentence, which we need to convert to 
   * GATE character offsets within the whole document.
   * 
   * Example: "This is a test." starting at offset 100, containing five tokens.
   * Stanford says "This" starts at 0 and ends at 1; GATE says 100 to 104.
   * Stanford says "is a test" starts at 1 and ends at 4;
   * GATE says 105 to 114.
   */
  
  
  public int numberOfTokens() {
    return nbrOfTokens;
  }
  
  public int numberOfMissingPosTags() {
    return nbrOfMissingPosTags;
  }
  

  /**
   * Convert a Stanford start position to the GATE Annotation of type
   * "Token" that starts there.
   */
  public Annotation startPos2token(int startPos) {
    return startPosToToken.get(new Integer(startPos));
  }

  /**
   * Convert a Stanford start position to a GATE offset.
   * @param startPos
   * @return
   */
  public Long startPos2offset(int startPos) {
    return startPosToOffset.get(new Integer(startPos));
  }

  /**
   * Convert a Stanford end position to a GATE offset.
   * @param endPos
   * @return
   */
  public Long endPos2offset(int endPos) {
    return endPosToOffset.get(new Integer(endPos));
  }

  
  /**
   * @return The data structure that is passed to the Stanford Parser itself.
   */
  public List<Word> getWordList() {
    return words;
  }
}
