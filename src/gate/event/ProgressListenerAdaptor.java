/*
 *  ProgressListenerAdaptor.java
 *
 *  Copyright (c) 1998-2005, The University of Sheffield.
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

package gate.event;

/** Convenience class for implementing ProgressListener
  */
public class ProgressListenerAdaptor implements ProgressListener {

  /** Debug flag */
  private static final boolean DEBUG = false;

  public void progressChanged(int i){}

  public void processFinished(){}

} // ProgressListenerAdaptor
