/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 17/05/2002
 *
 *  $Id$
 *
 */
package gate.creole.ir.lucene;

import gate.creole.ir.Search;
import gate.creole.ir.IndexManager;
import gate.creole.ir.IREngine;

/**
 * The lucene IR engine.
 * Packages a {@link LuceneIndexManager} and a {@link LuceneSearch}.
 */

public class LuceneIREngine implements IREngine{

  public LuceneIREngine() {
    search = new LuceneSearch();
    indexManager = new LuceneIndexManager();
  }

  public Search getSearch() {
    return search;
  }

  public IndexManager getIndexmanager() {
    return indexManager;
  }

  public String getName(){
    return "Lucene IR engine";
  }

  Search search;
  IndexManager indexManager;

}