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
public interface Annotation {

  /** The type of the annotation (corresponds to TIPSTER "name"). */
  public String getType();

  /** The features, or content of this arc (corresponds to TIPSTER
    * "attributes", and to LDC "label", which is the simplest case).
    */
  public FeatureSet getFeatures();

  /** The equivalence class of this annotation. */
  public String getEquivalenceClass();

//  /** The start of the span. */
//  public Long getStart();
//
//  /** The end of the span. */
//  public Long getEnd();

  /** The start node. */
  public Node getStartNode();

  /** The end node. */
  public Node getEndNode();

  /** The stereotype associated with this annotation. */
  public AnnotationStereotype getStereotype();

//  /** Does this annotation structurally include a? */
//  public boolean sIncludes(Annotation a);
//
//  /** Does this annotation temporally (i.e. by offset) include a? */
//  public boolean tIncludes(Annotation a);
//
//  /** Does this annotation include a? */
//  public boolean includes(Annotation a);

} // interface Annotation
