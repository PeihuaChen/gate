/*
 *  SimpleErrorHandle.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU,  8/May/2000
 *
 *  $Id$
 */

package gate.xml;

import java.io.*;
import org.xml.sax.*;

import gate.util.*;

public class SimpleErrorHandler implements ErrorHandler {

  /** Debug flag */
  private static final boolean DEBUG = false;

  /**
    * SimpleErrorHandler constructor comment.
    */
  public SimpleErrorHandler() {
    super();
  }

  /**
    * This error method is called by the SAX parser when it encounts a
    * recoverable(can continue parsing) error.
    */
  public void error(SAXParseException ex) throws SAXException {
    File fInput = new File (ex.getSystemId());

    if (fInput != null)
      Out.prln("SAX parser recoverable error:" + fInput.getPath() +
                                  ": line " + ex.getLineNumber() + ": " + ex);
    else
      Out.prln("SAX parser recoverable error:" +
                                  ": line " + ex.getLineNumber() + ": " + ex);
  }// error
  /**
    * This fatalError method is called by the SAX parser when it encounts a
    * fatal(can't continue parsing) error.
    */
  public void fatalError(SAXParseException ex) throws SAXException {
    File fInput = new File(ex.getSystemId());
    if (fInput != null)
      throw new GateSaxException("Fatal error: " + fInput.getName() +
                                ": line " + ex.getLineNumber() + ": " + ex);
    else
      throw new GateSaxException("Fatal error:" + ex.getLineNumber() +
              ": " + ex);
  }// fatalError
  /**
    * This warning is called by the SAX parser when there is the danger of a
    * confusion.
    */
  public void warning(SAXParseException ex) throws SAXException {
    File fInput = new File(ex.getSystemId());
    if (fInput != null)
      Out.prln("SAX parser warning: " + fInput.getName() +
                              ": line " + ex.getLineNumber() + ": " + ex);
    else
      Out.prln("SAX parser warning: " +
                              " : line " + ex.getLineNumber() + ": " + ex);

  }// warning
}// end class SimpleErrorHandler
