/*
 *  GooglePR.java
 *
 *  Copyright (c) 1998-2004, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Google API and other sources subject to Google License. Please
 *  see http://www.google.com/apis/
 */

package google;

import gate.ProcessingResource;
import gate.Resource;
import gate.creole.*;
import gate.gui.MainFrame;
import gate.corpora.*;
import gate.util.*;
import gate.*;

import com.google.soap.search.*;

public class GooglePR extends AbstractLanguageAnalyser
    implements ProcessingResource {

  private String query = null;
  private int limit = -1;
  private String key = null;
  private Corpus google = null;

  /** Constructor of the class*/
  public GooglePR() {
  }

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {
    google = Factory.newCorpus("google");
    return super.init();
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
    if (query == null) {
      throw new ExecutionException("Query is not initialized");
    }
    if (limit == -1) {
      throw new ExecutionException("Limit is not initialized");
    }
    if (key == null) {
      throw new ExecutionException("Key is not initialized");
    }
    /*if (corpus == null) {
      throw new ExecutionException("Corpus is not initialized");
         }*/

    // Create a Google Search object, set our authorization key
    GoogleSearch search = new GoogleSearch();
    search.setKey(key);
    //do search
    search.setQueryString(query);
    //set limit
    //search.setMaxResults(limit);
    int index = 1;
    try {
      while (index < limit) {
        search.setStartResult(index);
        //run search
        GoogleSearchResult results = search.doSearch();

        //Err.println("CHECK : " + query + "\n" + limit + "\n" + key + "\n" +
        //            results.toString());
        //An array that holds the list of result elements
        GoogleSearchResultElement[] rs = new GoogleSearchResultElement[limit];

        //Err.println("result element array size" + rs.length);
        rs = results.getResultElements();
        if (rs != null) {
        for (int i = 0; i < rs.length; i++) {
          GoogleSearchResultElement rElement = rs[i];
          Err.println(index+i+" - "+rElement.getURL());
          String docName = rElement.getURL() + "_" + Gate.genSym();
          FeatureMap params = Factory.newFeatureMap();
          params.put(Document.DOCUMENT_URL_PARAMETER_NAME, rElement.getURL());
          try {
            Document doc = (Document) Factory.createResource(
                  DocumentImpl.class.getName(), params, null, docName
                  );
            google.add(doc);

          }
          catch (ResourceInstantiationException e) {
            String nl = Strings.getNl();
            Err.prln(
                  "WARNING: could not intantiate document" + nl +
                  "  Document name was: " + docName + nl +
                  "  Exception was: " + e + nl + nl
                  );
          }

        }
      }


        index += 10;
      }
    } catch (Exception gsf) {
      Err.println("Google Search Fault: "+gsf);
      gsf.printStackTrace();
    }
  }



  public void setQuery(String query) {
    this.query = query;
  }

  public String getQuery() {
    return this.query;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getKey() {
    return this.key;
  }

  public void setLimit(Integer limit) {
    this.limit = limit.intValue();
  }

  public Integer getLimit() {
    return new Integer(this.limit);
  }

 }