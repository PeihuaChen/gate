/*
 *  Annotation.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 19/Jan/00
 *
 *  $Id$
 */

package gate;

import gate.util.*;

/** An Annotation is an arc in an AnnotationGraph. It is immutable, to avoid
  * the situation where each annotation has to point to its parent graph in
  * order to tell it to update its indices when it changes.
  * <P> Changes from TIPSTER: no ID; single span only.
  */
public interface Annotation extends FeatureBearer, IdBearer, Comparable {

  /** The type of the annotation (corresponds to TIPSTER "name"). */
  public String getType();

  /** The start node. */
  public Node getStartNode();

  /** The end node. */
  public Node getEndNode();

  /** Ordering */
  public int compareTo(Object o) throws ClassCastException;

  /** This verifies if <b>this</b> annotation is compatible with another one.
    * Compatible means that they hit the same enity and the FeatureMap of
    * <b>this</b> is incuded into aAnnot FeatureMap.
    * @param aAnnot a gate Annotation.
    * @return <code>true</code> if aAnnot is compatible with <b>this</> and
    * <code>false</code> otherwise.
    */
  public boolean isCompatible(Annotation aAnnot);

  /** This method verifies if two annotation and are partially compatible.
    * Partially compatible means that they overlap and the FeatureMap of
    * <b>this</b> is incuded into FeatureMap of aAnnot.
    * @param aAnnot a gate Annotation.
    * @return <code>true</code> if <b>this</b> is partially compatible with
    * aAnnot and <code>false</code> otherwise.
    */
  public boolean isPartiallyCompatible(Annotation aAnnot);

  /**  Two Annotation are coestensive if their offsets are the same.
    *  @param anAnnot A Gate annotation.
    *  @return <code>true</code> if two annotation hit the same possition and
    *  <code>false</code> otherwise
    */
  public boolean coextensive(Annotation anAnnot);

  /** This method tells if <b>this</b> overlaps aAnnot.
    * @param aAnnot a gate Annotation.
    * @return <code>true</code> if they overlap and <code>false</code> false if
    * they don't.
    */
  public boolean overlaps(Annotation aAnnot);



} // interface Annotation,
