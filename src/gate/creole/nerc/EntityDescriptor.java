/*
*	EntityDescriptor.java
*
*	Valentin Tablan, July/2000
*
*	$Id$
*/

package gate.creole.nerc;

import gate.*;
import gate.util.*;

/**Represents a single named entity*/
public class EntityDescriptor {

  /**Constructs a new entity descriptor*/
  public EntityDescriptor(String string, String category, int start, int end){
    this.string = normaliseString(string);
    this.category = category;
    offsets = new int[2];
    offsets[0] = start;
    offsets[1] = end;
  }

  /**Constructs a new entity descriptor starting from a Gate annotation*/
  public EntityDescriptor(Document document, Annotation annotation){
    offsets = new int[2];
    offsets[0] = annotation.getStartNode().getOffset().intValue();
    offsets[1] = annotation.getEndNode().getOffset().intValue();
    try{
      string = normaliseString(document.getContent().getContent(
                                    annotation.getStartNode().getOffset(),
                                    annotation.getEndNode().getOffset()).
                                    toString());
    }catch(InvalidOffsetException ioe){
      ioe.printStackTrace();
    }
    category = annotation.getType();
  }

  /**Returns a normalised string for the entity. This is the string from the
    *text document the entity was descovered in, with all whitespace sequences
    *replaced by a single space character
    */
  public String getString(){
    return string;
  }

  /**Returns the category of the entity*/
  public String getCategory(){
    return category;
  }

  /**Returns a pair of integers specifying the character offsets in the original
    *file where the entity occured
    */
  public int[] getOffsets(){
    return offsets;
  }

  /**Returns a string giving the category, offsets and normalised string for the
    *entity, with no newlines.
    */
  public String toString(){
    return category + " " + offsets[0] + " " + offsets[1] + " " + string;
  }

  String string;
  String category;
  int[] offsets;

  /**Normalises a string. That is removes all the leading and trailing
    *whitespace characters and replaces all inner whitespace sequences with a
    *single space character
    */
  protected String normaliseString(String text){
    String res = "";
    if(text == null) return null;
    int charIdx = 0;
    boolean lastWasSpace = false;
    //skip the leading spaces
    while(charIdx < text.length() &&
          Character.isWhitespace(text.charAt(charIdx))) charIdx++;
    //parse the rest of the text
    while(charIdx < text.length()){
      if(Character.isWhitespace(text.charAt(charIdx))){
        //reading spaces
        lastWasSpace = true;
      }else{
        //reading non-spaces
        if(lastWasSpace) res += " ";
        res += text.charAt(charIdx);
        lastWasSpace = false;
      }
      charIdx++;
    }//while(charIdx < text.length())
    return res;
  }

}
