/*
 *  GazeteerException.java
 *
 *  Copyright (c) 1998-2004, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 11/07/2000
 *
 *  $Id$
 */

package gate.creole.gazetteer;

import gate.util.GateException;

/** Used to signal Gazetteer specific exceptions */
public class GazetteerException extends GateException {

  public GazetteerException(String s) {
    super(s);
  }

} // GazetteerException
