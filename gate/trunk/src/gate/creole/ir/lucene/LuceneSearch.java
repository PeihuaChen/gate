/*
 *  LuceneSearch.java
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
import java.util.*;

public class LuceneSearch implements Search {

  private IndexedCorpus indexedCorpus;

  public void setCorpus(IndexedCorpus ic){
    this.indexedCorpus = ic;
  }

  public QueryResultList search(String query){
    //NOT IMPLEMENTED YET
    return null;
  }

  public QueryResultList search(String query, int limit){
    //NOT IMPLEMENTED YET
    return null;
  }

  public QueryResultList search(String query, List fieldNames){
    //NOT IMPLEMENTED YET
    return null;
  }

  public QueryResultList search(String query, int limit, List fieldNames){
    //NOT IMPLEMENTED YET
    return null;
  }

}