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



/** Represents a single ontology class.*/
public class OClassImpl implements OClass{

  /** the ontology to which the class belongs*/
  Ontology ontology;

  /** the URI of the class */
  String uri;

  /** the id of the class */
  String id;

  /**the name of the class */
  String name;

  /**the comment of the class*/
  String comment;

  /** the set of direct sub classes of this class */
  Set directSubClasses = new HashSet();

  /** the set of direct super classes of this class */
  Set directSuperClasses = new HashSet();

  /** The sub classes transitive closure set*/
  Set subClassesTransitiveClosure = new HashSet();

  /** The super classes transitive closure set*/
  Set superClassesTransitiveClosure = new HashSet();

  /**
   * Creates a new class given id,name,comment and ontology.
   * @param anId the id of the new class
   * @param aName the name of the new class
   * @param aComment the comment of the new class
   * @param anOntology the ontology to which the new class belongs
   */
  public OClassImpl(String anId, String aName, String aComment, Ontology anOntology) {
    id = anId;
    name = aName;
    comment = aComment;
    ontology = anOntology;
  }

  /**
   * Gets the id of the class.
   * @return the id of the class
   */
  public String getId(){
    return id;
  }

  /**
   * Gets the ontology to which this class is associated.
   * @return the ontology to which this class is associated.
   */
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
    try {
      if (null != (set = subClass.getSuperClasses(OClass.DIRECT_CLOSURE))) {
        if (!set.contains(this)) {
          subClass.addSuperClass(this);
        }
      }
      ontology.setModified(true);
    } catch (NoSuchClosureTypeException x) {
      throw new gate.util.GateRuntimeException(x.getMessage());
    }
  } // addSubClass();

  public void addSuperClass(OClass superClass) {
    try {
      directSuperClasses.add(superClass);
      Set set;
      if (null != (set = superClass.getSubClasses(OClass.DIRECT_CLOSURE))) {
        if (!set.contains(this)) {
          superClass.addSubClass(this);
        }
      }
      ontology.setModified(true);
    } catch (NoSuchClosureTypeException x) {
      throw new gate.util.GateRuntimeException(x.getMessage());
    }

  }

  public void removeSubClass(OClass subClass) {
    try {
      directSubClasses.remove(subClass);
      Set set;
      if (null!=(set=subClass.getSuperClasses(OClass.DIRECT_CLOSURE))){
        if ( set.contains(this) ) {
          subClass.removeSuperClass(this);
        }
      }
      ontology.setModified(true);
    } catch (NoSuchClosureTypeException x) {
      throw new gate.util.GateRuntimeException(x.getMessage());
    }
  }

  public void removeSuperClass(OClass superClass) {
    try {
      directSuperClasses.remove(superClass);
      Set set;
      if ( null != (set = superClass.getSubClasses(OClass.DIRECT_CLOSURE))) {
        if ( set.contains(this) ) {
          superClass.removeSubClass(this);
        }
      }
      ontology.setModified(true);
    } catch (NoSuchClosureTypeException x) {
      throw new gate.util.GateRuntimeException(x.getMessage());
    }
  }

  public Set getSubClasses(byte closure) throws NoSuchClosureTypeException {
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

  public Set getSuperClasses(byte closure) throws NoSuchClosureTypeException{
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
    try {

      List bag = new ArrayList(directSubClasses);
      subClassesTransitiveClosure = new HashSet();
      OClass currentClass;
      while (bag.size()>0) {
        currentClass = (OClass) bag.get(0);
        bag.remove(0);
        subClassesTransitiveClosure.add(currentClass);
        bag.addAll(currentClass.getSubClasses(OClass.DIRECT_CLOSURE));
      } //while bag is not empty

    } catch (NoSuchClosureTypeException x) {
      throw new gate.util.GateRuntimeException(x.getMessage());
    }

  } // inferSubClassesTransitiveClosure();

  public void inferSuperClassesTransitiveClosure(){
    try {
      List bag = new ArrayList(directSuperClasses);
      superClassesTransitiveClosure = new HashSet();
      OClass currentClass;
      while (bag.size()>0) {
        currentClass = (OClass) bag.get(0);
        bag.remove(0);
        superClassesTransitiveClosure.add(currentClass);
        bag.addAll(currentClass.getSuperClasses(OClass.DIRECT_CLOSURE));
      } //while bag is not empty
    } catch (NoSuchClosureTypeException x) {
      throw new gate.util.GateRuntimeException(x.getMessage());
    }


  } // inferSuperClassesTransitiveClosure();

  public boolean isTopClass(){
    return directSuperClasses.size() == 0;
  }

  public String toString(){
    return name;
  }




  public static Set getSubClasses(byte closure,Set classes) {
    try {
      Set result = new HashSet();
      Iterator ci = classes.iterator();
      OClass c;
      while (ci.hasNext()) {

        c = (OClass) ci.next();
        result.addAll(c.getSubClasses(closure));

      }// while classes
      return result;
    } catch (NoSuchClosureTypeException x) {
      throw new gate.util.GateRuntimeException(x.getMessage());
    }

  } // getSubClasses()


  public static Set getSuperClasses(byte closure,Set classes) {
    try {
      Set result = new HashSet();
      Iterator ci = classes.iterator();
      OClass c;
      while (ci.hasNext()) {

        c = (OClass) ci.next();
        result.addAll(c.getSuperClasses(closure));

      }// while classes
      return result;
    } catch (NoSuchClosureTypeException x) {
      throw new gate.util.GateRuntimeException(x.getMessage());
    }

  } // getSuperClasses()


  public ArrayList getSubClassesVSDistance() {
    try {
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
    } catch (NoSuchClosureTypeException x) {
      throw new gate.util.GateRuntimeException(x.getMessage());
    }

  } // getSubClassesVSDistance()



  public ArrayList getSuperClassesVSDistance() {
    try {
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
    } catch (NoSuchClosureTypeException x) {
      throw new gate.util.GateRuntimeException(x.getMessage());
    }

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