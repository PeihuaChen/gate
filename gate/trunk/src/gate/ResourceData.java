/*
	ResourceData.java 

	Hamish Cunningham, 1/Sept/2000

	$Id$
*/

package gate;

import java.util.*;

import gate.util.*;

/** Models an individual CREOLE resource metadata, plus configuration data,
  * plus the instantiations of the resource current within the system.
  */
public interface ResourceData {
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

  /** Set method for the resource jar file name */
  public void setJarFileName(String jarFileName);

  /** Get method for the resource jar file name */
  public String getJarFileName();

  /** Set method for resource autoloading flag */
  public void setAutoLoading(boolean autoLoading);

  /** Is the resource autoloading? */
  public boolean isAutoLoading();

} // ResourceData
