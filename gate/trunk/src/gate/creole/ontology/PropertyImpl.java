/*
 * PropertyImpl.java
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import com.ontotext.gate.ontology.OntologyImpl;

/**
 * This class provides implementations for methods common to all types of
 * ontological properties.
 */
public class PropertyImpl extends OntologyResourceImpl implements Property {
  /**
   * The set of domain restrictions (i.e. {@link OClass} objects} for this
   * property. This is composed from the {@link #directDomain} plus all the
   * domain restrictions from the super-properties. Once calculated this value
   * is cached.
   */
  protected Set domain;

  /**
   * The set of domain restrictions (i.e. {@link OClass} objects} set as domain
   * directly for this property.
   */
  protected Set directDomain;

  /**
   * The set of range restrictions (i.e. {@link OClass} or {@link Class}
   * objects) for this property. This is composed from the {@link #directRange}
   * plus all the range restrictions from the super-properties. Once calculated
   * this value is cached.
   */
  protected Set range;

  /**
   * The set of range restrictions (i.e. {@link OClass} or {@link Class}
   * objects) set as range directly for this property.
   */
  protected Set directRange;

  protected Set samePropertiesSet;

  protected Set superPropertiesSet;

  protected Set subPropertiesSet;

  protected Set superPropertiesTransitiveClosure;

  protected Set subPropertiesTransitiveClosure;

  protected boolean functional;

  protected boolean inverseFunctional;

  /**
   * Creates a property.
   * 
   * @param name
   *          the name of the property
   * @param domain
   *          the ontology class representing the domain for this property.
   * @param range
   *          a set containing range restrictions. These can either be
   *          {@link OClass} or {@link Class} objects depending on the types of
   *          the values that are permitted.
   * @param ontology
   *          the ontology this property is defined in.
   */
  public PropertyImpl(String name, String comment, Set domain, Set range,
          Ontology ontology) {
    super(name, comment, ontology);
    this.directDomain = new HashSet(domain);
    this.domain = new HashSet(directDomain);
    this.directRange = new HashSet(range);
    this.range = new HashSet(directRange);
    samePropertiesSet = new HashSet();
    superPropertiesSet = new HashSet();
    subPropertiesSet = new HashSet();
    superPropertiesTransitiveClosure = new HashSet();
    subPropertiesTransitiveClosure = new HashSet();
    this.functional = false;
    this.inverseFunctional = false;
  }

  public PropertyImpl(String name, String comment, OClass aDomainClass,
          Object aRangeType, Ontology ontology) {
    this(name, comment, new HashSet(), new HashSet(), ontology);
    if(aDomainClass != null) {
      this.directDomain.add(aDomainClass);
      this.domain.add(aDomainClass);
    }
    this.directRange.add(aRangeType);
    this.range.add(aRangeType);
  }

  public void setSamePropertyAs(Property theProperty) {
    this.samePropertiesSet.add(theProperty);
    OntologyModificationEvent ome = new OntologyModificationEvent(taxonomy,
      this, OntologyModificationEvent.SAME_AS_EVENT);
    taxonomy.fireOntologyModificationEvent(ome);
  }

  public Set getSamePropertyAs() {
    if(this.samePropertiesSet.isEmpty()
            && !this.getOntology().getPropertyDefinitions().contains(this)) {
      Property propDefinition = this.getOntology().getPropertyDefinitionByName(
              this.name);
      if(propDefinition == null)
        return this.samePropertiesSet;
      else return propDefinition.getSamePropertyAs();
    }
    return this.samePropertiesSet;
  }

  public void addSuperProperty(Property property) {
    this.superPropertiesSet.add(property);
    // add restrictions from super-property to the domain set
    domain.addAll(property.getDomain());
    OntologyImpl.reduceToMostSpecificClasses(domain);
    // propagate the changes to sub properties
    Iterator subPropIter = getSubProperties(TRANSITIVE_CLOSURE).iterator();
    while(subPropIter.hasNext()) {
      Property aSubProperty = (Property)subPropIter.next();
      if(aSubProperty instanceof PropertyImpl) {
        ((PropertyImpl)aSubProperty).recalculateDomain();
        ((PropertyImpl)aSubProperty).recalculateRange();
      }
    }
    OntologyModificationEvent ome = new OntologyModificationEvent(ontology,
            this, OntologyModificationEvent.SUPER_PROPERTY_ADDED_EVENT);
    ontology.fireOntologyModificationEvent(ome);
  }

  /**
   * Notifies this property that it should recalculate the range set (because
   * the range of a super-property has changed).
   */
  protected void recalculateDomain() {
    domain.clear();
    domain.addAll(directDomain);
    Iterator superPropIter = getSuperProperties(TRANSITIVE_CLOSURE).iterator();
    while(superPropIter.hasNext()) {
      domain.addAll(((Property)superPropIter.next()).getDomain());
    }
    OntologyImpl.reduceToMostSpecificClasses(domain);
  }

  /**
   * Notifies this property that it should recalculate the range set (because
   * the range of a super-property has changed).
   */
  protected void recalculateRange() {
    range.clear();
    range.addAll(directRange);
    Iterator superPropIter = getSuperProperties(TRANSITIVE_CLOSURE).iterator();
    while(superPropIter.hasNext()) {
      range.addAll(((Property)superPropIter.next()).getRange());
    }
    OntologyImpl.reduceToMostSpecificClasses(range);
  }

  public void removeSuperProperty(Property property) {
    this.superPropertiesSet.remove(property);
    OntologyModificationEvent ome = new OntologyModificationEvent(ontology,
            this, OntologyModificationEvent.SUPER_PROPERTY_REMOVED_EVENT);
    ontology.fireOntologyModificationEvent(ome);
  }

  /**
   * Add a SuperPropertyOf relation between the given property and this.
   * 
   * @param property
   */
  public void addSubProperty(Property property) {
    this.subPropertiesSet.add(property);
    OntologyModificationEvent ome = new OntologyModificationEvent(ontology,
            this, OntologyModificationEvent.SUB_PROPERTY_ADDED_EVENT);
    ontology.fireOntologyModificationEvent(ome);
  }

  public void removeSubProperty(Property property) {
    this.subPropertiesSet.remove(property);
    OntologyModificationEvent ome = new OntologyModificationEvent(ontology,
            this, OntologyModificationEvent.SUB_PROPERTY_REMOVED_EVENT);
    ontology.fireOntologyModificationEvent(ome);
  }

  /**
   * Returns the set of domain classes for this property. This is composed from
   * the classes declared as domain restriction for this property plus all the
   * domain restrictions from the super-properties.
   */
  public Set getDomain() {
    return this.domain;
  }

  /**
   * Returns the set of range classes for this property. This is composed from
   * the classes declared as range restriction for this property plus all the
   * range restrictions from the super-properties.
   */
  public Set getRange() {
    return this.range;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Property#isFunctional()
   */
  public boolean isFunctional() {
    return functional;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Property#isInverseFunctional()
   */
  public boolean isInverseFunctional() {
    return inverseFunctional;
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Property#setFunctional(boolean)
   */
  public void setFunctional(boolean functional) {
    this.functional = functional;
    OntologyModificationEvent ome = new OntologyModificationEvent(ontology,
            this, OntologyModificationEvent.FUNCTIONAL_EVENT);
    ontology.fireOntologyModificationEvent(ome);
  }

  /*
   * (non-Javadoc)
   * 
   * @see gate.creole.ontology.Property#setInverseFunctional(boolean)
   */
  public void setInverseFunctional(boolean inverseFunctional) {
    this.inverseFunctional = inverseFunctional;
    OntologyModificationEvent ome = new OntologyModificationEvent(ontology,
            this, OntologyModificationEvent.INVERSE_FUNCTIONAL_EVENT);
    ontology.fireOntologyModificationEvent(ome);
  }

  /**
   * Checks whether a provided value can be a domain value for this property. If
   * the value is an {@link OInstance} then, in order to be a valid domain value
   * it needs to be a member of <b>all</b> the classes defined as members of
   * the domain of this property. The domain of this property is defined
   * recursively based on its super-properties as well. For any other types of
   * values it returns <tt>true</tt>.
   * 
   * @param instance
   *          the instance to be checked.
   * @return <tt>true</tt> if the provided instance can be a domain value for
   *         this property.
   */
  public boolean isValidDomain(OntologyResource resource) {
    if(resource instanceof OInstance) {
      OInstance instance = (OInstance)resource;
      Set domainClasses = new HashSet(getDomain());
      boolean result = true;
      // if there are no restrictions on the domain, then any domain is
      // valid
      if(domainClasses.isEmpty()) return true;
      Iterator instanceClassIter = instance.getOClasses().iterator();
      while(result && instanceClassIter.hasNext()) {
        OClass anInstanceClass = (OClass)instanceClassIter.next();
        // first do the simple test
        if(!domainClasses.contains(anInstanceClass)) {
          // the class is not directly contained in the domain,
          // maybe one super class is?
          Set superClasses = anInstanceClass
                  .getSuperClasses(OntologyConstants.TRANSITIVE_CLOSURE);
          Set intersection = new HashSet(superClasses);
          intersection.retainAll(domainClasses);
          if(intersection.isEmpty()) result = false;
        }
      }
      return result;
    } else return true;
  }

  /**
   * Checks whether a provided instance can be a range value for this property.
   * For an instance to be a valid range value it needs to be a member of <b>all</b>
   * the classes defined as members of the range of this property. The range of
   * this property is defined recursively based on its super-properties as well.
   * 
   * @param instance
   *          the instance to be checked.
   * @return <tt>true</tt> if the provided instance can be a range value for
   *         this property.
   */
  public boolean isValidRange(Object value) {
    if(value instanceof OInstance) {
      // implementation for ObjectProperties
      OInstance instance = (OInstance)value;
      Set rangeClasses = new HashSet(getRange());
      boolean result = true;
      Iterator instanceClassIter = instance.getOClasses().iterator();
      while(result && instanceClassIter.hasNext()) {
        OClass anInstanceClass = (OClass)instanceClassIter.next();
        // first do the simple test
        if(!rangeClasses.contains(anInstanceClass)) {
          // the class is not directly contained in the range,
          // maybe one super class is?
          Set superClasses = anInstanceClass
                  .getSuperClasses(OntologyConstants.TRANSITIVE_CLOSURE);
          Set intersection = new HashSet(superClasses);
          intersection.retainAll(rangeClasses);
          if(intersection.isEmpty()) result = false;
        }
      }
      return result;
    } else if(value instanceof OntologyResource) {
      // implementation for generic (a.k.a. RDF) properties
      return true;
    } else {
      // implementation for DataType properties
      Iterator rangIter = getRange().iterator();
      while(rangIter.hasNext()) {
        if(!((Class)rangIter.next()).isAssignableFrom(value.getClass()))
          return false;
      }
      return true;
    }
  }

  public String toString() {
    return getName();
  }

  /**
   * Gets the set of super-properties for this property.
   * 
   * @param {@link OntologyConstants#DIRECT_CLOSURE}
   *          for direct super-properties only or
   *          {@link OntologyConstants#TRANSITIVE_CLOSURE} for all the
   *          super-properties.
   * @return a set of {@link Property} values.
   */
  public Set getSuperProperties(byte closure) {
    switch(closure){
      case DIRECT_CLOSURE:
        return superPropertiesSet;
      case TRANSITIVE_CLOSURE:
        if(superPropertiesTransitiveClosure.size() == 0
                || getOntology().isModified())
          calculateSuperPropertiesClosure();
        return superPropertiesTransitiveClosure;
      default:
        throw new IllegalArgumentException("Unknown closure type: " + closure);
    }
  }

  protected void calculateSuperPropertiesClosure() {
    superPropertiesTransitiveClosure.clear();
    LinkedList properties = new LinkedList(superPropertiesSet);
    while(properties.size() > 0) {
      Property aSuperProperty = (Property)properties.remove(0);
      if(superPropertiesTransitiveClosure.add(aSuperProperty)) {
        // this super property hasn't been seen before
        properties.addAll(aSuperProperty.getSuperProperties(DIRECT_CLOSURE));
      }
    }
  }

  /**
   * Gets the set of sub-properties for this property.
   * 
   * @param {@link OntologyConstants#DIRECT_CLOSURE}
   *          for direct sub-properties only or
   *          {@link OntologyConstants#TRANSITIVE_CLOSURE} for all the
   *          sub-properties.
   * @return a set of {@link Property} values.
   */
  public Set getSubProperties(byte closure) {
    switch(closure){
      case DIRECT_CLOSURE:
        return subPropertiesSet;
      case TRANSITIVE_CLOSURE:
        if(subPropertiesTransitiveClosure.isEmpty()
                || getOntology().isModified()) calculateSubPropertiesClosure();
        return subPropertiesTransitiveClosure;
      default:
        throw new IllegalArgumentException("Unknown closure type: " + closure);
    }
  }

  protected void calculateSubPropertiesClosure() {
    subPropertiesTransitiveClosure.clear();
    LinkedList properties = new LinkedList(subPropertiesSet);
    while(properties.size() > 0) {
      Property aSuperProperty = (Property)properties.remove(0);
      if(subPropertiesTransitiveClosure.add(aSuperProperty)) {
        // this super property hasn't been seen before
        properties.addAll(aSuperProperty.getSubProperties(DIRECT_CLOSURE));
      }
    }
  }
}
