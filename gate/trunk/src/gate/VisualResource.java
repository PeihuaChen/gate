/*
 *  VisualResource.java
 *
 *  Copyright (c) 2000-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June1991.
 *
 *  A copy of this licence is included in the distribution in the file
 *  licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 *  Hamish Cunningham, 16/Oct/2000
 *
 *  $Id$
 */

package gate;

import java.util.*;
import java.awt.*;

import gate.util.*;

/** Models all sorts of visual resources.
  */
public interface VisualResource extends Resource{
   public Component getGui();
} // interface VisualResource
