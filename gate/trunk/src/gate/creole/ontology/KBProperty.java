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

import java.util.Set;

public interface KBProperty {

  /**
   * @return the name of the property within the ontology's namespace
   */
  public String getName();

  /**
   * Returns the URI of this property.
   */
  public String getURI();

  /**
   * Sets the URI of the property
   * @param theURI
   */
  public void setURI(String theURI);

  /**
   * Add a samePropertyAs relation between the two properties.
   * Each property has a set of these, so it is possible to
   * have samePropertyAs relation between more than two properties.
   * @param theProperty
   */
  public void setSamePropertyAs(KBProperty theProperty);

  /**
   * Returns a set of all KBProperty instances that are in
   * SamePropertyAs relation with this property. Or null if
   * there are no such properties.
   * @return a {@link Set} value.
   */
  public Set getSamePropertyAs();

  /**
   * Add a SubPropertyOf relation between the given property and this.
   * @param propertyName
   */
  public void setSubPropertyOf(String propertyName);

  /**
   * Return a set of all local names of properties that are in a
   * subPropertyOf relation with this property. Null if no
   * such properties. This is not a transitive closure. To obtain
   * the full depth of the property hierarchy, one needs then to
   * get the sub-properties of the sub-properties of this, etc.
   * @return a {@link Set} value.
   */
  public Set getSubPropertyOf();

  /**
   * Returns the domain of a property. There is no corresponding set
   * method, because the property is created at knowledge base level
   * by specifying its domain and range
   */
  public KBClass getDomain();

  /**
   *
   * @param value
   * @return true if this value is compatible with the range
   * restrictions on the property. False otherwise.
   */
  public boolean isValueCompatible(Object value);

  /**Gets the ontology to which the class belongs.
   * @return  the ontology to which the class belongs
   */
  public KnowledgeBase getOntology() ;

  public Object getRange();

}