/*
	AnnotationSet.java

	Hamish Cunningham, 7/Feb/2000

	$Id$
*/

package gate;
import java.util.*;
import gate.util.*;

/** Annotation sets */
public interface AnnotationSet extends Set
{
  /** Create and add an annotation from database read data
    * In this case the id is already known being previously fetched from the
    * database*/
  public void add(
    Integer id, Long start, Long end, String type, FeatureMap features
  ) throws InvalidOffsetException;

  /** Create and add an annotation and return its id */
  public Integer add(Long start, Long end, String type, FeatureMap features)
    throws InvalidOffsetException;
  
  /** Add an existing annotation. Returns true when the set is modified. */
  public boolean add(Object o);

  /** Get an iterator for this set */
  public Iterator iterator();

  /** The size of this set */
  public int size();

  /** Remove an element from this set. */
  public boolean remove(Object o);

  /** Find annotations by id */
  public Annotation    get(Integer id);

  /** Get all annotations */
  public AnnotationSet get();

  /** Select annotations by type */
  public AnnotationSet get(String type);

  /** Select annotations by a set of types. Expects a Set of String. */
  public AnnotationSet get(Set types);

  /** Select annotations by type and features */
  public AnnotationSet get(String type, FeatureMap constraints);
  
  /** Select annotations by type, features and offset */
  public AnnotationSet get(String type, FeatureMap constraints, Long offset);
  
  /** Select annotations by offset. This returns the set of annotations
    * whose start node is the least such that it is less than or equal
    * to offset. If a positional index doesn't exist it is created.
    */
  public AnnotationSet get(Long offset);

  /** Get the node with the smallest offset */
  public Node firstNode();

  /** Get the node with the largest offset */
  public Node lastNode();

  /** Get the name of this set. */
  public String getName();

  /** Get the document this set is attached to. */
  public Document getDocument();

} // interface AnnotationGraph
