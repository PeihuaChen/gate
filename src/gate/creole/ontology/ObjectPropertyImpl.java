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

import java.util.HashSet;
import java.util.Set;

public class ObjectPropertyImpl extends PropertyImpl implements ObjectProperty {

  private OClass range;
  private Set inversePropertiesSet;

  public ObjectPropertyImpl(String aName, OClass aDomain, OClass aRange,
                              Ontology aKB) {
    super(aName, aDomain, aKB);
    range = aRange;
    inversePropertiesSet = new HashSet();
  }

  public boolean isValueCompatible(Object value) {
    if (value instanceof OClass)
      return true;
    return false;
  }

  public Object getRange() {
    return range;
  }

  public Set getInverseProperties() {
    return this.inversePropertiesSet;
  }

  public void setInverseOf(Property theInverse) {
    this.inversePropertiesSet.add(theInverse);
  }

  public String toString() {
    return this.getName() + "(" + this.getDomain() + "," + this.range + ")" +
            "\n sub-propertyOf "
            + this.getSubPropertyOf().toString() +
            "\n samePropertyAs " +
            this.getSamePropertyAs().toString();
  }

}