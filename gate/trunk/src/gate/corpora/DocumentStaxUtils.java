/*
 *  DocumentStaxUtils.java
 *
 *  Copyright (c) 1998-2006, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Ian Roberts, 20/Jul/2006
 *
 *  $Id$
 */
package gate.corpora;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.DocumentContent;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.TextualDocument;
import gate.event.StatusListener;
import gate.util.GateException;
import gate.util.GateRuntimeException;
import gate.util.InvalidOffsetException;
import gate.util.Out;

/**
 * This class provides support for reading and writing GATE XML format
 * using StAX (the Streaming API for XML).
 */
public class DocumentStaxUtils {

  /**
   * Reads GATE XML format data from the given XMLStreamReader and puts
   * the content and annotation sets into the given Document, replacing
   * its current content. The reader must be positioned on the opening
   * GateDocument tag (i.e. the last event was a START_ELEMENT for which
   * getLocalName returns "GateDocument"), and when the method returns
   * the reader will be left positioned on the corresponding closing
   * tag.
   * 
   * @param xsr the source of the XML to parse
   * @param doc the document to update
   * @throws XMLStreamException
   */
  public static void readGateXmlDocument(XMLStreamReader xsr, Document doc)
          throws XMLStreamException {
    readGateXmlDocument(xsr, doc, null);
  }

  /**
   * Reads GATE XML format data from the given XMLStreamReader and puts
   * the content and annotation sets into the given Document, replacing
   * its current content. The reader must be positioned on the opening
   * GateDocument tag (i.e. the last event was a START_ELEMENT for which
   * getLocalName returns "GateDocument"), and when the method returns
   * the reader will be left positioned on the corresponding closing
   * tag.
   * 
   * @param xsr the source of the XML to parse
   * @param doc the document to update
   * @param statusListener optional status listener to receive status
   *          messages
   * @throws XMLStreamException
   */
  public static void readGateXmlDocument(XMLStreamReader xsr, Document doc,
          StatusListener statusListener) throws XMLStreamException {
    DocumentContent savedContent = null;

    // check the precondition
    xsr.require(XMLStreamConstants.START_ELEMENT, null, "GateDocument");

    // process the document features
    xsr.nextTag();
    xsr.require(XMLStreamConstants.START_ELEMENT, null, "GateDocumentFeatures");

    if(statusListener != null) {
      statusListener.statusChanged("Reading document features");
    }
    FeatureMap documentFeatures = readFeatureMap(xsr);

    // read document text, building the map of node IDs to offsets
    xsr.nextTag();
    xsr.require(XMLStreamConstants.START_ELEMENT, null, "TextWithNodes");

    Map nodeIdToOffsetMap = new HashMap();
    if(statusListener != null) {
      statusListener.statusChanged("Reading document content");
    }
    String documentText = readTextWithNodes(xsr, nodeIdToOffsetMap);

    // save the content, in case anything goes wrong later
    savedContent = doc.getContent();
    // set the document content to the text with nodes text.
    doc.setContent(new DocumentContentImpl(documentText));

    try {
      // process annotation sets, using the node map built above
      SortedSet allAnnotIds = new TreeSet();
      // initially, we don't know whether annotation IDs are required or
      // not
      Boolean requireAnnotationIds = null;
      int eventType = xsr.nextTag();
      while(eventType == XMLStreamConstants.START_ELEMENT) {
        xsr.require(XMLStreamConstants.START_ELEMENT, null, "AnnotationSet");
        String annotationSetName = xsr.getAttributeValue(null, "Name");
        AnnotationSet annotationSet = null;
        if(annotationSetName == null) {
          if(statusListener != null) {
            statusListener.statusChanged("Reading default annotation set");
          }
          annotationSet = doc.getAnnotations();
        }
        else {
          if(statusListener != null) {
            statusListener.statusChanged("Reading \"" + annotationSetName
                    + "\" annotation set");
          }
          annotationSet = doc.getAnnotations(annotationSetName);
        }
        annotationSet.clear();
        requireAnnotationIds = readAnnotationSet(xsr, annotationSet,
                nodeIdToOffsetMap, allAnnotIds, requireAnnotationIds);
        // readAnnotationSet leaves reader positioned on the
        // </AnnotationSet> tag, so nextTag takes us to either the next
        // <AnnotationSet> or to the </GateDocument>
        eventType = xsr.nextTag();
      }

      // check we are on the end document tag
      xsr.require(XMLStreamConstants.END_ELEMENT, null, "GateDocument");

      doc.setFeatures(documentFeatures);

      // set the ID generator, if doc is a DocumentImpl
      if(doc instanceof DocumentImpl && allAnnotIds.size() > 0) {
        ((DocumentImpl)doc).setNextAnnotationId(((Integer)allAnnotIds.last())
                .intValue() + 1);
      }
      if(statusListener != null) {
        statusListener.statusChanged("Finished.  " + allAnnotIds.size()
                + " annotation(s) processed");
      }
    }
    // in case of exception, reset document content to the unparsed XML
    catch(XMLStreamException xse) {
      doc.setContent(savedContent);
      throw xse;
    }
    catch(RuntimeException re) {
      doc.setContent(savedContent);
      throw re;
    }
  }

  /**
   * Processes an AnnotationSet element from the given reader and fills
   * the given annotation set with the corresponding annotations. The
   * reader must initially be positioned on the starting AnnotationSet
   * tag and will be left positioned on the correspnding closing tag.
   * 
   * @param xsr the reader
   * @param annotationSet the annotation set to fill.
   * @param nodeIdToOffsetMap a map mapping node IDs (Integer) to their
   *          offsets in the text (Long). If null, we assume that the
   *          node ids and offsets are the same (useful if parsing an
   *          annotation set in isolation).
   * @param allAnnotIds a set to contain all annotation IDs specified in
   *          the document. It will be updated if any of the annotations
   *          in this set specify an ID.
   * @param requireAnnotationIds whether annotations are required to
   *          specify their IDs. If true, it is an error for an
   *          annotation to omit the Id attribute. If false, it is an
   *          error for the Id to be present. If null, we have not yet
   *          determined what style of XML this is.
   * @return <code>requireAnnotationIds</code>. If the passed in
   *         value was null, and we have since determined what it should
   *         be, the updated value is returned.
   * @throws XMLStreamException
   */
  public static Boolean readAnnotationSet(XMLStreamReader xsr,
          AnnotationSet annotationSet, Map nodeIdToOffsetMap, Set allAnnotIds,
          Boolean requireAnnotationIds) throws XMLStreamException {
    List collectedAnnots = new ArrayList();
    while(xsr.nextTag() == XMLStreamConstants.START_ELEMENT) {
      xsr.require(XMLStreamConstants.START_ELEMENT, null, "Annotation");
      AnnotationObject annObj = new AnnotationObject();
      annObj.setElemName(xsr.getAttributeValue(null, "Type"));
      try {
        int startNodeId = Integer.parseInt(xsr.getAttributeValue(null,
                "StartNode"));
        if(nodeIdToOffsetMap != null) {
          Long startOffset = (Long)nodeIdToOffsetMap.get(new Integer(
                  startNodeId));
          if(startOffset != null) {
            annObj.setStart(startOffset);
          }
          else {
            throw new XMLStreamException("Invalid start node ID", xsr
                    .getLocation());
          }
        }
        else {
          // no offset map, so just use the ID as an offset
          annObj.setStart(new Long(startNodeId));
        }
      }
      catch(NumberFormatException nfe) {
        throw new XMLStreamException("Non-integer value found for StartNode",
                xsr.getLocation());
      }

      try {
        int endNodeId = Integer
                .parseInt(xsr.getAttributeValue(null, "EndNode"));
        if(nodeIdToOffsetMap != null) {
          Long endOffset = (Long)nodeIdToOffsetMap.get(new Integer(endNodeId));
          if(endOffset != null) {
            annObj.setEnd(endOffset);
          }
          else {
            throw new XMLStreamException("Invalid end node ID", xsr
                    .getLocation());
          }
        }
        else {
          // no offset map, so just use the ID as an offset
          annObj.setEnd(new Long(endNodeId));
        }
      }
      catch(NumberFormatException nfe) {
        throw new XMLStreamException("Non-integer value found for EndNode", xsr
                .getLocation());
      }

      String annotIdString = xsr.getAttributeValue(null, "Id");
      if(annotIdString == null) {
        if(requireAnnotationIds == null) {
          // if one annotation doesn't specify Id than all must
          requireAnnotationIds = Boolean.FALSE;
        }
        else {
          if(requireAnnotationIds.booleanValue()) {
            // if we were expecting an Id but didn't get one...
            throw new XMLStreamException(
                    "New style GATE XML format requires that every annotation "
                            + "specify its Id, but an annotation with no Id was found",
                    xsr.getLocation());
          }
        }
      }
      else {
        // we have an ID attribute
        if(requireAnnotationIds == null) {
          // if one annotation specifies an Id then all must
          requireAnnotationIds = Boolean.TRUE;
        }
        else {
          if(!requireAnnotationIds.booleanValue()) {
            // if we were expecting not to have an Id but got one...
            throw new XMLStreamException(
                    "Old style GATE XML format requires that no annotation "
                            + "specifies its Id, but an annotation with an Id was found",
                    xsr.getLocation());
          }
        }
        try {
          Integer annotationId = Integer.valueOf(annotIdString);
          if(allAnnotIds.contains(annotationId)) {
            throw new XMLStreamException("Annotation IDs must be unique. "
                    + "Found duplicate ID", xsr.getLocation());
          }
          allAnnotIds.add(annotationId);
          annObj.setId(annotationId);
        }
        catch(NumberFormatException nfe) {
          throw new XMLStreamException("Non-integer annotation ID found", xsr
                  .getLocation());
        }
      }

      // get the features of this annotation
      annObj.setFM(readFeatureMap(xsr));
      // readFeatureMap leaves xsr on the </Annotation> tag
      collectedAnnots.add(annObj);
    }

    // now process all found annotations.to add to the set
    Iterator collectedAnnotsIt = collectedAnnots.iterator();
    while(collectedAnnotsIt.hasNext()) {
      AnnotationObject annObj = (AnnotationObject)collectedAnnotsIt.next();
      try {
        if(annObj.getId() != null) {
          annotationSet.add(annObj.getId(), annObj.getStart(), annObj.getEnd(),
                  annObj.getElemName(), annObj.getFM());
        }
        else {
          annotationSet.add(annObj.getStart(), annObj.getEnd(), annObj
                  .getElemName(), annObj.getFM());
        }
      }
      catch(InvalidOffsetException ioe) {
        // really shouldn't happen, but could if we're not using an id
        // to offset map
        throw new XMLStreamException("Invalid offset when creating annotation "
                + annObj, ioe);
      }
    }
    return requireAnnotationIds;
  }

  /**
   * Processes the TextWithNodes element from this XMLStreamReader,
   * returning the text content of the document. The supplied map is
   * updated with the offset of each Node element encountered. The
   * reader must be positioned on the starting TextWithNodes tag and
   * will be returned positioned on the corresponding closing tag.
   * 
   * @param xsr
   * @param nodeIdToOffsetMap
   * @return
   */
  public static String readTextWithNodes(XMLStreamReader xsr,
          Map nodeIdToOffsetMap) throws XMLStreamException {
    StringBuffer textBuf = new StringBuffer(20480);
    int eventType;
    while((eventType = xsr.next()) != XMLStreamConstants.END_ELEMENT) {
      switch(eventType) {
        case XMLStreamConstants.CHARACTERS:
          textBuf.append(xsr.getTextCharacters(), xsr.getTextStart(), xsr
                  .getTextLength());
          break;

        case XMLStreamConstants.START_ELEMENT:
          // only Node elements allowed
          xsr.require(XMLStreamConstants.START_ELEMENT, null, "Node");
          String idString = xsr.getAttributeValue(null, "id");
          if(idString == null) {
            throw new XMLStreamException("Node element has no id", xsr
                    .getLocation());
          }
          try {
            Integer id = Integer.valueOf(idString);
            Long offset = new Long(textBuf.length());
            nodeIdToOffsetMap.put(id, offset);
          }
          catch(NumberFormatException nfe) {
            throw new XMLStreamException("Node element must have "
                    + "integer id", xsr.getLocation());
          }

          // Node element must be empty
          if(xsr.next() != XMLStreamConstants.END_ELEMENT) {
            throw new XMLStreamException("Node element within TextWithNodes "
                    + "must be empty.", xsr.getLocation());
          }
          break;

        default:
          // do nothing - ignore comments, PIs...
      }
    }
    return textBuf.toString();
  }

  /**
   * Processes a GateDocumentFeatures or Annotation element to build a
   * feature map. The element is expected to contain Feature children,
   * each with a Name and Value. The reader will be returned positioned
   * on the closing GateDocumentFeatures or Annotation tag.
   * 
   * @param xsr
   * @return
   * @throws XMLStreamException
   */
  public static FeatureMap readFeatureMap(XMLStreamReader xsr)
          throws XMLStreamException {
    FeatureMap fm = Factory.newFeatureMap();
    while(xsr.nextTag() == XMLStreamConstants.START_ELEMENT) {
      xsr.require(XMLStreamConstants.START_ELEMENT, null, "Feature");
      Object featureName = null;
      Object featureValue = null;
      while(xsr.nextTag() == XMLStreamConstants.START_ELEMENT) {
        if("Name".equals(xsr.getLocalName())) {
          featureName = readFeatureNameOrValue(xsr);
        }
        else if("Value".equals(xsr.getLocalName())) {
          featureValue = readFeatureNameOrValue(xsr);
        }
        else {
          throw new XMLStreamException("Feature element should contain "
                  + "only Name and Value children", xsr.getLocation());
        }
      }
      fm.put(featureName, featureValue);
    }
    return fm;
  }

  /**
   * Read the name or value of a feature. The reader must be initially
   * positioned on an element with className and optional itemClassName
   * attributes, and text content convertable to this class. It will be
   * returned on the corresponding end tag.
   * 
   * @param xsr the reader
   * @return the name or value represented by this element.
   * @throws XMLStreamException
   */
  private static Object readFeatureNameOrValue(XMLStreamReader xsr)
          throws XMLStreamException {
    String className = xsr.getAttributeValue(null, "className");
    if(className == null) {
      className = "java.lang.String";
    }
    String itemClassName = xsr.getAttributeValue(null, "itemClassName");
    if(itemClassName == null) {
      itemClassName = "java.lang.String";
    }
    // get the string representation of the name/value
    StringBuffer stringRep = new StringBuffer(1024);
    int eventType;
    while((eventType = xsr.next()) != XMLStreamConstants.END_ELEMENT) {
      switch(eventType) {
        case XMLStreamConstants.CHARACTERS:
          stringRep.append(xsr.getTextCharacters(), xsr.getTextStart(), xsr
                  .getTextLength());
          break;

        case XMLStreamConstants.START_ELEMENT:
          throw new XMLStreamException("Elements not allowed within "
                  + "feature name or value element.", xsr.getLocation());

        default:
          // do nothing - ignore comments, PIs, etc.
      }
    }

    // shortcut - if class name is java.lang.String, just return the
    // string representation directly
    if("java.lang.String".equals(className)) {
      return stringRep.toString();
    }

    // otherwise, do some fancy reflection
    Class theClass = null;
    try {
      theClass = Class.forName(className, true, Gate.getClassLoader());
    }
    catch(ClassNotFoundException cnfe) {
      // give up and just return the String
      return stringRep.toString();
    }

    if(java.util.Collection.class.isAssignableFrom(theClass)) {
      Class itemClass = null;
      Constructor itemConstructor = null;
      Collection featObject = null;

      boolean addItemAsString = false;

      // construct the collection object to use as the feature value
      try {
        featObject = (Collection)theClass.newInstance();
      }
      // if we can't instantiate the collection class at all, give up
      // and return the value as a string
      catch(IllegalAccessException iae) {
        return stringRep.toString();
      }
      catch(InstantiationException ie) {
        return stringRep.toString();
      }

      // common case - itemClass *is* java.lang.String, so we can
      // avoid all the reflection
      if("java.lang.String".equals(itemClassName)) {
        addItemAsString = true;
      }
      else {
        try {
          itemClass = Class.forName(itemClassName, true, Gate.getClassLoader());
          // Let's detect if itemClass takes a constructor with a String
          // as param
          Class[] paramsArray = new Class[1];
          paramsArray[0] = java.lang.String.class;
          itemConstructor = itemClass.getConstructor(paramsArray);
        }
        catch(ClassNotFoundException cnfex) {
          Out.prln("Warning: Item class " + itemClassName + " not found."
                  + "Adding items as Strings");
          addItemAsString = true;
        }
        catch(NoSuchMethodException nsme) {
          addItemAsString = true;
        }
        catch(SecurityException se) {
          addItemAsString = true;
        }// End try
      }

      StringTokenizer strTok = new StringTokenizer(stringRep.toString(), ";");
      Object[] params = new Object[1];
      Object itemObj = null;
      while(strTok.hasMoreTokens()) {
        String itemStrRep = strTok.nextToken();
        if(addItemAsString)
          featObject.add(itemStrRep);
        else {
          params[0] = itemStrRep;
          try {
            itemObj = itemConstructor.newInstance(params);
          }
          catch(Exception e) {
            throw new XMLStreamException("An item(" + itemStrRep
                    + ")  does not comply with its class" + " definition("
                    + itemClassName + ")", xsr.getLocation());
          }// End try
          featObject.add(itemObj);
        }// End if
      }// End while

      return featObject;
    }// End if

    // If currentfeatClass is not a Collection and not String, test to
    // see if it has a constructor that takes a String as param
    Class[] params = new Class[1];
    params[0] = java.lang.String.class;
    try {
      Constructor featConstr = theClass.getConstructor(params);
      Object[] featConstrParams = new Object[1];
      featConstrParams[0] = stringRep.toString();
      Object featObject = featConstr.newInstance(featConstrParams);
      return featObject;
    }
    catch(Exception e) {
      return stringRep.toString();
    }// End try
  }

  // ////////// Writing methods ////////////

  private static XMLOutputFactory outputFactory = null;

  /**
   * Returns a string containing the specified document in GATE XML
   * format.
   * 
   * @param doc the document
   */
  public static String toXml(Document doc) {
    try {
      if(outputFactory == null) {
        outputFactory = XMLOutputFactory.newInstance();
      }
      StringWriter sw = new StringWriter(doc.getContent().size().intValue()
              * DocumentXmlUtils.DOC_SIZE_MULTIPLICATION_FACTOR);
      XMLStreamWriter xsw = outputFactory.createXMLStreamWriter(sw);

      // start the document
      if(doc instanceof TextualDocument) {
        xsw.writeStartDocument(((TextualDocument)doc).getEncoding(), "1.0");
      }
      else {
        xsw.writeStartDocument("1.0");
      }
      newLine(xsw);
      writeDocument(doc, xsw, "");
      xsw.close();

      return sw.toString();
    }
    catch(XMLStreamException xse) {
      throw new GateRuntimeException("Error converting document to XML", xse);
    }
  }

  /**
   * Write the specified GATE document to a File.
   * 
   * @param doc the document to write
   * @param file the file to write it to
   * @throws XMLStreamException
   * @throws IOException
   */
  public static void writeDocument(Document doc, File file) throws
          XMLStreamException, IOException {
    writeDocument(doc, file, "");
  }
  
  /**
   * Write the specified GATE document to a File, optionally putting the XML in
   * a namespace.
   * 
   * @param doc the document to write
   * @param file the file to write it to
   * @param namespaceURI the namespace URI to use for the XML elements.
   *          Must not be null, but can be the empty string if no
   *          namespace is desired.
   * @throws XMLStreamException
   * @throws IOException
   */
  public static void writeDocument(Document doc, File file, String namespaceURI)
          throws XMLStreamException, IOException {
    if(outputFactory == null) {
      outputFactory = XMLOutputFactory.newInstance();
    }

    XMLStreamWriter xsw = null;
    OutputStream outputStream = new FileOutputStream(file);
    try {
      if(doc instanceof TextualDocument) {
        xsw = outputFactory.createXMLStreamWriter(outputStream,
                ((TextualDocument)doc).getEncoding());
        xsw.writeStartDocument(((TextualDocument)doc).getEncoding(), "1.0");
      }
      else {
        xsw = outputFactory.createXMLStreamWriter(outputStream);
        xsw.writeStartDocument("1.0");
      }
      newLine(xsw);

      writeDocument(doc, xsw, namespaceURI);
    }
    finally {
      if(xsw != null) {
        xsw.close();
      }
      outputStream.close();
    }
  }

  /**
   * Write the specified GATE Document to an XMLStreamWriter. This
   * method writes just the GateDocument element - the XML declaration
   * must be filled in by the caller if required.
   * 
   * @param doc the Document to write
   * @param xsw the StAX XMLStreamWriter to use for output
   * @throws GateException if an error occurs during writing
   */
  public static void writeDocument(Document doc, XMLStreamWriter xsw,
          String namespaceURI) throws XMLStreamException {
    xsw.writeStartElement(namespaceURI, "GateDocument");
    xsw.setDefaultNamespace(namespaceURI);
    if(namespaceURI.length() > 0) {
      xsw.writeDefaultNamespace(namespaceURI);
    }
    newLine(xsw);
    // features
    xsw.writeComment(" The document's features");
    newLine(xsw);
    newLine(xsw);
    xsw.writeStartElement(namespaceURI, "GateDocumentFeatures");
    newLine(xsw);
    writeFeatures(doc.getFeatures(), xsw, namespaceURI);
    xsw.writeEndElement(); // GateDocumentFeatures
    newLine(xsw);
    // text with nodes
    xsw.writeComment(" The document content area with serialized nodes ");
    newLine(xsw);
    newLine(xsw);
    writeTextWithNodes(doc, xsw, namespaceURI);
    newLine(xsw);
    // Serialize as XML all document's annotation sets
    // Serialize the default AnnotationSet
    StatusListener sListener = (StatusListener)gate.gui.MainFrame
            .getListeners().get("gate.event.StatusListener");
    if(sListener != null)
      sListener.statusChanged("Saving the default annotation set ");
    xsw.writeComment(" The default annotation set ");
    newLine(xsw);
    newLine(xsw);
    writeAnnotationSet(doc.getAnnotations(), xsw, namespaceURI);
    newLine(xsw);

    // Serialize all others AnnotationSets
    // namedAnnotSets is a Map containing all other named Annotation
    // Sets.
    Map namedAnnotSets = doc.getNamedAnnotationSets();
    if(namedAnnotSets != null) {
      Iterator iter = namedAnnotSets.values().iterator();
      while(iter.hasNext()) {
        AnnotationSet annotSet = (AnnotationSet)iter.next();
        xsw.writeComment(" Named annotation set ");
        newLine(xsw);
        newLine(xsw);
        // Serialize it as XML
        if(sListener != null)
          sListener.statusChanged("Saving " + annotSet.getName()
                  + " annotation set ");
        writeAnnotationSet(annotSet, xsw, namespaceURI);
        newLine(xsw);
      }// End while
    }// End if

    // close the GateDocument element
    xsw.writeEndElement();
    newLine(xsw);
  }

  /**
   * Writes the given annotation set to an XMLStreamWriter as GATE XML
   * format. The Name attribute of the generated AnnotationSet element
   * is set to the default value, i.e. <code>annotations.getName</code>.
   * 
   * @param annotations the annotation set to write
   * @param xsw the writer to use for output
   * @param namespaceURI
   * @throws XMLStreamException
   */
  public static void writeAnnotationSet(AnnotationSet annotations,
          XMLStreamWriter xsw, String namespaceURI) throws XMLStreamException {
    writeAnnotationSet(annotations, annotations.getName(), xsw, namespaceURI);
  }

  /**
   * Writes the given annotation set to an XMLStreamWriter as GATE XML
   * format. The value for the Name attribute of the generated
   * AnnotationSet element is given by <code>asName</code>.
   * 
   * @param annotations the annotation set to write
   * @param asName the name under which to write the annotation set.
   *          <code>null</code> means that no name will be used.
   * @param xsw the writer to use for output
   * @param namespaceURI
   * @throws XMLStreamException
   */
  public static void writeAnnotationSet(AnnotationSet annotations,
          String asName, XMLStreamWriter xsw, String namespaceURI)
          throws XMLStreamException {
    if(annotations == null) {
      // write an empty AnnotationSet element
      xsw.writeStartElement(namespaceURI, "AnnotationSet");
      newLine(xsw);
      xsw.writeEndElement();
      newLine(xsw);
    }

    xsw.writeStartElement(namespaceURI, "AnnotationSet");
    if(asName != null) {
      xsw.writeAttribute("Name", asName);
    }
    newLine(xsw);

    Iterator iterator = annotations.iterator();
    while(iterator.hasNext()) {
      Annotation annot = (Annotation)iterator.next();
      xsw.writeStartElement(namespaceURI, "Annotation");
      xsw.writeAttribute("Id", String.valueOf(annot.getId()));
      xsw.writeAttribute("Type", annot.getType());
      xsw.writeAttribute("StartNode", String.valueOf(annot.getStartNode()
              .getOffset()));
      xsw.writeAttribute("EndNode", String.valueOf(annot.getEndNode()
              .getOffset()));
      newLine(xsw);
      writeFeatures(annot.getFeatures(), xsw, namespaceURI);
      xsw.writeEndElement();
      newLine(xsw);
    }

    // end AnnotationSet element
    xsw.writeEndElement();
  }

  /**
   * Writes the content of the given document to an XMLStreamWriter as a
   * mixed content element called "TextWithNodes". At each point where
   * there is the start or end of an annotation in any annotation set on
   * the document, a "Node" element is written with an "id" feature
   * whose value is the offset of that node.
   * 
   * @param doc
   * @param xsw
   * @param namespaceURI
   * @throws XMLStreamException
   */
  public static void writeTextWithNodes(Document doc, XMLStreamWriter xsw,
          String namespaceURI) throws XMLStreamException {
    String aText = doc.getContent().toString();
    // no text, so return an empty element
    if(aText == null) {
      xsw.writeEmptyElement(namespaceURI, "TextWithNodes");
      return;
    }

    // build a set of all the offsets where Nodes are required
    TreeSet offsetsSet = new TreeSet();
    Iterator annotSetIter = doc.getAnnotations().iterator();
    while(annotSetIter.hasNext()) {
      Annotation annot = (Annotation)annotSetIter.next();
      offsetsSet.add(annot.getStartNode().getOffset());
      offsetsSet.add(annot.getEndNode().getOffset());
    }// end While
    // Get the nodes from all other named annotation sets.
    Map namedAnnotSets = doc.getNamedAnnotationSets();
    if(namedAnnotSets != null) {
      Iterator iter = namedAnnotSets.values().iterator();
      while(iter.hasNext()) {
        AnnotationSet annotSet = (AnnotationSet)iter.next();
        Iterator iter2 = annotSet.iterator();
        while(iter2.hasNext()) {
          Annotation annotTmp = (Annotation)iter2.next();
          offsetsSet.add(annotTmp.getStartNode().getOffset());
          offsetsSet.add(annotTmp.getEndNode().getOffset());
        }// End while
      }// End while
    }// End if

    // write the TextWithNodes element
    char[] textArray = aText.toCharArray();
    xsw.writeStartElement(namespaceURI, "TextWithNodes");
    int lastNodeOffset = 0;
    // offsetsSet iterator is in ascending order of offset, as it is a
    // SortedSet
    Iterator offsetsIterator = offsetsSet.iterator();
    while(offsetsIterator.hasNext()) {
      int offset = ((Long)offsetsIterator.next()).intValue();
      // write characters since the last node output
      xsw.writeCharacters(textArray, lastNodeOffset, offset - lastNodeOffset);
      xsw.writeEmptyElement(namespaceURI, "Node");
      xsw.writeAttribute("id", String.valueOf(offset));
      lastNodeOffset = offset;
    }
    // write any remaining text after the last node
    xsw.writeCharacters(textArray, lastNodeOffset, textArray.length
            - lastNodeOffset);
    // and the closing TextWithNodes
    xsw.writeEndElement();
  }

  /**
   * Write a feature map to the given XMLStreamWriter. The map is output
   * as a sequence of "Feature" elements, each having "Name" and "Value"
   * children. Note that there is no enclosing element - the caller must
   * write the enclosing "GateDocumentFeatures" or "Annotation" element.
   * 
   * @param features
   * @param xsw
   * @param namespaceURI
   * @throws XMLStreamException
   */
  public static void writeFeatures(FeatureMap features, XMLStreamWriter xsw,
          String namespaceURI) throws XMLStreamException {
    if(features == null) {
      return;
    }

    Set keySet = features.keySet();
    Iterator keySetIterator = keySet.iterator();
    while(keySetIterator.hasNext()) {
      Object key = keySetIterator.next();
      Object value = features.get(key);
      if(key != null && value != null) {
        String keyClassName = null;
        String keyItemClassName = null;
        String valueClassName = null;
        String valueItemClassName = null;
        String key2String = key.toString();
        String value2String = value.toString();
        Object item = null;
        // Test key if it is String, Number or Collection
        if(key instanceof java.lang.String || key instanceof java.lang.Number
                || key instanceof java.util.Collection)
          keyClassName = key.getClass().getName();
        // Test value if it is String, Number or Collection
        if(value instanceof java.lang.String
                || value instanceof java.lang.Number
                || value instanceof java.util.Collection)
          valueClassName = value.getClass().getName();
        // Features and values that are not Strings, Numbers or
        // collections
        // will be discarded.
        if(keyClassName == null || valueClassName == null) continue;

        // If key is collection serialize the collection in a specific
        // format
        if(key instanceof java.util.Collection) {
          StringBuffer keyStrBuff = new StringBuffer();
          Iterator iter = ((Collection)key).iterator();
          if(iter.hasNext()) {
            item = iter.next();
            if(item instanceof java.lang.Number)
              keyItemClassName = item.getClass().getName();
            else keyItemClassName = String.class.getName();
            keyStrBuff.append(item.toString());
          }// End if
          while(iter.hasNext()) {
            item = iter.next();
            keyStrBuff.append(";").append(item.toString());
          }// End while
          key2String = keyStrBuff.toString();
        }// End if

        // If key is collection serialize the colection in a specific
        // format
        if(value instanceof java.util.Collection) {
          StringBuffer valueStrBuff = new StringBuffer();
          Iterator iter = ((Collection)value).iterator();
          if(iter.hasNext()) {
            item = iter.next();
            if(item instanceof java.lang.Number)
              valueItemClassName = item.getClass().getName();
            else valueItemClassName = String.class.getName();
            valueStrBuff.append(item.toString());
          }// End if
          while(iter.hasNext()) {
            item = iter.next();
            valueStrBuff.append(";").append(item.toString());
          }// End while
          value2String = valueStrBuff.toString();
        }// End if

        xsw.writeStartElement(namespaceURI, "Feature");
        xsw.writeCharacters("\n  ");

        // write the Name
        xsw.writeStartElement(namespaceURI, "Name");
        if(keyClassName != null) {
          xsw.writeAttribute("className", keyClassName);
        }
        if(keyItemClassName != null) {
          xsw.writeAttribute("itemClassName", keyItemClassName);
        }
        xsw.writeCharacters(key2String);
        xsw.writeEndElement();
        xsw.writeCharacters("\n  ");

        // write the Value
        xsw.writeStartElement(namespaceURI, "Value");
        if(valueClassName != null) {
          xsw.writeAttribute("className", valueClassName);
        }
        if(valueItemClassName != null) {
          xsw.writeAttribute("itemClassName", valueItemClassName);
        }
        xsw.writeCharacters(value2String);
        xsw.writeEndElement();
        newLine(xsw);

        // close the Feature element
        xsw.writeEndElement();
        newLine(xsw);
      }
    }
  }

  /**
   * Convenience method to write a single new line to the given writer.
   * 
   * @param xsw the XMLStreamWriter to write to.
   * @throws XMLStreamException
   */
  private static void newLine(XMLStreamWriter xsw) throws XMLStreamException {
    xsw.writeCharacters("\n");
  }

  /** An inner class modeling the information contained by an annotation. */
  static class AnnotationObject {
    /** Constructor */
    public AnnotationObject() {
    }// AnnotationObject

    /** Accesor for the annotation type modeled here as ElemName */
    public String getElemName() {
      return elemName;
    }// getElemName

    /** Accesor for the feature map */
    public FeatureMap getFM() {
      return fm;
    }// getFM()

    /** Accesor for the start ofset */
    public Long getStart() {
      return start;
    }// getStart()

    /** Accesor for the end offset */
    public Long getEnd() {
      return end;
    }// getEnd()

    /** Mutator for the annotation type */
    public void setElemName(String anElemName) {
      elemName = anElemName;
    }// setElemName();

    /** Mutator for the feature map */
    public void setFM(FeatureMap aFm) {
      fm = aFm;
    }// setFM();

    /** Mutator for the start offset */
    public void setStart(Long aStart) {
      start = aStart;
    }// setStart();

    /** Mutator for the end offset */
    public void setEnd(Long anEnd) {
      end = anEnd;
    }// setEnd();

    /** Accesor for the id */
    public Integer getId() {
      return id;
    }// End of getId()

    /** Mutator for the id */
    public void setId(Integer anId) {
      id = anId;
    }// End of setId()

    public String toString() {
      return " [id =" + id + " type=" + elemName + " startNode=" + start
              + " endNode=" + end + " features=" + fm + "] ";
    }

    // Data fields
    private String elemName = null;

    private FeatureMap fm = null;

    private Long start = null;

    private Long end = null;

    private Integer id = null;
  } // AnnotationObject
}
