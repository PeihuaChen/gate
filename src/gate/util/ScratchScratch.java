package gate.util;

import java.util.*;
import java.io.*;
// xml tools
import org.xml.sax.*;
import javax.xml.parsers.*;

import gate.creole.tokeniser.*;

public class ScratchScratch {

  public ScratchScratch() {
  }

  public void doIt()throws Exception{
	  try {

		  // Get a parser factory.
		  SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		  // Set up the factory to create the appropriate type of parser

      // non validating one
		  saxParserFactory.setValidating(false);
      // non namesapace aware one
		  saxParserFactory.setNamespaceAware(false);

      // create it
		  SAXParser parser = saxParserFactory.newSAXParser();

      // use it
      parser.parse("file:///d:/tmp/attributes.xml",new CustomDocumentHandler());
	  } catch (Exception ex) {
      ex.printStackTrace(System.err);
		  //System.exit(2);
	  }
  }
  public static void main(String[] args) {
    ScratchScratch scratchScratch = new ScratchScratch();
    try{
      scratchScratch.doIt();
    }catch(Exception e){
      e.printStackTrace(System.err);
    }

  }
}


/**
  * Implements the behaviour of the XML reader
  */
class CustomDocumentHandler extends HandlerBase{

  // member data

  /**
    * Constructor
    */
  public CustomDocumentHandler(){
  }

  /**
    * this method is called when the SAX parser encounts the beginning of the
    * XML document
    */
  public void startDocument() throws org.xml.sax.SAXException {
  }

  /**
    * this method is called when the SAX parser encounts the end of the
    * XML document
    */
  public void endDocument() throws org.xml.sax.SAXException {
  }

  /**
    * this method is called when the SAX parser encounts the beginning of an
    * XML element
    */
  public void startElement(String elemName, AttributeList atts){
    System.out.println(elemName + " " + atts);

  }

  /**
    * this method is called when the SAX parser encounts the end of an
    * XML element
    */
  public void endElement(String elemName) throws SAXException{
  }

  /**
  *  This method is called when the SAX parset encounts text int the XMl doc
  */
  public void characters( char[] text, int start, int length) throws SAXException{

    // some internal objects
    String content = new String(text, start, length);

  }

  /**
  * this method is called when the SAX parser encounts white spaces
  */
  public void ignorableWhitespace(char ch[], int start, int length) throws SAXException{
  }

  /**
  * error method comment.
  */
  public void error(SAXParseException ex) throws SAXException {
  }

  /**
  * fatalError method comment.
  */
  public void fatalError(SAXParseException ex) throws SAXException {
  }

  /**
  * warning method comment.
  */
  public void warning(SAXParseException ex) throws SAXException {
  }

} //CustomDocumentHandler

