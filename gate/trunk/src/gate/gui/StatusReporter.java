/*
 *  StatusReporter.java
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

/** 
 * THis interface describes a component able to generate status events (e.g. messages to be displayed in a status bar)
 * 
 */
public interface StatusReporter {

  /** 
   * Adds a new status listener.
   * 
   * @param listener 
   */
  public void addStatusListener(StatusListener listener);

  /** 
   * Removes a previously added status listener.
   * 
   * @param listener 
   */
  public void removeStatusListener(StatusListener listener);

} // interface StatusReporter
