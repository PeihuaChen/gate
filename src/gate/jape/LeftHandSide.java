/*
	LeftHandSide.java - transducer class

	Hamish Cunningham, 24/07/98

	$Id$
*/


package gate.jape;

import java.util.Enumeration;
import java.io.Serializable;
import com.objectspace.jgl.*;
import gate.annotation.*;
import gate.util.*;
import gate.*;


/**
  * The LHS of a CPSL rule. The pattern part. Has a ConstraintGroup and
  * binding information that associates labels with ComplexPatternElements.
  * Provides the Matcher interface.
  */
public class LeftHandSide implements Matcher, JapeConstants, Serializable
{
  /** The constraint group making up this LHS. */
  private ConstraintGroup constraintGroup;

  /** Mapping of binding names to ComplexPatternElements */
  private HashMap bindingTable;

  /** Flag for whether our last match was successful or not. */
  private boolean hasMatched = false;

  /** Construction from a ConstraintGroup */
  public LeftHandSide(ConstraintGroup constraintGroup) {
    this.constraintGroup = constraintGroup;
    bindingTable = new HashMap();
    hasMatched = false;
  } // construction from ConstraintGroup

  /** Add a binding record. */
  public void addBinding(
    String bindingName,
    ComplexPatternElement binding,
    HashSet bindingNameSet,
    boolean macroRef
  ) throws JapeException {
    if(bindingTable.get(bindingName) != null)
      throw new JapeException(
        "LeftHandSide.addBinding: " + bindingName + " already bound"
      );
    bindingTable.add(bindingName, binding);
 	  bindingNameSet.add(bindingName);

    // if it was a macro ref, we need to recursively set up bindings
    // in any CPEs that this one contains
    if(macroRef) {
      for(ArrayIterator i = binding.getCPEs(); ! i.atEnd(); i.advance()) {
        binding = (ComplexPatternElement) i.get();
        bindingName = binding.getBindingName();
        if(bindingName == null) // not all CPEs have binding names
          continue;
        if(bindingTable.get(bindingName) != null)
          throw new JapeException(
            "LeftHandSide.addBinding: " + bindingName + " already bound"
          );
        bindingTable.add(bindingName, binding);
 	      bindingNameSet.add(bindingName);
      } // for each binding
    } // macroRef

  } // addBinding

  /** Finish: replace dynamic data structures with Java arrays; called
    * after parsing.
    */
  public void finish() {
    constraintGroup.finish();
  } // finish

  /** Get annotations via a binding name. */
  public AnnotationSet getBoundAnnots(String bindingName) {
    ComplexPatternElement pat =
      (ComplexPatternElement) bindingTable.get(bindingName);
    if(pat == null) return null;
    return pat.getMatchedAnnots();
  } // getBoundAnnots

  /** For debugging only. 
    * Return a set of all annotations matched by the LHS during the
    * last call to matches. (May be null.)
    */
  AnnotationSet getMatchedAnnots() {
    return constraintGroup.getMatchedAnnots();
  } // getMatchedAnnots

  /** Clear the matched annotations cached in pattern elements. */
  public void reset() {
    constraintGroup.reset();
    hasMatched = false;
  } // reset

  /** Was the last match successful? */
  public boolean hasMatched() { return hasMatched; }

  /** Does the LHS match the document at this position? */
  public boolean matches(
    Document doc, int position, MutableInteger newPosition
  ) {
     boolean status = constraintGroup.matches(doc, position, newPosition);
     //Debug.pr(this, "LHS: status(" + status + "); this: " + this.toString());
     
     if(! status) { // purge caches of matched annotations
       constraintGroup.reset();
       hasMatched = false;
     } else {
       hasMatched = true;
     }
     return status;
  }  // matches

  /** Create a string representation of the object. */
  public String toString() { return toString(""); }

  /** Create a string representation of the object. */
  public String toString(String pad) {
    String newline = Strings.getNl();
    String newPad = Strings.addPadding(pad, INDENT_PADDING);

    StringBuffer buf = new StringBuffer(pad +
      "LHS: hasMatched(" + hasMatched + "); constraintGroup(" + newline +
      constraintGroup.toString(newPad) + newline + pad +
      "); bindingTable(" + newline + pad
    );

    for(HashMapIterator i = bindingTable.begin(); ! i.atEnd(); i.advance()) {
      String bName = ((String) i.key());
      ComplexPatternElement cpe = ((ComplexPatternElement) i.value());
      buf.append(
        pad + "bT.bn(" + bName + "), cpe.bn(" + cpe.getBindingName() + ")"
      );
    }

    buf.append(newline + pad + ") LHS." + newline);

    return buf.toString();
  } // toString

  /** Get the constraint group */
  public ConstraintGroup getConstraintGroup(){
    return constraintGroup;
  }
  
} // class LeftHandSide


// $Log$
// Revision 1.3  2000/05/02 16:54:26  hamish
// comment
//
// Revision 1.2  2000/04/14 18:02:46  valyt
// Added some gate.fsm classes
// added some accessor function in old jape classes
//
// Revision 1.1  2000/02/23 13:46:08  hamish
// added
//
// Revision 1.1.1.1  1999/02/03 16:23:01  hamish
// added gate2
//
// Revision 1.14  1998/11/01 21:21:37  hamish
// use Java arrays in transduction where possible
//
// Revision 1.13  1998/10/30 15:31:07  kalina
// Made small changes to make compile under 1.2 and 1.1.x
//
// Revision 1.12  1998/10/01 16:06:32  hamish
// new appelt transduction style, replacing buggy version
//
// Revision 1.11  1998/09/21 16:19:49  hamish
// cope with CPEs with no binding
//
// Revision 1.10  1998/09/17 16:48:32  hamish
// added macro defs and macro refs on LHS
//
// Revision 1.9  1998/08/19 20:21:39  hamish
// new RHS assignment expression stuff added
//
// Revision 1.8  1998/08/18 12:43:07  hamish
// fixed SPT bug, not advancing newPosition
//
// Revision 1.7  1998/08/12 19:05:45  hamish
// fixed multi-part CG bug; set reset to real reset and fixed multi-doc bug
//
// Revision 1.6  1998/08/12 15:39:37  hamish
// added padding toString methods
//
// Revision 1.5  1998/08/03 19:51:22  hamish
// rollback added
//
// Revision 1.4  1998/07/31 13:12:20  mks
// done RHS stuff, not tested
//
// Revision 1.3  1998/07/30 11:05:19  mks
// more jape
//
// Revision 1.2  1998/07/29 11:06:59  hamish
// first compiling version
//
// Revision 1.1.1.1  1998/07/28 16:37:46  hamish
// gate2 lives
