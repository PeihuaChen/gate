
//Title:        Java Document Manager
//Version:      $Id$
//Copyright:    Copyright (c) 1997
//Author:       Kalina Bontcheva
//Company:      NLP Group, DCS, Univ. of Sheffield
//Description:  This is JDM aimed at repeating the functionality of GDM


package gate.jape;

import gate.*;
import gate.util.*;
import java.io.Serializable;

/**
THIS CLASS SHOULDN'T BE HERE. Please let's all ignore it, and maybe
it will go away.
<P>
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

}

