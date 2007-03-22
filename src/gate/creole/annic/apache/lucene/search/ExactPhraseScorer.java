package gate.creole.annic.apache.lucene.search;

/**
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import gate.creole.annic.apache.lucene.index.*;
import gate.creole.annic.lucene.LuceneIndexSearcher;

final class ExactPhraseScorer extends PhraseScorer {

  int patternSize = 0;
  ExactPhraseScorer(Weight weight, TermPositions[] tps, Similarity similarity,
                    byte[] norms) throws IOException {
    super(weight, tps, similarity, norms);
  }

  /* Niraj */
  ExactPhraseScorer(Weight weight, TermPositions[] tps, java.util.Vector positions, int totalTerms, Similarity similarity,
                    byte[] norms) throws IOException {
    super(weight, tps, positions, similarity, norms);
    patternSize = totalTerms;
  }
  /* End */

  
  /* Niraj */
  ExactPhraseScorer(Weight weight, TermPositions[] tps, java.util.Vector positions, int totalTerms, Similarity similarity,
                    byte[] norms, IndexSearcher searcher) throws IOException {
    super(weight, tps, positions, similarity, norms, searcher);
    patternSize = totalTerms;
  }
  /* End */

  protected final float phraseFreq() throws IOException {
    // sort list with pq
    for (PhrasePositions pp = first; pp != null; pp = pp.next) {
      pp.firstPosition();
      pq.put(pp);				  // build pq from list
    }
    pqToList();					  // rebuild list from pq

    int freq = 0;
    /* Niraj */
    java.util.ArrayList firstPositions = new java.util.ArrayList();
    /* End */

    do {					  // find position w/ all terms
      while (first.position < last.position) {	  // scan forward in first
	do {
	  if (!first.nextPosition()) {
            /* Niraj */
            // here 1 represents the ExactPhraseQuery
	      	((LuceneIndexSearcher)searcher).setFirstTermPositions(1, first.doc, firstPositions, patternSize);
            /* End */
            return (float) freq;
          }
	} while (first.position < last.position);
	firstToLast();
      }
      firstPositions.add(new Integer(first.position));
      freq++;					  // all equal: a match
    } while (last.nextPosition());
    /* Niraj */
    ((LuceneIndexSearcher)searcher).setFirstTermPositions(1, first.doc, firstPositions, patternSize);
    /* End */
    return (float)freq;
  }
}
