/*
 * KBDatatypePropertyImpl.java
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

public class KBDatatypePropertyImpl extends KBPropertyImpl
                                    implements KBDatatypeProperty{
  private Object range;

  public KBDatatypePropertyImpl(String aName, KBClass aDomain, String aString, KnowledgeBase aKB) {
    super(aName, aDomain, aKB);
    range = aString;
  }

  public KBDatatypePropertyImpl(String aName, KBClass aDomain, Number number, KnowledgeBase aKB) {
    super(aName, aDomain, aKB);
    range = number;
  }

  public boolean isValueCompatible(Object value) {
    if (value instanceof String)
      return true;
    else if (value instanceof Number)
      return true;
    return false;
  }

  public Object getRange() {
    return range;
  }

  public String toString() {
    return this.getName() + "(" + this.getDomain() + "," + this.range + ")" +
            "\n sub-propertyOf "
            + this.getSubPropertyOf().toString() +
            "\n samePropertyAs " +
            this.getSamePropertyAs().toString();
  }

}