/*
 *  ObjectModificationListener.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 21/Sep/2001
 *
 */

package gate.event;


public class ObjectModificationListener implements GateListener {

  public ObjectModificationListener() {
  }

  public void processGateEvent(GateEvent e) {
    /**@todo: Implement this gate.event.GateListener method*/
    throw new java.lang.UnsupportedOperationException("Method processGateEvent() not yet implemented.");
  }

  public void objectCreated(Object o) {
  }

  public void objectModified(Object o) {
  }

  public void objectDeleted(Object o) {
  }


}