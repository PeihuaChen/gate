/*
 * KBProperty.java
 *
 * Copyright (c) 2002, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * Kalina Bontcheva 11/2003
 *
 *
 *  $Id$
 */

package gate.creole.ontology;


/**
 * A property, P, is tagged as functional if it
satisfies the following axiom: P(x, y) and P(x, z) -> y = z.
 * The range of a functional property can be both
 * an object (as in DatatypeProperty) and KBClass
 * (as in ObjectProperty).
 */
public interface KBFunctionalProperty extends KBProperty {
  public Object getRange();
}