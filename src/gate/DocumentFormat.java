/*
 *	DocumentFormat.java
 *
 *	Hamish Cunningham, 25/May/2000
 *
 *	$Id$
 */

package gate;

import java.util.*;
import java.net.*;
import org.w3c.www.mime.*;
import gate.util.*;

/** The format of Documents. Subclasses of DocumentFormat know about
  * particular MIME types and how to unpack the information in any
  * markup or formatting they contain into GATE annotations. Each MIME
  * type has its own subclass of DocumentFormat, e.g. XmlDocumentFormat,
  * RtfDocumentFormat, MpegDocumentFormat. These classes register themselves
  * with a static index residing here when they are constructed. Static
  * getDocumentFormat methods can then be used to get the appropriate
  * format class for a particular document.
  */
public abstract class DocumentFormat implements Resource
{
  /** The MIME type of this format. */
  private MimeType mimeType;

  /** Map of MimeType to DocumentFormat Class. This is used to find the
    * DocumentFormat subclass that deals with a particular MIME type.
    */
  static private Map mime2ClassMap = new HashMap();

  /** Map of Set of file suffixes to MimeType. This is used to figure
    * out what MIME type a document is from its file name.
    */
  static private Map suffixes2MimeTypeMap = new HashMap();

  /** Map of Set of magic numbers to MimeType. This is used to guess the
    * MIME type of a document, when we don't have any other clues.
    */
  static private Map magic2MimeTypeMap = new HashMap();

  /** Map of markup elements to annotation types. If it is null, the
    * unpackMarkup() method will convert all markup, using the element names
    * for annotation types. If it is non-null, only those elements specified
    * here will be converted.
    */
  private Map markupElementsMap = null;

  /** The features of this resource */
  private FeatureMap features = null;

  /** Default construction */
  public DocumentFormat() { register(); }

  /** Construction with a map of what markup elements we want to
    * convert when doing unpackMarkup(), and what annotation types
    * to convert them to.
    */
  public DocumentFormat(Map markupElementsMap) {
    this.markupElementsMap = markupElementsMap;
    register();
  } // construction with map

  /** This method populates the various maps of MIME type to format,
    * file suffix and magic numbers.
    */
  private void register() { }

  /** Unpack the markup in the document. This converts markup from the
    * native format (e.g. XML, RTF) into annotations in GATE format.
    * Uses the markupElementsMap to determine which elements to convert, and
    * what annotation type names to use.
    */
  abstract public void unpackMarkup(Document doc);

  /** Find a DocumentFormat implementation that deals with a particular
    * MIME type, given that type.
    */
  static public DocumentFormat getDocumentFormat(MimeType mimeType) {
    return null;
  } // getDocumentFormat(MimeType)

  /** Find a DocumentFormat implementation that deals with a particular
    * MIME type, given the file suffix (e.g. ".txt") that the document came
    * from.
    */
  static public DocumentFormat getDocumentFormat(String fileSuffix) {
    return null;
  } // getDocumentFormat(String)

  /** Find a DocumentFormat implementation that deals with a particular
    * MIME type, given the URL of the Document. If it is an HTTP URL, we 
    * can ask the web server. If it has a recognised file extension, we
    * can use that. Otherwise we need to use a map of magic numbers
    * to MIME types to guess the type, and then look up the format using the
    * type.
    */
  static public DocumentFormat getDocumentFormat(URL url) {
    return null;
  } // getDocumentFormat(URL)

  /** Get the feature set */
  public FeatureMap getFeatures() { return features; }

  /** Set the feature set */
  public void setFeatures(FeatureMap features) { this.features = features; }

} // class DocumentFormat
