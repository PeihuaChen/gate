/*
 *  QuotedTextCoref.java
 *
 *  Copyright (c) 1998-2002, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 17/Jan/2002
 *
 *  $Id$
 */

package gate.creole.coref;

import java.util.*;
import java.net.*;

import junit.framework.*;

import gate.*;
import gate.creole.*;
import gate.util.*;

public class QuotedTextCoref extends AbstractLanguageAnalyser
                              implements ProcessingResource{


  private Transducer qtTransducer;

  public QuotedTextCoref() {
  }

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {

    URL qtGrammarURL = null;

    try {
      qtGrammarURL = new URL("gate://gate/creole/coref/quoted.jape");
    }
    catch(MalformedURLException mue) {
      throw new ResourceInstantiationException(mue);
    }

    FeatureMap params = new SimpleFeatureMapImpl();
    params.put("grammarURL",qtGrammarURL);

    this.qtTransducer = (Transducer)Factory.createResource("gate.creole.Transducer",
                                                                   params);

    return super.init();
  } // init()

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
  } // reInit()


  /**
   * This method runs the coreferencer. It assumes that all the needed parameters
   * are set. If they are not, an exception will be fired.
   */
  public void execute() throws ExecutionException{

    this.qtTransducer.execute();
  }

  /** Set the document to run on. */
  public void setDocument(Document newDocument) {

    Assert.assertNotNull(newDocument);

    this.qtTransducer.setDocument(newDocument);
    super.setDocument(newDocument);

  }

  private void preprocess() {
  }

}