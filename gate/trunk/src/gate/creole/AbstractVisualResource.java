/*
 *  AbstractVisualResource.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Cristian URSU, 24/Jan/2001
 *
 *  $Id$
 */

package gate.creole;

import javax.swing.JPanel;
import gate.*;
import gate.util.*;

/** A convenience implementation of VisualResource with some default code. */
public abstract class AbstractVisualResource extends JPanel
                                             implements VisualResource{

  /**
   * Package access constructor to stop normal initialisation.
   * This kind of resources should only be created by the Factory class
   */
  public AbstractVisualResource(){
  }


  /** Accessor for features. */
  public FeatureMap getFeatures(){
    return features;
  }//getFeatures()

  /** Mutator for features*/
  public void setFeatures(FeatureMap features){
    this.features = features;
  }// setFeatures()

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {
    alreadyInited = true;
    return this;
  }//init()

  /**
   * Visual resources are not initialised at creation time in order to save time
   * and memory, that's why they need to be initialised when they become
   * visible.
   */
  public void setVisible(boolean aFlag) {
    //make the resource visible first in case it displays some information
    //during initialisation (like a progress bar or something)
    super.setVisible(aFlag);
    //initialise the visual resource
    if(!alreadyInited) try{
                        init();
                       }catch(ResourceInstantiationException rie){
                         throw new GateRuntimeException(rie.toString());
                       }
  }
  // Properties for the resource
  protected FeatureMap features;

  protected boolean alreadyInited = false;

}//AbstractVisualResource