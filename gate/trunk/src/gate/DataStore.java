/*
 *  DataStore.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 11/Feb/2000
 *
 *  $Id$
 */

package gate;

import java.util.*;
import java.net.*;

import gate.util.*;
import gate.persist.*;
import gate.event.*;

/** Models all sorts of data storage.
  */
public interface DataStore extends FeatureBearer {

  /** Set the URL for the underlying storage mechanism. */
  public void setStorageUrl(URL storageUrl) throws PersistenceException;

  /** Get the URL for the underlying storage mechanism. */
  public URL getStorageUrl();

  /**
   * Create a new data store. <B>NOTE:</B> for some data stores
   * creation is an system administrator task; in such cases this
   * method will throw an UnsupportedOperationException.
   */
  public void create()
  throws PersistenceException, UnsupportedOperationException;

  /** Open a connection to the data store. */
  public void open() throws PersistenceException;

  /** Close the data store. */
  public void close() throws PersistenceException;

  /**
   * Delete the data store. <B>NOTE:</B> for some data stores
   * deletion is an system administrator task; in such cases this
   * method will throw an UnsupportedOperationException.
   */
  public void delete()
  throws PersistenceException, UnsupportedOperationException;

  /**
   * Delete a resource from the data store.
   * @param lrId a data-store specific unique identifier for the resource
   * @param lrClassName class name of the type of resource
   */
  public void delete(String lrClassName, String lrId)
  throws PersistenceException;

  /**
   * Save: synchonise the in-memory image of the LR with the persistent
   * image.
   */
  public void sync(LanguageResource lr) throws PersistenceException;

  /**
   * Set method for the autosaving behaviour of the data store.
   * <B>NOTE:</B> many types of datastore have no auto-save function,
   * in which case this will throw an UnsupportedOperationException.
   */
  public void setAutoSaving(boolean autoSaving)
  throws UnsupportedOperationException;

  /** Get the autosaving behaviour of the LR. */
  public boolean isAutoSaving();

  /** Adopt a resource for persistence. */
  public LanguageResource adopt(LanguageResource lr)
  throws PersistenceException;

  /**
   * Get a resource from the persistent store.
   * <B>Don't use this method - use Factory.createResource with
   * DataStore and DataStoreInstanceId parameters set instead.</B>
   */
  public LanguageResource getLr(String lrClassName, String dataStoreInstanceId)
  throws PersistenceException;

  /** Get a list of the types of LR that are present in the data store. */
  public List getLrTypes() throws PersistenceException;

  /** Get a list of the IDs of LRs of a particular type that are present. */
  public List getLrIds(String lrType) throws PersistenceException;

  /** Get a list of the names of LRs of a particular type that are present. */
  public List getLrNames(String lrType) throws PersistenceException;

  /** Get the name of an LR from its ID. */
  public String getLrName(String lrId) throws PersistenceException;

  /**
   * Registers a new {@link gate.event.DatastoreListener} with this datastore
   */
  public void addDatastoreListener(DatastoreListener l);

  /**
   * Removes a a previously registered {@link gate.event.DatastoreListener}
   * from the list listeners for this datastore
   */
  public void removeDatastoreListener(DatastoreListener l);
} // interface DataStore
