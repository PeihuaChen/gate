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

import org.apache.lucene.index.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.document.*;
import org.apache.lucene.search.*;
import org.apache.lucene.queryParser.*;
import org.apache.lucene.store.*;

public class LuceneSearch implements Search {

  private IndexedCorpus indexedCorpus;

  public void setCorpus(IndexedCorpus ic){
    this.indexedCorpus = ic;
  }

  public QueryResultList search(String query) throws IndexException, SearchException{
    return search(query, -1);
  }

  public QueryResultList search(String query, int limit) throws IndexException, SearchException{
    return search(query, limit, null);
  }

  public QueryResultList search(String query, List fieldNames) throws IndexException, SearchException{
    return search(query, -1, fieldNames);
  }

  public QueryResultList search(String query, int limit, List fieldNames) throws IndexException, SearchException{
    Vector result = new Vector();

    try {
      IndexSearcher searcher = new IndexSearcher(indexedCorpus.getIndexDefinition().getIndexLocation());
      Query luceneQuery = QueryParser.parse(query, null, (Analyzer) indexedCorpus.getIndexDefinition().getAnalyzer());

      Hits hits = searcher.search(luceneQuery);
      int resultlength = hits.length();
      if (limit>-1) {
        resultlength = Math.min(limit,resultlength);
      }

      Vector fieldValues = null;
      for (int i=0; i<resultlength; i++) {

        if (fieldNames != null){
          fieldValues = new Vector();
          for (int j=0; j<fieldNames.size(); j++){
            fieldValues.add(hits.doc(i).get(fieldNames.get(j).toString()));
          }
        }

        result.add(new QueryResult(hits.doc(i).get(LuceneIndexManager.DOCUMENT_ID),hits.score(i),fieldValues));
      }// for (all search hints)

      searcher.close();

      return new QueryResultList(query, indexedCorpus, result);
    }
    catch (java.io.IOException ioe) {
      throw new IndexException(ioe.getMessage());
    }
    catch (org.apache.lucene.queryParser.ParseException pe) {
      throw new SearchException(pe.getMessage());
    }
  }

}