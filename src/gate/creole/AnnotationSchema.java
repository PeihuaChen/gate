/*
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
import gate.*;

import org.xml.sax.*;
import javax.xml.parsers.*;
import org.jdom.input.*;
import org.jdom.*;

/** This class handles annotation
  * schemas: annotation types, together with their attributes,
  * values and types.
  */
public class AnnotationSchema extends AbstractLanguageResource
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** A map between XSchema types and Java Types */
  private static Map xSchema2JavaMap;

  /** A map between JAva types and XSchema */
  private static Map java2xSchemaMap;

  /** Parser for the XSchema source files */
  private static DocumentBuilder xmlParser;

  /** This sets up two Maps between XSchema types and their coresponding
    * Java types
    */
  private static void setUpStaticData()
  throws ResourceInstantiationException
  {
    xSchema2JavaMap = new HashMap();
    java2xSchemaMap = new HashMap();

    xSchema2JavaMap.put("string",   String.class.getName());
    xSchema2JavaMap.put("integer",  Integer.class.getName());
    xSchema2JavaMap.put("int",      Integer.class.getName() );
    xSchema2JavaMap.put("boolean",  Boolean.class.getName());
    xSchema2JavaMap.put("float",    Float.class.getName());
    xSchema2JavaMap.put("double",   Double.class.getName());
    xSchema2JavaMap.put("short",    Short.class.getName());
    xSchema2JavaMap.put("byte",     Byte.class.getName());

    java2xSchemaMap.put(String.class.getName(),   "string");
    java2xSchemaMap.put(Integer.class.getName(),  "integer");
    java2xSchemaMap.put(Boolean.class.getName(),  "boolean");
    java2xSchemaMap.put(Float.class.getName(),    "float");
    java2xSchemaMap.put(Double.class.getName(),   "double");
    java2xSchemaMap.put(Short.class.getName(),    "short");
    java2xSchemaMap.put(Byte.class.getName(),     "byte");

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

    } catch(ParserConfigurationException e) {
      throw new ResourceInstantiationException(
        "couldn't create annotation schema parser: " + e
      );
    }
  } // setUpStaticData

  /** The name of the annotation */
  protected String annotationName = null;

  /** Returns the value of annotation name */
  public String getAnnotationName(){
    return annotationName;
  } // getAnnotationName

  /** Sets the annotation name */
  public void setAnnotationName(String annotationName) {
    this.annotationName = annotationName;
  } // setAnnotationName

  /** Schemas for the attributes */
  protected Set featureSchemaSet = null;

  /** Constructs an annotation schema. */
  public AnnotationSchema(){
  } // AnnotationSchema

  /** Returns the feature schema set */
  public Set getFeatureSchemaSet(){
    return featureSchemaSet;
  } // getAttributeSchemas

  /** Sets the feature schema set */
  public void setFeatureSchemaSet(Set featureSchemaSet) {
    this.featureSchemaSet = featureSchemaSet;
  } // setFeatureSchemaSet

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
  } // getFeatureSchema

  /** Initialise this resource, and return it. If the schema XML source file
    * URL has been set, it will construct itself from that file.
    */
  public Resource init() throws ResourceInstantiationException {
    // set up the static data if it's not there already
    if(xSchema2JavaMap == null || java2xSchemaMap == null || xmlParser == null)
      setUpStaticData();

    // parse the XML file if we have its URL
    if(xmlFileUrl != null)
      fromXSchema(xmlFileUrl);

    return this;
  } // init()

  /** The xml file URL of the resource */
  protected URL xmlFileUrl;

  /** Set method for the resource xml file URL */
  public void setXmlFileUrl(URL xmlFileUrl) { this.xmlFileUrl = xmlFileUrl; }

  /** Get method for the resource xml file URL */
  public URL getXmlFileUrl() { return xmlFileUrl; }

  /** Creates an AnnotationSchema object from an XSchema file
    * @param anXSchemaURL the URL where to find the XSchema file
    */
  public void fromXSchema(URL anXSchemaURL)
  throws ResourceInstantiationException {
    try {
      // Parse the document and create the DOM structure
      org.w3c.dom.Document dom =
                xmlParser.parse(anXSchemaURL.toString());
      org.jdom.Document jDom = buildJdomFromDom(dom);
      // don't need dom anymore
      dom = null;
      // Use JDOM
      workWithJDom(jDom);
    } catch (SAXException e){
      throw new ResourceInstantiationException(
        "couldn't parse annotation schema file: " + e
      );
    } catch (IOException e) {
      throw new ResourceInstantiationException(
        "couldn't open annotation schema file: " + e
      );
    }
  } // fromXSchema

  /** Creates an AnnotationSchema object from an XSchema file
    * @param anXSchemaInputStream the Input Stream containing the XSchema file
    */
  public void fromXSchema(InputStream anXSchemaInputStream)
  throws ResourceInstantiationException
  {
    try {
      // Parse the document and create the DOM structure
      org.w3c.dom.Document dom =
                xmlParser.parse(anXSchemaInputStream);
      org.jdom.Document jDom = buildJdomFromDom(dom);
      // don't need dom anymore
      dom = null;
      // Use JDOM
      workWithJDom(jDom);
    } catch (SAXException e){
      throw new ResourceInstantiationException(
        "couldn't parse annotation schema stream: " + e
      );
    } catch (IOException e) {
      throw new ResourceInstantiationException(
        "couldn't open annotation schema stream: " + e
      );
    }
  } // end fromXSchema

  /** This method builds a JDom structure from a W3C Dom one
    * @param aDom W3C dom structure
    * @return org.jdom.Document
    */
  private org.jdom.Document buildJdomFromDom(org.w3c.dom.Document aDom){
    org.jdom.Document jDom = null;
    // Create a new jDOM BUILDER
    DOMBuilder jDomBuilder = new DOMBuilder();
    // Create a JDOM structure from the dom one
    jDom = jDomBuilder.build(aDom);
    // Don't need dom anymore.
    return jDom;
  } // buildJdomFromDom

  /** This method uses the JDom structure for our XSchema needs
    */
  private void workWithJDom(org.jdom.Document jDom){
    // Use the jDom structure the way we want
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
  } // workWithJdom

  /** This method creates an AnnotationSchema object fom an org.jdom.Element
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
  } // createAnnoatationSchemaObject

  /** This method creates and adds a FeatureSchema object to the current
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
  } // createAndAddFeatureSchemaObject

  /** Writes an AnnotationSchema to a XSchema files
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
  }// toXSchema

} // AnnotationSchema

