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

/** Execute a list of PRs serially.
  */
public class SerialController
extends ArrayList implements Controller, List
{
  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {
    return this;
  } // init()

  /** Run the Processing Resources in sequence. */
  public void run() {
    Iterator iter = iterator();
    while(iter.hasNext()) {
      ProcessingResource pr = (ProcessingResource) iter.next();
      ResourceData rd =
        (ResourceData) Gate.getCreoleRegister().get(pr.getClass().getName());
      ParameterList params = rd.getParameterList();
      try {
        Factory.setResourceParameters(pr, params.getRuntimeDefaults());
      } catch(Exception e) {
        executionException =
          new ExecutionException("Couldn't set parameters: " + e);
        return;
      }

      pr.run();
      try {
        pr.check();
      } catch(ExecutionException e) {
        executionException = e;
        return;
      }
    } // for each PR in the resourceList

  } // run()

  /** Trigger any exception that was caught when <CODE>run()</CODE> was
    * invoked. If there is an exception stored it is cleared by this call.
    */
  public void check() throws ExecutionException {
    if(executionException != null) {
      ExecutionException e = executionException;
      executionException = null;
      throw e;
    }
  } // check()

  public void setRuntimeParameters(FeatureMap parameters){
  }

  /** Any exception caught during run() invocations are stored here. */
  protected ExecutionException executionException  = null;

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

} // class SerialController
