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


/** The CREOLE register records the set of resources that are currently
  * known to the system. Each member of the register is a
  * <A HREF=creole/ResourceData.html>ResourceData</A> object, indexed by
  * the class name or interface name of the resource.
  * <P>
  * The register is accessible from the static method
  * <A HREF=util/Gate.html#getCreoleRegister()>gate.util.Gate.getCreoleRegister
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
  * @see gate.util.Gate
  * @see gate.creole.ResourceData
  */
public interface CreoleRegister extends Map, Serializable
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

} // interface CreoleRegister
