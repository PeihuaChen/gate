/*
 *  Parameter.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 15/Oct/2000
 *
 *  $Id$
 */

package gate.creole;

import java.util.*;

import gate.*;
import gate.util.*;


/** Models a resource parameter. It is package access as only the
  * CREOLE register and the metadata parser deals directly with
  * objects of this type.
  */
class Parameter
{
  /** The value of the parameter as parsed from resource metadata */
  String valueString;

  /** Is the parameter optional? */
  boolean optional = false;

  /** Default value for the parameter */
  String defaultValueString;

  /** Comment for the parameter */
  String comment;

  /** Name for the parameter */
  String name;

  /** Is this a run-time parameter? */
  boolean runtime = false;

  /** String representation */
  public String toString() {
    return "Parameter: valueString=" + valueString + "; optional=" + optional +
           "; defaultValueString=" + defaultValueString + "; comment=" +
           comment + "; runtime=" + runtime + "; name=" + name;
  } // toString()
} // class Parameter
