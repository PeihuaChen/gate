/*
 * OntologyResource.java
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
 * Valentin Tablan 15-Sep-2005
 *
 *
 *  $Id$
 */
package gate.creole.ontology;

import java.util.List;
import java.util.Set;

/**
 * This is the top level interface for all ontology resources such as classes,
 * instances and properties.
 */
public interface OntologyResource {
  /**
   * Gets the URI of the resource.
   * 
   * @return the URI.
   */
  public String getURI();

  /**
   * Sets the URI of the resource.
   * 
   * @param theURI
   *          the new URI to be set
   */
  public void setURI(String theURI);

  /**
   * Gets the comment of the resource.
   * 
   * @return the comment of the resource
   */
  public String getComment();

  /**
   * Sets the resource comment.
   * 
   * @param aComment
   *          the comment to be set.
   */
  public void setComment(String aComment);

  /**
   * Gets resource name.
   * 
   * @return the name of the resource.
   */
  public String getName();

  /**
   * Sets the resource name.
   * 
   * @param aName
   *          the new name of the resource.
   */
  public void setName(String aName);

  /**
   * Gets the ontology to which the resource belongs.
   * 
   * @return the ontology to which the resource belongs
   */
  public Ontology getOntology();

  /**
   * Gets the taxonomy to which the resource belongs.
   * 
   * @return the taxonomy to which the resource belongs
   */
  public Taxonomy getTaxonomy();

  /**
   * Adds a new property with the given name and value.
   * 
   * @param propertyName
   *          the name of the property
   * @param theValue
   *          the value for the property
   * @return <tt>true</tt> if the property name is valid for this type of
   *         instance and the new value has been added, <tt>false</tt>
   *         otherwise.
   */
  public boolean addPropertyValue(String propertyName, Object theValue);

  /**
   * Gets the list of values for a given property name.
   * 
   * @param propertyName
   *          the name of the property
   * @return a List of values.
   */
  public List getPropertyValues(String propertyName);

  /**
   * Removes one of the values for a given property.
   * 
   * @param propertyName
   *          the name of the property
   * @param theValue
   *          the value to be removed.
   * @return <tt>true</tt> if the value was found and removed, <tt>false</tt>
   *         otherwise.
   */
  public boolean removePropertyValue(String propertyName, Object theValue);

  /**
   * Removes all values for a named property.
   * 
   * @param propertyName
   *          the property name.
   */
  public void removePropertyValues(String propertyName);

  /**
   * Gets the names of the properties that have set values for this instance.
   * 
   * @return a set of String values.
   */
  public Set getSetPropertiesNames();
}
