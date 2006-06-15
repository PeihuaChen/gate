/*
 * Property.java
 *
 * Copyright (c) 2005, The University of Sheffield.
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

/**
 * This interface defines an ontology property and is the top level interface
 * for all types of ontological properties.
 */
public interface Property extends OntologyResource, OntologyConstants {
  /**
   * Add a samePropertyAs relation between the two properties. Each property has
   * a set of these, so it is possible to have samePropertyAs relation between
   * more than two properties.
   * 
   * @param theProperty
   */
  public void setSamePropertyAs(Property theProperty);

  /**
   * Returns a set of all KBProperty instances that are in SamePropertyAs
   * relation with this property. Or null if there are no such properties.
   * 
   * @return a {@link Set} value.
   */
  public Set getSamePropertyAs();

  /**
   * Adds a SubPropertyOf relation between the given property and this.
   * 
   * @param property
   */
  public void addSuperProperty(Property property);

  /**
   * Removes a SubPropertyOf relation between the given property and this.
   * 
   * @param property
   */
  public void removeSuperProperty(Property property);

  /**
   * Add a SuperPropertyOf relation between the given property and this.
   * 
   * @param property
   */
  public void addSubProperty(Property property);

  /**
   * Removes a SuperPropertyOf relation between the given property and this.
   * 
   * @param property
   */
  public void removeSubProperty(Property property);

  /**
   * Returns the set of domain restrictions for this property.
   */
  public Set getDomain();

  /**
   * Gets the set of range restrictions for this property. If no range has been
   * set it returns an empty set.
   * 
   * @return a set of {@link OClass} or {@link Class} objects.
   */
  public Set getRange();

  /**
   * Checks whether this property can apply to the provided instance
   * 
   * @param instance
   *          the instance
   * @return <tt>true</tt> if the property is valid for the instance.
   */
  public boolean isValidDomain(OntologyResource instance);

  /**
   * 
   * @param value
   * @return true if this value is compatible with the range restrictions on the
   *         property. False otherwise.
   */
  public boolean isValidRange(Object value);

  /**
   * Answers whether this property is a functional property. Functional
   * properties are the ones that can have at most one value for any given value
   * from the domain. Both object properties and datatype properties can be
   * functional.
   * 
   * @return <tt>true</tt> if this property is functional.
   */
  public boolean isFunctional();

  /**
   * Sets the functional property flag on this property.
   * 
   * @param functional
   *          <tt>true</tt> iff the property should be marked as functional.
   */
  public void setFunctional(boolean functional);

  /**
   * Answers whether this property is an inverse functional property. Inverse
   * functional properties are the ones that for any given domain value there
   * can be at most one range value that is valid for this property. Both object
   * properties and datatype properties can be inverse functional.
   * 
   * @return <tt>true</tt> if this property is inverse functional.
   */
  public boolean isInverseFunctional();

  /**
   * Sets the inverse functional property flag on this property.
   * 
   * @param inverseFunctional
   *          <tt>true</tt> iff the property should be marked as inverse
   *          functional.
   */
  public void setInverseFunctional(boolean inverseFunctional);

  /**
   * Gets the set of super-properties for this property.
   * 
   * @param {@link OntologyConstants#DIRECT_CLOSURE}
   *          for direct super-properties only or
   *          {@link OntologyConstants#TRANSITIVE_CLOSURE} for all the
   *          super-properties.
   * @return a set of {@link Property} values.
   */
  public Set getSuperProperties(byte closure);

  /**
   * Gets the set of sub-properties for this property.
   * 
   * @param {@link OntologyConstants#DIRECT_CLOSURE}
   *          for direct sub-properties only or
   *          {@link OntologyConstants#TRANSITIVE_CLOSURE} for all the
   *          sub-properties.
   * @return a set of {@link Property} values.
   */
  public Set getSubProperties(byte closure);
}