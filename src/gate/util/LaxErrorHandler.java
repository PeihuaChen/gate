/*
 *  LaxErrorHandler.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *  
 *  Cristian URSU,  7/July/2000
 *
 *  $Id$
 */
package gate.util;

/**
 * LaxErrorHandler
 */
import java.io.*;
import org.xml.sax.*;

public abstract class LaxErrorHandler implements ErrorHandler {

  /** Debug flag */
  private static final boolean DEBUG = false;

  /**
   * LaxErrorHandler constructor comment.
   */
  public LaxErrorHandler() {super();}

  /**
   * error method comment.
   */
  public abstract void error(SAXParseException ex) throws SAXException;

  /**
   * fatalError method comment.
   */
  public abstract void fatalError(SAXParseException ex) throws SAXException ;

  /**
   * warning method comment.
   */
  public abstract void warning(SAXParseException ex) throws SAXException ;

} // class LaxErrorHandler
