/*
 *  AbstractLanguageAnalyser.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 13/Nov/2000
 *
 *  $Id$
 */

package gate.creole;

import java.util.*;

import gate.*;
import gate.util.*;

/** A parent implementation of language analysers with some default
 *  code.
 */
abstract public class AbstractLanguageAnalyser
extends AbstractProcessingResource
{
  /** Initialise this resource, and return it.*/
  public Resource init() throws ResourceInstantiationException {
    return this;
  } // init()

  /** Run the resource. It doesn't make sense not to override
   *  this in subclasses so the default implementation signals an
   *  exception.
   */
  public void run() {
    executionException = new ExecutionException(
      "Resource " + getClass() + " hasn't overriden the run() method"
    );
    return;
  } // run()

  /** Trigger any exception that was caught when <CODE>run()</CODE> was
   *  invoked. If there is an exception stored it is cleared by this call.
   */
  public void check() throws ExecutionException {
    if(executionException != null) {
      ExecutionException e = executionException;
      executionException = null;
      throw e;
    }
  } // check()

  /** Set the document property for this analyser. */
  public void setDocument(Document document) {
    this.document = document;
  } // setDocument()

  /** Get the document property for this analyser. */
  public Document getDocument() {
    return document;
  } // getDocument()

  /** The document property for this analyser. */
  protected Document document;

  /** Set the corpus property for this analyser. */
  public void setCorpus(Corpus corpus) {
    this.corpus = corpus;
  } // setCorpus()

  /** Get the corpus property for this analyser. */
  public Corpus getCorpus() {
    return corpus;
  } // getCorpus()

  /** The corpus property for this analyser. */
  protected Corpus corpus;

  /** Any exception caught during run() invocations are stored here. */
  protected ExecutionException executionException  = null;

} // class AbstractLanguageAnalyser
