/*
 *  DefaultActionContext.java
 *
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  $Id: $
 *
 */

package gate.jape;

import gate.Corpus;
import gate.FeatureMap;

/**
 * Default implementation for an action context.
 * 
 * @author Johann Petrak
 */
public class DefaultActionContext implements ActionContext {
  private Corpus corpus;
  private FeatureMap features;

  public DefaultActionContext() {}

  public void setCorpus(Corpus corpus) {
    this.corpus = corpus;
  }
  public void setPRFeatures(FeatureMap features) {
    this.features = features;
  }

  public Corpus getCorpus() {
    return corpus;
  }

  public FeatureMap getPRFeatures() {
    return features;
  }

}
