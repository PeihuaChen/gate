/*
 * KBClassImpl.java
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

public class KBClassImpl extends OClassImpl implements KBClass  {

  private Set disjointClassesSet;
  private Set sameClassesSet;
  private Set propertiesSet;

  /**
   * Creates a new class given id,name,comment and ontology.
   * @param anId the id of the new class
   * @param aName the name of the new class
   * @param aComment the comment of the new class
   * @param anOntology the ontology to which the new class belongs
   */
  public KBClassImpl(String anId, String aName, String aComment, Ontology anOntology) {
    super(anId, aName, aComment, anOntology);
    disjointClassesSet = new HashSet();
    sameClassesSet = new HashSet();
    propertiesSet = new HashSet();
  }

  public void setDisjointWith(KBClass theClass) {
    if (theClass == null)
      return;
    disjointClassesSet.add(theClass);
  }

  public void setSameClassAs(KBClass theClass) {
    if (theClass == null)
      return;
    this.sameClassesSet.add(theClass);
  }

  public Set getDisjointClasses() {
    if (this.disjointClassesSet.isEmpty())
      return null;
    return this.disjointClassesSet;
  }

  public Set getSameClasses() {
    if (this.sameClassesSet.isEmpty())
      return null;
    return this.sameClassesSet;
  }

  public Set getProperties() {
    if (this.propertiesSet.isEmpty())
      return null;
    return this.propertiesSet;
  }

  public KBProperty getPropertyByName(String name) {
    if (this.propertiesSet.isEmpty())
      return null;
    if (name == null)
      return null;
    Iterator iter = this.propertiesSet.iterator();
    while (iter.hasNext()) {
      KBProperty property = (KBProperty) iter.next();
      if (name.equals(property.getName()))
        return property;
    }
    return null;
  }

  public Set getInheritedProperties() {
    Set superClasses = null;
    try {
      this.getSuperClasses(this.TRANSITIVE_CLOSURE);
    } catch (NoSuchClosureTypeException ex) {};

    if (superClasses == null || superClasses.isEmpty())
      return null;
    Set inheritedProperties = new HashSet();
    Iterator iter = superClasses.iterator();
    while (iter.hasNext()) {
      Set classProperties = ((KBClass)iter.next()).getProperties();
      if (classProperties != null)
        inheritedProperties.addAll(classProperties);
    }//while
    return inheritedProperties;
  }

  protected boolean addProperty(KBProperty theProperty) {
    if (this.propertiesSet == null)
      this.propertiesSet = new HashSet();
    if (! this.equals(theProperty.getDomain()))
      return false;
    this.propertiesSet.add(theProperty);
    return true;
  }
}