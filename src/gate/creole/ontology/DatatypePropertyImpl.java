/*
 * DatatypePropertyImpl.java
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

public class DatatypePropertyImpl extends PropertyImpl
                                    implements DatatypeProperty{
  private Object range;

  public DatatypePropertyImpl(String aName, OClass aDomain, String aString, Ontology aKB) {
    super(aName, aDomain, aKB);
    range = aString;
  }

  public DatatypePropertyImpl(String aName, OClass aDomain, Number number, Ontology aKB) {
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