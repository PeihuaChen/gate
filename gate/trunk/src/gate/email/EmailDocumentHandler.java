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
   It takes the gate document representing a list with e-mails and
   creates Gate annotations on it.
  */
public class EmailDocumentHandler implements StatusReporter{

  /**
    Constructor initialises some private fields
    */
  public EmailDocumentHandler(gate.Document aGateDocument, Map  aMarkupElementsMap,
                            Map anElement2StringMap){

    gateDocument = aGateDocument;
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
  /*
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
    long cursor = 0;
    boolean insideAnEmail = false;
    try{
      // read each line from the reader
      while ((line = gateDocumentReader.readLine()) != null){
        if (lineBeginsMessage(line)){
              // inform the status listener to fire only if no of elements processed
              // so far is a multiple of ELEMENTS_RATE
            if ((++ emails % EMAILS_RATE) == 0)
                fireStatusChangedEvent("Reading emails : " + emails);

            // the cursor is update with the length of the line + the new line char
            cursor += line.length() + 1;
            // we are inside an e-mail
            insideAnEmail = true;

            continue;
        }
        if (!insideAnEmail){
          // the cursor is update with the length of the line + the new line char
          cursor += line.length() + 1;
          continue;
        }
        // here we are inside an e-mail message
        if (lineIsHeader)
      }
    }catch (IOException e){
      e.printStackTrace(System.err);
    }
  */  
  }//annotateMessages

  /**
    Tests if the line begins an e-mail message
    @return true if the line begins an e-mail message
    @return false if is doesn't
  */
  private boolean lineBeginsMessage(String aTextLine){
    int score = 0;
    // if first token is "From" and the rest contains Day, Zone, etc
    // then this line begins a message
    // create a new String Tokenizer with " " as separator
    StringTokenizer tokenizer = new StringTokenizer(aTextLine," ");

    String firstToken = null;
    if (tokenizer.hasMoreTokens())
        firstToken = tokenizer.nextToken();
    else return false;

    if (!firstToken.equals("From"))
        return false;
    // else continue the analize
    while (tokenizer.hasMoreTokens()){
      String token = tokenizer.nextToken();
      token.trim();
      if (hasAMeaning(token))
          score += 1;
      else
          score += 0;
    }

    if (score == 5) return true;
    else return false;
  }//lineBeginsMessage

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
  }

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
    Initialises the collections
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

  }

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
  private gate.AnnotationSet basicAS;

  // this map marks the elements that we don't want to create annotations
  private Map  markupElementsMap = null;

  // this map marks the elements after we want to insert some strings
  private Map element2StringMap = null;

  // listeners for status report
  protected List myStatusListeners = new LinkedList();

  // this reports the the number of emails that have beed processed so far
  private int emails = 0;

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

