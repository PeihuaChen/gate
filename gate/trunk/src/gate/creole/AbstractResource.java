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
      i++;
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
  public static void setParameterValue(Resource resource, BeanInfo resBeanInf,
                                       String paramaterName,
                                       Object parameterValue)
              throws ResourceInstantiationException{
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

    Iterator parnameIter = parameters.keySet().iterator();
    while(parnameIter.hasNext()){
      String parName = (String)parnameIter.next();
      setParameterValue(resource, resBeanInf, parName, parameters.get(parName));
    }
  }


  /**
   * Adds listeners to a resource.
   * @param listeners The listeners to be registered with the resource. A
   * {@link java.util.Map} that maps from fully qualified class name (as a
   * string) to listener (of the type declared by the key).
   * @param resource the resource that listeners will be registered to.
   */
  public static void setResourceListeners(Resource resource, Map listeners)
  throws
    IntrospectionException, InvocationTargetException,
    IllegalAccessException, GateException
  {
    // get the beaninfo for the resource bean, excluding data about Object
    BeanInfo resBeanInfo = Introspector.getBeanInfo(
      resource.getClass(), Object.class
    );

    // get all the events the bean can fire
    EventSetDescriptor[] events = resBeanInfo.getEventSetDescriptors();

    // add the listeners
    if (events != null) {
      EventSetDescriptor event;
      for(int i = 0; i < events.length; i++) {
        event = events[i];

        // did we get such a listener?
        Object listener =
          listeners.get(event.getListenerType().getName());
        if(listener != null) {
          Method addListener = event.getAddListenerMethod();

          // call the set method with the parameter value
          Object[] args = new Object[1];
          args[0] = listener;
          addListener.invoke(resource, args);
        }
      } // for each event
    }   // if events != null
  } // setResourceListeners()

  /**
   * Removes listeners from a resource.
   * @param listeners The listeners to be removed from the resource. A
   * {@link java.util.Map} that maps from fully qualified class name
   * (as a string) to listener (of the type declared by the key).
   * @param resource the resource that listeners will be removed from.
   */
  public static void removeResourceListeners(Resource resource, Map listeners)
                     throws IntrospectionException, InvocationTargetException,
                            IllegalAccessException, GateException{

    // get the beaninfo for the resource bean, excluding data about Object
    BeanInfo resBeanInfo = Introspector.getBeanInfo(
      resource.getClass(), Object.class
    );

    // get all the events the bean can fire
    EventSetDescriptor[] events = resBeanInfo.getEventSetDescriptors();

    //remove the listeners
    if(events != null) {
      EventSetDescriptor event;
      for(int i = 0; i < events.length; i++) {
        event = events[i];

        // did we get such a listener?
        Object listener =
          listeners.get(event.getListenerType().getName());
        if(listener != null) {
          Method removeListener = event.getRemoveListenerMethod();

          // call the set method with the parameter value
          Object[] args = new Object[1];
          args[0] = listener;
          removeListener.invoke(resource, args);
        }
      } // for each event
    }   // if events != null
  } // removeResourceListeners()

  /**
   * Checks whether the provided {@link Resource} has values for all the
   * required parameters from the provided list of parameters.
   *
   * @param resource the resource being checked
   * @param paramters is a {@link List} of {@link List} of {@link Parameter}
   * representing a list of parameter disjunctions (e.g. the one returned by
   * {@link ParameterList#getRuntimeParameters()}).
   * @return <tt>true</tt> if all the required parameters have non null values,
   * <tt>false</tt> otherwise.
   * @throw {@link ResourceInstantiationException} if problems occur while
   * inspecting the parameters for the resource. These will normally be
   * introspection problems and are usually caused by the lack of a parameter
   * or of the read accessor for a parameter.
   */
  public static boolean checkParameterValues(Resource resource,
                                             List parameters)
                throws ResourceInstantiationException{
    Iterator disIter = parameters.iterator();
    while(disIter.hasNext()){
      List disjunction = (List)disIter.next();
      boolean required = !((Parameter)disjunction.get(0)).isOptional();
      if(required){
        //at least one parameter in the disjunction must have a value
        boolean valueSet = false;
        Iterator parIter = disjunction.iterator();
        while(!valueSet && parIter.hasNext()){
          Parameter par = (Parameter)parIter.next();
          valueSet = (resource.getParameterValue(par.getName()) != null);
        }
        if(!valueSet) return false;
      }
    }
    return true;
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
    // get the beaninfo for the resource bean, excluding data about Object
    BeanInfo resBeanInf = null;
    try {
      resBeanInf = Introspector.getBeanInfo(this.getClass(), Object.class);
    } catch(Exception e) {
      throw new ResourceInstantiationException(
        "Couldn't get bean info for resource " + this.getClass().getName()
        + Strings.getNl() + "Introspector exception was: " + e
      );
    }
    setParameterValue(this, resBeanInf, paramaterName, parameterValue);
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
