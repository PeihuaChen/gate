/*
* Valentin Tablan, 29/03/2000
*/

package gate.fsm;

import java.util.*;

import gate.jape.*;

/**
*
*/
public class FSM implements JapeConstants{

  public FSM(State initialState){
    this.initialState = initialState;
  }

  public FSM(SinglePhaseTransducer spt){
    initialState = new State();
    Enumeration rulesEnum = spt.getRules().elements();
    Rule currentRule;
    while(rulesEnum.hasMoreElements()){
      currentRule = (Rule) rulesEnum.nextElement();
      FSM ruleFSM = new FSM(currentRule);
      initialState.addTransition(new Transition(null,
                                                ruleFSM.getInitialState()));
    }
  }

  public FSM(Rule rule){
    initialState = new State();
    LeftHandSide lhs = rule.getLHS();
    PatternElement[][] constraints =
                       lhs.getConstraintGroup().getPatternElementDisjunction();
    //the rectangular array constraints is a disjunction of sequences of
    //constraints = [[PE]:[PE]...[PE] ||
    //               [PE]:[PE]...[PE] ||
    //               ...
    //               [PE]:[PE]...[PE] ]

    //The current and the next state for the current ROW.
    State currentRowState, nextRowState;
    State finalState = new State(rule.getRHS());
    PatternElement currentPattern;
    for(int i = 0; i <= constraints.length; i++){
      //for each row we have to create a sequence of states that will accept the
      //sequence of annotations described by the restrictions on that row.
      //The final state of such a sequence will always be a finale state which
      //will have associated the right hand side of the rule used for this
      //constructor.

      //For each row we will start from the initial state.
      currentRowState = initialState;
      for(int j=0; j <= constraints[i].length; j++){
        //parse the sequence of constraints:
        //For each basic pattern element add a new state and link it to the
        //currentRowState.
        //The case of kleene operators has to be considered!
        currentPattern = constraints[i][j];
        if(currentPattern instanceof BasicPatternElement){
          //the easy case
          nextRowState = new State();
          currentRowState.addTransition(
            new Transition((BasicPatternElement)currentPattern, nextRowState));
          currentRowState = nextRowState;
        }else if(currentPattern instanceof ComplexPatternElement){
          //the current pattern is a complex pattern element
          //..it will probaly be converted into a sequence of states itself.
          currentRowState =  convertComplexPE(
                              currentRowState,
                              (ComplexPatternElement)currentPattern,
                              new LinkedList());
        }else{
          //we got an unknown kind of pattern
          throw new RuntimeException("Strange looking pattern:"+currentPattern);
        }

      }//for j
      //link the end of the current row to the final state using
      //an empty transition.
      currentRowState.addTransition(new Transition(null,finalState));
    }//for i
  }

  public State getInitialState(){
    return initialState;
  }

  /**
  * Gets a state to start from and a complex pattern element.
  * Parses the complex pattern element and creates all the necessary states
  * and transitions for accepting annotations described by the given PE.
  *@param state the state to start from
  *@param cpe the pattern to be recognized
  *@param label the bindings name for all the annotation accepted along the way
  *this is actually a listy of Strings. It is necessary to use a list becuase of
  *the reccursive definition of ComplexPatternElement.
  *@return the final state reached after accepting a sequence of annotations
  *as described in the pattern
  */
  private State convertComplexPE(State startState, ComplexPatternElement cpe,
                                 LinkedList labels){
    //create a copy
    LinkedList newBindings = (LinkedList)labels.clone();
    newBindings.add(cpe.getBindingName ());
    PatternElement[][] constraints =
                       cpe.getConstraintGroup().getPatternElementDisjunction();
    //the rectangular array constraints is a disjunction of sequences of
    //constraints = [[PE]:[PE]...[PE] ||
    //               [PE]:[PE]...[PE] ||
    //               ...
    //               [PE]:[PE]...[PE] ]

    //The current and the next state for the current ROW.
    State currentRowState, nextRowState, endState = new State();
    PatternElement currentPattern;
    for(int i = 0; i <= constraints.length; i++){
      //for each row we have to create a sequence of states that will accept the
      //sequence of annotations described by the restrictions on that row.
      //The final state of such a sequence will always be a finale state which
      //will have associated the right hand side of the rule used for this
      //constructor.

      //For each row we will start from the initial state.
      currentRowState = startState;
      for(int j=0; j <= constraints[i].length; j++){
        //parse the sequence of constraints:
        //For each basic pattern element add a new state and link it to the
        //currentRowState.
        //The case of kleene operators has to be considered!
        currentPattern = constraints[i][j];
        if(currentPattern instanceof BasicPatternElement){
          //the easy case
          nextRowState = new State();
          currentRowState.addTransition(
            new Transition((BasicPatternElement)currentPattern,
                            nextRowState,newBindings));
          currentRowState = nextRowState;
        }else if(currentPattern instanceof ComplexPatternElement){
          //the current pattern is a complex pattern element
          //..it will probaly be converted into a sequence of states itself.
          currentRowState =  convertComplexPE(
                              currentRowState,
                              (ComplexPatternElement)currentPattern,
                              newBindings);
        }else{
          //we got an unknown kind of pattern
          throw new RuntimeException("Strange looking pattern:"+currentPattern);
        }

      }//for j
        //link the end of the current row to the general end state using
        //an empty transition.
        currentRowState.addTransition(new Transition(null,endState));
    }//for i
    //let's take care of the kleene operator
    int kleeneOp = cpe.getKleeneOp();
    switch (kleeneOp){
      case NO_KLEENE_OP:{
        break;
      }
      case KLEENE_QUERY:{
        //allow to skip everything via a null transition
        startState.addTransition(new Transition(null,endState));
        break;
      }
      case KLEENE_PLUS:{
        //allow to return to startState
        endState.addTransition(new Transition(null,startState));
        break;
      }
      case KLEENE_STAR:{
        //allow to skip everything via a null transition
        startState.addTransition(new Transition(null,endState));
        //allow to return to startState
        endState.addTransition(new Transition(null,startState));
        break;
      }
      default:{
        throw new RuntimeException("Unknown Kleene operator"+kleeneOp);
      }
    }//switch (cpe.getKleeneOp())
    return endState;
  }

  private State initialState;

}
