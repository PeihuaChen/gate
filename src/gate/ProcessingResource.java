/*
 *  ProcessingResource.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 11/Feb/2000
 *
 *  $Id$
 */

package gate;

import java.util.*;

import gate.util.*;
import gate.creole.*;

/** Models all sorts of processing resources.
  * Because <CODE>run()</CODE> doesn't throw exceptions, we
  * have a <CODE>check()</CODE> that will re-throw any exception
  * that was caught when <CODE>run()</CODE> was invoked.
  */
public interface ProcessingResource extends Resource, Runnable
{
  /** Trigger any exception that was caught when run() was invoked. */
  public void check() throws ExecutionException;

  /**
   * Sets the runtime parameters from a {@link FeatureMap}
   */
  public void setRuntimeParameters(FeatureMap parameters);

  /**
   * Reinitialises the processing resource. After calling this method the
   * resource should be in the state it is after calling init.
   * If the resource depends on external resources (such as rules files) then
   * the resource will re-read those resources. If the data used to create
   * the resource has changed since the resource has been created then the
   * resource will change too after calling reInit().
   */
  public void reInit() throws ResourceInstantiationException;

} // interface ProcessingResource
