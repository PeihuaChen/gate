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

/** This class represents Lucene implementation of serching in index. */
public class LuceneSearch implements Search {

  /** An instance of indexed corpus*/
  private IndexedCorpus indexedCorpus;

  /** Set the indexed corpus resource for searching. */
  public void setCorpus(IndexedCorpus ic){
    this.indexedCorpus = ic;
  }

  /** Search in corpus with this query. Unlimited result length.*/
  public QueryResultList search(String query)
                                         throws IndexException, SearchException{
    return search(query, -1);
  }

  /** Search in corpus with this query.
   *  Size of the result list is limited. */
  public QueryResultList search(String query, int limit)
                                         throws IndexException, SearchException{
    return search(query, limit, null);
  }

  /** Search in corpus with this query.
   *  In each QueryResult will be added values of theise fields.
   *  Result length is unlimited. */
  public QueryResultList search(String query, List fieldNames)
                                         throws IndexException, SearchException{
    return search(query, -1, fieldNames);
  }

  /** Search in corpus with this query.
   *  In each QueryResult will be added values of theise fields.
   *  Result length is limited. */
  public QueryResultList search(String query, int limit, List fieldNames)
                                         throws IndexException, SearchException{
    Vector result = new Vector();

    try {
      IndexSearcher searcher = new IndexSearcher(indexedCorpus.getIndexDefinition().getIndexLocation());
      Query luceneQuery = QueryParser.parse(query, "body", new SimpleAnalyzer());

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
            fieldValues.add(new gate.creole.ir.Term( fieldNames.get(j).toString(), hits.doc(i).get(fieldNames.get(j).toString())));
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