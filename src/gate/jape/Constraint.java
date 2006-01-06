/*
 *  Constraint.java - transducer class
 *
 *  Copyright (c) 1998-2005, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 24/07/98
 *
 *  $Id$
 */


package gate.jape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import gate.FeatureMap;
import gate.util.SimpleFeatureMapImpl;


/**
  * An individual annotation/attribute/value expression. It doesn't extend
  * PatternElement, even though it has to "match", because a set of
  * Constraint must be applied together in order to avoid doing separate
  * selectAnnotations calls for each one.
  */
public class Constraint
implements JapeConstants, java.io.Serializable, Cloneable
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction from annot type string */
  public Constraint(String annotType) {
    this(annotType, new SimpleFeatureMapImpl());
  } // Construction from annot type

  /** Construction from annot type and attribute sequence */
  public Constraint(String annotType, FeatureMap attrs) {
    this.annotType = annotType;
    this.attrs1 = attrs;
  } // Construction from annot type and attribute sequence

  /** Construction from annot type and array of attributes */
  public Constraint(String annotType, ArrayList attrsArray) {
    this(annotType, new SimpleFeatureMapImpl());
    for ( Iterator i = attrsArray.iterator(); i.hasNext(); )
      attrs1.put(((JdmAttribute) i.next()).getName(),
                                          ((JdmAttribute) i.next()).getValue());
  } // Construction from annot type and array of attributes

  /** The type of annnotation we're looking for. */
  private String annotType;

  /** Are we negated? */
  private boolean negated = false;

  /** Set negation. */
  public void negate() { negated = true; }

  /** Access to negation flag. */
  public boolean isNegated() { return negated; }

  /** Get the type of annnotation we're looking for. */
  public String getAnnotType() { return annotType; }

  /** The attributes that must be present on the matched annotation. */
  private FeatureMap attrs1;

  /** The attributes array that must be present on the matched annotation. */
  private JdmAttribute[] attrs2;

  /** Get the attributes that must be present on the matched annotation. */
  public FeatureMap getAttributeSeq() { return attrs1; }

  /** Get the attributes that must be present on the matched annotation. */
  public JdmAttribute[] getAttributeArray() { return attrs2; }

  /** Add an attribute. */
  public void addAttribute(JdmAttribute attr) {
    attrs1.put(attr.getName(), attr.getValue());
  } // addAttribute

  /** Create and add an attribute. */
  public void addAttribute(String name, Object value) {
    attrs1.put(name, value);
  } // addAttribute

  /** Need cloning for processing of macro references. See comments on
    * <CODE>PatternElement.clone()</CODE>
    */
  public Object clone() {
    Constraint newC = null;
    try {
      newC = (Constraint) super.clone();
    } catch(CloneNotSupportedException e) {
      throw(new InternalError(e.toString()));
    }
    newC.annotType = annotType;
    newC.attrs1 = (FeatureMap) ((SimpleFeatureMapImpl) attrs1).clone();

    /* Enumeration e = attrs1.getElements();
       while(e.hasMoreElements())
         newC.attrs1.addAll(new JdmAttribute((JdmAttribute) e.nextElement()));
       newC.negated = negated;
    */
    return newC;
  } // clone

  /** Returns a boolean value indicating whether this Constraint is
    * equivalent to the given Constraint.  If the given object is not
    * a Constraint, compares the two objects using
    * <CODE>Object.equals()</CODE>.
    */
  public boolean equals(Object other) {
    if (!(other instanceof Constraint)) return super.equals(other);
    Constraint o = (Constraint) other;
    
    return (o.negated == negated &&
	    o.annotType.equals(annotType) &&
	    o.attrs1.toString().equals(attrs1.toString()));
  }

  /** Returns an integer hash code for this object.
    */
  public int hashCode() {
    int hashCode = negated ? 0 : 37 * 17;
    hashCode = 37 * hashCode + annotType.hashCode();
    hashCode = 37 * hashCode + attrs1.hashCode();
    return hashCode;
  }

 /** Finish: replace dynamic data structures with Java arrays; called
    * after parsing.
    */
  public void finish() {
    /*
    if(attrs1 == null || attrs1.size() == 0) {
      attrs2 = new JdmAttribute[0];
      attrs1 = null;
      return;
    }
    int attrsLen = attrs1.size();
    attrs2 = new JdmAttribute[attrsLen];

    int i = 0;
    //for(Enumeration e = attrs1.getElements(); e.hasMoreElements(); i++) {
    //  attrs2[i] = (JdmAttribute) e.nextElement();
    //}
    Iterator iter = attrs1.keySet().iterator();
    while(iter.hasNext()) {
      String name = (String) iter.next();
      Object value = attrs1.get(name);
      attrs2[i++] = new JdmAttribute(name, value);
    }
    attrs1 = null;
    */
  } // finish

  /** Create a string representation of the object. */
  public String toString() { return toString(""); }

  /** Create a string representation of the object. */
  public String toString(String pad) {
    StringBuffer buf = new StringBuffer
	(pad + "Constraint: " + annotType + (negated ? "!=" : "="));
    buf.append(attrs1 == null ? 
	       Arrays.asList(attrs2).toString() : 
	       attrs1.toString());
    return buf.toString();
  } // toString

  public String shortDesc() {
    String res = annotType + "(";
    if(attrs1 == null) {
      for(int i=0; i<attrs2.length; i++)
        res +=" " + attrs2[i];
    } else {
      res += attrs1.toString();
    }
    res += ")";
    return res;
  } // shortDesc

} // class Constraint


// $Log$
// Revision 1.14  2006/01/06 22:37:24  kwilliams
// Implement equals(Object) and hashCode() so we can usefully be put into Sets, HashMaps, etc.
//
// Revision 1.13  2006/01/06 22:03:04  kwilliams
// Define other constructors in terms of Constraint(String,FeatureMap)
//
// Revision 1.12  2005/07/15 15:37:32  valyt
// New toString() method from Ken Williams
//
// Revision 1.11  2005/01/11 13:51:36  ian
// Updating copyrights to 1998-2005 in preparation for v3.0
//
// Revision 1.10  2004/07/21 17:10:07  akshay
// Changed copyright from 1998-2001 to 1998-2004
//
// Revision 1.9  2004/03/25 13:01:14  valyt
// Imports optimisation throughout the Java sources
// (to get rid of annoying warnings in Eclipse)
//
// Revision 1.8  2001/09/13 12:09:49  kalina
// Removed completely the use of jgl.objectspace.Array and such.
// Instead all sources now use the new Collections, typically ArrayList.
// I ran the tests and I ran some documents and compared with keys.
// JAPE seems to work well (that's where it all was). If there are problems
// maybe look at those new structures first.
//
// Revision 1.7  2000/11/08 16:35:02  hamish
// formatting
//
// Revision 1.6  2000/10/26 10:45:30  oana
// Modified in the code style
//
// Revision 1.5  2000/10/16 16:44:33  oana
// Changed the comment of DEBUG variable
//
// Revision 1.4  2000/10/10 15:36:35  oana
// Changed System.out in Out and System.err in Err;
// Added the DEBUG variable seted on false;
// Added in the header the licence;
//
// Revision 1.3  2000/05/25 16:10:41  valyt
// JapeGUI is working
//
// Revision 1.2  2000/04/20 13:26:41  valyt
// Added the graph_drawing library.
// Creating of the NFSM and DFSM now works.
//
// Revision 1.1  2000/02/23 13:46:05  hamish
// added
//
// Revision 1.1.1.1  1999/02/03 16:23:01  hamish
// added gate2
//
// Revision 1.8  1998/11/05 13:36:30  kalina
// moved to use array of JdmAttributes for selectNextAnnotation instead of a sequence
//
// Revision 1.7  1998/11/01 22:35:56  kalina
// attribute seq hashtable mod
//
// Revision 1.6  1998/09/23 12:48:02  hamish
// negation added; noncontiguous BPEs disallowed
//
// Revision 1.5  1998/08/12 15:39:34  hamish
// added padding toString methods
//
// Revision 1.4  1998/07/31 13:12:14  mks
// done RHS stuff, not tested
//
// Revision 1.3  1998/07/30 11:05:15  mks
// more jape
//
// Revision 1.2  1998/07/29 11:06:55  hamish
// first compiling version
//
// Revision 1.1.1.1  1998/07/28 16:37:46  hamish
// gate2 lives
