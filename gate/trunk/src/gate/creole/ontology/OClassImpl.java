/*
 * OClassImpl.java
 *
 * Copyright (c) 2002-2004, The University of Sheffield.
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
import java.util.Set;

public class OClassImpl extends TClassImpl implements OClass {
  private Set disjointClassesSet;
  private Set sameClassesSet;

  /**
   * Creates a new class given id,name,comment and ontology.
   * 
   * @param anId
   *          the id of the new class
   * @param aName
   *          the name of the new class
   * @param aComment
   *          the comment of the new class
   * @param anOntology
   *          the ontology to which the new class belongs
   */
  public OClassImpl(String anId, String aName, String aComment,
          Ontology anOntology) {
    super(anId, aName, aComment, anOntology);
    disjointClassesSet = new HashSet();
    sameClassesSet = new HashSet();
    setURI(ontology.getSourceURI() + name);
  }

  public void setDisjointWith(OClass theClass) {
    if(theClass == null) return;
    disjointClassesSet.add(theClass);
  }

  public void setSameClassAs(OClass theClass) {
    if(theClass == null) return;
    this.sameClassesSet.add(theClass);
  }

  public Set getDisjointClasses() {
    if(this.disjointClassesSet.isEmpty()) return null;
    return this.disjointClassesSet;
  }

  public Set getSameClasses() {
    if(this.sameClassesSet.isEmpty()) return null;
    return this.sameClassesSet;
  }

  public String toString() {
    return this.getName();
  }
}