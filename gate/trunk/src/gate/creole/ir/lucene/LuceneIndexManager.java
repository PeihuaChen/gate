/*
 *  LuceneIndexManager.java
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

package gate.creole.ir.lucene;

import gate.creole.ir.*;

public class LuceneIndexManager implements IndexManager{

  private IndexDefinition idef;

  public LuceneIndexManager(IndexDefinition def){
    this.idef = def;
  }

  public void createIndex() throws IndexException{
  }

  public void optimizeIndex() throws IndexException{
  }

  public void deleteIndex() throws IndexException{
  }


}