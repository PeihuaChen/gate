/*
	CreoleLoader.java

	Hamish Cunningham, 5/Sept/2000

	$Id$
*/

package gate;

import java.util.*;
import java.net.*;
import java.io.*;

import gate.util.*;


/** The CREOLE loader is responsible for instantiating resources.
  * <P>
  * The loader is accessible from the static method
  * <A HREF=util/Gate.html#getCreoleLoader()>gate.util.Gate.getCreoleLoader</A>;
  * there is only one per application of the GATE framework.
  * <P>
  *
  * @see gate.util.Gate
  * @see gate.CreoleRegister
  */
public interface CreoleLoader
{
  /** Instantiate a resource based on its resource data. */
  public Resource load(ResourceData resourceData);

} // interface CreoleLoader
