/*
 *  ResourceData.java
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

package gate;

import java.util.*;
import java.net.*;

import gate.util.*;

/** Models an individual CREOLE resource metadata, plus configuration data,
  * plus the instantiations of the resource current within the system.
  * Metadata elements which are used by the CREOLE registration and loading
  * mechanisms are properties of ResourceData implementations and have their
  * own get/set methods. Other metadata elements are made features of the
  * ResourceData. So, for example, if you add an element "FunkyElementThaing"
  * to the metadata of a resource, this will be made a feature of that
  * resource's ResourceData.
  * @see CreoleRegister
  */
public interface ResourceData extends FeatureBearer {
  /** String representation */
  public String toString();

  /** Equality: two resource data objects are the same if they have the
    * same name
    */
  public boolean equals(Object other);

  /** Hashing, based on the name field of the object */
  public int hashCode();

  /** Set method for the resource name */
  public void setName(String name);

  /** Get method for the resource name */
  public String getName();

  /** Set method for the resource class name */
  public void setClassName(String className);

  /** Get method for the resource class name */
  public String getClassName();

  /** Set method for the resource interface name */
  public void setInterfaceName(String className);

  /** Get method for the resource interface name */
  public String getInterfaceName();

  /** Set method for the resource class */
  public void setResourceClass(Class resourceClass);

  /** Get method for the resource class */
  public Class getResourceClass() throws ClassNotFoundException;

  /** Set method for the resource jar file name */
  public void setJarFileName(String jarFileName);

  /** Get method for the resource jar file name */
  public String getJarFileName();

  /** Set method for the resource jar file URL */
  public void setJarFileUrl(URL jarFileUrl);

  /** Get method for the resource jar file URL */
  public URL getJarFileUrl();

  /** Set method for the resource xml file name */
  public void setXmlFileName(String xmlFileName);

  /** Get method for the resource xml file name */
  public String getXmlFileName();

  /** Set method for the resource xml file URL */
  public void setXmlFileUrl(URL xmlFileUrl);

  /** Get method for the resource xml file URL */
  public URL getXmlFileUrl();

  /** Get method for the resource comment */
  public String getComment();

  /** Set method for the resource comment */
  public void setComment(String comment);

  /** Add a parameter list */
  public void addParameterList(List parameterList);

  /** Get the set of parameter lists */
  public Set getParameterListsSet();

  /** Set method for resource autoloading flag */
  public void setAutoLoading(boolean autoLoading);

  /** Is the resource autoloading? */
  public boolean isAutoLoading();

} // ResourceData