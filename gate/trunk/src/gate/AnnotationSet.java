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

  /** Get annotations by type */
  public AnnotationGraph getAnnotations(String type);

  /** Get annotations by type and features */
  public AnnotationGraph getAnnotations(String type, FeatureMap features);

  /** Get annotations by type and equivalence class */
  public AnnotationGraph getAnnotations(String type, String equivalenceClass);

  /** Get annotations by type and position. This is the set of annotations of
    * a particular type which share the smallest leastUpperBound that is >=
    * offset */
  public AnnotationGraph getAnnotations(String type, Long offset);

  /** Get annotations by type, features and offset */
  public AnnotationGraph getAnnotations(String type, FeatureMap features,
					Long offset);

  /** Get annotations by type, equivalence class and offset */
  public AnnotationGraph getAnnotations(String type, String equivalenceClass,
					Long offset);

  /**Creates a new node with the offset offset
  @param offset the offset in document where the node will point*/
  public Node putNodeAt(Long id,double offset)throws gate.util.InvalidOffsetException;
  /**Returns the Id of the annotation graph*/
  public Long getId();

  public Annotation newAnnotation(Long id, Node start, Node end, String type, String equivalenceClass);

  public Annotation newAnnotation(Long id,long start, long end, String type, String equivalenceClass);

} // interface AnnotationGraph
