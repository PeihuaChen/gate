/*
 * OInstance.java
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
 * Kalina Bontcheva 03/2003
 *
 */

package gate.creole.ontology;

public interface OInstance {

  /** Gets the class of this instance.
   *  @return the class
   */
  public OClass getOClass();

  /** Gets the name of this instance.
   *  @return the name
   */
  public String getName();

  /** Sets the user data of this instance. To be used to
   * store arbitrary data on instances.
   */
  public void setUserData(Object theUserData);

  /** Gets the user data of this instance.
   *  @return the object which is user data
   */
  public Object getUserData();
}