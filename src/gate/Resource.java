/*
	Resource.java 

	Hamish Cunningham, 11/Feb/2000

	$Id$
*/

package gate;

import java.util.*;

import gate.util.*;

/** Models all sorts of resources.
  */
public interface Resource extends FeatureBearer
{
  /** Get the factory that created this resource. */
  public Factory getFactory();
   
} // interface Resource
