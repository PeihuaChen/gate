/*
 *  EmailDocumentFormat.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU, 3/Aug/2000
 *
 *  $Id$
 */

package gate.corpora;

import java.util.*;
import java.io.*;
import java.net.*;

import gate.util.*;
import gate.*;
import gate.email.*;
import gate.event.*;
import gate.creole.*;

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
public class EmailDocumentFormat extends TextualDocumentFormat
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Default construction */
  public EmailDocumentFormat() { super();}

  /** Unpack the markup in the document. This converts markup from the
    * native format (e.g. EMAIL) into annotations in GATE format.
    * Uses the markupElementsMap to determine which elements to convert, and
    * what annotation type names to use.
    * It always tryes to parse te doc's content. It doesn't matter if the
    * sourceUrl is null or not.
    *
    * @param Document doc The gate document you want to parse.
    *
    */

  public void unpackMarkup(gate.Document doc) throws DocumentFormatException{
    if ( (doc == null) ||
         (doc.getSourceUrl() == null && doc.getContent() == null)){

      throw new DocumentFormatException(
               "GATE document is null or no content found. Nothing to parse!");
    }// End if
    // create an EmailDocumentHandler
    EmailDocumentHandler emailDocHandler = null;
    emailDocHandler = new  gate.email.EmailDocumentHandler(
                                                       doc,
                                                       this.markupElementsMap,
                                                       this.element2StringMap);
    StatusListener statusListener = new StatusListener(){
        public void statusChanged(String text) {
          // this is implemented in DocumentFormat.java and inherited here
          fireStatusChanged(text);
        }//statusChanged(String text)
    };
    // Register a status listener with it
    emailDocHandler.addStatusListener(statusListener);
    try{
      // Call the method that creates annotations on the gate document
      emailDocHandler.annotateMessages();

    } catch (IOException e){
      throw new DocumentFormatException("Couldn't create a buffered reader ",e);
    } catch (InvalidOffsetException e){
      throw new DocumentFormatException(e);
    }finally{
      emailDocHandler.removeStatusListener(statusListener);
    }// End try
  }//unpackMarkup(doc)

  /** Unpack the markup in the document. This converts markup from the
    * native format (e.g. EMAIL) into annotations in GATE format.
    * Uses the markupElementsMap to determine which elements to convert, and
    * what annotation type names to use.
    * It uses the same behaviour as
    * <code>unpackMarkup(Document doc);</code> but the document's old content is
    * preserved into a feature attached to the doc.
    *
    * @param gate.Document doc The gate document you want to parse and create
    * annotations
    * @param String originalContentFeatureType The name of a feature that will
    * preserve the old content of the document.
    */
   public void unpackMarkup(gate.Document doc,
                                    String  originalContentFeatureType)
                                               throws DocumentFormatException{
     FeatureMap fm = doc.getFeatures ();

     if (fm == null)
        fm = new SimpleFeatureMapImpl();

     fm.put(originalContentFeatureType, doc.getContent().toString());
     doc.setFeatures(fm);
     unpackMarkup(doc);
  }// unpackMarkup(doc, originalContentFeatureType)

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException{
    // Register EMAIL mime type
    MimeType mime = new MimeType("text","email");
    // Register the class handler for this mime type
    mimeString2ClassHandlerMap.put(mime.getType()+ "/" + mime.getSubtype(),
                                                                          this);
    // Register the mime type with mine string
    mimeString2mimeTypeMap.put(mime.getType() + "/" + mime.getSubtype(), mime);
    // Register file sufixes for this mime type
    suffixes2mimeTypeMap.put("eml",mime);
    suffixes2mimeTypeMap.put("email",mime);
    suffixes2mimeTypeMap.put("mail",mime);
    // Register magic numbers for this mime type
    magic2mimeTypeMap.put("Subject:",mime);
    // Set the mimeType for this language resource
    setMimeType(mime);
    return this;
  }// init()
}// class EmailDocumentFormat

