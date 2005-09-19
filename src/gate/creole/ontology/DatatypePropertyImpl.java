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

import java.util.Set;

public class DatatypePropertyImpl extends PropertyImpl
                                    implements DatatypeProperty{
  /**
   * The range for this property. Datatype properties take Java objects as 
   * values so the range is a {@link Class} object.
   * If this is set to <tt>null</tt> then any type of Java Object is a valid
   * value.
   */
  protected Class range;

  public DatatypePropertyImpl(String name, String comment,  OClass aDomainClass,
          Ontology anOntology) {
    super(name, comment, aDomainClass, anOntology);
    range = null;
  }

  public DatatypePropertyImpl(String name, String comment, Set domain, 
          Ontology ontology) {
    super(name, comment, domain, ontology);
    range = null;
  }
  
  public DatatypePropertyImpl(String name, String comment, Set domain, 
          Class range, Ontology ontology) {
    super(name, comment, domain, ontology);
    this.range = range;
  }
  

  public boolean isValidRange(Object value) {
    return range == null || range.isAssignableFrom(value.getClass());
  }

  public Class getRange() {
    return range;
  }

}