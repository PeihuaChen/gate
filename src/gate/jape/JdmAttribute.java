/*
 *  JdmAttribute.java
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
 *  Kalina Bontcheva, 23/02/2000
 *
 *  $Id$
 *
 *  Description:  This is JDM aimed at repeating the functionality of GDM
 */

package gate.jape;

import gate.*;
import gate.util.*;
import java.io.Serializable;

/**
  * THIS CLASS SHOULDN'T BE HERE. Please let's all ignore it, and maybe
  * it will go away.
  * <P>
  * Implements the TIPSTER and GDM API for attributes.
  * Test code in <code>testAttributes</code> class. <P>
  * The JdmAttribute class would accept all java serialisable classes, all
  * jdm classes and also all user-defined classes provided they implement
  * the Serializable interface. This restriction is necessary  since Jdm
  * uses Java serialisation to ensure object persistency. However, making
  * classes serialisable is usually quite straightforward. <P>
  * @author Kalina Bontcheva
*/
public class JdmAttribute implements Serializable {

  /** Debug flag */
  private static final boolean DEBUG = false;

  private String name;
  private Object value;

  protected JdmAttribute() {
  }

  /** throws JdmException when the value isn't one of the types we know
    * how to store, i.e., a serialisable or Jdm class.
    */
  public JdmAttribute(String name, Object value) {
    this.name = name; this.value = value;
  }

  /** throws JdmException when the value isn't one of the types we know
    * how to store, i.e., a serialisable or Jdm class.
    */
  public JdmAttribute(JdmAttribute jdmAttr) {
  	String name = jdmAttr.getName();
    Object value = jdmAttr.getValue();
  }

  public String getName() {
  	return name;
  }

  public Object getValue() {
  	return value;
  }

  public String getValueType() {
  	return value.getClass().getName();
  }

  public boolean equals(Object obj) {
    JdmAttribute a = (JdmAttribute) obj;
    return a.getName().equals(name) && a.getValue().equals(value);
  }

  public String toString() {
         return "JdmAttr: name=" + name + "; value=" + value.toString();

  }

} // class JdmAttribute
