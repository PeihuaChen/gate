/*
 * NoSuchClosureTypeException.java
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

import gate.util.GateException;
/**NoSuchClosureTypeException
 * <br>
 * thrown whenever a closure type mismatch ocurrs
 * <br>
 */
public class NoSuchClosureTypeException extends GateException {

  /** the type of the closure*/
  private byte type;

  /** the core message */
  private final static String  MSG = "No Such Closure Type Exception : Type = ";

  /**Constructs a new blank exception */
  public NoSuchClosureTypeException() {
  }

  /**
   * Constructs the exception given the type of the closure.
   * @param aType the type of the closure
   */
  public NoSuchClosureTypeException(byte aType) {
    super(MSG + aType);
    type = aType;
  }

  /**
   * Gets the type of the closure.
   * @return the type of the closure
   */
  public byte getType() {
    return type;
  }

} // NoSuchClosureTypeException