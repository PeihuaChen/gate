/*
 *	DocumentFormat.java
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
public abstract class DocumentFormat implements Resource, StatusReporter
{
  /** Debug flag */
  private static final boolean DEBUG = false;

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

  /** This map is used inside uppackMarkup() method...
    * When an element from the map is encounted, The corresponding string
    * element is added to the document content
    */
  protected Map element2StringMap = null;

  /** The features of this resource */
  private FeatureMap features = null;

  /** Default construction */
  public DocumentFormat() {}

  /** listeners for status report */
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
    //register file sufixes
    suffixes2mimeStringMap.put("xml",mime.getType() + "/" + mime.getSubtype());
    suffixes2mimeStringMap.put("xhtm",mime.getType() + "/" + mime.getSubtype());
    suffixes2mimeStringMap.put("xhtml",mime.getType() + "/" + mime.getSubtype());

    // register HTML mime type
    mime = new MimeType("text","html");
    mime.addParameter ("ClassHandler","gate.corpora.HtmlDocumentFormat");
    // register the class with this map type
    mimeString2mimeTypeMap.put (mime.getType() + "/" + mime.getSubtype(), mime);
    //register file sufixes
    suffixes2mimeStringMap.put("htm",mime.getType() + "/" + mime.getSubtype());
    suffixes2mimeStringMap.put("html",mime.getType() + "/" + mime.getSubtype());

    // register SGML mime type
    mime = new MimeType("text","sgml");
    mime.addParameter ("ClassHandler","gate.corpora.SgmlDocumentFormat");
    // register the class with this map type
    mimeString2mimeTypeMap.put (mime.getType() + "/" + mime.getSubtype(), mime);
    //register file sufixes
    suffixes2mimeStringMap.put("sgm",mime.getType() + "/" + mime.getSubtype());
    suffixes2mimeStringMap.put("sgml",mime.getType() + "/" + mime.getSubtype());

    // register RTF mime type
    mime = new MimeType("text","rtf");
    mime.addParameter ("ClassHandler","gate.corpora.RtfDocumentFormat");
    // register the class with this map type
    mimeString2mimeTypeMap.put (mime.getType() + "/" + mime.getSubtype(), mime);
    //register file sufixes
    suffixes2mimeStringMap.put("rtf",mime.getType() + "/" + mime.getSubtype());

    // register E-mail mime type
    mime = new MimeType("text","email");
    mime.addParameter ("ClassHandler","gate.corpora.EmailDocumentFormat");
    // register the class with this map type
    mimeString2mimeTypeMap.put (mime.getType() + "/" + mime.getSubtype(), mime);
    //register file sufixes
    suffixes2mimeStringMap.put("eml",mime.getType() + "/" + mime.getSubtype());

  }//register

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
     Returns a MymeType having as input a fileSufix
     If the file sufix is not recognised then a null will be returned
    */
  static private MimeType  getMimeType(String fileSufix){
    String mimeTypeString = null;
    MimeType mimeType = null;
    // if a valid fileSuffix was introduced then search for it inside the MAP
    if (fileSufix != null){
      // get a mimeType string associated with this fileSuffix
      // Eg: for html get "text/html", for xml get "text/xml"
      mimeTypeString = (String) suffixes2mimeStringMap.get(fileSufix.toLowerCase());
      // if one mimeType string was found then produce a mime Type with mimeTye
      // string
      if (mimeTypeString != null){
        try{
          mimeType = new MimeType(mimeTypeString);
        }catch (MimeTypeFormatException e){
          e.printStackTrace(Err.getPrintWriter());
        }
      }//if
    }//if
    return mimeType;
  }//getMimeType

    /**
     Returns a MymeType having as input a url
    */
  static private MimeType  getMimeType(URL url){
    String mimeTypeString = null;
    String contentType = null;
    InputStream is = null;
    MimeType mimeType = null;
    String fileSufix = null;

    // ask the web server for the content type
    // we expect to get contentType something like this:
    // "text/html; charset=iso-8859-1"
    // charset is optional
    try{
      contentType = url.openConnection().getContentType();
    } catch (IOException e){
      e.printStackTrace(Err.getPrintWriter());
    }
    // if a content Type was returned by the server, try to get the mime Type
    // string
    // if contentType is something like this:"text/html; charset=iso-8859-1"
    // try to get content Type string (text/html)
    if (contentType != null){
      StringTokenizer st = new StringTokenizer(contentType, ";");
      // we supose that the first token is the mime type string...
      // if this doesn't happen then we are wrong
      mimeTypeString     = st.nextToken().toLowerCase();
    }
    // return the corresponding mime type from the assocated MAP
    mimeType = (MimeType) mimeString2mimeTypeMap.get(mimeTypeString);
    // if mimeType is null then we failed to recognise the mime type with the
    // web server...
    // Let's try a file suffix detection
    if (mimeType == null){
      // get the file sufix from the URL
      // see method definition for more details
      fileSufix = getFileSufix(url);
      // get the mime type based on the on file sufix
      mimeType = getMimeType(fileSufix);
      // if still null then we failed to recognise the mime type with  the file
      // suffix... maybe a file sufix wasn't present or wasn't recognised by the
      // gate application
      // Let's perform a magic numbers guess.. (our last hope)
      if (mimeType == null){
         // mimeType = guessTypeUsingMagicNumbers(is);

      }
      // if still null then surrender
    }
    return mimeType;
  }//getMimeType

  /**
    Return the fileSuffix or null if the url doesn't have a file suffix
    If the url is null then the file suffix will be null also
  */
  private static String getFileSufix(URL url){
    String fileName = null;
    String fileSuffix = null;

    // GIGO test  (garbage in garbage out)
    if (url != null){
       // get the file name from the URL
      fileName = url.getFile();
      // tokenize this file name with "." as separator...
      // the last token will be the file suffix
      StringTokenizer st = new StringTokenizer(fileName,".");
      // fileSuffix is the last token
      while (st.hasMoreTokens())
        fileSuffix = st.nextToken();
      // here fileSuffix is the last token
    }
    return fileSuffix;
  }//getFileSufix

  /**
    Find a DocumentFormat implementation that deals with a particular
    MIME type, given that type.
    @param  aGateDocument this document will receive as a feature
                          the associated Mime Type. The name of the feature is
                          MimeType and its value is in the format type/subtype
    @param  mimeType the mime type that is given as input
  */
  static public DocumentFormat getDocumentFormat(gate.Document aGateDocument,
                                                            MimeType mimeType){
    DocumentFormat  docFormat     = null;
    MimeType        mime          = null;
    String          classHandler  = null;
    FeatureMap      aFeatureMap    = null;

    if (mimeType != null){
      // if mime type is not null then try to get from the following map
      // the mime type wich has the class handler attached as a parameter value
      mime = (MimeType) mimeString2mimeTypeMap.get(mimeType.getType() + "/" +
                                                   mimeType.getSubtype());
      if (mime != null){
        // here we are in the position to get the class handler
        try{
          // extract the name of the class handler
          classHandler = mime.getParameterValue("ClassHandler");
          // create a new instance of the corresponding class handler
          docFormat = (DocumentFormat) Class.forName(classHandler).newInstance();
          //attach to the Gate Document received as a parameter, the detected
          // mime type

          // if the Gate Document doesn't have a feature map atached then
          // we will create and set one.
          if(aGateDocument.getFeatures() == null){
            aFeatureMap = new SimpleFeatureMapImpl();
            aGateDocument.setFeatures(aFeatureMap);
          }
          aGateDocument.getFeatures().put("MimeType",mime.getType() + "/" +
                                          mime.getSubtype());
        }catch (ClassNotFoundException e){
          e.printStackTrace(Err.getPrintWriter());
        }catch (IllegalAccessException e){
          e.printStackTrace(Err.getPrintWriter());
        }catch (InstantiationException e){
          e.printStackTrace(Err.getPrintWriter());
        }
      }//if
    }//if
    return docFormat;
  } // getDocumentFormat(aGateDocument, MimeType)

  /**
    Find a DocumentFormat implementation that deals with a particular
    MIME type, given the file suffix (e.g. ".txt") that the document came
    from.
    @param  aGateDocument this document will receive as a feature
                          the associated Mime Type. The name of the feature is
                          MimeType and its value is in the format type/subtype
    @param  fileSuffix the file suffix that is given as input
  */
  static public DocumentFormat getDocumentFormat(gate.Document aGateDocument,
                                                            String fileSuffix) {
    return getDocumentFormat(aGateDocument, getMimeType(fileSuffix));
  } // getDocumentFormat(String)

  /**
    Find a DocumentFormat implementation that deals with a particular
    MIME type, given the URL of the Document. If it is an HTTP URL, we
    can ask the web server. If it has a recognised file extension, we
    can use that. Otherwise we need to use a map of magic numbers
    to MIME types to guess the type, and then look up the format using the
    type.
    @param  aGateDocument this document will receive as a feature
                          the associated Mime Type. The name of the feature is
                          MimeType and its value is in the format type/subtype
    @param  url  the URL that is given as input
    */
  static public DocumentFormat getDocumentFormat(gate.Document aGateDocument,
                                                                      URL url) {
    return getDocumentFormat(aGateDocument, getMimeType(url));
  } // getDocumentFormat(URL)

  /** Get the feature set */
  public FeatureMap getFeatures() { return features; }

   /** Get the markup elements map */
  public Map getMarkupElementsMap() { return markupElementsMap; }

   /** Get the element 2 string map */
  public Map getElement2StringMap() { return element2StringMap; }

  /** Set the markup elements map */
  public void setMarkupElementsMap(Map markupElementsMap) {
   this.markupElementsMap = markupElementsMap;
  }

  /** Set the element 2 string map */
  public void setElement2StringMap(Map anElement2StringMap) {
   element2StringMap = anElement2StringMap;
  }

  public void setFeatures(FeatureMap features){}

  //StatusReporter Implementation
  public void addStatusListener(StatusListener listener){
    myStatusListeners.add(listener);
  }
  public void removeStatusListener(StatusListener listener){
    myStatusListeners.remove(listener);
  }
  // this is a bug in Soraris on JDK 1.21.
  // it has to be protected not public 
  public void fireStatusChangedEvent(String text){
    Iterator listenersIter = myStatusListeners.iterator();
    while(listenersIter.hasNext())
      ((StatusListener)listenersIter.next()).statusChanged(text);
  }
} // class DocumentFormat