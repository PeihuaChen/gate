/*
 * OClassImpl.java
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

import com.ontotext.gate.exception.*;

/** represents a single ontology class
 *  @todo: infer methods hidden but always invoked when needed
 *  @todo: safe Set of classes*/
public class OClassImpl implements OClass{

  Ontology ontology;
  String uri;
  String id;
  String name;
  String comment;
  Set directSubClasses = new HashSet();
  Set directSuperClasses = new HashSet();
  Set subClassesTransitiveClosure = new HashSet();
  Set superClassesTransitiveClosure = new HashSet();

  public OClassImpl(String anId, String aName, String aComment, Ontology anOntology) {
    id = anId;
    name = aName;
    comment = aComment;
    ontology = anOntology;
  }

  public String getId(){
    return id;
  }

  public Ontology getOntology() {
    return ontology;
  }

  public String getURI() {
    return uri;
  }

  public void setURI(String theURI) {
    if (-1 == theURI.indexOf('#')){
      theURI = getOntology().getSourceURI()+'#'+theURI;
    }
    uri = theURI;
    ontology.setModified(true);
  }

  public String getComment(){
    return comment;
  }

  public void setComment(String aComment) {
    comment = aComment;
    ontology.setModified(true);
  }

  public String getName() {
    return name;
  }

  public void setName(String aName) {
    name = aName;
    ontology.setModified(true);
  }

  public void addSubClass(OClass subClass) {
    this.directSubClasses.add(subClass);
    Set set;
    if (null != (set = subClass.getSuperClasses(OClass.DIRECT_CLOSURE))) {
      if (!set.contains(this)) {
        subClass.addSuperClass(this);
      }
    }
    ontology.setModified(true);
  } // addSubClass();

  public void addSuperClass(OClass superClass) {
    directSuperClasses.add(superClass);
    Set set;
    if (null != (set = superClass.getSubClasses(OClass.DIRECT_CLOSURE))) {
      if (!set.contains(this)) {
        superClass.addSubClass(this);
      }
    }
    ontology.setModified(true);
  }

  public void removeSubClass(OClass subClass) {
    directSubClasses.remove(subClass);
    Set set;
    if (null!=(set=subClass.getSuperClasses(OClass.DIRECT_CLOSURE))){
      if ( set.contains(this) ) {
        subClass.removeSuperClass(this);
      }
    }
    ontology.setModified(true);
  }

  public void removeSuperClass(OClass superClass) {
    directSuperClasses.remove(superClass);
    Set set;
    if ( null != (set = superClass.getSubClasses(OClass.DIRECT_CLOSURE))) {
      if ( set.contains(this) ) {
        superClass.removeSubClass(this);
      }
    }
    ontology.setModified(true);
  }

  public Set getSubClasses(byte closure){
    Set result;
    switch (closure) {
      case DIRECT_CLOSURE : {
        result = directSubClasses;
        break;
      }
      case TRANSITIVE_CLOSURE : {
        if (0==subClassesTransitiveClosure.size() ||
            getOntology().isModified() ) {
            /* infer again */
            inferSubClassesTransitiveClosure();
        } // if should infer again

        result = subClassesTransitiveClosure;
        break;
      }
      default : {
        throw new NoSuchClosureTypeException(closure);
      }
    } //switch

    return new HashSet(result);
  } // getSubClasses()

  public Set getSuperClasses(byte closure) {
    Set result;
    switch (closure) {
      case DIRECT_CLOSURE : {
        result = directSuperClasses;
        break;
      }
      case TRANSITIVE_CLOSURE : {
        if (0==superClassesTransitiveClosure.size() ||
            getOntology().isModified() ) {
            /* infer again */
            inferSuperClassesTransitiveClosure();
        } // if should infer again
        result = superClassesTransitiveClosure;
        break;
      }
      default : {
        throw new NoSuchClosureTypeException(closure);
      }
    } //switch

    return new HashSet(result);
  } // getSuperClasses()

  public void inferSubClassesTransitiveClosure(){
    List bag = new ArrayList(directSubClasses);
    subClassesTransitiveClosure = new HashSet();
    OClass currentClass;
    while (bag.size()>0) {
      currentClass = (OClass) bag.get(0);
      bag.remove(0);
      subClassesTransitiveClosure.add(currentClass);
      bag.addAll(currentClass.getSubClasses(OClass.DIRECT_CLOSURE));
    } //while bag is not empty

  } // inferSubClassesTransitiveClosure();

  public void inferSuperClassesTransitiveClosure(){
    List bag = new ArrayList(directSuperClasses);
    superClassesTransitiveClosure = new HashSet();
    OClass currentClass;
    while (bag.size()>0) {
      currentClass = (OClass) bag.get(0);
      bag.remove(0);
      superClassesTransitiveClosure.add(currentClass);
      bag.addAll(currentClass.getSuperClasses(OClass.DIRECT_CLOSURE));
    } //while bag is not empty

  } // inferSuperClassesTransitiveClosure();

  public boolean isTopClass(){
    return directSuperClasses.size() == 0;
  }

  public String toString(){
    return name;
  }



  /**
   * get the subclasses closure of a set of classes
   * @param closure
   * @param classes
   * @return subclasses closure of a set of classes
   */
  public static Set getSubClasses(byte closure,Set classes) {
    Set result = new HashSet();
    Iterator ci = classes.iterator();
    OClass c;
    while (ci.hasNext()) {

      c = (OClass) ci.next();
      result.addAll(c.getSubClasses(closure));

    }// while classes
    return result;
  } // getSubClasses()

  /**
   * get the super classes closure of a set of classes
   * @param closure
   * @param classes
   * @return super classes closure of a set of classes
   */
  public static Set getSuperClasses(byte closure,Set classes) {
    Set result = new HashSet();
    Iterator ci = classes.iterator();
    OClass c;
    while (ci.hasNext()) {

      c = (OClass) ci.next();
      result.addAll(c.getSuperClasses(closure));

    }// while classes
    return result;
  } // getSuperClasses()


  /**
   *
   * @return <b>distance</b> from this class to a <b>set of sub classes</b>
   * e.g. 1 : a,b
   *      2 : c,d
   */
  public ArrayList getSubClassesVSDistance() {
    ArrayList result = new ArrayList();
    Set set;
    int level = 0;
    OClass c;
    Set levelSet = new HashSet();
    levelSet.add(this);
    boolean rollon = (0 < this.getSubClasses(OClass.DIRECT_CLOSURE).size());

    while (rollon) {
      /* iterate over all the classes in levelSet and infre their subclasses in set*/
      set = new HashSet();
      Iterator li = levelSet.iterator();
      while (li.hasNext()) {
        c = (OClass) li.next();
        set.addAll(c.getSubClasses(OClass.DIRECT_CLOSURE));
      } //while leveset
      if ( 0 < set.size() ) {
        result.add(level++,set);
      }
      levelSet = set;
      rollon = 0 < levelSet.size();
    } // while sublcasses
    return result;
  } // getSubClassesVSDistance()


  /**
   *
   * @return <b>distance</b> from this class to a <b>set of super classes</b>
   * e.g. 1 : a,b
   *      2 : c,d
   */
  public ArrayList getSuperClassesVSDistance() {
    ArrayList result = new ArrayList();
    Set set;
    int level = 0;
    OClass c;
    Set levelSet = new HashSet();
    levelSet.add(this);
    boolean rollon = (0 < this.getSuperClasses(OClass.DIRECT_CLOSURE).size());

    while (rollon) {
      /* iterate over all the classes in levelSet and infre their subclasses in set*/
      set = new HashSet();
      Iterator li = levelSet.iterator();
      while (li.hasNext()) {
        c = (OClass) li.next();
        set.addAll(c.getSuperClasses(OClass.DIRECT_CLOSURE));
      } //while leveset
      if ( 0 < set.size() ) {
        result.add(level++,set);
      }
      levelSet = set;
      rollon = 0 < levelSet.size();
    } // while superlcasses
    return result;
  } // getSuperClassesVSDistance()


  public boolean equals(Object o) {
    boolean result = false;
    if ( o instanceof OClass ) {
      OClass c = (OClass) o;
      result = true;
      if (null != this.getId() & null!= c.getId())
        result &= this.getId().equals(c.getId());
      else
        result = this.getId() == c.getId();

      if (null != this.getName() & null!= c.getName())
        result &= this.getName().equals(c.getName());
      else
        result = this.getName() == c.getName();

      if (null != this.getOntology() & null!= c.getOntology())
        result &=  this.getOntology().equals(c.getOntology());
      else
        result = this.getOntology() == c.getOntology();

      if (null != this.getURI() & null!= c.getURI())
        result &= this.getURI().equals(c.getURI());
      else
        result = this.getURI() == c.getURI();
    }
    return result;
  } // equals

} //class OClassImpl