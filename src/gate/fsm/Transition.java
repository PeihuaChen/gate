/*
* Valentin Tablan, 11/04/2000
*/

package gate.fsm;

import gate.jape.*;

import java.util.*;

/**
* This class implements a Finite State Machine transition.
* A transition is owned by a gate.fsm.State object and contains set of
* restrictions and a reference to the next state that will be accessed after
* consuming a set of input symbols according to the restrictions.
* A transition can also hold information about the label that should be bound
* to the symbols (annotations) consumed during the state transition.
*/
public class Transition {

  /**
  * Default constructor. Creates a new transition with a new unique index.
  * This constructor should be called by all other constructors.
  */
  public Transition(){
    myIndex = Transition.index++;
  }

  /**
  * Creates a new transition using the given set of constraints and target
  * state.
  *@param constraints the set on constraints associated to this transition
  *@param state the target state of this transition
  */
  public Transition(BasicPatternElement constraints, State state) {
    this();
    this.constraints = constraints;
    target = state;
    labels = new LinkedList();
  }

  /**
  * Ctreates a new transition from a set of constraints, a target state and a
  * list of labels to be bound with the recognized input symbols
  * (aka annotations).
  */
  public Transition(BasicPatternElement constraints, State state,
                    LinkedList labels) {
    this();
    this.constraints = constraints;
    target = state;
    this.labels = labels;
  }

  /**
  * Gets the target state of this transition
  *@return an object of type gate.fsm.State
  */
  public State getTarget(){ return target; }

  /**
  * Gets the constraints associated to this transition
  */
  public BasicPatternElement getConstraints(){ return constraints; }

  /**
  * Returns a textual desciption of this transition.
  *@return a String
  */
  public String toString(){
    String res = "If: " + constraints + " then ->: " + target.getIndex();
    return res;
  }

  /**
  * Returns a shorter description that toSting().
  * Actually, it returns the unique index in String form.
  */
  public String shortDesc(){
    String res = "" + myIndex;
    return res;
  }

  /**
  *  Returns the list of bindings associated to this transition
  */
  protected LinkedList getLabels(){ return labels; }

  /**
  * The constraints on this transition.
  */
  private BasicPatternElement constraints;

  /**
  * The state this transition leads to
  */
  private State target;

  /**
  * A list with all the labels associated to the annotations recognized by this
  * transition.
  * We need to use the actual object and not the interface (java.util.List)
  * because we need this object to be cloneable
  */
  private LinkedList labels;

  private int myIndex;
  private static int index = 0;
}
