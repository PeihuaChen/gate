/*
 *  BasicPatternElement.java - transducer class
 *
 *  Copyright (c) 1998-2007, The University of Sheffield.
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

import gate.*;
import gate.annotation.AnnotationSetImpl;
import gate.util.*;


/**
  * A pattern element within curly braces. Has a set of Constraint,
  * which all must be satisfied at whatever position the element is being
  * matched at.
  */
public class BasicPatternElement
extends PatternElement implements JapeConstants, java.io.Serializable
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** A set of Constraint. Used during parsing. */
  private ArrayList<Constraint> constraints1;

  /** A set of Constraint. Used during matching. */
  private Constraint[] constraints2;

  /** A map of constraint annot type to constraint. Used during parsing. */
  private HashMap<Object, Constraint> constraintsMap;

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
    constraintsMap = new HashMap<Object, Constraint>();
    constraints1 = new ArrayList<Constraint>();
    lastFailurePoint = -1;
    //nextAvailable = new MutableInteger();
    matchedAnnots = new AnnotationSetImpl((Document) null);
  } // construction

  /** Need cloning for processing of macro references. See comments on
    * <CODE>PatternElement.clone()</CODE>
    */
  public Object clone() {
    BasicPatternElement newPE = (BasicPatternElement) super.clone();
    newPE.constraintsMap = (HashMap<Object, Constraint>) constraintsMap.clone();
    newPE.constraints1 = new ArrayList<Constraint>();
    int consLen = constraints1.size();
    for(int i = 0; i < consLen; i++)
      newPE.constraints1.add(
        (Constraint)constraints1.get(i).clone()
      );
//    newPE.matchedAnnots = new AnnotationSetImpl((Document) null);
//    newPE.matchedAnnots.addAll(matchedAnnots);
    return newPE;
  } // clone

  /** Add a constraint. Ensures that only one constraint of any given
    * annotation type and negation state exists.
    */
  public void addConstraint(Constraint newConstraint) {
    /* if a constraint with the same negation state as this constraint is
     * already mapped, put it's attributes on the existing constraint, else
     * add it
     */
    String annotType = newConstraint.getAnnotType();
    Pair typeNegKey = new Pair(annotType, newConstraint.isNegated());

    Constraint existingConstraint = constraintsMap.get(typeNegKey);
    if(existingConstraint == null) {
      constraintsMap.put(typeNegKey, newConstraint);
      constraints1.add(newConstraint);
    }
    else {
      existingConstraint.addAttributes(newConstraint.getAttributeSeq());
    }
  } // addConstraint


  /**
   * Indicates whether this constraint deals with only one type of annotation or
   * multiple types.
   */
  public boolean isMultiType() {
      return constraints2 != null ? constraints2.length > 1 :
             constraints1 != null ? constraints1.size() > 1 :
             false;
  }

  /** Finish: replace dynamic data structures with Java arrays; called
    * after parsing.
    */
  public void finish() {
    int j=0;
    constraints2 = new Constraint[constraints1.size()];
    for(Constraint c : constraints1 ) {
      constraints2[j] = c;
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
      matchedAnnots.removeAll((AnnotationSet) matchHistory.pop());
    }
  } // rollback

  /** Does this element match the document at this position? */
  public boolean matches (
    Document doc, int position, MutableInteger newPosition
  ) {
    @SuppressWarnings("unused")
    final int startingCacheSize = matchedAnnots.size();
    AnnotationSet addedAnnots = new AnnotationSetImpl((Document) null);

    //Debug.pr(this, "BPE.matches: trying at position " + position);
    //Debug.nl(this);
    int rightmostEnd = -1;
    int end = doc.getContent().size().intValue();
    MutableInteger nextAvailable = new MutableInteger();
    int nextAvailOfFirstConstraint = -1;

    for(int len = constraints2.length, i = 0; i < len; i++) {
      Constraint constraint = constraints2[i];
      MutableBoolean moreToTry = new MutableBoolean();

      if(DEBUG) {
        Out.println(
          "BPE.matches: selectAnn on lFP = " + lastFailurePoint +
          "; max(pos,lfp) = " + Math.max(position, lastFailurePoint) +
          "; constraint = " + constraint.getDisplayString("") + Strings.getNl()
        );
      }

      // ERS change begin:
      // I changed this just to compile, but as far as I can tell this
      // code is not used any more.
      //
      // Do not screen on the feature set or type from the Constraint
      // when selecting annotations that may match it. The Constraint
      // will do that. This allows the use of predicates other than equals
      // and for testing for non-existence of a given annotation type
      //
      // Of course, the previous code was incorrect as well because of a
      // change 8 years ago - it would never retrieve the attributes from
      // the constraint.
      AnnotationSet match = null;
      AnnotationSet potentialMatches = doc.getAnnotations().get(
        // this loses "April 2" on the frozen tests:
        // Math.max(nextAvailable.value, Math.max(position, lastFailurePoint)),
        //annotType,
        //(FeatureMap)null,
        new Long(Math.max(position, lastFailurePoint))  /*,
        nextAvailable,
        moreToTry */
      );
      List<Annotation> matchList = constraint.matches(potentialMatches, null, doc);
      if (!matchList.isEmpty()) {
        match = doc.getAnnotations().get("");
        for(Annotation annot : matchList) {
          match.add(annot);
        }
      }
      //ERS change end

      if(DEBUG) Out.println(
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
        else if(match.firstNode().getOffset().intValue() >= rightmostEnd) {
          // reject
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
          // Debug.pr(
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
        matchedAnnots.removeAll(addedAnnots);

        //Debug.pr(
        //  this, "BPE.matches: false, newPosition.value(" +
        //  newPosition.value + ")" + Debug.getNl()
        //);
        return false;
      } else {

        //Debug.pr(this,"BPE.matches: match= "+match.toString()+Debug.getNl());
        matchedAnnots.addAll(match);
        addedAnnots.addAll(match);
        newPosition.value = Math.max(newPosition.value, matchEnd);
      }

    } // for each constraint

    // success: store the annots added this time
    matchHistory.push(addedAnnots);

    //Debug.pr(this, "BPE.matches: returning true" + Debug.getNl());
    // under negation we may not have advanced...
    if(newPosition.value == position)
      newPosition.value++;

    return true;
  } // matches

  /** Create a string representation of the object. */
  public String toString() {
    StringBuffer result = new StringBuffer("{");
    Constraint[] constraints = getConstraints();
    for(int i = 0; i<constraints.length; i++){
      result.append(constraints[i].shortDesc() + ",");
    }
    result.setCharAt(result.length() -1, '}');
    return result.toString();
  }

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
          newline + constraints1.get(i).getDisplayString(newPad)
        );
    } else {
      for(int len = constraints2.length, i = 0; i < len; i++)
        buf.append(newline + constraints2[i].getDisplayString(newPad));
    }

    // matched annots
    buf.append(
      newline + pad + "matchedAnnots: " + matchedAnnots +
      newline + pad + ") BPE."
    );

    return buf.toString();
  } // toString

  /**
    * Returns a short description.
    */
  public String shortDesc() {
    String res = "";
    if(constraints1 != null) {
      for(int len = constraints1.size(), i = 0; i < len; i++)
        res += constraints1.get(i).toString();
    } else {
      for(int len = constraints2.length, i = 0; i < len; i++)
        res += constraints2[i].shortDesc();
    }
    return res;
  }

  /**
   * Get the current list of unfinished Constraint objects. This
   * can only be used before the finish() method is used.
   * @return the array list of constraint objects. Will be null after
   * the finish() method has been used.
   */
  public ArrayList<Constraint> getUnfinishedConstraints() {
    return constraints1;
  }

  /**
   * Get the finished Constraint objects. Can only be used after the
   * finish() method has been used.
   * @return an array of constraint objects. Will be null before the
   * finish() method has been used.
   */
  public Constraint[] getConstraints(){
    return constraints2;
  }
} // class BasicPatternElement

