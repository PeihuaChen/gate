/*
 * KBClass.java
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
 *  $Id$*
 */

 package gate.creole.ontology;

 import java.util.Set;

public interface KBClass extends OClass {

  /** Indicates that these are disjoint classes */
  public void setDisjointWith(KBClass theClass);

  /** Indicates that these classes are the same */
  public void setSameClassAs(KBClass theClass);

  /** Returns a set of all classes that are disjoint with ours. Null if
   *  no such classes.
   */
  public Set getDisjointClasses();

  /** Returns a set of all classes that are the same as ours. Null if
   *  no such classes.
   */
  public Set getSameClasses();

  /**
   * Returns a set of all KBProperty (ako relations) for which this class is
   * the domain (first predicate). The properties are associated with classes,
   * not independent of them and attached via anonymous classes and restrictions
   * as it is in DAML/OWL. Therefore our model is closer to the Protege
   * frame-based model. The advantage of having this kind of model is that it
   * can be generalised API both for Protege and DAML/OWL/RDF ontologies.
   */
  public Set getProperties();

  /**
   * Returns the set of properties with the given name. The set elements are
   * instances of KBProperty or sub-classes. The reason why we need a set
   * is because a class can have more than one property with the same name
   * but different ranges.
   * @param name
   * @return a {@link Set} value.
   */
  public Set getPropertiesByName(String name);

  /**
   * This method supplies all KBProperty inherited from the superclasses of the
   * given class. Null if no such properties exist.
   * Note that to get all properties (both inherited and associated with the
   * current class) one needs to call both getInheritedProperties and
   * getProperties.
   */
  public Set getInheritedProperties();

}