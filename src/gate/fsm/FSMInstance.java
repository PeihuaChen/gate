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

  /** Creates a new FSMInstance object.
    *@param supportGraph the transition graph of the FSM
    *@param FSMPosition the state this instance will be in
    *@param startNode the node in the AnnotationGraph where this FSM instance
    *started the matching
    *@ AGPosition the node in the AnnotationGraph up to which this FSM Instance
    *advanced during the matching.
    *@param bindings a HashMap that maps from labels (objects of type String) to
    *sets of annotations (objects of type AnnotationSet). This map stores all
    *the bindings that took place during the matching process.
    *This FSMInstance started the matching on an AnnotationSet from "startNode"
    *and advanced to "AGPosition"; during this process it traversed the path in
    *the transition graph "supportGraph" from the initial state to "FSMPosition"
    *and made the bindings stored in "bindings".
    */
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

  /** Returns the FSM transition graph that backs this FSM instance
    *@return an FSM object
    */
  public FSM getSupportGraph(){ return supportGraph; }

  /** Returns the position in the support graph for this FSM instance
    *@return an object of type State
    */
  public State getFSMPosition(){return FSMPosition; }

  /** Sets the position in the support transition graph for this FSM instance
    * Convenience method for when the state is not known at construction time.
    */
  public void setFSMPosition(State newFSMPos){
    FSMPosition = newFSMPos;
    fileIndex = FSMPosition.getFileIndex();
  }

  /** Returns the index in the Jape definition file of the rule that caused
    * the generation of the FSM state this instance is in.
    * This value is correct if and only if this FSM instance is in a final
    * state of the FSM transition graph.
    *@return an int value.
    */
  public int getFileIndex(){ return fileIndex;}

  /** Returns the node in the AnnotationGraph from which this FSM instance
    * started the matching process.
    *@return a gate.Node object
    */
  public Node getStartAGPosition(){ return startNode; }

  /** Returns the node up to which this FSM instance advanced in the
    *Annotation graph during the matching process.
    *@return a gate.Node object
    */
  public Node getAGPosition(){ return AGPosition; }

  /** Sets the current position in the AnnotationGraph.
    *Convenience method for cases when this value is not known at construction
    *time.
    *@param node a position in the AnnotationGraph
    */
  public void setAGPosition(Node node){
    AGPosition = node;
    length = AGPosition.getOffset().longValue() -
             startNode.getOffset().longValue();
  }

  /** Gets the map representing the bindings that took place during the matching
    *process this FSM instance performed.
    *@return a HashMap object
    */
  public HashMap getBindings(){ return bindings; }

  /** Returns the length of the parsed region in the document under scrutiny.
    * More precisely this is the distnace between the Node in the annotation
    *graph where the matching started and the current position.
    *@return a long value
    */
  public long getLength(){ return length; }

  /** Overrides the hashCode method from Object so this obejcts can be stored in
    * hash maps and hash sets.
    */
  public int hashCode() { return (int)length; }

  /** Returns a clone of this object.
    * The cloning is done bitwise except for the bindings that are cloned by
    *themselves
    *@return an Object value that is actually a FSMInstance object
    */
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

/*
  public Object clone() {
  //do a classic clone except for bindings which need to be cloned themselves
//System.out.println("Clone!");
    FSMInstance clone = FSMInstance.getNewInstance(this.supportGraph,
                                                   this.FSMPosition,
                                                   this.startNode,
                                                   this.AGPosition,
                                                   null);
    clone.bindings = (HashMap)(bindings.clone());
    return (FSMInstance)clone;
  }
*/
  /** Implementation of the compareTo method required by the Comparable
    *interface. The comparison is based on the size of the matched region and
    *the index in the definition file of the rule associated to this FSM
    *instance (which needs to be in a final state)
    * The order imposed by this method is the priority needed in case of a
    *multiple match.
    */
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

  /** Returns a textual representation of this FSM instance.
    */
  public String toString(){
    String res = "";
    res +=   "FSM position :" + FSMPosition.getIndex();
    res += "\nFirst matched ANN at:" + startNode.getId() +
           "\nLast matched ANN at :" + AGPosition.getId();
    res += "\nBindings     :" + bindings;
    return res;
  }

  /** The FSM for which this FSMInstance is an instance of. */
  private FSM supportGraph;

  /** The current state of this FSMInstance */
  private State FSMPosition;

  /** The place (Node) in the AnnotationGraph where the matching started*/
  private Node AGPosition, startNode;

  /** A map from java.lang.String to gate.AnnotationSet describing all the
    *bindings that took place during matching.
    *needs to be HashMap instead of simply Map in order to cloneable
    */
  private HashMap bindings;

  /** The size of the matched region in the Annotation Graph*/
  private long length = 0;
  
  /**
  * The index in the definition file of the rule from which the AGPosition
  * state was generated.
  */
  private int fileIndex;

  /** Static method that provides new FSM instances. This method handles some
    * basic object pooling in order to reuse the FSMInstance objects.
    * This is considered to be a good idea because during jape transducing
    * a large number of FSMIntances are needed for short periods.
    */
  public static FSMInstance getNewInstance(FSM supportGraph, State FSMPosition,
                                           Node startNode, Node AGPosition,
                                           HashMap bindings){
    FSMInstance res;
    if(myInstances.isEmpty()) res = new FSMInstance(supportGraph, FSMPosition,
                                                    startNode, AGPosition,
                                                    bindings);
    else{
      res = (FSMInstance)myInstances.removeFirst();
      res.supportGraph = supportGraph;
      res.FSMPosition = FSMPosition;
      res.startNode = startNode;
      res.AGPosition = AGPosition;
      res.bindings = bindings;
    }
    return res;
  }

  /** Static method used to return a FSMInstance that is not needed anymore
    */
  public static void returnInstance(FSMInstance ins){
    myInstances.addFirst(ins);
  }

  /** Release all the FSMInstances that are not currently in use */
  public static void clearInstances(){
    myInstances = new LinkedList();
  }

  //The list of existing instances of type FSMInstance
  private static LinkedList myInstances;
  static{
    myInstances = new LinkedList();
  }
}