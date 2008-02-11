package gate.creole.annic.lucene;

import java.io.*;
import java.util.*;
import gate.creole.annic.Constants;
import gate.creole.annic.SearchException;
import gate.creole.annic.apache.lucene.index.Term;
import gate.creole.annic.apache.lucene.search.*;

public class StatsCalculator {

  /**
   * Allows retriving frequencies for the given parameters.
   * Please make sure that you close the searcher on your own. Failing
   * to do so may result into many files being opened at the same time
   * and that can cause the problem with your OS.
   * 
   * @param searcher
   * @param corpusToSearchIn
   * @param annotationSetToSearchIn
   * @param annotationType
   * @param featureName
   * @param value
   * @return
   * @throws SearchException
   */
  public static int freq(IndexSearcher searcher, String corpusToSearchIn,
          String annotationSetToSearchIn, String annotationType,
          String featureName, String value) throws SearchException {

    try {
      corpusToSearchIn = corpusToSearchIn == null
              || corpusToSearchIn.trim().length() == 0
              ? null
              : corpusToSearchIn.trim();
      annotationSetToSearchIn = annotationSetToSearchIn == null
              || annotationSetToSearchIn.trim().length() == 0
              ? null
              : annotationSetToSearchIn.trim();
      if(annotationType == null)
        throw new SearchException("Annotation Type cannot be null");

      // term that contains a value to be searched in the index
      Term term = null;
      if(featureName == null || value == null) {
        term = new Term("contents", annotationType, "*");
      }
      else {
        term = new Term("contents", value, annotationType + "." + featureName);
      }

      // term query
      TermQuery tq = new TermQuery(term);

      // indicates whether we want to use booleanQuery
      boolean useBooleanQuery = false;
      BooleanQuery bq = new BooleanQuery();

      if(corpusToSearchIn != null) {
        PhraseQuery cq = new PhraseQuery();
        cq.add(new Term(Constants.CORPUS_ID, corpusToSearchIn), new Integer(0),
                true);
        bq.add(cq, true, false);
        useBooleanQuery = true;
      }

      if(annotationSetToSearchIn != null) {
        PhraseQuery aq = new PhraseQuery();
        aq.add(new Term(Constants.ANNOTATION_SET_ID, annotationSetToSearchIn),
                new Integer(0), true);
        bq.add(aq, true, false);
        useBooleanQuery = true;
      }

      Hits corpusHits = null;
      if(useBooleanQuery) {
        bq.add(tq, true, false);
        corpusHits = searcher.search(bq);
      }
      else {
        corpusHits = searcher.search(tq);
      }

      ArrayList[] firstTermPositions = searcher.getFirstTermPositions();

      // if no result available, set null to our scores
      if(firstTermPositions[0].size() == 0) {
        return 0;
      }

      int size = 0;
      // iterate through each result and collect necessary
      // information
      for(int hitIndex = 0; hitIndex < corpusHits.length(); hitIndex++) {
        int index = firstTermPositions[0].indexOf(new Integer(corpusHits
                .id(hitIndex)));

        // we fetch all the first term positions for the query
        // issued
        Integer freq = (Integer)firstTermPositions[4].get(index);
        size += freq.intValue();
      }
    }
    catch(IOException ioe) {
      throw new SearchException(ioe);
    }
    finally {
      searcher.initializeTermPositions();
    }

    return 0;
  }

  /**
   * Allows retriving frequencies for the given parameters.
   * Please make sure that you close the searcher on your own. Failing
   * to do so may result into many files being opened at the same time
   * and that can cause the problem with your OS.
   * 
   * @param searcher
   * @param corpusToSearchIn
   * @param annotationSetToSearchIn
   * @param annotationType
   * @return
   * @throws SearchException
   */
  public static int freq(IndexSearcher searcher, String corpusToSearchIn,
          String annotationSetToSearchIn, String annotationType)
          throws SearchException {

    return freq(searcher, corpusToSearchIn, annotationSetToSearchIn,
            annotationType, null, null);
  }
}
