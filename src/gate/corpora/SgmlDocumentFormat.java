/*
 *	SgmlDocumentFormat.java
 *
 *	Cristian URSU, 4/July/2000
 *
 *	$Id$
 */

package gate.corpora;

import java.util.*;
import java.io.*;
import java.net.*;

import gate.util.*;
import gate.*;
import gate.sgml.*;
import gate.gui.*;
import gate.xml.*;

import org.w3c.www.mime.*;
// xml tools
import javax.xml.parsers.*;
import org.xml.sax.*;

/** The format of Documents. Subclasses of DocumentFormat know about
  * particular MIME types and how to unpack the information in any
  * markup or formatting they contain into GATE annotations. Each MIME
  * type has its own subclass of DocumentFormat, e.g. XmlDocumentFormat,
  * RtfDocumentFormat, MpegDocumentFormat. These classes register themselves
  * with a static index residing here when they are constructed. Static
  * getDocumentFormat methods can then be used to get the appropriate
  * format class for a particular document.
  */
public class SgmlDocumentFormat extends TextualDocumentFormat
{

  /** Default construction */
  public SgmlDocumentFormat() { super(); }

  /** Construction with a map of what markup elements we want to
    * convert when doing unpackMarkup(), and what annotation types
    * to convert them to.
    */
  public SgmlDocumentFormat(Map markupElementsMap) {
    super(markupElementsMap);
  } // construction with map

  /** Unpack the markup in the document. This converts markup from the
    * native format (e.g. XML, RTF) into annotations in GATE format.
    * Uses the markupElementsMap to determine which elements to convert, and
    * what annotation type names to use.
    */
  public void unpackMarkup(Document doc){
	  try {

      Sgml2Xml sgml2Xml = new Sgml2Xml(doc);

      fireStatusChangedEvent("Performing SGML to XML...");
      // convert the SGML document
      String xmlUri = sgml2Xml.convert();
      fireStatusChangedEvent("DONE !");
      //System.out.println("Conversion done..." + xmlUri);
      //System.out.println(sgml2Xml.convert());

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
      if (null != doc){
        // create a new Xml document handler
        XmlDocumentHandler xmlDocHandler = new
                            XmlDocumentHandler(doc, this.markupElementsMap,
                                               this.element2StringMap);
        // register a status listener with it
        xmlDocHandler.addStatusListener(new StatusListener(){
          public void statusChanged(String text){
            // this is implemented in DocumentFormat.java and inherited here
            fireStatusChangedEvent(text);
          }
        });
        parser.parse(xmlUri, xmlDocHandler);
      }

	  } catch (Exception ex) {
      ex.printStackTrace(System.err);
		  //System.exit(2);
	  }

  }

  private String sgml2Xml(Document doc){
    String xmlUri = doc.getSourceURL ().toString ();

    return xmlUri;
  }

  /** Unpack the markup in the document. This converts markup from the
    * native format (e.g. XML, RTF) into annotations in GATE format.
    * Uses the markupElementsMap to determine which elements to convert, and
    * what annotation type names to use.
    * It also uses the originalContentfeaturetype to preserve the original content
    * of the Gate document
    */
   public void unpackMarkup(Document doc,
                                    String  originalContentFeatureType){

     FeatureMap fm = doc.getFeatures ();
     if (fm == null)
        fm = new SimpleFeatureMapImpl();
     fm.put(originalContentFeatureType, doc.getContent().toString());
     doc.setFeatures(fm);
     unpackMarkup (doc);
  }
} // class SgmlDocumentFormat
