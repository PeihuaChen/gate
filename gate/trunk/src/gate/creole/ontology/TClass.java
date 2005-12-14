/*
 * TClass.java
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
 * borislav popov 02/2002
 *
 *
 *  $Id$
 */
package gate.creole.ontology;

import java.util.ArrayList;
import java.util.Set;

/** An Interface representing a single ontology class */
public interface TClass extends OntologyResource, OntologyConstants {
  /**
   * Gets the id.
   * 
   * @return the id of the class
   */
  public String getId();

  /**
   * Adds a sub class to this class.
   * 
   * @param subClass
   *          the subClass to be added.
   */
  public void addSubClass(TClass subClass);

  /**
   * Adds a super class to this class.
   * 
   * @param superClass
   *          the super class to be added
   */
  public void addSuperClass(TClass superClass);

  /**
   * Removes a sub class.
   * 
   * @param subClass
   *          the sub class to be removed
   */
  public void removeSubClass(TClass subClass);

  /**
   * Removes a super class.
   * 
   * @param superClass
   *          the super class to be removed
   */
  public void removeSuperClass(TClass superClass);

  /**
   * Gets the subclasses according to the desired closure.
   * 
   * @param closure
   *          either DIRECT_CLOSURE or TRASITIVE_CLOSURE
   * @return the set of subclasses
   * @throws NoSuchClosureTypeException
   *           if an unknown closure is specified.
   */
  public Set getSubClasses(byte closure);

  /**
   * Gets the super classes according to the desired closure.
   * 
   * @param closure
   *          either DIRECT_CLOSURE or TRASITIVE_CLOSURE
   * @return the set of super classes
   * @throws NoSuchClosureTypeException
   *           if an unknown closure is specified.
   */
  public Set getSuperClasses(byte closure);

  /**
   * Infers the sub classes transitive closure.
   */
  void inferSubClassesTransitiveClosure();

  /**
   * Infers the super classes transitive closure.
   */
  void inferSuperClassesTransitiveClosure();

  /**
   * Checks whether this class is a top.
   * 
   * @return true if this is a top class, otherwise - false.
   */
  public boolean isTopClass();

  /**
   * Dumps the class to string.
   * 
   * @return the string representation of the class.
   */
  public String toString();

  /**
   * Gets the super classes, and returns them in an array list where on each
   * index there is a collection of the super classes at distance - the index.
   * 
   * @return <b>distance</b> from this class to a <b>set of super classes</b>
   *         e.g. 1 : a,b 2 : c,d
   */
  public ArrayList getSuperClassesVSDistance();

  /**
   * Gets the sub classes, and returns them in an array list where on each index
   * there is a collection of the sub classes at distance - the index.
   * 
   * @return <b>distance</b> from this class to a <b>set of sub classes</b>
   *         e.g. 1 : a,b 2 : c,d
   */
  public ArrayList getSubClassesVSDistance();

  /**
   * Checks the equality of two classes.
   * 
   * @param o
   *          the ontology class to be tested versus this one.
   * @return true, if the classes are equal, otherwise - false.
   */
  public boolean equals(Object o);
} // class TClass
