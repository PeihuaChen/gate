/*
 *	SimpleErrorHandle.java
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
 *	Cristian URSU,  8/May/2000
 *
 *  $Id$
 */

package gate.xml;

import java.io.*;
import org.xml.sax.*;

import gate.util.*;

public class SimpleErrorHandler implements ErrorHandler {
  /**
    *  This field is "final static" because it brings in
    *  the advantage of dead code elimination
    *  When DEBUG is set on false the code that it guardes will be eliminated
    *  by the compiler. This will spead up the progam a little bit.
    */
  private static final boolean DEBUG = false;

  /**
    * SimpleErrorHandler constructor comment.
    */
  public SimpleErrorHandler() {
	  super();
  }
  /**
    * error method comment.
    */
  public void error(SAXParseException ex) throws SAXException {
	  File fInput = new File (ex.getSystemId());
	  Err.println("e: " + fInput.getPath() + ": line " +
                                                ex.getLineNumber() + ": " + ex);
    Err.println("This is recoverable error. ");

  }
  /**
    * fatalError method comment.
    */
  public void fatalError(SAXParseException ex) throws SAXException {
	  File fInput = new File(ex.getSystemId());
	  Err.println("E: " + fInput.getName() + ": line " +
                                               ex.getLineNumber() + ": " + ex);
    Err.println("This is fatal error. ");
  }
  /**
    * warning method comment.
    */
  public void warning(SAXParseException ex) throws SAXException {
	  File fInput = new File(ex.getSystemId());
	  Err.println("w: " + fInput.getName() + ": line " +
                                               ex.getLineNumber() + ": " + ex);
    Err.println("This is just a warning. ");
  }
}