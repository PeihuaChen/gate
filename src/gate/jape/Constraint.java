/*
 *  Constraint.java - transducer class
 *
 *  Copyright (c) 2000-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June1991.
 *
 *  A copy of this licence is included in the distribution in the file
 *  licence.html, and is also available at http://gate.ac.uk/gate/licence.html.
 * 
 *  Hamish Cunningham, 24/07/98
 *
 *  $Id$
 */


package gate.jape;

import java.util.*;
import com.objectspace.jgl.*;
import gate.annotation.*;
import gate.util.*;
import gate.*;


/**
  * An individual annotation/attribute/value expression. It doesn't extend
  * PatternElement, even though it has to "match", because a set of
  * Constraint must be applied together in order to avoid doing separate
  * selectAnnotations calls for each one.
  */
public class Constraint
implements JapeConstants, java.io.Serializable, Cloneable
{
  /**
    *  This field is "final static" because it brings in
    *  the advantage of dead code elimination
    *  When DEBUG is set on false the code that it guardes will be eliminated
    *  by the compiler. This will spead up the progam a little bit.
    */
  private static final boolean DEBUG = false;

  /** Construction from annot type string */
  public Constraint(String annotType) {
    this.annotType = annotType;
    attrs1 = new SimpleFeatureMapImpl();
  } // Construction from annot type

  /** Construction from annot type and attribute sequence */
  public Constraint(String annotType, FeatureMap attrs) {
    this.annotType = annotType;
    this.attrs1 = attrs;
  } // Construction from annot type and attribute sequence

  /** Construction from annot type and array of attributes */
  public Constraint(String annotType, Array attrsArray) {
    this.annotType = annotType;
    attrs1 = new SimpleFeatureMapImpl();
    for(ArrayIterator i = attrsArray.begin(); ! i.atEnd(); i.advance())
      attrs1.put(((JdmAttribute) i.get()).getName(), ((JdmAttribute) i.get()).getValue());
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

//    Enumeration e = attrs1.getElements();
//    while(e.hasMoreElements())
//      newC.attrs1.addAll(new JdmAttribute((JdmAttribute) e.nextElement()));
//		newC.negated = negated;
    return newC;
  } // clone


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
    String newline = Strings.getNl();

    StringBuffer buf = new StringBuffer(
      pad + "Constraint: annotType(" + annotType + "); attrs(" + newline + pad
    );

    // constraints
//    for(int i=0; i<attrs.length(); i++)
//      buf.append(" " + attrs.nth(i));
//		for (Enumeration e = attrs.getElements(); e.hasMoreElements(); )
//    	buf.append(" " + ((JdmAttribute) e.nextElement() ).toString());
//    buf.append(newline + pad + ") Constraint." + newline);
    // constraints
    if(attrs1 == null) {
      for(int i=0; i<attrs2.length; i++)
        buf.append(" " + attrs2[i]);
    } else {
      //for (Enumeration e = attrs1.getElements(); e.hasMoreElements(); )
      //  buf.append(" " + ((JdmAttribute) e.nextElement() ).toString());
      buf.append(attrs1.toString());
    }
    buf.append(newline + pad + ") Constraint." + newline);

    return buf.toString();
  } // toString

  public String shortDesc(){
    String res = annotType + "(";
    if(attrs1 == null) {
      for(int i=0; i<attrs2.length; i++)
        res +=" " + attrs2[i];
    } else {
      res += attrs1.toString();
    }
    res += ")";
    return res;
  }
} // class Constraint


// $Log$
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