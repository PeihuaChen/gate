/**
 *  AnnotationSchema.java
 *
 *  Copyright (c) 2000-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June1991.
 *
 *  A copy of this licence is included in the distribution in the file
 *  licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 *  Cristian URSU, 27/Sept/2000
 *
 *  $Id$
 */

package gate.creole;

import java.util.*;
import java.net.*;
import java.io.*;

import gate.util.*;

import org.xml.sax.*;
import javax.xml.parsers.*;
import org.jdom.input.*;
import org.jdom.*;

/** This class handles all possible annotations together with their attributes,
  *  values and types
  */
public class AnnotationSchema {

  /** Debug flag */
  private static final boolean DEBUG = false;

/*
  public static void main(String[] args){
    URL url = null;
    try{
      url = new URL("file:///d:/cursu/XSchema/POSSchema.xml");
      //url = new URL("file:///d:/cursu/XSchema/SentenceSchema.xml");
      //url = new URL("file:///d:/cursu/XSchema/TokenSchema.xml");
      //url = new URL("file:///d:/cursu/XSchema/UtteranceSchema.xml");
      //url = new URL("file:///d:/cursu/XSchema/SyntaxTreeNodeSchema.xml");
      //url = new URL("file:///d:/cursu/XSchema/AllXSchema.xml");
    } catch (Exception e){
      e.printStackTrace(Err.getPrintWriter());
    }
    AnnotationSchema annotation = new AnnotationSchema("");
    annotation.fromXSchema(url);
    Out.pr(annotation.toXSchema());
  }
 */

  /**
    * A map between XSchema types and Java Types
    */
  private static Map xSchema2JavaMap = new HashMap();
  /**
    * A map between JAva types and XSchema
    */
  private static Map java2xSchemaMap = new HashMap();

  private static DocumentBuilder xmlParser = null;
  static{
    setUp();
  }

  /**
    *  This sets up two Maps between XSchema types and their coresponding
    *  Java types
    */
  private static  void setUp(){
    xSchema2JavaMap.put("string" ,new String().getClass().getName());
    xSchema2JavaMap.put("integer",new Integer(12).getClass().getName());
    xSchema2JavaMap.put("int",new Integer(12).getClass().getName());
    xSchema2JavaMap.put("boolean" ,new Boolean("true").getClass().getName());
    xSchema2JavaMap.put("float",new Float(12.12).getClass().getName());
    xSchema2JavaMap.put("double",new Double(12.12).getClass().getName());
    xSchema2JavaMap.put("short",new Short((short)12).getClass().getName());
    xSchema2JavaMap.put("byte",new Byte((byte)12).getClass().getName());

    java2xSchemaMap.put(new String().getClass().getName(),"string");
    java2xSchemaMap.put(new Integer(12).getClass().getName(),"integer");
    java2xSchemaMap.put(new Boolean("true").getClass().getName(),"boolean");
    java2xSchemaMap.put(new Float(12.12).getClass().getName(),"float");
    java2xSchemaMap.put(new Double(12.12).getClass().getName(),"double");
    java2xSchemaMap.put(new Short((short)12).getClass().getName(),"short");
    java2xSchemaMap.put(new Byte((byte)12).getClass().getName(),"byte");

    // Get an XML parser
    try {
      // Get a parser factory.
      DocumentBuilderFactory domBuilderFactory =
                                          DocumentBuilderFactory.newInstance();
      // Set up the factory to create the appropriate type of parser
      // A non validating one
      domBuilderFactory.setValidating(false);
      // A non namesapace aware one
      domBuilderFactory.setNamespaceAware(false);

      // Create the DOM parser
      xmlParser = domBuilderFactory.newDocumentBuilder();
      // Parse the document and create the DOM structure
    } catch (ParserConfigurationException e){
        e.printStackTrace(Err.getPrintWriter());
    }
  }// end setUp

  /** The name of the annotation */
  String annotationName = null;

  /** Schemas for the attributes */
  Set featureSchemaSet = null;

  /**
    * Constructs an annotation schema. Name and
    * feature schema set on null
    */
  public AnnotationSchema(){
    this(null,null);
  }//AnnotationSchema

  /**
    * Constructs an annotation schema given it's name.
    * Feature schema that it might contain is set on null
    */
  public AnnotationSchema(String anAnnotationName){
    this(anAnnotationName,null);
  }//AnnotationSchema

  /**
    * Constructs an AnnotationSchema object given it's name and a set of
    * FeatureSchema
    */
  public AnnotationSchema(String anAnnotationName,Set aFeatureSchemaSet){
    annotationName   = anAnnotationName;
    featureSchemaSet = aFeatureSchemaSet;
  }//AnnotationSchema

  /**
   * Returns the value of annotationName field
   */
  public String getAnnotationName(){
    return annotationName;
  }//getAnnotationName

  /** Returns the set of FeatureSchema*/
  public Set getFeatureSchemas(){
    return featureSchemaSet;
  }//getAttributeSchemas

  /** Returns a FeatureSchema object from featureSchemaSet, given a
    * feature name.
    * It will return null if the feature name is not found.
    */
  public FeatureSchema getFeatureSchema(String featureName) {
    Iterator fsIterator = featureSchemaSet.iterator();
    while (fsIterator.hasNext()) {
      FeatureSchema fs = (FeatureSchema) fsIterator.next();
      if (fs.getFeatureName().equals(featureName) )
        return fs;
    }
    return null;
  }//getFeatureSchema

  /**
    * Creates an AnnotationSchema object from an XSchema file
    * @param anXSchemaURL the URL where to find the XSchema file
    */
  public void fromXSchema(URL anXSchemaURL){
    try {
      // Parse the document and create the DOM structure
      org.w3c.dom.Document dom =
                xmlParser.parse(anXSchemaURL.toString());
      // Create a new jDOM BUILDER
      DOMBuilder jDomBuilder = new DOMBuilder();
      // Create a JDOM structure from the dom one
      org.jdom.Document jDom = jDomBuilder.build(dom);
      // Don't need dom anymore.
      dom = null;

      // Work with the jDom structure
      org.jdom.Element rootElement = jDom.getRootElement();
      // get all children elements from the rootElement
      List rootElementChildrenList = rootElement.getChildren("element");
      Iterator rootElementChildrenIterator =
                                            rootElementChildrenList.iterator();
      while (rootElementChildrenIterator.hasNext()){
        org.jdom.Element childElement =
              (org.jdom.Element) rootElementChildrenIterator.next();
        createAnnotationSchemaObject(childElement);
      }//end while
    } catch (SAXException e){
      e.printStackTrace(Err.getPrintWriter());
    } catch (IOException e) {
      e.printStackTrace(Err.getPrintWriter());
    }
  }// end fromXSchema

  /**
    * This method creates an AnnotationSchema object fom an org.jdom.Element
    * @param anElement is an XSchema element element
    */
  private void createAnnotationSchemaObject(org.jdom.Element anElement){
    // Get the value of the name attribute. If this attribute doesn't exists
    // then it will receive a default one.
    annotationName = anElement.getAttributeValue("name");
    if (annotationName == null)
        annotationName = "UnknownElement";
    // See if this element has a complexType element inside it
    org.jdom.Element complexTypeElement = anElement.getChild("complexType");
    if (complexTypeElement != null){
      List complexTypeCildrenList = complexTypeElement.getChildren("attribute");
      Iterator complexTypeCildrenIterator = complexTypeCildrenList.iterator();
      if (complexTypeCildrenIterator.hasNext())
        featureSchemaSet = new HashSet();
      while (complexTypeCildrenIterator.hasNext()){
        org.jdom.Element childElement =
                    (org.jdom.Element) complexTypeCildrenIterator.next();
        createAndAddFeatureSchemaObject(childElement);
      }// end while
    }//end if
  }// end createAnnoatationSchemaObject

  /**
    * This method creates and adds a FeatureSchema object to the current
    * AnnotationSchema one.
    * @param anElement is an XSchema attribute element
    */
  public void createAndAddFeatureSchemaObject(org.jdom.Element
                                                          anAttributeElement){
    String featureName = null;
    String featureType = null;
    String featureUse  = null;
    String featureDefaultValue = null;
    Set    featurePermissibleValuesSet = null;

    // Get the value of the name attribute. If this attribute doesn't exists
    // then it will receive a default one.
    featureName = anAttributeElement.getAttributeValue("name");
    if (featureName == null)
      featureName = "UnknownFeature";
    // See if it has a type attribute associated
    featureType = anAttributeElement.getAttributeValue("type");
    if (featureType != null)
      // Set it to the corresponding Java type
      featureType = (String) xSchema2JavaMap.get(featureType);
    // Get the value of use attribute
    featureUse = anAttributeElement.getAttributeValue("use");
    if (featureUse == null)
      // Set it to the default value
      featureUse = "optional";
    // Get the value of value attribute
    featureDefaultValue = anAttributeElement.getAttributeValue("value");
    if (featureDefaultValue == null)
      featureDefaultValue = "";
    // Let's check if it has a simpleType element inside
    org.jdom.Element simpleTypeElement  =
                                  anAttributeElement.getChild("simpleType");
    // If it has (!= null) then check to see if it has a restrictionElement
    if (simpleTypeElement != null){
      org.jdom.Element restrictionElement =
                              simpleTypeElement.getChild("restriction");
      if (restrictionElement != null){
        // Get the type attribute for restriction element
        featureType = restrictionElement.getAttributeValue("base");
        // Check to see if that attribute was present. getAttributeValue will
        // return null if it wasn't present
        if (featureType == null)
          // If it wasn't present then set it to default type (string)
          featureType =  (String) xSchema2JavaMap.get("string");
        else
          // Set it to the corresponding Java type
          featureType = (String) xSchema2JavaMap.get(featureType);
        // Check to see if there are any enumeration elements inside
        List enumerationElementChildrenList =
                                 restrictionElement.getChildren("enumeration");
        Iterator enumerationChildrenIterator =
                                enumerationElementChildrenList.iterator();
        // Check if there is any enumeration element in the list
        if (enumerationChildrenIterator.hasNext())
            featurePermissibleValuesSet = new HashSet();
        while (enumerationChildrenIterator.hasNext()){
          org.jdom.Element enumerationElement =
                        (org.jdom.Element) enumerationChildrenIterator.next();
          String permissibleValue =
                            enumerationElement.getAttributeValue("value");
          // Add that value to the featureSchema possible values set.
          featurePermissibleValuesSet.add(permissibleValue);
        }// end while
      }// end if( restrictionElement != null)
    }// end if (simpleTypeElement != null)

    // If it doesn't have a simpleTypeElement inside and featureType is null,
    // then we set a default type to string
    if (simpleTypeElement == null && featureType == null )
      featureType =  (String) xSchema2JavaMap.get("string");

    // Create an add a featureSchema object
    FeatureSchema featureSchema = new FeatureSchema(
                                                   featureName,
                                                   featureType,
                                                   featureDefaultValue,
                                                   featureUse,
                                                   featurePermissibleValuesSet);
    featureSchemaSet.add(featureSchema);
  }// end createAndAddFeatureSchemaObject

  /**
    * Writes an AnnotationSchema to a XSchema files
    */
  public String toXSchema(){
    StringBuffer schemaString = new StringBuffer();
    schemaString.append("<?xml version=\"1.0\"?>\n" +
                   "<schema xmlns=\"http://www.w3.org/2000/10/XMLSchema\">\n"+
                   " <element name=\"" + annotationName + "\"");
    if (featureSchemaSet == null)
      schemaString.append("/>\n");
    else {
      schemaString.append(">\n  <complexType>\n");
      Iterator featureSchemaSetIterator = featureSchemaSet.iterator();
      while (featureSchemaSetIterator.hasNext()){
        FeatureSchema fs = (FeatureSchema) featureSchemaSetIterator.next();
        schemaString.append("   " + fs.toXSchema(java2xSchemaMap));
      }// end while
      schemaString.append("  </complexType>\n");
      schemaString.append(" </element>\n");
    }// end if else
    schemaString.append("</schema>\n");
    return schemaString.toString();
  }//end toXSchema

}//AnnotationSchema

