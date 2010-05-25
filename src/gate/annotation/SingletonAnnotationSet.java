package gate.annotation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.FeatureMap;
import gate.Node;
import gate.corpora.DocumentImpl;
import gate.event.AnnotationSetEvent;
import gate.event.AnnotationSetListener;
import gate.event.GateEvent;
import gate.event.GateListener;
import gate.util.InvalidOffsetException;

/**
 * Immutable AnnotationSet containing only one Annotation but exposing
 * it through the generic AnnotationSet interface. This is meant to be used 
 * primarily by the JAPE bindings which create temporary AnnotationSets
 * usually containing only one Annotation. This way we can avoid the temporary creation 
 * of HashMaps, HashMap.entries, and class[] which are triggered by the creation of a full fledged 
 * AnnotationSetImpl.
 * This object being immutable, an exception is thrown when trying to modify it.
 */

public class SingletonAnnotationSet implements AnnotationSet {

  private Annotation annotation;
    
  //Empty AnnotationSet to be returned instead of null
  public static AnnotationSet emptyAnnotationSet;
 
  static {
  emptyAnnotationSet = new ImmutableAnnotationSetImpl(null,null);
  }
  
  public SingletonAnnotationSet(Annotation a){
    annotation = a;
  }

  public AnnotationSet get(String type) {
    if (type.equals(annotation.getType())) return new SingletonAnnotationSet(annotation);
    return emptyAnnotationSet;
  }
  
  public Annotation get(Integer id) {
    if (id.equals(annotation.getId())) return annotation;
    return null;
  }

  public AnnotationSet get() {
    return new SingletonAnnotationSet(annotation);
  }

  public AnnotationSet get(Set<String> types) {
    // check that the type of the annotation is in the list
    if (types.contains(annotation.getType())) return new SingletonAnnotationSet(annotation);
    return emptyAnnotationSet;
  }
  
  public AnnotationSet get(String type, FeatureMap constraints) {
    if (type.equals(annotation.getType())==false) return emptyAnnotationSet;
    if(annotation.getFeatures().subsumes(constraints)==false) return emptyAnnotationSet;
    return new SingletonAnnotationSet(annotation);
  }

  public AnnotationSet get(String type, Set featureNames) {    
    if (type.equals(annotation.getType())==false) return emptyAnnotationSet;
    if (annotation.getFeatures().keySet().containsAll(featureNames)==false) return emptyAnnotationSet;
    return new SingletonAnnotationSet(annotation);
  }

  public AnnotationSet get(String type, FeatureMap constraints, Long offset) {
    long start = annotation.getStartNode().getOffset().longValue();
    if (start<offset.longValue()) return emptyAnnotationSet;
    return get(type, constraints);
  }

  public AnnotationSet get(Long offset) {
    long start = annotation.getStartNode().getOffset().longValue();
    if (start>=offset.longValue())
      return new SingletonAnnotationSet(annotation);
    return emptyAnnotationSet;
  }

  public AnnotationSet get(Long startOffset, Long endOffset) {
    // must start or end between the two offsets
    long start = annotation.getStartNode().getOffset().longValue();
    long end = annotation.getEndNode().getOffset().longValue();
    boolean ok = false;
    if ((start>=startOffset.longValue()) & (start<=endOffset.longValue())) ok=true;
    else if ((end>=startOffset.longValue()) & (end<=endOffset.longValue())) ok=true;
    if (ok) return new SingletonAnnotationSet(annotation);
    return emptyAnnotationSet;
  }

  public AnnotationSet get(String type, Long startOffset, Long endOffset) {
    if (type.equals(annotation.getType())==false) return emptyAnnotationSet;
    return get(startOffset,  endOffset);
  }

  public AnnotationSet getContained(Long startOffset, Long endOffset) {
    long start = annotation.getStartNode().getOffset().longValue();
    long end = annotation.getEndNode().getOffset().longValue();
    if ((start>=startOffset.longValue()) && (end<=endOffset.longValue())) 
      return new SingletonAnnotationSet(annotation);
    return emptyAnnotationSet;
  }
  
  

  public AnnotationSet getCovering(String neededType, Long startOffset,
          Long endOffset) {
    if(annotation.getType().equals(neededType) &&
       annotation.getStartNode().getOffset()<= startOffset &&
       annotation.getEndNode().getOffset() >= endOffset)
      return new SingletonAnnotationSet(annotation);
    return emptyAnnotationSet;
  }

  public Set<String> getAllTypes() {
    HashSet set  = new HashSet();
    set.add(annotation.getType());
    return set;
  }

  public Document getDocument() {
    return null;
  }

  public String getName() {
    return null;
  }

  public Iterator<Annotation> iterator() {
    return new SingletonAnnotationSetIterator();
  }

  public boolean contains(Object arg0) {
    if (((Annotation)arg0).equals(annotation)) return true;
    return false;
  }

  public boolean containsAll(Collection<?> arg0) {
    if (arg0.size()>1) return false;
    if (arg0.size()==0) return true;
    Iterator iter = arg0.iterator();
    return contains(iter.next());
  }

  public boolean isEmpty() {
    return false;
  }

  public Object[] toArray() {
    return new Annotation[]{annotation};
  }

  public <T> T[] toArray(T[] arg0) {
    Annotation[] table = new Annotation[]{annotation};
    return (T[])table;
  }

  public Node firstNode() {
    return annotation.getStartNode();
  }
  
  public Node lastNode() {
    return annotation.getEndNode();
  }

  /**
   * Get the first node that is relevant for this annotation set and which has
   * the offset larger than the one of the node provided.
   */
  public Node nextNode(Node node) {
    if (node.getOffset().longValue()<annotation.getStartNode().getOffset().longValue()){
     return annotation.getStartNode();
    }
    if (node.getOffset().longValue()<annotation.getEndNode().getOffset().longValue()){
      return annotation.getEndNode();
     }
    return null;
  }

  public synchronized void removeGateListener(GateListener l) {
   // do nothing
  }
  
  public synchronized void removeAnnotationSetListener(AnnotationSetListener l) {
   // do nothing
  }

  public void add(Integer id, Long start, Long end, String type,
          FeatureMap features) throws InvalidOffsetException {
    throw new UnsupportedOperationException();
  }

  public void addAnnotationSetListener(AnnotationSetListener l) {
    throw new UnsupportedOperationException();
  }

  public void addGateListener(GateListener l) {
    throw new UnsupportedOperationException();
  }

  public Integer add(Node start, Node end, String type, FeatureMap features) {
    throw new UnsupportedOperationException();
  }

  public Integer add(Long start, Long end, String type, FeatureMap features)
          throws InvalidOffsetException {
    throw new UnsupportedOperationException();
  }

  public boolean add(Annotation a) {
    throw new UnsupportedOperationException();
  }

  public boolean removeAll(Collection<?> arg0) {
    throw new UnsupportedOperationException();
  }

  public boolean retainAll(Collection<?> arg0) {
    throw new UnsupportedOperationException();
  }
  
  public boolean addAll(Collection<? extends Annotation> arg0) {
    throw new UnsupportedOperationException();
  }

  public boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  public int size() {
    return 1;
  }

  public void clear() {
    throw new UnsupportedOperationException();
  }
  
  /**
   * This inner class serves as the return value from the iterator() method.
   */
  class SingletonAnnotationSetIterator implements Iterator<Annotation> {
    private boolean available = true;

    public boolean hasNext() {
      return available;
    }

    public Annotation next() {
      available=false;
      return annotation;
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  };
  

}
