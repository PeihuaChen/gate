/*
 *  Coreferencer.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 18/Dec/2001
 *
 *  $Id$
 */

package gate.creole.coref;

import junit.framework.*;

import gate.*;
import gate.creole.*;


public class Coreferencer extends AbstractLanguageAnalyser
                          implements ProcessingResource{

//  private Document  doc;
  private PronominalCoref pronominalModule;

  public Coreferencer() {
  }


  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {
    Resource result = super.init();
    //load all submodules
    this.pronominalModule = (PronominalCoref)Factory.createResource("gate.creole.coref.PronominalCoref");

    return result;
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


  /** Get the document we're running on. */
/*  public Document getDocument() {
    return this.doc;
  }
*/

  /** Set the document to run on. */
  public void setDocument(Document newDocument) {
    Assert.assertNotNull(newDocument);
    this.pronominalModule.setDocument(newDocument);

    super.setDocument(newDocument);
  }


  /**
   * This method runs the coreferencer. It assumes that all the needed parameters
   * are set. If they are not, an exception will be fired.
   */
  public void execute() throws ExecutionException{
    this.pronominalModule.execute();
  }

}