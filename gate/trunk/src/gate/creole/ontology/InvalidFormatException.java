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

/** An exception thrown when invalid format of an ontology file is detected */
public class InvalidFormatException extends GateException{

  /** the ontology file */
  private String file;
  /** the url of the file */
  private URL url;

  /** The basic exception message */
  private final static String MSG = "Invalid format of file is detected; file: ";

  /**
   * Construction given file and comment
   * @param file the ontology file
   * @param comment comment of the exception
   */
  public InvalidFormatException(String file,String comment) {
    super(MSG+file+"\n"+(null==comment ? "" : comment));
  }

  /**
   * Construction given file URL and comment
   * @param url the ontology url
   * @param comment comment of the exception
   */
  public InvalidFormatException(URL url,String comment) {
    super(MSG+url.toString()+"\n"+(null==comment ? "" : comment));
  }

  public InvalidFormatException() {
    super(MSG);
  }

  /**
   * Gets the file associated with this exception
   * @return the file associated with this exception
   */
  public String getFile(){
    return file;
  }

  /**
   * Gets the URL associated with this exception
   * @return the URL associated with this exception
   */
  private URL getURL() {
    return url;
  }
} // class InvalidFormatException