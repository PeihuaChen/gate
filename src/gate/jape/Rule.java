/*
	Rule.java - transducer class

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
  * A CPSL rule. Has an LHS, RHS and a name, and a priority.
  */
public class Rule extends Transducer
implements JapeConstants, java.io.Serializable
{
  /** Construction */
  public Rule(
    String name, int position, int priority,
    LeftHandSide lhs, RightHandSide rhs
  ) {
    this.name = name;
    this.position = position;
    this.priority = priority;
    this.lhs = lhs;
    this.rhs = rhs;
  } // Construction

  /** Debug flag. */
  static private final boolean debug = false;

  /** The LHS or pattern of the rule. */
  private LeftHandSide lhs;

  /** The RHS or action of the rule. */
  private RightHandSide rhs;

  /** The priority of the rule. */
  private int priority;

  /** Get the rule pririty. */
  public int getPriority() { return priority; }

  /** The rule's position in sequence (e.g. order in file). */
  private int position;

  /** Get the rule's position in sequence (e.g. order in file). */
  public int getPosition() { return position; }

  /** If we're pending (have matched), get the position we want to fire in,
    * else -1.
    */
  public int pending() {
    return pendingPosition;
  } // pending

  /** If we matched but didn't fire yet, this is our pending position. */
  private int pendingPosition = -1;

  /** Flag for end of document during getNextMatch. */
  private boolean weFinished = false;

  /** Have we hit the end of the document without matching? */
  public boolean finished() {
    return weFinished;
  } // finished

  /** Finish: replace dynamic data structures with Java arrays; called
    * after parsing. WARNING:
    * bad choice of names: this is not related to the weFinished
    * member or the finished method!
    */
  public void finish() {
    lhs.finish();
  } // finish

  /** If another match at or beyond <CODE>position</CODE> is possible return
    * the position we want to fire in, else -1.
    */
  public int getNextMatch(Document doc, int position, int end) {
    MutableInteger newPosition = new MutableInteger();
    newPosition.value = position;
    while(position < end) {

      if(matches(doc, position, newPosition)) {
        pendingPosition = getStartPosition();
        return pendingPosition;
      }
      position = Math.max(position + 1, newPosition.value);

    } // while position not final

    weFinished = true;
    return -1;
  } // getNextMatch

  /** Return the ending position of a match. This is the rightmost span
    * end of the matched annotations.
    */
  public int getEndPosition() {
    return lhs.getMatchedAnnots().lastNode().getOffset().intValue();
  }

  /** Return the starting position of a match. This is the leftmost span
    * start of the matched annotations.
    */
  public int getStartPosition() {
    return lhs.getMatchedAnnots().firstNode().getOffset().intValue();
  }

  /** Does this element match the document at this position? */
  public boolean matches(
    Document doc, int position, MutableInteger newPosition
  ) {
    if(debug) System.out.println("trying rule " + name + " at " + position);
    return lhs.matches(doc, position, newPosition);
  } // matches

  /** Apply the RHS of this rule (LHS must have been matched first). */
  public void transduce(Document doc, AnnotationSet annotations) throws JapeException {
    // the righthand side does the transduction, using bindings from lhs */
    if(debug) System.out.println("applying rule " + name);
//    rhs.transduce(doc);
    /*Debug.pr(
      this, "Rule.transduce: annotations after transduction: " +
      doc.selectAnnotations("Name", new FeatureMap()).toString() +
      Debug.getNl()
    );*/

    // clear the caches of matched annotations in the LHS
    reset();
    //Debug.pr(this, "LHS after reset: " + lhs.toString());

  } // transduce

  /** Clear away the results of a match. */
  public void reset() {
     if(weFinished) // no annotations cached
       weFinished = false;
     else
       lhs.reset();
     pendingPosition = -1;
  }

  /** For debugging. */
  // public String getName() { return name; }

  /** Clean up (delete action class files, for e.g.). */
  public void cleanUp() {
    rhs.cleanUp();
  } // cleanUp


  /** Create a string representation of the object. */
  public String toString() { return toString(""); }

  /** Create a string representation of the object. */
  public String toString(String pad) {
    String newline = Strings.getNl();
    String newPad = Strings.addPadding(pad, INDENT_PADDING);

    StringBuffer buf = new StringBuffer(
      pad + "Rule: name(" + name + "); position(" + position + "); priority(" +
      priority + "); pendingPosition(" + pendingPosition + "); " +
      "weFinished(" + weFinished + "); lhs(" + newline + 
      lhs.toString(newPad) + newline + pad + "); rhs(" + newline +
      rhs.toString(newPad) + newline + pad + ");"
    );

    buf.append(newline + pad + ") Rule." + newline);

    return buf.toString();
  } // toString

//needed by FSM
  public LeftHandSide getLHS(){
    return lhs;
  }
  public RightHandSide getRHS(){
    return rhs;
  }

  //StatusReporter VOID Implementation
  public void addStatusListener(StatusListener listener){}
  public void removeStatusListener(StatusListener listener){}
  //ProcessProgressReporter VOID implementation
  public void addProcessProgressListener(ProgressListener listener){}
  public void removeProcessProgressListener(ProgressListener listener){}
  //ProcessProgressReporter implementation ends here

} // class Rule


// $Log$
// Revision 1.5  2000/07/04 14:37:39  valyt
// Added some support for Jape-ing in a different annotations et than the default one;
// Changed the L&F for the JapeGUI to the System default
//
// Revision 1.4  2000/07/03 21:00:59  valyt
// Added StatusBar and ProgressBar support for tokenisation & Jape transduction
// (it looks great :) )
//
// Revision 1.3  2000/05/05 12:51:12  valyt
// Got rid of deprecation warnings
//
// Revision 1.2  2000/04/14 18:02:46  valyt
// Added some gate.fsm classes
// added some accessor function in old jape classes
//
// Revision 1.1  2000/02/23 13:46:11  hamish
// added
//
// Revision 1.1.1.1  1999/02/03 16:23:02  hamish
// added gate2
//
// Revision 1.16  1998/11/01 21:21:40  hamish
// use Java arrays in transduction where possible
//
// Revision 1.15  1998/10/30 14:06:46  hamish
// added getTransducer
//
// Revision 1.14  1998/10/29 12:16:13  hamish
// changed reset to not do lhs if weFinished  - coz
// there should be no state cached if the last try failed
//
// Revision 1.13  1998/10/01 16:06:37  hamish
// new appelt transduction style, replacing buggy version
//
// Revision 1.12  1998/09/18 13:36:00  hamish
// made Transducer a class
//
// Revision 1.11  1998/08/19 20:21:43  hamish
// new RHS assignment expression stuff added
//
// Revision 1.10  1998/08/12 19:05:48  hamish
// fixed multi-part CG bug; set reset to real reset and fixed multi-doc bug
//
// Revision 1.9  1998/08/12 15:39:43  hamish
// added padding toString methods
//
// Revision 1.8  1998/08/10 14:16:39  hamish
// fixed consumeblock bug and added batch.java
//
// Revision 1.7  1998/08/03 21:44:58  hamish
// moved parser classes to gate.jape.parser
//
// Revision 1.6  1998/08/03 19:51:27  hamish
// rollback added
//
// Revision 1.5  1998/07/31 16:50:19  mks
// RHS compilation works; it runs - and falls over...
//
// Revision 1.4  1998/07/31 13:12:27  mks
// done RHS stuff, not tested
//
// Revision 1.3  1998/07/30 11:05:25  mks
// more jape
//
// Revision 1.2  1998/07/29 11:07:11  hamish
// first compiling version
//
// Revision 1.1.1.1  1998/07/28 16:37:46  hamish
// gate2 lives
