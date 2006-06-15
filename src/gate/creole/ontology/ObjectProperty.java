/*
 * ObjectProperty.java
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

import java.util.Set;

public interface ObjectProperty extends Property {
  /**
   * Returns the set of inverse properties for this property. Null if no such
   * properties. The set contains objects of KBProperty instances.
   * 
   * @return a {@link Set} value.
   */
  public Set getInverseProperties();

  /**
   * Set theInverse as inverse property to this property.
   * 
   * @param theInverse
   */
  public void setInverseOf(Property theInverse);

  /**
   * @param instance
   * @return true if this value is compatible with the range restrictions on the
   *         property. False otherwise.
   */
  public boolean isValidRange(OInstance instance);
}