/*
 *	CreoleXmlHandler.java
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
 *  Hamish Cunningham, 1/Sept/2000
 *
 *  $Id$
 */

package gate.creole;

import java.util.*;

import org.xml.sax.*;
import javax.xml.parsers.*;
import java.net.*;

import gate.*;
import gate.util.*;


/** This is a SAX handler for processing <CODE>creole.xml</CODE> files.
  * It would have been better to write it using DOM or JDOM but....
  * Resource data objects are created and added to the CREOLE register.
  * URLs for resource JAR files are added to the GATE class loader.
  */
public class CreoleXmlHandler extends HandlerBase {

  /** A stack to stuff data onto for reading back at element ends */
  private Stack elementStack = new Stack();

  /** The current resource data object */
  private ResourceData resourceData;

  /** The current parameter list */
  private List currentParamList = new ArrayList();

  /** The current parameter */
  private Parameter currentParam = new Parameter();

  /** The current element's attribute list */
  private AttributeList currentAttributes;

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** The source URL of the directory file being parsed. */
  private URL sourceUrl;

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
    if(! elementStack.isEmpty()) {
      StringBuffer errorMessage =
        new StringBuffer("document ended but element stack not empty:");
      while(! elementStack.isEmpty())
        errorMessage.append((String) elementStack.pop());
      throw new GateSaxException(errorMessage.toString());
    }
  } // endDocument

  /** Called when the SAX parser encounts the beginning of an XML element */
  public void startElement(String elementName, AttributeList atts){
    if(DEBUG) {
      Out.pr("startElement: ");
      Out.println(
        elementName + " " +
        ((atts != null) && (atts.getLength() > 0) ? atts.toString() : "")
      );
    }

    if(elementName.toUpperCase().equals("RESOURCE")) {
      resourceData = new ResourceDataImpl();
      resourceData.setFeatures(Factory.newFeatureMap());
    }
    currentAttributes = atts;

    // process attributes of parameter elements
    if(elementName.toUpperCase().equals("PARAMETER")) {
      if(DEBUG) {
        for(int i=0, len=currentAttributes.getLength(); i<len; i++) {
          Out.prln(currentAttributes.getName(i));
          Out.prln(currentAttributes.getValue(i));
        }
      }
      currentParam.comment = currentAttributes.getValue("COMMENT");
      currentParam.defaultValueString = currentAttributes.getValue("DEFAULT");
      currentParam.optional =
        Boolean.valueOf(currentAttributes.getValue("OPTIONAL")).booleanValue();
      currentParam.name = currentAttributes.getValue("NAME");
      currentParam.runtime =
        Boolean.valueOf(currentAttributes.getValue("RUNTIME")).booleanValue();
    }
  } // startElement

  /** Utility function to throw exceptions on stack errors. */
  private void checkStack(String methodName, String elementName)
  throws GateSaxException {
    if(elementStack.isEmpty())
      throw new GateSaxException(
        methodName + " called for element " + elementName + " with empty stack"
      );
  } // checkStack

  /** Called when the SAX parser encounts the end of an XML element.
    * This is where ResourceData objects get values set, and where
    * they are added to the CreoleRegister when we parsed their complete
    * metadata entries.
    */
  public void endElement(String elementName)
  throws GateSaxException {
    if(DEBUG) Out.prln("endElement: " + elementName);

    if(elementName.toUpperCase().equals("RESOURCE")) {
      // add the new resource data object to the creole register
      //******************************
      // check that the resource has all mandatory elements, e.g. class name
      //******************************
      if(resourceData.getInterfaceName() != null) // index by intf if present
        register.put(resourceData.getInterfaceName(), resourceData);
      else // index by class name
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
        }

      if(DEBUG) Out.println("added: " + resourceData);

    } else if(elementName.toUpperCase().equals("NAME")) {
      checkStack("endElement", "NAME");
      resourceData.setName((String) elementStack.pop());
    } else if(elementName.toUpperCase().equals("JAR")) {
      checkStack("endElement", "JAR");

      // add jar file name
      String jarFileName = (String) elementStack.pop();
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
        }
      }
    } else if(elementName.toUpperCase().equals("XML")) {
      checkStack("endElement", "XML");

      // add XML file name
      String xmlFileName = (String) elementStack.pop();
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
        }
      }
    } else if(elementName.toUpperCase().equals("CLASS")) {
      checkStack("endElement", "CLASS");
      resourceData.setClassName((String) elementStack.pop());
    } else if(elementName.toUpperCase().equals("COMMENT")) {
      checkStack("endElement", "COMMENT");
      resourceData.setComment((String) elementStack.pop());
    } else if(elementName.toUpperCase().equals("INTERFACE")) {
      checkStack("endElement", "INTERFACE");
      resourceData.setInterfaceName((String) elementStack.pop());
    } else if(elementName.toUpperCase().equals("PARAMETER-LIST")) {
      resourceData.addParameterList(currentParamList);
      currentParamList = new ArrayList();
    } else if(elementName.toUpperCase().equals("PARAMETER")) {
      checkStack("endElement", "PARAMETER");
      currentParam.valueString = (String) elementStack.pop();
      currentParamList.add(currentParam);
      currentParam = new Parameter();
    } else if(elementName.toUpperCase().equals("AUTOLOAD")) {
      resourceData.setAutoLoading(true);
    } else if(elementName.toUpperCase().equals("CREOLE")) {
    } else if(elementName.toUpperCase().equals("CREOLE-DIRECTORY")) {
    } else { // arbitrary elements get added as features of the resource data
      if(resourceData != null)
        resourceData.getFeatures().put(
          elementName.toUpperCase(),
          ((elementStack.isEmpty()) ? null : (String) elementStack.pop())
        );
    }
  } // endElement

  /** Called when the SAX parser encounts text in the XML doc */
  public void characters(char[] text, int start, int length)
  throws SAXException {

    String content = new String(text, start, length);

    // not sure why this gets called when all that text is is spaces...
    // but don't want to do anything with them, hence this loop:
    boolean isSpaces = true;
    char contentChars[] = content.toCharArray();

    for(int i=0, len=contentChars.length; i < len; i++)
      if(! Character.isWhitespace(contentChars[i])) {
        isSpaces = false;
        break;
      }

    if(isSpaces) return;

    elementStack.push(content);

    if(DEBUG) Out.println(content);

  } // characters

  /** Called when the SAX parser encounts white space */
  public void ignorableWhitespace(char ch[], int start, int length)
  throws SAXException {
  } // ignorableWhitespace

  /** Called for parse errors. */
  public void error(SAXParseException ex) throws SAXException {
  } // error

  /** Called for fatal errors. */
  public void fatalError(SAXParseException ex) throws SAXException {
  } // fatalError

  /** Called for warnings. */
  public void warning(SAXParseException ex) throws SAXException {
  } // warning

} // CreoleXmlHandler
