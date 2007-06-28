/*
 * GateOntologyException.java
 *
 * Copyright (c) 1998-2005, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * Niraj Aswani, 18/06/2007
 */
package gate.creole.ontology;

/**
 * Exception used to signal an gate ontology exception within Gate.
 */
public class GateOntologyException extends RuntimeException {

  public GateOntologyException() {
  }

  public GateOntologyException(String message) {
    super(message);
  }
  
  public GateOntologyException(String message, Throwable cause) {
    super(message, cause);
  }
  
  public GateOntologyException(Throwable e) {
    super(e);
  }
}
