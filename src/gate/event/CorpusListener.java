/*
 *  Copyright (c) 1998-2004, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 13/07/2001
 *
 *  $Id$
 */

package gate.event;

import java.util.EventListener;

/**
 * A listener for events fired by {@link gate.Corpus}
 */
public interface CorpusListener extends EventListener {

  /**
   * Called when a document has been added
   */
  public void documentAdded(CorpusEvent e);

  /**
   * Called when a document has been removed
   */
  public void documentRemoved(CorpusEvent e);
}