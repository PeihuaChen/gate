/*
 *	Factory.java
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
 *	Hamish Cunningham, 25/May/2000
 *
 *	$Id$
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

/** Provides static methods for the creation of Resources.
  */
public abstract class Factory
{
  /** Debug flag */
  private static final boolean DEBUG = false;

  /** The CREOLE register */
  private static CreoleRegister reg = Gate.getCreoleRegister();

  /** Create an instance of a resource, and return it.
    * Callers of this method are responsible for
    * querying the resource's parameter lists, putting together a set that
    * is complete apart from runtime parameters, and passing a feature map
    * containing these parameter settings.
    *
    * @param resourceClassName the name of the class implementing the resource.
    * @param parameters the feature map containing parameters for the resource.
    * @return an instantiated resource.
    */
  public static Resource createResource(
    String resourceClassName, FeatureMap parameters
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
        "Couldn't get resource class from the resource data: " + e
      );
    }

    // type-specific stuff for LRs
    if(LanguageResource.class.isAssignableFrom(resClass)) {
      if(DEBUG) Out.prln(resClass.getName() + " is an LR");

//if the DS param is set, find an appropriate data store wrapper and:
/* resClass = dataStoreWrapperClass
   if none available then
   throw new ResourceInstantiationException(
     "Unknown wrapper class " + dataStoreWrapperClass
   );      OR maybe UnknownDataStoreException
*/

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

    // set the parameters of the resource
    try {
      if(DEBUG) Out.prln("Setting the parameters for  " + res.toString());
      setResourceParameters(res, parameters);
    } catch(Exception e) {
      if(DEBUG) Out.prln("Failed to set the parameters for " + res.toString());
      throw new ResourceInstantiationException("Parameterisation failure" + e);
    }

    // if the features of the resource have not been explicitly set,
    // set them to the features of the resource data
    if(res.getFeatures() == null || res.getFeatures().isEmpty())
      res.setFeatures(resData.getFeatures());

    // initialise the resource
    if(DEBUG) Out.prln("Initialising resource " + res.toString());
    res = res.init();

    return res;
  } // create(resourceClassName)

  /** For each paramter, set the appropriate property on the resource
    * using bean-style reflection.
    *
    * @see java.beans.Introspector
    * @param resource the resource to be parameterised.
    * @param parameters the parameters and their values.
    */
  protected static void setResourceParameters(
    Resource resource, FeatureMap parameters
  ) throws
    IntrospectionException, InvocationTargetException,
    IllegalAccessException, GateException
  {
    // the number of parameters that we manage to set on the bean
    int numParametersSet = 0;
    if(DEBUG) {
      Out.prln("setResourceParameters, params = ");
      Iterator iter = parameters.entrySet().iterator();
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
        if(setMethod == null) continue;

        // get the parameter value for this property, or continue
        Object paramValue = parameters.get(prop.getName());
        if(paramValue == null) continue;

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

      // did we set all the parameters?
      if(numParametersSet != parameters.size())
        throw new GateException(
          "couldn't set all the parameters of resource " + resource
        );

  } // setResourceParameters

  /** Create a new transient Corpus. */
  public static Corpus newCorpus(String name)
  throws ResourceInstantiationException
  {
    FeatureMap parameters = newFeatureMap();
    parameters.put("name", name);
    parameters.put("features", Factory.newFeatureMap());
    return (Corpus) createResource("gate.Corpus", parameters);
  } // newCorpus

  /** Create a new transient Document from a URL. */
  public static Document newDocument(URL sourceUrl)
  throws ResourceInstantiationException
  {
    FeatureMap parameters = newFeatureMap();
    parameters.put("sourceUrlName", sourceUrl.toExternalForm());
    return (Document) createResource("gate.Document", parameters);
  } // newDocument(URL)

  /** Create a new transient Document from a URL and an encoding. */
  public static Document newDocument(URL sourceUrl, String encoding)
  throws ResourceInstantiationException
  {
    FeatureMap parameters = newFeatureMap();
    parameters.put("sourceUrlName", sourceUrl.toExternalForm());
    parameters.put("encoding", encoding);
    return (Document) createResource("gate.Document", parameters);
  } // newDocument(URL)

  /** Create a new transient textual Document from a string. */
  public static Document newDocument(String content)
  throws ResourceInstantiationException
  {
    Document doc = (Document) createResource("gate.Document", newFeatureMap());

    // laziness: should fit this into createResource by adding a new
    // document parameter, but haven't time right now...
    doc.setContent(new DocumentContentImpl(content));

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
} // abstract Factory
