/*
 *  AnnotationSetImpl.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
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
import gate.event.*;


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
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction from Document. */
  public AnnotationSetImpl(Document doc) {
    annotsById = new VerboseHashMap();
    this.doc = (DocumentImpl) doc;
  } // construction from document

  /** Construction from Document and name. */
  public AnnotationSetImpl(Document doc, String name) {
    this(doc);
    this.name = name;
  } // construction from document and name

  /** Construction from Collection (which must be an AnnotationSet) */
//<<<dam: speedup constructor
/*
  public AnnotationSetImpl(Collection c) throws ClassCastException {
    this(((AnnotationSet) c).getDocument());
    addAll(c);
  } // construction from collection
*/
//===dam: now
  /** Construction from Collection (which must be an AnnotationSet) */
  public AnnotationSetImpl(Collection c) throws ClassCastException {
    this(((AnnotationSet) c).getDocument());

    if (c instanceof AnnotationSetImpl)
    {
        AnnotationSetImpl theC = (AnnotationSetImpl)c;
        annotsById = (HashMap)theC.annotsById.clone();
        if(theC.annotsByEndNode != null)
        {
            annotsByEndNode = (Map)((HashMap)theC.annotsByEndNode).clone();
            annotsByStartNode = (Map)((HashMap)theC.annotsByStartNode).clone();
        }
        if (theC.annotsByType != null)
            annotsByType = (Map)((HashMap)theC.annotsByType).clone();
        if (theC.nodesByOffset != null)
        {
            nodesByOffset = (RBTreeMap)theC.nodesByOffset.clone();
        }

    } else
        addAll(c);
  } // construction from collection
//>>>dam: end

  /** This inner class serves as the return value from the iterator()
    * method.
    */
  class AnnotationSetIterator implements Iterator {

    private Iterator iter;

    private Annotation lastNext = null;

    AnnotationSetIterator()  { iter = annotsById.values().iterator(); }

    public boolean hasNext() { return iter.hasNext(); }

    public Object next()     { return (lastNext = (Annotation) iter.next());}

    public void remove()     {
      // this takes care of the ID index
      iter.remove();
      //that's the second way of removing annotations from a set
      //apart from calling remove() on the set itself
      fireAnnotationRemoved(new AnnotationSetEvent(
                            AnnotationSetImpl.this,
                            AnnotationSetEvent.ANNOTATION_REMOVED,
                            getDocument(), (Annotation)lastNext));

      // remove from type index
      removeFromTypeIndex(lastNext);

      // remove from offset indices
      removeFromOffsetIndex(lastNext);

    } // remove()

  }; // AnnotationSetIterator

  /**
   * Class used for the indexById structure. This is a {@link java.util.HashMap}
   * that fires events when elements are removed.
   */
  public class VerboseHashMap extends HashMap{

    VerboseHashMap() {
      super(Gate.HASH_STH_SIZE);
    } //contructor

    public Object remove(Object key){
      Object res = super.remove(key);
      if(res != null) {
        fireAnnotationRemoved(new AnnotationSetEvent(
                                    AnnotationSetImpl.this,
                                    AnnotationSetEvent.ANNOTATION_REMOVED,
                                    getDocument(), (Annotation)res));
      }
      return res;
    }//public Object remove(Object key)
    static final long serialVersionUID = -4832487354063073511L;
  }//protected class VerboseHashMap extends HashMap

  /** Get an iterator for this set */
  public Iterator iterator() { return new AnnotationSetIterator(); }

  /** Remove an element from this set. */
  public boolean remove(Object o) throws ClassCastException {

    Annotation a = (Annotation) o;

    boolean wasPresent = removeFromIdIndex(a);
    if(wasPresent){
      removeFromTypeIndex(a);
      removeFromOffsetIndex(a);
    }
    return wasPresent;
  } // remove(o)

  /** Remove from the ID index. */
  protected boolean removeFromIdIndex(Annotation a) {
    if(annotsById.remove(a.getId()) == null)
      return false;

    return true;
  } // removeFromIdIndex(a)

  /** Remove from the type index. */
  protected void removeFromTypeIndex(Annotation a) {
    if(annotsByType != null) {

      AnnotationSet sameType = (AnnotationSet) annotsByType.get(a.getType());

      if(sameType != null) sameType.remove(a);

      if(sameType.isEmpty()) // none left of this type
        annotsByType.remove(a.getType());
    }
  } // removeFromTypeIndex(a)

  /** Remove from the offset indices. */
  protected void removeFromOffsetIndex(Annotation a) {
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
    Iterator iter = annotsById.values().iterator();
    while (iter.hasNext()) {
      Out.prln(iter.next().toString());
    }

    resultSet.addAll(annotsById.values());
    if(resultSet.isEmpty())
      return null;
    return resultSet;
  } // get()

  /** Select annotations by type */
  public AnnotationSet get(String type) {
    if(annotsByType == null) indexByType();

    // the aliasing that happens when returning a set directly from the
    // types index can cause concurrent access problems; but the fix below
    // breaks the tests....
    //AnnotationSet newSet =
    //  new AnnotationSetImpl((Collection) annotsByType.get(type));
    //return newSet;

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

  /** Select annotations by type and feature names */
  public AnnotationSet get(String type, Set featureNames) {
    if(annotsByType == null) indexByType();

    AnnotationSet typeSet= null;
    if (type != null) {
      //if a type is provided, try finding annotations of this type
      typeSet = get(type);
      //if none exist, then return coz nothing left to do
      if(typeSet == null)
       return null;
    }

    AnnotationSet resultSet = new AnnotationSetImpl(doc);

    Iterator iter = null;
    if (type != null)
      iter = typeSet.iterator();
    else
      iter = annotsById.values().iterator();

    while(iter.hasNext()) {
      Annotation a = (Annotation) iter.next();

      // we check for matching constraints by simple equality. a
      // feature map satisfies the constraints if it contains all the
      // key/value pairs from the constraints map
      if( a.getFeatures().keySet().containsAll( featureNames ) )
        resultSet.add(a);
    } // while

    if(resultSet.isEmpty())
      return null;
    return resultSet;
  } // get(type, featureNames)

  /** Select annotations by offset. This returns the set of annotations
    * whose start node is the least such that it is less than or equal
    * to offset. If a positional index doesn't exist it is created.
    * If there are no nodes at or beyond the offset param then it will return
    * null.
    */
  public AnnotationSet get(Long offset) {
    if(annotsByStartNode == null) indexByStartOffset();

    // find the next node at or after offset; get the annots starting there
    Node nextNode = (Node) nodesByOffset.getNextOf(offset);
    if(nextNode == null) // no nodes at or beyond this offset
      return null;

    AnnotationSet res = (AnnotationSet) annotsByStartNode.get(nextNode.getId());

    //get ready for next test
    nextNode = (Node) nodesByOffset.getNextOf(new Long(offset.longValue() + 1));

    //skip all the nodes that have no starting annotations
    while(res == null && nextNode != null){
      res = (AnnotationSet) annotsByStartNode.get(nextNode.getId());

      //get ready for next test
      nextNode = (Node) nodesByOffset.getNextOf(
        new Long(nextNode.getOffset().longValue() + 1)
      );
    }

    //res it either null (no suitable node found) or the correct result
    return res;
  } // get(offset)

  /**
    * Select annotations by offset. This returns the set of annotations
    * that overlap totaly or partially with the interval defined by the two
    * provided offsets.The result will include all the annotations that either:
    * <ul>
    * <li>start before the start offset and end strictly after it</li>
    * <li>OR</li>
    * <li>start at a position between the start and the end offsets</li>
    */
  public AnnotationSet get(Long startOffset, Long endOffset) {
    //the result will include all the annotations that either:
    //-start before the start offset and end strictly after it
    //or
    //-start at a position between the start and the end offsets
    if(annotsByStartNode == null) indexByStartOffset();
    AnnotationSet resultSet = new AnnotationSetImpl(doc);
    Iterator nodesIter;
    Iterator annotsIter;
    Node currentNode;
    Annotation currentAnnot;
    //find all the annots that start strictly before the start offset and end
    //strictly after it
    nodesIter = nodesByOffset.headMap(startOffset).values().iterator();
    while(nodesIter.hasNext()){
      currentNode = (Node)nodesIter.next();
      Set fromPoint = (Set)annotsByStartNode.get(currentNode.getId());
      if(fromPoint != null){
        annotsIter = (fromPoint).iterator();
        while(annotsIter.hasNext()){
          currentAnnot = (Annotation)annotsIter.next();
          if(currentAnnot.getEndNode().getOffset().compareTo(startOffset) > 0){
            resultSet.add(currentAnnot);
          }
        }
      }
    }
    //find all the annots that start at or after the start offset but strictly
    //before the end offset
    nodesIter = nodesByOffset.subMap(startOffset, endOffset).values().iterator();
    while(nodesIter.hasNext()){
      currentNode = (Node)nodesIter.next();
      Set fromPoint = (Set)annotsByStartNode.get(currentNode.getId());
      if(fromPoint != null) resultSet.addAll(fromPoint);
    }
    return resultSet;
  }//get(startOfset, endOffset)


  /**
    * Select annotations by offset. This returns the set of annotations
    * of the given type
    * that overlap totaly or partially with the interval defined by the two
    * provided offsets.The result will include all the annotations that either:
    * <ul>
    * <li>start before the start offset and end strictly after it</li>
    * <li>OR</li>
    * <li>start at a position between the start and the end offsets</li>
    */
  public AnnotationSet get(String neededType, Long startOffset, Long endOffset) {
    //the result will include all the annotations that either:
    //-start before the start offset and end strictly after it
    //or
    //-start at a position between the start and the end offsets
    if(annotsByStartNode == null) indexByStartOffset();
    AnnotationSet resultSet = new AnnotationSetImpl(doc);
    Iterator nodesIter;
    Iterator annotsIter;
    Node currentNode;
    Annotation currentAnnot;
    //find all the annots that start strictly before the start offset and end
    //strictly after it
    nodesIter = nodesByOffset.headMap(startOffset).values().iterator();
    while(nodesIter.hasNext()){
      currentNode = (Node)nodesIter.next();
      Set fromPoint = (Set)annotsByStartNode.get(currentNode.getId());
      if(fromPoint != null){
        annotsIter = (fromPoint).iterator();
        while(annotsIter.hasNext()){
          currentAnnot = (Annotation)annotsIter.next();
          if(currentAnnot.getType().equals(neededType) &&
             currentAnnot.getEndNode().getOffset().compareTo(startOffset) > 0
            ) {
            resultSet.add(currentAnnot);
          }//if
        }//while
      }
    }
    //find all the annots that start at or after the start offset but strictly
    //before the end offset
    nodesIter = nodesByOffset.subMap(startOffset, endOffset).values().iterator();
    while(nodesIter.hasNext()){
      currentNode = (Node)nodesIter.next();
      Set fromPoint = (Set)annotsByStartNode.get(currentNode.getId());
      if(fromPoint != null) {
        annotsIter = (fromPoint).iterator();
        while(annotsIter.hasNext()){
          currentAnnot = (Annotation)annotsIter.next();
          if(currentAnnot.getType().equals(neededType)) {
            resultSet.add(currentAnnot);
          }//if
        }//while
      } //if
    }
    return resultSet;
  }//get(type, startOfset, endOffset)


  /** Select annotations by type, features and offset */
  public AnnotationSet get(String type, FeatureMap constraints, Long offset) {

    // select by offset
    AnnotationSet nextAnnots = (AnnotationSet) get(offset);

    if(nextAnnots == null) return null;

    // select by type and constraints from the next annots
    return nextAnnots.get(type, constraints);

  } // get(type, constraints, offset)

  /**
    * Select annotations by offset that
    * start at a position between the start and end before the end offset
    */
  public AnnotationSet getContained(Long startOffset, Long endOffset) {
    //the result will include all the annotations that either:
    //start at a position between the start and end before the end offsets
    if(annotsByStartNode == null) indexByStartOffset();
    AnnotationSet resultSet = new AnnotationSetImpl(doc);
    Iterator nodesIter;
    Iterator annotsIter;
    Node currentNode;
    Annotation currentAnnot;
    //find all the annots that start at or after the start offset but strictly
    //before the end offset
    nodesIter = nodesByOffset.subMap(startOffset, endOffset).values().iterator();
    while(nodesIter.hasNext()){
      currentNode = (Node)nodesIter.next();
      Set fromPoint = (Set)annotsByStartNode.get(currentNode.getId());
      if (fromPoint == null) continue;
      //loop through the annotations and find only those that
      //also end before endOffset
      Iterator annotIter = fromPoint.iterator();
      while (annotIter.hasNext()) {
        Annotation annot = (Annotation) annotIter.next();
        if (annot.getEndNode().getOffset().compareTo(endOffset) <= 0)
          resultSet.add(annot);
      }
    }
    return resultSet;
  }//get(startOfset, endOffset)



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
  public Node nextNode(Node node) {
    indexByStartOffset();
    indexByEndOffset();
    return (Node)nodesByOffset.getNextOf(
                               new Long(node.getOffset().longValue() + 1)
                               );
  }

  /** Create and add an annotation with pre-existing nodes,
    * and return its id
    */
  public Integer add(Node start, Node end, String type, FeatureMap features) {

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
    if (annotsByType != null)
      addToTypeIndex(a);
    if (annotsByStartNode != null || annotsByEndNode != null)
      addToOffsetIndex(a);
      AnnotationSetEvent evt = new AnnotationSetEvent(
                                    this,
                                    AnnotationSetEvent.ANNOTATION_ADDED,
                                    doc, a);
      fireAnnotationAdded(evt);
      fireGateEvent(evt);
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
    if(nodesByOffset == null) {
      indexByStartOffset();
      indexByEndOffset();
    }

    // find existing nodes if appropriate nodes don't already exist, create them
    Node startNode  = (Node) nodesByOffset.getNextOf(start);
    if(startNode == null || ! startNode.getOffset().equals(start))
      startNode = new NodeImpl(doc.getNextNodeId(), start);

    Node endNode = null;
    if(start.equals(end))
      endNode = startNode;
    else
      endNode = (Node) nodesByOffset.getNextOf(end);

    if(endNode == null   || ! endNode.getOffset().equals(end))
      endNode = new NodeImpl(doc.getNextNodeId(), end);

    // delegate to the method that adds annotations with existing nodes
    return add(startNode, endNode, type, features);
  } // add(start, end, type, features)

  /** Create and add an annotation from database read data
    * In this case the id is already known being previously fetched from the
    * database
    */
  public void add(
    Integer id, Long start, Long end, String type, FeatureMap features
  ) throws InvalidOffsetException {

    // are the offsets valid?
    if(! doc.isValidOffsetRange(start, end))
      throw new InvalidOffsetException();

    // the set has to be indexed by position in order to add, as we need
    // to find out if nodes need creating or if they exist already
    if(nodesByOffset == null){
      indexByStartOffset();
      indexByEndOffset();
    }

    // find existing nodes if appropriate nodes don't already exist, create them
    Node startNode  = (Node) nodesByOffset.getNextOf(start);
    if(startNode == null || ! startNode.getOffset().equals(start))
      startNode = new NodeImpl(doc.getNextNodeId(), start);

    Node endNode = null;
    if(start.equals(end))
      endNode = startNode;
    else
      endNode = (Node) nodesByOffset.getNextOf(end);

    if(endNode == null   || ! endNode.getOffset().equals(end))
      endNode = new NodeImpl(doc.getNextNodeId(), end);

    // construct an annotation
    Annotation a = new AnnotationImpl(id, startNode, endNode, type, features);
    add(a);

  } // add(id, start, end, type, features)

  /** Construct the positional index. */
  protected void indexByType() {

    if(annotsByType != null) return;

    annotsByType = new HashMap(Gate.HASH_STH_SIZE);

    Annotation a;
    Iterator annotIter = annotsById.values().iterator();

    while (annotIter.hasNext())
      addToTypeIndex( (Annotation) annotIter.next() );

  } // indexByType()

  /** Construct the positional indices for annotation start */
  protected void indexByStartOffset() {

    if(annotsByStartNode != null) return;

    if(nodesByOffset == null)
      nodesByOffset = new RBTreeMap();
    annotsByStartNode = new HashMap(Gate.HASH_STH_SIZE);

    Annotation a;
    Iterator annotIter = annotsById.values().iterator();

    while(annotIter.hasNext())
      addToStartOffsetIndex( (Annotation) annotIter.next() );

  } // indexByStartOffset()

  /** Construct the positional indices for annotation end */
  protected void indexByEndOffset() {

    if(annotsByEndNode != null) return;

    if(nodesByOffset == null)
      nodesByOffset = new RBTreeMap();
    annotsByEndNode = new HashMap(Gate.HASH_STH_SIZE);

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
    if(annotsByEndNode == null)
      return;

    // get the annotations that start at the same node, or create new set
    AnnotationSet thisNodeAnnots =
      (AnnotationSet) annotsByEndNode.get(endNode.getId());

    if(thisNodeAnnots == null) {
      thisNodeAnnots = new AnnotationSetImpl(doc);
      annotsByEndNode.put(endNode.getId(), thisNodeAnnots);
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
//      if(true)
//        throw new LazyProgrammerException("this next call tries to remove " +
//          "from a map based on the value; note index is key; also note that " +
//          "some nodes may have no index....");

//There is at most one node at any given location so removing is safe.
//Also note that unrooted nodes have never been implemented so all nodes have
//offset
      nodesByOffset.remove(n.getOffset());

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
  public Set getAllTypes() {
    indexByType();
    return annotsByType.keySet();
  }

  /**
   *
   * @return
   * @throws CloneNotSupportedException
   */
  public Object clone() throws CloneNotSupportedException{
    return super.clone();
  }
  /**
   *
   * @param l
   */
  public synchronized void removeAnnotationSetListener(AnnotationSetListener l) {
    if (annotationSetListeners != null && annotationSetListeners.contains(l)) {
      Vector v = (Vector) annotationSetListeners.clone();
      v.removeElement(l);
      annotationSetListeners = v;
    }
  }
  /**
   *
   * @param l
   */
  public synchronized void addAnnotationSetListener(AnnotationSetListener l) {
    Vector v = annotationSetListeners == null ? new Vector(2) : (Vector) annotationSetListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      annotationSetListeners = v;
    }
  }
  /** String representation of the set */
  /*public String toString() {

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
    //  "; doc.getURL()=" + doc +
      "; annotsById=" + aBI +
    //  "; annotsByType=" + aBT +
      "; "
      ;
  } // toString()
  */

//  public int hashCode() {
//    int hash = 0;
//    Iterator i = this.iterator();
//    while (i.hasNext()) {
//        Annotation annot = (Annotation)i.next();
//        if ( annot != null)
//            hash += annot.hashCode();
//    }
//    int nameHash = (name == null ? 0 : name.hashCode());
//    //int docHash = (doc == null ? 0 : doc.hashCode());
//
//    return hash ^ nameHash;// ^ docHash;
//  }

  /** The name of this set */
  String name = null;

  /** The document this set belongs to */
  DocumentImpl doc;

  /** Maps annotation ids (Integers) to Annotations */
  protected HashMap annotsById;

  /** Maps annotation types (Strings) to AnnotationSets */
  Map annotsByType = null;

  /** Maps offsets (Longs) to nodes */
  RBTreeMap nodesByOffset = null;

  /** Maps node ids (Integers) to AnnotationSets representing those
    * annotations that start from that node
    */
  Map annotsByStartNode;

  /** Maps node ids (Integers) to AnnotationSets representing those
    * annotations that end at that node
    */
  Map annotsByEndNode;
  private transient Vector annotationSetListeners;
  private transient Vector gateListeners;
  /**
   *
   * @param e
   */
  protected void fireAnnotationAdded(AnnotationSetEvent e) {
    if (annotationSetListeners != null) {
      Vector listeners = annotationSetListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((AnnotationSetListener) listeners.elementAt(i)).annotationAdded(e);
      }
    }
  }
  /**
   *
   * @param e
   */
  protected void fireAnnotationRemoved(AnnotationSetEvent e) {
    if (annotationSetListeners != null) {
      Vector listeners = annotationSetListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((AnnotationSetListener) listeners.elementAt(i)).annotationRemoved(e);
      }
    }
  }
  /**
   *
   * @param l
   */
  public synchronized void removeGateListener(GateListener l) {
    if (gateListeners != null && gateListeners.contains(l)) {
      Vector v = (Vector) gateListeners.clone();
      v.removeElement(l);
      gateListeners = v;
    }
  }
  /**
   *
   * @param l
   */
  public synchronized void addGateListener(GateListener l) {
    Vector v = gateListeners == null ? new Vector(2) : (Vector) gateListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      gateListeners = v;
    }
  }
  /**
   *
   * @param e
   */
  protected void fireGateEvent(GateEvent e) {
    if (gateListeners != null) {
      Vector listeners = gateListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((GateListener) listeners.elementAt(i)).processGateEvent(e);
      }
    }
  }

 /** Freeze the serialization UID. */
  static final long serialVersionUID = 1479426765310434166L;
} // AnnotationSetImpl
