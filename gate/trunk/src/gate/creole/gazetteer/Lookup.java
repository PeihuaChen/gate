/*
 *  Lookup.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan, 11/07/2000
 *  borislav popov, 05/02/2002
 *
 *  $Id$
 */

package gate.creole.gazetteer;

/**
 * Used to describe a type of lookup annotations. A lookup is described by a
 * major type a minor type and a list of languages. Added members are :
 * ontologyClass and list.
 * All these values are strings (the list of languages is a string and it is
 * intended to represesnt a comma separated list).
 */
public class Lookup implements java.io.Serializable {

  /** Debug flag
   */
  private static final boolean DEBUG = false;

  /**
   * Creates a new Lookup value with the given major and minor types and
   * languages.
   *
   * @param major major type
   * @param minor minor type
   * @param theLanguages the languages
   */
  public Lookup(String theList, String major, String minor, String theLanguages){
    majorType = major;
    minorType = minor;
    languages = theLanguages;
    list = theList;
  }

  /** Tha major type for this lookup, e.g. "Organisation" */
  public String majorType;

  /** The minor type for this lookup, e.g. "Company"  */
  public String minorType;

  /** The languages for this lookup, e.g. "English, French" */
  public String languages;

  /** the ontology class of this lookup according to the mapping between
   *  list and ontology */
  public String oClass;

  /**  the ontology ID */
  public String ontology;

  /** the list represented by this lookup*/
  public String list;

  /**Returns a string representation of this lookup in the format
   * This method is used in equals()
   * that caused this method to implement dualistic behaviour :
   * i.e. whenever class and ontology are filled then use the
   * long version,incl. list, ontology and class;
   * else return just majorType.minorType */
  public String toString(){
    StringBuffer b = new StringBuffer();
    boolean longVersion = false;
    if (null!=ontology && null!=oClass){
      longVersion = true;
    }

    if ( longVersion ) {
      b.append(list);
      b.append(".");
    }
    b.append(majorType);
    b.append(".");
    if (null != minorType) {
      b.append(minorType);
      if (null!= languages) {
        b.append(".");
        b.append(languages);
      }//if
    }//if
    if (longVersion) {
      b.append("|");
      b.append(ontology);
      b.append(":");
      b.append(oClass);
    }
    return b.toString();
  }

  /**
   * 	Two lookups are equal if they have the same string representation
   *  (major type and minor type).
   * @param obj
   */
  public boolean equals(Object obj){
    if(obj instanceof Lookup) return obj.toString().equals(toString());
    else return false;
  } // equals

  /**    *
   */
  public int hashCode(){ return toString().hashCode();}

} // Lookup
