/* 
	PatternElement.java - transducer class

	Hamish Cunningham, 24/07/98

	$Id$
*/


package gate.jape;

import java.util.*;
import gate.annotation.*;
import gate.util.*;
import gate.*;


/**
  * Superclass of the various types of pattern element, and of
  * ConstraintGroup. Inherits from Matcher, providing matches and reset.
  * Provides access to the annotations that are cached by subclasses, and
  * multilevel rollback of those caches. Stores the match history.
  */
abstract public class PatternElement implements Cloneable, Matcher,
		      JapeConstants, java.io.Serializable
{
  /** Match history stack, for use in rollback. In BasicPatternElements
    * the objects on the stack are Integers giving the number of annots that
    * were cached at that point in the history. In ComplexPatternElements
    * the objects are Integers giving the number of times the component
    * ConstraintGroup was successfully matched. In ConstraintGroups the
    * elements are arrays representing conjunctions of PatternElement that
    * succeeded at that point in the history.
    */
  protected Stack matchHistory;

  /** Anonymous construction. */
  public PatternElement() {
    matchHistory = new Stack();
  } // Anonymous constructor.

  /** Cloning for processing of macro references. Note that it doesn't
    * really clone the match history, just set it to a new Stack. This is
    * because a) JGL doesn't have real clone methods and b) we don't
    * actually need it anywhere but during parsing the .jape, where there
    * is no match history yet.
    */
  public Object clone() {
    try {
      PatternElement newPE = (PatternElement) super.clone();
      newPE.matchHistory = new Stack();
      return newPE;
    } catch(CloneNotSupportedException e) {
      throw(new InternalError(e.toString()));
    }
  } // clone

  /** Access to the annotations that have been matched. */
  abstract public AnnotationSet getMatchedAnnots();

  /** Multilevel rollback of annotation caches. */
  abstract public void rollback(int arity);

  /** Reset: clear annotation caches etc. Most of the behaviour of
    * this method is the responsibility of subclasses.
    */
  public void reset() {
    matchHistory = new Stack();
  } // reset

  /** Create a string representation of the object with padding. */
  abstract public String toString(String pad);

} // class PatternElement


// $Log$
// Revision 1.1  2000/02/23 13:46:09  hamish
// added
//
// Revision 1.1.1.1  1999/02/03 16:23:02  hamish
// added gate2
//
// Revision 1.8  1998/11/03 19:06:49  hamish
// java stack, not jgl stack for matchHistory
//
// Revision 1.7  1998/11/01 23:18:44  hamish
// use new instead of clear on containers
//
// Revision 1.6  1998/09/26 09:19:18  hamish
// added cloning of PE macros
//
// Revision 1.5  1998/08/12 15:39:41  hamish
// added padding toString methods
//
// Revision 1.4  1998/08/03 19:51:24  hamish
// rollback added
//
// Revision 1.3  1998/07/30 11:05:22  hamish
// more jape
//
// Revision 1.2  1998/07/29 11:07:06  hamish
// first compiling version
//
// Revision 1.1.1.1  1998/07/28 16:37:46  hamish
// gate2 lives
