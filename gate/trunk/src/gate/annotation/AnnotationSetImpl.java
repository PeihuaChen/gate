/*
	AnnotationSet.java

	Hamish Cunningham, 7/Feb/2000

  

every set to which annotation will be added has to have positional
indexing, so that we can find or create the nodes on the new annotations

note that annotations added anywhere other than sets that are
stored on the document will not get stored anywhere...

nodes aren't doing anything useful now. needs some interface that allows
their creation, defaulting to no coterminous duplicates, but allowing such
if required


	$Id$
*/

package gate.annotation;

import java.util.*;
import gate.util.*;
import gate.*;


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
  * There are four indices: annotation by id, annotations by type, annotations
  * by node and nodes by offset. The last two jointly provide positional
  * indexing; construction of these is triggered by indexByOffset(), or by
  * calling a get method that selects on offset. The type
  * index is triggered by indexByType(), or calling a get method that selects
  * on type. The id index is always present.
  */
public class AnnotationSetImpl
extends AbstractSet
implements AnnotationSet
{
  /** Construction from Document. */
  public AnnotationSetImpl(Document doc) {
    annotsById = new HashMap();
    this.doc = doc;
  } // construction from document

  /** Construction from Collection (which must be an AnnotationSet */
  public AnnotationSetImpl(Collection c) throws ClassCastException {
    this(((AnnotationSet) c).getDocument());
    addAll(c);
  } // construction from collection

  /** This inner class serves as the return value from the iterator()
    * method.
    */
  private class AnnotationSetIterator implements Iterator {
    private Iterator iter;
    private Annotation lastNext = null;
    AnnotationSetIterator()  { iter = annotsById.values().iterator(); }
    public boolean hasNext() { return iter.hasNext(); }
    public Object next()     { return (lastNext = (Annotation) iter.next()); }
    public void remove()     { AnnotationSetImpl.this.remove(lastNext); }
  }; // AnnotationSetIterator

  /** Get an iterator for this set */
  public Iterator iterator() { return new AnnotationSetIterator(); }

  /** Remove an element from this set. */
  public boolean remove(Object o) throws ClassCastException {
    Annotation a = (Annotation) o;
    if(annotsById.remove(a.getId()) == null)
      return false;

    if(annotsByType != null) {
      AnnotationSet sameType = (AnnotationSet) annotsByType.get(a.getType());
      if(sameType != null) sameType.remove(a);
    }
    if(nodesByOffset != null) {
// knowing when a node is no longer needed would require keeping a reference
// count on annotations, or using a weak reference to the nodes in
// nodesByOffset
    }
    if(annotsByStartNode != null) {
      Integer id = a.getStartNode().getId();
      AnnotationSet starterAnnots = (AnnotationSet) annotsByStartNode.get(id);
      starterAnnots.remove(a);
      if(starterAnnots.isEmpty())
        annotsByStartNode.remove(id);
/*
      Node startNode = a.getStartNode();
      AnnotationSet thisNodeAnnots =
        (AnnotationSet) annotsByStartNode.get(startNode.getId());
      if(thisNodeAnnots != null)
        thisNodeAnnots.remove(a);
*/
    }

    return true;
  } // remove(o)

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
    else
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
    else
      return resultSet;
  } // get(type, constraints)

  /** Select annotations by offset. This returns the set of annotations
    * whose start node is the least such that it is less than or equal
    * to offset. If a positional index doesn't exist it is created.
    */
  public AnnotationSet get(Long offset) {
    if(annotsByStartNode == null) indexByOffset();

    // find the next node at or after offset; get the annots starting there
    Node nextNode = (Node) nodesByOffset.getNextOf(offset);
/*
SortedSet s = new TreeSet();
s.addAll(nodesByOffset.values());
System.out.println(s);
System.out.println(annotsByStartNode);
System.out.println("");
System.out.println(nodesByOffset);
System.out.println("");
System.out.println(annotsById);
System.out.println("");
Integer id = nextNode.getId();
// the next call won't trace into, and returns a null set when
// id = 1 (offset = 10)

// node with id 6 has offset 0!!!!!

AnnotationSet nextAnnots1 = (AnnotationSet) annotsByStartNode.get(id);
*/

    AnnotationSet nextAnnots =
      (AnnotationSet) annotsByStartNode.get(nextNode.getId());
    return nextAnnots;
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

  /** Add an existing annotation */
  public boolean add(Object o) throws ClassCastException {
    Annotation a = (Annotation) o;
    Object oldValue = annotsById.put(a.getId(), a);
    addToTypeIndex(a);
    addToOffsetIndex(a);
    return oldValue != a;
  } // add(o)

  /** Add an annotation and return its id */
  public Integer add(
    Long start, Long end, String type, FeatureMap features
  ) {
    // the set has to be indexed by position in order to add, as we need
    // to find out if nodes need creating or if they exist already
    if(nodesByOffset == null) indexByOffset();

    // find existing nodes
    Node startNode  = (Node) nodesByOffset.getNextOf(start);
    Node endNode    = (Node) nodesByOffset.getNextOf(end);

    // if appropriate nodes don't already exist, create them
    if(startNode == null || ! startNode.getOffset().equals(start))
      startNode = new NodeImpl(new Integer(nextNodeId++), start);
    if(endNode == null   || ! endNode.getOffset().equals(end))
      endNode = new NodeImpl(new Integer(nextNodeId++), end);

    // construct an annotation
    Integer id = new Integer(nextAnnotationId++);
    Annotation a = new AnnotationImpl(id, startNode, endNode, type, features);

    // add to the id index
    annotsById.put(id, a);

    // add to the type index
    addToTypeIndex(a);

    // add to the node index
    addToOffsetIndex(a);

    return id;
  } // add(start, end, type, features)

  /** Construct the positional index. */
  public void indexByType() {
    if(annotsByType != null) return;

    annotsByType = new HashMap();
    Annotation a;
    Iterator annotIter = annotsById.values().iterator();
    while(annotIter.hasNext())
      addToTypeIndex( (Annotation) annotIter.next() );
  } // indexByType()

  /** Construct the positional indices (nodes by offset and annotations
    * by node).
    */
  public void indexByOffset() {
    if(nodesByOffset != null) return;

    nodesByOffset = new RBTreeMap();
    annotsByStartNode = new HashMap();

    Annotation a;
    Iterator annotIter = annotsById.values().iterator();
    while(annotIter.hasNext())
      addToOffsetIndex( (Annotation) annotIter.next() );
  } // indexByOffset()

  /** Add an annotation to the type index. Does nothing if the index
    * doesn't exist.
    */
  private void addToTypeIndex(Annotation a) {
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
  private void addToOffsetIndex(Annotation a) {
    if(annotsByStartNode == null) return;

    Node startNode  = a.getStartNode();
    Node endNode    = a.getEndNode();
    Long start      = startNode.getOffset();
    Long end        = endNode.getOffset();

    // get the annotations that start at the same node, or create new set
    AnnotationSet thisNodeAnnots =
      (AnnotationSet) annotsByStartNode.get(startNode.getId());
    if(thisNodeAnnots == null) {
      thisNodeAnnots = new AnnotationSetImpl(doc);
      annotsByStartNode.put(startNode.getId(), thisNodeAnnots);
    }
    // add to the annots listed for a's start node
    thisNodeAnnots.add(a);

    // add a's nodes to the offset indices
    nodesByOffset.put(start, startNode);
    nodesByOffset.put(end, endNode);
  } // addToOffsetIndex(a)

  /** Get the name of this set. */
  public String getName() { return name; }

  /** Get the document this set is attached to. */
  public Document getDocument() { return doc; }

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
      HashMap thisType = iter.next()entrySet();
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
  private String name = null;

  /** The document this set belongs to */
  private Document doc;

  /** Maps longs (ids) to Annotations */
  private HashMap annotsById;

  /** Maps strings (types) to AnnotationSets */
  private HashMap annotsByType = null;

  /** Maps offsets to nodes */
  private RBTreeMap nodesByOffset;

  /** Maps annotations to nodes */
  private HashMap annotsByStartNode;



// these should move into Document
  /** The id of the next new annotation */
  private int nextAnnotationId = 0;

  /** The id of the next new node */
  private int nextNodeId = 0;

} // AnnotationSetImpl
