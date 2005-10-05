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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class DatatypePropertyImpl extends PropertyImpl
                                    implements DatatypeProperty{

  public DatatypePropertyImpl(String name, String comment,  OClass aDomainClass,
          Class aRangeType, Ontology anOntology) {
    super(name, comment, aDomainClass, aRangeType, anOntology);
  }

  public DatatypePropertyImpl(String name, String comment, Set domain, 
          Set range, Ontology ontology) {
    super(name, comment, domain, range, ontology);
  }
  
  public DatatypePropertyImpl(String name, String comment, Set domain, 
          Class range, Ontology ontology) {
    super(name, comment, domain, new HashSet(), ontology);
    this.directRange.add(range);
    this.range.add(range);
  }
  
}