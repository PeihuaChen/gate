/*
 *	HtmlDocumentFormat.java
 *
 *	Cristian URSU, 26/May/2000
 *
 *	$Id$
 */

package gate.corpora;

import java.util.*;
import java.io.*;
import java.net.*;
// html tools
import javax.swing.text.html.*;
import javax.swing.text.html.parser.*;
import javax.swing.text.html.HTMLEditorKit.*;
import javax.swing.text.*;

import gate.util.*;
import gate.*;
import gate.html.*;
import gate.gui.*;

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
public class HtmlDocumentFormat extends TextualDocumentFormat
{

  /** Default construction */
  public HtmlDocumentFormat() { super(); }

  /** Construction with a map of what markup elements we want to
    * convert when doing unpackMarkup(), and what annotation types
    * to convert them to.
    */
  public HtmlDocumentFormat(Map markupElementsMap) {
    super(markupElementsMap);
  } // construction with map

  /** Unpack the markup in the document. This converts markup from the
    * native format (e.g. XML, RTF) into annotations in GATE format.
    * Uses the markupElementsMap to determine which elements to convert, and
    * what annotation type names to use.
    */
  public void unpackMarkup(gate.Document doc){

    Reader reader = null;
    URLConnection conn = null;
    PrintWriter out = null;

    HTMLEditorKit.Parser parser = new ParserDelegator();
    try{
      conn = doc.getSourceURL().openConnection ();
      reader =  new InputStreamReader(conn.getInputStream ());
    } catch (Exception e){
      System.out.println (e);
      e.printStackTrace (System.err);
    }
    // create a new Htmldocument handler
    HtmlDocumentHandler htmlDocHandler = new
                             HtmlDocumentHandler(doc, this.markupElementsMap);
    // register a status listener with it
    htmlDocHandler.addStatusListener(new StatusListener(){
          public void statusChanged(String text){
            // this is implemented in DocumentFormat.java and inherited here
            fireStatusChangedEvent(text);
          }
    });

    try{
      // parse the HTML document
      parser.parse(reader, htmlDocHandler, true);
    } catch (Exception e){
     System.out.println (e);
      e.printStackTrace (System.err);
    }

  }

  /** Unpack the markup in the document. This converts markup from the
    * native format (e.g. XML, RTF) into annotations in GATE format.
    * Uses the markupElementsMap to determine which elements to convert, and
    * what annotation type names to use.
    * It also uses the originalContentfeaturetype to preserve the original
    * content of the Gate document.
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
}// class HtmlDocumentFormat
