/*
* Valentin Tablan, 11/04/2000
*/

package gate.fsm;

import gate.jape.*;

import java.util.*;

public class Transition {

  public Transition(BasicPatternElement constraints, State state) {
    this.constraints = constraints;
    this.state = state;
    labels = new LinkedList();
  }

  public Transition(BasicPatternElement constraints, State state,
                    LinkedList labels) {
    this.constraints = constraints;
    this.state = state;
    this.labels = labels;
  }

  /**
  * The constraints on this transition.
  */
  private BasicPatternElement constraints;
  /**
  * The state this transition leads to
  */
  private State state;
  /**
  * A list with all the labels associated to the annotations recognized by this
  * transition.
  * We need to use the actual object and not the interface (java.util.List)
  * because we need this object to be cloneable
  */
  private LinkedList labels;
}