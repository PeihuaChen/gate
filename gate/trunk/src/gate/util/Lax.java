/*
 *  Lax.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU, 07/July/2000
 *
 *  $Id$
 */

package gate.util;

import org.xml.sax.*;
import java.lang.reflect.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import java.util.*;
import java.io.*;

/** LAX (LazyProgrammer Api for XML) layer for a SAX parser,
  * based on Sun's JAXP layer...so it works with any JAXP compliant parser
  */
public class Lax extends org.xml.sax.helpers.DefaultHandler {

  /** Debug flag */
  private static final boolean DEBUG = false;

	// LAX translates XML content into method calls on this object
	private Vector _vecHandlers = null;

	private Vector _vecTags = null;

	private static Class[] _caNoArgs = null;

	private static Class[] _caAttrList = null;

	private static Class[] _caString = null;

	private LaxErrorHandler _seh = null;

  private boolean _validatingParser = false;

  private boolean _namespaceAwareParser = false;

	// Initialize class arrays used for reflection
	static {
		_caNoArgs = new Class[] {};
		_caAttrList = new Class[] {org.xml.sax.Attributes.class};
		_caString = new Class[] {java.lang.String.class};
	}

  /**
    * Lax default constructor
    */
  public Lax(LaxErrorHandler leh) {
    super();
    _vecHandlers = new Vector();
    _vecTags = new Vector();
    _seh = leh;
  }

  /**
    * Lax ctor with a single handler
    */
  public Lax(Object handler_ , LaxErrorHandler leh) {
    super();
    _vecHandlers = new Vector();
    _vecTags = new Vector();
    addHandler(handler_);
    _seh = leh;
  }

  /**
    * Sets the CustomErrorHandler
    * @param leh gate.util.LaxErrorHandler
    */
  public void setErrorHandler(LaxErrorHandler leh) {
    _seh = leh;
  }

  /**
    * Get the CustomErrorHandler
    * @return gate.util.LaxErrorHandler
    */
  public gate.util.LaxErrorHandler getErrorHandler() {
    return _seh;
  }

  /**
    * Sets the parser to be a validating one
    * implicit parameter is false (so the parser is not a validating one)
    * @param validating boolean
    */
  public void setValidating(boolean validating) {
    _validatingParser = validating;
  }

  /**
    * Get the validating property
    * @return boolean
    */
  public boolean getValidating(){
    return _validatingParser;
  }

  /**
    * Sets the parser to be a namespaces aware one
    * implicit parameter is false (so the parser is not a namespaces aware one)
    * @param namespacesAware boolean
    */
  public void setNamespacesAware(boolean namespacesAware) {
    _namespaceAwareParser = namespacesAware;
  }

  /**
    * Get the namespacesAware property
    * @return boolean
    */
  public boolean getNamespacesAware(){
    return _namespaceAwareParser;
  }

  /**
    * Add a handler to the list of handler objects.
    * @param objHandler_ java.lang.Object
    */
  public void addHandler(Object objHandler_) {
    _vecHandlers.addElement(objHandler_);
  }

  /**
    * Handle an incoming block of text by calling the textOf method for the
    * current tag.
    */
  public void characters(char[] caChars, int iStart, int iEnd)
                                                          throws SAXException {
    String sCurrentTag = sCurrentTag();

    if (sCurrentTag != null) {
      int i;
      String sTextMethodName = "textOf" + sCurrentTag;
      String sArg = null;

      // Call every text method for current tag found in the list of handlers.
      for (i = 0; i < _vecHandlers.size(); i++) {
        Object oThisHandler = _vecHandlers.elementAt(i);
        Method mTextMethod = mFindMethod(oThisHandler, sTextMethodName, _caString);
        if (mTextMethod != null) {
          try {
            if (sArg == null) {
              sArg = new String(caChars, iStart, iEnd);
            }
            mTextMethod.invoke(oThisHandler, new Object[] { sArg });
          } catch (InvocationTargetException ex) {
            Err.println(ex);
          } catch (IllegalAccessException ex) {
            Err.println(ex);
          }
        }
      }
    }
  } // characters

  /**
    * endDocument method comment.
    */
  public void endDocument() throws org.xml.sax.SAXException {
  }

  /**
    * Call all end tag methods in the handler list
    */
  public void endElement (String uri, String sTag, String qName)
                                                          throws SAXException{
    int i;
    String sEndMethodName = "end" + sTag;

    // Call every tag start method for this tag found in the list of handlers.
    for (i = 0; i < _vecHandlers.size(); i++) {
      Object oThisHandler = _vecHandlers.elementAt(i);
      Method mEndMethod = mFindMethod(oThisHandler, sEndMethodName, _caNoArgs);
      if (mEndMethod != null) {
        try {
          mEndMethod.invoke(oThisHandler, new Object[] {});
        } catch (InvocationTargetException ex) {
          Err.println(ex);
        } catch (IllegalAccessException ex) {
          Err.println(ex);
        }
      }
    }
    popTag();
  } // endElement

  /**
    * error method comment.
    */
  public void error(SAXParseException ex) throws SAXException {
    _seh.error(ex);
  }

  /**
    * fatalError method comment.
    */
  public void fatalError(SAXParseException ex) throws SAXException {
    _seh.fatalError(ex);
  }

  /**
    * Return a method of object oHandler
    * with the given name and argument list, or null if not found
    * @return java.lang.reflect.Method
    * @param oHandler java.lang.Object - The handler object to search for a
    * method.
    * @param sTag java.lang.String - The tag to find.
    */
  private Method mFindMethod(Object oHandler, String sMethodName,
                                                              Class[] caArgs) {
    Method m = null;
    Class classOfHandler = oHandler.getClass();

    // Find a method with the given name and argument list
    try {
      m = classOfHandler.getMethod(sMethodName, caArgs);
    } catch (NoSuchMethodException ex) {
      // Ignore exception - no such method exists.
    }
    return m;
  } // mFindMethod

  public void parseXmlDocument(java.io.File xmlFile) {
    try {
      // Get a "parser factory", an an object that creates parsers
      SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

      // Set up the factory to create the appropriate type of parser
      saxParserFactory.setValidating(_validatingParser);
      saxParserFactory.setNamespaceAware(_namespaceAwareParser);

      SAXParser parser = saxParserFactory.newSAXParser();

      parser.parse(xmlFile, this);
    } catch (Exception ex) {
      ex.printStackTrace(Err.getPrintWriter());
      // System.exit(2);
    }
  } // parseXmlDocument

  public void parseXmlDocument(org.xml.sax.InputSource xmlInputSource) {
    try {
      // Get a "parser factory", an an object that creates parsers
      SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

      // Set up the factory to create the appropriate type of parser
      saxParserFactory.setValidating(_validatingParser);
      saxParserFactory.setNamespaceAware(_namespaceAwareParser);

      SAXParser parser = saxParserFactory.newSAXParser();

      parser.parse(xmlInputSource, this);
    } catch (Exception ex) {
      ex.printStackTrace(Err.getPrintWriter());
      // System.exit(2);
    }
  } // parseXmlDocument

  public void parseXmlDocument(java.io.InputStream  xmlInputStream) {
    try {
      // Get a "parser factory", an an object that creates parsers
      SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

      // Set up the factory to create the appropriate type of parser
      saxParserFactory.setValidating(_validatingParser);
      saxParserFactory.setNamespaceAware(_namespaceAwareParser);

      SAXParser parser = saxParserFactory.newSAXParser();

      parser.parse(xmlInputStream, this);
    } catch (Exception ex) {
      ex.printStackTrace(Err.getPrintWriter());
      // System.exit(2);
    }
  } // parseXmlDocument(java.io.InputStream  xmlInputStream)

  public void parseXmlDocument(java.lang.String xmlURI) {
    try {
      // Get a "parser factory", an an object that creates parsers
      SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

      // Set up the factory to create the appropriate type of parser
      saxParserFactory.setValidating(_validatingParser);
      saxParserFactory.setNamespaceAware(_namespaceAwareParser);

      SAXParser parser = saxParserFactory.newSAXParser();

      parser.parse(xmlURI, this);
    } catch (Exception ex) {
      ex.printStackTrace(Err.getPrintWriter());
      //System.exit(2);
    }
  } // parseXmlDocument(java.lang.String xmlURI)

  /**
    * Pop tag off of tag stack.
    */
  private void popTag() {
    _vecTags.removeElementAt(_vecTags.size() - 1);
  }

  /**
    * Push tag onto tag stack.
    * @param sTag java.lang.String
    */
  private void pushTag(String sTag) {
    _vecTags.addElement(sTag);
  }

  /**
    * Return tag at top of tag stack. At any particular point in the parse,
    * this string represents the tag being processed.
    * @return java.lang.String
    */
  private String sCurrentTag() {
    int iIndex = _vecTags.size() - 1;
    if (iIndex >= 0) {
      return (String)(_vecTags.elementAt(_vecTags.size() - 1));
    } else {
      return null;
    }
  } // sCurrentTag()

  /**
    * startDocument method comment.
    */
  public void startDocument() throws org.xml.sax.SAXException {
  }

  /**
    * Call all start methods for this tag.
    */
  public void startElement (String uri, String sTag,String qName,
                                                            Attributes alAttrs){

    int i;
    String sStartMethodName = "start" + sTag;

    pushTag(sTag);

    // Call every tag start method for this tag found in the list of handlers.
    for (i = 0; i < _vecHandlers.size(); i++) {
      Object oThisHandler = _vecHandlers.elementAt(i);
      Method mStartMethod = mFindMethod(
                              oThisHandler, sStartMethodName, _caAttrList);
      if (mStartMethod == null) {
        mStartMethod = mFindMethod(oThisHandler, sStartMethodName, _caNoArgs);
      }
      if (mStartMethod != null) {

        try {
          // Call start method with or without attribute list
          Class[] caMethodArgs = mStartMethod.getParameterTypes();
          if (caMethodArgs.length == 0) {
            mStartMethod.invoke(oThisHandler, new Object[] {});
          } else {
            mStartMethod.invoke(oThisHandler, new Object[] {alAttrs});
          }
        } catch (InvocationTargetException ex) {
          Err.println(ex);
        } catch (IllegalAccessException ex) {
          Err.println(ex);
        }
      }
    }
  } // startElement

  /**
    * warning method comment.
    */
  public void warning(SAXParseException ex) throws SAXException {
    _seh.warning(ex);
  }

} // class Lax
