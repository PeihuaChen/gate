/*
 *	AnnotationSetImpl.java
 *
 *  Copyright (c) 2000-2001, The University of Sheffield.
 *  
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June1991.
 *  
 *  A copy of this licence is included in the distribution in the file
 *  licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *  
 *  Hamish Cunningham, 7/Feb/2000
 *
 *  Developer notes:
 *  ---
 *
 *  the addToIndex... and indexBy... methods could be refactored as I'm
 *  sure they can be made simpler
 *
 *  every set to which annotation will be added has to have positional
 *  indexing, so that we can find or create the nodes on the new annotations
 *
 *  note that annotations added anywhere other than sets that are
 *  stored on the document will not get stored anywhere...
 *
 *  nodes aren't doing anything useful now. needs some interface that allows
 *  their creation, defaulting to no coterminous duplicates, but allowing such
 *  if required
 *
 *  $Id$
 */

package gate.annotation;

import java.util.*;
import gate.util.*;

import gate.*;
import gate.corpora.*;


/** Implementation of AnnotationSet. Has a number of indices, all bar one
  * of which are null by default and are only constructed when asked
  * for. Has lots of get methods with various selection criteria; these
  * return views into the set, which are nonetheless valid sets in
  * their own right (but will not necesarily be fully indexed).
  * Has a name, which is null by default; clients of Document can
  * request named AnnotationSets if they so desire. Has a reference to the
  * Document it is attached to. Contrary to Collections convention,
  * there is no no-arg constructor, as this would leave the set in
  * an inconsistent state.
  * <P>
  * There are five indices: annotation by id, annotations by type, annotations
  * by start/end node and nodes by offset. The last three jointly provide
  * positional indexing; construction of these is triggered by
  * indexByStart/EndOffset(),
  * or by calling a get method that selects on offset. The type
  * index is triggered by indexByType(), or calling a get method that selects
  * on type. The id index is always present.
  */
public class AnnotationSetImpl
extends AbstractSet
implements AnnotationSet
{
  /**
    *  This field is "final static" because it brings in
    *  the advantage of dead code elimination
    *  When DEBUG is set on false the code that it guardes will be eliminated
    *  by the compiler. This will spead up the progam a little bit.
    */
  private static final boolean DEBUG = false;

  /** Construction from Document. */
  public AnnotationSetImpl(Document doc) {
    annotsById = new HashMap();
    this.doc = (DocumentImpl) doc;
  } // construction from document

  /** Construction from Document and name. */
  public AnnotationSetImpl(Document doc, String name) {
    this(doc);
    this.name = name;
  } // construction from document and name

  /** Construction from Collection (which must be an AnnotationSet) */
  public AnnotationSetImpl(Collection c) throws ClassCastException {
    this(((AnnotationSet) c).getDocument());
    addAll(c);
  } // construction from collection

  /** This inner class serves as the return value from the iterator()
    * method.
    */
  class AnnotationSetIterator implements Iterator {
    private Iterator iter;
    private Annotation lastNext = null;
    AnnotationSetIterator()  { iter = annotsById.values().iterator(); }
    public boolean hasNext() { return iter.hasNext(); }
    public Object next()     { return (lastNext = (Annotation) iter.next()); }
    public void remove()     {
      iter.remove();                    // this takes care of the ID index
      removeFromTypeIndex(lastNext);    // remove from type index
      removeFromOffsetIndex(lastNext);  // remove from offset indices
    } // remove()
  }; // AnnotationSetIterator

  /** Get an iterator for this set */
  public Iterator iterator() { return new AnnotationSetIterator(); }

  /** Remove an element from this set. */
  public boolean remove(Object o) throws ClassCastException {
    Annotation a = (Annotation) o;

    boolean wasPresent = removeFromIdIndex(a);
    removeFromTypeIndex(a);
    removeFromOffsetIndex(a);

    return wasPresent;
  } // remove(o)

  /** Remove from the ID index. */
  boolean removeFromIdIndex(Annotation a) {
    if(annotsById.remove(a.getId()) == null)
      return false;
    return true;
  } // removeFromIdIndex(a)

  /** Remove from the type index. */
  void removeFromTypeIndex(Annotation a) {
    if(annotsByType != null) {
      AnnotationSet sameType = (AnnotationSet) annotsByType.get(a.getType());
      if(sameType != null) sameType.remove(a);
      if(sameType.isEmpty()) // none left of this type
        annotsByType.remove(a.getType());
    }
  } // removeFromTypeIndex(a)

  /** Remove from the offset indices. */
  void removeFromOffsetIndex(Annotation a) {
    if(nodesByOffset != null) {
// knowing when a node is no longer needed would require keeping a reference
// count on annotations, or using a weak reference to the nodes in
// nodesByOffset
    }
    if(annotsByStartNode != null) {
      Integer id = a.getStartNode().getId();
      AnnotationSet starterAnnots = (AnnotationSet) annotsByStartNode.get(id);
      starterAnnots.remove(a);
      if(starterAnnots.isEmpty()) // no annotations start here any more
        annotsByStartNode.remove(id);
    }
    if(annotsByEndNode != null) {
      Integer id = a.getEndNode().getId();
      AnnotationSet endingAnnots = (AnnotationSet) annotsByEndNode.get(id);
      endingAnnots.remove(a);
      if(endingAnnots.isEmpty()) // no annotations start here any more
        annotsByEndNode.remove(id);
    }
  } // removeFromOffsetIndex(a)

  /** The size of this set */
  public int size() { return annotsById.size(); }

  /** Find annotations by id */
  public Annotation get(Integer id) {
    return (Annotation) annotsById.get(id);
  } // get(id)

  /** Get all annotations */
  public AnnotationSet get() {
    AnnotationSet resultSet = new AnnotationSetImpl(doc);
    resultSet.addAll(annotsById.values());
    if(resultSet.isEmpty())
      return null;
    return resultSet;
  } // get()

  /** Select annotations by type */
  public AnnotationSet get(String type) {
    if(annotsByType == null) indexByType();

    return (AnnotationSet) annotsByType.get(type);
  } // get(type)

  /** Select annotations by a set of types. Expects a Set of String. */
  public AnnotationSet get(Set types) throws ClassCastException {
    if(annotsByType == null) indexByType();

    Iterator iter = types.iterator();
    AnnotationSet resultSet = new AnnotationSetImpl(doc);
    while(iter.hasNext()) {
      String type = (String) iter.next();
      AnnotationSet as = (AnnotationSet) annotsByType.get(type);
      if(as != null)
        resultSet.addAll(as);
// need an addAllOfOneType method
    } // while

    if(resultSet.isEmpty())
      return null;
    return resultSet;
  } // get(types)

  /** Select annotations by type and features */
  public AnnotationSet get(String type, FeatureMap constraints) {
    if(annotsByType == null) indexByType();

    AnnotationSet typeSet = get(type);
    if(typeSet == null)
      return null;
    AnnotationSet resultSet = new AnnotationSetImpl(doc);

    Iterator iter = typeSet.iterator();
    while(iter.hasNext()) {
      Annotation a = (Annotation) iter.next();

      // we check for matching constraints by simple equality. a
      // feature map satisfies the constraints if it contains all the
      // key/value pairs from the constraints map
      if( a.getFeatures().entrySet().containsAll( constraints.entrySet() ) )
        resultSet.add(a);
    } // while

    if(resultSet.isEmpty())
      return null;
    return resultSet;
  } // get(type, constraints)

  /** Select annotations by offset. This returns the set of annotations
    * whose start node is the least such that it is less than or equal
    * to offset. If a positional index doesn't exist it is created.
    */
  public AnnotationSet get(Long offset) {
    if(annotsByStartNode == null) indexByStartOffset();

    // find the next node at or after offset; get the annots starting there
    Node nextNode = (Node) nodesByOffset.getNextOf(offset);
    if(nextNode == null) // no nodes beyond this offset
      return null;
    AnnotationSet res = (AnnotationSet) annotsByStartNode.get(nextNode.getId());
    //skip all the nodes that have no starting annotations
    while(nextNode != null && res == null){
      nextNode = (Node) nodesByOffset.getNextOf(new Long(offset.longValue() + 1));
      res = (AnnotationSet) annotsByStartNode.get(nextNode.getId());
    }
    //res it either null (no suitable node found) or the correct result
    return res;
  } // get(offset)

  /** Select annotations by type, features and offset */
  public AnnotationSet get(String type, FeatureMap constraints, Long offset)
  {
    // select by offset
    AnnotationSet nextAnnots = (AnnotationSet) get(offset);
    if(nextAnnots == null) return null;

    // select by type and constraints from the next annots
    return nextAnnots.get(type, constraints);
  } // get(type, constraints, offset)

  /** Get the node with the smallest offset */
  public Node firstNode() {
    indexByStartOffset();
    if(nodesByOffset.isEmpty()) return null;
    else return (Node) nodesByOffset.get(nodesByOffset.firstKey());
  } // firstNode

  /** Get the node with the largest offset */
  public Node lastNode() {
    indexByStartOffset();
    indexByEndOffset();
    if(nodesByOffset.isEmpty())return null;
    else return (Node) nodesByOffset.get(nodesByOffset.lastKey());
  } // lastNode

  /**
  * Get the first node that is relevant for this annotation set and which has
  * the offset larger than the one of the node provided.
  */
  public Node nextNode(Node node){
    indexByStartOffset();
    indexByEndOffset();
    return (Node)nodesByOffset.getNextOf(
                               new Long(node.getOffset().longValue() + 1)
                               );
  }

  /** Create and add an annotation with pre-existing nodes,
    * and return its id
    */
  public Integer add(Node start, Node end, String type, FeatureMap features)
  {
    // the id of the new annotation
    Integer id = doc.getNextAnnotationId();

    // construct an annotation
    Annotation a = new AnnotationImpl(id, start, end, type, features);

    // delegate to the method that adds existing annotations
    add(a);

    return id;
  } // add(Node, Node, String, FeatureMap)

  /** Add an existing annotation. Returns true when the set is modified. */
  public boolean add(Object o) throws ClassCastException {
    Annotation a = (Annotation) o;
    Object oldValue = annotsById.put(a.getId(), a);
    addToTypeIndex(a);
    addToOffsetIndex(a);
    return oldValue != a;
  } // add(o)

  /** Create and add an annotation and return its id */
  public Integer add(
    Long start, Long end, String type, FeatureMap features
  ) throws InvalidOffsetException {
    // are the offsets valid?
    if(! doc.isValidOffsetRange(start, end))
      throw new InvalidOffsetException();

    // the set has to be indexed by position in order to add, as we need
    // to find out if nodes need creating or if they exist already
    if(nodesByOffset == null) indexByStartOffset();

    // find existing nodes
    Node startNode  = (Node) nodesByOffset.getNextOf(start);
    Node endNode    = (Node) nodesByOffset.getNextOf(end);

    // if appropriate nodes don't already exist, create them
    if(startNode == null || ! startNode.getOffset().equals(start))
      startNode = new NodeImpl(doc.getNextNodeId(), start);
    if(endNode == null   || ! endNode.getOffset().equals(end))
      endNode = new NodeImpl(doc.getNextNodeId(), end);

    // delegate to the method that adds annotations with existing nodes
    return add(startNode, endNode, type, features);
  } // add(start, end, type, features)

  /** Create and add an annotation from database read data
    * In this case the id is already known being previously fetched from the
    * database*/
  public void add(
    Integer id, Long start, Long end, String type, FeatureMap features
  ) throws InvalidOffsetException {
    // are the offsets valid?
    if(! doc.isValidOffsetRange(start, end))
      throw new InvalidOffsetException();

    // the set has to be indexed by position in order to add, as we need
    // to find out if nodes need creating or if they exist already
    if(nodesByOffset == null) indexByStartOffset();

    // find existing nodes
    Node startNode  = (Node) nodesByOffset.getNextOf(start);
    Node endNode    = (Node) nodesByOffset.getNextOf(end);

    // if appropriate nodes don't already exist, create them
    if(startNode == null || ! startNode.getOffset().equals(start))
      startNode = new NodeImpl(doc.getNextNodeId(), start);
    if(endNode == null   || ! endNode.getOffset().equals(end))
      endNode = new NodeImpl(doc.getNextNodeId(), end);

    // construct an annotation
    Annotation a = new AnnotationImpl(id, startNode, endNode, type, features);
    add(a);

  } // add(id, start, end, type, features)

  /** Construct the positional index. */
  public void indexByType() {
    if(annotsByType != null) return;

    annotsByType = new HashMap();
    Annotation a;
    Iterator annotIter = annotsById.values().iterator();
    while(annotIter.hasNext())
      addToTypeIndex( (Annotation) annotIter.next() );
  } // indexByType()

  /** Construct the positional indices for annotation start */
  public void indexByStartOffset() {
    if(annotsByStartNode != null) return;

    if(nodesByOffset == null)
      nodesByOffset = new RBTreeMap();
    annotsByStartNode = new HashMap();

    Annotation a;
    Iterator annotIter = annotsById.values().iterator();
    while(annotIter.hasNext())
      addToStartOffsetIndex( (Annotation) annotIter.next() );
  } // indexByStartOffset()

  /** Construct the positional indices for annotation end */
  public void indexByEndOffset() {
    if(annotsByEndNode != null) return;

    if(nodesByOffset == null)
      nodesByOffset = new RBTreeMap();
    annotsByEndNode = new HashMap();

    Annotation a;
    Iterator annotIter = annotsById.values().iterator();
    while(annotIter.hasNext())
      addToEndOffsetIndex( (Annotation) annotIter.next() );
  } // indexByEndOffset()

  /** Add an annotation to the type index. Does nothing if the index
    * doesn't exist.
    */
  void addToTypeIndex(Annotation a) {
    if(annotsByType == null) return;

    String type = a.getType();
    AnnotationSet sameType = (AnnotationSet) annotsByType.get(type);
    if(sameType == null) {
      sameType = new AnnotationSetImpl(doc);
      annotsByType.put(type, sameType);
    }
    sameType.add(a);
  } // addToTypeIndex(a)

  /** Add an annotation to the offset indices. Does nothing if they
    * don't exist.
    */
  void addToOffsetIndex(Annotation a) {
    addToStartOffsetIndex(a);
    addToEndOffsetIndex(a);
  } // addToOffsetIndex(a)

  /** Add an annotation to the start offset index. Does nothing if the
    * index doesn't exist.
    */
  void addToStartOffsetIndex(Annotation a) {
    Node startNode  = a.getStartNode();
    Node endNode    = a.getEndNode();
    Long start      = startNode.getOffset();
    Long end        = endNode.getOffset();

    // add a's nodes to the offset index
    if(nodesByOffset != null)
      nodesByOffset.put(start, startNode);

    // if there's no appropriate index give up
    if(annotsByStartNode == null) return;

    // get the annotations that start at the same node, or create new set
    AnnotationSet thisNodeAnnots =
      (AnnotationSet) annotsByStartNode.get(startNode.getId());
    if(thisNodeAnnots == null) {
      thisNodeAnnots = new AnnotationSetImpl(doc);
      annotsByStartNode.put(startNode.getId(), thisNodeAnnots);
    }
    // add to the annots listed for a's start node
    thisNodeAnnots.add(a);

  } // addToStartOffsetIndex(a)

  /** Add an annotation to the end offset index. Does nothing if the
    * index doesn't exist.
    */
  void addToEndOffsetIndex(Annotation a) {
    Node startNode  = a.getStartNode();
    Node endNode    = a.getEndNode();
    Long start      = startNode.getOffset();
    Long end        = endNode.getOffset();

    // add a's nodes to the offset index
    if(nodesByOffset != null) nodesByOffset.put(end, endNode);

    // if there's no appropriate index give up
    if(annotsByEndNode == null) return;

    // get the annotations that start at the same node, or create new set
    AnnotationSet thisNodeAnnots =
      (AnnotationSet) annotsByEndNode.get(endNode.getId());
    if(thisNodeAnnots == null) {
      thisNodeAnnots = new AnnotationSetImpl(doc);
      annotsByEndNode.put(startNode.getId(), thisNodeAnnots);
    }
    // add to the annots listed for a's start node
    thisNodeAnnots.add(a);

  } // addToEndOffsetIndex(a)

  /** Propagate changes to the document content. Has, unfortunately,
    * to be public, to allow DocumentImpls to get at it. Oh for a
    * "friend" declaration. Doesn't thow InvalidOffsetException as
    * DocumentImpl is the only client, and that checks the offsets
    * before calling this method.
    */
  public void edit(Long start, Long end, DocumentContent replacement) {

    long s = start.longValue(), e = end.longValue();
    long rlen = // length of the replacement value
      ( (replacement == null) ? 0 : replacement.size().longValue() );

    indexByStartOffset();
    indexByEndOffset();

    Iterator replacedAreaNodesIter =
      nodesByOffset.subMap(start, end).values().iterator();
    while(replacedAreaNodesIter.hasNext()) {
      Node n = (Node) replacedAreaNodesIter.next();

      // remove from nodes map
if(true)
throw new LazyProgrammerException("this next call tries to remove from a map based on the value; note index is key; also note that some nodes may have no index....");
      nodesByOffset.remove(n);

      // remove annots that start at this node
      AnnotationSet invalidatedAnnots =
        (AnnotationSet) annotsByStartNode.get(n.getId());
      if(invalidatedAnnots != null)
        removeAll(invalidatedAnnots);

      // remove annots that end at this node
      invalidatedAnnots =
        (AnnotationSet) annotsByEndNode.get(n.getId());
      if(invalidatedAnnots != null)
        removeAll(invalidatedAnnots);
    } // loop over replaced area nodes

    // update the offsets of the other nodes
    Iterator nodesAfterReplacementIter =
      nodesByOffset.tailMap(end).values().iterator();
    while(nodesAfterReplacementIter.hasNext()) {
      NodeImpl n = (NodeImpl) nodesAfterReplacementIter.next();
      long oldOffset = n.getOffset().longValue();

      n.setOffset(new Long( oldOffset - ( (e-s) - rlen ) ));
    } // loop over nodes after replacement area

  } // edit(start,end,replacement)

  /** Get the name of this set. */
  public String getName() { return name; }

  /** Get the document this set is attached to. */
  public Document getDocument() { return doc; }

  /** Get a set of java.lang.String objects representing all the annotation
    * types present in this annotation set.
    */
  public Set getAllTypes(){
    indexByType();
    return annotsByType.keySet();
  }

  public Object clone() throws CloneNotSupportedException{
    return super.clone();
  }
  /** String representation of the set */
/*  public String toString() {

    // annotsById
    SortedSet sortedAnnots = new TreeSet();
    sortedAnnots.addAll(annotsById.values());
    String aBI = sortedAnnots.toString();

    // annotsByType

    StringBuffer buf = new StringBuffer();
    Iterator iter = annotsByType.iterator();
    while(iter.hasNext()) {
      HashMap thisType = iter.next().entrySet();
      sortedAnnots.clear();
      sortedAnnots.addAll(thisType.);
      buf.append("[type: " +
    }


    return
      "AnnotationSetImpl: " +
      "name=" + name +
//      "; doc.getURL()=" + doc +
      "; annotsById=" + aBI +
//      "; annotsByType=" + aBT +
      "; "
      ;
  } // toString()
*/

  /** The name of this set */
  String name = null;

  /** The document this set belongs to */
  DocumentImpl doc;

  /** Maps annotation ids (Integers) to Annotations */
  protected HashMap annotsById;

  /** Maps annotation types (Strings) to AnnotationSets */
  HashMap annotsByType = null;

  /** Maps offsets (Longs) to nodes */
  RBTreeMap nodesByOffset;

  /** Maps node ids (Integers) to AnnotationSets representing those
    * annotations that start from that node
    */
  HashMap annotsByStartNode;

  /** Maps node ids (Integers) to AnnotationSets representing those
    * annotations that end at that node
    */
  HashMap annotsByEndNode;
} // AnnotationSetImpl