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

  /**
    *  This field is "final static" because it brings in
    *  the advantage of dead code elimination
    *  When DEBUG is set on false the code that it guardes will be eliminated
    *  by the compiler. This will spead up the progam a little bit.
    */
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
  public void startDocument() throws SAXException {
    if(DEBUG) Out.prln("start document");
  } // startDocument

  /** Called when the SAX parser encounts the end of the XML document */
  public void endDocument() throws SAXException {
    if(! elementStack.isEmpty()) {
      StringBuffer errorMessage =
        new StringBuffer("document ended but element stack not empty:");
      while(! elementStack.isEmpty())
        errorMessage.append((String) elementStack.pop());
      throw new SAXException(errorMessage.toString());
    }
  } // endDocument

  /** Called when the SAX parser encounts the beginning of an XML element */
  public void startElement(String elementName, AttributeList atts){
    //elementStack.push(elementName);

    if(elementName.toUpperCase().equals("RESOURCE"))
      resourceData = new ResourceDataImpl();

    if(DEBUG) {
      Out.println(
        elementName + " " +
        ((atts.getLength() > 0) ? atts.toString() : "")
      );
    }
  } // startElement

  /** Utility function to throw exceptions on stack errors. */
  private void checkStack(String methodName, String elementName)
  throws SAXException {
    if(elementStack.isEmpty())
      throw new SAXException(
        methodName + " called for element " + elementName + " with empty stack"
      );
  } // checkStack

  /** Called when the SAX parser encounts the end of an XML element */
  public void endElement(String elementName)
  throws SAXException {

    if(elementName.toUpperCase().equals("RESOURCE")) {

      // add the new resource data object to the creole register
      register.put(resourceData.getName(), resourceData);
      if(DEBUG)
        Out.println("added: " + resourceData);

    } else if(elementName.toUpperCase().equals("NAME")) {
      checkStack("endElement", "NAME");

      // add the resource name
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
          throw new SAXException("bad URL " + jarFileUrl + e);
        }
      }

    } else if(elementName.toUpperCase().equals("CLASS")) {
      checkStack("endElement", "CLASS");

      // add class name
      resourceData.setClassName((String) elementStack.pop());

    } else if(elementName.toUpperCase().equals("AUTOLOAD")) {

      // add autoloading flag
      resourceData.setAutoLoading(true);
    }
  } // endElement

  /** Called when the SAX parset encounts text in the XML doc */
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