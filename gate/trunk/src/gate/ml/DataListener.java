/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 28 May 2002
 *
 *  $Id$
 */

package gate.ml;

public interface DataListener extends java.util.EventListener{

  /**
   * Called by the data collectore when new data becomes available.
   * @param offset the new offset reached in the document
   */
  public void dataAdvance(Long offset);

  /**
   * Sets the data collector this data listener lives into.
   * @param collector
   */
  public void setDataCollector(DataCollector collector);
}