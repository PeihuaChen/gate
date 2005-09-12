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
 * Kalina Bontcheva 11/2003
 *
 *
 *  $Id$
 */

package gate.creole.ontology;

import java.util.List;
import java.util.Set;

public interface OInstance{

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

  public void setDifferentFrom(OInstance theIndividual);

  public Set getDifferentFrom();

  /**
   * Adds a new property with the given name and value.
   * @param propertyName the name of the property
   * @param theValue the value for the property
   * @return <tt>true</tt> if the property name is valid for this type of 
   * instance and the new value has been added, <tt>false</tt> otherwise.
   */
  public boolean addPropertyValue(String propertyName, Object theValue);

  /**
   * Gets the list of values for a given property name.
   * @param propertyName the name of the property
   * @return a List of values.
   */
  public List getPropertyValues(String propertyName);
  
  /**
   * Removes one of the values for a given property.
   * @param propertyName the name of the property
   * @param theValue the value to be removed.
   * @return <tt>true</tt> if the value was foudn and removed, <tt>false</tt> 
   * otherwise.
   */
  public boolean removePropertyValue(String propertyName, Object theValue);
  
  /**
   * Removes all values for a named property.
   * @param propertyName the property name.
   */
  public void removePropertyValues(String propertyName);
  
  /**
   * Gets the names of the properties that have set values for this instance.
   * @return a set of String values.
   */
  public Set getSetPropertiesNames();
  public void setSameIndividualAs(OInstance theIndividual);

  public Set getSameIndividualAs();

}