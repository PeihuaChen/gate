/*
 *  SearchPR.java
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

import gate.*;
import gate.util.*;
import gate.creole.*;
import java.util.*;

public class SearchPR extends AbstractProcessingResource
                      implements ProcessingResource{

  private IndexedCorpus corpus = null;
  private String query  = null;
  private String searcherClassName = null;
  private QueryResultList resultList = null;
  private int limit = -1;
  private List fieldNames = null;

  private Search searcher = null;

  /** Constructor of the class*/
  public SearchPR(){
  }

   /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {
    Resource result = super.init();
    return result;
  }

  /**
   * Reinitialises the processing resource. After calling this method the
   * resource should be in the state it is after calling init.
   * If the resource depends on external resources (such as rules files) then
   * the resource will re-read those resources. If the data used to create
   * the resource has changed since the resource has been created then the
   * resource will change too after calling reInit().
  */
  public void reInit() throws ResourceInstantiationException {
    init();
  }

  /**
   * This method runs the coreferencer. It assumes that all the needed parameters
   * are set. If they are not, an exception will be fired.
   */
  public void execute() throws ExecutionException {
    if ( corpus == null){
      throw new ExecutionException("Corpus is not initialized");
    }
    if ( query == null){
      throw new ExecutionException("Query is not initialized");
    }
    if ( searcher == null){
      throw new ExecutionException("Searcher is not initialized");
    }

    try {
      resultList = null;
      searcher.setCorpus(corpus);
      resultList = searcher.search(query, limit, fieldNames);
    }
    catch (SearchException ie) {
      throw new ExecutionException(ie.getMessage());
    }
    catch (IndexException ie) {
      throw new ExecutionException(ie.getMessage());
    }
  }

  public void setCoprus(IndexedCorpus corpus) {
    this.corpus = corpus;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public void setSearcherClassName(String name){
    this.searcherClassName = name;
    try {
      searcher = (Search) Class.forName(searcherClassName).newInstance();
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  public void setLimit(int limit){
    this.limit = limit;
  }

  private void setFieldNames(List fieldNames){
    this.fieldNames = fieldNames;
  }

  private QueryResultList getResult(){
    return resultList;
  }
}