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
  static private Map mimeString2mimeTypeMap = new HashMap();

  /** Map of Set of file suffixes to MimeType. This is used to figure
    * out what MIME type a document is from its file name.
    */
  static private Map suffixes2mimeStringMap = new HashMap();

  /** Map of Set of magic numbers to MimeType. This is used to guess the
    * MIME type of a document, when we don't have any other clues.
    */
  static private Map magic2mimeStringMap = new HashMap();

  /** Map of markup elements to annotation types. If it is null, the
    * unpackMarkup() method will convert all markup, using the element names
    * for annotation types. If it is non-null, only those elements specified
    * here will be converted.
    */
  protected Map markupElementsMap = null;

  /** The features of this resource */
  private FeatureMap features = null;

  /** Default construction */
  public DocumentFormat() {}

  static{
    register();
  }
  /** Construction with a map of what markup elements we want to
    * convert when doing unpackMarkup(), and what annotation types
    * to convert them to.
    */
  public DocumentFormat(Map markupElementsMap) {
    this.markupElementsMap = markupElementsMap;
  } // construction with map

  /** This method populates the various maps of MIME type to format,
    * file suffix and magic numbers.
    */
  static private void register() {
    // register XML mime type
    MimeType mime = new MimeType("text","xml");
    mime.addParameter ("ClassHandler","gate.corpora.XmlDocumentFormat");
    // register the class with this map type
    mimeString2mimeTypeMap.put (mime.getType() + "/" + mime.getSubtype(), mime);

    suffixes2mimeStringMap.put("xml",mime.toString());
    suffixes2mimeStringMap.put("XML",mime.toString());
    suffixes2mimeStringMap.put("Xml",mime.toString());

    // register HTML mime type
    mime = new MimeType("text","html");
    mime.addParameter ("ClassHandler","gate.corpora.HtmlDocumentFormat");
    // register the class with this map type
    mimeString2mimeTypeMap.put (mime.getType() + "/" + mime.getSubtype(), mime);
    
    suffixes2mimeStringMap.put("htm",mime.toString());
    suffixes2mimeStringMap.put("html",mime.toString());

  }

  /** Unpack the markup in the document. This converts markup from the
    * native format (e.g. XML, RTF) into annotations in GATE format.
    * Uses the markupElementsMap to determine which elements to convert, and
    * what annotation type names to use.
    */
  abstract public void unpackMarkup(Document doc);
  /** Unpack the markup in the document. This converts markup from the
    * native format (e.g. XML, RTF) into annotations in GATE format.
    * Uses the markupElementsMap to determine which elements to convert, and
    * what annotation type names to use.
    */

  abstract public void unpackMarkup(Document doc,
                                    String  originalContentFeatureType
  );

  /**
    * Returns a MymeType having as input a fileSufix
    */
  static private MimeType  getMimeType(String fileSufix){
    // for the beginning
    return MimeType.TEXT;
  }

    /**
    * Returns a MymeType having as input a url
    */
  static private MimeType  getMimeType(URL url){
    return MimeType.TEXT;
  }

  /** Find a DocumentFormat implementation that deals with a particular
    * MIME type, given that type.
    */
  static public DocumentFormat getDocumentFormat(MimeType mimeType) {
    DocumentFormat docFormat = null;
    MimeType mime = null;
    String classHandler = null;

    mime = (MimeType) mimeString2mimeTypeMap.get(mimeType.getType() + "/" +
                                              mimeType.getSubtype());
    try{
      classHandler = mime.getParameterValue("ClassHandler");
      docFormat = (DocumentFormat) Class.forName(classHandler).newInstance();
      /*
      if (mimeType.toString ().equalsIgnoreCase ("text/xml"))
        docFormat = (gate.corpora.XmlDocumentFormat) Class.forName(
                      "gate.corpora.XmlDocumentFormat").newInstance();
      if (mimeType.toString ().equalsIgnoreCase ("text/html"))
        docFormat = (gate.corpora.HtmlDocumentFormat) Class.forName(
                      "gate.corpora.HtmlDocumentFormat").newInstance();
               */       
    }catch (ClassNotFoundException e){
      System.out.println(e);
    }catch (IllegalAccessException e){
      System.out.println(e);
    }catch (InstantiationException e){
      System.out.println(e);
    }
    return docFormat;
  } // getDocumentFormat(MimeType)

  /** Find a DocumentFormat implementation that deals with a particular
    * MIME type, given the file suffix (e.g. ".txt") that the document came
    * from.
    */
  static public DocumentFormat getDocumentFormat(String fileSuffix) {
    return getDocumentFormat (getMimeType (fileSuffix));
  } // getDocumentFormat(String)

  /** Find a DocumentFormat implementation that deals with a particular
    * MIME type, given the URL of the Document. If it is an HTTP URL, we
    * can ask the web server. If it has a recognised file extension, we
    * can use that. Otherwise we need to use a map of magic numbers
    * to MIME types to guess the type, and then look up the format using the
    * type.
    */
  static public DocumentFormat getDocumentFormat(URL url) {
    return getDocumentFormat (getMimeType (url));
  } // getDocumentFormat(URL)

  /** Get the feature set */
  public FeatureMap getFeatures() { return features; }

   /** Get the markup elements map */
  public Map getMarkupElementsMap() { return markupElementsMap; }

  /** Set the markup elements map */
  public void setMarkupElementsMap(Map markupElementsMap) {
   this.markupElementsMap = markupElementsMap;
  }

  public void setFeatures(FeatureMap features){}
} // class DocumentFormat
