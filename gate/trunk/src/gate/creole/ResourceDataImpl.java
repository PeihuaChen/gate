/*
	ResourceDataImpl.java 

	Hamish Cunningham, 1/Sept/2000

	$Id$
*/

package gate.creole;

import java.util.*;

import gate.*;
import gate.util.*;

/** Models an individual CREOLE resource metadata, plus configuration data,
  * plus the instantiations of the resource current within the system.
  */
public class ResourceDataImpl implements ResourceData {
  /** Construction */
  public ResourceDataImpl() { }

  /** String representation */
  public String toString() {
    return "ResourceDataImpl, name=" + name + "; className=" + className +
           "; jarFileName=" + jarFileName + "; autoLoading=" + autoLoading;
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

  /** The jar file name of the resource */
  private String jarFileName;

  /** Set method for the resource jar file name */
  public void setJarFileName(String jarFileName) {
    this.jarFileName = jarFileName;
  } // setJarFileName

  /** Get method for the resource jar file name */
  public String getJarFileName() { return jarFileName; }

  /** Autoloading flag */
  private boolean autoLoading;

  /** Set method for resource autoloading flag */
  public void setAutoLoading(boolean autoLoading) {
    this.autoLoading = autoLoading;
  } // setAutoLoading

  /** Is the resource autoloading? */
  public boolean isAutoLoading() { return autoLoading; }

} // ResourceDataImpl
