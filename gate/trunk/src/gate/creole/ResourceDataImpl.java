/*
 *  ResourceDataImpl.java
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
 *  Hamish Cunningham, 1/Sept/2000
 *
 *  $Id$
 */

package gate.creole;

import java.util.*;
import java.net.*;

import gate.*;
import gate.util.*;

/** Models an individual CREOLE resource metadata, plus configuration data,
  * plus the instantiations of the resource current within the system.
  */
public class ResourceDataImpl extends AbstractFeatureBearer
implements ResourceData {

  /** Debug flag */
  private static final boolean DEBUG = false;

  /** Construction */
  public ResourceDataImpl() { }

  /** String representation */
  public String toString() {
    StringBuffer s = new StringBuffer(
      "ResourceDataImpl, name=" + name + "; className=" + className +
      "; jarFileName=" + jarFileName + "; autoLoading=" + autoLoading +
      "; features=" + features
    );

    Iterator iter = parameterListsSet.iterator();
    if(iter.hasNext()) s.append(Strings.getNl() + "  params=");
    while(iter.hasNext()) {
      s.append(Strings.getNl() + "    ");
      List paramList = (List) iter.next();
      Iterator iter2 = paramList.iterator();
      while(iter2.hasNext())
        s.append( (Parameter) iter2.next() + Strings.getNl() + "    " );
    }

    return s.toString();
  } // toString

  /** Equality: two resource data objects are the same if they have the
    * same name
    */
  public boolean equals(Object other) {
    if(name.equals(((ResourceDataImpl) other).getName()))
      return true;
    return false;
  } // equals

  /** Hashing, based on the name field of the object */
  public int hashCode() {
    return name.hashCode();
  } // hashCode

  /** The name of the resource */
  private String name;

  /** Set method for the resource name */
  public void setName(String name) { this.name = name; }

  /** Get method for the resource name */
  public String getName() { return name; }

  /** The class name of the resource */
  private String className;

  /** Set method for the resource class name */
  public void setClassName(String className) { this.className = className; }

  /** Get method for the resource class name */
  public String getClassName() { return className; }

  /** The class of the resource */
  private Class resourceClass;

  /** Set method for the resource class */
  public void setResourceClass(Class resourceClass) {
    this.resourceClass = resourceClass;
  } // setResourceClass

  /** Get method for the resource class. Asks the GATE class loader
    * to load it, if it is not already present.
    */
  public Class getResourceClass() throws ClassNotFoundException {
    if(resourceClass == null) {
      GateClassLoader classLoader = Gate.getClassLoader();
      resourceClass = classLoader.loadClass(className);
    }

    return resourceClass;
  } // getResourceClass

  /** The jar file name of the resource */
  private String jarFileName;

  /** Set method for the resource jar file name */
  public void setJarFileName(String jarFileName) {
    this.jarFileName = jarFileName;
  } // setJarFileName

  /** Get method for the resource jar file name */
  public String getJarFileName() { return jarFileName; }

  /** The jar file URL of the resource */
  private URL jarFileUrl;

  /** Set method for the resource jar file URL */
  public void setJarFileUrl(URL jarFileUrl) { this.jarFileUrl = jarFileUrl; }

  /** Get method for the resource jar file URL */
  public URL getJarFileUrl() { return jarFileUrl; }

  /** The set of parameter lists */
  private Set parameterListsSet = new HashSet();

  /** Add a parameter list */
  public void addParameterList(List parameterList) {
    parameterListsSet.add(parameterList);
  } // addParameterList

  /** Get the set of parameter lists */
  public Set getParameterListsSet() { return parameterListsSet; }

  /** Autoloading flag */
  private boolean autoLoading;

  /** Set method for resource autoloading flag */
  public void setAutoLoading(boolean autoLoading) {
    this.autoLoading = autoLoading;
  } // setAutoLoading

  /** Is the resource autoloading? */
  public boolean isAutoLoading() { return autoLoading; }

} // ResourceDataImpl