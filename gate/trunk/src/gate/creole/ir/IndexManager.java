/*
 *  Indexmanager.java
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

import java.util.List;

public interface IndexManager{

  /** Creates index directory and indexing all
   *  documents in the corpus. */
  public void createIndex() throws IndexException;

  /** Optimize the existing index*/
  public void optimizeIndex() throws IndexException;

  /** Delete all index files and directories in index location. */
  public void deleteIndex() throws IndexException;

  /** Reindexing changed documents, removing removed documents and
   *  add to the index new corpus documents. */
  public void sync(List added, List removed, List changed) throws IndexException;

}