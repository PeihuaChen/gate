/*
 *  SerialController.java
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

package gate.creole;

import java.util.*;
import java.io.*;

import gate.*;
import gate.util.*;
import gate.creole.*;

/** Execute a list of PRs serially.
  */
public class SerialController
extends AbstractProcessingResource implements Controller
{
  /** The list of resources the controller runs. */
  protected List resourceList = new ArrayList();

  /** Get the list of resources the controller runs. */
  public List getResourceList() { return resourceList; }

  /** Set the list of resources the controller runs. */
  public void setResourceList(List resourceList) {
    this.resourceList = resourceList;
  } // setResourceList

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {
    return this;
  } // init()

  /** Run the Processing Resources in sequence. */
  public void run() {
    Iterator iter = resourceList.iterator();
    while(iter.hasNext()) {
      ProcessingResource pr = (ProcessingResource) iter.next();

      //reg.parameterise(pr, parameterListIdMap.get(pr));
      pr.run();
      try {
        pr.check();
      } catch(ProcessingResourceRuntimeException e) {
        runtimeException = e;
        return;
      }
    } // for each PR in the resourceList

  } // run()

} // class SerialController
