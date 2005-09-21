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

import java.util.*;
import com.ontotext.gate.ontology.OntologyImpl;

/**
 * This class provides implementations for methods common to all types of 
 * ontological properties.
 */
public abstract class PropertyImpl extends OntologyResourceImpl 
      implements Property {
  
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
  protected Set samePropertiesSet;
  protected Set superPropertiesSet;
  protected Set subPropertiesSet;
  
  protected Set superPropertiesTransitiveClosure;
  protected Set subPropertiesTransitiveClosure;
  
  protected boolean functional;
  protected boolean inverseFunctional;


  /**
   * Creates a property.
   * @param name the name of the property
   * @param aDomain the ontology class representing the domain for this 
   * property.
   * @param ontology the ontology this property is defined in.
   */
  public PropertyImpl(String name, String comment, Set domain, 
          Ontology ontology) {
    super(name, comment, ontology);
    this.directDomain = new HashSet(domain);
    this.domain = new HashSet(directDomain);
    samePropertiesSet = new HashSet();
    superPropertiesSet = new HashSet();
    subPropertiesSet = new HashSet();
    superPropertiesTransitiveClosure = new HashSet();
    subPropertiesTransitiveClosure = new HashSet();
    this.functional = false;
    this.inverseFunctional = false;    
  }
  
  public PropertyImpl(String name, String comment, OClass aDomainClass, 
          Ontology ontology) {
    this(name, comment, new HashSet(), ontology);
    this.directDomain.add(aDomainClass);
    this.domain.add(aDomainClass);
  }  
  

  public String getName() {
    return name;
  }

  public String getURI() {
    return uri;
  }

  public void setURI(String theURI) {
    uri = theURI;
  }

  public void setSamePropertyAs(Property theProperty) {
    this.samePropertiesSet.add(theProperty);
  }

  public Set getSamePropertyAs() {
    if (this.samePropertiesSet.isEmpty() &&
        ! this.getOntology().getPropertyDefinitions().contains(this)) {
      Property propDefinition =
        this.getOntology().getPropertyDefinitionByName(this.name);
      if (propDefinition == null)
        return this.samePropertiesSet;
      else
        return propDefinition.getSamePropertyAs();
    }
    return this.samePropertiesSet;
  }

  public void addSuperProperty(Property property) {
    this.superPropertiesSet.add(property);
    //add restrictions from super-property to the domain set
    domain.addAll(property.getDomain());
    OntologyImpl.reduceToMostSpecificClasses(domain);
    //propagate the changes to sub properties
    Iterator subPropIter = getSubProperties(TRANSITIVE_CLOSURE).iterator();
    while(subPropIter.hasNext()) {
      Property aSubProperty = (Property)subPropIter.next();
      if(aSubProperty instanceof PropertyImpl) {
        ((PropertyImpl)aSubProperty).recalculateDomain();
      }
    }
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
  
  public void removeSuperProperty(Property property) {
    this.superPropertiesSet.remove(property);
  }

  /**
   * Add a SuperPropertyOf relation between the given property and this.
   * 
   * @param property
   */
  public void addSubProperty(Property property) {
    this.subPropertiesSet.add(property);
  }
  
  public void removeSubProperty(Property property) {
    this.subPropertiesSet.remove(property);
  }
  
  /**
   * Returns the set of domain classes for this property. This only includes
   * the classes directly defined as domain members for this property (not the
   * ones from the super-properties).
   */
  public Set getDomain() {
    return this.domain;
  }

  /* (non-Javadoc)
   * @see gate.creole.ontology.Property#isFunctional()
   */
  public boolean isFunctional() {
    return functional;
  }


  /* (non-Javadoc)
   * @see gate.creole.ontology.Property#isInverseFunctional()
   */
  public boolean isInverseFunctional() {
    return inverseFunctional;
  }


  /* (non-Javadoc)
   * @see gate.creole.ontology.Property#setFunctional(boolean)
   */
  public void setFunctional(boolean functional) {
    this.functional = functional;
  }

  /* (non-Javadoc)
   * @see gate.creole.ontology.Property#setInverseFunctional(boolean)
   */
  public void setInverseFunctional(boolean inverseFunctional) {
    this.inverseFunctional = inverseFunctional;
  }
  
  /**
   *  Checks whether a provided instance can be a domain value for this 
   *  property. For an instance to be a valid domain value it needs to be a 
   *  member of <b>all</b> the classes defined as members of the domain of this
   *  property. The domain of this property is defined recursively based on its
   *  super-properties as well.  
   * @param instance the instance to be checked.
   * @return <tt>true</tt> if the provided instance can be a domain value for 
   * this property.
   */
  public boolean isValidDomain(OInstance instance) {
    Set domainClasses = new HashSet(getDomain());
    boolean result = true;
    Iterator instanceClassIter = instance.getOClasses().iterator();
    while(result && instanceClassIter.hasNext()) {
      OClass anInstanceClass = (OClass)instanceClassIter.next();
      //first do the simple test
      if(!domainClasses.contains(anInstanceClass)) {
        //the class is not directly contained in the domain,
        //maybe one super class is?
        Set superClasses = anInstanceClass.
            getSuperClasses(OntologyConstants.TRANSITIVE_CLOSURE);
        Set intersection = new HashSet(superClasses);
        intersection.retainAll(domainClasses);
        if(intersection.isEmpty()) result = false;
      }
    }
    return result;
  }
  

  public String toString() {
    return getName();
  }
  
  /**
   * Gets the set of super-properties for this property.
   * @param {@link OntologyConstants#DIRECT_CLOSURE} for direct super-properties 
   * only or {@link OntologyConstants#TRANSITIVE_CLOSURE} for all the 
   * super-properties.
   * @return a set of {@link Property} values.
   */
  public Set getSuperProperties(byte closure) {
    switch(closure) {
      case DIRECT_CLOSURE:
        return superPropertiesSet;
      case TRANSITIVE_CLOSURE:
        if(superPropertiesTransitiveClosure.size() == 0 || 
                getOntology().isModified())
          calculateSuperPropertiesClosure();
        return superPropertiesTransitiveClosure;
      default: throw new IllegalArgumentException("Unknown closure type: " + 
              closure);
    }
  }
  
  protected void calculateSuperPropertiesClosure() {
    superPropertiesTransitiveClosure.clear();
    LinkedList properties = new LinkedList(superPropertiesSet);
    while(properties.size() > 0) {
      Property aSuperProperty = (Property)properties.remove(0);
      if(superPropertiesTransitiveClosure.add(aSuperProperty)) {
        //this super property hasn't been seen before
        properties.addAll(aSuperProperty.getSuperProperties(DIRECT_CLOSURE)); 
      }
    }
  }
  
  /**
   * Gets the set of sub-properties for this property.
   * @param {@link OntologyConstants#DIRECT_CLOSURE} for direct sub-properties 
   * only or {@link OntologyConstants#TRANSITIVE_CLOSURE} for all the 
   * sub-properties.
   * @return a set of {@link Property} values.
   */
  public Set getSubProperties(byte closure) {
    switch(closure) {
      case DIRECT_CLOSURE:
        return subPropertiesSet;
      case TRANSITIVE_CLOSURE:
        if(subPropertiesTransitiveClosure.isEmpty() || 
                getOntology().isModified())
          calculateSubPropertiesClosure();
        return subPropertiesTransitiveClosure;
      default: throw new IllegalArgumentException("Unknown closure type: " + 
              closure);
    }
  }
  
  protected void calculateSubPropertiesClosure() {
    subPropertiesTransitiveClosure.clear();
    LinkedList properties = new LinkedList(subPropertiesSet);
    while(properties.size() > 0) {
      Property aSuperProperty = (Property)properties.remove(0);
      if(subPropertiesTransitiveClosure.add(aSuperProperty)) {
        //this super property hasn't been seen before
        properties.addAll(aSuperProperty.getSubProperties(DIRECT_CLOSURE)); 
      }
    }
  }  
}