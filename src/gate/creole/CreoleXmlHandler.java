/*
 *  CreoleXmlHandler.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 1/Sept/2000
 *
 *  $Id$
 */

package gate.creole;

import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.xml.parsers.*;
import java.net.*;

import gate.*;
import gate.util.*;
import gate.xml.SimpleErrorHandler;

/** This is a SAX handler for processing <CODE>creole.xml</CODE> files.
  * It would have been better to write it using DOM or JDOM but....
  * Resource data objects are created and added to the CREOLE register.
  * URLs for resource JAR files are added to the GATE class loader.
  */
public class CreoleXmlHandler extends DefaultHandler {

  /** A stack to stuff PCDATA onto for reading back at element ends.
   *  (Probably redundant to have a stack as we only push one item
   *  onto it. Probably. Ok, so I should check, but a) it works, b)
   *  I'm bald already and c) life is short.)
   */
  private Stack contentStack = new Stack();

  /** The current resource data object */
  private ResourceData resourceData;

  /** The current parameter list */
  private ParameterList currentParamList = new ParameterList();

  /** The current parameter disjunction */
  private List currentParamDisjunction = new ArrayList();

  /** The current parameter */
  private Parameter currentParam = new Parameter();

  /** The current element's attribute list */
  private Attributes currentAttributes;

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** The source URL of the directory file being parsed. */
  private URL sourceUrl;

  /** The features (attributes) of VIEW elements */
  private FeatureMap viewFeatures = Factory.newFeatureMap();

  /** This object indicates what to do when the parser encounts an error*/
  private SimpleErrorHandler _seh = new SimpleErrorHandler();

  /** Construction */
  public CreoleXmlHandler(CreoleRegister register, URL directoryUrl) {
    this.register = register;
    this.sourceUrl = directoryUrl;
  } // construction

  /** The register object that we add ResourceData objects to during parsing.
    */
  private CreoleRegister register;

  /** Called when the SAX parser encounts the beginning of the XML document */
  public void startDocument() throws GateSaxException {
    if(DEBUG) Out.prln("start document");
  } // startDocument

  /** Called when the SAX parser encounts the end of the XML document */
  public void endDocument() throws GateSaxException {
    if(DEBUG) Out.prln("end document");
    if(! contentStack.isEmpty()) {
      StringBuffer errorMessage =
        new StringBuffer("document ended but element stack not empty:");
      while(! contentStack.isEmpty())
        errorMessage.append(Strings.getNl()+"  "+(String) contentStack.pop());
      throw new GateSaxException(errorMessage.toString());
    }
  } // endDocument

  /** A verboase method for Attributes*/
  private String attributes2String(Attributes atts){
    StringBuffer strBuf = new StringBuffer("");
    if (atts == null) return strBuf.toString();
    for (int i = 0; i < atts.getLength(); i++) {
     String attName  = atts.getQName(i);
     String attValue = atts.getValue(i);
     strBuf.append(" ");
     strBuf.append(attName);
     strBuf.append("=");
     strBuf.append(attValue);
    }// End for
    return strBuf.toString();
  }// attributes2String()

  /** Called when the SAX parser encounts the beginning of an XML element */
  public void startElement (String uri, String qName, String elementName,
                                                             Attributes atts){

    if(DEBUG) {
      Out.pr("startElement: ");
      Out.println(
        elementName + " " +
        attributes2String(atts)
      );
    }

    // create a new ResourceData when it's a RESOURCE element
    if(elementName.toUpperCase().equals("RESOURCE")) {
      resourceData = new ResourceData();
      resourceData.setFeatures(Factory.newFeatureMap());
    }

    // record the attributes of this element
    currentAttributes = atts;

    // process attributes of parameter and view elements
    if(elementName.toUpperCase().equals("PARAMETER")) {
      if(DEBUG) {
        for(int i=0, len=currentAttributes.getLength(); i<len; i++) {
          Out.prln(currentAttributes.getQName(i));
          Out.prln(currentAttributes.getValue(i));
        }// End for
      }// End if
      currentParam.comment = currentAttributes.getValue("COMMENT");
      currentParam.defaultValueString = currentAttributes.getValue("DEFAULT");
      currentParam.optional =
        Boolean.valueOf(currentAttributes.getValue("OPTIONAL")).booleanValue();
      currentParam.name = currentAttributes.getValue("NAME");
      currentParam.runtime =
        Boolean.valueOf(currentAttributes.getValue("RUNTIME")).booleanValue();
    }else if(elementName.toUpperCase().equals("VIEW")){
      for(int i=0, len=currentAttributes.getLength(); i<len; i++) {
        viewFeatures.put(
          currentAttributes.getQName(i).toUpperCase(),
          currentAttributes.getValue(i)
        );
      }// End for
    }// End if

    // if there are any parameters awaiting addition to the list, add them
    // (note that they're not disjunctive or previous "/OR" would have got 'em)
    if(elementName.toUpperCase().equals("OR")) {
      if(! currentParamDisjunction.isEmpty()) {
        currentParamList.addAll(currentParamDisjunction);
        currentParamDisjunction = new ArrayList();
      }// End if
    }// End if
  } // startElement()

  /** Utility function to throw exceptions on stack errors. */
  private void checkStack(String methodName, String elementName)
  throws GateSaxException {
    if(contentStack.isEmpty())
      throw new GateSaxException(
        methodName + " called for element " + elementName + " with empty stack"
      );
  } // checkStack

  /** Called when the SAX parser encounts the end of an XML element.
    * This is where ResourceData objects get values set, and where
    * they are added to the CreoleRegister when we parsed their complete
    * metadata entries.
    */
  public void endElement (String uri, String qName, String elementName)
                                                    throws GateSaxException {
    if(DEBUG) Out.prln("endElement: " + elementName);

    //////////////////////////////////////////////////////////////////
    if(elementName.toUpperCase().equals("RESOURCE")) {
      // check for validity of the resource data
      if(! resourceData.isValid())
        throw new GateSaxException(
          "Invalid resource data: " + resourceData.getValidityMessage()
        );

      // add the new resource data object to the creole register
      register.put(resourceData.getClassName(), resourceData);

      // if the resource is auto-loading, try and load it
      if(resourceData.isAutoLoading())
        try {
          Resource res = Factory.createResource(
            resourceData.getClassName(), Factory.newFeatureMap()
          );
          resourceData.addInstantiation(res);
        } catch(ResourceInstantiationException e) {
          throw new GateSaxException(
            "Couldn't load autoloading resource: " +
            resourceData.getName() + "; problem was: " + e
          );
        }// End try

      // if there are any parameters awaiting addition to the list, add them
      // (note that they're not disjunctive or the "/OR" would have got them)
      if(! currentParamDisjunction.isEmpty()) {
        currentParamList.addAll(currentParamDisjunction);
        currentParamDisjunction = new ArrayList();
      }// End if

      // add the parameter list to the resource (and reinitialise it)
      resourceData.setParameterList(currentParamList);
      currentParamList = new ParameterList();

      if(DEBUG) Out.println("added: " + resourceData);

    // End RESOURCE processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("NAME")) {
      checkStack("endElement", "NAME");
      resourceData.setName((String) contentStack.pop());
    // End NAME processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("JAR")) {
      checkStack("endElement", "JAR");

      // add jar file name
      String jarFileName = (String) contentStack.pop();
      resourceData.setJarFileName(jarFileName);

      // add jar file URL if there is one
      if(sourceUrl != null) {
        String sourceUrlName = sourceUrl.toExternalForm();
        String separator = "/";

        if(sourceUrlName.endsWith(separator))
          separator = "";
        URL jarFileUrl = null;

        try {
          jarFileUrl = new URL(sourceUrlName + separator + jarFileName);
          resourceData.setJarFileUrl(jarFileUrl);

          // add the jar URL to the class loader
          if(DEBUG) Out.prln("adding URL to classloader: " + jarFileUrl);
          Gate.getClassLoader().addURL(jarFileUrl);
        } catch(MalformedURLException e) {
          throw new GateSaxException("bad URL " + jarFileUrl + e);
        }// End try
      }// End if
    // End JAR processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("XML")) {
      checkStack("endElement", "XML");

      // add XML file name
      String xmlFileName = (String) contentStack.pop();
      resourceData.setXmlFileName(xmlFileName);

      // add xml file URL if there is one
      if(sourceUrl != null) {
        String sourceUrlName = sourceUrl.toExternalForm();
        String separator = "/";
        if(sourceUrlName.endsWith(separator))
          separator = "";
        URL xmlFileUrl = null;

        try {
          xmlFileUrl = new URL(sourceUrlName + separator + xmlFileName);
          resourceData.setXmlFileUrl(xmlFileUrl);
        } catch(MalformedURLException e) {
          throw new GateSaxException("bad URL " + xmlFileUrl + e);
        }// End try
      }// End if
    // End XML processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("CLASS")) {
      checkStack("endElement", "CLASS");
      resourceData.setClassName((String) contentStack.pop());
    // End CLASS processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("COMMENT")) {
      checkStack("endElement", "COMMENT");
      resourceData.setComment((String) contentStack.pop());
    // End COMMENT processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("INTERFACE")) {
      checkStack("endElement", "INTERFACE");
      resourceData.setInterfaceName((String) contentStack.pop());
    // End INTERFACE processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("ICON")) {
      checkStack("endElement", "ICON");
      resourceData.setIcon((String) contentStack.pop());
    // End ICON processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("OR")) {
      currentParamList.add(currentParamDisjunction);
      currentParamDisjunction = new ArrayList();
    // End OR processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("PARAMETER")) {
      checkStack("endElement", "PARAMETER");
      currentParam.typeName = (String) contentStack.pop();
      currentParamDisjunction.add(currentParam);
      if(DEBUG)
        Out.prln("added param: " + currentParam);
      currentParam = new Parameter();
    // End PARAMETER processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("VIEW")) {
      checkStack("endElement", "VIEW");
      String viewType = (String) contentStack.pop();
      viewFeatures.put("TYPE", viewType);
      resourceData.addView(viewFeatures);
      if(DEBUG)
        Out.prln("added view: " + viewFeatures);

      // clear the holding map for the next view
      viewFeatures = Factory.newFeatureMap();
    // End VIEW processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("AUTOLOAD")) {
      resourceData.setAutoLoading(true);
    // End AUTOLOAD processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("PRIVATE")) {
      resourceData.setPrivate(true);
    // End PRIVATE processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("TOOL")) {
      resourceData.setTool(true);
    // End TOOL processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("CREOLE")) {
    // End CREOLE processing
    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("CREOLE-DIRECTORY")) {
    // End CREOLE-DIRECTORY processing
    //////////////////////////////////////////////////////////////////
    } else { // arbitrary elements get added as features of the resource data
      if(resourceData != null)
        resourceData.getFeatures().put(
          elementName.toUpperCase(),
          ((contentStack.isEmpty()) ? null : (String) contentStack.pop())
        );
    }
    //////////////////////////////////////////////////////////////////

  } // endElement

  /** Called when the SAX parser encounts text (PCDATA) in the XML doc */
  public void characters(char[] text, int start, int length)
  throws SAXException {
    // Get the trimmed text between elements
    String content = new String(text, start, length).trim();
    // If the entire text is empty or is made from whitespaces then we simply
    // return
    if (content.length() == 0) return;
    contentStack.push(content);
    if(DEBUG) Out.println(content);
  } // characters

  /** Called when the SAX parser encounts white space */
  public void ignorableWhitespace(char ch[], int start, int length)
  throws SAXException {
  } // ignorableWhitespace

  /** Called for parse errors. */
  public void error(SAXParseException ex) throws SAXException {
    _seh.error(ex);
  } // error

  /** Called for fatal errors. */
  public void fatalError(SAXParseException ex) throws SAXException {
    _seh.fatalError(ex);
  } // fatalError

  /** Called for warnings. */
  public void warning(SAXParseException ex) throws SAXException {
    _seh.warning(ex);
  } // warning

} // CreoleXmlHandler
