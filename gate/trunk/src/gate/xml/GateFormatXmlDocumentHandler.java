/*
 *  GateFormatXmlDocumentHandler.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU,  22 Nov 2000
 *
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
  */
public class GateFormatXmlDocumentHandler extends HandlerBase
                                           implements StatusReporter{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /**
    */
  public GateFormatXmlDocumentHandler(gate.Document aDocument){
    // This string contains the plain text (the text without markup)
    tmpDocContent = new StringBuffer("");

    // Colector is used later to transform all custom objects into annotation
    // objects
    colector = new LinkedList();

    // The Gate document
    doc = aDocument;
  }//GateFormatXmlDocumentHandler

  /**
    * This method is called when the SAX parser encounts the beginning of the
    * XML document.
    */
  public void startDocument() throws org.xml.sax.SAXException {
  }// startDocument

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
    long docSize = doc.getContent().size().longValue();

    // fire the status listener
    fireStatusChangedEvent("Total elements: " + elements);

    // If basicAs is null then get the default AnnotationSet,
    // based on the gate document.
    if (basicAS == null)
      basicAS = doc.getAnnotations();


    // Create and add annotation to the basicAs
    Iterator iterator = colector.iterator();
    while (iterator.hasNext()){
      AnnotationObject annot = (AnnotationObject) iterator.next();
      iterator.remove();
      // Create a new annotation and add it to the annotation set
      if (canCreateAnnotation(annot.getStart().longValue(),
                              annot.getEnd().longValue(),
                              docSize)
          )
        try{
          basicAS.add( annot.getStart(),
                       annot.getEnd(),
                       annot.getElemName(),
                       annot.getFM()
          );
        }catch (gate.util.InvalidOffsetException e){
          throw new GateSaxException(e);
        }
    }// End while
  }// endDocument

  /**
    * This method verifies if an Annotation can be created.
    */
  private boolean canCreateAnnotation(long start,
                                      long end,
                                      long gateDocumentSize){

    if (start < 0 || end < 0 ) return false;
    if (start > end ) return false;
    if ((start > gateDocumentSize) || (end > gateDocumentSize)) return false;
    return true;
  }// canCreateAnnotation

  /**
    * This method is called when the SAX parser encounts the beginning of an
    * XML element.
    */
  public void startElement(String elemName, AttributeList atts){
    // Inform the progress listener to fire only if no of elements processed
    // so far is a multiple of ELEMENTS_RATE
    if ((++elements % ELEMENTS_RATE) == 0)
        fireStatusChangedEvent("Processed elements : " + elements);

    // Set the curent element being processed
    currentElementStack.add(elemName);
    if ("GateDocument".equals(elemName))
      processGateDocumentElement(atts);

    if ("PlainText".equals(elemName))
      processPlainTextElement(atts);

    if ("AnnotationSet".equals(elemName))
      processAnnotationSetElement(atts);

    if ("Annotation".equals(elemName))
      processAnnotationElement(atts);

    if ("Features".equals(elemName))
      processFeaturesElement(atts);
  }// startElement

  /**
    * This method is called when the SAX parser encounts the end of an
    * XML element.
    */
  public void endElement(String elemName) throws SAXException{
    if ("Annotation".equals(elemName)){
      colector.add(currentAnnot);
      currentAnnot = null;
    }// End if
  }//endElement

  /**
    * This method is called when the SAX parser encounts text in the XML doc.
    * Here we calculate the end indices for all the elements present inside the
    * stack and update with the new values.
    */
  public void characters( char[] text,int start,int length) throws SAXException{
    // Create a string object based on the reported text
    String content = new String(text, start, length);
    if ("PlainText".equals((String)currentElementStack.peek()))
      processTextOfPlainTextElement(content);
  }//characters

  /**
    * This method is called when the SAX parser encounts white spaces
    */
  public void ignorableWhitespace(char ch[],int start,int length) throws
                                                                   SAXException{
  }//ignorableWhitespace

  /**
    * Error method.We deal with this exception inside SimpleErrorHandler class
    */
  public void error(SAXParseException ex) throws SAXException {
    // deal with a SAXParseException
    // see SimpleErrorhandler class
    _seh.error(ex);
  }//error

  /**
    * FatalError method.
    */
  public void fatalError(SAXParseException ex) throws SAXException {
    // deal with a SAXParseException
    // see SimpleErrorhandler class
    _seh.fatalError(ex);
  }//fatalError

  /**
    * Warning method comment.
    */
  public void warning(SAXParseException ex) throws SAXException {
    // deal with a SAXParseException
    // see SimpleErrorhandler class
    _seh.warning(ex);
  }//warning

  // Custom methods section

  /** This method deals with a GateDocument element.
    * If the element has attributes then it creates a FeatureMap for them.
    */
  private void processGateDocumentElement(AttributeList atts){
    // If there are attributes attached to GateDocument element then process
    // them.
    if (atts != null){
      documentFeatures = new SimpleFeatureMapImpl();
      for (int i = 0; i < atts.getLength(); i++) {
       // Extract name and value
       String attName  = atts.getName(i);
       String attValue = atts.getValue(i);
       // Put them into the map
       documentFeatures.put(attName,attValue);
      }// End For
    }// End if
  }// processGateDocumentElement

  /** This method deals with a PlainText element. */
  private void processPlainTextElement(AttributeList atts){
  }//processPlainTextElement

  /** This method deals with a AnnotationSet element. */
  private void processAnnotationSetElement(AttributeList atts){
  }//processAnnotationSetElement

  /** This method deals with a Annotation element. */
  private void processAnnotationElement(AttributeList atts){
    if (atts != null){
      currentAnnot = new AnnotationObject();
      for (int i = 0; i < atts.getLength(); i++) {
       // Extract name and value
       String attName  = atts.getName(i);
       String attValue = atts.getValue(i);

       if ("Type".equals(attName))
         currentAnnot.setElemName(attValue);

       try{
         if ("Start".equals(attName))
          currentAnnot.setStart(new Long(attValue));

         if ("End".equals(attName))
          currentAnnot.setEnd(new Long(attValue));
       } catch (NumberFormatException e){
          currentAnnot.setStart(new Long(0));
          currentAnnot.setEnd(new Long(0));
       }// End try
      }// End For
    }// End if
  }//processAnnotationElement

  /** This method deals with a Features element. */
  private void processFeaturesElement(AttributeList atts){
    FeatureMap fm = new SimpleFeatureMapImpl();
    if (atts != null){
      for (int i = 0; i < atts.getLength(); i++){
       // Extract name and value
       String attName  = atts.getName(i);
       String attValue = atts.getValue(i);
       // Add them to the fm
       fm.put(attName,attValue);
      }// End for
    }// End if
    // Set the fm to the current Annotation
    if (currentAnnot != null)
      currentAnnot.setFM(fm);
  }//processFeaturesElement

  /** This method deals with a Text belonging to PlainText element. */
  private void processTextOfPlainTextElement(String text){
    tmpDocContent.append(text);
  }//processTextOfPlainTextElement

  /**
    * This method is called when the SAX parser encounts a comment
    * It works only if the XmlDocumentHandler implements a
    * com.sun.parser.LexicalEventListener
    */
  public void comment(String text) throws SAXException {
  }//comment

  /**
    * This method is called when the SAX parser encounts a start of a CDATA
    * section
    * It works only if the XmlDocumentHandler implements a
    * com.sun.parser.LexicalEventListener
    */
  public void startCDATA()throws SAXException {
  }//startCDATA

  /**
    * This method is called when the SAX parser encounts the end of a CDATA
    * section.
    * It works only if the XmlDocumentHandler implements a
    * com.sun.parser.LexicalEventListener
    */
  public void endCDATA() throws SAXException {
  }//endCDATA

  /**
    * This method is called when the SAX parser encounts a parsed Entity
    * It works only if the XmlDocumentHandler implements a
    * com.sun.parser.LexicalEventListener
    */
  public void startParsedEntity(String name) throws SAXException {
  }//startParsedEntity

  /**
    * This method is called when the SAX parser encounts a parsed entity and
    * informs the application if that entity was parsed or not
    * It's working only if the CustomDocumentHandler implements a
    *  com.sun.parser.LexicalEventListener
    */
  public void endParsedEntity(String name, boolean included)throws SAXException{
  }//endParsedEntity

  //StatusReporter Implementation

  /**
    * This methos is called when a listener is registered with this class
    */
  public void addStatusListener(StatusListener listener){
    myStatusListeners.add(listener);
  }//addStatusListener
  /**
    * This methos is called when a listener is removed
    */
  public void removeStatusListener(StatusListener listener){
    myStatusListeners.remove(listener);
  }//removeStatusListener
  /**
    * This methos is called whenever we need to inform the listener about an
    * event.
  */
  protected void fireStatusChangedEvent(String text){
    Iterator listenersIter = myStatusListeners.iterator();
    while(listenersIter.hasNext())
      ((StatusListener)listenersIter.next()).statusChanged(text);
  }//fireStatusChangedEvent

  // XmlDocumentHandler member data

  /** This constant indicates when to fire the status listener.
    * This listener will add an overhead and we don't want a big overhead.
    * It will be callled from ELEMENTS_RATE to ELEMENTS_RATE
    */
  final static  int ELEMENTS_RATE = 128;

  /** This object indicates what to do when the parser encounts an error */
  private SimpleErrorHandler _seh = new SimpleErrorHandler();

  /** The content of the XML document, without any tag */
  private StringBuffer tmpDocContent = new StringBuffer("");

  /** A gate document */
  private gate.Document doc = null;

  /** An annotation set used for creating annotation reffering the doc */
  private gate.AnnotationSet basicAS = null;

  /** Listeners for status report */
  protected List myStatusListeners = new LinkedList();

  /** This reports the the number of elements that have beed processed so far*/
  private int elements = 0;

  /** We need a colection to retain all the CustomObjects that will be
    * transformed into annotation over the gate document...
    * The transformation will take place inside onDocumentEnd() method.
    */
  private List colector = null;


  private FeatureMap documentFeatures = null;
  private Stack currentElementStack = new Stack();
  private AnnotationObject currentAnnot = null;

}//XmlDocumentHandler

/**
  */
class  AnnotationObject{

  // Constructor
  public AnnotationObject(){
  }//AnnotationObject

  // accesor
  public String getElemName(){
    return elemName;
  }//getElemName

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