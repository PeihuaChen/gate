/*
 *  IndexedCorpus.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Rosen Marinov, 19/Apr/2002
 *
 */

package gate.creole.ir;

import gate.Corpus;

public interface IndexedCorpus {

  /** Sets the definition to this corpus.
   * @param definition of index for this corpus
   */
  public void setIndexDefinition(IndexDefinition definition);

  /** @return IndexDefinition definition of index for this corpus. */
  public IndexDefinition getIndexDefinition();

  /** @return IndexManager manager object for this corpus. It creates
   *  after seting of IndexDefinition by indexType property. */
  public IndexManager getIndexManager();

  /** @return IndexStatistics statistics for this index. */
  public IndexStatistics getIndexStatistics();

}