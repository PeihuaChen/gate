/*
 *	TextualDocumentFormat.java
 *
 *	Cristian URSU, 26/May/2000
 *
 *	$Id$
 */

package gate.corpora;

import java.util.*;
import java.net.*;

import gate.util.*;
import gate.*;

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
public class TextualDocumentFormat extends DocumentFormat
{

  /** Default construction */
  public TextualDocumentFormat() { super(); }

  /** Construction with a map of what markup elements we want to
    * convert when doing unpackMarkup(), and what annotation types
    * to convert them to.
    */
  public TextualDocumentFormat(Map markupElementsMap) {
    super(markupElementsMap);
  } // construction with map

  /** Unpack the markup in the document. This converts markup from the
    * native format (e.g. XML, RTF) into annotations in GATE format.
    * Uses the markupElementsMap to determine which elements to convert, and
    * what annotation type names to use.
    */
  public void unpackMarkup(Document doc){
    System.out.println("UNPACK called from TextualDocumentFormat");
  }

  public void unpackMarkup(Document doc,
                                    String  originalContentFeatureType){
    System.out.println("UNPACK called from TextualDocumentFormat");
  }

  /** Get the factory that created this object. */
  public Factory getFactory() {
    throw new LazyProgrammerException();
  } // getFactory()


} // class TextualDocumentFormat
