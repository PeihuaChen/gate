/*
	MultiPhaseTransducer.java - transducer class

	Hamish Cunningham, 24/07/98

	$Id$
*/


package gate.jape;

import java.util.*;
import com.objectspace.jgl.*;
import gate.annotation.*;
import gate.gui.*;
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
  /** Construction from name. */
  public MultiPhaseTransducer(String name) {
    this();
    setName(name);
  } // constr from name

  /** Anonymous construction */
  public MultiPhaseTransducer() {
    phases = new Array();
  } // anon construction

  /** Set the name. */
  public void setName(String name) { this.name = name; }

  /** The SinglePhaseTransducers that make up this one.
    * Keyed by their phase names.
    */
  private Array phases;

  /** Add phase. */
  public void addPhase(String name, Transducer phase) {
    //Debug.pr(this, "MPT: adding " + name + Debug.getNl());
    phases.add(phase);
  } // addPhase

  /** Change the phase order to the one specified in a list of names. */
  public void orderPhases(String[] phaseNames) {
    System.err.println("oops: MPT.orderPhases not done yet :-(");
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
    for(ArrayIterator i = phases.begin(); ! i.atEnd(); i.advance())
      ((Transducer) i.get()).finish();
  } // finish

  /** Transduce the document by running each phase in turn. */
  public void transduce(Document doc) throws JapeException {
    ProgressListener pListener = new ProgressListener(){
      public void processFinished(){
        donePhases ++;
        if(donePhases == phasesCnt) fireProcessFinishedEvent();
      }
      public void progressChanged(int i){
        int value = (donePhases * 100 + i)/phasesCnt;
        fireProgressChangedEvent(value);
      }
      int phasesCnt = phases.size();
      int donePhases = 0;
    };
    StatusListener sListener = new StatusListener(){
      public void statusChanged(String text){
        fireStatusChangedEvent(text);
      }
    };

    for(ArrayIterator i = phases.begin(); ! i.atEnd(); i.advance()) {
      Transducer t = (Transducer) i.get();
      try {
        fireStatusChangedEvent("Transducing " + doc.getSourceURL().getFile() +
                               " (Phase: " + t.getName() + ")...");
        t.addProcessProgressListener(pListener);
        t.addStatusListener(sListener);
        t.transduce(doc);
        t.removeProcessProgressListener(pListener);
        t.removeStatusListener(sListener);
        fireStatusChangedEvent("");
      } catch(JapeException e) {
        String errorMessage = new String(
          "Error transducing document " + doc.getSourceURL() +
          ", phase " + t.getName() + Strings.getNl() + e.getMessage()
        );
        throw(new JapeException(errorMessage));
      }
    }

    cleanUp();
  } // transduce

  /** Ask each phase to clean up (delete action class files, for e.g.). */
  public void cleanUp() {

    for(ArrayIterator i = phases.begin(); ! i.atEnd(); i.advance())
      ((Transducer) i.get()).cleanUp();

  } // cleanUp

  /** Create a string representation of the object. */
  public String toString() { return toString(""); }

  /** Create a string representation of the object. */
  public String toString(String pad) {
    String newline = Strings.getNl();

    StringBuffer buf = new StringBuffer(
      pad + "MPT: name(" + name + "); phases(" + newline + pad
    );

    for(ArrayIterator i = phases.begin(); ! i.atEnd(); i.advance())
      buf.append(
        ((Transducer) i.get()).toString(
            Strings.addPadding(pad, INDENT_PADDING)
        ) + " "
      );

    buf.append(newline + pad + ")." + newline);

    return buf.toString();
  } // toString
//needed by FSM
  public Array getPhases(){ return phases; }
} // class MultiPhaseTransducer



// $Log$
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

