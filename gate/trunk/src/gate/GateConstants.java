/*
 *  GateConstants.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU, 8/Nov/2001
 *
 *  $Id$
 */

package gate;

/** Interface used to hold different GATE constants */
public interface GateConstants {

  /** The name of config data files (<TT>gate.xml</TT>). */
  public static String GATE_DOT_XML = "gate.xml";

  /** The name of the annotation set storing original markups in a document */
  public static final String
    ORIGINAL_MARKUPS_ANNOT_SET_NAME = "Original markups";

} // GateConstants