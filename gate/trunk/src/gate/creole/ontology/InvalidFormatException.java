/*
 * InvalidFormatException.java
 *
 * Copyright (c) 2002, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * borislav popov 16/04/2002
 *
 * $Id$
 */
package gate.creole.ontology;

import java.net.URL;
import gate.util.GateException;

/** exception thrown when an invalid format of an ontology file is detected */
public class InvalidFormatException extends GateException{

  private String file;
  private URL url;

  private final static String MSG = "Invalid format of file is detected; file: ";

  public InvalidFormatException(String file,String comment) {
    super(MSG+file+"\n"+(null==comment ? "" : comment));
  }

  public InvalidFormatException(URL url,String comment) {
    super(MSG+url.toString()+"\n"+(null==comment ? "" : comment));
  }

  public InvalidFormatException() {
    super(MSG);
  }

  public String getFile(){
    return file;
  }

  private URL getURL() {
    return url;
  }
} // class InvalidFormatException