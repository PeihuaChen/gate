package gate.impl.ag;

import gate.Node;
import java.util.*;
import gate.Annotation;
import gate.FeatureSet;

public class AnnotationGraph
extends TreeSet implements gate.AnnotationGraph
{

  public AnnotationGraph() {
  }

  /** find a node by ID */
  public Node getNode(String id){
    return null;
  }//getNode(String id)

  /** The ordered set of anchors present in the nodes of the AG. */
  // unnecessary?
  public SortedSet getAnchors(){
    return null;
  }//getAnchors()

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

}//gate.impl.ag.AnnotationGraph
