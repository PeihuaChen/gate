/*
 *  OClass.java
 *
 *  Niraj Aswani, 09/March/07
 *
 *  $Id$
 */
package gate.creole.ontology;

import java.util.ArrayList;
import java.util.Set;

/**
 * Each OClass (Ontology Class) represents a concept/class in ontology.
 * It provides various methods (including and not limited) to iterate
 * through its super and sub classes in the taxonomy hierarchy.
 * 
 * @author niraj
 * 
 */
public interface OClass extends OResource, OConstants {
  /**
   * Adds a sub class to this class.
   * 
   * @param subClass the subClass to be added.
   */
  public void addSubClass(OClass subClass);

  /**
   * Adds a super class to this class.
   * 
   * @param superClass the super class to be added
   */
  public void addSuperClass(OClass superClass);

  /**
   * Removes a sub class.
   * 
   * @param subClass the sub class to be removed
   */
  public void removeSubClass(OClass subClass);

  /**
   * Removes a super class.
   * 
   * @param superClass the super class to be removed
   */
  public void removeSuperClass(OClass superClass);

  /**
   * Gets the subclasses according to the desired closure.
   * 
   * @param closure either DIRECT_CLOSURE or TRASITIVE_CLOSURE
   * @return the set of subclasses
   */
  public Set<OClass> getSubClasses(byte closure);

  /**
   * Gets the super classes according to the desired closure.
   * 
   * @param closure either DIRECT_CLOSURE or TRASITIVE_CLOSURE
   * @return the set of super classes
   */
  public Set<OClass> getSuperClasses(byte closure);

  /**
   * Checks whether the class is a super class of the given class.
   * 
   * @param aClass
   * @param closure either OntologyConstants.DIRECT_CLOSURE or
   *          OntologyConstants.TRANSITIVE_CLOSURE
   * @return true, if the class is a super class of the given class,
   *         otherwise - false.
   */
  public boolean isSuperClassOf(OClass aClass, byte closure);

  /**
   * Checks whether the class is a sub class of the given class.
   * 
   * @param aClass
   * @param closure either OntologyConstants.DIRECT_CLOSURE or
   *          OntologyConstants.TRANSITIVE_CLOSURE
   * @return true, if the class is a sub class of the given class,
   *         otherwise - false.
   */
  public boolean isSubClassOf(OClass aClass, byte closure);

  /**
   * Checks whether this class is a top.
   * 
   * @return true if this is a top class, otherwise - false.
   */
  public boolean isTopClass();

  /** Indicates that these classes are the equivalent */
  public void setEquivalentClassAs(OClass theClass);

  /**
   * Returns a set of all classes that are equivalent as this one. Null
   * if no such classes.
   */
  public Set<OClass> getEquivalentClasses();

  /**
   * Checks whether the class is equivalent as the given class.
   * 
   * @param aClass
   * @return true, if the class is equivalent as the aClass, otherwise -
   *         false.
   */
  public boolean isEquivalentClassAs(OClass aClass);

  /**
   * Gets the super classes, and returns them in an array list where on
   * each index there is a collection of the super classes at distance -
   * the index.
   */
  public ArrayList<Set<OClass>> getSuperClassesVSDistance();

  /**
   * Gets the sub classes, and returns them in an array list where on
   * each index there is a collection of the sub classes at distance -
   * the index.
   */
  public ArrayList<Set<OClass>> getSubClassesVsDistance();

}
