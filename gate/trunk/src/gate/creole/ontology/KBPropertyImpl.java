/*
 * KBPropertyImpl.java
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

public abstract class KBPropertyImpl implements KBProperty {
  private String name;
  private String uri;
  private KBClass domain;
  private Set samePropertiesSet;
  private Set superPropertiesSet;
  private Set subPropertiesSet;


  protected KBPropertyImpl(String aName, KBClass aDomain) {
    this.name = aName;
    this.domain = aDomain;
    samePropertiesSet = new HashSet();
    superPropertiesSet = new HashSet();
    subPropertiesSet = new HashSet();
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

  public void setSamePropertyAs(KBProperty theProperty) {
    this.samePropertiesSet.add(theProperty);
  }

  public Set getSamePropertyAs() {
    if (this.samePropertiesSet.isEmpty())
      return null;
    return this.samePropertiesSet;
  }

  public void setSubPropertyOf(KBProperty theProperty) {
    this.subPropertiesSet.add(theProperty);
    if (theProperty instanceof KBPropertyImpl)
      ((KBPropertyImpl)theProperty).setSuperProperty(this);
  }

  public Set getSubPropertyOf() {
    if (this.subPropertiesSet.isEmpty())
      return null;
    return this.subPropertiesSet;
  }

  protected void setSuperProperty(KBProperty theProperty) {
    this.superPropertiesSet.add(theProperty);
  }

  public Set getSuperProperties() {
    if (this.superPropertiesSet.isEmpty())
      return null;
    return this.superPropertiesSet;
  }

  public KBClass getDomain() {
    return this.domain;
  }
}