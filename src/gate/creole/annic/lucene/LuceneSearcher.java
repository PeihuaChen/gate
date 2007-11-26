/*
 *  LuceneSearcher.java
 *
 *  Niraj Aswani, 19/March/07
 *
 *  $Id: LuceneSearcher.html,v 1.0 2007/03/19 16:22:01 niraj Exp $
 */
package gate.creole.annic.lucene;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import gate.creole.annic.Hit;
import gate.creole.annic.Pattern;
import gate.creole.annic.Constants;
import gate.creole.annic.PatternAnnotation;
import gate.creole.annic.SearchException;
import gate.creole.annic.Searcher;
import gate.creole.annic.apache.lucene.document.Document;
import gate.creole.annic.apache.lucene.index.Term;
import gate.creole.annic.apache.lucene.search.Hits;
import gate.creole.annic.apache.lucene.search.IndexSearcher;
import gate.creole.annic.apache.lucene.search.TermQuery;

/**
 * This class provides the Searching functionality for annic.
 * 
 * @author niraj
 * 
 */
public class LuceneSearcher implements Searcher {

  /**
   * A List of index locations. It allows searching at multiple
   * locations.
   */
  private List<String> indexLocations = null;

  /**
   * The submitted query.
   */
  private String query = null;

  /**
   * An object where we store our results.
   */
  private gate.creole.annic.lucene.LuceneQueryResultList resultList = null;

  /**
   * The number of base token annotations to show in left and right
   * context of the pattern. By default 5.
   */
  private int contextWindow = 5;

  /**
   * Found patterns.
   */
  private List<Pattern> annicPatterns = new ArrayList<Pattern>();

  /**
   * Found annotation types in the annic patterns. The maps keeps record
   * of found annotation types and features for each of them.
   */
  public Map<String, List<String>> annotationTypesMap = new HashMap<String, List<String>>();

  /**
   * Search parameters.
   */
  private Map parameters = null;

  /**
   * Corpus to search in.
   */
  private String corpusToSearchIn = null;

  /**
   * Annotation set to search in.
   */
  private String annotationSetToSearchIn = null;

  /**
   * Hits returned by the lucene.
   */
  private Hits luceneHits = null;

  /**
   * Indicates if the query was to delete certain documents.
   */
  private boolean wasDeleteQuery = false;
 
  /**
   * A query can result into multiple queries. For example: (A|B)C is
   * converted into two queries: AC and AD. For each query a separate
   * thread is started.
   */
  private List<LuceneSearchThread> luceneSearchThreads = null;

  /**
   * Indicates if the search was successful.
   */
  private boolean success = false;

  /**
   * Tells which thread to use to retrieve results from.
   */
  private int luceneSearchThreadIndex = 0;

  /**
   * Tells if we have reached at the end of of found results.
   */
  private boolean fwdIterationEnded = false;

  /**
   * Return the next numberOfHits -1 indicates all
   * 
   * @return
   */
  public Hit[] next(int numberOfHits) throws SearchException {

    annotationTypesMap = new HashMap<String, List<String>>();
    annicPatterns = new ArrayList<Pattern>();

    if(!success) {
      this.annicPatterns = new ArrayList<Pattern>();
      return getHits();
    }

    if(fwdIterationEnded) {
      this.annicPatterns = new ArrayList<Pattern>();
      return getHits();
    }

    try {
      if(wasDeleteQuery) {
        List<String> docIDs = new ArrayList<String>();
        List<String> setNames = new ArrayList<String>();
        for(int i = 0; i < luceneHits.length(); i++) {
          Document luceneDoc = luceneHits.doc(i);
          String documentID = luceneDoc.get(Constants.DOCUMENT_ID);
          String annotationSetID = luceneDoc.get(Constants.ANNOTATION_SET_ID);
          int index = docIDs.indexOf(documentID);
          if(index == -1) {
            docIDs.add(documentID);
            setNames.add(annotationSetID);
          }
          else {
            if(!setNames.get(index).equals(annotationSetID)) {
              docIDs.add(documentID);
              setNames.add(annotationSetID);  
            }
          }
        }

        Hit[] toReturn = new Hit[docIDs.size()];
        for(int i = 0; i < toReturn.length; i++) {
          toReturn[i] = new Hit(docIDs.get(i), setNames.get(i), 0, 0, "");
        }
        return toReturn;
      }

      for(; luceneSearchThreadIndex < luceneSearchThreads.size(); luceneSearchThreadIndex++) {
        LuceneSearchThread lst = luceneSearchThreads.get(luceneSearchThreadIndex);
        List<Pattern> results = lst.next(numberOfHits);
        if(results != null) {
          if(numberOfHits != -1) {
            numberOfHits -= results.size();
          }

          this.annicPatterns.addAll(results);
          if(numberOfHits == 0) {
            return getHits();
          }
        }
      }

      // if we are here, there wer no sufficient patterns available
      // so what we do is make success to false so that this method
      // return null on next call
      fwdIterationEnded = true;
      return getHits();
    }
    catch(Exception e) {
      throw new SearchException(e);
    }
  }

  
  /**
   * Method retunrs true/false indicating whether results were found or
   * not.
   */
  public boolean search(String query, Map parameters) throws SearchException {
    luceneHits = null;
    annicPatterns = new ArrayList<Pattern>();
    annotationTypesMap = new HashMap();
    luceneSearchThreads = new ArrayList<LuceneSearchThread>();
    luceneSearchThreadIndex = 0;
    success = false;
    fwdIterationEnded = false;
    wasDeleteQuery = false;

    if(parameters == null)
      throw new SearchException("Parameters cannot be null");

    this.parameters = parameters;

    /*
     * lets first check if the query is to search the document names
     * This is used when we only wants to search for documents stored
     * under the specific corpus
     */
    if(parameters.size() == 2
            && parameters.get(Constants.INDEX_LOCATION_URL) != null) {
      String corpusID = (String)parameters.get(Constants.CORPUS_ID);
      String indexLocation = new File(((URL)parameters
              .get(Constants.INDEX_LOCATION_URL)).getFile()).getAbsolutePath();
      if(corpusID != null && indexLocation != null) {
        wasDeleteQuery = true;
        Term term = new Term(Constants.CORPUS_ID, corpusID);
        TermQuery tq = new TermQuery(term);
        try {
          gate.creole.annic.apache.lucene.search.Searcher searcher = new IndexSearcher(
                  indexLocation);
          // and now execute the query
          // result of which will be stored in hits
          luceneHits = searcher.search(tq);
          success = luceneHits.length() > 0 ? true : false;
          return success;
        }
        catch(IOException ioe) {
          ioe.printStackTrace();
          throw new SearchException(ioe);
        }
      }
    }

    // check for index locations
    if(parameters.get(Constants.INDEX_LOCATIONS) == null)
      throw new SearchException("Parameter " + Constants.INDEX_LOCATIONS
              + " has not been provided!");

    
    indexLocations = new ArrayList((List)parameters
            .get(Constants.INDEX_LOCATIONS));

    if(indexLocations.size() == 0)
      throw new SearchException("Corpus is not initialized");

    // check for valid context window
    if(parameters.get(Constants.CONTEXT_WINDOW) == null)
      throw new SearchException("Parameter " + Constants.CONTEXT_WINDOW
              + " is not provided!");

    contextWindow = ((Integer)parameters.get(Constants.CONTEXT_WINDOW))
            .intValue();

    if(getContextWindow().intValue() <= 0)
      throw new SearchException("Context Window must be atleast 1 or > 1");

    if(query == null) throw new SearchException("Query is not initialized");

    this.query = query;
    this.corpusToSearchIn = (String)parameters.get(Constants.CORPUS_ID);
    this.annotationSetToSearchIn = (String)parameters
            .get(Constants.ANNOTATION_SET_ID);

    annicPatterns = new ArrayList<Pattern>();
    annotationTypesMap = new HashMap();
    resultList = null;

    luceneSearchThreads = new ArrayList<LuceneSearchThread>();

    // for different indexes, we create a different instance of
    // indexSearcher
    for(int indexCounter = 0; indexCounter < indexLocations.size(); indexCounter++) {
      String location = indexLocations.get(indexCounter);
      // we create a separate Thread for each index
      LuceneSearchThread lst = new LuceneSearchThread();
      if(lst.search(query, contextWindow, location, corpusToSearchIn,
              annotationSetToSearchIn, this)) {
        luceneSearchThreads.add(lst);
      }
    }

    success = luceneSearchThreads.size() > 0 ? true : false;
    return success;
  }

  /**
   * For each query lucene tells us the position of the first term that
   * matched the pattern. This method given required parameters, goes
   * through each such position and tries to retrieve other required
   * annotations information from the token streams. Finally a list of
   * annic patterns is returned.
   * 
   * @param docID
   * @param gateAnnotations
   * @param positions
   * @param patternLength
   * @param queryString
   * @return
   */
  private List<Pattern> locatePatterns(String docID, String annotationSetName,
          List<List<PatternAnnotation>> gateAnnotations, List<Integer> positions,
          List<Integer> patternLength, String queryString) {

    // patterns
    List<Pattern> pat = new ArrayList<Pattern>();
    for(int i = 0; i < gateAnnotations.size(); i++) {

      // each element in the tokens stream is a pattern
      List<PatternAnnotation> annotations = gateAnnotations.get(i);
      if(annotations.size() == 0) {
        continue;
      }
      // from this annotations we need to create a text string
      // so lets find out the smallest and the highest offsets
      int smallest = Integer.MAX_VALUE;
      int highest = -1;
      for(int j = 0; j < annotations.size(); j++) {
        // each annotation is an instance of GateAnnotation
        PatternAnnotation ga = annotations.get(j);
        if(ga.getStartOffset() < smallest) {
          smallest = ga.getStartOffset();
        }

        if(ga.getEndOffset() > highest) {
          highest = ga.getEndOffset();
        }
      }

      // we have smallest and highest offsets
      char[] patternText = new char[highest - smallest];

      for(int j = 0; j < patternText.length; j++) {
        patternText[j] = ' ';
      }

      // and now place the text
      for(int j = 0; j < annotations.size(); j++) {
        // each annotation is an instance of GateAnnotation
        PatternAnnotation ga = annotations.get(j);
        if(ga.getText() == null) {
          // this is to avoid annotations such as split
          continue;
        }

        for(int k = ga.getStartOffset() - smallest, m = 0; m < ga.getText()
                .length()
                && k < patternText.length; m++, k++) {
          patternText[k] = ga.getText().charAt(m);
        }

        // we will initiate the annotTypes as well
        if(annotationTypesMap.keySet().contains(ga.getType())) {
          List<String> aFeatures = annotationTypesMap.get(ga.getType());
          Map<String, String> features = ga.getFeatures();
          if(features != null) {
            Iterator<String> fSet = features.keySet().iterator();
            while(fSet.hasNext()) {
              String feature = fSet.next();
              if(!aFeatures.contains(feature)) {
                aFeatures.add(feature);
              }
            }
          }
          annotationTypesMap.put(ga.getType(), aFeatures);
        }
        else {
          Map<String, String> features = ga.getFeatures();
          List<String> aFeatures = new ArrayList<String>();
          aFeatures.add("All");
          if(features != null) {
            aFeatures.addAll(features.keySet());
          }
          annotationTypesMap.put(ga.getType(), aFeatures);
        }
        // end of initializing annotationTypes for the comboBox
      }

      // we have the text
      // smallest is the textStOffset
      // highest is the textEndOffset
      // how to find the patternStartOffset
      int stPos = ((Integer)positions.get(i)).intValue();
      int endOffset = ((Integer)patternLength.get(i)).intValue();
      int patStart = Integer.MAX_VALUE;

      for(int j = 0; j < annotations.size(); j++) {
        // each annotation is an instance of GateAnnotation
        PatternAnnotation ga = annotations.get(j);
        if(ga.getPosition() == stPos) {
          if(ga.getStartOffset() < patStart) {
            patStart = ga.getStartOffset();
          }
        }

      }

      if(patStart == Integer.MAX_VALUE) {
        continue;
      }

      if(patStart < smallest || endOffset > highest) {
        continue;
      }

      // now create the pattern for this
      Pattern ap = new Pattern(docID, annotationSetName,
              new String(patternText), patStart, endOffset, smallest, highest,
              annotations, queryString);
      pat.add(ap);
    }
    return pat;
  }

  /**
   * Gets the submitted query.
   */
  public String getQuery() {
    return this.query;
  }

  /**
   * Gets the number of base token annotations to show in the context.
   * 
   * @return
   */
  public Integer getContextWindow() {
    return new Integer(this.contextWindow);
  }

  /**
   * Gets the found hits (annic patterns).
   */
  public Hit[] getHits() {
    if(annicPatterns == null) annicPatterns = new ArrayList<Pattern>();
    Hit[] hits = new Hit[annicPatterns.size()];
    for(int i = 0; i < annicPatterns.size(); i++) {
      hits[i] = (Pattern)annicPatterns.get(i);
    }
    return hits;
  }

  /**
   * Gets the map of found annotation types and annotation features.
   */
  public Map<String, List<String>> getAnnotationTypesMap() {
    return annotationTypesMap;
  }

  /**
   * Gets the search parameters set by user.
   */
  public Map getParameters() {
    return parameters;
  }

  /**
   * A Map used for caching query tokens created for a query.
   */
  private Map<String, List<String>> queryTokens = new HashMap<String, List<String>>();

  /**
   * Gets the query tokens for the given query.
   * 
   * @param query
   * @return
   */
  public synchronized List<String> getQueryTokens(String query) {
    return queryTokens.get(query);
  }

  /**
   * Adds the query tokens for the given query.
   * 
   * @param query
   * @param queryTokens
   */
  public synchronized void addQueryTokens(String query, List<String> queryTokens) {
    this.queryTokens.put(query, queryTokens);
  }

  /**
   * This method allow exporting results in to the provided file. This
   * method has not been implemented yet.
   */
  public void exportResults(File outputFile) {
    throw new RuntimeException("ExportResults method is not implemented yet!");
  }
}
