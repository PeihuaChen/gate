/*
 *  CreoleLoader.java
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
 *  Hamish Cunningham, 5/Sept/2000
 *
 *  $Id$
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