/*
 *  CreoleRegisterImpl.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 1/Sept/2000
 *
 *  $Id$
 */

package gate.creole;

import java.util.*;
import java.net.*;
import java.io.*;

import org.xml.sax.*;
import javax.xml.parsers.*;
import org.xml.sax.helpers.*;

import gate.*;
import gate.util.*;
import gate.event.*;


/** This class implements the CREOLE register interface. DO NOT
  * construct objects of this class unless your name is gate.Gate
  * (in which case please go back to the source code repository and stop
  * looking at other class's code).
  * <P>
  * The CREOLE register records the set of resources that are currently
  * known to the system. Each member of the register is a
  * {@link gate.creole.ResourceData} object, indexed by
  * the class name of the resource.
  * @see gate.CreoleRegister
  */
public class CreoleRegisterImpl extends HashMap
          implements CreoleRegister, CreoleListener
{
  /** Debug flag */
  protected static final boolean DEBUG = false;

  /** The set of CREOLE directories (URLs). */
  protected Set directories;

  /** The parser for the CREOLE directory files */
  protected transient SAXParser parser = null;

  /**
   * Default constructor. Sets up directory files parser. <B>NOTE:</B>
   * only Factory should call this method.
   */
  public CreoleRegisterImpl() throws GateException {

    // initialise the various maps
    directories = new HashSet();
    lrTypes = new HashSet();
    prTypes = new HashSet();
    vrTypes = new LinkedList();
    toolTypes = new HashSet();

    // construct a SAX parser for parsing the CREOLE directory files
    try {
      // Get a parser factory.
      SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

      // Set up the factory to create the appropriate type of parser:
      // non validating one
      saxParserFactory.setValidating(false);
      // non namespace aware one
      saxParserFactory.setNamespaceAware(true);

      // create the parser
      parser = saxParserFactory.newSAXParser();

    } catch (SAXException e) {
      if(DEBUG) Out.println(e);
      throw(new GateException(e));
    } catch (ParserConfigurationException e) {
      if(DEBUG) Out.println(e);
      throw(new GateException(e));
    }

  } // default constructor

  /** Add a CREOLE directory URL to the register and to the GATE classloader.
    * The directory is <B>not</B> registered.
    */
  public void addDirectory(URL directoryUrl) {
    directories.add(directoryUrl);
  } // addDirectory

  /** Get the list of CREOLE directory URLs. */
  public Set getDirectories() {
    return Collections.unmodifiableSet(directories);
  } // getDirectories

  /** Register all the CREOLE directories that we know of.
    * The <CODE>creole.xml</CODE> files
    * at the URLs are parsed, and <CODE>ResourceData</CODE> objects added
    * to the register.
    * URLs for resource JAR files are added to the GATE class loader.
    */
  public void registerDirectories() throws GateException {
    Iterator iter = directories.iterator();

    while(iter.hasNext()) {
      URL directoryUrl = (URL) iter.next();
      registerDirectories(directoryUrl);
    }
  } // registerDirectories

  /** Register a single CREOLE directory. The <CODE>creole.xml</CODE>
    * file at the URL is parsed, and <CODE>CreoleData</CODE> objects added
    * to the register. If the directory URL has not yet been added it
    * is now added.
    * URLs for resource JAR files are added to the GATE class loader.
    */
  public void registerDirectories(URL directoryUrl) throws GateException {

    // directory URLs shouldn't include "creole.xml"
    String urlName = directoryUrl.toExternalForm();
    if(urlName.toLowerCase().endsWith("creole.xml")) {
      throw new GateException(
        "CREOLE directory URLs should point to the parent location of " +
        "the creole.xml file, not the file itself; bad URL was: " + urlName
      );
    }

    // create a URL for the creole.xml file, based on the directory URL
    URL directoryXmlFileUrl = directoryUrl;
    String separator = "/";
    if(urlName.endsWith(separator)) separator = "";
    try {
      directoryXmlFileUrl = new URL(urlName + separator + "creole.xml");
    } catch(MalformedURLException e) {
      throw(new GateException("bad creole.xml URL, based on " + urlName));
    }

    // add the URL
    //if already present do nothing
    if(directories.add(directoryUrl)){
      // parse the directory file
      try {
        parseDirectory(directoryXmlFileUrl.openStream(), directoryUrl);
      } catch(IOException e) {
        throw(new GateException("couldn't open creole.xml: " + e.toString()));
      }
    }
  } // registerDirectories(URL)

  /** Parse a directory file (represented as an open stream), adding
    * resource data objects to the CREOLE register as they occur.
    * If the resource is from a URL then that location is passed (otherwise
    * null).
    */
  protected void parseDirectory(InputStream directoryStream, URL directoryUrl)
  throws GateException
  {
    // create a handler for the directory file and parse it;
    // this will create ResourceData entries in the register
    try {
      DefaultHandler handler = new CreoleXmlHandler(this, directoryUrl);
      parser.parse(directoryStream, handler);
      if(DEBUG) {
        Out.prln(
          "done parsing " +
          ((directoryUrl == null) ? "null" : directoryUrl.toString())
        );
      }
    } catch (IOException e) {
      throw(new GateException(e));
    } catch (SAXException e) {
      if(DEBUG) Out.println(e.toString());
      throw(new GateException(e));
    }

  } // parseDirectory

  /** Register resources that are built in to the GATE distribution.
    * These resources are described by the <TT>creole.xml</TT> file in
    * <TT>resources/creole</TT>.
    */
  public void registerBuiltins() throws GateException {
    try {
      parseDirectory(
        new URL("gate:/creole/creole.xml").openStream(),
        new URL("gate:/creole/")
      );
    } catch(IOException e) {
      if (DEBUG) Out.println(e);
      throw(new GateException(e));
    }
  } // registerBuiltins()

  /** This is a utility method for creating CREOLE directory files
    * (typically called <CODE>creole.xml</CODE>) from a list of Jar
    * files that contain resources. The method concatenates the
    * <CODE>resource.xml</CODE> files that the Jars contain.
    * <P>
    * If Java allowed class methods in interfaces this would be static.
    */
  public File createCreoleDirectoryFile(File directoryFile, Set jarFileNames)
  {
    ////////////////////
    // dump xml header and comment header and <CREOLE-DIRECTORY> into dirfile
    // for each jar file pick out resource.xml
    // strip xml header
    // dump into dirfile
    // put </CREOLE-DIRECTORY> into dirfile
    throw new LazyProgrammerException();
  } // createCreoleDirectoryFile

  /** Overide HashMap's put method to maintain a list of all the
    * types of LR in the register, and a list of tool types. The key is
    * the resource type, the value its data.
    */
  public Object put(Object key, Object value) {
    ResourceData rd = (ResourceData) value;

    // get the resource implementation class
    Class resClass = null;
    try {
      resClass = rd.getResourceClass();
    } catch(ClassNotFoundException e) {
      throw new GateRuntimeException(
        "Couldn't get resource class from the resource data:" + e
      );
    }

    // add class names to the type lists
    if(LanguageResource.class.isAssignableFrom(resClass)) {
      if(DEBUG) Out.prln("LR: " + resClass);
      if(lrTypes == null) lrTypes = new HashSet(); // for deserialisation
      lrTypes.add(rd.getClassName());
    } else if(ProcessingResource.class.isAssignableFrom(resClass)) {
      if(DEBUG) {
        Out.prln("PR: " + resClass);
        //Out.prln("prTypes: " + prTypes);
        //Out.prln("rd.getClassName(): " + rd.getClassName());
      }
      if(prTypes == null) prTypes = new HashSet(); // for deserialisation
      prTypes.add(rd.getClassName());
    } else if(VisualResource.class.isAssignableFrom(resClass)) {
      if(DEBUG) Out.prln("VR: " + resClass);
      if(vrTypes == null) vrTypes = new LinkedList(); // for deserialisation
      vrTypes.add(rd.getClassName());
    }else if(Controller.class.isAssignableFrom(resClass)) {
      if(DEBUG) Out.prln("Controller: " + resClass);
      if(controllerTypes == null) controllerTypes = new HashSet(); // for deserialisation
      controllerTypes.add(rd.getClassName());
    }

    // maintain tool types list
    if(rd.isTool()) {
      if(toolTypes == null) toolTypes = new HashSet(); // for deserialisation
      toolTypes.add(rd.getClassName());
    }

    return super.put(key, value);
  } // put(key, value)

  /** Overide HashMap's delete method to update the lists of types
    * in the register.
    */
  public Object remove(Object key) {
    ResourceData rd = (ResourceData) get(key);
    if(rd == null) return null;

    if(DEBUG) {
      Out.prln(key);
      Out.prln(rd);
    }
    if(LanguageResource.class.isAssignableFrom(rd.getClass()))
      lrTypes.remove(rd.getClassName());
    else if(ProcessingResource.class.isAssignableFrom(rd.getClass()))
      prTypes.remove(rd.getClassName());
    else if(VisualResource.class.isAssignableFrom(rd.getClass()))
      vrTypes.remove(rd.getClassName());

    // maintain tool types list
    if(rd.isTool())
      toolTypes.remove(rd.getClassName());

    return super.remove(key);
  } // remove(Object)

  /** Overide HashMap's clear to update the list of LR types in the register,
    * and remove all resources and forgets all directories.
    */
  public void clear() {
    lrTypes.clear();
    prTypes.clear();
    vrTypes.clear();
    toolTypes.clear();
    directories.clear();
    super.clear();
  } // clear()

  /** Get the list of types of LR in the register. */
  public Set getLrTypes() { return Collections.unmodifiableSet(lrTypes);}

  /** Get the list of types of PR in the register. */
  public Set getPrTypes() { return Collections.unmodifiableSet(prTypes);}

  /** Get the list of types of VR in the register. */
  public Set getVrTypes() { return Collections.unmodifiableSet(new HashSet(vrTypes));}

  /** Get the list of types of VR in the register. */
  public Set getControllerTypes() {
    return Collections.unmodifiableSet(controllerTypes);
  }

  /** Get the list of types of TOOL respurces in the register. */
  public Set getToolTypes() { return Collections.unmodifiableSet(toolTypes);}

  /** Get a list of all instantiations of LR in the register. */
  public List getLrInstances() {
    Set lrTypeSet = getLrTypes();
    List instances = new ArrayList();

    Iterator iter = lrTypeSet.iterator();
    while(iter.hasNext()) {
      String type = (String) iter.next();
      instances.addAll(getLrInstances(type));
    }// End while
    return Collections.unmodifiableList(instances);
  } // getLrInstances()

  /** Get a list of all instantiations of PR in the register. */
  public List getPrInstances() {
    Set prTypeSet = getPrTypes();
    List instances = new ArrayList();

    Iterator iter = prTypeSet.iterator();
    while(iter.hasNext()) {
      String type = (String) iter.next();
      instances.addAll(getPrInstances(type));
    }// End while

    return Collections.unmodifiableList(instances);
  } // getPrInstances()

  /** Get a list of all instantiations of VR in the register. */
  public List getVrInstances() {
    Set vrTypeSet = getVrTypes();
    List instances = new ArrayList();

    Iterator iter = vrTypeSet.iterator();
    while(iter.hasNext()) {
      String type = (String) iter.next();
      instances.addAll(getVrInstances(type));
    }// End while

    return Collections.unmodifiableList(instances);
  } // getVrInstances()

  /** Get a list of instantiations of a type of LR in the register. */
  public List getLrInstances(String resourceTypeName) {
    ResourceData resData = (ResourceData) get(resourceTypeName);
    if(resData == null)
      return Collections.unmodifiableList(new ArrayList());

    return Collections.unmodifiableList(resData.getInstantiations());
  } // getLrInstances

  /** Get a list of instantiations of a type of PR in the register. */
  public List getPrInstances(String resourceTypeName) {
    ResourceData resData = (ResourceData) get(resourceTypeName);
    if(resData == null)
      return Collections.unmodifiableList(new ArrayList());

    return Collections.unmodifiableList(resData.getInstantiations());
  } // getPrInstances

  /** Get a list of instantiations of a type of VR in the register. */
  public List getVrInstances(String resourceTypeName) {
    ResourceData resData = (ResourceData) get(resourceTypeName);
    if(resData == null)
      return Collections.unmodifiableList(new ArrayList());

    return Collections.unmodifiableList(resData.getInstantiations());
  } // getVrInstances

  /** Get a list of all non-private instantiations of LR in the register. */
  public List getPublicLrInstances() {
    return Collections.unmodifiableList(getPublics(getLrInstances()));
  }// getPublicLrInstances()

  /** Get a list of all non-private instantiations of PR in the register. */
  public List getPublicPrInstances() {
    return Collections.unmodifiableList(getPublics(getPrInstances()));
  }// getPublicPrInstances()

  /** Get a list of all non-private instantiations of VR in the register. */
  public List getPublicVrInstances() {
    return Collections.unmodifiableList(getPublics(getVrInstances()));
  }//getPublicVrInstances()

  /** Get a list of all non-private types of LR in the register. */
  public List getPublicLrTypes() {
    return Collections.unmodifiableList(getPublicTypes(getLrTypes()));
  }//getPublicLrTypes()

  /** Get a list of all non-private types of PR in the register. */
  public List getPublicPrTypes() {
    return Collections.unmodifiableList(getPublicTypes(getPrTypes()));
  }//getPublicPrTypes()

  /** Get a list of all non-private types of VR in the register. */
  public List getPublicVrTypes() {
    return Collections.unmodifiableList(getPublicTypes(getVrTypes()));
  }//getPublicVrTypes()

  /** Get a list of all non-private types of controller in the register. */
  public List getPublicControllerTypes() {
    return Collections.unmodifiableList(getPublicTypes(getControllerTypes()));
  }//getPublicPrTypes()


  /**
   * Gets all the instantiations of a given type and all its derivate types;
   * It doesn't return instances that have the hidden attribute set to "true"
   */
  public List getAllInstances(String type) throws GateException{
    Iterator typesIter = keySet().iterator();
    List res = new ArrayList();
    Class targetClass;
    try{
      targetClass = Gate.getClassLoader().loadClass(type);
    }catch(ClassNotFoundException cnfe){
      throw new GateException("Invalid type " + type);
    }
    while(typesIter.hasNext()){
      String aType = (String)typesIter.next();
      Class aClass;
      try{
        aClass = Gate.getClassLoader().loadClass(aType);
        if(targetClass.isAssignableFrom(aClass)){
          //filter out hidden instances
          Iterator newInstancesIter = ((ResourceData)get(aType)).
                                      getInstantiations().iterator();
          while(newInstancesIter.hasNext()){
            Resource instance = (Resource)newInstancesIter.next();
            if(!Gate.getHiddenAttribute(instance.getFeatures())){
              res.add(instance);
            }
          }
        }
      }catch(ClassNotFoundException cnfe){
        throw new LuckyException(
          "A type registered in the creole register does not exist in the VM!");
      }

    }//while(typesIter.hasNext())

    return res;
  }

  /**
   * Returns a list of strings representing class names for large VRs valid
   * for a given type of language/processing resource.
   * The default VR will be the first in the returned list.
   * @param resoureClassName the name of the resource that has large viewers. If
   * resourceClassName is <b>null</b> then an empty list will be returned.
   * @return a list with Strings representing the large VRs for the
   * resourceClassName
   */
  public List getLargeVRsForResource(String resourceClassName){
    return getVRsForResource(resourceClassName, ResourceData.LARGE_GUI);
  }// getLargeVRsForResource()

  /**
   * Returns a list of strings representing class names for small VRs valid
   * for a given type of language/processing resource
   * The default VR will be the first in the returned list.
   * @param resoureClassName the name of the resource that has large viewers. If
   * resourceClassName is <b>null</b> then an empty list will be returned.
   * @return a list with Strings representing the large VRs for the
   * resourceClassName
   */
  public List getSmallVRsForResource(String resourceClassName){
    return getVRsForResource(resourceClassName, ResourceData.SMALL_GUI);
  }// getSmallVRsForResource

  /**
   * Returns a list of strings representing class names for guiType VRs valid
   * for a given type of language/processing resource
   * The default VR will be the first in the returned list.
   * @param resoureClassName the name of the resource that has large viewers. If
   * resourceClassName is <b>null</b> then an empty list will be returned.
   * @param guiType can be ResourceData's LARGE_GUI or SMALL_GUI
   * @return a list with Strings representing the large VRs for the
   * resourceClassName
   */
  private List getVRsForResource(String resourceClassName, int guiType){
    // If resurceClassName is null return a simply list
    if (resourceClassName == null)
      return Collections.unmodifiableList(new ArrayList());
    // create a Class object for the resource
    Class resourceClass = null;
    GateClassLoader classLoader = Gate.getClassLoader();
    try{
      resourceClass = classLoader.loadClass(resourceClassName);
    } catch (ClassNotFoundException ex){
      throw new GateRuntimeException(
        "Couldn't get resource class from the resource name:" + ex
      );
    }// End try
    LinkedList responseList = new LinkedList();
    String defaultVR = null;
    // Take all VRs and for each large one, test if
    // resourceClassName is asignable form VR's RESOURCE_DISPLAYED
    Iterator vrIterator = vrTypes.iterator();
    while (vrIterator.hasNext()){
      String vrClassName = (String) vrIterator.next();
      ResourceData vrResourceData = (ResourceData) this.get(vrClassName);
      if (vrResourceData == null)
        throw new GateRuntimeException(
          "Couldn't get resource data for VR called " + vrClassName
        );
      if (vrResourceData.getGuiType() == guiType){
        String resourceDisplayed = vrResourceData.getResourceDisplayed();
        if (resourceDisplayed != null){
          Class resourceDisplayedClass = null;
          try{
            resourceDisplayedClass = classLoader.loadClass(resourceDisplayed);
          } catch (ClassNotFoundException ex){
              throw new GateRuntimeException(
                "Couldn't get resource class from the resource name :" +
                resourceDisplayed + " " +ex );
          }// End try
          if (resourceDisplayedClass.isAssignableFrom(resourceClass)){
            responseList.add(vrClassName);
            if (vrResourceData.isMainView()){
              defaultVR = vrClassName;
            }// End if
          }// End if
        }// End if
      }// End if
    }// End while
    if (defaultVR != null){
      responseList.remove(defaultVR);
      responseList.addFirst(defaultVR);
    }// End if
    return Collections.unmodifiableList(responseList);
  }// getVRsForResource()

  /**
   * Returns a list of strings representing class names for annotation VRs
   * that are able to display/edit all types of annotations.
   * The default VR will be the first in the returned list.
   * @return a list with all VRs that can display all annotation types
   */
  public List getAnnotationVRs(){
    LinkedList responseList = new LinkedList();
    String defaultVR = null;
    Iterator vrIterator = vrTypes.iterator();
    while (vrIterator.hasNext()){
      String vrClassName = (String) vrIterator.next();
      ResourceData vrResourceData = (ResourceData) this.get(vrClassName);
      if (vrResourceData == null)
        throw new GateRuntimeException(
          "Couldn't get resource data for VR called " + vrClassName
        );
      Class vrResourceClass = null;
      try{
        vrResourceClass = vrResourceData.getResourceClass();
      } catch(ClassNotFoundException ex){
        throw new GateRuntimeException(
          "Couldn't create a class object for VR called " + vrClassName
        );
      }// End try
      // Test if VR can display all types of annotations
      if ( vrResourceData.getGuiType() == ResourceData.NULL_GUI &&
           vrResourceData.getAnnotationTypeDisplayed() == null &&
           vrResourceData.getResourceDisplayed() == null &&
           gate.creole.AnnotationVisualResource.class.
                                          isAssignableFrom(vrResourceClass)){

          responseList.add(vrClassName);
          if (vrResourceData.isMainView())
              defaultVR = vrClassName;
      }// End if
    }// End while
    if (defaultVR != null){
      responseList.remove(defaultVR);
      responseList.addFirst(defaultVR);
    }// End if
    return Collections.unmodifiableList(responseList);
  }// getAnnotationVRs()

  /**
   * Returns a list of strings representing class names for annotation VRs
   * that are able to display/edit a given annotation type
   * The default VR will be the first in the returned list.
   */
  public List getAnnotationVRs(String annotationType){
    if (annotationType == null)
      return Collections.unmodifiableList(new ArrayList());
    LinkedList responseList = new LinkedList();
    String defaultVR = null;
    Iterator vrIterator = vrTypes.iterator();
    while (vrIterator.hasNext()){
      String vrClassName = (String) vrIterator.next();
      ResourceData vrResourceData = (ResourceData) this.get(vrClassName);
      if (vrResourceData == null)
        throw new GateRuntimeException(
          "Couldn't get resource data for VR called " + vrClassName
        );
      Class vrResourceClass = null;
      try{
        vrResourceClass = vrResourceData.getResourceClass();
      } catch(ClassNotFoundException ex){
        throw new GateRuntimeException(
          "Couldn't create a class object for VR called " + vrClassName
        );
      }// End try
      // Test if VR can display all types of annotations
      if ( vrResourceData.getGuiType() == ResourceData.NULL_GUI &&
           vrResourceData.getAnnotationTypeDisplayed() != null &&
           gate.creole.AnnotationVisualResource.class.
                                          isAssignableFrom(vrResourceClass)){

          String annotationTypeDisplayed =
                                  vrResourceData.getAnnotationTypeDisplayed();
          if (annotationTypeDisplayed.equals(annotationType)){
            responseList.add(vrClassName);
            if (vrResourceData.isMainView())
              defaultVR = vrClassName;
          }// End if
      }// End if
    }// End while
    if (defaultVR != null){
      responseList.remove(defaultVR);
      responseList.addFirst(defaultVR);
    }// End if
    return Collections.unmodifiableList(responseList);
  }//getAnnotationVRs()

   /**
    * Renames an existing resource.
    */
   public void setResourceName(Resource res, String newName){
    String oldName = res.getName();
    res.setName(newName);
    fireResourceRenamed(res, oldName, newName);
   }


  /**
   * Returns a list of strings representing annotations types for which
   * there are custom viewers/editor registered.
   */
  public List getVREnabledAnnotationTypes(){
    LinkedList responseList = new LinkedList();
    Iterator vrIterator = vrTypes.iterator();
    while (vrIterator.hasNext()){
      String vrClassName = (String) vrIterator.next();
      ResourceData vrResourceData = (ResourceData) this.get(vrClassName);
      if (vrResourceData == null)
        throw new GateRuntimeException(
          "Couldn't get resource data for VR called " + vrClassName
        );
      // Test if VR can display all types of annotations
      if ( vrResourceData.getGuiType() == ResourceData.NULL_GUI &&
           vrResourceData.getAnnotationTypeDisplayed() != null ){

          String annotationTypeDisplayed =
                                  vrResourceData.getAnnotationTypeDisplayed();
          responseList.add(annotationTypeDisplayed);
      }// End if
    }// End while
    return Collections.unmodifiableList(responseList);
  }// getVREnabledAnnotationTypes()



  /** Get a list of all non-private instantiations. */
  protected List getPublics(List instances) {
    Iterator iter = instances.iterator();
    List publics = new ArrayList();

    // for each instance, if resource data specifies it isn't private,
    // add to the publics list
    while(iter.hasNext()) {
      Resource res = (Resource) iter.next();
      ResourceData rd = (ResourceData) get(res.getClass().getName());
      if(! rd.isPrivate()) publics.add(res);
    }

    return Collections.unmodifiableList(publics);
  } // getPublics

  /** Gets a list of all non private types from alist of types*/
  protected List getPublicTypes(Collection types){
    Iterator iter = types.iterator();
    List publics = new ArrayList();
    while(iter.hasNext()){
      String oneType = (String)iter.next();
      ResourceData rData = (ResourceData)get(oneType);
      if(rData != null && !rData.isPrivate()) publics.add(oneType);
    }
    return Collections.unmodifiableList(publics);
  }//getPublicTypes

  public synchronized void removeCreoleListener(CreoleListener l) {
    if (creoleListeners != null && creoleListeners.contains(l)) {
      Vector v = (Vector) creoleListeners.clone();
      v.removeElement(l);
      creoleListeners = v;
    }
  }

  public synchronized void addCreoleListener(CreoleListener l) {
    Vector v = creoleListeners == null ? new Vector(2) : (Vector) creoleListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      creoleListeners = v;
    }
  } // getPublicTypes

  /**
   * Removes a {@link gate.event.CreoleListener} previously registered with this
   * CreoleRegister. {@see #addCreoleListener()}
   */

  /**
   * Registers a {@link gate.event.CreoleListener}with this CreoleRegister.
   * The register will fire events every time a resource is added to or removed
   * from the system.
   */// addCreoleListener

  /**
   * Notifies all listeners that a new {@link gate.Resource} has been loaded
   * into the system
   */// fireResourceLoaded

  /**
   * Notifies all listeners that a {@link gate.Resource} has been unloaded
   * from the system
   */// fireResourceUnloaded

  /** A list of the types of LR in the register. */
  protected Set lrTypes;

  /** A list of the types of PR in the register. */
  protected Set prTypes;

  /** A list of the types of VR in the register. */
  protected List vrTypes;

  /** A list of the types of VR in the register. */
  protected Set controllerTypes;

  /** A list of the types of TOOL in the register. */
  protected Set toolTypes;

  private transient Vector creoleListeners;
  protected void fireResourceLoaded(CreoleEvent e) {
    if (creoleListeners != null) {
      Vector listeners = creoleListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((CreoleListener) listeners.elementAt(i)).resourceLoaded(e);
      }
    }
  }

  protected void fireResourceUnloaded(CreoleEvent e) {
    if (creoleListeners != null) {
      Vector listeners = creoleListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((CreoleListener) listeners.elementAt(i)).resourceUnloaded(e);
      }
    }
  }

  protected void fireResourceRenamed(Resource res, String oldName,
                                     String newName) {
    if (creoleListeners != null) {
      Vector listeners = creoleListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((CreoleListener) listeners.elementAt(i)).resourceRenamed(res,
                                                                  oldName,
                                                                  newName);
      }
    }
  }

  protected void fireDatastoreOpened(CreoleEvent e) {
    if (creoleListeners != null) {
      Vector listeners = creoleListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((CreoleListener) listeners.elementAt(i)).datastoreOpened(e);
      }
    }
  }
  protected void fireDatastoreCreated(CreoleEvent e) {
    if (creoleListeners != null) {
      Vector listeners = creoleListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((CreoleListener) listeners.elementAt(i)).datastoreCreated(e);
      }
    }
  }

  protected void fireDatastoreClosed(CreoleEvent e) {
    if (creoleListeners != null) {
      Vector listeners = creoleListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((CreoleListener) listeners.elementAt(i)).datastoreClosed(e);
      }
    }
  }

  public void resourceLoaded(CreoleEvent e) {
    fireResourceLoaded(e);
  }

  public void resourceUnloaded(CreoleEvent e) {
    fireResourceUnloaded(e);
  }

  public void resourceRenamed(Resource resource, String oldName,
                              String newName){
    fireResourceRenamed(resource, oldName, newName);
  }

  public void datastoreOpened(CreoleEvent e) {
    fireDatastoreOpened(e);
  }

  public void datastoreCreated(CreoleEvent e) {
    fireDatastoreCreated(e);
  }

  public void datastoreClosed(CreoleEvent e) {
    fireDatastoreClosed(e);
  }

  /**The lists of listeners registered with this CreoleRegister*/
} // class CreoleRegisterImpl
