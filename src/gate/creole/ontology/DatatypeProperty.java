/*
 * DatatypeProperty.java
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
 * Interface for datatype properties. Datatype properties have as range values
 * datatype values (different from object properties which have instances as
 * values). Values are Java objects.
 */
public interface DatatypeProperty extends Property {
  /**
   * 
   * @param value
   * @return true if this value is compatible with the range restrictions on the
   *         property. False otherwise.
   */
  public boolean isValidRange(Object value);
}