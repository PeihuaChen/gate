/* Ontology.java
 *
 * Copyright (c) 1998-2004, The University of Sheffield.
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
 *
 *  $Id$
*/

package gate.creole.ontology;

import java.util.List;
import java.util.Set;

public interface Ontology extends Taxonomy {

  /**Adds an instance to the ontology.
   * @param name the instance name to be added
   * @param theClass the class to be added
   * @return the OInstance that has been added to the ontology */
  public OInstance addInstance(String name, OClass theClass);

  /**Adds an instance to the ontology.*/
  public void addInstance(OInstance theInstance);

  /**Removes the instance from the ontology.
   * @param theInstance to be removed */
  public void removeInstance(OInstance theInstance);

  /**Gets all instances in the ontology.
   * @return List of OInstance objects */
  public List getInstances();

  /**Gets all instances in the ontology, which belong to this class,
   * including instances of sub-classes. If only the instances
   * of the given class are needed, then use getDirectInstances.
   * @param theClass the class of the instances
   * @return List of OInstance objects */
  public List getInstances(OClass theClass);

  /**Gets all instances in the ontology, which belong to the
   * given class only.
   * @param theClass the class of the instances
   * @return List of OInstance objects */
  public List getDirectInstances(OClass theClass);

  /**Gets the instance with the given name.
   * @param instanceName the instance name
   * @return the OInstance object with this name */
  public OInstance getInstanceByName(String instanceName);

  /**
   * Create a DatatypeProperty with the given domain and range
   * @param domain
   * @param range
   */
  public DatatypeProperty addDatatypeProperty(String name, OClass domain, String range);

  /**
   * Create a DatatypeProperty with the given domain and range
   * @param domain
   * @param range
   */
  public DatatypeProperty addDatatypeProperty(String name, OClass domain, Number range);

  /**
   * Create a FunctionalProperty with the given domain and range
   * @param domain
   * @param range
   * @return a {@link KBFunctionalProperty} value.
   */
  public FunctionalProperty addFunctionalProperty(String name, OClass domain, Object range);

  public ObjectProperty addObjectProperty(String name, OClass domain, OClass range);

  public SymmetricProperty addSymmetricProperty(String name, OClass domain, OClass range);

  public TransitiveProperty addTransitiveProperty(OClass domain, OClass range);

  public void addPropertyDefinition(Property theProperty);

  public Set getPropertyDefinitions();

  public Property getPropertyDefinitionByName(String name);

}