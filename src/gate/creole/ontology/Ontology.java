/* Ontology.java
 *
 * Copyright (c) 1998-2005, The University of Sheffield.
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

import java.util.Set;

/**
 * This is the base interface for all concrete implementations of ontologies.
 */
public interface Ontology extends Taxonomy {
  /**
   * Adds an instance to the ontology.
   * 
   * @param name
   *          the name for the new instance
   * @param theClass
   *          the class to which the instance belongs
   * @return the OInstance that has been added to the ontology
   */
  public OInstance addInstance(String name, OClass theClass);

  /**
   * Adds a preconstructed instance to the ontology.
   */
  public OInstance addInstance(OInstance theInstance);

  /**
   * Removes the instance from the ontology.
   * 
   * @param theInstance
   *          to be removed
   */
  public void removeInstance(OInstance theInstance);

  /**
   * Gets all instances in the ontology.
   * 
   * @return a {@link Set} of OInstance objects
   */
  public Set getInstances();

  /**
   * Gets all instances in the ontology, which belong to this class, including
   * instances of sub-classes. If only the instances of the given class are
   * needed, then use getDirectInstances.
   * 
   * @param theClass
   *          the class of the instances
   * @return {@link Set} of OInstance objects
   */
  public Set getInstances(OClass theClass);

  /**
   * Gets all instances in the ontology, which belong to the given class only.
   * 
   * @param theClass
   *          the class of the instances
   * @return {@link Set} of OInstance objects
   */
  public Set getDirectInstances(OClass theClass);

  /**
   * Gets the instance with the given name.
   * 
   * @param instanceName
   *          the instance name
   * @return the OInstance object with this name
   */
  public OInstance getInstanceByName(String instanceName);

  /**
   * A method to remove the existing propertyDefinition
   * 
   * @param property
   */
  public void removePropertyDefinition(Property property);

  /**
   * Creates a new Datatype property in this ontology where the domain consists
   * of a single {@link OClass}.
   * 
   * @param name
   *          the name for the new property.
   * @param comment
   *          the comment for the new property.
   * @param domain
   *          the {@link OClass} to which this property applies.
   * @param range
   *          the {@link Class} specifying the types of Java objects that this
   *          property has as values.
   * @return the newly created property.
   */
  public DatatypeProperty addDatatypeProperty(String name, String comment,
          OClass domain, Class range);

  /**
   * Create a DatatypeProperty with the given domain and range.
   * 
   * @param name
   *          the name for the new property.
   * @param comment
   *          the comment for the new property.
   * @param domain
   *          the set of ontology classes (i.e. {@link OClass} objects} that
   *          constitutes the range for the new property. The property only
   *          applies to instances that belong to <b>all</b> classes included
   *          in its domain. An empty set means that the property applies to
   *          instances of any class.
   * @param range
   *          the {@link Class} specifying the types of Java objects that this
   *          property has as values.
   * @return the newly created property.
   */
  public DatatypeProperty addDatatypeProperty(String name, String comment,
          Set domain, Class range);

  /**
   * Add a Datatype Property
   * 
   * @author niraj
   * @param properity
   */
  public DatatypeProperty addDatatypeProperty(DatatypeProperty properity);

  /**
   * Creates a new generic property that is neither datatype or object property.
   * This can be for instance a RDF property.
   * 
   * @param name
   *          the name for the new property.
   * @param comment
   *          the comment for the new property.
   * @param domain
   *          the set of ontology classes (i.e. {@link OClass} objects} that
   *          constitutes the range for the new property. The property only
   *          applies to instances that belong to <b>all</b> classes included
   *          in its domain. An empty set means that the property applies to
   *          instances of any class.
   * @param range
   *          the set of ontology classes (i.e. {@link OClass} objects} that
   *          constitutes the range for the new property.
   * @return the newly created property.
   */
  public Property addProperty(String name, String comment, Set domain, Set range);

  /**
   * Creates a new generic property that is neither datatype or object property.
   * This can be for instance a RDF property.
   * 
   * @param name
   *          the name for the new property.
   * @param comment
   *          the comment for the new property.
   * @param domain
   *          the {@link OClass} defining the type of instances this property
   *          can apply to.
   * @param range
   *          Java {@link Class} defining the type of values this proeprty can
   *          take.
   * @return the newly created property.
   */
  public Property addProperty(String name, String comment, OClass domain,
          Class range);

  /**
   * Add a Property
   * 
   * @author niraj
   * @param properity
   */
  public Property addProperty(Property properity);

  /**
   * Creates a new object property (a property that takes instances as values).
   * 
   * @param name
   *          the name for the new property.
   * @param comment
   *          the comment for the new property.
   * @param domain
   *          the set of ontology classes (i.e. {@link OClass} objects} that
   *          constitutes the range for the new property. The property only
   *          applies to instances that belong to <b>all</b> classes included
   *          in its domain. An empty set means that the property applies to
   *          instances of any class.
   * @param range
   *          the set of ontology classes (i.e. {@link OClass} objects} that
   *          constitutes the range for the new property.
   * @return the newly created property.
   */
  public ObjectProperty addObjectProperty(String name, String comment,
          Set domain, Set range);

  /**
   * Creates a new object property (a property that takes instances as values).
   * 
   * @param name
   *          the name for the new property.
   * @param comment
   *          the comment for the new property.
   * @param domain
   *          the {@link OClass} to which this property applies.
   * @param range
   *          the {@link OClass} specifying the types of instances that this
   *          property can take as values.
   * @return the newly created property.
   */
  public ObjectProperty addObjectProperty(String name, String comment,
          OClass domain, OClass range);

  /**
   * Add an Object Property
   * 
   * @author niraj
   * @param properity
   */
  public ObjectProperty addObjectProperty(ObjectProperty properity);

  /**
   * Creates a new symmetric property (an object property that is symmetric).
   * 
   * @param name
   *          the name for the new property.
   * @param comment
   *          the comment for the new property.
   * @param domain
   *          the set of ontology classes (i.e. {@link OClass} objects} that
   *          constitutes the range for the new property. The property only
   *          applies to instances that belong to <b>all</b> classes included
   *          in its domain. An empty set means that the property applies to
   *          instances of any class.
   * @param range
   *          the set of ontology classes (i.e. {@link OClass} objects} that
   *          constitutes the range for the new property.
   * @return the newly created property.
   */
  public SymmetricProperty addSymmetricProperty(String name, String comment,
          Set domain, Set range);

  /**
   * Creates a new symmetric property.
   * 
   * @param name
   *          the name for the new property.
   * @param comment
   *          the comment for the new property.
   * @param domain
   *          the {@link OClass} to which this property applies.
   * @param range
   *          the {@link OClass} specifying the types of instances that this
   *          property can take as values.
   * @return the newly created property.
   */
  public SymmetricProperty addSymmetricProperty(String name, String comment,
          OClass domain, OClass range);

  /**
   * Add a Transitive Property
   * 
   * @author niraj
   * @param properity
   */
  public SymmetricProperty addSymmetricProperty(SymmetricProperty properity);

  /**
   * Creates a new transitive property (an object property that is transitive).
   * 
   * @param name
   *          the name for the new property.
   * @param comment
   *          the comment for the new property.
   * @param domain
   *          the set of ontology classes (i.e. {@link OClass} objects} that
   *          constitutes the range for the new property. The property only
   *          applies to instances that belong to <b>all</b> classes included
   *          in its domain. An empty set means that the property applies to
   *          instances of any class.
   * @param range
   *          the set of ontology classes (i.e. {@link OClass} objects} that
   *          constitutes the range for the new property.
   * @return the newly created property.
   */
  public TransitiveProperty addTransitiveProperty(String name, String comment,
          Set domain, Set range);

  /**
   * Creates a new transitive property.
   * 
   * @param name
   *          the name for the new property.
   * @param comment
   *          the comment for the new property.
   * @param domain
   *          the {@link OClass} to which this property applies.
   * @param range
   *          the {@link OClass} specifying the types of instances that this
   *          property can take as values.
   * @return the newly created property.
   */
  public TransitiveProperty addTransitiveProperty(String name, String comment,
          OClass domain, OClass range);

  /**
   * Add a Transitive Property
   * 
   * @author niraj
   * @param properity
   */
  public TransitiveProperty addTransitiveProperty(TransitiveProperty properity);

  /**
   * Gets the set of all known property definitions in this ontology.
   * 
   * @return a {@link Set} of {@link Property} objects.
   */
  public Set getPropertyDefinitions();

  /**
   * Returns the property definition for a given property.
   * 
   * @param name
   *          the name for which the definition is sought.
   * @return a{@link Property} object.
   */
  public Property getPropertyDefinitionByName(String name);
}