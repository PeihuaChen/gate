/*
 * StatusListener.java
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

public interface StatusListener {

  public void statusChanged(String text);

} // interface StatusListener
