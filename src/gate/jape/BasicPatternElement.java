/*
	BasicPatternElement.java - transducer class

	Hamish Cunningham, 24/07/98

	$Id$
*/


package gate.jape;

import java.util.Enumeration;
import com.objectspace.jgl.*;
import gate.annotation.*;
import gate.util.*;
import gate.*;


/**
  * A pattern element within curly braces. Has a set of Constraint,
  * which all must be satisfied at whatever position the element is being
  * matched at.
  */
public class BasicPatternElement
extends PatternElement implements JapeConstants, java.io.Serializable
{
  /** Debug flag. */
  static private final boolean debug = false;

  /** A set of Constraint. Used during parsing. */
  private Array constraints1;

  /** A set of Constraint. Used during matching. */
  private Constraint[] constraints2;

  /** A map of constraint annot type to constraint. Used during parsing. */
  private HashMap constraintsMap;

  /** Cache of the last position we failed at (-1 when none). */
  private int lastFailurePoint = -1;

  /** The position of the next available annotation of the type required
    * by the first constraint.
    */
  //private MutableInteger nextAvailable = new MutableInteger();

  /** The set of annotations we have matched. */
  private AnnotationSet matchedAnnots;

  /** Access to the annotations that have been matched. */
  public AnnotationSet getMatchedAnnots() { return matchedAnnots; }

  /** Construction. */
  public BasicPatternElement() {
    constraintsMap = new HashMap();
    constraints1 = new Array();
    lastFailurePoint = -1;
    //nextAvailable = new MutableInteger();
    matchedAnnots = new AnnotationSetImpl((Document) null);
  } // construction

  /** Need cloning for processing of macro references. See comments on
    * <CODE>PatternElement.clone()</CODE>
    */
  public Object clone() {
    BasicPatternElement newPE = (BasicPatternElement) super.clone();
    newPE.constraintsMap = (HashMap) constraintsMap.clone();
    newPE.constraints1 = new Array();
    int consLen = constraints1.size();
    for(int i = 0; i < consLen; i++)
      newPE.constraints1.add(
        ((Constraint) constraints1.at(i)).clone()
      );
    newPE.matchedAnnots = new AnnotationSetImpl((Document) null);
    newPE.matchedAnnots.addAll(matchedAnnots);
    return newPE;
  } // clone

  /** Add a constraint. Ensures that only one constraint of any given
    * annotation type exists.
    */
  public void addConstraint(Constraint newConstraint) {
    /* if the constraint is already mapped, put it's attributes on the
     * existing constraint, else add it
     */
    String annotType = newConstraint.getAnnotType();
    Constraint existingConstraint = (Constraint) constraintsMap.get(annotType);
    if(existingConstraint == null) {
      constraintsMap.add(annotType, newConstraint);
      constraints1.add(newConstraint);
    }
    else {
      FeatureMap newAttrs = newConstraint.getAttributeSeq();
      FeatureMap existingAttrs =
        existingConstraint.getAttributeSeq();
				existingAttrs.putAll(newAttrs);
      if(newConstraint.isNegated())
        existingConstraint.negate();
    }
  } // addConstraint


  /** Finish: replace dynamic data structures with Java arrays; called
    * after parsing.
    */
  public void finish() {
    int j=0;
    constraints2 = new Constraint[constraints1.size()];
    for(ArrayIterator i=constraints1.begin(); !i.atEnd(); i.advance()) {
      constraints2[j] = (Constraint) i.get();
      constraints2[j++].finish();
		}
    constraints1 = null;
  } // finish

  /** Reset: clear last failure point and matched annotations list. */
  public void reset() {
    super.reset();
    lastFailurePoint = -1;
    //nextAvailable.value = -1;
    matchedAnnots = new AnnotationSetImpl((Document) null);
  } // reset

  /** Multilevel rollback of the annotation cache. */
  public void rollback(int arity) {
    //Debug.pr(this, "BPE rollback(" + arity + "), matchHistory.size() = " +
    //          matchHistory.size());
    //Debug.nl(this);

    for(int i=0; i<arity; i++) {
      int previousSize = ((Integer) matchHistory.pop()).intValue();
      //for(int j = matchedAnnots.size() - 1; j >= previousSize; j--)
      //  matchedAnnots.removeNth(j);
      removeN(matchedAnnots, matchedAnnots.size() - previousSize);
    }
  } // rollback

  /** Does this element match the document at this position? */
  public boolean matches (
    Document doc, int position, MutableInteger newPosition
  ) {
    final int startingCacheSize = matchedAnnots.size();
    //Debug.pr(this, "BPE.matches: trying at position " + position);
    //Debug.nl(this);
    int rightmostEnd = -1;
    int end = doc.getContent().size().intValue();
    MutableInteger nextAvailable = new MutableInteger();
    int nextAvailOfFirstConstraint = -1;

    for(int len = constraints2.length, i = 0; i < len; i++) {
      Constraint constraint = constraints2[i];
      String annotType = constraint.getAnnotType();
      JdmAttribute[] constraintAttrs = constraint.getAttributeArray();
      MutableBoolean moreToTry = new MutableBoolean();

      if(debug) {
        System.out.println(
          "BPE.matches: selectAnn on lFP = " + lastFailurePoint +
          "; max(pos,lfp) = " + Math.max(position, lastFailurePoint) +
          "; annotType = " + annotType + "; attrs = " +
          constraintAttrs.toString() + Strings.getNl()
        );
        for(int j=0; j<constraintAttrs.length; j++)
          System.out.println(
            "BPE.matches attr: " + constraintAttrs[j].toString()
          );
      }
      FeatureMap features = new SimpleFeatureMapImpl();
      for(int j = constraintAttrs.length - 1; j >= 0; j--)
        features.put(constraintAttrs[j].getName(), constraintAttrs[j].getValue());
      AnnotationSet match = doc.getAnnotations().get(
        // this loses "April 2" on the frozen tests:
        // Math.max(nextAvailable.value, Math.max(position, lastFailurePoint)),
        annotType,
        features,
        new Long(Math.max(position, lastFailurePoint))  /*,
        nextAvailable,
        moreToTry */
      );
      if(debug) System.out.println(
        "BPE.matches: selectAnn returned " + match + ".... moreToTry = " +
        moreToTry.value + "    nextAvailable = " + nextAvailable.value
      );

      // store first constraint's next available
      if(nextAvailOfFirstConstraint == -1)
        nextAvailOfFirstConstraint = nextAvailable.value;

      // if there are no more annotations of this type, then we can
      // say that we failed this BPE and that we tried the whole document
      if(! moreToTry.value) {
        if(match != null)
          throw(new RuntimeException("BPE: no more annots but found one!"));
        lastFailurePoint = end;
        newPosition.value = end;
      }

      // selectNextAnnotation ensures that annotations matched will
      // all start >= position. we also need to ensure that second and
      // subsequent matches start <= to the rightmost end. otherwise
      // BPEs can match non-contiguous annotations, which is not the
      // intent. so we record the rightmostEnd, and reject annotations
      // whose leftmostStart is > this value.
      int matchEnd = -1;
      if(match != null) {
        matchEnd = match.lastNode().getOffset().intValue();
        if(rightmostEnd == -1) { // first time through
          rightmostEnd = matchEnd;
        }
        else if(match.firstNode().getOffset().intValue() >= rightmostEnd) { // reject
          lastFailurePoint = matchEnd;
          match = null;
        }
        else { // this one is ok; reset rightmostEnd
          if(rightmostEnd < matchEnd)
            rightmostEnd = matchEnd;
        }
      } // match != null

      // negation
      if(constraint.isNegated()) {
        if(match == null) {
          //Debug.pr(
          //  this, "BPE.matches: negating failed constraint" + Debug.getNl()
          //);
          continue;
        }
        else {
          //Debug.pr(
          //  this, "BPE.matches: negating successful constraint, match = " +
          //  match.toString() + Debug.getNl()
          //);
          lastFailurePoint = matchEnd;
          match = null;
        }
      } // constraint is negated

      if(match == null) { // clean up
        //Debug.pr(this, "BPE.matches: selectNextAnnotation returned null");
        //Debug.nl(this);

        newPosition.value = Math.max(position + 1, nextAvailOfFirstConstraint);
        lastFailurePoint = nextAvailable.value;

        // we clear cached annots added this time, not all: maybe we were
        // applied under *, for example, and failure doesn't mean we should
        // purge the whole cache
        //for(int j = matchedAnnots.size() - 1; j >= startingCacheSize; j--)
        //  matchedAnnots.removeNth(j);
        removeN(matchedAnnots, matchedAnnots.size() - startingCacheSize);
        //Debug.pr(
        //  this, "BPE.matches: false, newPosition.value(" +
        //  newPosition.value + ")" + Debug.getNl()
        //);
        return false;
      } else {
        //Debug.pr(this,"BPE.matches: match= "+match.toString()+Debug.getNl());
        matchedAnnots.addAll(match);
        newPosition.value = Math.max(newPosition.value, matchEnd);
      }

    } // for each constraint

    // success: store the size of the cache before this match
    matchHistory.push(new Integer(startingCacheSize));
    //Debug.pr(this, "BPE.matches: returning true" + Debug.getNl());
    // under negation we may not have advanced...
    if(newPosition.value == position)
      newPosition.value++;
    return true;
  } // matches

  /** Create a string representation of the object. */
  public String toString() { return toString(""); }

  /** Create a string representation of the object. */
  public String toString(String pad) {
    String newline = Strings.getNl();
    String newPad = Strings.addPadding(pad, INDENT_PADDING);

    StringBuffer buf = new StringBuffer(pad +
      "BPE: lastFailurePoint(" + lastFailurePoint + "); constraints("
    );

    // constraints
    if(constraints1 != null) {
      for(int len = constraints1.size(), i = 0; i < len; i++)
        buf.append(
          newline + ((Constraint) constraints1.at(i)).toString(newPad)
        );
    } else {
      for(int len = constraints2.length, i = 0; i < len; i++)
        buf.append(newline + constraints2[i].toString(newPad));
    }

    // matched annots
    buf.append(
      newline + pad + "matchedAnnots: " + matchedAnnots +
      newline + pad + ") BPE."
    );

    return buf.toString();
  } // toString

  /** Utility method to replace JDM annotation set code. Removes the
    * n most recently added elements of as. Relies on ID numbers of
    * annotations (probably a BAD idea).
    */
  private void removeN(AnnotationSet as, int n) {
    // get the next annotation ID number
    // count down from it, removing annots until we did n
    
  } // removeN(as, n)

} // class BasicPatternElement


// $Log$
// Revision 1.1  2000/02/23 13:46:03  hamish
// added
//
// Revision 1.1.1.1  1999/02/03 16:23:01  hamish
// added gate2
//
// Revision 1.26  1998/11/24 16:19:00  hamish
// now feeds back next available of first constraint as newPosition
//
// Revision 1.25  1998/11/13 13:17:15  hamish
// merged in the doc length bug fix
//
// Revision 1.24  1998/11/12 17:47:27  kalina
// A bug fixed, wasn't restoring the document length
//
// Revision 1.23  1998/11/05 13:36:29  kalina
// moved to use array of JdmAttributes for selectNextAnnotation
// instead of a sequence
//
// Revision 1.22  1998/11/04 20:10:12  hamish
// set lastFailurePoint from nextAvailable.value
//
// Revision 1.21  1998/11/01 23:18:43  hamish
// use new instead of clear on containers
//
// Revision 1.20  1998/11/01 22:35:55  kalina
// attribute seq hashtable mod
//
// Revision 1.19  1998/11/01 21:21:34  hamish
// use Java arrays in transduction where possible
//
// Revision 1.18  1998/11/01 15:17:30  hamish
// initial mod for feeding back next available; not currently used though
//
// Revision 1.17  1998/10/30 14:06:42  hamish
// added getTransducer
//
// Revision 1.16  1998/10/29 12:00:34  hamish
// debug code out
//
// Revision 1.15  1998/10/06 16:16:08  hamish
// negation percolation during constrain add; position advance when none at end
//
// Revision 1.14  1998/10/01 16:06:27  hamish
// new appelt transduction style, replacing buggy version
//
// Revision 1.13  1998/09/26 09:19:13  hamish
// added cloning of PE macros
//
// Revision 1.12  1998/09/24 17:41:17  hamish
// fixed multiple constraints on adjacent tokens bug
//
// Revision 1.11  1998/09/23 12:48:00  hamish
// negation added; noncontiguous BPEs disallowed
//
// Revision 1.10  1998/09/15 16:36:11  hamish
// debug printing bug
//
// Revision 1.9  1998/08/13 14:17:31  hamish
// workaround class cast bug in jdm seq append
//
// Revision 1.8  1998/08/12 19:05:40  hamish
// fixed multi-part CG bug; set reset to real reset and fixed multi-doc bug
//
// Revision 1.7  1998/08/12 15:39:30  hamish
// added padding toString methods
//
// Revision 1.6  1998/08/07 16:18:44  hamish
// parser pretty complete, with backend link done
//
// Revision 1.5  1998/08/05 21:58:03  hamish
// backend works on simple test
//
// Revision 1.4  1998/08/03 19:51:18  hamish
// rollback added
//
// Revision 1.3  1998/07/30 11:05:13  mks
// more jape
//
// Revision 1.2  1998/07/29 11:06:52  hamish
// first compiling version
//
// Revision 1.1.1.1  1998/07/28 16:37:46  hamish
// gate2 lives
