/*
 * ProcessProgressReporter.java
 *
 * Copyright (c) 2000-2001, The University of Sheffield.
 * 
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 * 
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 * 
 * Valentin Tablan, 03/07/2000
 *
 * $Id$
 */
 
package gate.gui;

/** This interface describes a processing resource that can report on the
  * progress of its processing activity.
  * It is useful for implementing progress bars and for waiting on precssing
  * resources that use theur own thread for processing purposes.
  */
public interface ProcessProgressReporter {

  public void addProcessProgressListener(ProgressListener listener);
  
  public void removeProcessProgressListener(ProgressListener listener);

} // ProcessProgressReporter
