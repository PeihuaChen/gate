package gate.creole.tokeniser;

import java.util.*;
  /** A state of the finite state machine that is the actual tokeniser
    */
  class FSMState{
    public FSMState(){
    }

    public FSMState(DefaultTokeniser owner){
      myIndex = index++;
      owner.fsmStates.add(this);
    }

    void put(UnicodeType type, FSMState state){
      if(null == type) put(128,state);
      else put(type.type , state);
    }

    Set nextSet(UnicodeType type){
      if(null == type) return transitionFunction[256];
      else return transitionFunction[type.type +128];
    }

    Set nextSet(byte type){
      return transitionFunction[type + 128];
    }

    void put(int index, FSMState state){
      if(null == transitionFunction[index +128])
        transitionFunction[index +128] = new HashSet();
      transitionFunction[index +128].add(state);
    }

    void setRhs(String rhs){this.rhs = rhs;}
    String getRhs(){return rhs;}
    boolean isFinal() {return (null != rhs);}
    int getIndex(){return myIndex;}

    String getEdgesGML(){
      String res = "";
      Set nextSet;
      Iterator nextSetIter;
      FSMState nextState;
      for(int i = 0; i< transitionFunction.length; i++){
        nextSet = transitionFunction[i];
        if(null != nextSet){
          nextSetIter = nextSet.iterator();
          while(nextSetIter.hasNext()){
            nextState = (FSMState)nextSetIter.next();
            res += "edge [ source " + myIndex +
            " target " + nextState.getIndex() +
            " label \"";
            if(i -128 > Byte.MAX_VALUE) res += "[]";
            else res += DefaultTokeniser.typesMnemonics[i];
            res += "\" ]\n";
          }//while(nextSetIter.hasNext())
        }
      };
/*
      Iterator transIter = transitions.iterator();
      BasicPatternElement bpe;
      while(transIter.hasNext()){
        Transition currentTrans = (Transition)transIter.next();
        res += "edge [ source " + myIndex +
               " target " + currentTrans.getTarget().getIndex() +
               " label \"" + currentTrans.shortDesc() + ":";
               bpe = currentTrans.getConstraints();
               if(bpe == null) res += "null";
               else res += bpe.shortDesc();
               res += " :" + currentTrans.getBindings() +
               "\" ]\n";
      }
*/
      return res;
    }
    Set[] transitionFunction = new Set[257];
    String rhs;
    int myIndex;
    static int index;
    static{
      index = 0;
    }
  }//class FSMState

