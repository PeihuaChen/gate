/**
 *	XmlDocumentHandler.java
 *
 *	Cristian URSU,  9/May/2000
 *  $Id$
 */

package gate.xml;

import java.util.*;

import gate.corpora.*;
import gate.util.*;
import gate.*;
import gate.gui.*;


import org.xml.sax.*;


/**
  * Implements the behaviour of the XML reader
  */
public class XmlDocumentHandler extends HandlerBase
                                           implements StatusReporter{

  // member data

  // the markupElementsMap
  private java.util.Map markupElementsMap = null;

  // error handler
  private SimpleErrorHandler _seh = new SimpleErrorHandler();

  // the content of the XML document, without any tag
  // for internal use
  private String tmpDocContent = null;

  // a stack used to remember elements
  private java.util.Stack stack = null;


  // a gate document
  protected gate.Document doc = null;

  // an annotation set
  protected gate.AnnotationSet basicAS;

  // listeners for status report
  protected List myStatusListeners = new LinkedList();

  private int elements = 0;
  private int elementsRate = 128;

  /**
    * Constructor
    */
  public XmlDocumentHandler(gate.Document doc, java.util.Map markupElementsMap){
    // init stack, tmpDocContent, doc
    stack = new java.util.Stack();
    tmpDocContent = new String("");
    this.doc = doc ;
    this.markupElementsMap = markupElementsMap;
  }

  /**
    * this method is called when the SAX parser encounts the beginning of the
    * XML document
    */
  public void startDocument() throws org.xml.sax.SAXException {
    // gets AnnotationSet based on the gate document
    basicAS = doc.getAnnotations();
  }

  /**
    * this method is called when the SAX parser encounts the end of the
    * XML document
    */
  public void endDocument() throws org.xml.sax.SAXException {
    // replace the document content with the one without markups
    doc.setContent(new DocumentContentImpl(tmpDocContent));
    fireStatusChangedEvent("Total elements : " + elements);
  }

  /**
    * this method is called when the SAX parser encounts the beginning of an
    * XML element
    */
  public void startElement(String elemName, AttributeList atts){
    // inform the progress listener about that
    if ((++elements % elementsRate) == 0)
        fireStatusChangedEvent("Processed elements : " + elements);
    // construct a SimpleFeatureMapImpl from the list of attributes
    FeatureMap fm = new SimpleFeatureMapImpl();
    // for all attributes do
    for (int i = 0; i < atts.getLength(); i++) {
     String attName = atts.getName(i);
     //String type = atts.getType(i);
     String attValue = atts.getValue(i);
     fm.put(attName,attValue);
    }
    // create the START index of the annotation
    Long startIndex = new Long(tmpDocContent.length ());
    // initialy the Start index is equal with End index
    MyCustomObject obj = new MyCustomObject(elemName,fm, startIndex, startIndex);
    // put it into the stack
    stack.push (obj);
  }

  /**
    * this method is called when the SAX parser encounts the end of an
    * XML element
    */
  public void endElement(String elemName) throws SAXException{
    // obj is for internal use
    MyCustomObject obj = null;
    // if the stack is not empty, we extract the custom object and delete it
    if (!stack.isEmpty ()){
      obj = (MyCustomObject) stack.pop ();
    }
    // create a new annotation and add it to the annotation set
    try{
        // the annotation type will be conforming with markupElementsMap
        //add the annotation to the Annotation Set

        if (markupElementsMap == null)
          basicAS.add(obj.getStart (), obj.getEnd(), obj.getElemName(),
              obj.GetFM ()
          );
        else {
          // get the type of the annotation from Map
          String annotationType = (String) markupElementsMap.get(obj.getElemName());
          if (annotationType != null)
            basicAS.add(obj.getStart (),obj.getEnd(), annotationType, obj.GetFM());
        }

    }catch (gate.util.InvalidOffsetException e){
      e.printStackTrace(System.err);
    }
  }

  /**
  *  This method is called when the SAX parset encounts text int the XMl doc
  */
  public void characters( char[] text, int start, int length) throws SAXException{
    // some internal objects
    String content = new String(text, start, length);
   /*
    // triming section
    if (content.charAt(content.length() - 1) == '\n')
         hasNewLine = true;;
    content = content.trim();
    content = content + " ";
    if (hasNewLine)
            content = content + "\n";
    */
    // if u don't want '\n' inside your document decoment the line below
    //content = content.replace('\n',' ');

    // used to deal with the stack content later
    MyCustomObject obj = null;

    // calculate the End index for all the elements of the stack
    // the expression is : End = Current doc length + length
    Long end = new Long(tmpDocContent.length() + content.length());

    // iterate through stack to modify the End index of the existing elements
    java.util.Iterator iterator = stack.iterator();
    while (iterator.hasNext ()){
      // get the object and move to the next one
      obj = (MyCustomObject) iterator.next ();
      // if obj is not null
      if (null != obj){
        // sets its End index
        obj.setEnd (end);
      }
    }
    // update the document content
    tmpDocContent += content;
  }

  /**
  * this method is called when the SAX parser encounts white spaces
  */
  public void ignorableWhitespace(char ch[], int start, int length) throws SAXException{

    // internal String object
    String  text = new String(ch, start, length);
    // if the last character in tmpDocContent is \n and the read whitespace is \n
    // then don't add it to tmpDocContent...

    if (tmpDocContent.length () != 0)
      if (tmpDocContent.charAt (tmpDocContent.length () - 1) != '\n' ||
        !text.equalsIgnoreCase("\n")
      )
         tmpDocContent += text;
  }

  /**
  * error method comment.
  */
  public void error(SAXParseException ex) throws SAXException {
    // deal with a SAXParseException
    // see SimpleErrorhandler class
	  _seh.error(ex);
  }

  /**
  * fatalError method comment.
  */
  public void fatalError(SAXParseException ex) throws SAXException {
    // deal with a SAXParseException
    // see SimpleErrorhandler class
	  _seh.fatalError(ex);
  }

  /**
  * warning method comment.
  */
  public void warning(SAXParseException ex) throws SAXException {
    // deal with a SAXParseException
    // see SimpleErrorhandler class
	  _seh.warning(ex);
  }

  /**
  * this method is called when the SAX parser encounts a comment
  * It's working only if the XmlDocumentHandler implements a com.sun.parser.LexicalEventListener
  *
  */
  public void comment(String text) throws SAXException{
    // create a FeatureMap and then add the comment to the annotation set.
    /*
    gate.util.SimpleFeatureMapImpl fm = new gate.util.SimpleFeatureMapImpl();
    fm.put ("text_comment",text);
    Long node = new Long (tmpDocContent.length());
    try{
      basicAS.add(node,node, "COMMENT",fm);
    }catch (gate.util.InvalidOffsetException e){
      System.out.println(e);
    }
    */
  }

  /**
  * this method is called when the SAX parser encounts a start of a CDATA section
  * It's working only if the XmlDocumentHandler implements a com.sun.parser.LexicalEventListener
  */
  public void startCDATA()throws SAXException{
  }

  /**
  * this method is called when the SAX parser encounts the end of a CDATA section
  * It's working only if the XmlDocumentHandler implements a com.sun.parser.LexicalEventListener
  */
  public void endCDATA() throws SAXException{
  }

  /**
  * this method is called when the SAX parser encounts a parsed Entity
  * It's working only if the XmlDocumentHandler implements a com.sun.parser.LexicalEventListener
  */
  public void startParsedEntity(String name) throws SAXException{
  }

  /**
  * this method is called when the SAX parser encounts a parsed entity and
  * informs the application if that entity was parsed or not
  * It's working only if the CustomDocumentHandler implements a com.sun.parser.LexicalEventListener
  */
  public void endParsedEntity(String name, boolean included)throws SAXException{
  }

  //StatusReporter Implementation
  public void addStatusListener(StatusListener listener){
    myStatusListeners.add(listener);
  }
  public void removeStatusListener(StatusListener listener){
    myStatusListeners.remove(listener);
  }
  protected void fireStatusChangedEvent(String text){
    Iterator listenersIter = myStatusListeners.iterator();
    while(listenersIter.hasNext())
      ((StatusListener)listenersIter.next()).statusChanged(text);
  }
} //CustomDocumentHandler


/*
 * The objects belonging to thsi class are used inside the stack
 */
class  MyCustomObject{

  // data fields
  private String elemName = null;
  private FeatureMap fm = null;
  private Long start = null;
  private Long end  = null;

  // constructor
  public MyCustomObject(String elemName, FeatureMap fm,
                         Long start, Long end){
    this.elemName = elemName;
    this.fm = fm;
    this.start = start;
    this.end = end;
  }

  // accesor
  public String getElemName(){
    return elemName;
  }
  public FeatureMap GetFM(){
    return fm;
  }
  public Long getStart(){
    return start;
  }
  public Long getEnd(){
    return end;
  }


  // mutator
  public void setElemName(String elemName){
    this.elemName = elemName;
  }
  public void setFM(FeatureMap fm){
    this.fm = fm;
  }
  public void setStart(Long start){
    this.start = start;
  }
  public void setEnd(Long end){
    this.end = end;
  }

}// MyCustomObject


