/*
 *  SerialController.java
 *
 *  Copyright (c) 1998-2005, The University of Sheffield.
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

import org.apache.log4j.Logger;

import gate.*;
import gate.creole.metadata.*;
import gate.event.*;
import gate.util.Benchmark;
import gate.util.Benchmarkable;
import gate.util.Err;
import gate.util.GateRuntimeException;
import gate.util.profile.Profiler;
import gate.util.Out;

/**
 * Execute a list of PRs serially.
 */
@CreoleResource(name = "Pipeline",
    comment = "A simple serial controller for PR pipelines.",
    helpURL = "http://gate.ac.uk/cgi-bin/userguide/sec:howto:apps")
public class SerialController extends AbstractController implements
                                                        CreoleListener {

  protected static final Logger log = Logger.getLogger(SerialController.class);

  /** Profiler to track PR execute time */
  protected Profiler prof;
  protected HashMap timeMap;
  protected HashMap<String, Long> prTimeMap;

  public SerialController() {
    prList = Collections.synchronizedList(new ArrayList());
    sListener = new InternalStatusListener();
    prTimeMap = new HashMap<String, Long>();

    if(log.isDebugEnabled()) {
      prof = new Profiler();
      prof.enableGCCalling(false);
      prof.printToSystemOut(true);
      timeMap = new HashMap();
    }
    Gate.getCreoleRegister().addCreoleListener(this);
  }

  /**
   * Returns all the {@link gate.ProcessingResource}s contained by thisFeature
   * controller as an unmodifiable list.
   */
  public Collection getPRs() {
    return Collections.unmodifiableList(prList);
  }

  /**
   * Populates this controller from a collection of {@link ProcessingResource}s
   * (optional operation).
   *
   * Controllers that are serializable must implement this method needed by GATE
   * to restore the contents of the controllers.
   *
   * @throws UnsupportedOperationException
   *           if the <tt>setPRs</tt> method is not supported by this
   *           controller.
   */
  public void setPRs(Collection prs) {
    prList.clear();
    Iterator prIter = prs.iterator();
    while(prIter.hasNext())
      add((ProcessingResource)prIter.next());
  }

  public void add(int index, ProcessingResource pr) {
    prList.add(index, pr);
    fireResourceAdded(new ControllerEvent(this, ControllerEvent.RESOURCE_ADDED,
      pr));
  }

  public void add(ProcessingResource pr) {
    prList.add(pr);
    fireResourceAdded(new ControllerEvent(this, ControllerEvent.RESOURCE_ADDED,
      pr));
  }

  public ProcessingResource remove(int index) {
    ProcessingResource aPr = (ProcessingResource)prList.remove(index);
    fireResourceRemoved(new ControllerEvent(this,
      ControllerEvent.RESOURCE_REMOVED, aPr));
    return aPr;
  }

  public boolean remove(ProcessingResource pr) {
    boolean ret = prList.remove(pr);
    if(ret)
      fireResourceRemoved(new ControllerEvent(this,
        ControllerEvent.RESOURCE_REMOVED, pr));
    return ret;
  }

  public ProcessingResource set(int index, ProcessingResource pr) {
    return (ProcessingResource)prList.set(index, pr);
  }

  /**
   * Verifies that all PRs have all their required rutime parameters set.
   */
  protected void checkParameters() throws ExecutionException {
    List badPRs;
    try {
      badPRs = getOffendingPocessingResources();
    }
    catch(ResourceInstantiationException rie) {
      throw new ExecutionException(
        "Could not check runtime parameters for the processing resources:\n"
          + rie.toString());
    }
    if(badPRs != null && !badPRs.isEmpty()) { throw new ExecutionException(
      "Some of the processing resources in this controller have unset "
        + "runtime parameters:\n" + badPRs.toString()); }
  }

  /** Run the Processing Resources in sequence. */
  protected void executeImpl() throws ExecutionException {
    // check all the PRs have the right parameters
    checkParameters();

    if(log.isDebugEnabled()) {
      prof.initRun("Execute controller [" + getName() + "]");
    }

    // execute all PRs in sequence
    interrupted = false;
    for(int i = 0; i < prList.size(); i++) {
      if(isInterrupted()) {

      throw new ExecutionInterruptedException("The execution of the "
        + getName() + " application has been abruptly interrupted!"); }

      runComponent(i);
      if(log.isDebugEnabled()) {
        prof.checkPoint("~Execute PR ["
          + ((ProcessingResource)prList.get(i)).getName() + "]");
        Long timeOfPR =
          (Long)timeMap.get(((ProcessingResource)prList.get(i)).getName());
        if(timeOfPR == null)
          timeMap.put(((ProcessingResource)prList.get(i)).getName(), new Long(
            prof.getLastDuration()));
        else timeMap.put(((ProcessingResource)prList.get(i)).getName(),
          new Long(timeOfPR.longValue() + prof.getLastDuration()));
        log.debug("Time taken so far by "
          + ((ProcessingResource)prList.get(i)).getName() + ": "
          + timeMap.get(((ProcessingResource)prList.get(i)).getName()));

      }
    }

    if(log.isDebugEnabled()) {
      prof.checkPoint("Execute controller [" + getName() + "] finished");
    }

  } // executeImpl()

  /**
   * Resets the Time taken by various PRs
   */
  public void resetPrTimeMap() {
    prTimeMap.clear();
  }

  /**
   * Returns the HashMap that lists the total time taken by each PR
   * @return
   */
  public HashMap<String, Long> getPrTimeMap() {
    return this.prTimeMap;
  }

  /**
   * Executes a {@link ProcessingResource}.
   */
  protected void runComponent(int componentIndex) throws ExecutionException {
    ProcessingResource currentPR =
      (ProcessingResource)prList.get(componentIndex);

    // create the listeners
    FeatureMap listeners = Factory.newFeatureMap();
    listeners.put("gate.event.StatusListener", sListener);
    int componentProgress = 100 / prList.size();
    listeners.put("gate.event.ProgressListener", new IntervalProgressListener(
      componentIndex * componentProgress, (componentIndex + 1)
        * componentProgress));

    // add the listeners
    try {
      AbstractResource.setResourceListeners(currentPR, listeners);
    }
    catch(Exception e) {
      // the listeners setting failed; nothing important
      log.error("Could not set listeners for " + currentPR.getClass().getName()
        + "\n" + e.toString() + "\n...nothing to lose any sleep over.");
    }

    benchmarkFeatures.put(Benchmark.PR_NAME_FEATURE, currentPR.getName());

    long startTime = System.currentTimeMillis();
    // run the thing
    Benchmark.executeWithBenchmarking(currentPR,
            Benchmark.createBenchmarkId(Benchmark.PR_PREFIX + currentPR.getName(),
                    getBenchmarkId()), this, benchmarkFeatures);

    benchmarkFeatures.remove(Benchmark.PR_NAME_FEATURE);

    // calculate the time taken by the PR
    long timeTakenByThePR = System.currentTimeMillis() - startTime;
    Long time = prTimeMap.get(currentPR.getName());
    if(time == null) {
      time = new Long(0);
    }
    time = new Long(time.longValue() + timeTakenByThePR);
    prTimeMap.put(currentPR.getName(), time);


    // remove the listeners
    try {
      AbstractResource.removeResourceListeners(currentPR, listeners);
    }
    catch(Exception e) {
      // the listeners removing failed; nothing important
      log.error("Could not clear listeners for "
        + currentPR.getClass().getName() + "\n" + e.toString()
        + "\n...nothing to lose any sleep over.");
    }
  }// protected void runComponent(int componentIndex)

  /**
   * Cleans the internal data and prepares this object to be collected
   */
  public void cleanup() {
    // close all PRs in this controller
    if(prList != null && !prList.isEmpty()) {
      for(Iterator prIter = new ArrayList(prList).iterator(); prIter.hasNext(); Factory
        .deleteResource((Resource)prIter.next()))
        ;
    }
    Gate.getCreoleRegister().removeCreoleListener(this);
  }

  /** The list of contained PRs */
  protected List prList;

  /** A proxy for status events */
  protected StatusListener sListener;

  public void resourceLoaded(CreoleEvent e) {
  }

  public void resourceUnloaded(CreoleEvent e) {
    // remove all occurences of the resource from this controller
    if(e.getResource() instanceof ProcessingResource)
    // while(prList.remove(e.getResource()));
      // remove all occurrences of this PR from the controller
      // Use the controller's remove method (rather than prList's so that
      // subclasses of this controller type can add their own functionality
      while(remove((ProcessingResource)e.getResource()))
        ;
    // remove links in parameters
    for(int i = 0; i < prList.size(); i++) {
      ProcessingResource aPr = (ProcessingResource)prList.get(i);
      ResourceData rData =
        (ResourceData)Gate.getCreoleRegister().get(aPr.getClass().getName());
      if(rData != null) {
        Iterator rtParamDisjIter =
          rData.getParameterList().getRuntimeParameters().iterator();
        while(rtParamDisjIter.hasNext()) {
          List aDisjunction = (List)rtParamDisjIter.next();
          Iterator rtParamIter = aDisjunction.iterator();
          while(rtParamIter.hasNext()) {
            Parameter aParam = (Parameter)rtParamIter.next();
            String paramName = aParam.getName();
            try {
              if(aPr.getParameterValue(paramName) == e.getResource()) {
                aPr.setParameterValue(paramName, null);
              }
            }
            catch(ResourceInstantiationException rie) {
              // nothing to do - this should never happen
              throw new GateRuntimeException(rie);
            }
          }
        }
      }
    }
  }

  public void resourceRenamed(Resource resource, String oldName, String newName) {
  }

  public void datastoreOpened(CreoleEvent e) {
  }

  public void datastoreCreated(CreoleEvent e) {
  }

  public void datastoreClosed(CreoleEvent e) {
  }

} // class SerialController
