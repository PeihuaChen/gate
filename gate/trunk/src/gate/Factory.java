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
public abstract class Factory {
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** The CREOLE register */
  private static CreoleRegister reg = Gate.getCreoleRegister();

  /** The DataStore register */
  private static DataStoreRegister dsReg = Gate.getDataStoreRegister();

  /** An object to source events from. */
  private static CreoleProxy creoleProxy;

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
    * @param parameterValues the feature map containing intialisation time
    *   parameterValues for the resource.
    * @param listeners The listeners to be registered with the resource during
    * its initialisation. A {@link java.util.Map} that maps from fully
    * qualified class name (as a string) to listener (of the type declared by
    * the key).
    * @return an instantiated resource.
    */
  public static Resource createResource(
    String resourceClassName, FeatureMap parameterValues,
    Map listeners
  ) throws ResourceInstantiationException
  {
    return createResource(resourceClassName, parameterValues, null,
                          listeners, null);
  } // createResource(resClassName, paramVals, listeners)

  /** Create an instance of a resource, and return it.
    * Callers of this method are responsible for
    * querying the resource's parameter lists, putting together a set that
    * is complete apart from runtime parameters, and passing a feature map
    * containing these parameter settings.
    *
    * @param resourceClassName the name of the class implementing the resource.
    * @param parameterValues the feature map containing intialisation time
    *   parameterValues for the resource.
    * @param features the features for the new resource
    * @return an instantiated resource.
    */
  public static Resource createResource(
    String resourceClassName, FeatureMap parameterValues,
    FeatureMap features
    ) throws ResourceInstantiationException
   {
      return createResource(resourceClassName, parameterValues,
                            features, null, null);
   }

  /** Create an instance of a resource, and return it.
    * Callers of this method are responsible for
    * querying the resource's parameter lists, putting together a set that
    * is complete apart from runtime parameters, and passing a feature map
    * containing these parameter settings.
    *
    * In the case of ProcessingResources they will have their runtime parameters
    * initialised to their default values.
    *
    * @param resourceClassName the name of the class implementing the resource.
    * @param parameterValues the feature map containing intialisation time
    *   parameterValues for the resource.
    * @param listeners The listeners to be registered with the resource during
    * its initialisation. A {@link java.util.Map} that maps freom fully
    * qualified class name (as a string) to listener (of the type declared by
    * the key).
    * @param features the features for the new resource
    * @return an instantiated resource.
    */
  public static Resource createResource(
    String resourceClassName, FeatureMap parameterValues,
    FeatureMap features, Map listeners, String resourceName
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

    //set the name
    res.setName(resourceName);
    // type-specific stuff for LRs
    if(LanguageResource.class.isAssignableFrom(resClass)) {
      if(DEBUG) Out.prln(resClass.getName() + " is an LR");

      DataStore dataStore = (DataStore)
                          parameterValues.get(DataStore.DATASTORE_FEATURE_NAME);
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
        Object instanceId = parameterValues.get(DataStore.LR_ID_FEATURE_NAME);
        if(instanceId == null)
          throw new
            ResourceInstantiationException("No instance id for " + resClass);
        try {
          res = dataStore.getLr(resClass.getName(), instanceId);
        } catch(PersistenceException e) {
          throw new ResourceInstantiationException("Bad read from DB: " + e);
        }
        resData.addInstantiation(res);
        if(features != null) res.getFeatures().putAll(features);

        // fire the event
        creoleProxy.fireResourceLoaded(
          new CreoleEvent(res, CreoleEvent.RESOURCE_LOADED)
        );

        return res;
      } // datastore was present

    // type-specific stuff for PRs
    } else if(ProcessingResource.class.isAssignableFrom(resClass)) {
      if(DEBUG) Out.prln(resClass.getName() + " is a PR");
      //set the runtime parameters to their defaults
      try{
        FeatureMap parameters = newFeatureMap();
        parameters.putAll(resData.getParameterList().getRuntimeDefaults());
        res.setParameterValues(parameters);
      }catch(ParameterException pe){
        throw new ResourceInstantiationException(
                  "Could not set the runtime parameters " +
                  "to their default values for: " + res.getClass().getName() +
                  " :\n" + pe.toString()
                  );
      }
    // type-specific stuff for VRs
    } else if(VisualResource.class.isAssignableFrom(resClass)) {
      if(DEBUG) Out.prln(resClass.getName() + " is a VR");

    // we have a resource which is not an LR, PR or VR
    } else {
      Err.prln("WARNING: instantiating resource which is not a PR, LR or VR:");
      Err.prln(resData + "END OF WARNING" + Strings.getNl());
    }



    //set the parameterValues of the resource
    try{
      FeatureMap parameters = newFeatureMap();
      //put the defaults
      parameters.putAll(resData.getParameterList().getInitimeDefaults());
      //overwrite the defaults with the user provided values
      parameters.putAll(parameterValues);
      res.setParameterValues(parameters);
    }catch(ParameterException pe){
        throw new ResourceInstantiationException(
                    "Could not set the init parameters for: " +
                    res.getClass().getName() + " :\n" + pe.toString()
                  );
    }



    // set the listeners if any
    if(listeners != null && !listeners.isEmpty()) {
      try {
        if(DEBUG) Out.prln("Setting the listeners for  " + res.toString());
        AbstractResource.setResourceListeners(res, listeners);
      } catch(Exception e) {
        if(DEBUG) Out.prln("Failed to set listeners for " + res.toString());
        throw new
          ResourceInstantiationException("Parameterisation failure" + e);
      }
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

    // remove the listeners if any
    if(listeners != null && !listeners.isEmpty()) {
      try {
        if(DEBUG) Out.prln("Removing the listeners for  " + res.toString());
        AbstractResource.removeResourceListeners(res, listeners);
      } catch(Exception e) {
        if (DEBUG) Out.prln(
          "Failed to remove the listeners for " + res.toString()
        );
        throw new
          ResourceInstantiationException("Parameterisation failure" + e);
      }
    }

    // record the instantiation on the resource data's stack
    resData.addInstantiation(res);

    // add the features specified by the user
    if(features != null) res.getFeatures().putAll(features);

    // fire the event
    creoleProxy.fireResourceLoaded(
      new CreoleEvent(res, CreoleEvent.RESOURCE_LOADED)
    );

    return res;
  } // create(resourceClassName, parameterValues, features, listeners)

  /** Create an instance of a resource, and return it.
    * Callers of this method are responsible for
    * querying the resource's parameter lists, putting together a set that
    * is complete apart from runtime parameters, and passing a feature map
    * containing these parameter settings.
    *
    * @param resourceClassName the name of the class implementing the resource.
    * @param parameterValues the feature map containing intialisation time
    *   parameterValues for the resource.
    * @return an instantiated resource.
    */
  public static Resource createResource(
    String resourceClassName, FeatureMap parameterValues
  ) throws ResourceInstantiationException
  {
    return createResource(resourceClassName, parameterValues, null);
  } // createResource(resClassName, paramVals)

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
//    resource.clear();
    creoleProxy.fireResourceUnloaded(
      new CreoleEvent(resource, CreoleEvent.RESOURCE_UNLOADED)
    );
  } // deleteResource


  /** Create a new transient Corpus. */
  public static Corpus newCorpus(String name)
                                          throws ResourceInstantiationException
  {
    FeatureMap parameterValues = newFeatureMap();
    parameterValues.put("name", name);
//    parameterValues.put("features", Factory.newFeatureMap());
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
/*    try {
      doc.setSourceUrl(new URL("http://localhost/"));
    } catch(MalformedURLException e) {
      throw new ResourceInstantiationException(
        "Couldn't create dummy URL in newDocument(String): " + e
      );
    }
*/
    doc.setSourceUrl(null);
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
    if(dsReg.add(ds))
      creoleProxy.fireDatastoreOpened(
        new CreoleEvent(ds, CreoleEvent.DATASTORE_OPENED)
      );

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
    if(dsReg.add(ds))
      creoleProxy.fireDatastoreCreated(
        new CreoleEvent(ds, CreoleEvent.DATASTORE_CREATED)
      );

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
  } // instantiateDS(dataStoreClassName, storageURL)

  /** Add a listener */
  public static synchronized void addCreoleListener(CreoleListener l){
    creoleProxy.addCreoleListener(l);
  } // addCreoleListener(CreoleListener)

  /** Static initialiser to set up the CreoleProxy event source object */
  static {
    creoleProxy = new CreoleProxy();
  } // static initialiser

} // abstract Factory

/**
 * Factory is basically a collection of static methods but events need to
 * have as source an object and not a class. The CreolProxy class addresses
 * this issue acting as source for all events fired by the Factory class.
 */
class CreoleProxy {

  public synchronized void removeCreoleListener(CreoleListener l) {
    if (creoleListeners != null && creoleListeners.contains(l)) {
      Vector v = (Vector) creoleListeners.clone();
      v.removeElement(l);
      creoleListeners = v;
    }// if
  }// removeCreoleListener(CreoleListener l)

  public synchronized void addCreoleListener(CreoleListener l) {
    Vector v =
      creoleListeners == null ? new Vector(2) : (Vector) creoleListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      creoleListeners = v;
    }// if
  }// addCreoleListener(CreoleListener l)

  protected void fireResourceLoaded(CreoleEvent e) {
    if (creoleListeners != null) {
      Vector listeners = creoleListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((CreoleListener) listeners.elementAt(i)).resourceLoaded(e);
      }// for
    }// if
  }// fireResourceLoaded(CreoleEvent e)

  protected void fireResourceUnloaded(CreoleEvent e) {
    if (creoleListeners != null) {
      Vector listeners = creoleListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((CreoleListener) listeners.elementAt(i)).resourceUnloaded(e);
      }// for
    }// if
  }// fireResourceUnloaded(CreoleEvent e)

  protected void fireDatastoreOpened(CreoleEvent e) {
    if (creoleListeners != null) {
      Vector listeners = creoleListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((CreoleListener) listeners.elementAt(i)).datastoreOpened(e);
      }// for
    }// if
  }// fireDatastoreOpened(CreoleEvent e)

  protected void fireDatastoreCreated(CreoleEvent e) {
    if (creoleListeners != null) {
      Vector listeners = creoleListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((CreoleListener) listeners.elementAt(i)).datastoreCreated(e);
      }// for
    }// if
  }// fireDatastoreCreated(CreoleEvent e)

  protected void fireDatastoreClosed(CreoleEvent e) {
    if (creoleListeners != null) {
      Vector listeners = creoleListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((CreoleListener) listeners.elementAt(i)).datastoreClosed(e);
      }// for
    }// if
  }// fireDatastoreClosed(CreoleEvent e)

  private transient Vector creoleListeners;
}//class CreoleProxy
