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
import gate.gui.ResourceHandle;

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
    return this;
  }//init()

  /**
   * Called by the GUI when this viewer/editor has to initialise itself for a
   * specific object.
   * @param target the object (be it a {@link gate.Resource},
   * {@link gate.DataStore} or whatever) this viewer has to display
   */
  public void setTarget(Object target){
    throw new RuntimeException(
      "Class " + getClass() + " hasn't implemented the setTarget() method!");
  }


  /**
   * Used by the main GUI to tell this VR what handle created it. The VRs can
   * use this information e.g. to add items to the popup for the resource.
   */
  public void setHandle(ResourceHandle handle){
  }


  // Properties for the resource
  protected FeatureMap features;

}//AbstractVisualResource