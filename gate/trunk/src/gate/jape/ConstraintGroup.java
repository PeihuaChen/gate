/*
 *  ConstraintGroup.java - transducer class
 *
 *  Copyright (c) 2000-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June1991.
 *
 *  A copy of this licence is included in the distribution in the file
 *  licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 * 
 *  Hamish Cunningham, 24/07/98
 *
 *  $Id$
 */


package gate.jape;

import java.util.*;
import com.objectspace.jgl.*;
import gate.annotation.*;
import gate.util.*;
import gate.*;


/**
  * A sequence of conjunctions of PatternElement that form a
  * disjunction.
  */
public class ConstraintGroup
extends PatternElement implements JapeConstants, java.io.Serializable
{
  /**
    *  This field is "final static" because it brings in
    *  the advantage of dead code elimination
    *  When DEBUG is set on false the code that it guardes will be eliminated
    *  by the compiler. This will spead up the progam a little bit.
    */
  private static final boolean DEBUG = false;

  /** Anonymous constructor. */
  public ConstraintGroup() {
    patternElementDisjunction1 = new Array();
    currentConjunction = new Array();
    patternElementDisjunction1.add(currentConjunction);
  } // Anonymous constructor

  /** Need cloning for processing of macro references. See comments on
    * <CODE>PatternElement.clone()</CODE>
    */
  public Object clone() {
    ConstraintGroup newPE = (ConstraintGroup) super.clone();

    // created by createDisjunction
    newPE.currentConjunction = null;

    newPE.patternElementDisjunction1 = new Array();
    // for each (conjunction) member of the pattern element discjunction
    for(
      ArrayIterator disjunction = patternElementDisjunction1.begin();
      ! disjunction.atEnd();
      disjunction.advance()
    ) {

      newPE.createDisjunction();
      // for each pattern element making up this conjunction
      for(
        ArrayIterator conjunction = ((Array) disjunction.get()).begin();
        ! conjunction.atEnd();
        conjunction.advance()
      ) {
        PatternElement pat = (PatternElement) conjunction.get();

        newPE.addPatternElement((PatternElement) pat.clone());
      } // for each element of the conjunction
    } // for each conjunction (element of the disjunction)

    return newPE;
  } // clone

  /** An array of arrays that represent PatternElement conjunctions
    * during parsing of the .jape. Each conjunction is
    * considered as being disjunct with the next. (I.e. they are
    * or'd, in the same way as expressions around "||" in C and
    * Java.) Set during parsing; replaced by finish().
    */
  private Array patternElementDisjunction1;

  /** The pattern element disjunction for transduction - Java arrays. */
  private PatternElement[][] patternElementDisjunction2;

  /** An array of PatternElements making up a conjunction. It is a member of
    * patternElementDisjunction. This is the one we're adding to
    * at present. Used during parsing, not matching.
    */
  private Array currentConjunction;

  /** Make a new disjunction at this point. */
  public void createDisjunction() {
    currentConjunction = new Array();
    patternElementDisjunction1.add(currentConjunction);
  } // createDisjunction

  /** Add an element to the current conjunction. */
  public void addPatternElement(PatternElement pe) {
    currentConjunction.add(pe);
  } // addPatternElement

  /** Get an list of CPEs that we contain. */
  protected ArrayIterator getCPEs() {
    Array cpes = new Array();

    // for each (conjunction) member of the pattern element discjunction
    for(
      ArrayIterator disjunction = patternElementDisjunction1.begin();
      ! disjunction.atEnd();
      disjunction.advance()
    ) {
      // for each pattern element making up this conjunction
      for(
        ArrayIterator conjunction = ((Array) disjunction.get()).begin();
        ! conjunction.atEnd();
        conjunction.advance()
      ) {
        PatternElement pat = (PatternElement) conjunction.get();

        ArrayIterator i = null;
        if(pat instanceof ComplexPatternElement) {
          cpes.add(pat);
          i = ((ComplexPatternElement) pat).getCPEs();
        }
        else if(pat instanceof ConstraintGroup)
          i = ((ConstraintGroup) pat).getCPEs();

        if(i != null)
          for( ; ! i.atEnd(); i.advance())
            cpes.add(i.get());
      } // for each element of the conjunction
    } // for each conjunction (element of the disjunction)

    return cpes.begin();
  } // getCPEs

  /** Finish: replace dynamic data structures with Java arrays; called
    * after parsing.
    */
  public void finish() {
    int i = 0; // index into patternElementDisjunction2
    int j = 0; // index into the conjunctions (second dimension of pED2)
    patternElementDisjunction2 =
      new PatternElement[patternElementDisjunction1.size()][];

    // for each (conjunction) member of the pattern element discjunction
    for(
      ArrayIterator disjuncIter = patternElementDisjunction1.begin();
      ! disjuncIter.atEnd();
      disjuncIter.advance(), i++
    ) {
      Array conjunction = (Array) disjuncIter.get();
      patternElementDisjunction2[i] = new PatternElement[conjunction.size()];
      j = 0;
      
      // for each pattern element making up this conjunction
      for(
        ArrayIterator conjIter = conjunction.begin();
        ! conjIter.atEnd();
        conjIter.advance(), j++
      ) {
        patternElementDisjunction2[i][j] = (PatternElement) conjIter.get();
        patternElementDisjunction2[i][j].finish();
      } // loop on conjunction

    }   // loop on patternElementDisjunction1

    patternElementDisjunction1 = null;
  }     // finish

  /** Access to the annotations that have been matched by this group. */
  public AnnotationSet getMatchedAnnots() {
    AnnotationSet matchedAnnots = new AnnotationSetImpl((Document) null);
    int pEDLen = patternElementDisjunction2.length;

    // for each (conjunction) member of the pattern element disjunction
    for(int i = 0; i < pEDLen; i++) {
      int conjLen = patternElementDisjunction2[i].length;

      // for each pattern element making up this conjunction
      for(int j = 0; j < conjLen; j++) {
        PatternElement pat = patternElementDisjunction2[i][j];
        AnnotationSet patMatchedAnnots = pat.getMatchedAnnots();
        if(patMatchedAnnots != null)
          matchedAnnots.addAll(pat.getMatchedAnnots());
      } // for each element of the conjunction

    }   // for each conjunction (element of the disjunction)

    return matchedAnnots;
  } // getMatchedAnnots


  /** Clear all the annotations that have been matched by this group. */
  public void reset() {
    //Debug.pr(this, "CG reset, matchHistory.size() = " + matchHistory.size());
    int pEDLen = patternElementDisjunction2.length;

    // for each (conjunction) member of the pattern element disjunction
    for(int i = 0; i < pEDLen; i++) {
      int conjLen = patternElementDisjunction2[i].length;

      // for each pattern element making up this conjunction
      for(int j = 0; j < conjLen; j++)
        patternElementDisjunction2[i][j].reset();
    }

    super.reset(); // should be redundant: there for if PE.reset changes
  } // reset

  /** Multilevel rollback of annot caches etc. */
  public void rollback(int arity) {
    //Debug.pr(this, "CG rollback(" + arity + "), matchHistory.size() = " +
    //                   matchHistory.size());
    for(int i=0; i<arity; i++) {
      PatternElement[] conjunction = (PatternElement[]) matchHistory.pop();
      int conjLen = conjunction.length;
      for(int j = 0; j < conjLen; j++)
        conjunction[j].rollback(1);
    }
  } // rollback


  /** Does this element match the document at this position? */
  public boolean matches(
    Document doc, int position, MutableInteger newPosition
  ) {
    // if a whole conjunction matches, we set newPosition to the max of
    // rightmost advance of all the composite elements that matched, and
    // position.
    int rightmostAdvance = position;

    // when we fail the whole disjunction, we set newPosition to the max of
    // leftmost failure point, and position
    int leftmostFailurePoint = Integer.MAX_VALUE;

    // outerLoop:
    // for each conjunction
    //   for each element in the conjunction
    //     if it fails continue outerLoop;
    //   return true;
    // return false;

    // for each member of the disjunctions array
    int savedPosition = position;
    int pEDLen = patternElementDisjunction2.length;
    outerLoop:
    for(int i = 0; i < pEDLen; i++) {
      int conjLen = patternElementDisjunction2[i].length;
      position = savedPosition;
      rightmostAdvance = position;

      // for each pattern element making up this conjunction
      for(int j = 0; j < conjLen; j++) {
        PatternElement pat = patternElementDisjunction2[i][j];

        if(! pat.matches(doc, position, newPosition)) {
          // reset the last failure point to the furthest we got so far
          leftmostFailurePoint =
            Math.min(leftmostFailurePoint, newPosition.value);

          // rollback matches done in the previous elements of this conjunction
          for(int k = j - 1; k >= 0; k--)
            patternElementDisjunction2[i][k].rollback(1);

          // try the next conjunction
          continue outerLoop;
        }

        // reset our advance point to the furthest so far
        position = rightmostAdvance =
          Math.max(rightmostAdvance, newPosition.value);

      } // for each element of the conjunction

      // a whole conjunction matched: record advance and which conj succeeded
      newPosition.value = rightmostAdvance;
      matchHistory.push(patternElementDisjunction2[i]);
      //Debug.pr(this, "CG matches: pushing");
      return true;

    } // for each conjunction (element of the disjunction)

    // we reached the end of the disjunction without matching a
    // whole conjunction
    if(leftmostFailurePoint == Integer.MAX_VALUE)
      leftmostFailurePoint = position + 1;
    newPosition.value = Math.max(position + 1, leftmostFailurePoint);
    return false; // annot caches have been rolled back already in inner loop
  } // matches


  /** Create a string representation of the object. */
  public String toString() { return toString(""); }

  /** Create a string representation of the object. */
  public String toString(String pad) {
    String newline = Strings.getNl();

    StringBuffer buf =
      new StringBuffer(pad + "CG: disjunction(" + newline);
    String newPad = Strings.addPadding(pad, INDENT_PADDING);

    boolean firstTime = true;

    if(patternElementDisjunction1 != null) { // before finish()
      // for each (conjunction) member of the pattern element discjunction
      for(
        ArrayIterator disjunction = patternElementDisjunction1.begin();
        ! disjunction.atEnd();
        disjunction.advance()
      ) {
        if(firstTime) firstTime = false;
        else buf.append(newline + pad + "|" + newline);

        // for each pattern element making up this conjunction
        for(
          ArrayIterator conjunction = ((Array) disjunction.get()).begin();
          ! conjunction.atEnd();
          conjunction.advance()
        ) {
          buf.append(
            ((PatternElement) conjunction.get()).toString(newPad) + newline
          );
        } // for each element of the conjunction
      } // for each conjunction (element of the disjunction)

    } else { // after finish
      int pEDLen = patternElementDisjunction2.length; 
      if(firstTime) firstTime = false;
      else buf.append(newline + pad + "|" + newline);
      
      for(int i = 0; i < pEDLen; i++) {
        int conjLen = patternElementDisjunction2[i].length;
        // for each pattern element making up this conjunction
        for(int j = 0; j < conjLen; j++)
          buf.append(
            patternElementDisjunction2[i][j].toString(newPad) + newline
          );
      }
    }

    buf.append(pad + ") CG." + newline);

    return buf.toString();
  } // toString


//needed by FSM
  public PatternElement[][] getPatternElementDisjunction(){
    return patternElementDisjunction2;
  }
  
} // class ConstraintGroup


// $Log$
// Revision 1.3  2000/10/10 15:36:35  oana
// Changed System.out in Out and System.err in Err;
// Added the DEBUG variable seted on false;
// Added in the header the licence;
//
// Revision 1.2  2000/04/14 18:02:46  valyt
// Added some gate.fsm classes
// added some accessor function in old jape classes
//
// Revision 1.1  2000/02/23 13:46:06  hamish
// added
//
// Revision 1.1.1.1  1999/02/03 16:23:01  hamish
// added gate2
//
// Revision 1.17  1998/11/24 16:18:29  hamish
// fixed toString for calls after finish
//
// Revision 1.16  1998/11/01 21:21:36  hamish
// use Java arrays in transduction where possible
//
// Revision 1.15  1998/11/01 14:55:54  hamish
// fixed lFP setting in matches
//
// Revision 1.14  1998/10/30 14:06:45  hamish
// added getTransducer
//
// Revision 1.13  1998/10/29 12:07:49  hamish
// toString change
//
// Revision 1.12  1998/10/06 16:16:10  hamish
// negation percolation during constrain add; position advance when none at end
//
// Revision 1.11  1998/10/01 16:06:30  hamish
// new appelt transduction style, replacing buggy version
//
// Revision 1.10  1998/09/26 09:19:16  hamish
// added cloning of PE macros
//
// Revision 1.9  1998/09/17 16:48:31  hamish
// added macro defs and macro refs on LHS
//
// Revision 1.8  1998/08/12 19:05:43  hamish
// fixed multi-part CG bug; set reset to real reset and fixed multi-doc bug
//
// Revision 1.7  1998/08/12 15:39:35  hamish
// added padding toString methods
//
// Revision 1.6  1998/08/05 21:58:06  hamish
// backend works on simple test
//
// Revision 1.5  1998/08/03 19:51:20  hamish
// rollback added
//
// Revision 1.4  1998/07/31 13:12:16  hamish
// done RHS stuff, not tested
//
// Revision 1.3  1998/07/30 11:05:16  hamish
// more jape
//
// Revision 1.2  1998/07/29 11:06:56  hamish
// first compiling version
//
// Revision 1.1.1.1  1998/07/28 16:37:46  hamish
// gate2 lives