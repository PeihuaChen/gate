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

import java.util.Comparator;
import gate.*;

/**
 * Compares annotations by start offset
 */
public class OffsetComparator implements Comparator {

  public int compare(Object o1, Object o2){
    Annotation a1 = (Annotation)o1;
    Annotation a2 = (Annotation)o2;
    return a1.getStartNode().getOffset().compareTo(
            a2.getStartNode().getOffset());
  }
}