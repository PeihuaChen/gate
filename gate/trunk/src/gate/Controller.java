/*
 *  Controller.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 9/Nov/2000
 *
 *  $Id$
 */

package gate;

import java.util.*;
import java.io.*;

import gate.util.*;
import gate.creole.*;

/** Models the execution of groups of ProcessingResources.
  */
public interface Controller extends Resource, Executable,
                                    NameBearer, FeatureBearer
{
  /**
   * Returns all the {@link gate.ProcessingResource}s contained by this
   * controller.
   * The actual type of collection returned depends on the controller type.
   */
  public Collection getPRs();


  /**
   * Populates this controller from a collection of {@link ProcessingResource}s
   * (optional operation).
   *
   * Controllers that are serializable must implement this method needed by GATE
   * to restore their contents.
   * @throws UnsupportedOperationException if the <tt>setPRs</tt> method
   * 	       is not supported by this controller.
   */
  public void setPRs(Collection PRs);


} // interface Controller
