/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 11 Apr 2002
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

/**
 * Execute a list of PRs serially. For each PR a running strategy is stored
 * which decides whether the PR will be run always, never or upon a condition
 * being satisfied.
 * This controller uses {@link AnalyserRunningStrategy} objects as running
 * strategies and they only work with {@link LanguageAnalyser}s so the PRs that
 * are not analysers will get a default &quot;run always&quot; strategy.
 */
public class ConditionalSerialController extends SerialController
                                         implements ConditionalController{

  public ConditionalSerialController(){
    strategiesList = new ArrayList();
  }

  public Collection getRunningStrategies(){
    return Collections.unmodifiableList(strategiesList);
  }

  /**
   * Set a PR at a specified location.
   * The running strategy defaults to run always.
   * @param index the position for the PR
   * @param pr the PR to be set.
   */
  public void add(int index, ProcessingResource pr){
    super.add(index, pr);
    if(pr instanceof LanguageAnalyser){
      strategiesList.add(index,
                         new AnalyserRunningStrategy((LanguageAnalyser)pr,
                                                      RunningStrategy.RUN_ALWAYS,
                                                      null, null));
    }else{
      strategiesList.add(index, new RunningStrategy.RunAlwaysStrategy(pr));
    }
  }

  /**
   * Add a PR to the end of the execution list.
   * @param pr the PR to be added.
   */
  public void add(ProcessingResource pr){
    super.add(pr);
    if(pr instanceof LanguageAnalyser){
      strategiesList.add(new AnalyserRunningStrategy((LanguageAnalyser)pr,
                                                      RunningStrategy.RUN_ALWAYS,
                                                      null, null));
    }else{
      strategiesList.add(new RunningStrategy.RunAlwaysStrategy(pr));
    }
  }

  public ProcessingResource remove(int index){
    ProcessingResource aPr = super.remove (index);
    strategiesList.remove(index);
    return aPr;
  }

  public boolean remove(ProcessingResource pr){
    int index = prList.indexOf(pr);
    if(index != -1){
      prList.remove(index);
      strategiesList.remove(index);
      return true;
    }
    return false;
  }

  public void setRunningStrategy(int index, AnalyserRunningStrategy strategy){
    strategiesList.set(index, strategy);
  }

  /**
   * Populates this controller with the appropiate running strategies from a
   * collection of running strategies
   * (optional operation).
   *
   * Controllers that are serializable must implement this method needed by GATE
   * to restore their contents.
   * @throws UnsupportedOperationException if the <tt>setPRs</tt> method
   * 	       is not supported by this controller.
   */
  public void setRunningStrategies(Collection strategies){
    strategiesList.clear();
    Iterator stratIter = strategies.iterator();
    while(stratIter.hasNext()) strategiesList.add(stratIter.next());
  }

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
    if(((RunningStrategy)strategiesList.get(componentIndex)).shouldRun()){
      currentPR.execute();
    }


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
    super.cleanup();
    strategiesList.clear();
  }


  /**
   * The list of running strategies for the member PRs.
   */
  protected List strategiesList;
} // class SerialController
