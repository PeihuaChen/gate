/*
 *  AnnotationSchema.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU, 27/Sept/2000
 *
 *  $Id$
 */
package gate.creole;

import java.io.InputStream;
import java.net.URL;
import java.util.*;

import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

import gate.Resource;

/** This class handles annotation schemas.An annotation schema is a
  * representation of an annotation, together with its types and their
  * attributes, values and types.
  */
public class AnnotationSchema extends AbstractLanguageResource{
  public static final String FILE_URL_PARAM_NAME = "xmlFileUrl";

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** A map between XSchema types and Java Types */
  private static Map xSchema2JavaMap;

  /** A map between JAva types and XSchema */
  private static Map java2xSchemaMap;

  /** This sets up two Maps between XSchema types and their coresponding
    * Java types + a DOM xml parser
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
  } //setUpStaticData

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

  /** @return a FeatureSchema object from featureSchemaSet, given a
    * feature name.It will return null if the feature name is not found.
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
    if(xSchema2JavaMap == null || java2xSchemaMap == null)
      setUpStaticData();

    // parse the XML file if we have its URL
    if(xmlFileUrl != null) fromXSchema(xmlFileUrl);

    return this;
  } // init()

  /** The xml file URL of the resource */
  protected URL xmlFileUrl;

  /**
   * The namepsace used in the xml file
   */
  protected Namespace namespace;

  /** Set method for the resource xml file URL */
  public void setXmlFileUrl(URL xmlFileUrl) { this.xmlFileUrl = xmlFileUrl; }

  /** Get method for the resource xml file URL */
  public URL getXmlFileUrl() { return xmlFileUrl; }

  /** Creates an AnnotationSchema object from an XSchema file
    * @param anXSchemaURL the URL where to find the XSchema file
    */
  public void fromXSchema(URL anXSchemaURL)
              throws ResourceInstantiationException {
    org.jdom.Document jDom = null;
    SAXBuilder saxBuilder = new SAXBuilder(false);
    try{
      jDom = saxBuilder.build(anXSchemaURL);
    }catch(JDOMException je){
      throw new ResourceInstantiationException(je);
    }
    workWithJDom(jDom);
  } // fromXSchema

  /** Creates an AnnotationSchema object from an XSchema file
    * @param anXSchemaInputStream the Input Stream containing the XSchema file
    */
  public void fromXSchema(InputStream anXSchemaInputStream)
              throws ResourceInstantiationException {
    org.jdom.Document jDom = null;
    SAXBuilder saxBuilder = new SAXBuilder(false);
    try{
      jDom = saxBuilder.build(anXSchemaInputStream);
    }catch(JDOMException je){
      throw new ResourceInstantiationException(je);
    }
    workWithJDom(jDom);
  } // end fromXSchema

  /** This method uses the JDom structure for our XSchema needs. What it does is
    * to add semantics to the XML elements defined in XSchema. In the end we need
    * to construct an AnnotationSchema object form an XSchema file.
    *
    * @param jDom the JDOM structure containing the XSchema document. It must not
    * be <b>null<b>
    */
  private void workWithJDom(org.jdom.Document jDom){
    // Use the jDom structure the way we want
    org.jdom.Element rootElement = jDom.getRootElement();
    namespace = rootElement.getNamespace();
    // get all children elements from the rootElement
    List rootElementChildrenList = rootElement.getChildren("element", namespace);
    Iterator rootElementChildrenIterator = rootElementChildrenList.iterator();
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
    org.jdom.Element complexTypeElement = anElement.getChild("complexType",
                                                             namespace);
    if (complexTypeElement != null){
      List complexTypeCildrenList = complexTypeElement.getChildren("attribute",
                                                                   namespace);
      Iterator complexTypeCildrenIterator = complexTypeCildrenList.iterator();
      if (complexTypeCildrenIterator.hasNext())
        featureSchemaSet = new HashSet();
      while (complexTypeCildrenIterator.hasNext()) {
        org.jdom.Element childElement =
                    (org.jdom.Element) complexTypeCildrenIterator.next();
        createAndAddFeatureSchemaObject(childElement);
      }// end while
    }// end if
  } // createAnnoatationSchemaObject

  /** This method creates and adds a FeatureSchema object to the current
    * AnnotationSchema one.
    * @param anElement is an XSchema attribute element
    */
  public void createAndAddFeatureSchemaObject(org.jdom.Element
                                                          anAttributeElement) {
    String featureName = null;
    String featureType = null;
    String featureUse  = null;
    String featureValue = null;
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
    featureValue = anAttributeElement.getAttributeValue("value");
    if (featureValue == null)
      featureValue = "";

    // Let's check if it has a simpleType element inside
    org.jdom.Element simpleTypeElement  =
                                  anAttributeElement.getChild("simpleType",
                                                              namespace);

    // If it has (!= null) then check to see if it has a restrictionElement
    if (simpleTypeElement != null) {
      org.jdom.Element restrictionElement =
                              simpleTypeElement.getChild("restriction",
                                                         namespace);
      if (restrictionElement != null) {
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
                                 restrictionElement.getChildren("enumeration",
                                                                namespace);
        Iterator enumerationChildrenIterator =
                                enumerationElementChildrenList.iterator();

        // Check if there is any enumeration element in the list
        if (enumerationChildrenIterator.hasNext())
            featurePermissibleValuesSet = new HashSet();
        while (enumerationChildrenIterator.hasNext()) {
          org.jdom.Element enumerationElement =
                        (org.jdom.Element) enumerationChildrenIterator.next();
          String permissibleValue =
                            enumerationElement.getAttributeValue("value");
          // Add that value to the featureSchema possible values set.
          featurePermissibleValuesSet.add(permissibleValue);
        }// end while
      }// end if( restrictionElement != null)
    }// end if (simpleTypeElement != null)

    // If it doesn't have a simpleTypeElement inside and featureType is null or
    // it wasn't recognised, then we set the default type to string.
    if (simpleTypeElement == null && featureType == null )
      featureType =  (String) xSchema2JavaMap.get("string");

    // Create an add a featureSchema object
    FeatureSchema featureSchema = new FeatureSchema(
                                                   featureName,
                                                   featureType,
                                                   featureValue,
                                                   featureUse,
                                                   featurePermissibleValuesSet);
    featureSchemaSet.add(featureSchema);
  } // createAndAddFeatureSchemaObject

  /** @return a String containing the XSchema document representing
    *  an AnnotationSchema object.
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


