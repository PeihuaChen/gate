package gate.impl.ag;

import gate.Node;
import java.util.*;
import gate.Annotation;
import gate.FeatureSet;
import calsavara.strucs.AVLTree;


public class AnnotationGraph
extends TreeSet implements gate.AnnotationGraph
{

  public AnnotationGraph(gate.Document document) {
    this.document=document;
    nodeSet=new AVLTree();
  }
  /** find a node by ID */
  public Node getNode(String id){
    return null;
  }//getNode(String id)

  /** Greatest lower bound on an annotation: the greatest anchor in the AG
    * such that there is a node with this anchor which structurally precedes
    * the start node of annotation a. */
  public Long greatestLowerBound(Annotation a){
    return null;
  }//greatestLowerBound(Annotation a)

  /** Least upper bound on an annotation: the smallest anchor in the AG
    * such that there is a node with this anchor is structurally preceded
    * by the end node of annotation a. */
  public Long leastUpperBound(Annotation a){
    return null;
  }//leastUpperBound(Annotation a)

  /** The set of annotations overlapping a */
  public gate.AnnotationGraph getOverlappingAnnotations(Annotation a){
    return null;
  }//getOverlappingAnnotations(Annotation a)

  /** The set of annotations included by a */
  public gate.AnnotationGraph getIncludedAnnotations(Annotation a){
    return null;
  }//getIncludedAnnotations(Annotation a)

  /** Get annotations by type */
  public gate.AnnotationGraph getAnnotations(String type){
    return null;
  }//getAnnotations(String type)

  /** Get annotations by type and features */
  public gate.AnnotationGraph getAnnotations(String type, FeatureSet features){
    return null;
  }//getAnnotations(String type, FeatureSet features)

  /** Get annotations by type and equivalence class */
  public gate.AnnotationGraph getAnnotations(String type, String equivalenceClass){
    return null;
  }//getAnnotations(String type, String equivalenceClass)

  /** Get annotations by type and position. This is the set of annotations of
    * a particular type which share the smallest leastUpperBound that is >=
    * offset */
  public gate.AnnotationGraph getAnnotations(String type, Long offset){
    return null;
  }//getAnnotations(String type, Long offset)

  /** Get annotations by type and features */
  public gate.AnnotationGraph getAnnotations(String type,
                                             FeatureSet features,
                                             Long offset){
    return null;
  }//getAnnotations(String type,FeatureSet features,Long offset)

  /** Get annotations by type and equivalence class */
  public gate.AnnotationGraph getAnnotations(String type,
                                             String equivalenceClass,
                                             Long offset){
    return null;
  }//getAnnotations(String type,String equivalenceClass,Long offset)

  /**Creates a new node with the offset offset
  @param offset the offset in document where the node will point*/
  public void putNodeAt(int id,double offset)throws gate.util.InvalidOffsetException{
    if (offset > document.getLength())
      throw(new gate.util.InvalidOffsetException("Offset out of bounds: "
                                                  +offset+">"
                                                  +document.getLength()));
    if (document instanceof gate.TextualDocument){
      if(! (((offset*10)%10)>0) )
        throw(new gate.util.InvalidOffsetException("Offset is not an integer value: "
                                                    +offset
                                                    +". Textual documents only accept integer offsets!"));
    };
    gate.Node newNode=new gate.impl.ag.Node(id, new Double(offset));
    if(nodeSet.containsKey(newNode.getOffset()))
      throw(new gate.util.InvalidOffsetException("There is already a node at the given offset:"
                                                    +offset));
    nodeSet.put(newNode.getOffset(),newNode);
  };

  private AVLTree nodeSet;
  private gate.Document document;
}//gate.impl.ag.AnnotationGraph
