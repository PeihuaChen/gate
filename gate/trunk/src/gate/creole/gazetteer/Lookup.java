/*
 * Lookup.java
 *
 * Copyright (c) 2000-2001, The University of Sheffield.
 *
 * This file is part of GATE (see http://gate.ac.uk/), and is free
 * software, licenced under the GNU Library General Public License,
 * Version 2, June1991.
 *
 * A copy of this licence is included in the distribution in the file
 * licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 *
 * Valentin Tablan, 11/07/2000
 *
 * $Id$
 */

package gate.creole.gazetteer;

/** Used to describe a type of lookup annotations */
class Lookup {

  /** Debug flag */
  private static final boolean DEBUG = false;

  public Lookup(String major, String minor, String languages){
    majorType = major;
    minorType = minor;
    this.languages = languages;
  }

  String majorType;
  String minorType;
  String languages;

  public String toString(){
    if(null == minorType) return majorType;
    else return majorType + "." + minorType;
  }

  public boolean equals(Object obj){
    if(obj instanceof Lookup) return obj.toString().equals(toString());
    else return false;
  } // equals

  public int hashCode(){ return toString().hashCode();}

} // Lookup
