/*
 *  ProcessProgressReporter.java
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

/** This interface describes a processing resource that can report on the
 * progress of its processing activity.
 * It is useful for implementing progress bars and for waiting on precssing
 * resources that use theur own thread for processing purposes.
 * 
 */
public interface ProcessProgressReporter {

  /** 
   * Registers a new listener intersted in progress events.
   * 
   * @param listener 
   */
  public void addProcessProgressListener(ProgressListener listener);
  
  /** 
   * Removes a ProcessProgressListener.
   * 
   * @param listener 
   */
  public void removeProcessProgressListener(ProgressListener listener);

} // ProcessProgressReporter
