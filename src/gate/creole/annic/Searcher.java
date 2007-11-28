/*
 *  Searcher.java
 *
 *  Niraj Aswani, 19/March/07
 *
 *  $Id: Searcher.html,v 1.0 2007/03/19 16:22:01 niraj Exp $
 */
package gate.creole.annic;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Searcher interface.
 * @author niraj
 *
 */
public interface Searcher {

  /**
   * Search method that allows searching
   * 
   * @param query
   * @param numberOfPatterns
   * @param patternWindow
   * @return
   * @throws SearchException
   */
  public boolean search(String query, Map parameters) throws SearchException;

  /**
   * Query to search
   * 
   * @return
   */
  public String getQuery();

  /**
   * Return the next numberOfHits -1 indicates all
   * 
   * @return
   */
  public Hit[] next(int numberOfHits) throws SearchException;

  
  /**
   * Returns the Map containing all possible values of AnnotationTypes
   * and Feature Values for each of this annotationType
   * 
   * @return
   */
  public Map<String, List<String>> getAnnotationTypesMap();

  
  /**
   * Returns an containing names of the indexed annotation sets
   * @param indexLocation
   * @return
   * @throws SearchException
   */
  public String[] getIndexedAnnotationSetNames(String indexLocation) throws SearchException;
  
  /**
   * Returns the recently set parameters
   * 
   * @return
   */
  public Map<Object, Object> getParameters();

  /**
   * This method can be used for exporting results
   * 
   * @param outputFile
   */
  public void exportResults(File outputFile);

  /**
   * return the last seen hits once again
   * 
   * @return
   */
  public Hit[] getHits();
}
