/*
 *	Annotation.java
 *
 *	Hamish Cunningham, 19/Jan/00
 *
 *	$Id$
 */

package gate;

import gate.util.*;

/** An Annotation is an arc in an AnnotationGraph. It is immutable, to avoid
  * the situation where each annotation has to point to its parent graph in
  * order to tell it to update its indices when it changes.
  * <P> Changes from TIPSTER: no ID; single span only.
  */
public interface Annotation extends FeatureBearer {

  /** The type of the annotation (corresponds to TIPSTER "name"). */
  public String getType();

  /** The equivalence class of this annotation. */
  public String getEquivalenceClass();

  /** The start node. */
  public Node getStartNode();

  /** The end node. */
  public Node getEndNode();

  /** The id of this annotation. */
  public Long getId();

} // interface Annotation
