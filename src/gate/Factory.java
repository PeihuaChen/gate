/*
 *  Factory.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 25/May/2000
 *
 *  $Id$
 */

package gate;

import java.util.*;
import java.net.*;
import java.io.*;
import java.beans.*;
import java.lang.reflect.*;

import gate.corpora.*;
import gate.util.*;
import gate.annotation.*;
import gate.creole.*;
import gate.persist.*;
import gate.event.*;

/** Provides static methods for the creation of Resources.
  */
public abstract class Factory
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** The CREOLE register */
  private static CreoleRegister reg = Gate.getCreoleRegister();

  /** The DataStore register */
  private static DataStoreRegister dsReg = Gate.getDataStoreRegister();

  /** Create an instance of a resource using default parameter values.
    * @see #createResource(String,FeatureMap)
    */
  public static Resource createResource(String resourceClassName)
  throws ResourceInstantiationException
  {
    // get the resource metadata
    ResourceData resData = (ResourceData) reg.get(resourceClassName);
    if(resData == null)
      throw new ResourceInstantiationException(
        "Couldn't get resource data for " + resourceClassName
      );

    // get the parameter list and default values
    ParameterList paramList = resData.getParameterList();
    FeatureMap parameterValues = null;
    try {
      parameterValues = paramList.getInitimeDefaults();
    } catch(ParameterException e) {
      throw new ResourceInstantiationException(
        "Couldn't get default parameters for " + resourceClassName + ": " + e
      );
    }

    return createResource(resourceClassName, parameterValues);
  } // createResource(resClassName)

  /** Create an instance of a resource, and return it.
    * Callers of this method are responsible for
    * querying the resource's parameter lists, putting together a set that
    * is complete apart from runtime parameters, and passing a feature map
    * containing these parameter settings.
    *
    * @param resourceClassName the name of the class implementing the resource.
    * @param parameterValues the feature map containing
    *   parameterValues for the resource.
    * @return an instantiated resource.
    */
  public static Resource createResource(
    String resourceClassName, FeatureMap parameterValues
  ) throws ResourceInstantiationException
   {
    // get the resource metadata
    ResourceData resData = (ResourceData) reg.get(resourceClassName);
    if(resData == null)
      throw new ResourceInstantiationException(
        "Couldn't get resource data for " + resourceClassName
      );

    // get the default implementation class
    Class resClass = null;
    try {
      resClass = resData.getResourceClass();
    } catch(ClassNotFoundException e) {
      throw new ResourceInstantiationException(
        "Couldn't get resource class from the resource data:"+Strings.getNl()+e
      );
    }

    // create an object using the resource's default constructor
    Resource res = null;
    try {
      if(DEBUG) Out.prln("Creating resource " + resClass.getName());
      res = (Resource) resClass.newInstance();
    } catch(IllegalAccessException e) {
      throw new ResourceInstantiationException(
        "Couldn't create resource instance, access denied: " + e
      );
    } catch(InstantiationException e) {
      throw new ResourceInstantiationException(
        "Couldn't create resource instance due to newInstance() failure: " + e
      );
    }

    // type-specific stuff for LRs
    if(LanguageResource.class.isAssignableFrom(resClass)) {
      if(DEBUG) Out.prln(resClass.getName() + " is an LR");

      DataStore dataStore = (DataStore) parameterValues.get("DataStore");
      if(dataStore != null) {
        if(dataStore instanceof SerialDataStore) {
          // SDS doesn't need a wrapper class; just check for serialisability
          if(! (resClass instanceof Serializable))
            throw new ResourceInstantiationException(
              "Resource cannot be (de-)serialized: " + resClass.getName()
            );
        } else { // non-serialisation datastores

          // find an appropriate wrapper class and replace resClass

          /* resClass = dataStoreWrapperClass
             if none available then
             throw new ResourceInstantiationException(
               "Unknown wrapper class " + dataStoreWrapperClass
             );      OR maybe UnknownDataStoreException
          */
        }

        // get the datastore instance id and retrieve the resource
        String instanceId = (String) parameterValues.get("DataStoreInstanceId");
        if(instanceId == null)
          throw new
            ResourceInstantiationException("No instance id for " + resClass);
        try {
          res = dataStore.getLr(resClass.getName(), instanceId);
        } catch(PersistenceException e) {
          throw new ResourceInstantiationException("Bad read from DB: " + e);
        }
        resData.addInstantiation(res);
        return res;
      } // datastore was present

    // type-specific stuff for PRs
    } else if(ProcessingResource.class.isAssignableFrom(resClass)) {
      if(DEBUG) Out.prln(resClass.getName() + " is a PR");

    // type-specific stuff for VRs
    } else if(VisualResource.class.isAssignableFrom(resClass)) {
      if(DEBUG) Out.prln(resClass.getName() + " is a VR");

    // we have a resource which is not an LR, PR or VR
    } else {
      Err.prln("WARNING: instantiating resource which is not a PR, LR or VR:");
      Err.prln(resData + "END OF WARNING" + Strings.getNl());
    }

    // set the parameterValues of the resource and add the listeners
    List listenersToRemove;
    try {
      if(DEBUG) Out.prln("Setting the parameters for  " + res.toString());
      listenersToRemove = setResourceParameters(res, parameterValues);
    } catch(Exception e) {
      if(DEBUG) Out.prln("Failed to set the parameters for " + res.toString());
      throw new ResourceInstantiationException("Parameterisation failure" + e);
    }

    // if the features of the resource have not been explicitly set,
    // set them to the features of the resource data
    if(res.getFeatures() == null || res.getFeatures().isEmpty()){
      FeatureMap fm = newFeatureMap();
      fm.putAll(resData.getFeatures());
      res.setFeatures(fm);
    }

    // initialise the resource
    if(DEBUG) Out.prln("Initialising resource " + res.toString());
    res = res.init();

    //remove all the listeners added
    if(listenersToRemove != null){
      Iterator listenersIter = listenersToRemove.iterator();
      while(listenersIter.hasNext()){
        Object[] data = (Object[])listenersIter.next();
        try {
          if(DEBUG) Out.prln("Removing listeners for  " + res.toString());
          ((Method)data[0]).invoke(res, new Object[]{data[1]});
        } catch(Exception e) {
          if(DEBUG) Out.prln("Failed to remove listeners for " + res);
          throw new ResourceInstantiationException(
            "Failed to remove listeners for " + e
          );
        }
      }
    }

    // record the instantiation on the resource data's stack
    resData.addInstantiation(res);
    ((CreoleRegisterImpl)reg).fireResourceLoaded(
                               new CreoleEvent(res, CreoleEvent.RESOURCE_LOADED)
                              );
    return res;
  } // create(resourceClassName)

  /** Delete an instance of a resource. This involves removing it from
    * the stack of instantiations maintained by this resource type's
    * resource data. Deletion does not guarantee that the resource will
    * become a candidate for garbage collection, just that the GATE framework
    * is no longer holding references to the resource.
    *
    * @param resource the resource to be deleted.
    */
  public static void deleteResource(Resource resource) {
    ResourceData rd =
      (ResourceData) reg.get(resource.getClass().getName());
    List instances = rd.getInstantiations();
    instances.remove(resource);
    ((CreoleRegisterImpl)reg).fireResourceUnloaded(
            new CreoleEvent(resource, CreoleEvent.RESOURCE_UNLOADED)
    );
  } // deleteResource

  /** For each paramter, set the appropriate property on the resource
    * using bean-style reflection.
    *
    * @see java.beans.Introspector
    * @param resource the resource to be parameterised.
    * @param parameterValues the parameters and their values.
    */
  public static List setResourceParameters(
    Resource resource, FeatureMap parameterValues
  ) throws
    IntrospectionException, InvocationTargetException,
    IllegalAccessException, GateException
  {
    // the number of parameters that we manage to set on the bean
    int numParametersSet = 0;
    if(DEBUG) {
      Out.prln("setResourceParameters, params = ");
      Iterator iter = parameterValues.entrySet().iterator();
      while(iter.hasNext()) Out.prln("  " + iter.next());
    }

    // get the beaninfo for the resource bean, excluding data about Object
    BeanInfo resBeanInfo =
      Introspector.getBeanInfo(resource.getClass(), Object.class);
    PropertyDescriptor[] properties = resBeanInfo.getPropertyDescriptors();
    // for each property of the resource bean
    if(properties != null)
      for(int i = 0; i<properties.length; i++) {
        // get the property's set method, or continue
        PropertyDescriptor prop = properties[i];
        Method setMethod = prop.getWriteMethod();

        if(setMethod == null)
          continue;

        // get the parameter value for this property, or continue
        Object paramValue = parameterValues.get(prop.getName());
        if(paramValue == null)  {
          continue;
        }

        //convert the parameter to the right type eg String -> URL
        Class propertyType = prop.getPropertyType();
        Class paramType = paramValue.getClass();
        try{
          if(!propertyType.isAssignableFrom(paramType)){
            if(DEBUG) Out.pr("Converting " + paramValue.getClass());
            paramValue = propertyType.getConstructor(new Class[]{paramType}).
                         newInstance(new Object[]{paramValue});
            if(DEBUG) Out.prln(" to " + paramValue.getClass());
          }
        }catch(NoSuchMethodException nsme){
          if(DEBUG) Out.prln("...Error while converting: " + nsme.toString());
          continue;
        }catch(InstantiationException ie){
          if(DEBUG) Out.prln("...Error while converting: " + ie.toString());
          continue;
        }
        // call the set method with the parameter value
        Object[] args = new Object[1];
        args[0] = paramValue;
        if(DEBUG) {
          Out.pr("setting res param, property = ");
          TestCreole.printProperty(prop);
          Out.prln("to paramValue = " + paramValue);
        }

        setMethod.invoke(resource, args);
        numParametersSet++;
      } // for each property

    // get all the events the bean can fire
    // a list of pairs: [removeListenerMethod, listener]
    List removeListenersData = null;
    EventSetDescriptor[] events = resBeanInfo.getEventSetDescriptors();

    // add the listeners for the initialisation phase
    if(events != null) {
      EventSetDescriptor event;
      removeListenersData = new ArrayList();

      for(int i = 0; i < events.length; i++) {
        event = events[i];

        // did we get such a listener?
        Object listener =
          parameterValues.get(event.getListenerType().getName());
        if(listener != null){
          Method addListener = event.getAddListenerMethod();

          // call the set method with the parameter value
          Object[] args = new Object[1];
          args[0] = listener;
          addListener.invoke(resource, args);
          numParametersSet++;
          removeListenersData.add(
            new Object[] { event.getRemoveListenerMethod(), listener }
          );
        }
      } // for each event
    }   // if events != null

    // did we set all the parameters?
    // Where the number of parameters that
    // are successfully set on the resource != the number of parameter
    // values, throw an exception
    if(numParametersSet != parameterValues.size())
      throw new GateException(
        "couldn't set all the parameters of resource " +
        resource.getClass().getName()
      );
    return removeListenersData;
  } // setResourceParameters

  /** Create a new transient Corpus. */
  public static Corpus newCorpus(String name)
  throws ResourceInstantiationException
  {
    FeatureMap parameterValues = newFeatureMap();
    parameterValues.put("name", name);
    parameterValues.put("features", Factory.newFeatureMap());
    return (Corpus) createResource("gate.corpora.CorpusImpl", parameterValues);
  } // newCorpus

  /** Create a new transient Document from a URL. */
  public static Document newDocument(URL sourceUrl)
  throws ResourceInstantiationException
  {
    FeatureMap parameterValues = newFeatureMap();
    parameterValues.put("sourceUrl", sourceUrl);
    return
      (Document) createResource("gate.corpora.DocumentImpl", parameterValues);
  } // newDocument(URL)

  /** Create a new transient Document from a URL and an encoding. */
  public static Document newDocument(URL sourceUrl, String encoding)
  throws ResourceInstantiationException
  {
    FeatureMap parameterValues = newFeatureMap();
    parameterValues.put("sourceUrl", sourceUrl);
    parameterValues.put("encoding", encoding);
    return
      (Document) createResource("gate.corpora.DocumentImpl", parameterValues);
  } // newDocument(URL)

  /** Create a new transient textual Document from a string. */
  public static Document newDocument(String content)
  throws ResourceInstantiationException
  {
    FeatureMap params = newFeatureMap();
    params.put("stringContent", content);
    Document doc =
      (Document) createResource("gate.corpora.DocumentImpl", params);
/*
    // laziness: should fit this into createResource by adding a new
    // document parameter, but haven't time right now...
    doc.setContent(new DocumentContentImpl(content));
*/
    // various classes are in the habit of assuming that a document
    // inevitably has a source URL...  so give it a dummy one
    try {
      doc.setSourceUrl(new URL("http://localhost/"));
    } catch(MalformedURLException e) {
      throw new ResourceInstantiationException(
        "Couldn't create dummy URL in newDocument(String): " + e
      );
    }

    return doc;
  } // newDocument(String)

  /** Create a new FeatureMap. */
  public static FeatureMap newFeatureMap() {
    return new SimpleFeatureMapImpl();
  } // newFeatureMap

  /** Open an existing DataStore. */
  public static DataStore openDataStore(
    String dataStoreClassName, URL storageUrl
  ) throws PersistenceException {
    DataStore ds = instantiateDataStore(dataStoreClassName, storageUrl);
    ds.open();
    dsReg.add(ds);
    return ds;
  } // openDataStore()

  /** Create a new DataStore and open it. <B>NOTE:</B> for some data stores
    * creation is an system administrator task; in such cases this
    * method will throw an UnsupportedOperationException.
    */
  public static DataStore createDataStore(
    String dataStoreClassName, URL storageUrl
  ) throws PersistenceException, UnsupportedOperationException {
    DataStore ds = instantiateDataStore(dataStoreClassName, storageUrl);
    ds.create();
    ds.open();
    dsReg.add(ds);
    return ds;
  } // createDataStore()

  /** Instantiate a DataStore (not open or created). */
  protected static DataStore instantiateDataStore(
    String dataStoreClassName, URL storageUrl
  ) throws PersistenceException {
    DataStore godfreyTheDataStore = null;
    try {
      godfreyTheDataStore =
        (DataStore) Class.forName(dataStoreClassName).newInstance();
    } catch(Exception e) {
      throw new PersistenceException("Couldn't create DS class: " + e);
    }

    if(dsReg == null) // static init ran before Gate.init....
      dsReg = Gate.getDataStoreRegister();
    godfreyTheDataStore.setStorageUrl(storageUrl);

    return godfreyTheDataStore;
  } // instantiateDS(URL)
} // abstract Factory
