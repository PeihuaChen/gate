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
import gate.event.*;

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

/** should clear all internal data of the resource. Does nothing now **/
  public void clear() {
  }

  public synchronized void removeStatusListener(StatusListener l) {
    if (statusListeners != null && statusListeners.contains(l)) {
      Vector v = (Vector) statusListeners.clone();
      v.removeElement(l);
      statusListeners = v;
    }
  }

  public synchronized void addStatusListener(StatusListener l) {
    Vector v = statusListeners == null ? new Vector(2) : (Vector) statusListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      statusListeners = v;
    }
  }

  protected void fireStatusChanged(String e) {
    if (statusListeners != null) {
      Vector listeners = statusListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((StatusListener) listeners.elementAt(i)).statusChanged(e);
      }
    }
  }

  public synchronized void addProgressListener(ProgressListener l) {
    Vector v = progressListeners == null ? new Vector(2) : (Vector) progressListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      progressListeners = v;
    }
  }

  public synchronized void removeProgressListener(ProgressListener l) {
    if (progressListeners != null && progressListeners.contains(l)) {
      Vector v = (Vector) progressListeners.clone();
      v.removeElement(l);
      progressListeners = v;
    }
  }

  protected void fireProgressChanged(int e) {
    if (progressListeners != null) {
      Vector listeners = progressListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((ProgressListener) listeners.elementAt(i)).progressChanged(e);
      }
    }
  }

  protected void fireProcessFinished() {
    if (progressListeners != null) {
      Vector listeners = progressListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((ProgressListener) listeners.elementAt(i)).processFinished();
      }
    }
  }

  /**
   * A progress listener used to convert a 0..100 interval into a smaller one
   */
  protected class CustomProgressListener implements ProgressListener{
    public CustomProgressListener(int start, int end){
      this.start = start;
      this.end = end;
    }
    public void progressChanged(int i){
      fireProgressChanged(start + (end - start) * i / 100);
    }

    public void processFinished(){
      fireProgressChanged(end);
    }

    int start;
    int end;
  }//CustomProgressListener

  private transient Vector statusListeners;
  private transient Vector progressListeners;

  /** Any exception caught during run() invocations are stored here. */
  protected ExecutionException executionException  = null;

} // class AbstractProcessingResource
