/*
 *  Pair.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 13/Sept/2001
 *
 *  $Id$
 */


package gate.util;

// Imports
import java.lang.String;
import java.lang.Object;
import java.io.Serializable;

public class Pair implements Serializable {

  // Fields
  public Object first;
  public Object second;
  static final long serialVersionUID = 3690756099267025454L;

  // Constructors
  public Pair(Object p0, Object p1) { first = p0; second = p1;}
  public Pair() { first = null; second = null;}
  public Pair(Pair p0) {first = p0.first; second = p0.second; }

  // Methods
  public int hashCode() { return first.hashCode() ^ second.hashCode(); }
  public String toString() { return "<" + first.toString() +
                                    ", " + second.toString() + ">" ;}
  public boolean equals(Object p0) {
    if (!p0.getClass().equals(this.getClass()))
      return false;
    return equals((Pair) p0);
  }//equals
  public boolean equals(Pair p0) {
    if (p0.first.equals(first)&& p0.second.equals(second))
      return true;
    return false;
  } //equals
  public synchronized Object clone() { return new Pair(first, second); }
}