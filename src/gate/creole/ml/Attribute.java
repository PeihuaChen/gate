/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 19/11/2002
 *
 *  $Id$
 *
 */
package gate.creole.ml;

import java.util.*;
import java.io.*;

import org.jdom.*;

import gate.util.*;

/**
 * Describes an attribute associated to a ML instance.
 */

public class Attribute implements Serializable{

  public Attribute(Element jdomElement) throws GateException {
    //find the name
    Element anElement = jdomElement.getChild("NAME");
    if(anElement == null) throw new GateException(
      "Required element \"NAME\" not present in attribute:\n" +
      jdomElement.toString() + "!");
    else name = anElement.getTextTrim();

    //find the type
    anElement = jdomElement.getChild("TYPE");
    if(anElement == null) throw new GateException(
      "Required element \"TYPE\" not present in attribute:\n" +
      jdomElement.toString() + "!");
    else type = anElement.getTextTrim();

    //find the feature if present
    anElement = jdomElement.getChild("FEATURE");
    if(anElement != null)feature = anElement.getTextTrim();

    //find the position if present
    anElement = jdomElement.getChild("POSITION");
    if(anElement == null) position = 0;
    else position = Integer.parseInt(anElement.getTextTrim());

    //find the class if present
    isClass = jdomElement.getChild("CLASS") != null;

    //find the allowed values if present
    anElement = jdomElement.getChild("VALUES");
    if(anElement == null) values = null;
    else{
      values = new ArrayList();
      Iterator valuesIter = anElement.getChildren("VALUE").iterator();
      while(valuesIter.hasNext()){
        values.add(((Element)valuesIter.next()).getTextTrim());
      }
    }
  }

  public Attribute(){
    name = null;
    type =null;
    feature = null;
    isClass = false;
    position = 0;
    values = null;
  }

  public String toString(){
    StringBuffer res = new StringBuffer();
    res.append("Name: " + name + "\n");
    res.append("Type: " + type + "\n");
    res.append("Feature: " + feature + "\n");
    Iterator valIter = values.iterator();
    while(valIter.hasNext()){
      res.append("  Value:" + valIter.next().toString() + "\n");
    }
    return res.toString();
  }

  public boolean isClass(){
    return isClass;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void setFeature(String feature) {
    this.feature = feature;
  }

  public String getFeature() {
    return feature;
  }

  public java.util.List getValues() {
    return values;
  }

  public int getPosition() {
    return position;
  }

  public void setClass(boolean isClass) {
    this.isClass = isClass;
  }

  public void setValues(java.util.List values) {
    this.values = values;
  }

  public void setPosition(int position) {
    this.position = position;
  }

  boolean isClass = false;
  private String name;
  private String type;
  private String feature;
  private java.util.List values;
  private int position;
}