/*
 * OClass.java
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
 *  $Id$*
 */
package gate.creole.ontology;

import java.util.Set;

public interface OClass extends TClass {
  /** Indicates that these are disjoint classes */
  public void setDisjointWith(OClass theClass);

  /** Indicates that these classes are the same */
  public void setSameClassAs(OClass theClass);

  /**
   * Returns a set of all classes that are disjoint with ours. Null if no such
   * classes.
   */
  public Set getDisjointClasses();

  /**
   * Returns a set of all classes that are the same as ours. Null if no such
   * classes.
   */
  public Set getSameClasses();
}