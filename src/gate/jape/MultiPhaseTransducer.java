/*
 *  MultiPhaseTransducer.java - transducer class
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
import gate.annotation.*;
import gate.event.*;
import gate.util.*;
import gate.*;


/**
  * Represents a complete CPSL grammar, with a phase name, options and
  * rule set (accessible by name and by sequence).
  * Implements a transduce method taking a Document as input.
  * Constructs from String or File.
  */
public class MultiPhaseTransducer extends Transducer
implements JapeConstants, java.io.Serializable
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction from name. */
  public MultiPhaseTransducer(String name) {
    this();
    setName(name);
  } // constr from name

  /** Anonymous construction */
  public MultiPhaseTransducer() {
    phases = new ArrayList();
  } // anon construction

  /** Set the name. */
  public void setName(String name) { this.name = name; }

  /** The SinglePhaseTransducers that make up this one.
    * Keyed by their phase names.
    */
  private ArrayList phases;


  /** Add phase. */
  public void addPhase(String name, Transducer phase) {
    //Debug.pr(this, "MPT: adding " + name + Debug.getNl());
    phases.add(phase);
  } // addPhase

  /** Change the phase order to the one specified in a list of names. */
  public void orderPhases(String[] phaseNames) {
    Err.println("oops: MPT.orderPhases not done yet :-(");
    /*
    // for each phaseName
    //   destructively get the phase and add to new array map
    // errors: any phaseName not in phases,
    HashMap newPhaseMap = new HashMap();
    for(int i=0; i<phaseNames.length; i++) {
      Transducer t = (Transducer) phases.remove(phaseNames[i]);
      if(t == null) {
        // ERROR
      }
      else {
        newPhaseMap.add(t);
      }
    }
    phases = newPhaseMap;
    */
  } // orderPhases


  /** Finish: replace dynamic data structures with Java arrays; called
    * after parsing.
    */
  public void finish() {
    for(Iterator i = phases.iterator(); i.hasNext(); )
      ((Transducer) i.next()).finish();
  } // finish


  /** Transduce the document by running each phase in turn. */
  public void transduce(Document doc, AnnotationSet input,
                        AnnotationSet output) throws JapeException {

    ProgressListener pListener = null;
    StatusListener sListener = null;
    if (! Main.batchMode) {//fire events if not in batch mode
      pListener = new ProgressListener(){
        public void processFinished(){
          donePhases ++;
          if(donePhases == phasesCnt) fireProcessFinished();
        }

        public void progressChanged(int i){
          int value = (donePhases * 100 + i)/phasesCnt;
          fireProgressChanged(value);
        }

        int phasesCnt = phases.size();
        int donePhases = 0;
      };

      sListener = new StatusListener(){
        public void statusChanged(String text){
          fireStatusChanged(text);
        }
      };
    }//if

    for(Iterator i = phases.iterator(); i.hasNext(); ) {
      Transducer t = (Transducer) i.next();
      try {
        if (! Main.batchMode) {//fire events if not in batch mode
          fireStatusChanged("Transducing " + doc.getName() +
                               " (Phase: " + t.getName() + ")...");
          t.addProgressListener(pListener);
          t.addStatusListener(sListener);
        }//if
        t.transduce(doc, input, output);
        if (! Main.batchMode) {//fire events if not in batch mode
          t.removeProgressListener(pListener);
          t.removeStatusListener(sListener);
          fireStatusChanged("");
        }//if
      } catch(JapeException e) {
        String errorMessage = new String(
          "Error transducing document " + doc.getSourceUrl() +
          ", phase " + t.getName() + Strings.getNl() + e.getMessage()
        );
        throw(new JapeException(errorMessage));
      }
    }

    cleanUp();
  } // transduce

  /** Ask each phase to clean up (delete action class files, for e.g.). */
  public void cleanUp() {

    for(Iterator i = phases.iterator(); i.hasNext(); )
      ((Transducer) i.next()).cleanUp();

  } // cleanUp

  /** Create a string representation of the object. */
  public String toString() { return toString(""); }

  /** Create a string representation of the object. */
  public String toString(String pad) {
    String newline = Strings.getNl();

    StringBuffer buf = new StringBuffer(
      pad + "MPT: name(" + name + "); phases(" + newline + pad
    );

    for(Iterator i = phases.iterator(); i.hasNext(); )
      buf.append(
        ((Transducer) i.next()).toString(
            Strings.addPadding(pad, INDENT_PADDING)
        ) + " "
      );

    buf.append(newline + pad + ")." + newline);

    return buf.toString();
  } // toString

  //needed by FSM
  public ArrayList getPhases(){ return phases; }

} // class MultiPhaseTransducer



// $Log$
// Revision 1.18  2001/09/13 12:09:50  kalina
// Removed completely the use of jgl.objectspace.Array and such.
// Instead all sources now use the new Collections, typically ArrayList.
// I ran the tests and I ran some documents and compared with keys.
// JAPE seems to work well (that's where it all was). If there are problems
// maybe look at those new structures first.
//
// Revision 1.17  2001/09/12 15:24:44  kalina
// Made the batchMode flag in Main public. This is now checked before
// events are fired and listeners created. No bugs in tests or anywhere else
// yet. To disable events, set batchMode to true in your batch code. By default
// it is false, because some batch code e.g., MUSE, use events for progress
// indication. Not having events does give some small performance gains, but
// not much.
//
// Revision 1.16  2001/05/17 11:50:41  valyt
//
// 	Factory now handles Runtime parameters as well as inittime ones.
//
// 	There is a new rule application style Appelt-shortest
//
// Revision 1.15  2001/05/16 19:03:45  valyt
//
// Added a new option for jape in order to allow the use of the shortest match in appelt rules
//
// Revision 1.14  2001/04/30 16:56:32  valyt
//
//
// Unification of the NAME attribute implementation.
//
// Revision 1.13  2001/04/17 18:18:06  valyt
//
// events for jape & applications
//
// Revision 1.12  2001/03/06 20:11:14  valyt
//
// <b><em><strong>DOCUMENTATION</></></> for most of the GUI classes.
//
// Cleaned up some obsolete classes
//
// Revision 1.11  2001/01/21 20:51:31  valyt
// Added the DocumentEditor class and the necessary changes to the gate API
//
// Revision 1.10  2000/11/08 16:35:03  hamish
// formatting
//
// Revision 1.9  2000/10/26 10:45:30  oana
// Modified in the code style
//
// Revision 1.8  2000/10/18 13:26:47  hamish
// Factory.createResource now working, with a utility method that uses reflection (via java.beans.Introspector) to set properties on a resource from the
//     parameter list fed to createResource.
//     resources may now have both an interface and a class; they are indexed by interface type; the class is used to instantiate them
//     moved createResource from CR to Factory
//     removed Transients; use Factory instead
//
// Revision 1.7  2000/10/16 16:44:34  oana
// Changed the comment of DEBUG variable
//
// Revision 1.6  2000/10/10 15:36:36  oana
// Changed System.out in Out and System.err in Err;
// Added the DEBUG variable seted on false;
// Added in the header the licence;
//
// Revision 1.5  2000/07/12 14:19:19  valyt
// Testing CVS
//
// Revision 1.4  2000/07/04 14:37:39  valyt
// Added some support for Jape-ing in a different annotations et than the default one;
// Changed the L&F for the JapeGUI to the System default
//
// Revision 1.3  2000/07/03 21:00:59  valyt
// Added StatusBar and ProgressBar support for tokenisation & Jape transduction
// (it looks great :) )
//
// Revision 1.2  2000/04/14 18:02:46  valyt
// Added some gate.fsm classes
// added some accessor function in old jape classes
//
// Revision 1.1  2000/02/23 13:46:08  hamish
// added
//
// Revision 1.1.1.1  1999/02/03 16:23:02  hamish
// added gate2
//
// Revision 1.10  1998/11/01 21:21:39  hamish
// use Java arrays in transduction where possible
//
// Revision 1.9  1998/10/06 16:14:59  hamish
// phase ordering prob fixed; made phases an array
//
// Revision 1.8  1998/10/01 16:06:33  hamish
// new appelt transduction style, replacing buggy version
//
// Revision 1.7  1998/09/26 09:19:17  hamish
// added cloning of PE macros
//
// Revision 1.6  1998/09/18 13:35:59  hamish
// made Transducer a class
//
// Revision 1.5  1998/08/19 20:21:40  hamish
// new RHS assignment expression stuff added
//
// Revision 1.4  1998/08/12 15:39:39  hamish
// added padding toString methods
//
// Revision 1.3  1998/08/10 14:16:37  hamish
// fixed consumeblock bug and added batch.java
//
// Revision 1.2  1998/08/07 16:39:17  hamish
// parses, transduces. time for a break
//
// Revision 1.1  1998/08/07 16:18:45  hamish
// parser pretty complete, with backend link done
