/*
 *  EventAwareAnnotationSet.java
 *
 *  Copyright (c) 1998-2005, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 02/Nov/2001
 *
 *
 *  $Id$
 */

package gate.annotation;

import java.util.Collection;

import gate.AnnotationSet;



public interface EventAwareAnnotationSet extends AnnotationSet {

  public Collection getAddedAnnotations();

  public Collection getChangedAnnotations();

  public Collection getRemovedAnnotations();

}