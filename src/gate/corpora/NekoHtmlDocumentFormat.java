/*
 *  XmlDocumentFormat.java
 *
 *  Copyright (c) 1998-2005, The University of Sheffield.
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

// import com.sun.xml.parser.* ;
import gate.Document;
import gate.GateConstants;
import gate.Resource;
import gate.TextualDocument;
import gate.creole.ResourceInstantiationException;
import gate.event.StatusListener;
import gate.html.NekoHtmlDocumentHandler;
import gate.util.DocumentFormatException;
import gate.util.Out;
import gate.xml.XmlDocumentHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;

import org.cyberneko.html.parsers.SAXParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

// import org.w3c.www.mime.*;

/**
 * The format of Documents. Subclasses of DocumentFormat know about
 * particular MIME types and how to unpack the information in any markup
 * or formatting they contain into GATE annotations. Each MIME type has
 * its own subclass of DocumentFormat, e.g. XmlDocumentFormat,
 * RtfDocumentFormat, MpegDocumentFormat. These classes register
 * themselves with a static index residing here when they are
 * constructed. Static getDocumentFormat methods can then be used to get
 * the appropriate format class for a particular document.
 */
public class NekoHtmlDocumentFormat extends TextualDocumentFormat {
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Default construction */
  public NekoHtmlDocumentFormat() {
    super();
  }

  private HashSet<String> ignorableTags = null;

  public void setIgnorableTags(HashSet<String> newTags) {
    this.ignorableTags = newTags;
  }

  public HashSet<String> getIgnorableTags() {
    return ignorableTags;
  }

  /** We could collect repositioning information during XML parsing */
  public Boolean supportsRepositioning() {
    return new Boolean(true);
  } // supportsRepositioning

  /** Old style of unpackMarkup (without collecting of RepositioningInfo) */
  public void unpackMarkup(Document doc) throws DocumentFormatException {
    unpackMarkup(doc, (RepositioningInfo)null, (RepositioningInfo)null);
  } // unpackMarkup

  /**
   * Unpack the markup in the document. This converts markup from the
   * native format (e.g. XML) into annotations in GATE format. Uses the
   * markupElementsMap to determine which elements to convert, and what
   * annotation type names to use. If the document was created from a
   * String, then is recomandable to set the doc's sourceUrl to <b>null</b>.
   * So, if the document has a valid URL, then the parser will try to
   * parse the XML document pointed by the URL.If the URL is not valid,
   * or is null, then the doc's content will be parsed. If the doc's
   * content is not a valid XML then the parser might crash.
   * 
   * @param doc The gate document you want to parse. If
   *          <code>doc.getSourceUrl()</code> returns <b>null</b>
   *          then the content of doc will be parsed. Using a URL is
   *          recomended because the parser will report errors corectlly
   *          if the XML document is not well formed.
   */
  public void unpackMarkup(Document doc, RepositioningInfo repInfo,
          RepositioningInfo ampCodingInfo) throws DocumentFormatException {
    if((doc == null)
            || (doc.getSourceUrl() == null && doc.getContent() == null)) {

      throw new DocumentFormatException(
              "GATE document is null or no content found. Nothing to parse!");
    }// End if

    // Create a status listener
    StatusListener statusListener = new StatusListener() {
      public void statusChanged(String text) {
        // This is implemented in DocumentFormat.java and inherited here
        fireStatusChanged(text);
      }
    };
    boolean docHasContentButNoValidURL = hasContentButNoValidUrl(doc);

    NekoHtmlDocumentHandler handler = null;
    try {
      org.cyberneko.html.parsers.SAXParser saxParser = new SAXParser();

      // convert element and attribute names to lower case
      saxParser.setProperty("http://cyberneko.org/html/properties/names/elems",
              "lower");
      saxParser.setProperty("http://cyberneko.org/html/properties/names/attrs",
              "lower");

      // Create a new Xml document handler
      handler = new NekoHtmlDocumentHandler(doc, null, ignorableTags);
      // Register a status listener with it
      handler.addStatusListener(statusListener);
      // set repositioning object
      handler.setRepositioningInfo(repInfo);
      // set the object with ampersand coding positions
      handler.setAmpCodingInfo(ampCodingInfo);

      // Set up the factory to create the appropriate type of parser
      // non validating one
      // http://xml.org/sax/features/validation set to false
      // newxmlParser.setFeature("http://xml.org/sax/features/validation",
      // false);
      // namesapace aware one
      // http://xml.org/sax/features/namespaces set to true
      // newxmlParser.setFeature("http://xml.org/sax/features/namespaces",
      // true);
      // newxmlParser.setFeature("http://xml.org/sax/features/namespace-prefixes",
      // true);
      saxParser.setContentHandler(handler);
      saxParser.setErrorHandler(handler);
      saxParser.setDTDHandler(handler);
      saxParser.setEntityResolver(handler);
      // Parse the XML Document with the appropriate encoding
      InputSource is;

      if(docHasContentButNoValidURL) {
        // no URL, so parse from string
        is = new InputSource(new StringReader(doc.getContent().toString()));
      }
      else if(doc instanceof TextualDocument) {
        // textual document - load with user specified encoding
        String docEncoding = ((TextualDocument)doc).getEncoding();
        Reader docReader = new InputStreamReader(doc.getSourceUrl()
                .openStream(), docEncoding);
        is = new InputSource(docReader);
        // must set system ID to allow relative URLs (e.g. to a DTD) to
        // work
        is.setSystemId(doc.getSourceUrl().toString());

        // since we control the encoding, tell the parser to ignore any
        // http-equiv hints
        saxParser
                .setFeature(
                        "http://cyberneko.org/html/features/scanner/ignore-specified-charset",
                        true);
      }
      else {
        // let the parser decide the encoding
        is = new InputSource(doc.getSourceUrl().toString());
      }
      saxParser.parse(is);
      // Angel - end
      ((DocumentImpl)doc).setNextAnnotationId(handler.getCustomObjectsId());
    }
    catch(SAXException e) {
      doc.getFeatures().put("parsingError", Boolean.TRUE);

      Boolean bThrow = (Boolean)doc.getFeatures().get(
              GateConstants.THROWEX_FORMAT_PROPERTY_NAME);

      if(bThrow != null && bThrow.booleanValue()) {
        // the next line is commented to avoid Document creation fail on
        // error
        throw new DocumentFormatException(e);
      }
      else {
        Out.println("Warning: Document remains unparsed. \n"
                + "\n  Stack Dump: ");
        e.printStackTrace(Out.getPrintWriter());
      } // if

    }
    catch(IOException e) {
      throw new DocumentFormatException("I/O exception for "
              + doc.getSourceUrl().toString(), e);
    }
    finally {
      if(handler != null) handler.removeStatusListener(statusListener);
    }// End if else try

  }

  /**
   * This is a test to see if the GATE document has a valid URL or a
   * valid content. If doesn't has a valid URL then try to parse its
   * content as XML
   * 
   * @param doc
   * @throws DocumentFormatException
   */
  private static boolean hasContentButNoValidUrl(Document doc)
          throws DocumentFormatException {
    try {
      if(doc.getSourceUrl() == null && doc.getContent() != null) {
        // The doc's url is null but there is a content.
        return true;
      }
      else {
        doc.getSourceUrl().openConnection();
      }
    }
    catch(IOException ex1) {
      // The URL is not null but is not valid.
      if(doc.getContent() == null)
      // The document content is also null. There is nothing we can do.
        throw new DocumentFormatException("The document doesn't have a"
                + " valid URL and also no content");
      return true;
    }// End try

    return false;
  }

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {
    // Register HTML mime type
    MimeType mime = new MimeType("text", "html");
    // Register the class handler for this mime type
    mimeString2ClassHandlerMap.put(mime.getType() + "/" + mime.getSubtype(),
            this);
    // Register the mime type with mine string
    mimeString2mimeTypeMap.put(mime.getType() + "/" + mime.getSubtype(), mime);
    // Register file sufixes for this mime type
    suffixes2mimeTypeMap.put("html", mime);
    suffixes2mimeTypeMap.put("htm", mime);
    // Register magic numbers for this mime type
    magic2mimeTypeMap.put("<html", mime);
    // Set the mimeType for this language resource
    setMimeType(mime);
    return this;
  }// init()

}// class XmlDocumentFormat
