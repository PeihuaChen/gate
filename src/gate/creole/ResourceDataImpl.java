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
  * @see gate.ResourceData
  */
public class ResourceDataImpl extends AbstractFeatureBearer
implements ResourceData {

  /** Debug flag */
  protected static final boolean DEBUG = false;

  /** Construction */
  public ResourceDataImpl() { }

  /** String representation */
  public String toString() {
    StringBuffer s = new StringBuffer(
      "ResourceDataImpl, name=" + name + "; className=" + className +
      "; jarFileName=" + jarFileName + "; jarFileUrl=" + jarFileUrl +
      "; xmlFileName=" + xmlFileName + "; xmlFileUrl=" + xmlFileUrl +
      "; autoLoading=" + autoLoading + "; interfaceName=" + interfaceName +
      "; parameterListsSet=" + parameterListsSet +
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
  protected String name;

  /** Set method for the resource name */
  public void setName(String name) { this.name = name; }

  /** Get method for the resource name */
  public String getName() { return name; }

  /** The class name of the resource */
  protected String className;

  /** Set method for the resource class name */
  public void setClassName(String className) { this.className = className; }

  /** Get method for the resource class name */
  public String getClassName() { return className; }

  /** The interface name of the resource */
  protected String interfaceName;

  /** Set method for the resource interface name */
  public void setInterfaceName(String interfaceName) {
    this.interfaceName = interfaceName;
  } // setInterfaceName

  /** Get method for the resource interface name */
  public String getInterfaceName() { return interfaceName; }

  /** The class of the resource */
  protected Class resourceClass;

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
  protected String jarFileName;

  /** Set method for the resource jar file name */
  public void setJarFileName(String jarFileName) {
    this.jarFileName = jarFileName;
  } // setJarFileName

  /** Get method for the resource jar file name */
  public String getJarFileName() { return jarFileName; }

  /** The jar file URL of the resource */
  protected URL jarFileUrl;

  /** Set method for the resource jar file URL */
  public void setJarFileUrl(URL jarFileUrl) { this.jarFileUrl = jarFileUrl; }

  /** Get method for the resource jar file URL */
  public URL getJarFileUrl() { return jarFileUrl; }

  /** The xml file name of the resource */
  protected String xmlFileName;

  /** Set method for the resource xml file name */
  public void setXmlFileName(String xmlFileName) {
    this.xmlFileName = xmlFileName;
  } // setXmlFileName

  /** Get method for the resource xml file name */
  public String getXmlFileName() { return xmlFileName; }

  /** The xml file URL of the resource */
  protected URL xmlFileUrl;

  /** Set method for the resource xml file URL */
  public void setXmlFileUrl(URL xmlFileUrl) { this.xmlFileUrl = xmlFileUrl; }

  /** Get method for the resource xml file URL */
  public URL getXmlFileUrl() { return xmlFileUrl; }

  /** The comment string */
  protected String comment;

  /** Get method for the resource comment */
  public String getComment() { return comment; }

  /** Set method for the resource comment */
  public void setComment(String comment) { this.comment = comment; }

  /** The set of parameter lists */
  protected Set parameterListsSet = new HashSet();

  /** Add a parameter list */
  public void addParameterList(List parameterList) {
    parameterListsSet.add(parameterList);
  } // addParameterList

  /** Get the set of parameter lists */
  public Set getParameterListsSet() { return parameterListsSet; }

  /** Autoloading flag */
  protected boolean autoLoading;

  /** Set method for resource autoloading flag */
  public void setAutoLoading(boolean autoLoading) {
    this.autoLoading = autoLoading;
  } // setAutoLoading

  /** Is the resource autoloading? */
  public boolean isAutoLoading() { return autoLoading; }

} // ResourceDataImpl