/*
 * Taxonomy.java
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
 * borislav popov 02/2002
 *
 *
 *  $Id$
 */
package gate.creole.ontology;

import gate.LanguageResource;
import gate.creole.ResourceInstantiationException;
import gate.event.ObjectModificationListener;

import java.net.URL;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

/** Defines the interface of an ontology */
public interface Taxonomy extends LanguageResource {

  /**
   * Gets the label.
   * 
   * @return the label of the ontology
   */
  public String getLabel();

  /**
   * Sets the label of the ontology.
   * 
   * @param theLabel
   *          the label to be set
   */
  public void setLabel(String theLabel);

  /**
   * Gets the url of this ontology
   * 
   * @return the url of this ontology
   */
  public URL getURL();

  /**
   * Set the url of this ontology
   * 
   * @param aUrl
   *          the url to be set
   */
  public void setURL(URL aUrl);

  /**
   * Loads this ontology. According to different storages - different
   * implementations are expected. Should take care of the modifiedAfterLoading
   * member
   */
  public void load() throws ResourceInstantiationException;

  /**
   * Stores this ontology. According to different storages - different
   * implementations are expected. Should take care of the modifiedAfterLoading
   * member
   */
  public void store() throws ResourceInstantiationException;

  /**
   * Sets the URI of the ontology
   * 
   * @param theURI
   *          the URI to be set
   */
  public void setDefaultNameSpace(String theURI);

  /**
   * Gets the default name space for this ontology. This value is prepended
   * to local URIs (the ones not containing '#'.
   * 
   * @return a String value.
   */
  public String getDefaultNameSpace();

  /**
   * Sets version to this ontology.
   * 
   * @param theVersion
   *          the version to be set
   */
  public void setVersion(String theVersion);

  /**
   * Gets the version of this ontology.
   * 
   * @return the version of this ontology
   */
  public String getVersion();

  /**
   * Gets the id of this ontology.
   * 
   * @return the id of this ontology
   */
  public String getId();

  /**
   * Sets the id of this ontology.
   * 
   * @param theId
   *          the id to be set
   */
  public void setId(String theId);

  /**
   * Gets the comment of this ontology.
   * 
   * @return the comment of this ontology
   */
  public String getComment();

  /**
   * Sets the comment of this ontology.
   * 
   * @param theComment
   *          the comment to be set
   */
  public void setComment(String theComment);

  /**
   * Creates a new OClass and returns it.
   * 
   * @param aName
   *          the name of this class
   * @param aComment
   *          the comment of this class
   * @return the newly created class
   */
  public TClass createClass(String aName, String aComment);

  /**
   * Removes a class from this ontology.
   * 
   * @param theClass
   *          the class to be removed
   */
  public void removeClass(TClass theClass);

  /**
   * Adds a class to the ontology.
   * 
   * @param theClass
   *          the class to be added
   */
  public void addClass(TClass theClass);

  /**
   * Retrieves a class by its name.
   * 
   * @param theName
   *          the name of the class
   * @return the class matching the name or null if no matches.
   */
  public TClass getClassByName(String theName);

  /**
   * Checks if the ontology contains a class with the given name.
   * 
   * @param theName
   *          name of a class
   * @return true if the ontology contains a class with the name specified
   */
  public boolean containsClassByName(String theName);

  /**
   * Retrieves all classes as a set.
   * 
   * @return set of all the classes in this ontology
   */
  public Set getClasses();

  /**
   * Retireves an iterator over the classes, ordered according to the
   * comparator.
   * 
   * @param comp
   *          a comparator defining the order of iterating the classes
   * @return an iterator over the classes
   */
  public Iterator getClasses(Comparator comp);

  /**
   * Gets the top classes.
   * 
   * @return set of the top classes of this ontology
   */
  public Set getTopClasses();

  /**
   * Gets the taxonomic distance between 2 classes.
   * 
   * @param class1
   *          the first class
   * @param class2
   *          the second class
   * @return the taxonomic distance between the 2 classes
   */
  public int getTaxonomicDistance(TClass class1, TClass class2);

  /**
   * Check for subclass relation with transitive closure
   * 
   * @param cls1
   *          the first class
   * @param cls2
   *          the second class
   */
  public boolean isSubClassOf(String cls1, String cls2);

  /**
   * Check for subclass relation with direct closure
   * 
   * @param cls1
   *          the first class
   * @param cls2
   *          the second class
   */
  public boolean isDirectSubClassOf(String cls1, String cls2);

  /**
   * Checks the equality of two ontologies.
   * 
   * @param o
   *          the other ontology
   * @return true if the ontologies are considered equal, otherwise - false.
   */
  public boolean equals(Object o);

  /**
   * Sets the modified flag.
   * 
   * @param isModified
   *          sets this param as a value of the modified property of the
   *          ontology
   */
  public void setModified(boolean isModified);

  /**
   * Checks the modified flag.
   * 
   * @return whether the ontology has been modified after the loading
   */
  public boolean isModified();
  
  public void addObjectModificationListener(ObjectModificationListener listener);
}// interface Taxonomy
