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
import java.io.*;

import gate.*;
import gate.util.*;


/** Models a resource parameter.
  */
public class Parameter implements Serializable
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

  /** The name of the item's class. If the parameter is a collection then
    * we need  to know the class of its items in order to create them the
    * way we want.
    */
  String itemClassName = null;

  /** A set of strings representing suffixes for URL params*/
  Set suffixes = null;

  /** Calculate and return the default value for this parameter */
  public Object calculateDefaultValue() throws ParameterException {
    // if there's no default string and this is a builtin type, return null
    if(
      defaultValueString == null && typeName != null &&
      typeName.startsWith("java.")
    )
      return null;

    return calculateValueFromString(defaultValueString);
  } // calculateDefaultValue()

  /** Calculate and return the value for this parameter starting from a String
   */
  public Object calculateValueFromString(String stringValue)
  throws ParameterException {
    //if we have no string we can't construct a value
    Object value = null;

    // get the Class for the parameter via Class.forName or CREOLE register
    Class paramClass = getParameterClass();


    // Test if the paramClass is a collection and if it is, try to
    // construct the param as a collection of items specified in the
    // default string value...
    if (Collection.class.isAssignableFrom(paramClass) &&
        (!paramClass.isInterface())){
      // Create an collection object belonging to paramClass
      Collection colection = null;
      try{
        colection = (Collection)paramClass.getConstructor(new Class[]{}).
                                  newInstance(new Object[]{});
      } catch(Exception ex){
          throw new ParameterException("Could not construct an object of type "
            + typeName + " for param " + name +
            "\nProblem was: " + ex.toString());
      }// End try
      // If an itemClassName was specified then try to create objects belonging
      // to this class and add them to the collection. Otherwise add the
      // string tokens to the collection.
      if(itemClassName == null){
        // Read the tokens from the default value and try to create items
        // belonging to the itemClassName
        StringTokenizer strTokenizer = new StringTokenizer(
                                                      defaultValueString,";");
        while(strTokenizer.hasMoreTokens()){
          String itemStringValue = strTokenizer.nextToken();
          colection.add(itemStringValue);
        }// End while
      }else{
        Class itemClass = null;
        try{
          itemClass = Gate.getClassLoader().loadClass(itemClassName);
        }catch(ClassNotFoundException e){
          throw new ParameterException("Could not construct a class object for "
            + itemClassName + " for param "+ name +
            ", with type name="+ typeName);
        }// End try
        // Read the tokens from the default value and try to create items
        // belonging to the itemClassName
        StringTokenizer strTokenizer = new StringTokenizer(
                                                      defaultValueString,";");
        while(strTokenizer.hasMoreTokens()){
          // Read a string item and construct an object belonging to
          // itemClassName
          String itemStringValue = strTokenizer.nextToken();
          Object itemValue = null;
          try{
            itemValue = itemClass.getConstructor(new Class[]{String.class}).
                                  newInstance(new Object[]{itemStringValue});
          }catch(Exception e){
            throw new ParameterException("Could not create an object of " +
            itemClassName + " for param name "+ name + ", with type name ="+
            typeName);
          }// End try
          // Add the item value object to the collection
          colection.add(itemValue);
        }// End while
      }// End if(itemClassName == null)
      return colection;
    }// End if (Collection.class.isAssignableFrom(paramClass))
    // java builtin types
    if(typeName.startsWith("java.")) {
      if(typeName.equals("java.lang.Boolean"))
        value = Boolean.valueOf(stringValue);
      else if(typeName.equals("java.lang.Long"))
        value = Long.valueOf(stringValue);
      else if(typeName.equals("java.lang.Integer"))
        value = Integer.valueOf(stringValue);
      else if(typeName.equals("java.lang.String"))
        value = stringValue;
      else if(typeName.equals("java.lang.Double"))
        value = Double.valueOf(stringValue);
      else if(typeName.equals("java.lang.Float"))
        value = Float.valueOf(stringValue);
      else{
        //try to construct a new value from the string using a constructor
        // e.g. for URLs
        try{
          if(!paramClass.isAssignableFrom(String.class)){
            value = paramClass.getConstructor(new Class[]{String.class}).
                         newInstance(new Object[]{stringValue});
          }
        }catch(Exception e){
          throw new ParameterException("Unsupported parameter type " + typeName);
        }
      }
    } else {
      // non java types
      if(resData == null)
        resData = (ResourceData) Gate.getCreoleRegister().get(typeName);
      if(resData == null){
        //unknown type
        return null;
      }

      WeakBumpyStack instantiations = resData.getInstantiations();
      if(! instantiations.isEmpty()) value = instantiations.peek();
    }

    return value;
  } // calculateValueFromString()


  /** The resource data that this parameter is part of. */
  protected ResourceData resData;

  /** Get the default value for this parameter. If the value is
    * currently null it will try and calculate a value.
    * @see #calculateDefaultValue()
    */
  public Object getDefaultValue() throws ParameterException {
    return calculateDefaultValue();
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

  /** Get the suffixes atached with this param. If it's null then there are
   *  no suffices attached with it
   */
  public Set getSuffixes(){ return suffixes;}

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
      ResourceData resData = (ResourceData)
                             Gate.getCreoleRegister().get(typeName);
      if(resData == null){
        paramClass = Gate.getClassLoader().loadClass(typeName);
      }else{
        paramClass = resData.getResourceClass();
      }

//      if(typeName.startsWith("java."))
//          paramClass = Class.forName(typeName);
//      else {
//        ResourceData resData =
//          (ResourceData) Gate.getCreoleRegister().get(typeName);
//        if(resData == null)
//          throw new ParameterException(
//            "No resource data for " + typeName + " in Parameter/getParamClz"
//          );
//        paramClass = resData.getResourceClass();
//      }
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
    try{
      return "Parameter: name="+ name+ "; valueString=" + typeName +
             "; optional=" + optional +
             "; defaultValueString=" + defaultValueString +
             "; defaultValue=" + getDefaultValue() + "; comment=" +
             comment + "; runtime=" + runtime +
             "; itemClassName=" + itemClassName +
             "; suffixes=" + suffixes;
    }catch(ParameterException pe){
      throw new GateRuntimeException(pe.toString());
    }
  }

  /**
   * If this parameter is a List type this will return the type of the items
   * in the list. If the type is <tt>null</tt> String will be assumed.
   */
  public String getItemClassName() {
    return itemClassName;
  } // toString()
} // class Parameter
