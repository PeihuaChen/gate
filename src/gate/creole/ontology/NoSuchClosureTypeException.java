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

  private byte type;

  private final static String  MSG = "No Such Closure Type Exception : Type = ";

  public NoSuchClosureTypeException() {
  }

  public NoSuchClosureTypeException(byte aType) {
    super(MSG + aType);
    type = aType;
  }

  public byte getType() {
    return type;
  }

} // NoSuchClosureTypeException