/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 02/10/2001
 *
 *  $Id$
 *
 */
package gate.util;

import gate.*;
import java.util.Comparator;

/**
 * Compares {@link NameBearer}s by name (string comparation)
 */
public class NameComparator implements Comparator {

  public int compare(Object o1, Object o2){
    NameBearer nb1 = (NameBearer)o1;
    NameBearer nb2 = (NameBearer)o2;
    return nb1.getName().compareTo(nb2.getName());
  }
}