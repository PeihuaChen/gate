/*
 *	XmlDocumentFormat.java
 *
 *	Cristian URSU, 26/May/2000
 *
 *	$Id$
 */

package gate.corpora;

//import com.sun.xml.parser.* ;
import java.util.*;
import java.io.*;
import java.net.*;
import org.w3c.www.mime.*;
import gate.util.*;
import gate.*;

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
public class XmlDocumentFormat extends TextualDocumentFormat
{

  /** Default construction */
  public XmlDocumentFormat() { super(); }

  /** Construction with a map of what markup elements we want to
    * convert when doing unpackMarkup(), and what annotation types
    * to convert them to.
    */
  public XmlDocumentFormat(Map markupElementsMap) {
    super(markupElementsMap);
  } // construction with map

  /** Unpack the markup in the document. This converts markup from the
    * native format (e.g. XML, RTF) into annotations in GATE format.
    * Uses the markupElementsMap to determine which elements to convert, and
    * what annotation type names to use.
    */
  public void unpackMarkup(Document doc){
	  try {

      // use Excerces XML parser with JAXP
      //System.setProperty("javax.xml.parsers.SAXParserFactory",
      //                         "org.apache.xerces.jaxp.SAXParserFactoryImpl");
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
        // parse and construct the gate annotations
        //String s = doc.getContent().toString();
        //StringReader sr = new StringReader(s);
        //InputSource is = new InputSource (sr);
        //doc.getSourceURL ().toString ()
        parser.parse(doc.getSourceURL ().toString (),
                new gate.xml.CustomDocumentHandler(doc, this.markupElementsMap)
        );
      }

	  } catch (Exception ex) {
      ex.printStackTrace(System.err);
		  //System.exit(2);
	  }
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
} // class XmlDocumentFormat
