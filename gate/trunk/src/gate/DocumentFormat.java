/*
 *  DocumentFormat.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 25/May/2000
 *
 *  $Id$
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
public abstract class DocumentFormat implements LanguageResource, StatusReporter
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** The MIME type of this format. */
  private MimeType mimeType = null;

  /** Map of MimeTypeString to ClassHandler class. This is used to find the
    * language resource that deals with the specific Document format
    */
  protected static Map mimeString2ClassHandlerMap = new HashMap();
  /** Map of MimeType to DocumentFormat Class. This is used to find the
    * DocumentFormat subclass that deals with a particular MIME type.
    */
  protected static Map mimeString2mimeTypeMap = new HashMap();

  /** Map of Set of file suffixes to MimeType. This is used to figure
    * out what MIME type a document is from its file name.
    */
  protected static Map suffixes2mimeTypeMap = new HashMap();

  /** Map of Set of magic numbers to MimeType. This is used to guess the
    * MIME type of a document, when we don't have any other clues.
    */
  protected static Map magic2mimeTypeMap = new HashMap();

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
  private transient Vector statusListeners;

  /** Unpack the markup in the document. This converts markup from the
    * native format (e.g. XML, RTF) into annotations in GATE format.
    * Uses the markupElementsMap to determine which elements to convert, and
    * what annotation type names to use.
    */
  abstract public void unpackMarkup(Document doc)
                                      throws DocumentFormatException;
  /** Unpack the markup in the document. This converts markup from the
    * native format (e.g. XML, RTF) into annotations in GATE format.
    * Uses the markupElementsMap to determine which elements to convert, and
    * what annotation type names to use.
    */
  abstract public void unpackMarkup(Document doc,
                                    String  originalContentFeatureType )
                                        throws DocumentFormatException;

  /**
    * Returns a MymeType having as input a fileSufix
    * If the file sufix is not recognised then a null will be returned
    */
  static private MimeType  getMimeType(String fileSufix){
    // Get a mimeType string associated with this fileSuffix
    // Eg: for html get "text/html", for xml get "text/xml"
    if(fileSufix == null) return null;
    return (MimeType) suffixes2mimeTypeMap.get(fileSufix.toLowerCase());
  }//getMimeType

  /**
    * Returns a MymeType having as input a url
    */
  static private MimeType  getMimeType(URL url) {
    String mimeTypeString = null;
    String contentType = null;
    InputStream is = null;
    MimeType mimeTypeFromWebServer = null;
    MimeType mimeTypeFromFileSuffix = null;
    MimeType mimeTypeFromMagicNumbers = null;
    String fileSufix = null;

    // Ask the web server for the content type
    // We expect to get contentType something like this:
    // "text/html; charset=iso-8859-1"
    // Charset is optional
    try{
      is = url.openConnection().getInputStream();
      contentType = url.openConnection().getContentType();
    } catch (IOException e){
      // Failed to get the mime type with te Web server.
      // Let's try some other methods.
    }
    // If a content Type was returned by the server, try to get the mime Type
    // string
    // If contentType is something like this:"text/html; charset=iso-8859-1"
    // try to get content Type string (text/html)
    if (contentType != null){
      StringTokenizer st = new StringTokenizer(contentType, ";");
      // We assume that the first token is the mime type string...
      // If this doesn't happen then we are wrong...
      mimeTypeString     = st.nextToken().toLowerCase();
    }// end if
    // Return the corresponding mime type from the assocated MAP
    mimeTypeFromWebServer = (MimeType)
                                mimeString2mimeTypeMap.get(mimeTypeString);
    // Let's try a file suffix detection
    // Get the file sufix from the URL
    // See method definition for more details
    fileSufix = getFileSufix(url);
    // Get the mime type based on the on file sufix
    mimeTypeFromFileSuffix = getMimeType(fileSufix);

    // Let's perform a magic numbers guess..
    mimeTypeFromMagicNumbers = guessTypeUsingMagicNumbers(is);
    // If all those mimeTypes are != null the the priority is the folowing:
    // 1. mimeTypeFromFileSuffix
    // 2. mimeTypeFromWebServer
    // 3. mimeTypeFromMagicNumbers

    if(mimeTypeFromFileSuffix != null)
      return mimeTypeFromFileSuffix;
    if(mimeTypeFromWebServer != null)
      return mimeTypeFromWebServer;
    if(mimeTypeFromMagicNumbers != null)
      return mimeTypeFromMagicNumbers;
    // If all of them are null then surrender.
    return null;
  }//getMimeType
  /**
    * This method tries to guess the mime Type using some magic numbers
    */
  protected static MimeType guessTypeUsingMagicNumbers(InputStream
                                                                aInputStream){
    // Doesn't do anything right now.
    return null;
  }//guessTypeUsingMagicNumbers

  /**
    * Return the fileSuffix or null if the url doesn't have a file suffix
    * If the url is null then the file suffix will be null also
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
    } // End if
    return fileSuffix;
  }//getFileSufix

  /**
    * Find a DocumentFormat implementation that deals with a particular
    * MIME type, given that type.
    * @param  aGateDocument this document will receive as a feature
    *                      the associated Mime Type. The name of the feature is
    *                      MimeType and its value is in the format type/subtype
    * @param  mimeType the mime type that is given as input
    */
  static public DocumentFormat getDocumentFormat(gate.Document aGateDocument,
                                                            MimeType mimeType){
    FeatureMap      aFeatureMap    = null;
    if (mimeType != null){
      // If the Gate Document doesn't have a feature map atached then
      // We will create and set one.
      if(aGateDocument.getFeatures() == null){
            aFeatureMap = new SimpleFeatureMapImpl();
            aGateDocument.setFeatures(aFeatureMap);
      }// end if
      aGateDocument.getFeatures().put("MimeType",mimeType.getType() + "/" +
                                          mimeType.getSubtype());

      return (DocumentFormat) mimeString2ClassHandlerMap.get(mimeType.getType()
                                               + "/" + mimeType.getSubtype());
    }// end If
    return null;
  } // getDocumentFormat(aGateDocument, MimeType)

  /**
    * Find a DocumentFormat implementation that deals with a particular
    * MIME type, given the file suffix (e.g. ".txt") that the document came
    * from.
    * @param  aGateDocument this document will receive as a feature
    *                     the associated Mime Type. The name of the feature is
    *                     MimeType and its value is in the format type/subtype
    * @param  fileSuffix the file suffix that is given as input
    */
  static public DocumentFormat getDocumentFormat(gate.Document aGateDocument,
                                                            String fileSuffix) {
    return getDocumentFormat(aGateDocument, getMimeType(fileSuffix));
  } // getDocumentFormat(String)

  /**
    * Find a DocumentFormat implementation that deals with a particular
    * MIME type, given the URL of the Document. If it is an HTTP URL, we
    * can ask the web server. If it has a recognised file extension, we
    * can use that. Otherwise we need to use a map of magic numbers
    * to MIME types to guess the type, and then look up the format using the
    * type.
    * @param  aGateDocument this document will receive as a feature
    *                      the associated Mime Type. The name of the feature is
    *                      MimeType and its value is in the format type/subtype
    * @param  url  the URL that is given as input
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

  /** Set the features map*/
  public void setFeatures(FeatureMap features){this.features = features;}

  /** Set the mime type*/

  public void setMimeType(MimeType aMimeType){mimeType = aMimeType;}
  /** Gets the mime Type*/
  public MimeType getMimeType(){return mimeType;}

  //StatusReporter Implementation


  public synchronized void removeStatusListener(StatusListener l) {
    if (statusListeners != null && statusListeners.contains(l)) {
      Vector v = (Vector) statusListeners.clone();
      v.removeElement(l);
      statusListeners = v;
    }
  }
  public synchronized void addStatusListener(StatusListener l) {
    Vector v = statusListeners == null ? new Vector(2) : (Vector) statusListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      statusListeners = v;
    }
  }
  protected void fireStatusChanged(String e) {
    if (statusListeners != null) {
      Vector listeners = statusListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((StatusListener) listeners.elementAt(i)).statusChanged(e);
      }
    }
  }

} // class DocumentFormat
