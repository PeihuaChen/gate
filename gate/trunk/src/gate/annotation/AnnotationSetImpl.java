/*
	AnnotationSet.java

	Hamish Cunningham, 7/Feb/2000

	$Id$
*/

package gate.annotation;

import java.util.*;
import gate.util.*;
import gate.*;


/** Simple implementation of AnnotationSet */

/* to do: 
 * make nodes map null by default
 * annotations don't point to nodes, they just have offsets?
 */

public abstract class AnnotationSetImpl
extends AbstractSet
implements AnnotationSet
{
//  /** No-arg constructor */
//  public AnnotationSetImpl() {
//    annotsById = new HashMap();
//    annotsByType = new HashMap();
//  } // default construction
//
//  /** Construction from Collection */
//  public AnnotationSetImpl(Collection c) {
//    addAll(c);
//  } // construction from collection
//
//  /** Get an iterator for this set */
//  public Iterator iterator() {
//    return null;
///*
//    return new
//      AnnotationSetIterator() implements Iterator {
//        private Iterator iter;
//        private Annotation lastNext = null;
//
//        public Iterator() { iter = annotsById.iterator(); }
//
//        public boolean hasNext() { return iter.hasNext(); }
//
//        public Object next() {
//          lastNext = (Annotation) iter.next();
//          return lastNext;
//        }
//
//        public Object remove() {
//          AnnotationSet sameType = annotsByType.get(lastNext.getType());
//          if(sameType != null) sameType.remove(lastNext);
//          annotsById.remove(lastNext);
//          if(nodesMap != null)
//            // *************************************** remove from nodes map
//          return lastNext;
//        }
//      }; // AnnotationSetIterator
//*/
//  } // iterator()
//
//  /** Remove an element from this set. We override
//    * the default implementation (from AbstractSet)
//    * because it uses the iterator, and is
//    * therefore very inefficient in this case.
//    */
//  public boolean remove(Object o) {
//    Annotation a = (Annotation) o;
//    boolean wasPresent = false;
//
//    if(annotsById.remove(a) != null)
//      wasPresent = true;
//    AnnotationSet sameType = annotsByType.get(lastNext.getType());
//    if(sameType != null) sameType.remove(a);
//    if(nodesMap != null)
//      // *************************************** remove from nodes map
//    return wasPresent;
//  } // remove(o)
//
//  /** Add an Annotation to this set */
//  public boolean add(Object o) {
//    Annotation a = (Annotation) o;
//    
//    add(a.getStart(), a.getEnd(), a.getType(),
//        a.getEqClass(), a.getFeatures());
//
//  } // add(object)
//
//  /** The size of this set */
//  public int size() { return annotsById.size(); }
//
//  /** Find annotations by id */
//  public Annotation get(long id) {
//    return (Annotation) annotsById.get(id);
//  } // get(id)
//
//  /** Select annotations by type */
//  public AnnotationSet get(String type) {
//    return (AnnotationSet) annotsByType.get(type); 
//  } // get(type)
//
//  /** Select annotations by type and features */
//  public AnnotationSet get(String type, FeatureMap constraints) {
//    AnnotationSet typeSet = get(type);
//    AnnotationSet resultSet = new AnnotationSet();
//
//    Iterator iter = typeSet.iterator();
//    while(iter.hasNext()) {
//      Annotation a = (Annotation) iter.next();
//
//      // we check for matching constraints by simple equality. a
//      // feature map satisfies the constraints if it contains all the
//      // key/value pairs from the constraints map
//      if(a.getFeatures().containsAll(constraints))
//        resultSet.add(a);
//    } // while
//
//    // ???
//    // should we return null if there aren't any? we need a policy re.
//    // null vs. empty set return values, and probably do the same thing
//    // in all cases
//    return resultSet;
//  } // get(type, constraints)
//
//  /** Select annotations by type and equivalence class */
//  public AnnotationSet get(String type, String eqClass) {
//    // ???
//    // does this make sense?
//    return (AnnotationSet) annotsByEqClass.get(eqClass).get(type);
//  } // get(eqClass)
//
//  /** Select annotations by type, features and offset */
//  public AnnotationSet get(String type, FeatureMap constraints, long offset)
//  {
//    Node nearestNode = (Node) nodeMap.get(offset);
//
//    if(nearestNode == null)
//      return null;
//    else
//      return nearestNode.getStartingAnnotations().get(type, constraints);
//
//  } // get(type, constraints, offset)
//
//  /** Add an annotation and return its id */
//  public long add(
//    long start, long end, String type, String eqClass, FeatureMap features
//  ) {
//
//    // construct an annotation
//    long id = nextAnnotationId++;
//    Annotation a = new Annotation(id, start, end, type, eqClass, features);
//
//    // add to the id index
//    annotsById.add(id, a);
//
//    // add to the type index
//    AnnotationSet sameType = annots.ByType.get(type);
//    if(sameType == null) {
//      sameType = new AnnotationSet();
//      annotsByType.put(type, sameType);
//    }
//    sameType.add(a);
//
//    // add to the eq class index
//    AnnotationSet sameEqClass = annots.ByEqClass.get(eqClass);
//    if(sameEqClass == null) {
//      sameEqClass = new AnnotationSet();
//      annotsByEqClass.put(eqClass, sameEqClass);
//    }
//    sameEqClass.add(a);
//
//    // add to the nodes map, creating nodes where necessary
//    Node startNode = nodesMap.get(start);
//    Node endNode = nodesMap.get(end);
//
//    if(startNode == null || startNode.getOffset() != start)
//      startNode = new Node(start);
//    if(endNode == null || endNode.getOffset() != end)
//      endNode = new Node(end);
//
//    startNode.addStartingAnnotation(a);
//    endNode.addEndingAnnotation(a);
//
//    return id;
//
//  } // add(start, end, type, eqClass, features
//
//  /** The name of this set */
//  private String name = null;
//
//  /** The document this set belongs to */
//  private Document doc;
//
//  /** Maps offsets to nodes */
//  private RBTreeMap nodeMap;
//
//  /** Maps longs (ids) to Annotations */
//  private HashMap annotsById;
//
//  /** Maps strings (types) to AnnotationSets */
//  private HashMap annotsByType = null;
//
//  /** Maps strings (eq classes) to AnnotationSets.
//    * Perhaps we should do this with a special feature instead?
//    */
//  private HashMap annotsByEqClass;
//
//  /** The id of the next new annotation */
//  private long nextAnnotationId = 0;

} // AnnotationSetImpl
