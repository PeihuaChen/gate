/*
*	FSMInstance.java
*
*	Valentin Tablan, 05/May/2000
*
*	$Id$
*/
package gate.fsm;

import gate.*;

import java.util.HashMap;

/**
* The objects of this class represent instances of working Finite State Machine
* during parsing a gate document (annotation set).
* In order to completely define the state a FSM is in one needs to store
* information regarding:
* -the position in the FSM transition graph
* -the position in the annotation graph
* -the set of bindings that occured up to the current state.
*   note that a set of bindings is an object of type Map that maps names
* (java.lang.String) to bags of annotations (gate.AnnotationSet)
*/
public class FSMInstance implements Comparable, Cloneable{

  public FSMInstance(FSM supportGraph, State FSMPosition,
                     Node startNode, Node AGPosition, HashMap bindings) {
    this.supportGraph = supportGraph;
    this.FSMPosition = FSMPosition;
    this.AGPosition = AGPosition;
    this.bindings = bindings;
    length = AGPosition.getOffset().longValue() -
             startNode.getOffset().longValue();
  }

  public FSM getSupportGraph(){ return supportGraph; }
  public State getFSMPosition(){return FSMPosition; }
  public Node getAGPosition(){ return AGPosition; }
  public void setAGPosition(Node node){ AGPosition = node;}
  public HashMap getBindings(){ return bindings; }

//  public void setBindings(HashMap newBindings) { bindings = newBindings; }
  public long getLength(){ return length; }
  public int hashCode() { return (int)length; }


  public Object clone() {
  //do a classic clone except for bindings which itself needs to be cloned
    try{
      FSMInstance clone = (FSMInstance)super.clone();
      clone.bindings = (HashMap)clone.bindings.clone();
      return clone;
    }catch (CloneNotSupportedException cnse){
      cnse.printStackTrace(System.err);
      return null;
    }
  }
  public int compareTo(Object obj){
    if (obj instanceof FSMInstance){
      if(obj == this) return 0;
      FSMInstance other = (FSMInstance)obj;
      if(length <= other.getLength()) return -1;
      else return 1;
    }else throw new ClassCastException(
                    "Attempt to compare a FSMInstance object to an object " +
                    "of type " + obj.getClass()+"!");
  }

  private FSM supportGraph;
  private State FSMPosition;
  private Node AGPosition;
  //maps from java.lang.String to gate.AnnotationSet.
  //needs to be HashMap instead of simply Map in order to cloneable
  private HashMap bindings;
  private long length = 0;
}