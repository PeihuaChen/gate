/*
 *  Copyright (c) 2012--2014, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  $Id$
 */
package gate.termraider.modes;

import gate.termraider.util.Utilities;
import java.util.*;


public enum MergingMode {
  MINIMUM, 
  MEAN, 
  MAXIMUM;
  
  public static Double calculate(MergingMode mode, List<Double> list) {
    if (mode == MAXIMUM) {
      return Collections.max(list);
    }
    
    if (mode == MINIMUM) {
      return Collections.min(list);
    }
    
    // must be MEAN
    return Utilities.meanDoubleList(list);
  }

}