/*
 *  AbstractProcessingResource.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 10/Nov/2000
 *
 *  $Id$
 */

package gate.creole;

import java.util.*;

import gate.*;
import gate.util.*;

/** A convenience implementation of ProcessingResource with some default
  * code.
  */
abstract public class AbstractProcessingResource
extends AbstractResource implements ProcessingResource
{
  /** Initialise this resource, and return it. */
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

  /**
   * Reinitialises the processing resource. After calling this method the
   * resource should be in the state it is after calling init.
   * If the resource depends on external resources (such as rules files) then
   * the resource will re-read those resources. If the data used to create
   * the resource has changed since the resource has been created then the
   * resource will change too after calling reInit().
   * The implementation in this class simply calls {@link #init()}. This
   * functionality must be overriden by derived classes as necessary.
   */
  public void reInit() throws ResourceInstantiationException{
    init();
  } // reInit()

  public void setRuntimeParameters(FeatureMap parameters) {
    Out.println(parameters);
  }

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

  /** Any exception caught during run() invocations are stored here. */
  protected ExecutionException executionException  = null;

} // class AbstractProcessingResource
