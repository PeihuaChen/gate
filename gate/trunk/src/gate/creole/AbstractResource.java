/*
 *  AbstractResource.java
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
import java.beans.*;
import java.lang.reflect.*;


import gate.*;
import gate.util.*;


/** A convenience implementation of Resource with some default code.
  */
abstract public class AbstractResource
extends AbstractFeatureBearer implements Resource, Serializable
{
  static final long serialVersionUID = -9196293927841163321L;

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {
    return this;
  } // init()

    /** Sets the name of this resource*/
  public void setName(String name){
    this.name = name;
  }

  /** Returns the name of this resource*/
  public String getName(){
    return name;
  }

  protected String name;
  /**
   * releases the memory allocated to this resource
   */
  public void cleanup(){
  }

  //Parameters utility methods
  /**
   * Gets the value of a parameter for a resource.
   * @param resource the resource from which the parameter value will be
   * obtained
   * @param paramaterName the name of the parameter
   * @return the current value of the parameter
   */
  public static Object getParameterValue(Resource resource,
                                         String paramaterName)
                throws ResourceInstantiationException{
    // get the beaninfo for the resource bean, excluding data about Object
    BeanInfo resBeanInf = null;
    try {
      resBeanInf = Introspector.getBeanInfo(resource.getClass(), Object.class);
    } catch(Exception e) {
      throw new ResourceInstantiationException(
        "Couldn't get bean info for resource " + resource.getClass().getName()
        + Strings.getNl() + "Introspector exception was: " + e
      );
    }
    PropertyDescriptor[] properties = resBeanInf.getPropertyDescriptors();

    //find the property we're interested on
    if(properties == null){
      throw new ResourceInstantiationException(
        "Couldn't get properties info for resource " +
        resource.getClass().getName());
    }
    boolean done = false;
    int i = 0;
    Object value = null;
    while(!done && i < properties.length){
      PropertyDescriptor prop = properties[i];
      if(prop.getName().equals(paramaterName)){
        Method getMethod = prop.getReadMethod();
        if(getMethod == null){
          throw new ResourceInstantiationException(
            "Couldn't get read accessor method for parameter " + paramaterName +
            " in " + resource.getClass().getName());
        }
        // call the get method with the parameter value
        Object[] args = new Object[0];
        try {
          value = getMethod.invoke(resource, args);
        } catch(Exception e) {
          throw new ResourceInstantiationException(
            "couldn't invoke get method: " + e
          );
        }
        done = true;
      }//if(prop.getName().equals(paramaterName))
    }//while(!done && i < properties.length)
    if(done) return value;
    else throw new ResourceInstantiationException(
            "Couldn't find parameter named " + paramaterName +
            " in " + resource.getClass().getName());
  }

  /**
   * Sets the value for a specified parameter for a resource.
   *
   * @param resource the resource for which the parameter value will be set
   * @param paramaterName the name for the parameteer
   * @param parameterValue the value the parameter will receive
   */
  public static void setParameterValue(Resource resource,
                                       String paramaterName,
                                       Object parameterValue)
              throws ResourceInstantiationException{
    // get the beaninfo for the resource bean, excluding data about Object
    BeanInfo resBeanInf = null;
    try {
      resBeanInf = Introspector.getBeanInfo(resource.getClass(), Object.class);
    } catch(Exception e) {
      throw new ResourceInstantiationException(
        "Couldn't get bean info for resource " + resource.getClass().getName()
        + Strings.getNl() + "Introspector exception was: " + e
      );
    }
    PropertyDescriptor[] properties = resBeanInf.getPropertyDescriptors();
    //find the property we're interested on
    if(properties == null){
      throw new ResourceInstantiationException(
        "Couldn't get properties info for resource " +
        resource.getClass().getName());
    }
    boolean done = false;
    int i = 0;
    while(!done && i < properties.length){
      PropertyDescriptor prop = properties[i];
      if(prop.getName().equals(paramaterName)){
        Method setMethod = prop.getWriteMethod();
        if(setMethod == null){
          throw new ResourceInstantiationException(
            "Couldn't get write accessor method for parameter " +
            paramaterName + " in " + resource.getClass().getName());
        }

        // convert the parameter to the right type eg String -> URL
        if(parameterValue != null){
          Class propertyType = prop.getPropertyType();
          Class paramType = parameterValue.getClass();
          try {
            if(!propertyType.isAssignableFrom(paramType)) {
              parameterValue =
                propertyType.getConstructor(
                  new Class[]{paramType}
                ).newInstance( new Object[]{parameterValue} );
            }
          } catch(Exception e) {
            throw new ResourceInstantiationException(
              "Error converting " + parameterValue.getClass() +
              " to " + propertyType + ": " + e.toString()
            );
          }
        }//if(parameterValue != null)

        // call the set method with the parameter value
        Object[] args = new Object[1];
        args[0] = parameterValue;
        try {
          setMethod.invoke(resource, args);
        } catch(Exception e) {
          throw new ResourceInstantiationException(
            "couldn't invoke set method for " + paramaterName +
            " on " + resource.getClass().getName() + ": " + e);
        }
        done = true;
      }//if(prop.getName().equals(paramaterName))
      i++;
    }//while(!done && i < properties.length)
    if(!done) throw new ResourceInstantiationException(
                          "Couldn't find parameter named " + paramaterName +
                          " in " + resource.getClass().getName());
  }//public void setParameterValue(String paramaterName, Object parameterValue)


  /**
   * Sets the values for more parameters for a resource in one step.
   *
   * @param parameters a feature map that has paramete names as keys and
   * parameter values as values.
   */
  public static void setParameterValues(Resource resource,
                                        FeatureMap parameters)
              throws ResourceInstantiationException{

    Iterator parnameIter = parameters.keySet().iterator();
    while(parnameIter.hasNext()){
      String parName = (String)parnameIter.next();
      setParameterValue(resource, parName, parameters.get(parName));
    }
  }

  /**
   * Gets the value of a parameter of this resource.
   * @param paramaterName the name of the parameter
   * @return the current value of the parameter
   */
  public Object getParameterValue(String paramaterName)
                throws ResourceInstantiationException{
    return getParameterValue(this, paramaterName);
  }

  /**
   * Sets the value for a specified parameter for this resource.
   *
   * @param paramaterName the name for the parameter
   * @param parameterValue the value the parameter will receive
   */
  public void setParameterValue(String paramaterName, Object parameterValue)
              throws ResourceInstantiationException{
    setParameterValue(this, paramaterName, parameterValue);
  }

  /**
   * Sets the values for more parameters for this resource in one step.
   *
   * @param parameters a feature map that has paramete names as keys and
   * parameter values as values.
   */
  public void setParameterValues(FeatureMap parameters)
              throws ResourceInstantiationException{
    setParameterValues(this, parameters);
  }


} // class AbstractResource
