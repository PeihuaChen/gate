/*
 *  CorpusController.java
 *
 *  Copyright (c) 1998-2002, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 10/May/2002
 *
 *  $Id$
 */

package gate;

/** Models the execution of groups of ProcessingResources on a given corpus.
  */
public interface CorpusController extends Controller {

  /**
   * Returns the {@link gate.Corpus} used by this
   * controller.
   */
  public gate.Corpus getCorpus();

  /**
   * Sets the {@link gate.Corpus} which contains the data on which
   * the controller is going to run.
   */  public void setCorpus(gate.Corpus corpus);

}