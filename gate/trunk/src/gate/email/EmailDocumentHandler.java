/**
 *	EmailDocumentHandler.java
 *
 *	Cristian URSU,  3/Aug/2000
 *  $Id$
 */

package gate.email;

import java.util.*;
import java.io.*;

import gate.corpora.*;
import gate.util.*;
import gate.*;
import gate.gui.*;


  /**
   Implements the behaviour of the Email reader
   It takes the Gate Document representing a list with e-mails and
   creates Gate annotations on it.
  */
public class EmailDocumentHandler implements StatusReporter{

  /**
    Constructor initialises some private fields
  */
  public EmailDocumentHandler(gate.Document aGateDocument, Map  aMarkupElementsMap,
                            Map anElement2StringMap){

    gateDocument = aGateDocument;
    // gets AnnotationSet based on the new gate document
    basicAS = aGateDocument.getAnnotations();
    markupElementsMap = aMarkupElementsMap;
    element2StringMap = anElement2StringMap;
    setUp();
  }

  /**
    Reads the Gate Document line by line and does the folowing things:
    <ul>
    <li> Each line is analized in order to detect where an e-mail starts.
    <li> If the line belongs to an e-mail header then creates the annotation if
         the markupElementsMap allows that.
    <li> Lines belonging to the e-mail body are placed under a Gate annotation
         called messageBody.
    </ul>
  */
  public void annotateMessages(){
    // obtain a BufferedReader form the Gate document...
    BufferedReader gateDocumentReader = null;
    try{
      gateDocumentReader = new BufferedReader(new InputStreamReader(
              gateDocument.getSourceURL().openConnection().getInputStream()));
    } catch (IOException e){
      e.printStackTrace();
    }
    // for each line read from the gateDocumentReader do
      // if the line begins an e-mail message then fire a status listener, mark
      // that we are processing an e-mail, update the cursor and go to the next
      // line.

      // if we are inside an e-mail, test if the line belongs to the message header
      // if so, create a header field annotation.

      // if we are inside a a body and this is the first line from the body,
      // create the message body annotation.
      // Otherwise just update the cursor and go to the next line

      // if the line doesn't belong to an e-mail message then just update the cursor.
    // next line

    String line = null;
    String aFieldName = null;
    long cursor = 0;
    long endEmail = 0;
    long startEmail = 0;
    long endHeader = 0;
    long startHeader = 0;
    long endBody = 0;
    long startBody = 0;
    long endField = 0;
    long startField = 0;
    boolean insideAnEmail   = false;
    boolean insideHeader    = false;
    boolean insideBody      = false;
    boolean emailReadBefore = false;
    boolean fieldReadBefore = false;
    long nlSize = detectNLSize();
    //System.out.println("NL SIZE = " + nlSize);
    try{
      // read each line from the reader
      while ((line = gateDocumentReader.readLine()) != null){
        // Here we test if the line delimitates two e-mail messages.
        // Each e-mail message begins with a line like this:
        // From P.Fairhurst Thu Apr 18 12:22:23 1996
        // Method lineBeginsMessage() detects such lines.
        if (lineBeginsMessage(line)){
              // inform the status listener to fire only if no. of elements processed
              // so far is a multiple of ELEMENTS_RATE
            if ((++ emails % EMAILS_RATE) == 0)
                fireStatusChangedEvent("Reading emails : " + emails);
            // if there are e-mails read before then the previous e-mail end here
            if (true == emailReadBefore){
              // cursor points at the beggining of the line
              // e-mail and Body ends before the \n char
              // email ends as cursor value indicates
              endEmail = cursor - nlSize ;
              // also the e-mail body ends when an e-mail ends
              endBody = cursor - nlSize;

              //System.out.println("Email ends at :" + endEmail);
              //System.out.println("EMAIL IS: ****************************************************");
              //System.out.println(gateDocument.getContent().toString().substring(startEmail,endEmail));
              //Annotate an E-mail body (startBody, endEmail)
              createAnnotation("Body",startBody,endBody,null);
              //Annotate an E-mail message(startEmail, endEmail) Email starts
              createAnnotation("Message",startEmail,endEmail,null);
            }
            // if no e-mail was read before, now there is at list one message read
            emailReadBefore = true;
            // the cursor is updated with the length of the line + the new line char
            cursor += line.length() + nlSize;
            // E-mail starts imediately after this line which sepatates 2 messages
            startEmail = cursor;
            // E-mail header starts also from here
            startHeader = cursor;
            //System.out.println("Email starts at :"+ startEmail + " With LINE: " + line);
            // we are inside an e-mail
            insideAnEmail = true;
            // next is the E-mail header
            insideHeader = true;
            // no field inside header has been read before
            fieldReadBefore = false;
            // read the next line
            continue;
        }//if (lineBeginsMessage(line))
        if (false == insideAnEmail){
          // the cursor is update with the length of the line + the new line char
          cursor += line.length() + nlSize;
          // read the next line
          continue;
        }//if
        // here we are inside an e-mail message (inside Header or Body)
        if (true == insideHeader){
          // E-mail spec sais that E-mail header is separated by E-mail body
          // by a \n char
          if (line.equals("")){
            // this \n sepatates the header of an e-mail form its body
            // If we are here it means that the header has ended.
            insideHeader  = false;
            // e-mail header ends here
            endHeader = cursor - nlSize;
            // update the cursor with the length of \n
            cursor += line.length() + nlSize;
            // E-mail body starts from here
            startBody = cursor;
            // if fields were read before, it means that the e-mail has a header
            if (true == fieldReadBefore){
              endField = endHeader;
              //Create a field annotation (fieldName, startField, endField)
              createAnnotation(aFieldName, startField, endField, null);
              //Create an e-mail header annotation
              createAnnotation("Header",startHeader,endHeader,null);
            }//if
            // read the next line
            continue;
          }//if (line.equals(""))
          // if line begins with a field then prepare to create an
          // annotation with the name of the field
          if (lineBeginsWithField(line)){
            // if a field was read before, it means that the previous field ends
            // here
            if (true == fieldReadBefore){
              // the previous field end here
              endField = cursor - nlSize;
              //Create a field annotation (fieldName, startField, endField)
              createAnnotation(aFieldName, startField, endField, null);
            }//if
            fieldReadBefore = true;
            aFieldName = getFieldName();
            startField = cursor + aFieldName.length() + ":".length();
          }//if
          // in both cases the cursor is updated and read the next line
          // the cursor is update with the length of the line + the new line char
          cursor += line.length() + nlSize;
          // read the next line
          continue;
        }//if (true == insideHeader)
        // here we are inside the E-mail body
        // the body will end when the e-mail will end.
        // here we just update the cursor
        cursor += line.length() + nlSize;
      }//while
      // it might be possible that the file to contain only one e-mail and
      // if the file contains only one e-mail message then the variable
      // emailReadBefore must be set on true value
      if (true == emailReadBefore){
        endBody  = cursor - nlSize;
        endEmail = cursor - nlSize;
        //System.out.println("Email ends at :" + endEmail);
        //System.out.println("EMAIL IS: ****************************************************");
        //Annotate an E-mail body (startBody, endEmail)
        createAnnotation("Body",startBody,endBody,null);
        //Annotate an E-mail message(startEmail, endEmail) Email starts
        createAnnotation("Message",startEmail,endEmail,null);
        //System.out.println(gateDocument.getContent().toString().substring(startEmail,endEmail));
      }
      // if emailReadBefore is not set on true, that means that we didn't
      // encounter any line like this:
      // From P.Fairhurst Thu Apr 18 12:22:23 1996
    }catch (IOException e){
      e.printStackTrace(System.err);
    }
  }//annotateMessages

  /**
    This method detects if the text file which contains e-mail messages is under
    MSDOS or UNIX format.
    Under MSDOS the size of NL is 2 (\n \r) and under UNIX (\n) the size is 1
    @return the size of the NL (1,2 or 0 = if no \n is found)
  */
  private int detectNLSize(){
   //*
    // get a char array
    char[] document = null;
    // transform the gate Document into a char array
    document = gateDocument.getContent().toString().toCharArray();
    // search for the \n char
    // when it is found test if is followed by the \r char
    for (int i=0; i<document.length; i++){
      if (document[i] == '\n'){
        // we just found a \n char.
        // here we test if is followed by a \r char or preceded by a \r char
        if (
            (((i+1) < document.length) && (document[i+1] == '\r'))
            ||
            (((i-1) >= 0)              && (document[i-1] == '\r'))
           ) return 2;
        else return 1;
      }
    }
    //if no \n char is found then the document is contained into a single text
    // line.
    return 0;
   //*/
  }//detectNLSize

  /**
    This method creates a gate annotation given its name, start, end and feature
    map.
  */
  private void createAnnotation(String anAnnotationName, long anAnnotationStart,
                                 long anAnnotationEnd, FeatureMap aFeatureMap){
    if (aFeatureMap == null)
        aFeatureMap = new SimpleFeatureMapImpl();                              
    try{
      basicAS.add(new Long(anAnnotationStart), new Long(anAnnotationEnd),
                  anAnnotationName, aFeatureMap
                  );
    }catch (gate.util.InvalidOffsetException e){
      //System.out.println("NAME=" + anAnnotationName + " START=" + anAnnotationStart + " STOP=" + anAnnotationEnd + " DOC SIZE=" + gateDocument.getContent().size());
      e.printStackTrace(System.err);
    }
  }//createAnnotation

  /**
    Tests if the line begins an e-mail message
    @param aTextLine a line from the file containing the e-mail messages
    @return true if the line begins an e-mail message
    @return false if is doesn't
  */
  private boolean lineBeginsMessage(String aTextLine){
    int score = 0;
    // if first token is "From" and the rest contains Day, Zone, etc
    // then this line begins a message
    // create a new String Tokenizer with " " as separator
    StringTokenizer tokenizer = new StringTokenizer(aTextLine," ");
    // get the first token
    String firstToken = null;
    if (tokenizer.hasMoreTokens())
        firstToken = tokenizer.nextToken();
    else return false;
    // trim it
    firstToken.trim();
    // check against "From" word
    // if the first token is not From then the entire line can not begin a message
    if (!firstToken.equals("From"))
        return false;
    // else continue the analize
    while (tokenizer.hasMoreTokens()){
      // get the next token
      String token = tokenizer.nextToken();
      token.trim();
      // see if it has a meaning ( analize if is a Day, Month,Zone, Time, Year )
      if (hasAMeaning(token))
          score += 1;
    }
    // a score greather or equql with 5 means that this line begins a message
    if (score >= 5) return true;
    else return false;
  }//lineBeginsMessage

  /**
    Tests if the line begins with a field from the e-mail header
    If the answer is true then it also sets the member fieldName with the
    value of this e-mail header field.
    @param aTextLine a line from the file containing the e-mail text
    @return true if the line begins with a field from the e-mail header
    @return false if is doesn't
  */
  private boolean lineBeginsWithField(String aTextLine){
    if (containsSemicolon(aTextLine)){
      StringTokenizer tokenizer = new StringTokenizer(aTextLine,":");
      // get the first token
      String firstToken = null;
      if (tokenizer.hasMoreTokens())
        firstToken = tokenizer.nextToken();
      else return false;
      if (firstToken != null){
        // trim it
        firstToken.trim();
        if (containsWhiteSpaces(firstToken)) return false;
        // set the member field
        fieldName = firstToken;
      }
      return true;
    }else return false;
  }//lineBeginsWithField

  /**
    This method checks if a String contains white spaces.
  */
  private boolean containsWhiteSpaces(String aString){
    for (int i = 0; i<aString.length(); i++)
      if (Character.isWhitespace(aString.charAt(i))) return true;
    return false;
  }//containsWhiteSpaces

  /**
    This method checks if a String contains a semicolon char
  */
  private boolean containsSemicolon(String aString){
    for (int i = 0; i<aString.length(); i++)
      if (aString.charAt(i) == ':') return true;
    return false;
  }//containsSemicolon

  /**
    This method tests a token if is Day, Month, Zone, Time, Year
  */
  private boolean hasAMeaning(String aToken){
    // if token is a Day return true
    if (day.contains(aToken)) return true;

    // if token is a Month return true
    if (month.contains(aToken)) return true;

    // if token is a Zone then return true
    if (zone.contains(aToken)) return true;

    // test if is a day number or a year
    Integer dayNumberOrYear = null;
    try{
      dayNumberOrYear = new Integer(aToken);
    } catch (NumberFormatException e){
      dayNumberOrYear = null;
    }
    // if the creation succeded, then test if is day or year
    if (dayNumberOrYear != null){
      int number = dayNumberOrYear.intValue();
      // if is a number between 1 and 31 then is a day
      if ((number > 0) && (number < 32)) return true;
      // if is a number between 1900 si 3000 then is a year ;))
      if ((number > 1900) && (number < 3000)) return true;
      // it might be the last two digits of 19xx
      if ((number >= 0) && (number <= 99)) return true;
    }
    // test if is time: hh:mm:ss
    if (isTime(aToken)) return true;
   return false;
  }//hasAMeaning

  /**
    Tests a token if is in time format HH:MM:SS
  */
  private boolean isTime(String aToken){
    StringTokenizer st = new StringTokenizer(aToken,":");
    // test each token if is hour, minute or second
    String hourString = null;
    if (st.hasMoreTokens())
        hourString = st.nextToken();
    // if there are no more tokens, it means that is not a time
    if (hourString == null) return false;
    // test if is a number between 0 and 23
    Integer hourInteger = null;
    try{
      hourInteger = new Integer(hourString);
    } catch (NumberFormatException e){
      hourInteger = null;
    }
    if (hourInteger == null) return false;
    // if is not null then it means is a number
    // test if is in 0 - 23 range
    // if is not in this range then is not an hour
    int hour = hourInteger.intValue();
    if ( (hour < 0) || (hour > 23) ) return false;
    // we have the hour
    // now repeat the test for minute and seconds

    // minutes
    String minutesString = null;
    if (st.hasMoreTokens())
        minutesString = st.nextToken();
    // if there are no more tokens (minutesString == null) then return false
    if (minutesString == null) return false;
    // test if is a number between 0 and 59
    Integer minutesInteger = null;
    try{
      minutesInteger = new Integer (minutesString);
    } catch (NumberFormatException e){
      minutesInteger = null;
    }
    if (minutesInteger == null) return false;
    // if is not null then it means is a number
    // test if is in 0 - 59 range
    // if is not in this range then is not a minute
    int minutes = minutesInteger.intValue();
    if ( (minutes < 0) || (minutes > 59) ) return false;

    // seconds
    String secondsString = null;
    if (st.hasMoreTokens())
        secondsString = st.nextToken();
    // if there are no more tokens (secondsString == null) then return false
    if (secondsString == null) return false;
    // test if is a number between 0 and 59
    Integer secondsInteger = null;
    try{
      secondsInteger = new Integer (secondsString);
    } catch (NumberFormatException e){
      secondsInteger = null;
    }
    if (secondsInteger == null) return false;
    // if is not null then it means is a number
    // test if is in 0 - 59 range
    // if is not in this range then is not a minute
    int seconds = secondsInteger.intValue();
    if ( (seconds < 0) || (seconds > 59) ) return false;

    // if there are more tokens in st it means that we don't have this format:
    // HH:MM:SS
    if (st.hasMoreTokens()) return false;

    // if we are here it means we have a time
    return true;
  }// isTime

  /**
    Initialises the collections with data used by method lineBeginsMessage()
  */
  private void setUp(){
    day = new HashSet();
    day.add("Mon");
    day.add("Tue");
    day.add("Wed");
    day.add("Thu");
    day.add("Fri");
    day.add("Sat");
    day.add("Sun");

    month = new HashSet();
    month.add("Jan");
    month.add("Feb");
    month.add("Mar");
    month.add("Apr");
    month.add("May");
    month.add("Jun");
    month.add("Jul");
    month.add("Aug");
    month.add("Sep");
    month.add("Oct");
    month.add("Nov");
    month.add("Dec");

    zone = new HashSet();
    zone.add("UT");
    zone.add("GMT");
    zone.add("EST");
    zone.add("EDT");
    zone.add("CST");
    zone.add("CDT");
    zone.add("MST");
    zone.add("MDT");
    zone.add("PST");
    zone.add("PDT");
  }//setUp
  
  /**
    This method returns the value of the member fieldName.
    fieldName is set by the method lineBeginsWithField(String line).
    Each time the the line begins with a field name, that fiels will be stored
    in this member.
  */
  private String getFieldName(){
    if (fieldName == null) return new String("");
    else return fieldName;
  }//getFieldName

  //StatusReporter Implementation

  /**
    This methos is called when a listener is registered with this class
  */
  public void addStatusListener(StatusListener listener){
    myStatusListeners.add(listener);
  }
  /**
    This methos is called when a listener is removed
  */
  public void removeStatusListener(StatusListener listener){
    myStatusListeners.remove(listener);
  }
  /**
    This methos is called whenever we need to inform the listener about an event
  */
  protected void fireStatusChangedEvent(String text){
    Iterator listenersIter = myStatusListeners.iterator();
    while(listenersIter.hasNext())
      ((StatusListener)listenersIter.next()).statusChanged(text);
  }

  private static final int EMAILS_RATE = 16;
  // the content of the e-mail document, without any tag
  // for internal use
  private String tmpDocContent = null;


  // a gate document
  private gate.Document gateDocument = null;

  // an annotation set used for creating annotation reffering the doc
  private gate.AnnotationSet basicAS = null;

  // this map marks the elements that we don't want to create annotations
  private Map  markupElementsMap = null;

  // this map marks the elements after we want to insert some strings
  private Map element2StringMap = null;

  // listeners for status report
  protected List myStatusListeners = new LinkedList();

  // this reports the the number of emails that have beed processed so far
  private int emails = 0;

  // this is set by the method lineBeginsWithField(String line)
  // each time the the line begins with a field name, that fiels will be stored
  // in this member.
  private String fieldName = null;

  private Collection day = null;
  private Collection month = null;
  private Collection zone = null;

} //EmailDocumentHandler

/*
class Position {
  public Position(){
  }

  public void setBegin(long aBeginOffset){
    begin = aBeginOffset;
  }
  public void setEnd(long anEndOffset){
    end = anEndOffset;
  }

  public long getBegin(){
    return begin;
  }
  public long getEnd(){
    return end;
  }

  private long begin = 0;
  private long end = 0;
}
*/

