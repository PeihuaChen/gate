/*
*	FSMInstance.java
*
*	Valentin Tablan, 05/May/2000
*
*	$Id$
*/
package gate.fsm;

import gate.*;
import gate.annotation.AnnotationSetImpl;

import java.util.*;

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
    this.startNode = startNode;
    this.AGPosition = AGPosition;
    this.bindings = bindings;
    length = AGPosition.getOffset().longValue() -
             startNode.getOffset().longValue();
    fileIndex = FSMPosition.getFileIndex();
  }

  public FSM getSupportGraph(){ return supportGraph; }
  public State getFSMPosition(){return FSMPosition; }
  public void setFSMPosition(State newFSMPos){
    FSMPosition = newFSMPos;
    fileIndex = FSMPosition.getFileIndex();
  }

  public int getFileIndex(){ return fileIndex;}
  public Node getStartAGPosition(){ return startNode; }
  public Node getAGPosition(){ return AGPosition; }
  public void setAGPosition(Node node){ AGPosition = node;}
  public HashMap getBindings(){ return bindings; }

//  public void setBindings(HashMap newBindings) { bindings = newBindings; }
  public long getLength(){ return length; }
  public int hashCode() { return (int)length; }


  public Object clone() {
  //do a classic clone except for bindings which need to be cloned themselves
    try{
      FSMInstance clone = (FSMInstance)super.clone();
      clone.bindings = (HashMap)bindings.clone();
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
      if(length < other.getLength()) return -1;
      else if(length > other.getLength()) return 1;
      else if(fileIndex <= other.fileIndex) return 1;
      else return -1;
    }else throw new ClassCastException(
                    "Attempt to compare a FSMInstance object to an object " +
                    "of type " + obj.getClass()+"!");
  }

  public String toString(){
    String res = "";
    res +=   "FSM position :" + FSMPosition.getIndex();
    res += "\nFirst matched ANN at:" + startNode.getId() +
           "\nLast matched ANN at :" + AGPosition.getId();
    res += "\nBindings     :" + bindings;
    return res;
  }
  private FSM supportGraph;
  private State FSMPosition;
  private Node AGPosition, startNode;
  //maps from java.lang.String to gate.AnnotationSet.
  //needs to be HashMap instead of simply Map in order to cloneable
  private HashMap bindings;
  private long length = 0;
  /**
  * The index in the definition file of the rule from which the AGPosition
  * state was generated.
  */
  private int fileIndex;
}