/*
 *  SimpleAnnotationSet.java
 *
 *  Copyright (c) 1998-2005, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 23/Jul/2004
 *
 *  $Id$
 */

package gate;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

import gate.util.InvalidOffsetException;

/** Annotation sets */
public interface SimpleAnnotationSet extends Set, Cloneable, Serializable
{
  /** Create and add an annotation with pre-existing nodes,
    * and return its id
    */
  public Integer add(Node start, Node end, String type, FeatureMap features);

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

  /** Get the name of this set. */
  public String getName();

  /** Get a set of java.lang.String objects representing all the annotation
    * types present in this annotation set.
    */
  public Set getAllTypes();

  /** Get the document this set is attached to. */
  public Document getDocument();

} // interface SimpleAnnotationSet
