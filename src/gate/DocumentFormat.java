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
import java.io.*;

import gate.util.*;
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
public abstract class DocumentFormat implements Resource,StatusReporter
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

  // listeners for status report
  protected List myStatusListeners = new LinkedList();
  
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

    suffixes2mimeStringMap.put("xml",mime.getType() + "/" + mime.getSubtype());

    // register HTML mime type
    mime = new MimeType("text","html");
    mime.addParameter ("ClassHandler","gate.corpora.HtmlDocumentFormat");
    // register the class with this map type
    mimeString2mimeTypeMap.put (mime.getType() + "/" + mime.getSubtype(), mime);

    suffixes2mimeStringMap.put("htm",mime.getType() + "/" + mime.getSubtype());
    suffixes2mimeStringMap.put("html",mime.getType() + "/" + mime.getSubtype());

    // register SGML mime type
    mime = new MimeType("text","sgml");
    mime.addParameter ("ClassHandler","gate.corpora.SgmlDocumentFormat");
    // register the class with this map type
    mimeString2mimeTypeMap.put (mime.getType() + "/" + mime.getSubtype(), mime);

    suffixes2mimeStringMap.put("sgm",mime.getType() + "/" + mime.getSubtype());
    suffixes2mimeStringMap.put("sgml",mime.getType() + "/" + mime.getSubtype());
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
    String mimeTypeString = null;
    MimeType mimeType = null;
    if (fileSufix != null){
      mimeTypeString = (String) suffixes2mimeStringMap.get(fileSufix.toLowerCase());
      if (mimeTypeString != null){
        try{
          mimeType = new MimeType(mimeTypeString);
        }catch (MimeTypeFormatException e){
          e.printStackTrace(System.err);
        }
      }
    }
    // default type
    return mimeType;
  }

    /**
    * Returns a MymeType having as input a url
    */
  static private MimeType  getMimeType(URL url){
    String mimeTypeString = null;
    String contentType = null;
    InputStream is = null;
    MimeType mimeType = null;
    String fileSufix = null;

    try{
      contentType = url.openConnection().getContentType();
    } catch (IOException e){
      e.printStackTrace(System.err);
    }
    StringTokenizer st = new StringTokenizer(contentType, ";");
    mimeTypeString = st.nextToken();
    // return the corresponding mime type
    mimeType = (MimeType) mimeString2mimeTypeMap.get(mimeTypeString);
    if (mimeType == null){
      // get the file sufix
      fileSufix = getFileSufix(url);
      // guess the mime type on the on file sufix
      mimeType = getMimeType(fileSufix);
      // if still null then perform magic numbers guess
      if (mimeType == null){
         // mimeType = guessTypeUsingMagicNumbers(is);
      }
      // if still null then surrender
    }
    // try to guess the the mime type from the filesuffix
    return mimeType;
  }

    /**
    * return the fileSuffix or null if the url doesn't have a file suffix
    *
    */
  private static String getFileSufix(URL url){
    String fileName = null;
    String fileSuffix = null;

    if (url != null){
      fileName = url.getFile();
      StringTokenizer st = new StringTokenizer(fileName,".");
      // fileSuffix is the last token
      while (st.hasMoreTokens())
        fileSuffix = st.nextToken();
    }
    return fileSuffix;
  }

  /** Find a DocumentFormat implementation that deals with a particular
    * MIME type, given that type.
    */
  static public DocumentFormat getDocumentFormat(MimeType mimeType) {
    DocumentFormat docFormat = null;
    MimeType mime = null;
    String classHandler = null;
    if (mimeType != null){
      mime = (MimeType) mimeString2mimeTypeMap.get(mimeType.getType() + "/" +
                                                   mimeType.getSubtype());
      if (mime != null){
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
          e.printStackTrace(System.err);
        }catch (IllegalAccessException e){
          e.printStackTrace(System.err);
        }catch (InstantiationException e){
          e.printStackTrace(System.err);
        }
      }
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

  //StatusReporter Implementation
  public void addStatusListener(StatusListener listener){
    myStatusListeners.add(listener);
  }
  public void removeStatusListener(StatusListener listener){
    myStatusListeners.remove(listener);
  }
  protected void fireStatusChangedEvent(String text){
    Iterator listenersIter = myStatusListeners.iterator();
    while(listenersIter.hasNext())
      ((StatusListener)listenersIter.next()).statusChanged(text);
  }
} // class DocumentFormat
