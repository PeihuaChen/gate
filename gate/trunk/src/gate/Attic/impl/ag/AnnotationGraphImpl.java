/**AnnotationGraphImpl.java
*@author Valentin Tablan
*/
package  gate.impl.ag;




import  java.util.*;
import  gate.*;
import gate.util.*;



/**Provides an implementation for gate.AnnoatationGraph without using databases
  *To be used for testing purposes only!
  */
public class AnnotationGraphImpl
    implements AnnotationGraph
{
    /**Constructor.
      *@param document The document to wich this Annotation graph will belong;
      *@param id The id of this graph.
      */
    public AnnotationGraphImpl (Document document, Long id) {
        this.document = document;
        this.id = id;
        nodeSet = new gate.util.RBTreeMap();
    }

    /**Constructor.
      *@param document The document to wich this Annotation graph will belong;
      *The id for this graph will be assigned automatically.
      */
    public AnnotationGraphImpl(Document document){
      this(document, Tools.gensym());
    }

    /** find a node by ID */
    public Node getNode (Long id) {
        return  null;
    } //getNode(String id)



    /** Returns the set of annotations overlapping a
      *@param a The annotation for wich all the overlapping annotations will
      *be retrieved.
      */
    public AnnotationGraph getOverlappingAnnotations (gate.Annotation a) {
        return  null;
    } //getOverlappingAnnotations(Annotation a)


    /** The set of annotations included by a
      *@param a The annotation for wich all the included annotations will
      *be retrieved.
      */
    public gate.AnnotationGraph getIncludedAnnotations (gate.Annotation a) {
        return  null;
    } //getIncludedAnnotations(Annotation a)


    /** Get annotations by type
      *@param type The type of annotations sought in this query.
      */
    public gate.AnnotationGraph getAnnotations (String type) {
        java.util.HashSet sameType = (java.util.HashSet)annotsByType.get(type);

        if (sameType != null) {
            AnnotationGraphImpl result = new AnnotationGraphImpl(document, gate.util.Tools.gensym());
            java.util.Iterator annotsIter = sameType.iterator();
            gate.Annotation currentAnn;

            while (annotsIter.hasNext()) {
                currentAnn = (gate.Annotation)annotsIter.next();
                result.addNode(currentAnn.getStartNode());
                result.addNode(currentAnn.getEndNode());
                result.addAnnotation(currentAnn);
            } //while

            return  result;
        }

        return  null;
    } //getAnnotations(String type)


    /** Get annotations by type and features
      *@param type The type of annotations sought in this query;
      *@param features The set of requested features for the annotations to
      *be returned.
      */
    public gate.AnnotationGraph getAnnotations (String type, FeatureMap features) {
        return  null;
    } //getAnnotations(String type, FeatureSet features)


    /** Get annotations by type and position. This is the set of annotations of
    * a particular type which share the smallest leastUpperBound that is >=
    * offset
    *@param type type of annotations
    *@param offset the starting offset for the requested annotations
    */
    public gate.AnnotationGraph getAnnotations (String type, Long offset) {
        return  null;
    } //getAnnotations(String type, Long offset)


    /** Get annotations by type and features */
    public gate.AnnotationGraph getAnnotations (String type, FeatureMap features, Long offset) {
        return  null;
    } //getAnnotations(String type,FeatureSet features,Long offset)


    /**Creates a new node with the offset offset
  @param offset the offset in document where the node will point*/
    public gate.Node putNodeAt (Long id, long offset)
        throws gate.util.InvalidOffsetException
    {
        if (offset > document.getLength()) throw  (new gate.util.InvalidOffsetException("Offset out of bounds: " + offset + ">" + document.getLength()));

        if (document instanceof gate.TextualDocument) {
            if ((((offset*10)%10) > 0)) throw  (new gate.util.InvalidOffsetException("Offset is not an integer value: " + offset + ". Textual documents only accept integer offsets!"));
        }

        ;
        gate.Node newNode = new NodeImpl(id, new Long(offset));
        if (nodeSet.containsKey(newNode.getOffset())) throw  (new gate.util.InvalidOffsetException("There is already a node at the given offset:" + offset));
        nodeSet.put(newNode.getOffset(), newNode);
        return  newNode;
    }


    ;


    /**Returns the id of this AnnotationGraph.
    */
    public Long getId () {
        return  id;
    }

    /**Creates a new annotation in this AnnotationGraph.
      *The newly created annotation will be added to the departing annotations list
      *for the start node and to the arriving annotations list for the end node.
      *@param id the id for the new annotation;
      *@param start The start node for the new annotation;
      *@param end The end node for new annotation;
      *@param type The type of the new annotation;
      */
    public Annotation newAnnotation (Long id, gate.Node start, gate.Node end,
                                     String type) {
        AnnotationImpl annot = new AnnotationImpl(id, start, end, type);
        //start.addStartAnnotation(annot);
        //end.addEndAnnotation(annot);
        annotations.put(annot.getId(), annot);
        java.util.HashSet sameType = (java.util.HashSet)annotsByType.get(annot.getType());

        if (sameType == null) {
            sameType = new java.util.HashSet();
            sameType.add(annot);
            annotsByType.put(annot.getType(), sameType);
        }
        else {
            sameType.add(annot);
        }

        return  annot;
    }

    /**Creates a new annotation in this AnnotationGraph.
      *The newly created annotation will be added to the departing annotations list
      *for the start node and to the arriving annotations list for the end node.
      *If there are no nodes at the requested offsets, new nodes will be created.
      *@param id the id for the new annotation;
      *@param start The offset of the start node of the new annotation;
      *@param end The offset for the end node of the new annotation;
      *@param type The type of the new annotation;
      */
    public gate.Annotation newAnnotation (Long id, long start, long end,
                                          String type) {
        gate.Node startN = null;
        gate.Node endN = null;

        try {
            Object[] pair = nodeSet.getClosestMatch(new Double(start));
            if (pair[0] == pair[1]) startN = (gate.Node)pair[0];
            else startN = putNodeAt(gate.util.Tools.gensym(), start);
            pair = nodeSet.getClosestMatch(new Double(end));
            if (pair[0] == pair[1]) endN = (gate.Node)pair[0];
            else endN = putNodeAt(gate.util.Tools.gensym(), end);
        }
        catch (gate.util.InvalidOffsetException e) {
            e.printStackTrace(System.err);
        }

        return  newAnnotation(id, startN, endN, type);
    }

    /**
    *Protected access member used for AnnotationGraphs created on line (e.g. as
    *a result for a query). It is used to add a Node to the AnnotationGraph. It
    *doesn't take care of anything else than adding the node to the set of nodes.
    *(e.g. it doesn't check wether the Annotation departing/arriving from/in this
    *node are actually in this AnnotationGraph.
    *@param node the node to be added.
    */
    void addNode (Node node) {
        nodeSet.put(node.getOffset(), node);
    }//addNode


    /**
    *Protected access member used for AnnotationGraphs created on line (e.g. as
    *a result for a query). It is used to add a Annotation to the AnnotationGraph.
    *It doesn't take care of anything else than adding the annotation to the set
    *of annotations. (e.g. it doesn't check wether the nodes where this annotation
    *is anchored are actually in the node set of this graph.
    *@param annot the annotation to be added.
    */
    void addAnnotation (Annotation annot) {
        annotations.put(annot.getId(), annot);
        java.util.HashSet sameType = (java.util.HashSet)annotsByType.get(annot.getType());

        if (sameType == null) {
            sameType = new java.util.HashSet();
            sameType.add(annot);
            annotsByType.put(annot.getType(), sameType);
        }
        else {
            sameType.add(annot);
        }
    }

    /**Returns a string representation of this AnnotationGraph.
    */
    public String toString () {
        String result = "==============================\n";
        result += "====   Annotation Graph   ====\n";
        result += "==============================\n";
        result += "Id:" + id + "\n";
        result += "Document:" + document.getId() + "\n";
        result += "Nodes:\n";
        java.util.Iterator nodesIter = nodeSet.values().iterator();
        gate.Node currentNode;

        while (nodesIter.hasNext()) {
            currentNode = (gate.Node)nodesIter.next();
            result += "Id:" + currentNode.getId() +
                      " ,Offset:" + currentNode.getOffset() + ";\n";
        } //while

        result += "\nAnnotations:\n";
        java.util.Iterator annIter = annotations.values().iterator();
        gate.Annotation currentAnn;
        gate.Node start, end;

        while (annIter.hasNext()) {
            currentAnn = (gate.Annotation)annIter.next();
            start = currentAnn.getStartNode();
            end = currentAnn.getEndNode();
            result += "Id:" + currentAnn.getId() + " ," +
                      "From: " + start.getId() + " to: " + end.getId() + ";";

            if (document instanceof gate.TextualDocument) {
                result += " Covered text: \"" +
                          ((gate.TextualDocument)document).getContentOf(currentAnn) +
                          "\"\n";
            }
        }//while

        return  result + "==============================";
    }


    private gate.util.RBTreeMap nodeSet;
    private Long id;
    private gate.Document document;
    private java.util.Hashtable annotations = new java.util.Hashtable();
    private java.util.Hashtable annotsByType = new java.util.Hashtable();

} //gate.impl.ag.AnnotationGraph

