/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 04/10/2001
 *
 *  $Id$
 *
 */

package gate.gui;

import gate.*;
import gate.creole.*;
import gate.util.*;

import java.util.*;

/**
 * Represents a list of Parameters which are alternative to each other.
 * This class only gives access to one of those parameters ot any one moment.
 * The currently accessible (selected) parameter can be changed using the
 * {@link setSelectedIndex(int index)} method.
 */
class ParameterDisjunction {

  /**
   * Creation from a resources and a list of names.
   * The initial values of the parameters will be read from the resource. If any
   * of these values is null than the default value will be used. After
   * initialisation  the values will be cached inside this object; any changes
   * made to these values will not affect the actual values on the resource.
   *
   * @param the resource these parameters belong to.
   * @param parameters a list containing the parameters in this paramater d
   * isjunction; each element is a {@link gate.creole.Parameter}.
   */
  public ParameterDisjunction(Resource resource, List parameters){
    params = new Parameter[parameters.size()];
    names = new String[parameters.size()];
    values = new Object[parameters.size()];
    comments = new String[parameters.size()];
    types = new String[parameters.size()];
    required = new Boolean[parameters.size()];

    for(int i = 0; i < parameters.size(); i++){
      params[i] = (Parameter)parameters.get(i);
      names[i] = params[i].getName();
      comments[i] = params[i].getComment();
      types[i] = params[i].getTypeName();
      try{
        values[i] = (resource == null) ?
                    null : resource.getParameterValue(params[i].getName());
      }catch(ResourceInstantiationException rie){
        values[i] = null;
      }
      try{
        if(values[i] == null) values[i] = params[i].getDefaultValue();
      }catch(ParameterException pe){
        values[i] = null;
      }
      required[i] = new Boolean(!params[i].isOptional());
    }

    setSelectedIndex(0);
  }

  /**
   * Sets the currently selected parameter for this disjunction.
   */
  public void setSelectedIndex(int index){
    selectedIndex = index;
  }

  /**
   * gets the number of parameters in this disjunction.
   */
  public int size(){
    return params.length;
  }

  /**
   * is the currently selected parameter required?
   */
  public Boolean isRequired(){
    return required[selectedIndex];
  }

  /**
   * returns the name of the curently selected parameter.
   */
  public String getName(){
    return names[selectedIndex];
  }

  /**
   * returns the comment for the curently selected parameter.
   */
  public String getComment(){
    return comments[selectedIndex];
  }

  /**
   * returns the type for the curently selected parameter.
   */
  public String getType(){
    return types[selectedIndex];
  }

  /**
   * Returns the names of the parameters in this disjunction.
   */
  public String[] getNames(){
    return names;
  }

  public void setValue(Object value){
    values[selectedIndex] = value;
  }

  public Object getValue(){
    return values[selectedIndex];
  }

  public Parameter[] getParameters(){
    return params;
  }

  int selectedIndex;
  String[] names;
  String[] comments;
  String[] types;
  Object[] values;
  Boolean[] required;
  Parameter[] params;
}////// class ParameterDisjunction