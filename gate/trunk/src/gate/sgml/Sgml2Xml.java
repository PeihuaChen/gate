/**
 *	Sgml2Xml.java
 *
 *	Cristian URSU,  4/July/2000
 *  $Id$
 */

package gate.sgml;

import java.util.*;
import java.io.*;


import gate.corpora.*;
import gate.util.*;
import gate.*;

/**
  Not so fast...
  This class is not a realy Sgml2Xml convertor.
  It takes an SGML document and tries to prepare it for an XML parser
  For a true conversion we need an Java SGML parser...
  If you know one let me know....

  What does it do:
  <ul>
    <li>If it finds something like this : &lt;element attribute = value&gt;
        it will produce: &lt;element attribute = "value"&gt;
    <li>If it finds something like this : &lt;element something attribute2=value&gt;
      it will produce : &lt;element defaultAttribute="something" attribute2="value"&gt;
    <li>If it finds : &lt;element att1='value1 value2' att2="value2 value3"&gt; it will
      produce: &lt;element att1="value1 value2" att2="value2 value3"&gt;
    <li>If it finds : &lt;element1&gt; &lt;elem&gt;text &lt;/element1&gt; will produce:
        &lt;element1&gt; &lt;elem&gt;text&lt;elem&gt; &lt;/element1&gt;
    <li>If it find : &lt;element1&gt; &lt;elem&gt;[white spaces]&lt;/element1&gt;, it will produce:
        &lt;element1&gt; &lt;elem/&gt;[white spaces]&lt;/element1&gt;
  </ul>
  What doesn't:
  <ul>
    <li>Doesn't expand the entities. So the entities from the SGML document must be
        resolved by the XML parser
    <li>Doesn't replace internal entities with their corresponding value
  </ul>

*/

public class Sgml2Xml{
  Document m_doc = null;
  StringBuffer m_modifier = null;

  Stack stack = null;
  List dubiousElements = null;
  Set whiteSpaces = null;
  int m_cursor = 0;
  int m_currState = 1;
  char m_currChar = ' ';

  public Sgml2Xml(String s){
    m_modifier = new StringBuffer(s);
    whiteSpaces = new HashSet();
    whiteSpaces.add(" ");
    whiteSpaces.add("\t");
    whiteSpaces.add("\n");
    dubiousElements = new ArrayList();
    stack = new Stack();
  }
  public Sgml2Xml(Document doc){
    m_doc = doc;

    m_modifier = new StringBuffer(m_doc.getContent().toString());
    /*
    whiteSpaces = new HashSet();
    whiteSpaces.add(" ");
    whiteSpaces.add("\t");
    whiteSpaces.add("\n");
    */
    dubiousElements = new ArrayList();
    stack = new Stack();
  }

  public static void main(String[] args){
    Sgml2Xml convertor = new Sgml2Xml("<w VVI>say <w VBZ>is\n<trunc> <w UNC>th </trunc>");
   /*
    Sgml2Xml convertor = new Sgml2Xml(

"<s n=16>\n" +
" <w PNP>I<w VBB>'m <w AJ0>supposed <w TO0>to <w ORD>first <w VVI>say <w VBZ>is\n"+
"<trunc> <w UNC>th </trunc> <w VBB>are <w PNP>you <w VVG>finding <w AT0>the\n"+
" <w NN1>course <w AV0>okay <w CJC>and <w TO0>to <w VVI>ask <w PNP>you <w PRP>about\n" +
" <w DPS>your <w NN2>projects<c PUN>.\n");
*/
    try{
      System.out.println(convertor.convert());
    } catch (Exception e){
      e.printStackTrace(System.err);
    }
  }

  public String convert()throws Exception{
    int charPos = 0;
    String elemName = null;
    int elemNameStart = 0;
    int elemNameEnd = 0;
    int closePos = 0;
    int attrStart = 0;
    int attrEnd = 0;

    while (thereAreCharsToBeProcessed()){
      // take the next char and increment the m_cursor
      m_currChar = read();
      switch(m_currState){
        case 1:
          if ('<' == m_currChar){
              // change state
              m_currState = 2;
              if (!stack.isEmpty()){
                // peek the element from the top of the stack
                CustomObject o = (CustomObject) stack.peek();
                // set some properties for this element
                if (charPos > 0){
                  // this is not an empty element because there is text that follows
                  // set the element from the top of the stack to be a non empty one
                  o.closePos = charPos;
                  o.empty = false;
                  // reset the charPos
                  charPos = 0;
                }// if (charPos > 0)
              }//if (!stack.isEmpty())
          }//if ('<' == m_currChar)

          // if m_currChar is not whiteSpace then save the position of the last
          // char that was read
          if (('<' != m_currChar) && !isWhiteSpace(m_currChar))
              charPos = m_cursor;
          break;

        case 2: // we read '<'
          if ('/' == m_currChar){
            // go to state 11
            m_currState = 11;
          }
          if (('/' != m_currChar) && !isWhiteSpace(m_currChar)){
              // save the position where starts the element's name
              elemNameStart = m_cursor -1;
              // go to state 3
              m_currState = 3;
          }
          break;

        case 3:// we just read the first char from the element's name
          if ( '>' == m_currChar ){
            // save the pos where the element's name ends
            elemNameEnd = m_cursor - 1;
            // this is also the pos where to insert '/' for empty elements in this case
            // we have this situation <w> sau < w>
            closePos = m_cursor - 1;
            // get the name of the element
            elemName = m_modifier.substring(elemNameStart,elemNameEnd);
            // we put the element into stack
            // we think in this point that the element is empty...
            performFinalAction(elemName, closePos);
            // go to state 1
            m_currState = 1;
          }
          if (isWhiteSpace(m_currChar)){
            // go to state 4
            m_currState = 4;
            // save the pos where the element's name ends
            elemNameEnd = m_cursor - 1;
            // get the name of the element
            elemName = m_modifier.substring(elemNameStart,elemNameEnd);
          }
          break;

        case 4://we read the name of the element and we prepare for '>' or attributes
          if ( '>' == m_currChar ){
            // this is also the pos where to insert '/' for empty elements in this case
            closePos = m_cursor -1 ;
            // we put the element into stack
            // we think in this point that the element is empty...
            performFinalAction(elemName, closePos);
            // go to state 1
            m_currState = 1;
          }
          if (( '>' != m_currChar ) && !isWhiteSpace(m_currChar)){
            // we just read the first char from the attrib name or attrib value..
            // go to state 5
            m_currState = 5;
            // remember the position where starts the attrib or the value of an attrib
            attrStart = m_cursor - 1;
          }
          break;

        case 5://
          if ( '=' == m_currChar )
                       m_currState = 6;
          if ( '>' == m_currChar ){
            // this mean that the attribute was a value and we have to create
            // a default attribute
            // the same as in state 10
            attrEnd = m_cursor -1 ;
            m_modifier.insert(attrEnd,'"');
            m_modifier.insert(attrStart,"defaultAttr=\"");
            // go to state 4
            m_currState = 4;
            // parse again the entire sequence
            m_cursor = attrStart;
          }
          if (isWhiteSpace(m_currChar)){
             m_currState = 10;
             attrEnd = m_cursor -1;
          }
          break;
        case 6:
          if ( ('\'' == m_currChar) || ('"' == m_currChar) ){
            if ('\'' == m_currChar){
              // we have to replace ' with "
              m_modifier = m_modifier.replace(m_cursor - 1, m_cursor,"\"");
            }
            m_currState = 7;
          }
          if ( ('\'' != m_currChar) && ('"' != m_currChar) && !isWhiteSpace(m_currChar)){
            // this means that m_curChar is any char
            m_currState = 8;
            m_modifier.insert(m_cursor - 1, '"');
            m_cursor ++;
          }
          break;

        case 7:
          if ( ('\'' == m_currChar) || ('"' == m_currChar) ){
            if ('\'' == m_currChar){
              // we have to replace ' with "
              m_modifier = m_modifier.replace(m_cursor - 1, m_cursor,"\"");
            }
            m_currState = 9;
          }
          break;

        case 8:
          if ('>' == m_currChar){
            m_currState = 1;
            m_modifier.insert(m_cursor - 1, '"');
            m_cursor ++;
            performFinalAction(elemName, m_cursor - 1);
          }
          if (isWhiteSpace(m_currChar)){
            m_currState = 9;
            m_modifier.insert(m_cursor - 1, '"');
            m_cursor ++;
          }
          break;

        case 9:
          if ('>' == m_currChar){
            m_currState = 1;
            performFinalAction(elemName, m_cursor - 1);
          }
          if (('>' != m_currChar) && !isWhiteSpace(m_currChar)){
            // this is the same as state 4->5
            m_currState = 5;
            attrStart = m_cursor - 1;
          }
          break;

        case 10:
          if ('=' == m_currChar) m_currState = 6;
          if ( ('=' != m_currChar) && !isWhiteSpace(m_currChar)){
            // this mean that the attribute was a value and we have to create
            // a default attribute
            m_modifier.insert(attrEnd,'"');
            m_modifier.insert(attrStart,"defaultAttr=\"");
            m_currState = 4;
            m_cursor = attrStart;
          }
          break;

        case 11:
          if (!isWhiteSpace(m_currChar)){
             m_currState = 12;
             elemNameStart = m_cursor - 1;
          }
          break;

        case 12:
          if ('>' == m_currChar){
            elemNameEnd = m_cursor - 1;
            elemName = m_modifier.substring(elemNameStart,elemNameEnd);
            performActionWithEndElem(elemName);
            m_currState = 1;
          }
          if (isWhiteSpace(m_currChar)){
            m_currState = 13;
            elemNameEnd = m_cursor - 1;
          }
          break;

        case 13:
          if ('>' == m_currChar){
            elemName = m_modifier.substring(elemNameStart,elemNameEnd);
            performActionWithEndElem(elemName);
            m_currState = 1;
          }
          break;

      }// switch(m_currState)
    }// while (thereAreCharsToBeProcessed())

    while (!stack.isEmpty()){
      CustomObject o = (CustomObject) stack.pop();
      dubiousElements.add(o);
    }

    // sort the dubiousElements list descending on closePos...
    Collections.sort(dubiousElements, new MyComparator ());

    ListIterator listIterator = dubiousElements.listIterator();
    while (listIterator.hasNext()){
      CustomObject o = (CustomObject) listIterator.next();
      makeFinalModifications(o);
    }
    //System.out.println(m_modifier.toString());

    // get a InputStream from m_modifier and write it into a temp file
    // finally return the URI of the new XML document
    ByteArrayInputStream is = new ByteArrayInputStream(m_modifier.toString().getBytes());
    File file = Files.writeTempFile(is);

    //return m_doc.getSourceURL().toString();
    return file.toURL().toString();
  }// convert()

  private boolean thereAreCharsToBeProcessed(){
    if (m_cursor < m_modifier.length()) return true;
    else return false;
  }

  private char read(){
    return m_modifier.charAt(m_cursor ++);
  }

  private void performFinalAction(String elemName, int pos){
    CustomObject o = new CustomObject();
    o.elemName = elemName;
    o.closePos = pos;
    o.empty = true;
    stack.push( o );
  }

  private void performActionWithEndElem(String elemName){
    CustomObject o = null;
    boolean stop = false;
    while (!stack.isEmpty() && !stop){
      o = (CustomObject) stack.pop();
      if (o.elemName.equalsIgnoreCase(elemName)) stop = true;
      else dubiousElements.add(o);
    }
  }

  private void makeFinalModifications(CustomObject o){
    String strElem = null;
    if (true == o.empty)
        m_modifier.insert(o.closePos,"/");
    else{
      strElem = "</" + o.elemName + ">";
      m_modifier.insert(o.closePos,strElem);
    }
  }

  private boolean isWhiteSpace(char c){
    //if (whiteSpaces.contains(new Character(c).toString())) return true;
    //else return false;
    return Character.isWhitespace(c);
  }
}

/*
 * The objects belonging to this class are used inside the stack
 */
class  CustomObject{

  // data fields
  public String elemName = null;
  public int closePos = 0;
  public boolean empty = false;

  // constructor
  public CustomObject(){
  }

}// MyCustomObject

class MyComparator implements Comparator{

      public MyComparator(){
      }
      public int compare(Object o1, Object o2){
        if ( !(o1 instanceof CustomObject) ||
             !(o2 instanceof CustomObject)) return 0;

        CustomObject co1 = (CustomObject) o1;
        CustomObject co2 = (CustomObject) o2;
        int result = 0;
        if (co1.closePos < co2.closePos)  result = -1;
        if (co1.closePos == co2.closePos) result = 0;
        if (co1.closePos > co2.closePos)  result = 1;

        return -result;
      }//compare
}//class MyComparator

