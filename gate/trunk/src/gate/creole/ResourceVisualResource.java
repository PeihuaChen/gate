/*
 *  Copyright (c) 2000-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June1991.
 *
 *  A copy of this licence is included in the distribution in the file
 *  licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 *  Valentin Tablan 09/07/2001
 *
 *  $Id$
 */
package gate.creole;

import gate.*;
import gate.util.*;

/**
 * Visual Resources that display and/or edit various types of {@link Resource}s.
 */
public interface ResourceVisualResource extends VisualResource {
  /**
   * Called by the GUI when this viewer/editor has to initialise itself for a
   * specific {@link Resource}.
   * @param resource the resource this viewer has to display
   */
  public void setResource(Resource resource);

}//public interface AnnotationVisualResource extends VisualResource