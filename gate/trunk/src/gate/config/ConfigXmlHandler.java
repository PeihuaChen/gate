/*
 *  ConfigXmlHandler.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 9/Nov/2000
 *
 *  $Id$
 */

package gate.config;

import java.util.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.xml.parsers.*;
import java.net.*;

import gate.*;
import gate.util.*;

////// rem later?
import gate.creole.*;


/** This is a SAX handler for processing <CODE>gate.xml</CODE> files.
  */
public class ConfigXmlHandler extends DefaultHandler {

  /** A stack to stuff PCDATA onto for reading back at element ends.
   *  (Probably redundant to have a stack as we only push one item
   *  onto it. Probably. Ok, so I should check, but a) it works, b)
   *  I'm bald already and c) life is short.)
   */
  private Stack contentStack = new Stack();

  /** The current resource data object */
  private SystemData systemData;

  /** The current element's attribute list */
  private Attributes currentAttributes;

  /** A feature map representation of the current element's attribute list */
  private FeatureMap currentAttributeMap;

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** The source URL of the config file being parsed. */
  private URL sourceUrl;

  /** Construction */
  public ConfigXmlHandler(URL configUrl) {
    this.register = Gate.getCreoleRegister();
    this.sourceUrl = configUrl;
  } // construction

  /** The register object that we add CREOLE directories to during parsing.
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

  /** Called when the SAX parser encounts the beginning of an XML element */
  public void startElement (String uri, String elementName,String qName,
                                                             Attributes atts){

    if(DEBUG) {
      Out.pr("startElement: ");
      Out.println(
        elementName + " " +
        ((atts != null) && (atts.getLength() > 0) ? atts.toString() : "")
      );
    }

    // record the attributes of this element for endElement()
    currentAttributes = atts;
    currentAttributeMap = attributeListToParameterList();

    // if it's a SYSTEM, create a new one and set its name
    if(elementName.toUpperCase().equals("SYSTEM")) {
      systemData = new SystemData();
      for(int i=0, len=currentAttributes.getLength(); i<len; i++) {
        if(currentAttributes.getLocalName(i).toUpperCase().equals("NAME"))
          systemData.systemName = currentAttributes.getValue(i);
      }
    }

  } // startElement

  /** Utility function to throw exceptions on stack errors. */
  private void checkStack(String methodName, String elementName)
  throws GateSaxException {
    if(contentStack.isEmpty())
      throw new GateSaxException(
        methodName + " called for element " + elementName + " with empty stack"
      );
  } // checkStack

  /** Called when the SAX parser encounts the end of an XML element.
    * This is actions happen.
    */
  public void endElement (String uri, String elementName, String qName)
                                                      throws GateSaxException {
    if(DEBUG) Out.prln("endElement: " + elementName);

    //////////////////////////////////////////////////////////////////
    if(elementName.toUpperCase().equals("GATE")) {

    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("CREOLE-DIRECTORY")) {
      String dirUrlName = (String) contentStack.pop();
      try {
        register.addDirectory(new URL(dirUrlName));
      } catch(MalformedURLException e) {
        throw new GateSaxException("bad URL " + dirUrlName + e);
      }

    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("SYSTEM")) {
// check we got correct params on systemData?
      systemData.createSystem();

    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("CONTROLLER")) {
      systemData.controllerTypeName = (String) contentStack.pop();

    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("LR")) {
      // create an LR and add it to the SystemData
      createResource((String) contentStack.pop(), systemData.lrList);

    //////////////////////////////////////////////////////////////////
    } else if(elementName.toUpperCase().equals("PR")) {
      // create a PR and add it to the SystemData
      createResource((String) contentStack.pop(), systemData.prList);

    //////////////////////////////////////////////////////////////////
    } else {
      throw new GateSaxException(
        "Unknown config data element: " + elementName +
        "; encountered while parsing " + sourceUrl
      );
    }
    //////////////////////////////////////////////////////////////////

  } // endElement

  /** Called when the SAX parser encounts text (PCDATA) in the XML doc */
  public void characters(char[] text, int start, int length)
  throws SAXException {

    String content = new String(text, start, length);

    // this gets called when all that text is is spaces...
    // don't want to do anything with them, hence this loop:
    boolean isSpaces = true;
    char contentChars[] = content.toCharArray();

    for(int i=0, len=contentChars.length; i < len; i++)
      if(! Character.isWhitespace(contentChars[i])) {
        isSpaces = false;
        break;
      }

    if(isSpaces) return;

    contentStack.push(content);

    if(DEBUG) Out.println(content);

  } // characters

  /** Utility method to create a resource and add to appropriate list.
   *  Parameters for the resource are pulled out of the current attribute
   *  list.
   */
  protected void createResource(String resourceTypeName, List resourceList)
  throws GateSaxException
  {
    if(DEBUG) Out.prln(resourceTypeName + ": " + currentAttributeMap);
    try {
      resourceList.add(
        Factory.createResource(
          resourceTypeName, currentAttributeMap
        )
      );
    } catch(ResourceInstantiationException e) {
      throw new GateSaxException(
        "Couldn't create resource for SYSTEM: " +
        systemData.systemName + "; problem was: " + Strings.getNl() + e
      );
    }
  } // createResource

  /** Utility method to convert the current SAX attribute list to a
   *  FeatureMap
   */
  protected FeatureMap attributeListToParameterList() {
    FeatureMap params = Factory.newFeatureMap();

    // for each attribute of this element, add it to the param list
    for(int i=0, len=currentAttributes.getLength(); i<len; i++) {
      params.put(
        currentAttributes.getLocalName(i), currentAttributes.getValue(i)
      );
    }

    return params;
  } // attributeListToParameterList

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

} // ConfigXmlHandler
