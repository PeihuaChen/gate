/*
 *  ActionContext.java
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

import gate.Controller;
import gate.Corpus;
import gate.FeatureMap;

/**
 * Interface describing an "action context" for a JAPE Java RHS. An action
 * context provides access to the JAPE processing resource's feature map and
 * the corpus the JAPE PR is running on.
 *
 * @author Johann Petrak
 */
public interface ActionContext {
  /**
   * Provide access to the corpus a JAPE processing resource is running on.
   * @return the corpus LR the JAPE transducer is processing, null if no
   * such corpus exists.
   */
  public Corpus getCorpus();
  /**
   * Provide access to the feature map associated with the JAPE processing
   * resource.
   * @return the FeatureMap of the processing resource
   */
  public FeatureMap getPRFeatures();
  /**
   * Provide access to the controller running the PR this action context
   * lives in.
   * @return the Controller resource
   */
  public Controller getController();
}
