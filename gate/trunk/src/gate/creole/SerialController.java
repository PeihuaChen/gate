/*
 *  SerialController.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 9/Nov/2000
 *
 *  $Id$
 */

package gate.creole;

import java.util.*;
import java.io.*;

import gate.*;
import gate.util.*;
import gate.creole.*;
import gate.event.*;

/** Execute a list of PRs serially.
  */
public class SerialController
extends ArrayList implements Controller, List
{
  /** Run the Processing Resources in sequence. */
  public void execute() throws ExecutionException{
    Iterator iter = iterator();
    while(iter.hasNext()) {
      ProcessingResource pr = (ProcessingResource) iter.next();
      ResourceData rd =
        (ResourceData) Gate.getCreoleRegister().get(pr.getClass().getName());
      ParameterList params = rd.getParameterList();
      try {
        pr.setParameterValues(params.getRuntimeDefaults());
      } catch(Exception e) {
        throw new ExecutionException("Couldn't set parameters: " + e);
      }
      pr.execute();
    } // for each PR in the resourceList

  } // execute()

  public boolean isInterrupted(){
    return interrupted;
  }

  public void interrupt(){
    interrupted = true;
  }

    /** Sets the name of this resource*/
  public void setName(String name){
    this.name = name;
  }

  /** Returns the name of this resource*/
  public String getName(){
    return name;
  }

  protected String name;

  public void setRuntimeParameters(FeatureMap parameters){
  }

  /** Get the feature set */
  public FeatureMap getFeatures() { return features; }

  /** Set the feature set */
  public void setFeatures(FeatureMap features) { this.features = features; }

  /** The feature set */
  protected FeatureMap features;

  /**
   * Two controller that contain the same modules are not equal.
   * Two controllers are only equal if they are the same.
   * equals() overriden to return "==".
   */
  public boolean equals(Object other){
    return this == other;
  }

  protected boolean interrupted = false;
} // class SerialController
