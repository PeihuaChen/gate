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
  public Integer add(Long start, Long end, String type, FeatureMap features);
  // public Integer add(Node startNode, Node endNode, String type, FeatureMap features);
  public boolean add(Object o);

  public Iterator iterator();
  public int size();

  public boolean remove(Object o);

  public Annotation get(Integer id);
  public AnnotationSet get(String type);
  public AnnotationSet get(Set types);
  public AnnotationSet get(String type, FeatureMap constraints);
  public AnnotationSet get(String type, FeatureMap constraints, Long offset);

  public void indexByType();
  public void indexByOffset();

  /** Get the name of this set. */
  public String getName();

  /** Get the document this set is attached to. */
  public Document getDocument();
  
} // interface AnnotationGraph
