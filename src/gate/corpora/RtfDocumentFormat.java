/*
 *	RtfDocumentFormat.java
 *
 *	Cristian URSU, 26/July/2000
 *
 *	$Id$
 */

package gate.corpora;

import java.util.*;
import java.io.*;
import java.net.*;

import gate.util.*;
import gate.*;
import gate.gui.*;

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
  /** Default construction */
  public RtfDocumentFormat() { super(); }

  /** Construction with a map of what markup elements we want to
    * convert when doing unpackMarkup(), and what annotation types
    * to convert them to.
    */
  public RtfDocumentFormat(Map markupElementsMap) {
    super(markupElementsMap);
  } // construction with map

  /** Unpack the markup in the document. This converts markup from the
    * native format (e.g. XML, RTF) into annotations in GATE format.
    * Uses the markupElementsMap to determine which elements to convert, and
    * what annotation type names to use.
    */
  public void unpackMarkup(gate.Document doc){
    // create a RTF editor kit
    RTFEditorKit aRtfEditorkit = new RTFEditorKit();
    // create a Styled Document
    // NOTE that RTF Kit works only with Systled Document interface
    StyledDocument styledDoc = new DefaultStyledDocument();
    // get an Input stream from the gate document
    InputStream in = new ByteArrayInputStream(doc.getContent().toString().getBytes());
    try{
      aRtfEditorkit.read(in, styledDoc, 0);
      // replace the document content with the one without markups
      doc.setContent(new DocumentContentImpl(styledDoc.getText(0,styledDoc.getLength())));
    } catch (Exception e){
      e.printStackTrace(System.err);
    }
  }//unpackMarkup

  /** Unpack the markup in the document. This converts markup from the
    * native format (e.g. XML, RTF) into annotations in GATE format.
    * Uses the markupElementsMap to determine which elements to convert, and
    * what annotation type names to use.
    * It also uses the originalContentfeaturetype to preserve the original content
    * of the Gate document
    */
   public void unpackMarkup(gate.Document doc,
                                    String  originalContentFeatureType){

     FeatureMap fm = doc.getFeatures ();
     if (fm == null)
        fm = new SimpleFeatureMapImpl();
     fm.put(originalContentFeatureType, doc.getContent().toString());
     doc.setFeatures(fm);
     unpackMarkup (doc);
  }
} // class XmlDocumentFormat
