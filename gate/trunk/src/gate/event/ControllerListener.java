/*
 *  Copyright (c) 1998-2004, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  ControllerListener.java
 *
 *  Valentin Tablan, 28-Jun-2004
 *
 *  $Id$
 */

package gate.event;

import java.util.EventListener;

/**
 * A listener for events generate by controllers.
 */
public interface ControllerListener extends EventListener{
  
  /**
   * Called by the controller when a new PR has been added.
   * @param evt the event. 
   */
  public void resourceAdded(ControllerEvent evt);
  
  /**
   * Called by the controller when a new PR has been removed.
   * @param evt the event.
   */
  public void resourceRemoved(ControllerEvent evt);
}
