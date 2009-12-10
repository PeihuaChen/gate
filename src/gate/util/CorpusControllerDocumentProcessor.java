/*
 *  CorpusControllerDocumentProcessor.java
 *  Copyright (c) 1995-2010, The University of Sheffield. See the file
 *  COPYRIGHT.txt in the software or at http://gate.ac.uk/gate/COPYRIGHT.txt
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Ian Roberts, 03/Sep/2009
 *
 *  $Id$
 */

package gate.util;

import gate.Corpus;
import gate.CorpusController;
import gate.Document;
import gate.Factory;
import gate.creole.ExecutionException;

/**
 * {@link DocumentProcessor} that processes documents using a
 * {@link CorpusController}.
 */
public class CorpusControllerDocumentProcessor implements DocumentProcessor {

  /**
   * The controller used to process documents.
   */
  private CorpusController controller;

  /**
   * Corpus used to contain the document being processed.
   */
  private Corpus corpus;

  public CorpusControllerDocumentProcessor() {
  }

  /**
   * Set the controller used to process documents.
   */
  public void setController(CorpusController c) {
    this.controller = c;
  }

  public synchronized void processDocument(Document doc) throws GateException {
    if(corpus == null) {
      corpus = Factory.newCorpus("DocumentProcessor corpus");
    }
    try {
      corpus.add(doc);
      controller.setCorpus(corpus);
      controller.execute();
    }
    finally {
      controller.setCorpus(null);
      corpus.clear();
    }
  }

  /**
   * Clean up resources.  Should be called when this processor is no longer
   * required.
   */
  public synchronized void cleanup() {
    Factory.deleteResource(controller);
    Factory.deleteResource(corpus);
  }
}
