package gate.impl.ag;


import java.util.*;
import gate.FeatureSet;



public class AnnotationGraph implements gate.AnnotationGraph
{

  public AnnotationGraph(gate.Document document, String id) {
    this.document=document;
    this.id=id;
    nodeSet=new gate.util.TreeMap();
  }
  /** find a node by ID */
  public gate.Node getNode(String id){
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
    java.util.HashSet sameType=(java.util.HashSet) annotsByType.get(type);
    if (sameType!=null){
      gate.impl.ag.AnnotationGraph result=new gate.impl.ag.AnnotationGraph(document,gate.util.Tools.gensym("AG"));
      java.util.Iterator annotsIter=sameType.iterator();
      gate.Annotation currentAnn;
      while(annotsIter.hasNext()){
        currentAnn=(gate.Annotation)annotsIter.next();
        result.addNode(currentAnn.getStartNode());
        result.addNode(currentAnn.getEndNode());
        result.addAnnotation(currentAnn);
      }//while
      return result;
    }
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
  public gate.Node putNodeAt(String id,double offset)throws gate.util.InvalidOffsetException{
    if (offset > document.getLength())
      throw(new gate.util.InvalidOffsetException("Offset out of bounds: "
                                                  +offset+">"
                                                  +document.getLength()));
    if (document instanceof gate.TextualDocument){
      if((((offset*10)%10)>0) )
        throw(new gate.util.InvalidOffsetException("Offset is not an integer value: "
                                                    +offset
                                                    +". Textual documents only accept integer offsets!"));
    };
    gate.Node newNode=new gate.impl.ag.Node(id, new Double(offset));
    if(nodeSet.containsKey(newNode.getOffset()))
      throw(new gate.util.InvalidOffsetException("There is already a node at the given offset:"
                                                    +offset));
    nodeSet.put(newNode.getOffset(),newNode);
    return newNode;
  };

  void addNode(gate.Node node){
    nodeSet.put(node.getOffset(),node);
  }

  public String getId(){
    return id;
  }

  public gate.Annotation newAnnotation(String id,gate.Node start, gate.Node end, String type, String equivalenceClass){
    Annotation annot=new Annotation(id,start,end,type,equivalenceClass);
    start.addStartAnnotation(annot);
    end.addEndAnnotation(annot);
    annotations.put(annot.getId(),annot);
    java.util.HashSet sameType=(java.util.HashSet)annotsByType.get(annot.getType());
    if(sameType==null){
      sameType=new java.util.HashSet();
      sameType.add(annot);
      annotsByType.put(annot.getType(),sameType);
    }else{
      sameType.add(annot);
    }
    return annot;
  }

  public gate.Annotation newAnnotation(String id,long start, long end, String type, String equivalenceClass){
    gate.Node startN=null;
    gate.Node endN=null;
    try{
      Object[] pair=nodeSet.getClosestMatch(new Double(start));
      if(pair[0]==pair[1])startN=(gate.Node)pair[0];
      else startN=putNodeAt(gate.util.Tools.gensym("Node"),start);

      pair=nodeSet.getClosestMatch(new Double(end));
      if(pair[0]==pair[1])endN=(gate.Node)pair[0];
      else endN=putNodeAt(gate.util.Tools.gensym("Node"),end);

    }catch(gate.util.InvalidOffsetException e){
      e.printStackTrace(System.err);
    }

    return newAnnotation(id,startN,endN,type,equivalenceClass);
  }

  void addAnnotation(gate.Annotation annot){
    annotations.put(annot.getId(),annot);
    java.util.HashSet sameType=(java.util.HashSet)annotsByType.get(annot.getType());
    if(sameType==null){
      sameType=new java.util.HashSet();
      sameType.add(annot);
      annotsByType.put(annot.getType(),sameType);
    }else{
      sameType.add(annot);
    }
  }

  public String toString(){
    String result="==============================\n";
    result+=      "====   Annotation Graph   ====\n";
    result+=      "==============================\n";
    result+="Id:"+id+"\n";
    result+="Document:"+document.getId()+"\n";
    result+="Nodes:\n";
    java.util.Iterator nodesIter=nodeSet.values().iterator();
    gate.Node currentNode;
    while(nodesIter.hasNext()){
      currentNode=(gate.Node)nodesIter.next();
      result+="Id:"+currentNode.getId()+
              " ,Offset:"+currentNode.getOffset()+";\n";
    }//while
    result+="\nAnnotations:\n";
    java.util.Iterator annIter=annotations.values().iterator();
    gate.Annotation currentAnn;
    gate.Node start,end;
    while(annIter.hasNext()){
      currentAnn=(gate.Annotation)annIter.next();
      start=currentAnn.getStartNode();
      end=currentAnn.getEndNode();
      result+="Id:"+currentAnn.getId()+" ,"+
              "From: "+start.getId()+" to: "+end.getId()+";\n";
      if(document instanceof gate.TextualDocument){
          result+="Covered text: \""+((gate.TextualDocument)document).getContentOf(currentAnn)+"\"\n";
      }
    }//while
    return result+"==============================";
  }
  private gate.util.TreeMap nodeSet;
  private String id;
  private gate.Document document;
  private java.util.Hashtable annotations=new java.util.Hashtable();
  private java.util.Hashtable annotsByType=new java.util.Hashtable();
}//gate.impl.ag.AnnotationGraph
