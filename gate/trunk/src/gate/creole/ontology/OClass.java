/*
 * OClass.java
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
 */
package gate.creole.ontology;

import java.util.*;
import gate.creole.ontology.*;


/**An Interface representing a single ontology class */
public interface OClass {

  /**denotes a direct closure(no transitivity)*/
  public static final byte DIRECT_CLOSURE = 0;

  /**denotes atransitive closure */
  public static final byte TRANSITIVE_CLOSURE = 1;

  /**Gets the id.
   * @return the id of the class
   */
  public String getId();

  /**Gets the ontology to which the class belongs.
   * @return  the ontology to which the class belongs
   */
  public Ontology getOntology() ;

  /**Gets the URI of the class.
   * @return the URI of the class
   */
  public String getURI() ;

  /**
   * Sets the URI of the class.
   * @param theURI the new URI to be set
   */
  public void setURI(String theURI) ;

  /** Gets the comment of the class.
   *  @return the comment of the class
   */
  public String getComment();

  /** Sets the class comment.
   * @param aComment the comment to be set
   */
  public void setComment(String aComment) ;

  /** Gets class name.
   *  @return the name of the class
   */
  public String getName() ;

  /** Sets the class name.
    * @param aName the new name of the class
    */
  public void setName(String aName) ;

  /**
   * Adds a sub class to this class.
   * @param subClass the subClass to be added.
   */
  public void addSubClass(OClass subClass) ;

  /** Adds a super class to this class.
   *  @param superClass the super class to be added
   */
  public void addSuperClass(OClass superClass) ;

  /**
   * Removes a sub class.
   * @param subClass the sub class to be removed
   */
  public void removeSubClass(OClass subClass) ;

  /**
   * Removes a super class.
   * @param superClass the super class to be removed
   */
  public void removeSuperClass(OClass superClass) ;

  /**
   * Gets the subclasses according to the desired closure.
   * @param closure either DIRECT_CLOSURE or TRASITIVE_CLOSURE
   * @return the set of subclasses
   * @throws NoSuchClosureTypeException if an unknown closure is specified.
   */
  public Set getSubClasses(byte closure) throws NoSuchClosureTypeException;

  /**
   * Gets the super classes according to the desired closure.
   * @param closure either DIRECT_CLOSURE or TRASITIVE_CLOSURE
   * @return the set of super classes
   * @throws NoSuchClosureTypeException if an unknown closure is specified.
   */
  public Set getSuperClasses(byte closure)throws NoSuchClosureTypeException ;

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
   * @return true if this is a top class, otherwise - false.
   */
  public boolean isTopClass();

  /**
   * Dumps the class to string.
   * @return the string representation of the class.
   */
  public String toString();

  /**
   * Gets the super classes, and returns them in an array list where on each index there
   * is a collection of the super classes at distance - the index.
   * @return <b>distance</b> from this class to a <b>set of super classes</b>
   * e.g. 1 : a,b
   *      2 : c,d
   */
  public ArrayList getSuperClassesVSDistance();

  /**
   * Gets the sub classes, and returns them in an array list where on each index there
   * is a collection of the sub classes at distance - the index.
   * @return <b>distance</b> from this class to a <b>set of sub classes</b>
   * e.g. 1 : a,b
   *      2 : c,d
   */
  public ArrayList getSubClassesVSDistance();


  /**
   * Checks the equality of two classes.
   * @param o the ontology class to be tested versus this one.
   * @return true, if the classes are equal, otherwise - false.
   */
  public boolean equals(Object o);

} //class OClass