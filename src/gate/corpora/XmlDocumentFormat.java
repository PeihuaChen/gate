/*
 *  XmlDocumentFormat.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU, 26/May/2000
 *
 *  $Id$
 */

package gate.corpora;

//import com.sun.xml.parser.* ;
import java.util.*;
import java.io.*;
import java.net.*;

import gate.util.*;
import gate.*;
import gate.xml.*;
import gate.event.*;
import gate.creole.*;

// xml tools
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.w3c.www.mime.*;

/** The format of Documents. Subclasses of DocumentFormat know about
  * particular MIME types and how to unpack the information in any
  * markup or formatting they contain into GATE annotations. Each MIME
  * type has its own subclass of DocumentFormat, e.g. XmlDocumentFormat,
  * RtfDocumentFormat, MpegDocumentFormat. These classes register themselves
  * with a static index residing here when they are constructed. Static
  * getDocumentFormat methods can then be used to get the appropriate
  * format class for a particular document.
  */
public class XmlDocumentFormat extends TextualDocumentFormat
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Default construction */
  public XmlDocumentFormat() { super(); }

  /** Unpack the markup in the document. This converts markup from the
    * native format (e.g. XML) into annotations in GATE format.
    * Uses the markupElementsMap to determine which elements to convert, and
    * what annotation type names to use. If the document was created from a
    * String, then is recomandable to set the doc's sourceUrl to <b>null</b>.
    * So, if the document has a valid URL, then the parser will try to
    * parse the XML document pointed by the URL.If the URL is not valid, or
    * is null, then the doc's content will be parsed. If the doc's content is
    * not a valid XML then the parser might crash.
    *
    * @param Document doc The gate document you want to parse. If
    * <code>doc.getSourceUrl()</code> returns <b>null</b> then the content of
    * doc will be parsed. Using a URL is recomended because the parser will
    * report errors corectlly if the XML document is not well formed.
    */
  public void unpackMarkup(Document doc) throws DocumentFormatException{
/*
    // create the element2String map
    Map anElement2StringMap = null;
    anElement2StringMap = new HashMap();

    // populate it
    anElement2StringMap.put("S","\n\n");
    anElement2StringMap.put("s","\n\n");
    setElement2StringMap(anElement2StringMap);
*/
    if ( (doc == null) ||
         (doc.getSourceUrl() == null && doc.getContent() == null)){

      throw new DocumentFormatException(
               "GATE document is null or no content found. Nothing to parse!");
    }// End if

    boolean docHasContentButNoValidURL = false;
    // This is a test to see if the GATE document has a valid URL or a valid
    // content. If doesn't has a valid URL then try to parse its content as XML
    try{
      if (doc.getSourceUrl() == null && doc.getContent() != null){
        // The doc's url is null but there is a content.
        docHasContentButNoValidURL = true;
      }else {URLConnection conn = doc.getSourceUrl().openConnection();}
    }catch (IOException ex1){
      // The URL is not null but is not valid.
      if(doc.getContent() == null)
        // The document content is also null. There is nothing we can do.
        throw new DocumentFormatException("The document doesn't have a" +
        " valid URL and also no content");

      docHasContentButNoValidURL = true;
    }// End try

    if (docHasContentButNoValidURL)
      parseDocumentWithoutURL(doc);
    else try {
      // use Excerces XML parser with JAXP
      // System.setProperty("javax.xml.parsers.SAXParserFactory",
      //                         "org.apache.xerces.jaxp.SAXParserFactoryImpl");
      // Get a parser factory.
      SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
      // Set up the factory to create the appropriate type of parser
      // non validating one
      saxParserFactory.setValidating(false);
      // non namesapace aware one
      saxParserFactory.setNamespaceAware(false);
      // create it
      SAXParser xmlParser = saxParserFactory.newSAXParser();
      if (isGateXmlDocument){
        // Construct the appropiate xml handler for the job.
        GateFormatXmlDocumentHandler gateXmlHandler =
                        new GateFormatXmlDocumentHandler(doc);
        // Register a status listener
        gateXmlHandler.addStatusListener(new StatusListener(){
          public void statusChanged(String text){
            // This is implemented in DocumentFormat.java and inherited here
            fireStatusChanged(text);
          }
        });
        // Parse the Gate Document
        xmlParser.parse(doc.getSourceUrl().toString(), gateXmlHandler);
      }else{
        // create a new Xml document handler
        XmlDocumentHandler xmlDocHandler =  new
                    XmlDocumentHandler(doc,
                                       this.markupElementsMap,
                                       this.element2StringMap);

        // register a status listener with it
        xmlDocHandler.addStatusListener(new StatusListener(){
          public void statusChanged(String text){
            // this is implemented in DocumentFormat.java and inherited here
            fireStatusChanged(text);
          }
        });
        // parse the document handler
        xmlParser.parse(doc.getSourceUrl().toString(), xmlDocHandler );
      }// End if
    } catch (ParserConfigurationException e){
        throw
        new DocumentFormatException("XML parser configuration exception ", e);
    } catch (SAXException e){
        throw new DocumentFormatException(e);
    } catch (IOException e){
        throw new DocumentFormatException("I/O exception for " +
                                      doc.getSourceUrl().toString());
    }// End if else try

  }// unpackMarkup

  /** Unpack the markup in the document. This converts markup from the
    * native format (e.g. XML, RTF) into annotations in GATE format.
    * Uses the markupElementsMap to determine which elements to convert, and
    * what annotation type names to use.
    * It uses the same behaviour as
    * <code>unpackMarkup(Document doc);</code> but the document's old content is
    * preserved into a feature attached to the doc.
    * <p><b>WARNING :</b> If you are using this method, you should know
    * that when it comes to dump the document as a GATE XML
    * one(to assure persistence), it will result in bad document format.
    * In this case you should use the Java persistency implemented
    * in @see gate.persist package.</p>
    *
    * @param gate.Document doc The gate document you want to parse and create
    * annotations
    * @param String originalContentFeatureType The name of a feature that will
    * preserve the old content of the document.
    */
   public void unpackMarkup( Document doc,
                             String  originalContentFeature)
                                                throws DocumentFormatException{

     FeatureMap fm = doc.getFeatures ();

     if (fm == null)
        fm = new SimpleFeatureMapImpl();

     fm.put(originalContentFeature, doc.getContent().toString());
     doc.setFeatures(fm);
     unpackMarkup (doc);
  }// unpackMarkup

  private void parseDocumentWithoutURL(gate.Document aDocument)
                                              throws DocumentFormatException {

//    ByteArrayInputStream in =  new ByteArrayInputStream(
//                                  aDocument.getContent().toString().getBytes());
    try{
      Reader reader = new InputStreamReader(
        new ByteArrayInputStream(aDocument.getContent().toString().getBytes()),
        "UTF-8");
      InputSource is = new InputSource(reader);


      // use Excerces XML parser with JAXP
      // System.setProperty("javax.xml.parsers.SAXParserFactory",
      //                         "org.apache.xerces.jaxp.SAXParserFactoryImpl");
      // Get a parser factory.
      SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
      // Set up the factory to create the appropriate type of parser
      // non validating one
      saxParserFactory.setValidating(false);
      // non namesapace aware one
      saxParserFactory.setNamespaceAware(false);
      // create it
      SAXParser xmlParser = saxParserFactory.newSAXParser();
      // create a new Xml document handler
      XmlDocumentHandler xmlDocHandler =  new XmlDocumentHandler(aDocument,
                                                    this.markupElementsMap,
                                                    this.element2StringMap);

      // register a status listener with it
      xmlDocHandler.addStatusListener(new StatusListener(){
        public void statusChanged(String text){
          // this is implemented in DocumentFormat.java and inherited here
          fireStatusChanged(text);
        }
      }); // xmlDocHandler.addStatusListener(new StatusListener(){
      // parse the document handler
      xmlParser.parse(is, xmlDocHandler );
    } catch (ParserConfigurationException e){
        throw new DocumentFormatException(
                        "XML parser configuration exception ", e);
    } catch (SAXException e){
        throw new DocumentFormatException(e);
    } catch (IOException e){
        throw new DocumentFormatException(e);
    }// End try

  }// End parseDocumentWithoutURL()

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException{
    // Register XML mime type
    MimeType mime = new MimeType("text","xml");
    // Register the class handler for this mime type
    mimeString2ClassHandlerMap.put(mime.getType()+ "/" + mime.getSubtype(),
                                                                          this);
    // Register the mime type with mine string
    mimeString2mimeTypeMap.put(mime.getType() + "/" + mime.getSubtype(), mime);
    // Register file sufixes for this mime type
    suffixes2mimeTypeMap.put("xml",mime);
    suffixes2mimeTypeMap.put("xhtm",mime);
    suffixes2mimeTypeMap.put("xhtml",mime);
    // Register magic numbers for this mime type
    magic2mimeTypeMap.put("<?xml",mime);
    // Set the mimeType for this language resource
    setMimeType(mime);
    return this;
  }// init()

}//class XmlDocumentFormat
