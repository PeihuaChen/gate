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
import java.util.HashSet;
import java.util.Set;

public class ObjectPropertyImpl extends PropertyImpl implements ObjectProperty {
  private Set range;
  private Set inversePropertiesSet;

  /**
   * Convenience constructor for simple cases where the domain and range are
   * sungle classes. 
   * @param aName the name of the property.
   * @param aDomainClass the class representing the domain.
   * @param aRange the class representing the range.
   * @param anOntology the ontology this property belongs to.
   */
  public ObjectPropertyImpl(String name, String comment, OClass aDomainClass, 
          OClass aRange, Ontology anOntology) {
    super(name, comment, aDomainClass, anOntology);
    range = new HashSet();
    range.add(aRange);
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
    range = new HashSet();
    range.addAll(aRange);
    inversePropertiesSet = new HashSet();
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
    Iterator superPropIter = superPropertiesSet.iterator();
    while(superPropIter.hasNext()) 
      rangeClasses.addAll(((ObjectProperty)superPropIter.next()).getRange());
    return instance.getOClasses().containsAll(rangeClasses);
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