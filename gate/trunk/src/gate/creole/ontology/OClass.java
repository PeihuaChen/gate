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
import com.ontotext.gate.exception.NoSuchClosureTypeException;

/** interface representing a single ontology class */
public interface OClass {

  /**denotes a direct closure(no transitivity)*/
  public static final byte DIRECT_CLOSURE = 0;
  /**denotes atransitive closure */
  public static final byte TRANSITIVE_CLOSURE = 1;

  public String getId();

  public Ontology getOntology() ;

  public String getURI() ;

  public void setURI(String theURI) ;

  public String getComment();

  public void setComment(String aComment) ;

  public String getName() ;

  public void setName(String aName) ;

  public void addSubClass(OClass subClass) ;

  public void addSuperClass(OClass superClass) ;

  public void removeSubClass(OClass subClass) ;

  public void removeSuperClass(OClass superClass) ;

  public Set getSubClasses(byte closure) throws NoSuchClosureTypeException;

  public Set getSuperClasses(byte closure)throws NoSuchClosureTypeException ;

  void inferSubClassesTransitiveClosure();

  void inferSuperClassesTransitiveClosure();

  public boolean isTopClass();

  public String toString();

  /**
   *
   * @return <b>distance</b> from this class to a <b>set of super classes</b>
   * e.g. 1 : a,b
   *      2 : c,d
   */
  public ArrayList getSuperClassesVSDistance();

  /**
   *
   * @return <b>distance</b> from this class to a <b>set of sub classes</b>
   * e.g. 1 : a,b
   *      2 : c,d
   */
  public ArrayList getSubClassesVSDistance();


  public boolean equals(Object o);
} //class OClass