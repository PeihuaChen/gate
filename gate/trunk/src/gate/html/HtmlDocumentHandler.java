/*
 *  HtmlDocumentHandler.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU,  12/June/2000
 *
 *  $Id$
 */

package gate.html;

import javax.swing.text.html.*;
import javax.swing.text.html.parser.*;
import javax.swing.text.html.HTMLEditorKit.*;
import javax.swing.text.*;

import java.util.*;

import gate.corpora.*;
import gate.util.*;
import gate.*;
import gate.event.*;


/** Implements the behaviour of the HTML reader.
  * Methods of an object of this class are called by the HTML parser when
  * events will appear.
  * The idea is to parse the HTML document and construct Gate annotations
  * objects.
  * This class also will replace the content of the Gate document with a
  * new one containing anly text from the HTML document.
  */
public class HtmlDocumentHandler extends ParserCallback {

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Constructor initialises all the private memeber data.
    * This will use the default annotation set taken from the gate document.
    * @param aDocument The gate document that will be processed
    * @param aMarkupElementsMap The map containing the elements that will
    * transform into annotations
    */
  public HtmlDocumentHandler(gate.Document aDocument, Map aMarkupElementsMap) {
    this(aDocument,aMarkupElementsMap,null);
  }

  /** Constructor initialises all the private memeber data
    * @param aDocument The gate document that will be processed
    * @param aMarkupElementsMap The map containing the elements that will
    * transform into annotations
    * @param anAnnoatationSet The annotation set that will contain annotations
    * resulted from the processing of the gate document
    */
  public HtmlDocumentHandler(gate.Document       aDocument,
                             Map                 aMarkupElementsMap,
                             gate.AnnotationSet  anAnnotationSet) {
    // init stack
    stack = new java.util.Stack();

    // this string contains the plain text (the text without markup)
    tmpDocContent = new StringBuffer("");

    // colector is used later to transform all custom objects into
    // annotation objects
    colector = new LinkedList();

    // the Gate document
    doc = aDocument;

    // this map contains the elements name that we want to create
    // if it's null all the elements from the XML documents will be transformed
    // into Gate annotation objects
    markupElementsMap = aMarkupElementsMap;

    // init an annotation set for this gate document
    basicAS = anAnnotationSet;
  }

  /** This method is called when the HTML parser encounts the beginning
    * of a tag that means that the tag is paired by an end tag and it's
    * not an empty one.
    */
  public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
    // Fire the status listener if the elements processed exceded the rate
    if (0 == (++elements % ELEMENTS_RATE))
        fireStatusChangedEvent("Processed elements : " + elements);

    // Construct a feature map from the attributes list
    FeatureMap fm = new SimpleFeatureMapImpl();

    // Take all the attributes an put them into the feature map
    if (0 != a.getAttributeCount()){
      Enumeration enum = a.getAttributeNames();
      while (enum.hasMoreElements()){
        Object attribute = enum.nextElement();
        fm.put(attribute.toString(),(a.getAttribute(attribute)).toString());
      }// while
    }// if

    // Just analize the tag t and add some\n chars and spaces to the
    // tmpDocContent.The reason behind is that we need to have a readable form
    // for the final document.
    customizeAppearanceOfDocumentWithStartTag(t);

    // If until here the "tmpDocContent" ends with a NON whitespace char,
    // then we add a space char before calculating the START index of this
    // tag.
    // This is done in order not to concatenate the content of two separate tags
    // and obtain a different NEW word.
    int tmpDocContentSize = tmpDocContent.length();
    if ( tmpDocContentSize != 0 &&
         !Character.isWhitespace(tmpDocContent.charAt(tmpDocContentSize - 1))
       ) tmpDocContent.append(" ");

    // create the start index of the annotation
    Long startIndex = new Long(tmpDocContent.length());

    // initialy the start index is equal with the End index
    CustomObject obj = new CustomObject(t.toString(),fm,startIndex,startIndex);

    // put it into the stack
    stack.push (obj);

  }//handleStartTag

   /** This method is called when the HTML parser encounts the end of a tag
     * that means that the tag is paired by a beginning tag
     */
  public void handleEndTag(HTML.Tag t, int pos){
    // obj is for internal use
    CustomObject obj = null;

    // if the stack is not empty then we get the object from the stack
    if (!stack.isEmpty()){
      obj = (CustomObject) stack.pop();

      // we add it to the colector
      colector.add(obj);
    }

    // If element has text between, then customize its apearance
    if ( obj != null &&
         obj.getStart().longValue() != obj.getEnd().longValue()
       )
      // Customize the appearance of the document
      customizeAppearanceOfDocumentWithEndTag(t);

    // if t is the </HTML> tag then we reached the end of theHTMLdocument
    if (t == HTML.Tag.HTML){
      // replace the old content with the new one
      doc.setContent (new DocumentContentImpl(tmpDocContent.toString()));

      // If basicAs is null then get the default annotation
      // set from this gate document
      if (basicAS == null)
        basicAS = doc.getAnnotations();

      // iterate through colector and construct annotations
      Iterator anIterator = colector.iterator();
      while (anIterator.hasNext()){
        obj = (CustomObject) anIterator.next();
          // Construct an annotation from this obj
          try{
            if (markupElementsMap == null){
               basicAS.add( obj.getStart(),
                            obj.getEnd(),
                            obj.getElemName(),
                            obj.getFM()
                           );
            }else{
              String annotationType =
                     (String) markupElementsMap.get(obj.getElemName());
              if (annotationType != null)
                 basicAS.add( obj.getStart(),
                              obj.getEnd(),
                              annotationType,
                              obj.getFM()
                             );
            }
          }catch (InvalidOffsetException e){
              Err.prln("Error creating an annot :" + obj + " Discarded...");
          }// end try
//        }// end if
      }//while

      // notify the listener about the total amount of elements that
      // has been processed
      fireStatusChangedEvent("Total elements : " + elements);

    }//else

  }//handleEndTag

  /** This method is called when the HTML parser encounts an empty tag
    */
  public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos){
    // fire the status listener if the elements processed exceded the rate
    if ((++elements % ELEMENTS_RATE) == 0)
       fireStatusChangedEvent("Processed elements : " + elements);

    // construct a feature map from the attributes list
    // these are empty elements
    FeatureMap fm = new SimpleFeatureMapImpl();

    // take all the attributes an put them into the feature map
    if (0 != a.getAttributeCount ()){

       // Out.println("HAS  attributes = " + a.getAttributeCount ());
        Enumeration enum = a.getAttributeNames ();
        while (enum.hasMoreElements ()){
          Object attribute = enum.nextElement ();
          fm.put ( attribute.toString(),(a.getAttribute(attribute)).toString());

        }//while

    }//if

    // create the start index of the annotation
    Long startIndex = new Long(tmpDocContent.length ());

    // initialy the start index is equal with the End index
    CustomObject obj = new CustomObject(t.toString(),fm,startIndex,startIndex);

    // we add the object directly into the colector
    // we don't add it to the stack because this is an empty tag
    colector.add(obj);

    // Just analize the tag t and add some\n chars and spaces to the
    // tmpDocContent.The reason behind is that we need to have a readable form
    // for the final document.
    customizeAppearanceOfDocumentWithSimpleTag(t);

  } // handleSimpleTag

  /** This method is called when the HTML parser encounts text (PCDATA)
    */
  public void handleText(char[] text, int pos){
    // create a string object from this text array
    String content = new String(text);
    int tmpDocContentSize = 0;
    tmpDocContentSize = tmpDocContent.length();
/*
    // If the first char of the text just read "text[0]" is NOT whitespace AND
    // the last char of the tmpDocContent[SIZE-1] is NOT whitespace then
    // concatenation "tmpDocContent + content" will result into a new different
    // word... and we want to avoid that...
    if (tmpDocContentSize != 0)
     if (!Character.isWhitespace(content.charAt(0)) &&
      !Character.isWhitespace(tmpDocContent.charAt(tmpDocContentSize - 1)))
          content = " " + content;
*/
    CustomObject obj = null;
    Long end = new Long(tmpDocContentSize + content.length());

    // modify all the elements from the stack with the new calculated end index
    Iterator iterator = stack.iterator ();
    while (iterator.hasNext()){
      obj = (CustomObject) iterator.next();
      obj.setEnd(end);
    }

    // update the document content
    tmpDocContent.append(content);
  }

  /** This method analizes the tag t and adds some \n chars and spaces to the
    * tmpDocContent.The reason behind is that we need to have a readable form
    * for the final document. This method modifies the content of tmpDocContent.
    * @param t the Html tag encounted by the HTML parser
    */
  protected void customizeAppearanceOfDocumentWithSimpleTag(HTML.Tag t){
    // if the HTML tag is BR then we add a new line character to the document
    if (HTML.Tag.BR == t)
      tmpDocContent.append("\n");
  }// customizeAppearanceOfDocumentWithSimpleTag

  /** This method analizes the tag t and adds some \n chars and spaces to the
    * tmpDocContent.The reason behind is that we need to have a readable form
    * for the final document. This method modifies the content of tmpDocContent.
    * @param t the Html tag encounted by the HTML parser
    */
  protected void customizeAppearanceOfDocumentWithStartTag(HTML.Tag t){
//*
    if (HTML.Tag.P == t){
      int tmpDocContentSize = tmpDocContent.length();
      if ( tmpDocContentSize >= 2 &&
           '\n' != tmpDocContent.charAt(tmpDocContentSize - 2)
         ) tmpDocContent.append("\n");
    }// End if
//*/
  }// customizeAppearanceOfDocumentWithStartTag

  /** This method analizes the tag t and adds some \n chars and spaces to the
    * tmpDocContent.The reason behind is that we need to have a readable form
    * for the final document. This method modifies the content of tmpDocContent.
    * @param t the Html tag encounted by the HTML parser
    */
  protected void customizeAppearanceOfDocumentWithEndTag(HTML.Tag t){
    // if the HTML tag is BR then we add a new line character to the document
    if ( (HTML.Tag.P == t) ||

         (HTML.Tag.H1 == t) ||
         (HTML.Tag.H2 == t) ||
         (HTML.Tag.H3 == t) ||
         (HTML.Tag.H4 == t) ||
         (HTML.Tag.H5 == t) ||
         (HTML.Tag.H6 == t) ||
         (HTML.Tag.TR == t) ||
         (HTML.Tag.CENTER == t) ||
         (HTML.Tag.LI == t)
       ) tmpDocContent.append("\n");

    if (HTML.Tag.TITLE == t)
      tmpDocContent.append("\n\n");
  }// customizeAppearanceOfDocumentWithEndTag

  /**
    * This method is called when the HTML parser encounts an error
    * it depends on the programmer if he wants to deal with that error
    */
  public void handleError(String errorMsg, int pos) {
    //Out.println ("ERROR CALLED : " + errorMsg);
  }

  /** This method is called once, when the HTML parser reaches the end
    * of its input streamin order to notify the parserCallback that there
    * is nothing more to parse.
    */
  public void flush() throws BadLocationException{
  }// flush

  /** This method is called when the HTML parser encounts a comment
    */
  public void handleComment(char[] text, int pos) {
  }

  //StatusReporter Implementation

  public void addStatusListener(StatusListener listener) {
    myStatusListeners.add(listener);
  }

  public void removeStatusListener(StatusListener listener) {
    myStatusListeners.remove(listener);
  }

  protected void fireStatusChangedEvent(String text) {
    Iterator listenersIter = myStatusListeners.iterator();
    while(listenersIter.hasNext())
      ((StatusListener)listenersIter.next()).statusChanged(text);
  }

  /**
    * This method verifies if data contained by the CustomObject can be used
    * to create a GATE annotation.
    */
/*  private boolean canCreateAnnotation(CustomObject aCustomObject){
    long start            = aCustomObject.getStart().longValue();
    long end              = aCustomObject.getEnd().longValue();
    long gateDocumentSize = doc.getContent().size().longValue();

    if (start < 0 || end < 0 ) return false;
    if (start > end ) return false;
    if ((start > gateDocumentSize) || (end > gateDocumentSize)) return false;
    return true;
  }// canCreateAnnotation
*/

  // HtmlDocumentHandler member data

  // this constant indicates when to fire the status listener
  // this listener will add an overhead and we don't want a big overhead
  // this listener will be callled from ELEMENTS_RATE to ELEMENTS_RATE
  final static  int ELEMENTS_RATE = 128;

  // this map contains the elements name that we want to create
  // if it's null all the elements from the HTML documents will be transformed
  // into Gate annotation objects otherwise only the elements it contains will
  // be transformed
  private Map markupElementsMap = null;

  // the content of the HTML document, without any tag
  // for internal use
  private StringBuffer tmpDocContent = null;

  // a stack used to remember elements and to keep the order
  private java.util.Stack stack = null;

  // a gate document
  private gate.Document doc = null;

  // an annotation set used for creating annotation reffering the doc
  private gate.AnnotationSet basicAS;

  // listeners for status report
  protected List myStatusListeners = new LinkedList();

  // this reports the the number of elements that have beed processed so far
  private int elements = 0;

  // we need a colection to retain all the CustomObjects that will be
  // transformed into annotation over the gate document...
  // the transformation will take place inside onDocumentEnd() method
  private List colector = null;

} // HtmlDocumentHandler


/**
  * The objects belonging to this class are used inside the stack.
  * This class is for internal needs
  */
class  CustomObject {

  // constructor
  public CustomObject(String anElemName, FeatureMap aFm,
                         Long aStart, Long anEnd) {
    elemName = anElemName;
    fm = aFm;
    start = aStart;
    end = anEnd;
  }

  // accesor
  public String getElemName() {
    return elemName;
  }

  public FeatureMap getFM() {
    return fm;
  }

  public Long getStart() {
    return start;
  }

  public Long getEnd() {
    return end;
  }

  // mutator
  public void setElemName(String anElemName) {
    elemName = anElemName;
  }

  public void setFM(FeatureMap aFm) {
    fm = aFm;
  }

  public void setStart(Long aStart) {
    start = aStart;
  }

  public void setEnd(Long anEnd) {
    end = anEnd;
  }

  // data fields
  private String elemName = null;
  private FeatureMap fm = null;
  private Long start = null;
  private Long end  = null;
} // CustomObject

