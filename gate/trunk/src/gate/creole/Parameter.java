/*
 *  Parameter.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 15/Oct/2000
 *
 *  $Id$
 */

package gate.creole;

import java.util.*;

import gate.*;
import gate.util.*;


/** Models a resource parameter.
  */
public class Parameter
{
  /** The type name of the parameter */
  String typeName;

  /** Set the type name for this parameter */
  public void setTypeName(String typeName) { this.typeName = typeName; }

  /** Get the type name for this parameter */
  public String getTypeName() { return typeName; }

  /** Is the parameter optional? */
  boolean optional = false;

  /** Set optionality of this parameter */
  public void setOptional(boolean optional) { this.optional = optional; }

  /** Is the parameter optional? */
  public boolean isOptional() { return optional; }

  /** Default value for the parameter */
  Object defaultValue;

  /** Calculate and return the default value for this parameter */
  public Object calculateDefaultValue() throws ParameterException {
    // nuke any previous default value as it may no longer be valid
    defaultValue = null;

    // get the Class for the parameter via Class.forName or CREOLE register
    Class paramClass = getParameterClass();

    // java builtin types
    if(typeName.startsWith("java.")) {
      if(typeName.equals("java.lang.Boolean"))
        defaultValue = Boolean.valueOf(defaultValueString);
      else if(typeName.equals("java.lang.Long"))
        defaultValue = Long.valueOf(defaultValueString);
      else if(typeName.equals("java.lang.Integer"))
        defaultValue = Integer.valueOf(defaultValueString);
      else if(typeName.equals("java.lang.String"))
        defaultValue = defaultValueString;
      else if(typeName.equals("java.lang.Double"))
        defaultValue = Double.valueOf(defaultValueString);
      else if(typeName.equals("java.lang.Float"))
        defaultValue = Float.valueOf(defaultValueString);
      else
        throw new ParameterException("Unsupported parameter type " + typeName);

    // resource types
    } else {
      if(resData == null)
        resData = (ResourceData) Gate.getCreoleRegister().get(typeName);
      if(resData == null)
        throw new ParameterException("No resource data for " + typeName);

      Stack instantiations = resData.getInstantiations();
      if(! instantiations.isEmpty())
        defaultValue = instantiations.peek();
    }

    return defaultValue;
  } // calculateDefaultValue()

  /** The resource data that this parameter is part of. */
  protected ResourceData resData;

  /** Get the default value for this parameter. If the value is
    * currently null it will try and calculate a value.
    * @see #calculateDefaultValue()
    */
  public Object getDefaultValue() throws ParameterException {
    if(defaultValue == null)
      calculateDefaultValue();

    return defaultValue;
  } // getDefaultValue

  /** Default value string (unprocessed, from the metadata)
    * for the parameter
    */
  String defaultValueString;

  /** Set the default value string (from the metadata)
    * for the parameter
    */
  public void setDefaultValueString(String defaultValueString) {
    this.defaultValueString = defaultValueString;
  } // setDefaultValueString

  /** Get the default value string (unprocessed, from the metadata)
    * for the parameter
    */
  public String getDefaultValueString() { return defaultValueString; }

  /** Comment for the parameter */
  String comment;

  /** Set the comment for this parameter */
  public void setComment(String comment) { this.comment = comment; }

  /** Get the comment for this parameter */
  public String getComment() { return comment; }

  /** Name for the parameter */
  String name;

  /** Set the name for this parameter */
  public void setName(String name) { this.name = name; }

  /** Get the name for this parameter */
  public String getName() { return name; }

  /** Is this a run-time parameter? */
  boolean runtime = false;

  /** Set runtime status of this parameter */
  public void setRuntime(boolean runtime) { this.runtime = runtime; }

  /** Is the parameter runtime? */
  public boolean isRuntime() { return runtime; }

  /** The Class for the parameter type */
  protected Class paramClass;

  /** Find the class for this parameter type. */
  protected Class getParameterClass() throws ParameterException
  {
    // get java builtin classes via class; else look in the register
    try {
      if(typeName.startsWith("java."))
          paramClass = Class.forName(typeName);
      else {
        ResourceData resData =
          (ResourceData) Gate.getCreoleRegister().get(typeName);
        if(resData == null)
          throw new ParameterException("No resource data for " + typeName);
        paramClass = resData.getResourceClass();
      }
    } catch(ClassNotFoundException e) {
      throw new ParameterException(
        "Couldn't find class " + typeName + ": " + Strings.getNl() + e
      );
    }

    if(paramClass == null)
      throw new ParameterException("Couldn't find class " + typeName);

    return paramClass;
  } // getParameterClass

  /** String representation */
  public String toString() {
    return "Parameter: valueString=" + typeName + "; optional=" + optional +
           "; defaultValueString=" + defaultValueString +
           "; defaultValue=" + defaultValue + "; comment=" +
           comment + "; runtime=" + runtime + "; name=" + name;
  } // toString()
} // class Parameter
