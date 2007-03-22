/*
 *  LuceneIndexSearcher.java
 *
 *  Niraj Aswani, 19/March/07
 *
 *  $Id: LuceneIndexSearcher.html,v 1.0 2007/03/19 16:22:01 niraj Exp $
 */
package gate.creole.annic.lucene;

import java.io.IOException;
import java.util.ArrayList;
import gate.creole.annic.apache.lucene.index.IndexReader;
import gate.creole.annic.apache.lucene.search.Filter;
import gate.creole.annic.apache.lucene.search.IndexSearcher;
import gate.creole.annic.apache.lucene.search.Query;
import gate.creole.annic.apache.lucene.search.TopDocs;
import gate.creole.annic.apache.lucene.store.Directory;

/**
 * This class provides an implementation that searches within the lucene
 * index to retrieve the results of a query submitted by user.
 * 
 * @author niraj
 * 
 */
public class LuceneIndexSearcher extends IndexSearcher {

  /**
   * Each pattern is a result of either simple or a boolean query. The
   * type number indicates if the query used to retrieve that pattern
   * was simple or boolean.
   */
  private ArrayList queryType = new ArrayList();

  /**
   * Each Integer value in this list is an index of first annotation of
   * the pattern that matches with the user query.
   */
  private ArrayList firstTermPositions = new ArrayList();

  /**
   * document numbers
   */
  private ArrayList documentNumbers = new ArrayList();

  /**
   * Stores how long each pattern is (in terms of number of
   * annotations).
   */
  private ArrayList patternLengths = new ArrayList();

  /** Creates a searcher searching the index in the named directory. */
  public LuceneIndexSearcher(String path) throws IOException {
    super(path);
  }

  /** Creates a searcher searching the index in the provided directory. */
  public LuceneIndexSearcher(Directory directory) throws IOException {
    super(directory);
  }

  /** Creates a searcher searching the provided index. */
  public LuceneIndexSearcher(IndexReader r) {
    super(r);
  }

  /**
   * Sets the firstTermPositions.
   * 
   * @param qType
   * @param doc
   * @param positions
   * @param patternLength
   */
  public void setFirstTermPositions(int qType, int doc, ArrayList positions,
          int patternLength) {
    queryType.add(new Integer(qType));
    firstTermPositions.add(positions);
    documentNumbers.add(new Integer(doc));
    patternLengths.add(new Integer(patternLength));
  }

  /**
   * Initializes all local variables
   * 
   */
  public void initializeTermPositions() {
    queryType = new ArrayList();
    firstTermPositions = new ArrayList();
    documentNumbers = new ArrayList();
    patternLengths = new ArrayList();
  }

  /**
   * Returns an array of arrayLists where the first list contains
   * document numbers, second list contains first term positions, third
   * list contains the pattern lengths and the fourth one contains the
   * query type for each pattern.
   * 
   * @return
   */
  public ArrayList[] getFirstTermPositions() {
    return new ArrayList[] {documentNumbers, firstTermPositions,
        patternLengths, queryType};
  }

  /**
   * Searches through the lucene index and returns an instance of TopDocs.
   */
  public TopDocs search(Query query, Filter filter, final int nDocs)
          throws IOException {
    initializeTermPositions();
    return super.search(query, filter, nDocs);
  }
}
