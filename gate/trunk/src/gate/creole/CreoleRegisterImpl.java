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

import gate.*;
import gate.util.*;
import gate.event.*;


/** This class implements the CREOLE register interface. DO NOT
  * construct objects of this class unless your name is gate.util.Gate
  * (in which case please go back to the source code repository and stop
  * looking at other class's code).
  * <P>
  * The CREOLE register records the set of resources that are currently
  * known to the system. Each member of the register is a
  * <A HREF=creole/ResourceData.html>ResourceData</A> object, indexed by
  * the class name of the resource.
  * @see gate.CreoleRegister
  */
public class CreoleRegisterImpl extends HashMap implements CreoleRegister, CreoleListener
{
  /** Debug flag */
  protected static final boolean DEBUG = false;

  /** The set of CREOLE directories (URLs). */
  protected Set directories = new HashSet();

  /** The parser for the CREOLE directory files */
  protected SAXParser parser = null;

  /** Default constructor. Sets up directory files parser. */
  public CreoleRegisterImpl() throws GateException {

    // construct a SAX parser for parsing the CREOLE directory files
    try {
      // Get a parser factory.
      SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

      // Set up the factory to create the appropriate type of parser:
      // non validating one
      saxParserFactory.setValidating(false);
      // non namespace aware one
      saxParserFactory.setNamespaceAware(false);

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
    return directories;
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

    // add the URL (may overwrite an existing one; who cares)
    directories.add(directoryUrl);

    // directory URLs shouldn't include "creole.xml"
    String urlName = directoryUrl.toExternalForm().toLowerCase();
    if(urlName.endsWith("creole.xml")) {
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

    // parse the directory file
    try {
      parseDirectory(directoryXmlFileUrl.openStream(), directoryUrl);
    } catch(IOException e) {
      throw(new GateException("couldn't open creole.xml: " + e.toString()));
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
      HandlerBase handler = new CreoleXmlHandler(this, directoryUrl);
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
        Files.getGateResourceAsStream("creole/creole.xml"),
        Gate.getClassLoader().getResource("gate/resources/creole/")
      );
    } catch(IOException e) {
      if(DEBUG) System.out.println(e);
      throw(new GateException(e));
    }
  } // registerBuiltins()

  /** This is a utility method for creating CREOLE directory files
    * (typically called <CODE>creole.xml</CODE>) from a list of Jar
    * files that contain resources. The method concatenates the
    * <CODE>creole.xml</CODE> files that the Jars contain.
    * <P>
    * If Java allowed class methods in interfaces this would be static.
    */
  public File createCreoleDirectoryFile(File directoryFile, Set jarFileNames)
  {
    ////////////////////
    // dump xml header and comment header and <CREOLE-DIRECTORY> into dirfile
    // for each jar file pick out creole.xml
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
      lrTypes.add(rd.getClassName());
    } else if(ProcessingResource.class.isAssignableFrom(resClass)) {
      if(DEBUG) Out.prln("PR: " + resClass);
      prTypes.add(rd.getClassName());
    } else if(VisualResource.class.isAssignableFrom(resClass)) {
      if(DEBUG) Out.prln("VR: " + resClass);
      vrTypes.add(rd.getClassName());
    }

    // maintain tool types list
    if(rd.isTool())
      toolTypes.add(rd.getClassName());

    return super.put(key, value);
  } // put(key, value)

  /** Overide HashMap's delete method to update the lists of types
    * in the register.
    */
  public Object remove(Object key) {
    ResourceData rd = (ResourceData) get(key);
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
  public Set getLrTypes() { return lrTypes; }

  /** Get the list of types of PR in the register. */
  public Set getPrTypes() { return prTypes; }

  /** Get the list of types of VR in the register. */
  public Set getVrTypes() { return vrTypes; }

  /** Get the list of types of TOOL respurces in the register. */
  public Set getToolTypes() { return toolTypes; }

  /** Get a list of all instantiations of LR in the register. */
  public List getLrInstances() {
    Set lrTypeSet = getLrTypes();
    List instances = new ArrayList();

    Iterator iter = lrTypeSet.iterator();
    while(iter.hasNext()) {
      String type = (String) iter.next();
      instances.addAll(getLrInstances(type));
    }

    return instances;
  } // getLrInstances()

  /** Get a list of all instantiations of PR in the register. */
  public List getPrInstances() {
    Set prTypeSet = getPrTypes();
    List instances = new ArrayList();

    Iterator iter = prTypeSet.iterator();
    while(iter.hasNext()) {
      String type = (String) iter.next();
      instances.addAll(getPrInstances(type));
    }

    return instances;
  } // getPrInstances()

  /** Get a list of all instantiations of VR in the register. */
  public List getVrInstances() {
    Set vrTypeSet = getVrTypes();
    List instances = new ArrayList();

    Iterator iter = vrTypeSet.iterator();
    while(iter.hasNext()) {
      String type = (String) iter.next();
      instances.addAll(getVrInstances(type));
    }

    return instances;
  } // getVrInstances()

  /** Get a list of instantiations of a type of LR in the register. */
  public List getLrInstances(String resourceTypeName) {
    ResourceData resData = (ResourceData) get(resourceTypeName);
    if(resData == null)
      return new ArrayList();

    return resData.getInstantiations();
  } // getLrInstances

  /** Get a list of instantiations of a type of PR in the register. */
  public List getPrInstances(String resourceTypeName) {
    ResourceData resData = (ResourceData) get(resourceTypeName);
    if(resData == null)
      return new ArrayList();

    return resData.getInstantiations();
  } // getPrInstances

  /** Get a list of instantiations of a type of VR in the register. */
  public List getVrInstances(String resourceTypeName) {
    ResourceData resData = (ResourceData) get(resourceTypeName);
    if(resData == null)
      return new ArrayList();

    return resData.getInstantiations();
  } // getVrInstances

  /** Get a list of all non-private instantiations of LR in the register. */
  public List getPublicLrInstances() { return getPublics(getLrInstances()); }

  /** Get a list of all non-private instantiations of PR in the register. */
  public List getPublicPrInstances() { return getPublics(getPrInstances()); }

  /** Get a list of all non-private instantiations of VR in the register. */
  public List getPublicVrInstances() { return getPublics(getVrInstances()); }

  /** Get a list of all non-private types of LR in the register. */
  public List getPublicLrTypes() { return getPublicTypes(getLrTypes()); }

  /** Get a list of all non-private types of PR in the register. */
  public List getPublicPrTypes() { return getPublicTypes(getPrTypes()); }

  /** Get a list of all non-private types of VR in the register. */
  public List getPublicVrTypes() { return getPublicTypes(getVrTypes()); }

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

    return publics;
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
    return publics;
  }
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
  protected Set lrTypes = new HashSet();

  /** A list of the types of PR in the register. */
  protected Set prTypes = new HashSet();

  /** A list of the types of VR in the register. */
  protected Set vrTypes = new HashSet();

  /** A list of the types of TOOL in the register. */
  protected Set toolTypes = new HashSet();

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
