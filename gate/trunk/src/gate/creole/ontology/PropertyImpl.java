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
import java.util.Set;

public abstract class PropertyImpl implements Property {
  private String name;
  private String uri;
  private OClass domain;
  private Set samePropertiesSet;
  private Set superPropertiesSet;
  private Ontology ontology;


  public PropertyImpl(String aName, OClass aDomain, Ontology aKB) {
    this.name = aName;
    this.domain = aDomain;
    this.ontology = aKB;
    samePropertiesSet = new HashSet();
    superPropertiesSet = new HashSet();
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

  public void setSubPropertyOf(String propertyName) {
    this.superPropertiesSet.add(propertyName);
  }

  public Set getSubPropertyOf() {
    if (this.superPropertiesSet.isEmpty() &&
        ! this.getOntology().getPropertyDefinitions().contains(this)) {
      Property propDefinition =
        this.getOntology().getPropertyDefinitionByName(this.name);
      if (propDefinition == null)
        return this.superPropertiesSet;
      else
        return propDefinition.getSubPropertyOf();
    }
    return this.superPropertiesSet;
  }

  public OClass getDomain() {
    return this.domain;
  }

  public Ontology getOntology() {
    return this.ontology;
  }

  public String toString() {
    return this.getName() + "(" + this.domain + ")" + "\n sub-propertyOf "
            + this.getSubPropertyOf().toString() + "\n samePropertyAs " +
            this.getSamePropertyAs().toString();
  }
}