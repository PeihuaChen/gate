/*
 *  Transducer.java - transducer class
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 24/07/98
 *
 *  $Id$
 */


package gate.jape;

import java.util.*;
import java.net.*;
import java.io.*;

import com.objectspace.jgl.*;

import gate.annotation.*;
import gate.util.*;
import gate.event.*;
import gate.*;


/**
  * Represents a single or multiphase transducer.
  */
public abstract class Transducer implements Serializable
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Name of this transducer. */
  protected String name;

  /** Get the phase name of this transducer */
  public String getName() { return name; }

  /** Transduce a document.  */
  public abstract void transduce(Document doc, AnnotationSet inputAS,
                                 AnnotationSet outputAS)
                                 throws JapeException;

  /** Finish: replace dynamic data structures with Java arrays; called
    * after parsing.
    */
  public abstract void finish();

  /** Clean up (delete action class files, for e.g.). */
  public abstract void cleanUp();

  /** Create a string representation of the object with padding. */
  public abstract String toString(String pad);

  //StatusReporter Implementation
  public void addStatusListener(StatusListener listener){
    myStatusListeners.add(listener);
  }

  public void removeStatusListener(StatusListener listener){
    myStatusListeners.remove(listener);
  }

  protected void fireStatusChangedEvent(String text){
    Iterator listenersIter = myStatusListeners.iterator();
    while(listenersIter.hasNext())
      ((StatusListener)listenersIter.next()).statusChanged(text);
  }

  //ProcessProgressReporter implementation
  public void addProcessProgressListener(ProgressListener listener){
    myProgressListeners.add(listener);
  }

  public void removeProcessProgressListener(ProgressListener listener){
    myProgressListeners.remove(listener);
  }

  protected void fireProgressChangedEvent(int i){
    Iterator listenersIter = myProgressListeners.iterator();
    while(listenersIter.hasNext())
      ((ProgressListener)listenersIter.next()).progressChanged(i);
  }

  protected void fireProcessFinishedEvent(){
    Iterator listenersIter = myProgressListeners.iterator();
    while(listenersIter.hasNext())
      ((ProgressListener)listenersIter.next()).processFinished();
  }
  public void setBaseURL(java.net.URL newBaseURL) {
    baseURL = newBaseURL;
  }
  public java.net.URL getBaseURL() {
    return baseURL;
  }






  private transient List myProgressListeners = new LinkedList();

  private transient List myStatusListeners = new LinkedList();


  private URL baseURL;

  //ProcessProgressReporter implementation ends here

} // class Transducer



