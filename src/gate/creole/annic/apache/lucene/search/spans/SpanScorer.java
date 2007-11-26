package gate.creole.annic.apache.lucene.search.spans;

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

import gate.creole.annic.apache.lucene.search.Weight;
import gate.creole.annic.apache.lucene.search.Scorer;
import gate.creole.annic.apache.lucene.search.Explanation;
import gate.creole.annic.apache.lucene.search.Similarity;
import gate.creole.annic.apache.lucene.search.IndexSearcher;

public class SpanScorer extends Scorer {
  private Spans spans;
  private Weight weight;
  private byte[] norms;
  private float value;

  private boolean firstTime = true;
  private boolean more = true;

  private int doc;
  private float freq;

  SpanScorer(Spans spans, Weight weight, Similarity similarity, byte[] norms)
    throws IOException {
    super(similarity);
    this.spans = spans;
    this.norms = norms;
    this.weight = weight;
    this.value = weight.getValue();
  }

  public boolean next(IndexSearcher searcher) throws IOException {
    this.searcher = searcher;
    return next();
  }
  
  public boolean next() throws IOException {
    if (firstTime) {
      more = spans.next();
      firstTime = false;
    }

    if (!more) return false;

    freq = 0.0f;
    doc = spans.doc();

    while (more && doc == spans.doc()) {
      int matchLength = spans.end() - spans.start();
      freq += getSimilarity().sloppyFreq(matchLength);
      more = spans.next();
    }

    return more || freq != 0.0f;
  }

  public int doc() { return doc; }

  /* Niraj */
  public float score(IndexSearcher searcher) throws IOException {
      return score();
  }
  /* End */
  
  public float score() throws IOException {
    float raw = getSimilarity().tf(freq) * value; // raw score
    return raw * Similarity.decodeNorm(norms[doc]); // normalize
  }

  public boolean skipTo(int target) throws IOException {
    more = spans.skipTo(target);

    if (!more) return false;

    freq = 0.0f;
    doc = spans.doc();

    while (more && spans.doc() == target) {
      freq += getSimilarity().sloppyFreq(spans.end() - spans.start());
      more = spans.next();
    }

    return more || freq != 0.0f;
  }

  public Explanation explain(final int doc) throws IOException {
    Explanation tfExplanation = new Explanation();

    skipTo(doc);

    float phraseFreq = (doc() == doc) ? freq : 0.0f;
    tfExplanation.setValue(getSimilarity().tf(phraseFreq));
    tfExplanation.setDescription("tf(phraseFreq=" + phraseFreq + ")");

    return tfExplanation;
  }

}
