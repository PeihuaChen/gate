/*
 *  StatusListener.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 * 
 *  Valentin Tablan, 03/07/2000
 *
 *  $Id$
 */

package gate.gui;

import java.util.*;

/** 
 * This interface describes a listener that is interested in status events. This type of listener is intended to be used
 * by components similar to a status bar which can display messages coming from more components.
 * 
 */
public interface StatusListener extends EventListener{

  /** 
   * Calleed when there a new status message.
   * 
   * @param text 
   */
  public void statusChanged(String text);

} // interface StatusListener
