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
public class SerialController extends AbstractController{

  public SerialController(){
    prList = Collections.synchronizedList(new ArrayList());
    sListener = new InternalStatusListener();
  }

  /**
   * Returns all the {@link gate.ProcessingResource}s contained by this
   * controller.
   * The actual type of collection returned is a list. The returned list is
   * backed by this controller; any changes made to it will reflect in its
   * contents.
   */
  public Collection getPRs(){
    return prList;
  }


  /** Run the Processing Resources in sequence. */
  public void execute() throws ExecutionException{
    interrupted = false;
    //stop access to the list of PRs
    prList = Collections.unmodifiableList(prList);
    try{
      for (int i = 0; i < prList.size(); i++){
        if(isInterrupted()) throw new ExecutionInterruptedException(
          "The execution of the " + getName() +
          " application has been abruptly interrupted!");
        runComponent(i);
      }
    }finally{
      //restore access to the list of PRs
      prList = Collections.synchronizedList(new ArrayList(prList));
    }
  } // execute()


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

    //start DB transactions

    //run the thing
    currentPR.execute();

    //commit DB transactions


    //remove the listeners
    try{
      AbstractResource.removeResourceListeners(currentPR, listeners);
    }catch(Exception e){
      // the listeners removing failed; nothing important
      Err.prln("Could not clear listeners for " +
               currentPR.getClass().getName() +
               "\n" + e.toString() + "\n...nothing to lose any sleep over.");
    }
  }

  /** The list of contained PRs*/
  protected List prList;

  /** A proxy for status events*/
  protected StatusListener sListener;

} // class SerialController
