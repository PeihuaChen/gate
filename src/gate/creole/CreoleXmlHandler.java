/*
	CreoleXmlHandler.java 

	Hamish Cunningham, 1/Sept/2000

	$Id$
*/

package gate.creole;

import java.util.*;

import org.xml.sax.*;
import javax.xml.parsers.*;

import gate.*;
import gate.util.*;


/** This is a SAX handler for processing <CODE>creole.xml</CODE> files.
  */
public class CreoleXmlHandler extends HandlerBase {

  /** A stack to stuff data onto for reading back at element ends */
  private Stack elementStack = new Stack();

  /** The current resource data object */
  private ResourceData resourceData;

  /** Are we debugging? */
  private static final boolean debug = true;

  /** Construction */
  public CreoleXmlHandler(CreoleRegister register){ this.register = register; }

  /** The register object that we add ResourceData objects to during parsing.
    */
  private CreoleRegister register;

  /** Called when the SAX parser encounts the beginning of the XML document */
  public void startDocument() throws SAXException {
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

    if(debug) {
      System.out.println(
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
  public void endElement(String elementName) throws SAXException{

    if(elementName.toUpperCase().equals("RESOURCE")) {
      register.put(resourceData.getName(), resourceData);
      
      if(debug)
        System.out.println("added: " + resourceData);
    } else if(elementName.toUpperCase().equals("NAME")) {
      checkStack("endElement", "NAME");
      resourceData.setName((String) elementStack.pop());
    } else if(elementName.toUpperCase().equals("JAR")) {
      checkStack("endElement", "JAR");
      resourceData.setJarFileName((String) elementStack.pop());
    } else if(elementName.toUpperCase().equals("CLASS")) {
      checkStack("endElement", "CLASS");
      resourceData.setClassName((String) elementStack.pop());
    } else if(elementName.toUpperCase().equals("AUTOLOAD")) {
      resourceData.setAutoLoading(true);
    }

  } // startElement

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

    if(debug) System.out.println(content);

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
