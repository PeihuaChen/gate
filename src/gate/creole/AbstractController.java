/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 27 Sep 2001
 *
 *  $I$
 */
package gate.creole;

import java.util.*;

import gate.*;
import gate.util.*;
import gate.event.*;


public abstract class AbstractController extends AbstractResource
                                         implements Controller{


  //executable code
  /**
   * Starts the execution of this executable
   */
  public void execute() throws ExecutionException {
    throw new ExecutionException(
      "Controller " + getClass() + " hasn't overriden the execute() method"
    );  }


  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException{
    return this;
  }

  /** Clears the internal data of the resource, when it gets released **/
  public void cleanup(){
  }


  /**
   * Notifies all the PRs in this controller that they should stop their
   * execution as soon as possible.
   */
  public synchronized void interrupt(){
    interrupted = true;
    Iterator prIter = getPRs().iterator();
    while(prIter.hasNext()){
      ((ProcessingResource)prIter.next()).interrupt();
    }
  }

  public synchronized boolean isInterrupted() {
    return interrupted;
  }


  //events code
  /**
   * Removes a {@link gate.event.StatusListener} from the list of listeners for
   * this processing resource
   */
  public synchronized void removeStatusListener(StatusListener l) {
    if (statusListeners != null && statusListeners.contains(l)) {
      Vector v = (Vector) statusListeners.clone();
      v.removeElement(l);
      statusListeners = v;
    }
  }

  /**
   * Adds a {@link gate.event.StatusListener} to the list of listeners for
   * this processing resource
   */
  public synchronized void addStatusListener(StatusListener l) {
    Vector v = statusListeners == null ? new Vector(2) : (Vector) statusListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      statusListeners = v;
    }
  }

  /**
   * Notifies all the {@link gate.event.StatusListener}s of a change of status.
   * @param e the message describing the status change
   */
  protected void fireStatusChanged(String e) {
    if (statusListeners != null) {
      Vector listeners = statusListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((StatusListener) listeners.elementAt(i)).statusChanged(e);
      }
    }
  }

  /**
   * Adds a {@link gate.event.ProgressListener} to the list of listeners for
   * this processing resource.
   */
  public synchronized void addProgressListener(ProgressListener l) {
    Vector v = progressListeners == null ? new Vector(2) : (Vector) progressListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      progressListeners = v;
    }
  }

  /**
   * Removes a {@link gate.event.ProgressListener} from the list of listeners
   * for this processing resource.
   */
  public synchronized void removeProgressListener(ProgressListener l) {
    if (progressListeners != null && progressListeners.contains(l)) {
      Vector v = (Vector) progressListeners.clone();
      v.removeElement(l);
      progressListeners = v;
    }
  }

  /**
   * Notifies all the {@link gate.event.ProgressListener}s of a progress change
   * event.
   * @param e the new value of execution completion
   */
  protected void fireProgressChanged(int e) {
    if (progressListeners != null) {
      Vector listeners = progressListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((ProgressListener) listeners.elementAt(i)).progressChanged(e);
      }
    }
  }

  /**
   * Notifies all the {@link gate.event.ProgressListener}s of a progress
   * finished.
   */
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
  protected class IntervalProgressListener implements ProgressListener{
    public IntervalProgressListener(int start, int end){
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


  /**
   * A simple status listener used to forward the events upstream.
   */
  protected class InternalStatusListener implements StatusListener{
    public void statusChanged(String message){
      fireStatusChanged(message);
    }
  }


  /**
   * Checks whether all the contained PRs have all the required runtime
   * parameters set.
   *
   * @return a {@link List} of {@link ProcessingResource}s that have required
   * parameters with null values if they exist <tt>null</tt> otherwise.
   * @throw {@link ResourceInstantiationException} if problems occur while
   * inspecting the parameters for one of the resources. These will normally be
   * introspection problems and are usually caused by the lack of a parameter
   * or of the read accessor for a parameter.
   */
  public List getOffendingPocessingResources()
         throws ResourceInstantiationException{
    //take all the contained PRs
    ArrayList badPRs = new ArrayList(getPRs());
    //remove the ones that no parameters problems
    Iterator prIter = getPRs().iterator();
    while(prIter.hasNext()){
      ProcessingResource pr = (ProcessingResource)prIter.next();
      ResourceData rData = (ResourceData)Gate.getCreoleRegister().
                                              get(pr.getClass().getName());
      if(AbstractResource.checkParameterValues(pr,
                                               rData.getParameterList().
                                               getRuntimeParameters())){
        badPRs.remove(pr);
      }
    }
    return badPRs.isEmpty() ? null : badPRs;
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

  /**
   * The list of {@link gate.event.StatusListener}s registered with this
   * resource
   */
  private transient Vector statusListeners;

  /**
   * The list of {@link gate.event.ProgressListener}s registered with this
   * resource
   */
  private transient Vector progressListeners;


  protected boolean interrupted = false;
}