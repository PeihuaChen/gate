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

import gate.*;
import gate.event.*;
import gate.util.Err;
import gate.util.profile.Profiler;

/** Execute a list of PRs serially.
 */
public class SerialController extends AbstractController
    implements CreoleListener{
  private final static boolean DEBUG = false;

  /** Profiler to track PR execute time */
  protected Profiler prof;

  public SerialController(){
    prList = Collections.synchronizedList(new ArrayList());
    sListener = new InternalStatusListener();

    if(DEBUG) {
      prof = new Profiler();
      prof.enableGCCalling(false);
      prof.printToSystemOut(true);
    }
  }

  /**
   * Returns all the {@link gate.ProcessingResource}s contained by this
   * controller as an unmodifiable list.
   */
  public Collection getPRs(){
    return Collections.unmodifiableList(prList);
  }

  /**
   * Populates this controller from a collection of {@link ProcessingResource}s
   * (optional operation).
   *
   * Controllers that are serializable must implement this method needed by GATE
   * to restore the contents of the controllers.
   * @throws UnsupportedOperationException if the <tt>setPRs</tt> method
   * 	       is not supported by this controller.
   */
  public void setPRs(Collection prs){
    prList.clear();
    Iterator prIter = prs.iterator();
    while(prIter.hasNext()) prList.add(prIter.next());
  }

  public void add(int index, ProcessingResource pr){
    prList.add(index, pr);
  }

  public void add(ProcessingResource pr){
    prList.add(pr);
  }

  public ProcessingResource remove(int index){
    return (ProcessingResource)prList.remove(index);
  }

  public boolean remove(ProcessingResource pr){
    return prList.remove(pr);
  }

  public ProcessingResource set(int index, ProcessingResource pr){
    return (ProcessingResource)prList.set(index, pr);
  }

  /**
   * Verifies that all PRs have all their required rutime parameters set.
   */
  protected void checkParameters() throws ExecutionException{
    List badPRs;
    try{
      badPRs = getOffendingPocessingResources();
    }catch(ResourceInstantiationException rie){
      throw new ExecutionException(
          "Could not check runtime parameters for the processing resources:\n" +
          rie.toString());
    }
    if(badPRs != null && !badPRs.isEmpty()){
      throw new ExecutionException(
          "Some of the processing resources in this controller have unset " +
          "runtime parameters:\n" +
          badPRs.toString());
    }
  }

  /** Run the Processing Resources in sequence. */
  public void execute() throws ExecutionException{
    //check all the PRs have the right parameters
    checkParameters();

    if(DEBUG) {
      prof.initRun("Execute controller [" + getName() + "]");
    }

    //execute all PRs in sequence
    interrupted = false;
    for (int i = 0; i < prList.size(); i++){
      if(isInterrupted()) throw new ExecutionInterruptedException(
          "The execution of the " + getName() +
          " application has been abruptly interrupted!");

      runComponent(i);
      if (DEBUG) {
        prof.checkPoint("~Execute PR ["+((ProcessingResource)
                                   prList.get(i)).getName()+"]");
      }
    }

    if (DEBUG) {
      prof.checkPoint("Execute controller [" + getName() + "] finished");
    }

  } // execute()


  /**
   * Executes a {@link ProcessingResource}.
   */
  protected void runComponent(int componentIndex) throws ExecutionException{
    ProcessingResource currentPR = (ProcessingResource)
                                   prList.get(componentIndex);

    //create the listeners
    FeatureMap listeners = Factory.newFeatureMap();
    listeners.put("gate.event.StatusListener", sListener);
    int componentProgress = 100 / prList.size();
    listeners.put("gate.event.ProgressListener",
                  new IntervalProgressListener(
                  componentIndex * componentProgress,
                  (componentIndex +1) * componentProgress)
                  );

    //add the listeners
    try{
      AbstractResource.setResourceListeners(currentPR, listeners);
    }catch(Exception e){
      // the listeners setting failed; nothing important
      Err.prln("Could not set listeners for " + currentPR.getClass().getName() +
               "\n" + e.toString() + "\n...nothing to lose any sleep over.");
    }

    //run the thing
    currentPR.execute();

    //remove the listeners
    try{
      AbstractResource.removeResourceListeners(currentPR, listeners);
    }catch(Exception e){
      // the listeners removing failed; nothing important
      Err.prln("Could not clear listeners for " +
               currentPR.getClass().getName() +
               "\n" + e.toString() + "\n...nothing to lose any sleep over.");
    }
  }//protected void runComponent(int componentIndex)

  /**
   * Cleans the internal data and prepares this object to be collected
   */
  public void cleanup(){
    // Diana desire to remove PR-s
    Resource res;
    for(int i=0; i<prList.size(); ++i) {
      res = (Resource) prList.get(i);
      Factory.deleteResource(res);
    } // for
    prList.clear();
  }

  /** The list of contained PRs*/
  protected List prList;

  /** A proxy for status events*/
  protected StatusListener sListener;
  public void resourceLoaded(CreoleEvent e) {
  }
  public void resourceUnloaded(CreoleEvent e) {
    //remove all occurences of the resource from this controller
    if(e.getResource() instanceof ProcessingResource)
      while(prList.remove(e.getResource()));
  }

  public void resourceRenamed(Resource resource, String oldName,
                              String newName){
  }

  public void datastoreOpened(CreoleEvent e) {
  }
  public void datastoreCreated(CreoleEvent e) {
  }
  public void datastoreClosed(CreoleEvent e) {
  }

} // class SerialController