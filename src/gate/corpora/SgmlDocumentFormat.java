/*
 *	SgmlDocumentFormat.java
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
import gate.creole.*;

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
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Default construction */
  public SgmlDocumentFormat() { super(); }

  /** Unpack the markup in the document. This converts markup from the
    * native format (e.g. XML, RTF) into annotations in GATE format.
    * Uses the markupElementsMap to determine which elements to convert, and
    * what annotation type names to use.
    */
  public void unpackMarkup(Document doc) throws DocumentFormatException{
    try {
      Sgml2Xml sgml2Xml = new Sgml2Xml(doc);

      fireStatusChangedEvent("Performing SGML to XML...");

      // convert the SGML document
      String xmlUri = sgml2Xml.convert();

      fireStatusChangedEvent("DONE !");

      //Out.println("Conversion done..." + xmlUri);
      //Out.println(sgml2Xml.convert());
      // Get a parser factory.
      SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
      // Set up the factory to create the appropriate type of parser

      // Set up the factory to create the appropriate type of parser
      // non validating one
      saxParserFactory.setValidating(false);
      // non namesapace aware one
      saxParserFactory.setNamespaceAware(false);

      // Create a SAX parser
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
     }// end if
    } catch (ParserConfigurationException e){
        throw
        new DocumentFormatException("XML parser configuration exception ", e);
    } catch (SAXException e){
        throw new DocumentFormatException(e);
    } catch (IOException e){
        throw new DocumentFormatException("I/O exception for " +
                                      doc.getSourceUrl().toString());
    }

  }// unpackMarkup

  private String sgml2Xml(Document doc) {
    String xmlUri = doc.getSourceUrl().toString ();

    return xmlUri;
  }

  /** Unpack the markup in the document. This converts markup from the
    * native format (e.g. XML, RTF) into annotations in GATE format.
    * Uses the markupElementsMap to determine which elements to convert, and
    * what annotation type names to use.
    * It also uses the originalContentfeaturetype to preserve the original
    * content of the Gate document.
    */
   public void unpackMarkup(Document doc,
                                    String  originalContentFeatureType)
                                                throws DocumentFormatException{
     FeatureMap fm = doc.getFeatures ();

     if (fm == null)
        fm = new SimpleFeatureMapImpl();

     fm.put(originalContentFeatureType, doc.getContent().toString());
     doc.setFeatures(fm);
     unpackMarkup (doc);
  }// unpackMarkup

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException{
    // Register SGML mime type
    MimeType mime = new MimeType("text","sgml");
    // Register the class handler for this mime type
    mimeString2ClassHandlerMap.put(mime.getType()+ "/" + mime.getSubtype(),
                                                                          this);
    // Register the mime type with mine string
    mimeString2mimeTypeMap.put(mime.getType() + "/" + mime.getSubtype(), mime);
    // Register file sufixes for this mime type
    suffixes2mimeTypeMap.put("sgm",mime);
    suffixes2mimeTypeMap.put("sgml",mime);
    setMimeType(mime);
    return this;
  }// init

}//class SgmlDocumentFormat