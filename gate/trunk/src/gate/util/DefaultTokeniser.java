package gate.util;

import gate.*;

import java.util.*;
import java.text.BreakIterator;

public class DefaultTokeniser implements ProcessingResource,
                                         ProcessProgressReporter,
                                         Runnable{

  public DefaultTokeniser(){
  }

  public static void main(String[] args) {
//    DefaultTokenizer defaultTokenizer = new DefaultTokenizer();
  }

  public Factory getFactory(){
    return new Transients();
  }

  public FeatureMap getFeatures(){
    return features;
  }

  public void setFeatures(FeatureMap features){
    this.features = features;
  }

  /** Tokenises the given document writting all the generated annotations in
    * the provided annotation set.
    * It is the user's responsability to make sure that the annotation set
    * provided belongs to the document as the tokeniser will not make any
    * checks.
    */
  public void tokenise(Document doc, AnnotationSet annotationSet,
                       boolean runInNewThread){
    this.doc =  doc;
    this.annotationSet = annotationSet;
    if(runInNewThread){
      Thread thread = new Thread(this);
      thread.start();
    }else run();
  }

  /** Tokenises the given document writting all the generated annotations in
    * the default annotation set.
    */
  public void tokenise(Document doc, boolean runInNewThread){
    tokenise(doc, doc.getAnnotations(), runInNewThread);
  }

  public void run(){
    String content = doc.getContent().toString();
    FeatureMap fm;
    boolean containsDigits = false, containsLetters = false,
            containsDash = false, containsSlash = false,
            containsComma = false, containsPeriod = false,
            containsUpper = false, containsLower = false,
            upperInitial = false, containsUnknownChar = false;
    char currentChar;
    int charIdx = 0;
    int length = content.length();
    int tokenStart;
    //each step of this "do..while" cycle should parse one or more tokens
    try{
      do{
        tokenStart = charIdx;
        //parse the whitespace
        while(charIdx < length &&
              Character.isWhitespace(content.charAt(charIdx))){
          //parse the gap
          //all characters that are space are considered gap
          while(charIdx < length &&
                Character.isSpaceChar(content.charAt(charIdx))){

            charIdx++;
          }//parse gap
          //add the gap
          if(charIdx > tokenStart){
            fm = Transients.newFeatureMap();
            fm.put("String", content.substring(tokenStart, charIdx));
            fm.put("kind", "gap");
            annotationSet.add(new Long(tokenStart), new Long(charIdx),
                              "SpaceToken", fm);
            tokenStart = charIdx;
          }

          //parse the break
          //all the character that are whitespace but not space are breaks
          while(charIdx < length &&
                Character.isWhitespace(content.charAt(charIdx)) &&
                !Character.isSpaceChar(content.charAt(charIdx))){

            charIdx++;
          }//parse break
          //add the break
          if(charIdx > tokenStart){
            fm = Transients.newFeatureMap();
            fm.put("String", content.substring(tokenStart, charIdx));
            fm.put("kind", "break");
            annotationSet.add(new Long(tokenStart), new Long(charIdx),
                              "SpaceToken", fm);
            tokenStart = charIdx;
          }
        }//parse whitespace

        //parse the text
        while(charIdx < length &&
              !Character.isWhitespace(content.charAt(charIdx))){
          currentChar = content.charAt(charIdx);

          if(Character.isDigit(currentChar)) containsDigits = true;
          else if(Character.isLetter(currentChar)){
            containsLetters = true;
            if(Character.isUpperCase(currentChar)){
              containsUpper = true;
              upperInitial = (charIdx == tokenStart);
            }else if(Character.isLowerCase(currentChar)) containsLower = true;
          }else if(currentChar == '-' ||
                   currentChar == '_') containsDash = true;
          else if(currentChar == '/' ||
                  currentChar == '\\') containsSlash = true;
          else if(currentChar == '.') containsPeriod = true;
          else if(currentChar == ',') containsComma = true;
          else containsUnknownChar = true;
          charIdx++;
        }//parse the text
        //add the token
        if(charIdx > tokenStart){
          fm = Transients.newFeatureMap();
          if(containsDigits){
            if(containsLetters)
              if(containsDash)
                if(containsUnknownChar) fm.put("kind", "containsDigitsAndLettersAndDashesAndOther");
                else fm.put("kind", "containsDigitsAndLettersAndDashes");
              else fm.put("kind", "containsDigitsAndLetters");
            else if(containsDash) fm.put("kind", "containsDigitsAndDash");
            else if(containsSlash) fm.put("kind", "containsDigitsAndSlash");
            else if(containsComma) fm.put("kind", "containsDigitsAndComma");
            else if(containsPeriod) fm.put("kind", "containsDigitsAndPeriod");
            else if(containsUnknownChar) fm.put("kind", "containsDigitsAndOther");
          }else{//does not contain digits
            if(containsLetters){
              if(containsDash) fm.put("kind","containsLettersAndDashes");
              else if(!containsSlash && !containsComma &&
                      !containsPeriod && !containsUnknownChar){
                fm.put("kind", "alpha");
                if(upperInitial) fm.put("orth", "initCap");
                else if(containsUpper)
                  if(containsLower) fm.put("orth","mixedCase");
                  else fm.put("orth","allCaps");
                else fm.put("orth", "lowerCase");

              }else fm.put("kind", "other");
            }else //does not contain letters or digits
              fm.put("kind", "other");
          }
          fm.put("String", content.substring(tokenStart, charIdx));          
          annotationSet.add(new Long(tokenStart), new Long(charIdx),
                            "Token", fm);
          tokenStart = charIdx;
          containsDigits = false;
          containsLetters = false;
          containsDash = false;
          containsSlash = false;
          containsComma = false;
          containsPeriod = false;
          containsUpper = false;
          containsLower = false;
          upperInitial = false;
          containsUnknownChar = false;
        }
      }while(charIdx < length);
    }catch(InvalidOffsetException ioe){
      ioe.printStackTrace(System.err);
    }


//    try{
//      for (int charIdx = 0; charIdx < content.length(); charIdx++){
//        currentChar = content.charAt(charIdx);

//      }
//    }catch(InvalidOffsetException ioe){
//      ioe.printStackTrace(System.err);
//    }
  }

  public void run_(){
    String content = doc.getContent().toString();
    BreakIterator bi = BreakIterator.getWordInstance();
    bi.setText(content);
    int start = bi.first();
    FeatureMap fm;
    try{
      for (int end = bi.next();
           end != BreakIterator.DONE;
           start = end, end = bi.next())
      {
        if(!Character.isWhitespace(content.charAt(start))){
          fm = Transients.newFeatureMap();
          fm.put("string", content.substring(start, end));
          annotationSet.add(new Long(start),
                            new Long(end),
                            "Token", fm);
        }
      }
    }catch(InvalidOffsetException ioe){
      ioe.printStackTrace(System.err);
    }
  }

  //ProcessProgressReporter implementation
  public void addProcessProgressListener(ProgressListener listener){
    myListeners.add(listener);
  }

  public void removeProcessProgressListener(ProgressListener listener){
    myListeners.remove(listener);
  }

  protected void fireProgressChangedEvent(int i){
    Iterator listenersIter = myListeners.iterator();
    while(listenersIter.hasNext())
      ((ProgressListener)listenersIter.next()).progressChanged(i);
  }

  protected void fireProcessFinishedEvent(){
    Iterator listenersIter = myListeners.iterator();
    while(listenersIter.hasNext())
      ((ProgressListener)listenersIter.next()).processFinished();
  }

  private FeatureMap features  = null;
  private List myListeners = new LinkedList();
  private Document doc;
  private AnnotationSet annotationSet;
}