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


  public void setBaseURL(java.net.URL newBaseURL) {
    baseURL = newBaseURL;
  }
  public java.net.URL getBaseURL() {
    return baseURL;
  }
  public synchronized void removeProgressListener(ProgressListener l) {
    if (progressListeners != null && progressListeners.contains(l)) {
      Vector v = (Vector) progressListeners.clone();
      v.removeElement(l);
      progressListeners = v;
    }
  }
  public synchronized void addProgressListener(ProgressListener l) {
    Vector v = progressListeners == null ? new Vector(2) : (Vector) progressListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      progressListeners = v;
    }
  }






  private URL baseURL;

  private transient Vector progressListeners;
  private transient Vector statusListeners;
  /**
   * This property affects the Appelt style of rules application.
   * If true then the longest match will be fired otherwise the shortest will
   * be used. By default it is true.
   */
  protected void fireProgressChanged(int e) {
    if (Main.batchMode)
      return;
    if (progressListeners != null || progressListeners.isEmpty()) {
      Vector listeners = progressListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((ProgressListener) listeners.elementAt(i)).progressChanged(e);
      }
    }
  }
  protected void fireProcessFinished() {
    if (progressListeners != null && !Main.batchMode) {
      Vector listeners = progressListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((ProgressListener) listeners.elementAt(i)).processFinished();
      }
    }
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
    if (statusListeners != null && !Main.batchMode) {
      Vector listeners = statusListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((StatusListener) listeners.elementAt(i)).statusChanged(e);
      }
    }
  }

  //ProcessProgressReporter implementation ends here

} // class Transducer



