/*
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Valentin Tablan 26/10/2001
 *
 *  $Id$
 *
 */
package gate.util.persistence;

import gate.*;
import gate.creole.*;
import gate.util.*;
import gate.persist.PersistenceException;

import java.util.*;

public class DSPersistence implements Persistence{


  /**
   * Populates this Persistence with the data that needs to be stored from the
   * original source object.
   */
  public void extractDataFromSource(Object source)throws PersistenceException{
    //check input
    if(! (source instanceof DataStore)){
      throw new UnsupportedOperationException(
                getClass().getName() + " can only be used for " +
                DataStore.class.getName() +
                " objects!\n" + source.getClass().getName() +
                " is not a " + DataStore.class.getName());
    }

    DataStore ds = (DataStore)source;
    className = ds.getClass().getName();
    storageUrlString = ds.getStorageUrl();
  }

  /**
   * Creates a new object from the data contained. This new object is supposed
   * to be a copy for the original object used as source for data extraction.
   */
  public Object createObject()throws PersistenceException,
                                     ResourceInstantiationException{
    return Factory.openDataStore(className, storageUrlString);
  }

  private String className;
  private String storageUrlString;
  static final long serialVersionUID = 5952924943977701708L;
}
