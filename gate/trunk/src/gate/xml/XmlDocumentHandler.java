/*
 *  XmlDocumentHandler.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU,  9 May 2000
 *
 *  $Id$
 */

package gate.xml;

import java.util.*;

import gate.corpora.*;
import gate.util.*;
import gate.*;
import gate.event.*;


import org.xml.sax.*;


/**
  * Implements the behaviour of the XML reader
  * Methods of an object of this class are called by the SAX parser when
  * events will appear.
  * The idea is to parse the XML document and construct Gate annotations
  * objects.
  * This class also will replace the content of the Gate document with a
  * new one containing anly text from the XML document.
  */
public class XmlDocumentHandler extends HandlerBase{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /**
    * Constructs a XmlDocumentHandler object. The annotationSet set will be the
    * default one taken from the gate document.
    * @param aDocument the Gate document that will be processed.
    * @param aMarkupElementsMap this map contains the elements name that we
    * want to create.
    * @param anElement2StringMap this map contains the strings that will be
    * added to the text contained by the key element.
    */
  public XmlDocumentHandler(gate.Document aDocument, Map  aMarkupElementsMap,
                            Map anElement2StringMap){
    this(aDocument,aMarkupElementsMap,anElement2StringMap,null);
  } // XmlDocumentHandler

  /**
    * Constructs a XmlDocumentHandler object.
    * @param aDocument the Gate document that will be processed.
    * @param aMarkupElementsMap this map contains the elements name that we
    * want to create.
    * @param anElement2StringMap this map contains the strings that will be
    * added to the text contained by the key element.
    * @param anAnnotationSet is the annotation set that will be filled when the
    * document was processed
    */
  public XmlDocumentHandler(gate.Document       aDocument,
                            Map                 aMarkupElementsMap,
                            Map                 anElement2StringMap,
                            gate.AnnotationSet  anAnnotationSet){
    // init stack
    stack = new java.util.Stack();

    // this string contains the plain text (the text without markup)
    tmpDocContent = new StringBuffer("");

    // colector is used later to transform all custom objects into annotation
    // objects
    colector = new LinkedList();

    // the Gate document
    doc = aDocument;

    // this map contains the elements name that we want to create
    // if it's null all the elements from the XML documents will be transformed
    // into Gate annotation objects
    markupElementsMap = aMarkupElementsMap;

    // this map contains the string that we want to insert iside the document
    // content, when a certain element is found
    // if the map is null then no string is added
    element2StringMap = anElement2StringMap;

    basicAS = anAnnotationSet;
  }

  /**
    * This method is called when the SAX parser encounts the beginning of the
    * XML document.
    */
  public void startDocument() throws org.xml.sax.SAXException {
  }

  /**
    * This method is called when the SAX parser encounts the end of the
    * XML document.
    * Here we set the content of the gate Document to be the one generated
    * inside this class (tmpDocContent).
    * After that we use the colector to generate all the annotation reffering
    * this new gate document.
    */
  public void endDocument() throws org.xml.sax.SAXException {

    // replace the document content with the one without markups
    doc.setContent(new DocumentContentImpl(tmpDocContent.toString()));

    // fire the status listener
    fireStatusChangedEvent("Total elements: " + elements);

    // If basicAs is null then get the default AnnotationSet,
    // based on the gate document.
    if (basicAS == null)
      basicAS = doc.getAnnotations();

    // create all the annotations (on this new document) from the collector
    Iterator anIterator = colector.iterator();
    while (anIterator.hasNext()){
      CustomObject obj = (CustomObject) anIterator.next();

      // create a new annotation and add it to the annotation set
      try{
        // the annotation type will be conforming with markupElementsMap
        //add the annotation to the Annotation Set
        if (markupElementsMap == null)
          basicAS.add(obj.getStart (), obj.getEnd(), obj.getElemName(),
                      obj.getFM ()
          );
        else {
          // get the type of the annotation from Map
          String annotationType = (String)
                                markupElementsMap.get(obj.getElemName());
          if (annotationType != null)
            basicAS.add(obj.getStart(),obj.getEnd(),annotationType,obj.getFM());
        }
      }catch (gate.util.InvalidOffsetException e){
        //e.printStackTrace(Err.getPrintWriter());
        throw new GateSaxException(e);
      }
    }// while
  }

  /**
    * This method is called when the SAX parser encounts the beginning of an
    * XML element.
    */
  public void startElement(String elemName, AttributeList atts){
    // inform the progress listener to fire only if no of elements processed
    // so far is a multiple of ELEMENTS_RATE
    if ((++elements % ELEMENTS_RATE) == 0)
        fireStatusChangedEvent("Processed elements : " + elements);

    // construct a SimpleFeatureMapImpl from the list of attributes
    FeatureMap fm = new SimpleFeatureMapImpl();

    //get the name and the value of the attributes and add them to a FeaturesMAP
    for (int i = 0; i < atts.getLength(); i++) {
     String attName  = atts.getName(i);
     String attValue = atts.getValue(i);
     fm.put(attName,attValue);
    }

    // create the START index of the annotation
    Long startIndex = new Long(tmpDocContent.length ());

    // initialy the Start index is equal with End index
    CustomObject obj = new CustomObject(elemName,fm, startIndex, startIndex);

    // put this object into the stack
    stack.push(obj);
  }

  /**
    * This method is called when the SAX parser encounts the end of an
    * XML element.
    * Here we extract
    */
  public void endElement(String elemName) throws SAXException{
    // obj is for internal use
    CustomObject obj = null;

    // if the stack is not empty, we extract the custom object and delete it
    if (!stack.isEmpty ()){
      obj = (CustomObject) stack.pop();
    }

    // put the object into colector
    // later, when the document ends we will use colector to create all the
    // annotations
    colector.add(obj);

    // if element is found on Element2String map, then add the string to the
    // end of the document content
    if (element2StringMap != null){
      String stringFromMap = null;

      // test to see if element is inside the map
      // if it is then get the string value and add it to the document content
      stringFromMap = (String) element2StringMap.get(elemName);
      if (stringFromMap != null)
          tmpDocContent.append(stringFromMap);
    }
  }

  /**
    * This method is called when the SAX parser encounts text in the XML doc.
    * Here we calculate the end indices for all the elements present inside the
    * stack and update with the new values.
    */
  public void characters( char[] text,int start,int length) throws SAXException{
    // create a string object based on the reported text
    String content = new String(text, start, length);

    // calculate the End index for all the elements of the stack
    // the expression is : End index = Current doc length + text length
    Long end = new Long(tmpDocContent.length() + content.length());

    CustomObject obj = null;
    // iterate through stack to modify the End index of the existing elements

    java.util.Iterator anIterator = stack.iterator();
    while (anIterator.hasNext ()){

      // get the object and move to the next one
      obj = (CustomObject) anIterator.next ();

      // if obj is not null then sets its end index
      if (null != obj){
        // sets its End index
        obj.setEnd(end);
      }
    }
    // update the document content
    tmpDocContent.append(content);
  }

  /**
    * This method is called when the SAX parser encounts white spaces
    */
  public void ignorableWhitespace(char ch[],int start,int length) throws
                                                                   SAXException{

    // internal String object
    String  text = new String(ch, start, length);
    // if the last character in tmpDocContent is \n and the read whitespace is
    // \n then don't add it to tmpDocContent...

    if (tmpDocContent.length () != 0)
      if (tmpDocContent.charAt (tmpDocContent.length () - 1) != '\n' ||
        !text.equalsIgnoreCase("\n")
      )
         tmpDocContent.append(text);
  }

  /**
    * Error method.We deal with this exception inside SimpleErrorHandler class
    */
  public void error(SAXParseException ex) throws SAXException {
    // deal with a SAXParseException
    // see SimpleErrorhandler class
    _seh.error(ex);
  }

  /**
    * FatalError method.
    */
  public void fatalError(SAXParseException ex) throws SAXException {
    // deal with a SAXParseException
    // see SimpleErrorhandler class
    _seh.fatalError(ex);
  }

  /**
    * Warning method comment.
    */
  public void warning(SAXParseException ex) throws SAXException {
    // deal with a SAXParseException
    // see SimpleErrorhandler class
    _seh.warning(ex);
  }

  /**
    * This method is called when the SAX parser encounts a comment
    * It works only if the XmlDocumentHandler implements a
    * com.sun.parser.LexicalEventListener
    */
  public void comment(String text) throws SAXException {
    // create a FeatureMap and then add the comment to the annotation set.
    /*
    gate.util.SimpleFeatureMapImpl fm = new gate.util.SimpleFeatureMapImpl();
    fm.put ("text_comment",text);
    Long node = new Long (tmpDocContent.length());
    CustomObject anObject = new CustomObject("Comment",fm,node,node);
    colector.add(anObject);
    */
  }

  /**
    * This method is called when the SAX parser encounts a start of a CDATA
    * section
    * It works only if the XmlDocumentHandler implements a
    * com.sun.parser.LexicalEventListener
    */
  public void startCDATA()throws SAXException {
  }

  /**
    * This method is called when the SAX parser encounts the end of a CDATA
    * section.
    * It works only if the XmlDocumentHandler implements a
    * com.sun.parser.LexicalEventListener
    */
  public void endCDATA() throws SAXException {
  }

  /**
    * This method is called when the SAX parser encounts a parsed Entity
    * It works only if the XmlDocumentHandler implements a
    * com.sun.parser.LexicalEventListener
    */
  public void startParsedEntity(String name) throws SAXException {
  }

  /**
    * This method is called when the SAX parser encounts a parsed entity and
    * informs the application if that entity was parsed or not
    * It's working only if the CustomDocumentHandler implements a
    *  com.sun.parser.LexicalEventListener
    */
  public void endParsedEntity(String name, boolean included)throws SAXException{
  }

  //StatusReporter Implementation

  /**
    * This methos is called when a listener is registered with this class
    */
  public void addStatusListener(StatusListener listener){
    myStatusListeners.add(listener);
  }
  /**
    * This methos is called when a listener is removed
    */
  public void removeStatusListener(StatusListener listener){
    myStatusListeners.remove(listener);
  }
  /**
    * This methos is called whenever we need to inform the listener about an
    * event.
  */
  protected void fireStatusChangedEvent(String text){
    Iterator listenersIter = myStatusListeners.iterator();
    while(listenersIter.hasNext())
      ((StatusListener)listenersIter.next()).statusChanged(text);
  }

  // XmlDocumentHandler member data

  // this constant indicates when to fire the status listener
  // this listener will add an overhead and we don't want a big overhead
  // this listener will be callled from ELEMENTS_RATE to ELEMENTS_RATE
  final static  int ELEMENTS_RATE = 128;

  // this map contains the elements name that we want to create
  // if it's null all the elements from the XML documents will be transformed
  // into Gate annotation objects otherwise only the elements it contains will
  // be transformed
  private Map markupElementsMap = null;

  // this map contains the string that we want to insert iside the document
  // content, when a certain element is found
  // if the map is null then no string is added
  private Map element2StringMap = null;

  // this object inducates what to do when the parser encounts an error
  private SimpleErrorHandler _seh = new SimpleErrorHandler();

  // the content of the XML document, without any tag
  // for internal use
  private StringBuffer tmpDocContent = null;

  // a stack used to remember elements and to keep the order
  private java.util.Stack stack = null;

  // a gate document
  private gate.Document doc = null;

  // an annotation set used for creating annotation reffering the doc
  private gate.AnnotationSet basicAS = null;

  // listeners for status report
  protected List myStatusListeners = new LinkedList();

  // this reports the the number of elements that have beed processed so far
  private int elements = 0;

  // we need a colection to retain all the CustomObjects that will be
  // transformed into annotation over the gate document...
  // the transformation will take place inside onDocumentEnd() method
  private List colector = null;

} //XmlDocumentHandler


/**
  * The objects belonging to this class are used inside the stack.
  * This class is for internal needs
  */
class  CustomObject {

  // constructor
  public CustomObject(String anElemName, FeatureMap aFm,
                         Long aStart, Long anEnd){
    elemName = anElemName;
    fm = aFm;
    start = aStart;
    end = anEnd;
  }

  // accesor
  public String getElemName(){
    return elemName;
  }

  public FeatureMap getFM(){
    return fm;
  }

  public Long getStart(){
    return start;
  }

  public Long getEnd(){
    return end;
  }

  // mutator
  public void setElemName(String anElemName){
    elemName = anElemName;
  }

  public void setFM(FeatureMap aFm){
    fm = aFm;
  }

  public void setStart(Long aStart){
    start = aStart;
  }

  public void setEnd(Long anEnd){
    end = anEnd;
  }

  // data fields
  private String elemName = null;

  private FeatureMap fm = null;

  private Long start = null;

  private Long end  = null;

} // CustomObject

