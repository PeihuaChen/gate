/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 25/10/2001
 *
 *  $Id$
 *
 */
package gate.util.persistence;

import gate.persist.PersistenceException;
import gate.creole.ResourceInstantiationException;

import java.io.*;
/**
 * Defines an object that holds persistent data about another object.
 * Storing an arbitrary object will consist of creating an appropiate
 * Persistence object for it and storing that one (via serialisation).
 *
 * Restoring a previously saved object will consist of restoring the persistence
 * object and using the data it stores to create a new object that is as similar
 * as possible to the original object.
 */
public interface Persistence extends Serializable{

  /**
   * Populates this Persistence with the data that needs to be stored from the
   * original source object.
   */
  public void extractDataFromSource(Object source)throws PersistenceException;

  /**
   * Creates a new object from the data contained. This new object is supposed
   * to be a copy for the original object used as source for data extraction.
   */
  public Object createObject()throws PersistenceException,
                                     ResourceInstantiationException;
}