/*
 * KBObjectPropertyImpl.java
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

public class KBObjectPropertyImpl extends KBPropertyImpl implements KBObjectProperty {

  private KBClass range;
  private Set inversePropertiesSet;

  public KBObjectPropertyImpl(String aName, KBClass aDomain, KBClass aRange,
                              KnowledgeBase aKB) {
    super(aName, aDomain, aKB);
    range = aRange;
    inversePropertiesSet = new HashSet();
  }

  public boolean isValueCompatible(Object value) {
    if (value instanceof KBClass)
      return true;
    return false;
  }

  public Object getRange() {
    return range;
  }

  public Set getInverseProperties() {
    return this.inversePropertiesSet;
  }

  public void setInverseOf(KBProperty theInverse) {
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