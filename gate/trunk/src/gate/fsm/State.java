/*
* Valentin Tablan, 11/04/2000
*/

package gate.fsm;

import java.util.*;
import gate.jape.*;

public class State {

  public State() {
  }
  //creates a final state that has an associated action(right hand side)
  public State(RightHandSide rhs){
    this.action = rhs;
  }
  /**
  * Reports if this state is a final one.
  * Note: A state has an associated action if and only if it is final.
  */
  public boolean isFinal(){ return action != null; }

  public Set getTransitions(){ return transitions; }

  public void addTransition(Transition transition){
    transitions.add(transition);
  }

  /**
  * A set of objects of type gata.fsm.Transition.
  */

  private Set transitions;
  /**
  * The right hand side associated to the rule for which this state recognizes
  * the lhs.
  */
  private RightHandSide action = null;
}