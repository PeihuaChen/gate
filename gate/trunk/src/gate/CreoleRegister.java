/*
 *  CreoleRegister.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 31/Aug/2000
 *
 *  $Id$
 */

package gate;

import java.util.*;
import java.net.*;
import java.io.*;

import gate.util.*;
import gate.event.*;

/** The CREOLE register records the set of resources that are currently
  * known to the system. Each member of the register is a
  * <A HREF=creole/ResourceData.html>ResourceData</A> object, indexed by
  * the class name of the resource.
  * <P>
  * The register is accessible from the static method
  * <A HREF=Gate.html#getCreoleRegister()>gate.Gate.getCreoleRegister
  * </A>;
  * there is only one per application of the GATE framework.
  * <P>
  * Clients use the register by adding URLs (using the
  * <A HREF=#addDirectory(java.net.URL)>addDirectory</A> method)
  * pointing to CREOLE directories. A <B>CREOLE directory</B> is a URL at
  * which resides a file called <CODE>creole.xml</CODE> describing
  * the resources present, and one or more Jar files implementing
  * those resources. E.g., the CREOLE resources at
  * <A HREF=http://gate.ac.uk/>gate.ac.uk</A> are registered by Gate.init()
  * by registering the directory URL
  * <A HREF=http://gate.ac.uk/creole/>http://gate.ac.uk/creole/</A>, under
  * which lives a file called creole.xml.
  * <P>
  * To register resources clients use the <CODE>registerDirectories</CODE>
  * methods. When resources have been registered they can be accessed via
  * their <CODE>ResourceData</CODE> objects. So a typical use of the register
  * is to: add the set of URLs containing CREOLE directories; register
  * all resources found at those URLs; browse the set of registered
  * resources.
  * <P>
  * In all cases, where a resource or a directory is added which is
  * already present in the register, the new silently overwrites the old.
  *
  * The CreoleRegister notifies all registered listeners of all
  * {@link gate.event.CreoleEvent}s that occur in the system regardless of
  * whether they were initially fired by the {@link Factory}, the
  * {@link DataStoreRegister} or the {@link CreoleRegister} itself.
  *
  * @see gate.Gate
  * @see gate.creole.ResourceData
  */
public interface CreoleRegister extends Map, Serializable, CreoleListener
{
  /** Add a CREOLE directory URL. The directory is <B>not</B> registered. */
  public void addDirectory(URL directoryUrl);

  /** Get the list of CREOLE directory URLs. */
  public Set getDirectories();

  /** Register all the CREOLE directories that we know of.
    * The <CODE>creole.xml</CODE> files
    * at the URLs are parsed, and <CODE>CreoleData</CODE> objects added
    * to the register.
    */
  public void registerDirectories() throws GateException;

  /** Register a single CREOLE directory. The <CODE>creole.xml</CODE>
    * file at the URL is parsed, and <CODE>CreoleData</CODE> objects added
    * to the register. If the directory URL has not yet been added it
    * is now added.
    */
  public void registerDirectories(URL directoryUrl) throws GateException;

  /** Register resources that are built in to the GATE distribution.
    * These resources are described by the <TT>creole.xml</TT> file in
    * <TT>resources/creole</TT>.
    */
  public void registerBuiltins() throws GateException;

  /** This is a utility method for creating CREOLE directory files
    * (typically called <CODE>creole.xml</CODE>) from a list of Jar
    * files that contain resources. The method concatenates the
    * <CODE>resource.xml</CODE> files that the Jars contain.
    * <P>
    * If Java allowed class methods in interfaces this would be static.
    */
  public File createCreoleDirectoryFile(File directoryFile, Set jarFileNames);

  /** Get the list of types of LR in the register. */
  public Set getLrTypes();

  /** Get the list of types of PR in the register. */
  public Set getPrTypes();

  /** Get the list of types of VR in the register. */
  public Set getVrTypes();

  /** Get the list of types of VR in the register. */
  public Set getControllerTypes();

  /** Get a list of all instantiations of LR in the register. */
  public List getLrInstances();

  /** Get a list of all instantiations of PR in the register. */
  public List getPrInstances();

  /** Get a list of all instantiations of VR in the register. */
  public List getVrInstances();

  /** Get a list of instantiations of a type of LR in the register. */
  public List getLrInstances(String resourceTypeName);

  /** Get a list of instantiations of a type of PR in the register. */
  public List getPrInstances(String resourceTypeName);

  /** Get a list of instantiations of a type of VR in the register. */
  public List getVrInstances(String resourceTypeName);

  /** Get a list of all non-private instantiations of LR in the register. */
  public List getPublicLrInstances();

  /** Get a list of all non-private instantiations of PR in the register. */
  public List getPublicPrInstances();

  /** Get a list of all non-private instantiations of VR in the register. */
  public List getPublicVrInstances();

  /** Get a list of all non-private types of LR in the register. */
  public List getPublicLrTypes();

  /** Get a list of all non-private types of PR in the register. */
  public List getPublicPrTypes();

  /** Get a list of all non-private types of VR in the register. */
  public List getPublicVrTypes();

  /** Get a list of all non-private types of Controller in the register. */
  public List getPublicControllerTypes();

  /**
   * Gets all the instantiations of a given type and all its derivate types;
   * It doesn't return instances that have the hidden attribute set to "true"
   */
  public List getAllInstances(String type) throws GateException;

  /**
   * Returns a list of strings representing class names for large VRs valid
   * for a given type of language/processing resource.
   * The default VR will be the first in the returned list.
   */
  public List getLargeVRsForResource(String resourceClassName);

  /**
   * Returns a list of strings representing class names for small VRs valid
   * for a given type of language/processing resource
   * The default VR will be the first in the returned list.
   */
  public List getSmallVRsForResource(String resourceClassName);

  /**
    * Returns a list of strings representing class names for annotation VRs
    * that are able to display/edit all types of annotations.
    * The default VR will be the first in the returned list.
    */
   public List getAnnotationVRs();

  /**
    * Returns a list of strings representing class names for annotation VRs
    * that are able to display/edit a given annotation type
    * The default VR will be the first in the returned list.
    */
   public List getAnnotationVRs(String annotationType);


  /**
    * Returns a list of strings representing annotations types for which
    * there are custom viewers/editor registered.
    */
   public List getVREnabledAnnotationTypes();

   /**
    * Renames an existing resource.
    */
   public void setResourceName(Resource res, String newName);

  /**
   * Registers a {@link gate.event.CreoleListener}with this CreoleRegister.
   * The register will fire events every time a resource is added to or removed
   * from the system and when a datastore is created, opened or closed.
   */
  public void addCreoleListener(CreoleListener l);

  /**
   * Removes a {@link gate.event.CreoleListener} previously registered with this
   * CreoleRegister. {@see #addCreoleListener()}
   */
  public void removeCreoleListener(CreoleListener l);

} // interface CreoleRegister
