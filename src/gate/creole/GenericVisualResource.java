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
import gate.gui.*;
import gate.util.*;

/**
 * Visual Resources that display and/or edit various types of Gate objects
 * such as {@link Resource}s, {@link gate.DataStore}s, applications, etc.
 */
public interface GenericVisualResource extends VisualResource {
  /**
   * Called by the GUI when this viewer/editor has to initialise itself for a
   * specific object.
   * @param target the object (be it a {@link gate.Resource},
   * {@link gate.DataStore} or whatever) this viewer has to display
   */
  public void setTarget(Object target);


  /**
   * Used by the main GUI to tell this VR what handle created it. The VRs can
   * use this information e.g. to add items to the popup for the resource.
   */
  public void setHandle(ResourceHandle handle);

}//public interface AnnotationVisualResource extends VisualResource