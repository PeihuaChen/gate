/*
 * KBObjectProperty.java
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

public interface KBObjectProperty extends KBProperty {

  /**
   * Returns the range of the ObjectProperty which is always a
   * KBClass.
   * @return
   */
  public KBClass getRange();

  /**
   * Returns the set of inverse properties for this property. Null if no
   * such properties. The set contains objects of KBProperty instances.
   * @return
   */
  public Set getInverseProperties();

  /**
   * Set theInverse as inverse property to this property.
   * @param theInverse
   */
  public void setInverseOf(KBProperty theInverse);
}