/*
 * TransitiveProperyImpl.java
 *
 * Copyright (c) 2005, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * Valentin Tablan 16-Sep-2005
 *
 *
 *  $Id$
 */
package gate.creole.ontology;

import java.util.Set;

/**
 * @author Valentin Tablan
 * 
 */
public class TransitivePropertyImpl extends ObjectPropertyImpl implements
                                                              TransitiveProperty {
  /**
   * @param name
   * @param comment
   * @param aDomainClass
   * @param aRange
   * @param anOntology
   */
  public TransitivePropertyImpl(String name, String comment,
          OClass aDomainClass, OClass aRange, Ontology anOntology) {
    super(name, comment, aDomainClass, aRange, anOntology);
  }

  /**
   * @param name
   * @param comment
   * @param aDomain
   * @param aRange
   * @param anOntology
   */
  public TransitivePropertyImpl(String name, String comment, Set aDomain,
          Set aRange, Ontology anOntology) {
    super(name, comment, aDomain, aRange, anOntology);
  }
}
