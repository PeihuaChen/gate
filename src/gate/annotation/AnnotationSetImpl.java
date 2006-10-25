/*
 *  AnnotationSetImpl.java
 *
 *  Copyright (c) 1998-2005, The University of Sheffield.
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

import java.io.*;
import java.util.*;
import gate.*;
import gate.corpora.DocumentImpl;
import gate.event.*;
import gate.util.InvalidOffsetException;
import gate.util.RBTreeMap;

/**
 * Implementation of AnnotationSet. Has a number of indices, all bar one of
 * which are null by default and are only constructed when asked for. Has lots
 * of get methods with various selection criteria; these return views into the
 * set, which are nonetheless valid sets in their own right (but will not
 * necesarily be fully indexed). Has a name, which is null by default; clients
 * of Document can request named AnnotationSets if they so desire. Has a
 * reference to the Document it is attached to. Contrary to Collections
 * convention, there is no no-arg constructor, as this would leave the set in an
 * inconsistent state.
 * <P>
 * There are five indices: annotation by id, annotations by type, annotations by
 * start/end node and nodes by offset. The last three jointly provide positional
 * indexing; construction of these is triggered by indexByStart/EndOffset(), or
 * by calling a get method that selects on offset. The type index is triggered
 * by indexByType(), or calling a get method that selects on type. The id index
 * is always present.
 */
public class AnnotationSetImpl extends AbstractSet<Annotation> implements AnnotationSet {
  
  /** Freeze the serialization UID. */
  static final long serialVersionUID = 1479426765310434166L;
  /** The name of this set */
  String name = null;

  /** The document this set belongs to */
  DocumentImpl doc;

  /** Maps annotation ids (Integers) to Annotations */
  transient protected HashMap<Integer, Annotation> annotsById;

  /** Maps offsets (Longs) to nodes */
  transient RBTreeMap nodesByOffset = null;

  /**
   * This field is used temporarily during serialisation to store all the 
   * annotations that need to be saved. At all other times, this will be null;
   */
  private Annotation[] annotations;
  
  /** Maps annotation types (Strings) to AnnotationSets */
  transient Map<String, AnnotationSet> annotsByType = null;

  /**
   * Maps node ids (Integers) to Annotations or a Collection of Annotations that
   * start from that node
   */
  transient Map<Integer, Object> annotsByStartNode;

  protected transient Vector annotationSetListeners;

  private transient Vector gateListeners;

  /** Construction from Document. */
  public AnnotationSetImpl(Document doc) {
    // annotsById = new VerboseHashMap();
    annotsById = new HashMap<Integer, Annotation>();
    this.doc = (DocumentImpl)doc;
  } // construction from document

  /** Construction from Document and name. */
  public AnnotationSetImpl(Document doc, String name) {
    this(doc);
    this.name = name;
  } // construction from document and name

  /** Construction from Collection (which must be an AnnotationSet) */
  public AnnotationSetImpl(Collection<? extends Annotation> c) throws ClassCastException {
    this(((AnnotationSet)c).getDocument(), ((AnnotationSet)c).getName());
    if(c instanceof AnnotationSetImpl) {
      AnnotationSetImpl theC = (AnnotationSetImpl)c;
      annotsById = new HashMap<Integer, Annotation>();
      annotsById.putAll(theC.annotsById);
      if(theC.annotsByStartNode != null) {
        annotsByStartNode = new HashMap<Integer, Object>(Gate.HASH_STH_SIZE);
        annotsByStartNode.putAll(theC.annotsByStartNode);
      }
      if(theC.annotsByType != null)
        annotsByType = new HashMap<String, AnnotationSet>(Gate.HASH_STH_SIZE);
        annotsByType.putAll(theC.annotsByType);
      if(theC.nodesByOffset != null) {
        nodesByOffset = (RBTreeMap)theC.nodesByOffset.clone();
      }
    } else addAll(c);
  } // construction from collection

  // >>>dam: end
  /**
   * This inner class serves as the return value from the iterator() method.
   */
  class AnnotationSetIterator implements Iterator<Annotation> {
    private Iterator<Annotation> iter;

    protected Annotation lastNext = null;

    AnnotationSetIterator() {
      iter = annotsById.values().iterator();
    }

    public boolean hasNext() {
      return iter.hasNext();
    }

    public Annotation next() {
      return (lastNext = iter.next());
    }

    public void remove() {
      // this takes care of the ID index
      iter.remove();
      // remove from type index
      removeFromTypeIndex(lastNext);
      // remove from offset indices
      removeFromOffsetIndex(lastNext);
      // that's the second way of removing annotations from a set
      // apart from calling remove() on the set itself
      fireAnnotationRemoved(new AnnotationSetEvent(AnnotationSetImpl.this,
              AnnotationSetEvent.ANNOTATION_REMOVED, getDocument(),
              (Annotation)lastNext));
    } // remove()
  }; // AnnotationSetIterator

  /** Get an iterator for this set */
  public Iterator<Annotation> iterator() {
    return new AnnotationSetIterator();
  }

  /** Remove an element from this set. */
  public boolean remove(Object o) throws ClassCastException {
    Annotation a = (Annotation)o;
    boolean wasPresent = removeFromIdIndex(a);
    if(wasPresent) {
      removeFromTypeIndex(a);
      removeFromOffsetIndex(a);
    }
    // fire the event
    fireAnnotationRemoved(new AnnotationSetEvent(AnnotationSetImpl.this,
            AnnotationSetEvent.ANNOTATION_REMOVED, getDocument(), a));
    return wasPresent;
  } // remove(o)

  /** Remove from the ID index. */
  protected boolean removeFromIdIndex(Annotation a) {
    if(annotsById.remove(a.getId()) == null) return false;
    return true;
  } // removeFromIdIndex(a)

  /** Remove from the type index. */
  protected void removeFromTypeIndex(Annotation a) {
    if(annotsByType != null) {
      AnnotationSet sameType = annotsByType.get(a.getType());
      if(sameType != null) sameType.remove(a);
      if(sameType.isEmpty()) // none left of this type
        annotsByType.remove(a.getType());
    }
  } // removeFromTypeIndex(a)

  /** Remove from the offset indices. */
  protected void removeFromOffsetIndex(Annotation a) {
    if(nodesByOffset != null) {
      // knowing when a node is no longer needed would require keeping a
      // reference
      // count on annotations, or using a weak reference to the nodes in
      // nodesByOffset
    }
    if(annotsByStartNode != null) {
      Integer id = a.getStartNode().getId();
      // might be an annotation or an annotationset
      Object objectAtNode = annotsByStartNode.get(id);
      if(objectAtNode instanceof Annotation) {
        annotsByStartNode.remove(id); // no annotations start here any more
        return;
      }
      // otherwise it is a Collection
      Collection<Annotation> starterAnnots = (Collection<Annotation>)objectAtNode;
      starterAnnots.remove(a);
      // if there is only one annotation left
      // we discard the set and put directly the annotation
      if(starterAnnots.size() == 1)
        annotsByStartNode.put(id, starterAnnots.iterator().next());
    }
  } // removeFromOffsetIndex(a)

  /** The size of this set */
  public int size() {
    return annotsById.size();
  }

  /** Find annotations by id */
  public Annotation get(Integer id) {
    return annotsById.get(id);
  } // get(id)

  /** Get all annotations */
  public AnnotationSet get() {
    AnnotationSetImpl resultSet = new AnnotationSetImpl(doc);
    resultSet.addAllKeepIDs(annotsById.values());
    if(resultSet.isEmpty()) return null;
    return resultSet;
  } // get()

  /** Select annotations by type */
  public AnnotationSet get(String type) {
    if(annotsByType == null) indexByType();
    // the aliasing that happens when returning a set directly from the
    // types index can cause concurrent access problems; but the fix below
    // breaks the tests....
    // AnnotationSet newSet =
    // new AnnotationSetImpl((Collection) annotsByType.get(type));
    // return newSet;
    return annotsByType.get(type);
  } // get(type)

  /** Select annotations by a set of types. Expects a Set of String. */
  public AnnotationSet get(Set<String> types) throws ClassCastException {
    if(annotsByType == null) indexByType();
    Iterator<String> iter = types.iterator();
    AnnotationSetImpl resultSet = new AnnotationSetImpl(doc);
    while(iter.hasNext()) {
      String type = iter.next();
      AnnotationSet as = annotsByType.get(type);
      if(as != null) resultSet.addAllKeepIDs(as);
      // need an addAllOfOneType method
    } // while
    if(resultSet.isEmpty()) return null;
    return resultSet;
  } // get(types)

  /**
   * Select annotations by type and features
   * 
   * This will return an annotation set containing just those annotations of a
   * particular type (i.e. with a particular name) and which have features with
   * specific names and values. (It will also return annotations that have
   * features besides those specified, but it will not return any annotations
   * that do not have all the specified feature-value pairs.)
   * 
   * However, if constraints contains a feature whose value is equal to
   * gate.creole.ANNIEConstants.LOOKUP_CLASS_FEATURE_NAME (which is normally
   * "class"), then GATE will attempt to match that feature using an ontology
   * which it will try to retreive from a feature on the both the annotation and
   * in constraints. If these do not return identical ontologies, or if either
   * the annotation or constraints does not contain an ontology, then matching
   * will fail, and the annotation will not be added. In summary, this method
   * will not work normally for features with the name "class".
   * 
   * @param type
   *          The name of the annotations to return.
   * @param constraints
   *          A feature map containing all of the feature value pairs that the
   *          annotation must have in order for them to be returned.
   * @return An annotation set containing only those annotations with the given
   *         name and which have the specified set of feature-value pairs.
   */
  public AnnotationSet get(String type, FeatureMap constraints) {
    if(annotsByType == null) indexByType();
    AnnotationSet typeSet = get(type);
    if(typeSet == null) return null;
    AnnotationSet resultSet = new AnnotationSetImpl(doc);
    Iterator<Annotation> iter = typeSet.iterator();
    while(iter.hasNext()) {
      Annotation a = iter.next();
      // we check for matching constraints by simple equality. a
      // feature map satisfies the constraints if it contains all the
      // key/value pairs from the constraints map
      // if (a.getFeatures().entrySet().containsAll(constraints.entrySet()))
      if(a.getFeatures().subsumes(constraints)) resultSet.add(a);
    } // while
    if(resultSet.isEmpty()) return null;
    return resultSet;
  } // get(type, constraints)

  /** Select annotations by type and feature names */
  public AnnotationSet get(String type, Set featureNames) {
    if(annotsByType == null) indexByType();
    AnnotationSet typeSet = null;
    if(type != null) {
      // if a type is provided, try finding annotations of this type
      typeSet = get(type);
      // if none exist, then return coz nothing left to do
      if(typeSet == null) return null;
    }
    AnnotationSet resultSet = new AnnotationSetImpl(doc);
    Iterator<Annotation> iter = null;
    if(type != null)
      iter = typeSet.iterator();
    else iter = annotsById.values().iterator();
    while(iter.hasNext()) {
      Annotation a = iter.next();
      // we check for matching constraints by simple equality. a
      // feature map satisfies the constraints if it contains all the
      // key/value pairs from the constraints map
      if(a.getFeatures().keySet().containsAll(featureNames)) resultSet.add(a);
    } // while
    if(resultSet.isEmpty()) return null;
    return resultSet;
  } // get(type, featureNames)

  /**
   * Select annotations by offset. This returns the set of annotations whose
   * start node is the least such that it is less than or equal to offset. If a
   * positional index doesn't exist it is created. If there are no nodes at or
   * beyond the offset param then it will return null.
   */
  public AnnotationSet get(Long offset) {
    if(annotsByStartNode == null) indexByStartOffset();
    // find the next node at or after offset; get the annots starting there
    Node nextNode = (Node)nodesByOffset.getNextOf(offset);
    if(nextNode == null) // no nodes at or beyond this offset
      return null;
    Collection<Annotation> res = getAnnotsByStartNode(nextNode.getId());
    // get ready for next test
    nextNode = (Node)nodesByOffset.getNextOf(new Long(offset.longValue() + 1));
    // skip all the nodes that have no starting annotations
    while(res == null && nextNode != null) {
      res = getAnnotsByStartNode(nextNode.getId());
      // get ready for next test
      nextNode = (Node)nodesByOffset.getNextOf(new Long(nextNode.getOffset()
              .longValue() + 1));
    }
    // res it either null (no suitable node found) or the correct result
    if(res == null) return null;
    // otherwise it is a collection - which we'll convert to a normal
    // AnnotationSet
    AnnotationSetImpl as = new AnnotationSetImpl(doc);
    as.addAll(res);
    return as;
  } // get(offset)

  /**
   * Select annotations by offset. This returns the set of annotations that
   * overlap totaly or partially with the interval defined by the two provided
   * offsets.The result will include all the annotations that either:
   * <ul>
   * <li>start before the start offset and end strictly after it</li>
   * <li>OR</li>
   * <li>start at a position between the start and the end offsets</li>
   */
  public AnnotationSet get(Long startOffset, Long endOffset) {
    // the result will include all the annotations that either:
    // -start before the start offset and end strictly after it
    // or
    // -start at a position between the start and the end offsets
    if(annotsByStartNode == null) indexByStartOffset();
    AnnotationSetImpl resultSet = new AnnotationSetImpl(doc);
    Iterator nodesIter;
    Iterator<Annotation> annotsIter;
    Node currentNode;
    Annotation currentAnnot;
    // find all the annots that start strictly before the start offset and end
    // strictly after it
    nodesIter = nodesByOffset.headMap(startOffset).values().iterator();
    while(nodesIter.hasNext()) {
      currentNode = (Node)nodesIter.next();
      Collection<Annotation> objectAtNode = getAnnotsByStartNode(currentNode.getId());
      if(objectAtNode == null) continue;
      annotsIter = objectAtNode.iterator();
      while(annotsIter.hasNext()) {
        currentAnnot = annotsIter.next();
        if(currentAnnot.getEndNode().getOffset().compareTo(startOffset) > 0) {
          resultSet.add(currentAnnot);
        }
      }
    }
    // find all the annots that start at or after the start offset but strictly
    // before the end offset
    nodesIter = nodesByOffset.subMap(startOffset, endOffset).values()
            .iterator();
    while(nodesIter.hasNext()) {
      currentNode = (Node)nodesIter.next();
      Collection<Annotation> objectAtNode = getAnnotsByStartNode(currentNode.getId());
      if(objectAtNode == null) continue;
      resultSet.addAllKeepIDs(objectAtNode);
    }
    return resultSet;
  } // get(startOfset, endOffset)

  /**
   * Select annotations by offset. This returns the set of annotations that
   * overlap strictly with the interval defined by the two provided offsets.The
   * result will include all the annotations that start at the start offset and
   * end strictly at the end offset
   */
  public AnnotationSet getStrict(Long startOffset, Long endOffset) {
    // the result will include all the annotations that
    // start at the start offset and end strictly at the end offset
    if(annotsByStartNode == null) indexByStartOffset();
    AnnotationSet resultSet = new AnnotationSetImpl(doc);
    Iterator<Annotation> annotsIter;
    Node currentNode;
    Annotation currentAnnot;
    // find all the annots that start at the start offset
    currentNode = (Node)nodesByOffset.get(startOffset);
    if(currentNode != null) {
      Collection<Annotation> objFromPoint = getAnnotsByStartNode(currentNode.getId());
      if(objFromPoint != null) {
        annotsIter = objFromPoint.iterator();
        while(annotsIter.hasNext()) {
          currentAnnot = annotsIter.next();
          if(currentAnnot.getEndNode().getOffset().compareTo(endOffset) == 0) {
            resultSet.add(currentAnnot);
          } // if
        } // while
      } // if
    } // if
    return resultSet;
  } // getStrict(startOfset, endOffset)

  /**
   * Select annotations by offset. This returns the set of annotations of the
   * given type that overlap totaly or partially with the interval defined by
   * the two provided offsets.The result will include all the annotations that
   * either:
   * <ul>
   * <li>start before the start offset and end strictly after it</li>
   * <li>OR</li>
   * <li>start at a position between the start and the end offsets</li>
   */
  public AnnotationSet get(String neededType, Long startOffset, Long endOffset) {
    // the result will include all the annotations that either:
    // -start before the start offset and end strictly after it
    // or
    // -start at a position between the start and the end offsets
    if(annotsByStartNode == null) indexByStartOffset();
    AnnotationSet resultSet = new AnnotationSetImpl(doc);
    Iterator nodesIter;
    Iterator<Annotation> annotsIter;
    Node currentNode;
    Annotation currentAnnot;
    // find all the annots that start strictly before the start offset and end
    // strictly after it
    nodesIter = nodesByOffset.headMap(startOffset).values().iterator();
    while(nodesIter.hasNext()) {
      currentNode = (Node)nodesIter.next();
      Collection<Annotation> objFromPoint = getAnnotsByStartNode(currentNode.getId());
      if(objFromPoint == null) continue;
      annotsIter = objFromPoint.iterator();
      while(annotsIter.hasNext()) {
        currentAnnot = annotsIter.next();
        if(currentAnnot.getType().equals(neededType)
                && currentAnnot.getEndNode().getOffset().compareTo(startOffset) > 0) {
          resultSet.add(currentAnnot);
        } // if
      } // while
    }
    // find all the annots that start at or after the start offset but strictly
    // before the end offset
    nodesIter = nodesByOffset.subMap(startOffset, endOffset).values()
            .iterator();
    while(nodesIter.hasNext()) {
      currentNode = (Node)nodesIter.next();
      Collection<Annotation> objFromPoint = getAnnotsByStartNode(currentNode.getId());
      if(objFromPoint == null) continue;
      annotsIter = objFromPoint.iterator();
      while(annotsIter.hasNext()) {
        currentAnnot = annotsIter.next();
        if(currentAnnot.getType().equals(neededType)) {
          resultSet.add(currentAnnot);
        } // if
      } // while
    }
    return resultSet;
  } // get(type, startOfset, endOffset)

  /** Select annotations by type, features and offset */
  public AnnotationSet get(String type, FeatureMap constraints, Long offset) {
    // select by offset
    AnnotationSet nextAnnots = (AnnotationSet)get(offset);
    if(nextAnnots == null) return null;
    // select by type and constraints from the next annots
    return nextAnnots.get(type, constraints);
  } // get(type, constraints, offset)

  /**
   * Select annotations by offset that start at a position between the start and
   * end before the end offset
   */
  public AnnotationSet getContained(Long startOffset, Long endOffset) {
    // the result will include all the annotations that either:
    // start at a position between the start and end before the end offsets
    if(annotsByStartNode == null) indexByStartOffset();
    AnnotationSet resultSet = new AnnotationSetImpl(doc);
    Iterator nodesIter;
    Node currentNode;
    Iterator<Annotation> annotIter;
    // find all the annots that start at or after the start offset but strictly
    // before the end offset
    nodesIter = nodesByOffset.subMap(startOffset, endOffset).values()
            .iterator();
    while(nodesIter.hasNext()) {
      currentNode = (Node)nodesIter.next();
      Collection<Annotation> objFromPoint = getAnnotsByStartNode(currentNode.getId());
      if(objFromPoint == null) continue;
      // loop through the annotations and find only those that
      // also end before endOffset
      annotIter = objFromPoint.iterator();
      while(annotIter.hasNext()) {
        Annotation annot = annotIter.next();
        if(annot.getEndNode().getOffset().compareTo(endOffset) <= 0)
          resultSet.add(annot);
      }
    }
    return resultSet;
  } // get(startOfset, endOffset)

  /** Get the node with the smallest offset */
  public Node firstNode() {
    indexByStartOffset();
    if(nodesByOffset.isEmpty())
      return null;
    else return (Node)nodesByOffset.get(nodesByOffset.firstKey());
  } // firstNode

  /** Get the node with the largest offset */
  public Node lastNode() {
    indexByStartOffset();
    if(nodesByOffset.isEmpty())
      return null;
    else return (Node)nodesByOffset.get(nodesByOffset.lastKey());
  } // lastNode

  /**
   * Get the first node that is relevant for this annotation set and which has
   * the offset larger than the one of the node provided.
   */
  public Node nextNode(Node node) {
    indexByStartOffset();
    return (Node)nodesByOffset.getNextOf(new Long(
            node.getOffset().longValue() + 1));
  }

  protected static AnnotationFactory annFactory;

  /**
   * Set the annotation factory used to create annotation objects. The default
   * factory is {@link DefaultAnnotationFactory}.
   */
  public static void setAnnotationFactory(AnnotationFactory newFactory) {
    annFactory = newFactory;
  }

  static {
    // set the default factory to always create AnnotationImpl objects
    setAnnotationFactory(new DefaultAnnotationFactory());
  }

  /**
   * Create and add an annotation with pre-existing nodes, and return its id
   */
  public Integer add(Node start, Node end, String type, FeatureMap features) {
    // the id of the new annotation
    Integer id = doc.getNextAnnotationId();
    // construct an annotation
    annFactory.createAnnotationInSet(this, id, start, end, type, features);
    return id;
  } // add(Node, Node, String, FeatureMap)

  /** Add an existing annotation. Returns true when the set is modified. */
  public boolean add(Annotation a) throws ClassCastException {
    Object oldValue = annotsById.put(a.getId(), a);
    if(annotsByType != null) addToTypeIndex(a);
    if(annotsByStartNode != null) addToStartOffsetIndex(a);
    AnnotationSetEvent evt = new AnnotationSetEvent(this,
            AnnotationSetEvent.ANNOTATION_ADDED, doc, a);
    fireAnnotationAdded(evt);
    fireGateEvent(evt);
    return oldValue != a;
  } // add(o)

  /**
   * Adds multiple annotations to this set in one go. All the objects in the
   * provided collection should be of {@link gate.Annotation} type, otherwise a
   * ClassCastException will be thrown. The provided annotations will be used to
   * create new annotations using the appropriate add() methods from this set.
   * The new annotations will have different IDs from the old ones (which is
   * required in order to preserve the uniqueness of IDs inside an annotation
   * set).
   * 
   * @param c
   *          a collection of annotations
   * @return <tt>true</tt> if the set has been modified as a result of this
   *         call.
   */
  public boolean addAll(Collection<? extends Annotation> c) {
    Iterator<? extends Annotation> annIter = c.iterator();
    boolean changed = false;
    while(annIter.hasNext()) {
      Annotation a = annIter.next();
      try {
        add(a.getStartNode().getOffset(), a.getEndNode().getOffset(), a
                .getType(), a.getFeatures());
        changed = true;
      } catch(InvalidOffsetException ioe) {
        throw new IllegalArgumentException(ioe.toString());
      }
    }
    return changed;
  }

  /**
   * Adds multiple annotations to this set in one go. All the objects in the
   * provided collection should be of {@link gate.Annotation} type, otherwise a
   * ClassCastException will be thrown. This method does not create copies of
   * the annotations like addAll() does but simply adds the new annotations to
   * the set. It is intended to be used solely by annotation sets in order to
   * construct the results for various get(...) methods.
   * 
   * @param c
   *          a collection of annotations
   * @return <tt>true</tt> if the set has been modified as a result of this
   *         call.
   */
  protected boolean addAllKeepIDs(Collection<? extends Annotation> c) {
    Iterator<? extends Annotation> annIter = c.iterator();
    boolean changed = false;
    while(annIter.hasNext()) {
      Annotation a = annIter.next();
      changed |= add(a);
    }
    return changed;
  }

  /** Create and add an annotation and return its id */
  public Integer add(Long start, Long end, String type, FeatureMap features)
          throws InvalidOffsetException {
    // are the offsets valid?
    if(!doc.isValidOffsetRange(start, end)) throw new InvalidOffsetException();
    // the set has to be indexed by position in order to add, as we need
    // to find out if nodes need creating or if they exist already
    if(nodesByOffset == null) {
      indexByStartOffset();
    }
    // find existing nodes if appropriate nodes don't already exist, create them
    Node startNode = (Node)nodesByOffset.getNextOf(start);
    if(startNode == null || !startNode.getOffset().equals(start))
      startNode = new NodeImpl(doc.getNextNodeId(), start);
    Node endNode = null;
    if(start.equals(end))
      endNode = startNode;
    else endNode = (Node)nodesByOffset.getNextOf(end);
    if(endNode == null || !endNode.getOffset().equals(end))
      endNode = new NodeImpl(doc.getNextNodeId(), end);
    // delegate to the method that adds annotations with existing nodes
    return add(startNode, endNode, type, features);
  } // add(start, end, type, features)

  /**
   * Create and add an annotation from database read data In this case the id is
   * already known being previously fetched from the database
   */
  public void add(Integer id, Long start, Long end, String type,
          FeatureMap features) throws InvalidOffsetException {
    // are the offsets valid?
    if(!doc.isValidOffsetRange(start, end)) throw new InvalidOffsetException();
    // the set has to be indexed by position in order to add, as we need
    // to find out if nodes need creating or if they exist already
    if(nodesByOffset == null) {
      indexByStartOffset();
    }
    // find existing nodes if appropriate nodes don't already exist, create them
    Node startNode = (Node)nodesByOffset.getNextOf(start);
    if(startNode == null || !startNode.getOffset().equals(start))
      startNode = new NodeImpl(doc.getNextNodeId(), start);
    Node endNode = null;
    if(start.equals(end))
      endNode = startNode;
    else endNode = (Node)nodesByOffset.getNextOf(end);
    if(endNode == null || !endNode.getOffset().equals(end))
      endNode = new NodeImpl(doc.getNextNodeId(), end);
    // construct an annotation
    annFactory.createAnnotationInSet(this, id, startNode, endNode, type,
            features);
  } // add(id, start, end, type, features)

  /** Construct the positional index. */
  protected void indexByType() {
    if(annotsByType != null) return;
    annotsByType = new HashMap<String, AnnotationSet>(Gate.HASH_STH_SIZE);
    Iterator<Annotation> annotIter = annotsById.values().iterator();
    while(annotIter.hasNext())
      addToTypeIndex(annotIter.next());
  } // indexByType()

  /** Construct the positional indices for annotation start */
  protected void indexByStartOffset() {
    if(annotsByStartNode != null) return;
    if(nodesByOffset == null) nodesByOffset = new RBTreeMap();
    annotsByStartNode = new HashMap<Integer, Object>(annotsById.size());
    Iterator<Annotation> annotIter = annotsById.values().iterator();
    while(annotIter.hasNext())
      addToStartOffsetIndex(annotIter.next());
  } // indexByStartOffset()

  /**
   * Add an annotation to the type index. Does nothing if the index doesn't
   * exist.
   */
  void addToTypeIndex(Annotation a) {
    if(annotsByType == null) return;
    String type = a.getType();
    AnnotationSet sameType = annotsByType.get(type);
    if(sameType == null) {
      sameType = new AnnotationSetImpl(doc);
      annotsByType.put(type, sameType);
    }
    sameType.add(a);
  } // addToTypeIndex(a)

  /**
   * Add an annotation to the start offset index. Does nothing if the index
   * doesn't exist.
   */
  void addToStartOffsetIndex(Annotation a) {
    Node startNode = a.getStartNode();
    Node endNode = a.getEndNode();
    Long start = startNode.getOffset();
    Long end = endNode.getOffset();
    // add a's nodes to the offset index
    if(nodesByOffset != null) {
      nodesByOffset.put(start, startNode);
      nodesByOffset.put(end, endNode);
    }
    // if there's no appropriate index give up
    if(annotsByStartNode == null) return;
    // get the annotations that start at the same node, or create new set
    Object thisNodeObject = annotsByStartNode.get(startNode.getId());
    if(thisNodeObject == null) {
      // put directly the annotation
      annotsByStartNode.put(startNode.getId(), a);
    } else { // already something there : a single Annotation or a Collection
      Set<Annotation> newCollection = null;
      if(thisNodeObject instanceof Annotation) {
        // we need to create a set - we have more than one annotation starting
        // at this Node
        if(thisNodeObject.equals(a)) return;
        newCollection = new HashSet<Annotation>(3);
        newCollection.add((Annotation)thisNodeObject);
        annotsByStartNode.put(startNode.getId(), newCollection);
      } else newCollection = (Set<Annotation>)thisNodeObject;
      // get the existing set
      // add the new node annotation
      newCollection.add(a);
    }
  } // addToStartOffsetIndex(a)

  /**
   * Propagate changes to the document content. Has, unfortunately, to be
   * public, to allow DocumentImpls to get at it. Oh for a "friend" declaration.
   * Doesn't throw InvalidOffsetException as DocumentImpl is the only client,
   * and that checks the offsets before calling this method.
   */
  public void edit(Long start, Long end, DocumentContent replacement) {
    // make sure we have the indices computed
    indexByStartOffset();
    if(end.compareTo(start) > 0) {
      // get the nodes that need to be processed (the nodes internal to the
      // removed section plus the marginal ones
      List affectedNodes = new ArrayList(nodesByOffset.subMap(start,
              new Long(end.longValue() + 1)).values());
      // if we have more than 1 node we need to delete all apart from the first
      // and move the annotations so that they refer to the one we keep (the
      // first)
      NodeImpl firstNode = null;
      if(!affectedNodes.isEmpty()) {
        firstNode = (NodeImpl)affectedNodes.get(0);
        List startingAnnotations = new ArrayList();
        List endingAnnotations = new ArrayList();
        // now we need to find all the annotations
        // ending in the zone
        List beforeNodes = new ArrayList(nodesByOffset.subMap(new Long(0),
                new Long(end.longValue() + 1)).values());
        Iterator beforeNodesIter = beforeNodes.iterator();
        while(beforeNodesIter.hasNext()) {
          Node currentNode = (Node)beforeNodesIter.next();
          Collection annotations = getAnnotsByStartNode(currentNode.getId());
          if(annotations == null) continue;
          // iterates on the annotations in this set
          Iterator localIterator = ((Collection)annotations).iterator();
          while(localIterator.hasNext()) {
            Annotation annotation = (Annotation)localIterator.next();
            long offsetEndAnnotation = annotation.getEndNode().getOffset()
                    .longValue();
            // we are interested only in the annotations ending
            // inside the zone
            if(offsetEndAnnotation >= start.longValue()
                    && offsetEndAnnotation <= end.longValue())
              endingAnnotations.add(annotation);
          }
        }
        for(int i = 1; i < affectedNodes.size(); i++) {
          Node aNode = (Node)affectedNodes.get(i);
          Collection annSet = getAnnotsByStartNode(aNode.getId());
          if(annSet != null) {
            startingAnnotations.addAll(annSet);
          }
          // remove the node
          // nodesByOffset.remove(aNode.getOffset());
          // annotsByStartNode.remove(aNode);
        }
        // modify the annotations so they point to the saved node
        Iterator annIter = startingAnnotations.iterator();
        while(annIter.hasNext()) {
          AnnotationImpl anAnnot = (AnnotationImpl)annIter.next();
          anAnnot.start = firstNode;
          // remove the modified annotation if it has just become zero-length
          if(anAnnot.start == anAnnot.end){
            remove(anAnnot);
          }else{
            addToStartOffsetIndex(anAnnot);
          }
        }
        annIter = endingAnnotations.iterator();
        while(annIter.hasNext()) {
          AnnotationImpl anAnnot = (AnnotationImpl)annIter.next();
          anAnnot.end = firstNode;
          //remove the modified annotation if it has just become zero-length
          if(anAnnot.start == anAnnot.end){
            remove(anAnnot);
          }
        }
        // remove the unused nodes inside the area
        for(int i = 1; i < affectedNodes.size(); i++) {
          Node aNode = (Node)affectedNodes.get(i);          
          nodesByOffset.remove(aNode.getOffset());
          annotsByStartNode.remove(aNode);
        } 
        // repair the first node
        // remove from offset index
        nodesByOffset.remove(firstNode.getOffset());
        // change the offset for the saved node
        firstNode.setOffset(start);
        // add back to the offset index
        nodesByOffset.put(firstNode.getOffset(), firstNode);
      }
    }
    // now handle the insert and/or update the rest of the nodes' position
    // get the user selected behaviour (defaults to append)
    boolean shouldPrepend = Gate.getUserConfig().getBoolean(
            GateConstants.DOCEDIT_INSERT_PREPEND).booleanValue();
    long s = start.longValue(), e = end.longValue();
    long rlen = // length of the replacement value
    ((replacement == null) ? 0 : replacement.size().longValue());
    // update the offsets and the index by offset for the rest of the nodes
    List nodesAfterReplacement = new ArrayList(nodesByOffset.tailMap(start)
            .values());
    // remove from the index by offset
    Iterator nodesAfterReplacementIter = nodesAfterReplacement.iterator();
    while(nodesAfterReplacementIter.hasNext()) {
      NodeImpl n = (NodeImpl)nodesAfterReplacementIter.next();
      nodesByOffset.remove(n.getOffset());
    }
    // change the offsets
    nodesAfterReplacementIter = nodesAfterReplacement.iterator();
    while(nodesAfterReplacementIter.hasNext()) {
      NodeImpl n = (NodeImpl)nodesAfterReplacementIter.next();
      long oldOffset = n.getOffset().longValue();
      // by default we move all nodes back
      long newOffset = oldOffset - (e - s) + rlen;
      // for the first node we need behave differently
      if(oldOffset == s) {
        // the first offset never moves back
        if(newOffset < s) newOffset = s;
        // if we're prepending we don't move forward
        if(shouldPrepend) newOffset = s;
      }
      n.setOffset(new Long(newOffset));
    }
    // add back to the index by offset with the new offsets
    nodesAfterReplacementIter = nodesAfterReplacement.iterator();
    while(nodesAfterReplacementIter.hasNext()) {
      NodeImpl n = (NodeImpl)nodesAfterReplacementIter.next();
      nodesByOffset.put(n.getOffset(), n);
    }
    // //rebuild the indices with the new offsets
    // nodesByOffset = null;
    // annotsByStartNode = null;
    // annotsByEndNode = null;
    // indexByStartOffset();
    // indexByEndOffset();
  } // edit(start,end,replacement)

  /** Get the name of this set. */
  public String getName() {
    return name;
  }

  /** Get the document this set is attached to. */
  public Document getDocument() {
    return doc;
  }

  /**
   * Get a set of java.lang.String objects representing all the annotation types
   * present in this annotation set.
   */
  public Set<String> getAllTypes() {
    indexByType();
    return annotsByType.keySet();
  }

  /**
   * Returns a set of annotations starting at that position This intermediate
   * method is used to simplify the code as the values of the annotsByStartNode
   * hashmap can be Annotations or a Collection of Annotations. Returns null if
   * there are no Annotations at that position
   */
  private final Collection<Annotation> getAnnotsByStartNode(Integer id) {
    Object objFromPoint = annotsByStartNode.get(id);
    if(objFromPoint == null) return null;
    if(objFromPoint instanceof Annotation) {
      List<Annotation> al = new ArrayList<Annotation>(2);
      al.add((Annotation)objFromPoint);
      return al;
    }
    // it is already a collection
    // return it
    return (Collection<Annotation>)objFromPoint;
  }

  /**
   * 
   * @return a clone of this set.
   * @throws CloneNotSupportedException
   */
  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  public synchronized void removeAnnotationSetListener(AnnotationSetListener l) {
    if(annotationSetListeners != null && annotationSetListeners.contains(l)) {
      Vector v = (Vector)annotationSetListeners.clone();
      v.removeElement(l);
      annotationSetListeners = v;
    }
  }

  public synchronized void addAnnotationSetListener(AnnotationSetListener l) {
    Vector v = annotationSetListeners == null
            ? new Vector(2)
            : (Vector)annotationSetListeners.clone();
    if(!v.contains(l)) {
      v.addElement(l);
      annotationSetListeners = v;
    }
  }

  protected void fireAnnotationAdded(AnnotationSetEvent e) {
    if(annotationSetListeners != null) {
      Vector listeners = annotationSetListeners;
      int count = listeners.size();
      for(int i = 0; i < count; i++) {
        ((AnnotationSetListener)listeners.elementAt(i)).annotationAdded(e);
      }
    }
  }

  protected void fireAnnotationRemoved(AnnotationSetEvent e) {
    if(annotationSetListeners != null) {
      Vector listeners = annotationSetListeners;
      int count = listeners.size();
      for(int i = 0; i < count; i++) {
        ((AnnotationSetListener)listeners.elementAt(i)).annotationRemoved(e);
      }
    }
  }

  public synchronized void removeGateListener(GateListener l) {
    if(gateListeners != null && gateListeners.contains(l)) {
      Vector v = (Vector)gateListeners.clone();
      v.removeElement(l);
      gateListeners = v;
    }
  }

  public synchronized void addGateListener(GateListener l) {
    Vector v = gateListeners == null ? new Vector(2) : (Vector)gateListeners
            .clone();
    if(!v.contains(l)) {
      v.addElement(l);
      gateListeners = v;
    }
  }

  protected void fireGateEvent(GateEvent e) {
    if(gateListeners != null) {
      Vector listeners = gateListeners;
      int count = listeners.size();
      for(int i = 0; i < count; i++) {
        ((GateListener)listeners.elementAt(i)).processGateEvent(e);
      }
    }
  }

  // how to serialize this object?
  // there is no need to serialize the indices
  // so it's probably as fast to just recreate them
  // if required
  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    ObjectOutputStream.PutField pf = out.putFields();
    pf.put("name", this.name);
    pf.put("doc", this.doc);
//    
//    out.writeObject(this.name);
//    out.writeObject(this.doc);
    // save only the annotations
    // in an array that will prevent the need for casting
    // when deserializing
    annotations = new Annotation[this.annotsById.size()];
    annotations = this.annotsById.values().toArray(annotations);
//    out.writeObject(annotations);
    pf.put("annotations", this.annotations);
    out.writeFields();
    annotations = null;
    boolean isIndexedByType = (this.annotsByType != null);
    boolean isIndexedByStartNode = (this.annotsByStartNode != null);
    out.writeBoolean(isIndexedByType);
    out.writeBoolean(isIndexedByStartNode);
  }

  private void readObject(java.io.ObjectInputStream in) throws IOException,
          ClassNotFoundException {
    ObjectInputStream.GetField gf = in.readFields();
    this.name = (String)gf.get("name", null);
    this.doc = (DocumentImpl)gf.get("doc", null);
    boolean isIndexedByType = false;
    boolean isIndexedByStartNode = false;
    this.annotations = (Annotation[])gf.get("annotations", null);
    if(this.annotations == null){
      //old style serialised version
      Map<Integer, Annotation> annotsByIdMap = (Map<Integer, Annotation>)
        gf.get("annotsById", null);
      if(annotsByIdMap == null) throw new IOException(
              "Invalid serialised data: neither annotations array or map by id" +
              " are present.");
      annotations = annotsByIdMap.values().toArray(new Annotation[]{});
    }else{
      //new style serialised version
      isIndexedByType = in.readBoolean();
      isIndexedByStartNode = in.readBoolean();
    }
    
    
//    this.name = (String)in.readObject();
//    this.doc = (DocumentImpl)in.readObject();
//    Annotation[] annotations = (Annotation[])in.readObject();
    // do we need to create the indices?
//    boolean isIndexedByType = in.readBoolean();
//    boolean isIndexedByStartNode = in.readBoolean();
    this.annotsById = new HashMap<Integer, Annotation>(annotations.length);
    // rebuilds the indices if required
    if(isIndexedByType) {
      annotsByType = new HashMap<String, AnnotationSet>(Gate.HASH_STH_SIZE);
    }
    if(isIndexedByStartNode) {
      nodesByOffset = new RBTreeMap();
      annotsByStartNode = new HashMap<Integer, Object>(annotations.length);
    }
    // add all the annotations one by one
    for (int i=0;i<annotations.length;i++) {
      add(annotations[i]);
    }
    annotations = null;
  }
} // AnnotationSetImpl
