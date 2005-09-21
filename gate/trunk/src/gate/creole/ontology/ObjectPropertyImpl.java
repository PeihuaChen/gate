/*
 * ObjectPropertyImpl.java
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

public class ObjectPropertyImpl extends PropertyImpl implements ObjectProperty {
  /**
   * The set of range restrictions (i.e. {@link OClass} objects} for this 
   * property. This is composed from the {@link #directRange} plus all the 
   * range restrictions from the super-properties. Once calculated this value 
   * is cached.
   */  
  protected Set range;
  
  /**
   * The set of range restrictions (i.e. {@link OClass} objects} set as range
   * directly for this property. 
   */  
  protected Set directRange;
  protected Set inversePropertiesSet;

  /**
   * Convenience constructor for simple cases where the domain and range are
   * single classes. 
   * @param aName the name of the property.
   * @param aDomainClass the class representing the domain.
   * @param aRange the class representing the range.
   * @param anOntology the ontology this property belongs to.
   */
  public ObjectPropertyImpl(String name, String comment, OClass aDomainClass, 
          OClass aRange, Ontology anOntology) {
    super(name, comment, aDomainClass, anOntology);
    directRange = new HashSet();
    directRange.add(aRange);
    range = new HashSet(directRange);
    inversePropertiesSet = new HashSet();
  }
  
  /**
   * Constructor for this property.
   * @param aName the name of the property.
   * @param aDomain the set of domain restrictions for this property. A set of 
   * {@link OClass} values.
   * @param aRange the set of range restrictions for this property. A set of 
   * {@link OClass} values. 
   * @param anOntology the ontology this property belongs to.
   */
  public ObjectPropertyImpl(String name, String comment, Set aDomain, Set aRange, 
          Ontology anOntology) {
    super(name, comment, aDomain, anOntology);
    this.directRange = new HashSet(aRange);
    this.range = new HashSet(directRange);
    inversePropertiesSet = new HashSet();
  }
  
  public void addSuperProperty(Property property) {
    super.addSuperProperty(property);
    //add restrictions from super-property to the range set
    range.addAll(((ObjectProperty)property).getRange());
    OntologyImpl.reduceToMostSpecificClasses(range);
    //propagate the changes to sub properties
    Iterator subPropIter = getSubProperties(TRANSITIVE_CLOSURE).iterator();
    while(subPropIter.hasNext()) {
      Property aSubProperty = (Property)subPropIter.next();
      if(aSubProperty instanceof ObjectPropertyImpl) {
        ((ObjectPropertyImpl)aSubProperty).recalculateRange();
      }
    }
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
      range.addAll(((ObjectProperty)superPropIter.next()).getRange());
    }
    OntologyImpl.reduceToMostSpecificClasses(range);
  }

  /**
   *  Checks whether a provided instance can be a range value for this 
   *  property. For an instance to be a valid range value it needs to be a 
   *  member of <b>all</b> the classes defined as members of the range of this
   *  property. The range of this property is defined recursively based on its
   *  super-properties as well.  
   * @param instance the instance to be checked.
   * @return <tt>true</tt> if the provided instance can be a range value for 
   * this property.
   */
  public boolean isValidRange(OInstance instance) {
    Set rangeClasses = new HashSet(getRange());
    
    boolean result = true;
    Iterator instanceClassIter = instance.getOClasses().iterator();
    while(result && instanceClassIter.hasNext()) {
      OClass anInstanceClass = (OClass)instanceClassIter.next();
      //first do the simple test
      if(!rangeClasses.contains(anInstanceClass)) {
        //the class is not directly contained in the range,
        //maybe one super class is?
        Set superClasses = anInstanceClass.
            getSuperClasses(OntologyConstants.TRANSITIVE_CLOSURE);
        Set intersection = new HashSet(superClasses);
        intersection.retainAll(rangeClasses);
        if(intersection.isEmpty()) result = false;
      }
    }
    return result;    
  }
  
  public Set getRange() {
    return range;
  }
  
  public Set getInverseProperties() {
    return this.inversePropertiesSet;
  }
  
  public void setInverseOf(Property theInverse) {
    this.inversePropertiesSet.add(theInverse);
  }
  
}