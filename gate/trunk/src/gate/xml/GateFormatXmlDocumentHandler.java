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
import gate.annotation.*;
import gate.util.*;
import gate.*;
import gate.event.*;


import org.xml.sax.*;
import org.xml.sax.helpers.*;


/**
  * Implements the behaviour of the XML reader. This is the reader for
  * Gate Xml documents saved with DocumentImplementation.toXml() method.
  */
public class GateFormatXmlDocumentHandler extends DefaultHandler{
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
    currentAnnotationSet = doc.getAnnotations();
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

  }// endDocument

  /**
    * This method is called when the SAX parser encounts the beginning of an
    * XML element.
    */
  public void startElement (String uri, String qName, String elemName,
                                                             Attributes atts){

    // Inform the progress listener to fire only if no of elements processed
    // so far is a multiple of ELEMENTS_RATE
    if ((++elements % ELEMENTS_RATE) == 0)
        fireStatusChangedEvent("Processed elements : " + elements);

    // Set the curent element being processed
    currentElementStack.add(elemName);

    if ("AnnotationSet".equals(elemName))
      processAnnotationSetElement(atts);

    if ("Annotation".equals(elemName))
      processAnnotationElement(atts);

    if ("Feature".equals(elemName))
      processFeatureElement(atts);

    if ("Node".equals(elemName))
      processNodeElement(atts);
  }// startElement

  /**
    * This method is called when the SAX parser encounts the end of an
    * XML element.
    */
    public void endElement (String uri, String qName, String elemName )
                                                           throws SAXException{

    currentElementStack.pop();
    // Deal with Annotation
    if ("Annotation".equals(elemName)){
      if (currentFeatureMap == null)
        currentFeatureMap = Factory.newFeatureMap();
      currentAnnot.setFM(currentFeatureMap);
      colector.add(currentAnnot);
      currentAnnot = null;
      currentFeatureMap = null;
      return;
    }// End if
    // Deal with Feature
    if ("Feature".equals(elemName)){
      if(currentFeatureName == null || currentFeatureValue == null){
        // Cannot add the (key,value) pair to the map
        // One of them is null something was wrong in the XML file.
        Out.prln("A key or a value was null. The feature wasn't added into" +
                " the feature map");
      }else {
        if (currentFeatureMap == null){
          // The XMl file was somehow altered and a start Feature wasn't found.
          Out.prln("Abnormal behavior: A feature map wasn't created. Creating" +
                " one... A start Feature element is missing");
          currentFeatureMap = Factory.newFeatureMap();
        }// End if
//        Err.prln("Feature name="+currentFeatureName+" value=" + currentFeatureValue);
        currentFeatureMap.put(currentFeatureName,currentFeatureValue);
      }// End if
      // Reset the Name & Value pair.
      currentFeatureName = null;
      currentFeatureValue = null;
      return;
    }//End if
    // Deal GateDocumentFeatures
    if ("GateDocumentFeatures".equals(elemName)){
      if (currentFeatureMap == null)
        currentFeatureMap = Factory.newFeatureMap();
//      Err.prln("Setting features for doc :" + currentFeatureMap);
      doc.setFeatures(currentFeatureMap);
      currentFeatureMap = null;
      return;
    }// End if

    // Deal with AnnotationSet
    if ("AnnotationSet".equals(elemName)){
      // Create and add annotations to the currentAnnotationSet
      Iterator iterator = colector.iterator();
      while (iterator.hasNext()){
        AnnotationObject annot = (AnnotationObject) iterator.next();
        // Clear the annot from the colector
        iterator.remove();
        // Create a new annotation and add it to the annotation set
        try{
          currentAnnotationSet.add(annot.getStart(),
                                   annot.getEnd(),
                                   annot.getElemName(),
                                   annot.getFM());
        }catch (gate.util.InvalidOffsetException e){
          Err.prln("Tried to add an invalid annotation(" +
          annot + ") ! Discarded !");
        }// End try
      }// End while
      // The colector is empty and ready for the next AnnotationSet
      return;
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
    if ("TextWithNodes".equals((String)currentElementStack.peek())){
      processTextOfTextWithNodesElement(content);
      return;
    }// End if
    if ("Name".equals((String)currentElementStack.peek())){
      processTextOfNameElement(content);
      return;
    }// End if
    if ("Value".equals((String)currentElementStack.peek())){
      processTextOfValueElement(content);
      return;
    }// End if
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


  /** This method deals with a AnnotationSet element. */
  private void processAnnotationSetElement(Attributes atts){
    if (atts != null){
      for (int i = 0; i < atts.getLength(); i++) {
       // Extract name and value
       String attName  = atts.getQName(i);
       String attValue = atts.getValue(i);
       if ("Name".equals(attName))
          currentAnnotationSet = doc.getAnnotations(attValue);
      }// End for
    }// End if
  }//processAnnotationSetElement

  /** This method deals with a Annotation element. */
  private void processAnnotationElement(Attributes atts){
    if (atts != null){
      currentAnnot = new AnnotationObject();
      for (int i = 0; i < atts.getLength(); i++) {
       // Extract name and value
       String attName  = atts.getQName(i);
       String attValue = atts.getValue(i);

       if ("Type".equals(attName))
         currentAnnot.setElemName(attValue);

       try{
         if ("StartNode".equals(attName)){
          Integer id = new Integer(attValue);
          Long offset = (Long)id2Offset.get(id);
          if (offset == null){
            currentAnnot.setStart(new Long(0));
            Out.prln("Warning: Couldn't found Node with id = " + id +
            ".It was specified in annot " + currentAnnot.getElemName() +
            " as a start node!" +
            "Annot was created with this node reset to 0." +
            "Data might not have the same consistency");
          }else
            currentAnnot.setStart(offset);
         }// Endif
         if ("EndNode".equals(attName)){
          Integer id = new Integer(attValue);
          Long offset = (Long) id2Offset.get(id);
          if (offset == null){
            currentAnnot.setEnd(new Long(0));
            Out.prln("Warning: Couldn't found Node with id = " + id +
            ".It was specified in annot " + currentAnnot.getElemName() +
            " as a end node!" +
            "Annot was created with this node reset to 0." +
            "Data might not have the same consistency");
          }else
            currentAnnot.setEnd(offset);
         }// End if
       } catch (NumberFormatException e){
          currentAnnot.setStart(new Long(0));
          currentAnnot.setEnd(new Long(0));
          Out.prln("Warning: Offsets problems(cannot create Integers from"+
          " id[" + attValue + "]) in annot " + currentAnnot.getElemName() +
          ".Setting them to (0,0). Data might loose consistency.");
       }// End try
      }// End For
    }// End if
  }//processAnnotationElement

  /** This method deals with a Features element. */
  private void processFeatureElement(Attributes atts){
    // The first time feature is calle it will create a features map.
    if (currentFeatureMap == null)
      currentFeatureMap = Factory.newFeatureMap();
  }//processFeatureElement

  /** This method deals with a Node element. */
  private void processNodeElement(Attributes atts){
    if (atts != null){
      for (int i = 0; i < atts.getLength(); i++) {
        // Extract name and value
        String attName  = atts.getQName(i);
        String attValue = atts.getValue(i);
        if ("id".equals(attName)){
          try{
            Integer id = new Integer(attValue);
            id2Offset.put(id,new Long(tmpDocContent.length()));
          }catch(NumberFormatException e){
          }// End try
        }// End if
      }// End for
    }// End if
  }// processNodeElement();

  /** This method deals with a Text belonging to TextWithNodes element. */
  private void processTextOfTextWithNodesElement(String text){
    tmpDocContent.append(text);
  }//processTextOfTextWithNodesElement

  /** This method deals with a Text belonging to Name element. */
  private void processTextOfNameElement(String text){
    if (currentFeatureMap == null)
      Out.prln("GATE XML FORMAT WARNING:" +
      " Found a Name element that is not enclosed into a Feature one." +
      " Discarding...");
    else{
      currentFeatureName = text;
    }// End If
  }//processTextOfNameElement();

  /** This method deals with a Text belonging to Value element. */
  private void processTextOfValueElement(String text){
    if (currentFeatureMap == null)
      Out.prln("GATE XML FORMAT WARNING:" +
      " Found a Value element that is not enclosed into a Feature one." +
      " Discarding...");
    else{
      currentFeatureValue = text;
    }// End If

  }//processTextOfValueElement();

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
  private Map id2Offset = new TreeMap();

  private Stack currentElementStack = new Stack();
  private AnnotationObject currentAnnot = null;
  private FeatureMap  currentFeatureMap = null;
  private String currentFeatureName = null;
  private String currentFeatureValue = null;
  private AnnotationSet currentAnnotationSet = null;

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