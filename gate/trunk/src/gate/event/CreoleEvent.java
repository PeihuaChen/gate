/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 08/03/2001
 *
 *  $Id$
 */

package gate.event;

import gate.*;
import gate.util.*;

/**
 * Events related to the {@link gate.creole} package. This kind of events will
 * be fired when resources are loaded or unloaded in the Gate system.
 */
public class CreoleEvent extends GateEvent {

  /**
   * Constructor
   * @param res the {@link gate.Resource} that has been (un)loaded
   * @param type the type of the event
   */
  public CreoleEvent(Resource res, int type){
    //the source will always be the Creole register
    super(Gate.getCreoleRegister(), type);
    this.resource = res;
  }

  /**
   * Gets the resource that has been (un)loaded.
   */
  public gate.Resource getResource() {
    return resource;
  }

  /**Event type that mark the loading of a new resource into the Gate system*/
  public static int RESOURCE_LOADED = 1;

  /**Event type that mark the unloading of a resource from the Gate system*/
  public static int RESOURCE_UNLOADED = 2;


  private gate.Resource resource;
}