/*
 *  RtfDocumentFormat.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU, 26/July/2000
 *
 *  $Id$
 */

package gate.corpora;

import java.util.*;
import java.io.*;
import java.net.*;

import gate.util.*;
import gate.*;
import gate.gui.*;
import gate.creole.*;

// rtf tools
import javax.swing.text.rtf.*;
import javax.swing.text.*;
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
public class RtfDocumentFormat extends TextualDocumentFormat
{

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Default construction */
  public RtfDocumentFormat() { super(); }

  /** Unpack the markup in the document. This converts markup from the
    * native format (e.g.RTF) into annotations in GATE format.
    * Uses the markupElementsMap to determine which elements to convert, and
    * what annotation type names to use.
    * It always tryes to parse te doc's content. It doesn't matter if the
    * sourceUrl is null or not.
    *
    * @param Document doc The gate document you want to parse.
    *
    */
  public void unpackMarkup(gate.Document doc) throws DocumentFormatException {

    if ( (doc == null) ||
         (doc.getSourceUrl() == null && doc.getContent() == null)){

      throw new DocumentFormatException(
               "GATE document is null or no content found. Nothing to parse!");
    }// End if

    // create a RTF editor kit
    RTFEditorKit aRtfEditorkit = new RTFEditorKit();

    // create a Styled Document
    // NOTE that RTF Kit works only with Systled Document interface
    StyledDocument styledDoc = new DefaultStyledDocument();

    // get an Input stream from the gate document
    InputStream in = new ByteArrayInputStream(
                                         doc.getContent().toString().getBytes()
                                         );

    try {
      aRtfEditorkit.read(in, styledDoc, 0);
      // replace the document content with the one without markups
      doc.setContent(new DocumentContentImpl(
                                      styledDoc.getText(0,styledDoc.getLength())
                                            )
                    );
    } catch (BadLocationException e) {
      throw new DocumentFormatException(e);
    } catch (IOException e){
      throw new DocumentFormatException("I/O exception for " +
                                        doc.getSourceUrl().toExternalForm(),e);
    }
  } // unpackMarkup(doc)

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
   public void unpackMarkup(gate.Document doc,
                            String        originalContentFeatureType)
                                                throws DocumentFormatException{

     FeatureMap fm = doc.getFeatures ();
     if (fm == null)
        fm = new SimpleFeatureMapImpl();
     fm.put(originalContentFeatureType, doc.getContent().toString());
     doc.setFeatures(fm);
     unpackMarkup (doc);
  }// unpackMarkup(doc, originalContentFeatureType)

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException{
    // Register RTF mime type
    MimeType mime = new MimeType("text","rtf");
    // Register the class handler for this mime type
    mimeString2ClassHandlerMap.put(mime.getType()+ "/" + mime.getSubtype(),
                                                                          this);
    // Register the mime type with mine string
    mimeString2mimeTypeMap.put(mime.getType() + "/" + mime.getSubtype(), mime);
    // Register file sufixes for this mime type
    suffixes2mimeTypeMap.put("rtf",mime);
    // Register magic numbers for this mime type
    magic2mimeTypeMap.put("{\\rtf1",mime);
    // Set the mimeType for this language resource
    setMimeType(mime);
    return this;
  }// init()

}// class RtfDocumentFormat
