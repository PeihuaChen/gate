/*
 *	XmlDocumentFormat.java
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
 *	Cristian URSU, 26/May/2000
 *
 *	$Id$
 */

package gate.corpora;

//import com.sun.xml.parser.* ;
import java.util.*;
import java.io.*;
import java.net.*;

import gate.util.*;
import gate.*;
import gate.gui.*;

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
  /**
    *  This field is "final static" because it brings in
    *  the advantage of dead code elimination
    *  When DEBUG is set on false the code that it guardes will be eliminated
    *  by the compiler. This will spead up the progam a little bit.
    */
  private static final boolean DEBUG = false;

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
    // create the element2String map
    Map anElement2StringMap = null;
    anElement2StringMap = new HashMap();
    // populate it
    anElement2StringMap.put("S","\n\n");
    anElement2StringMap.put("s","\n\n");
    setElement2StringMap(anElement2StringMap);
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
		  SAXParser xmlParser = saxParserFactory.newSAXParser();

      // use it
      if (null != doc){
        // create a new Xml document handler
        gate.xml.XmlDocumentHandler xmlDocHandler =  new
                    gate.xml.XmlDocumentHandler(doc, this.markupElementsMap,
                                                this.element2StringMap);
        // register a status listener with it 
        xmlDocHandler.addStatusListener(new StatusListener(){
          public void statusChanged(String text){
            // this is implemented in DocumentFormat.java and inherited here
            fireStatusChangedEvent(text);
          }
        });
        // parse the document handler
        xmlParser.parse(doc.getSourceURL().toString(), xmlDocHandler );
      }

	  } catch (Exception ex) {
      ex.printStackTrace(Err.getPrintWriter());
		  //System.exit(2);
	  }
  }

  /** Unpack the markup in the document. This converts markup from the
    * native format (e.g. XML, RTF) into annotations in GATE format.
    * Uses the markupElementsMap to determine which elements to convert, and
    * what annotation type names to use.
    * It also uses the originalContentfeaturetype to preserve the original
    * content of the Gate document.
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
}//class XmlDocumentFormat