/*
 *  SerialDataStore.java
 *
 *  Copyright (c) 1998-2001, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Hamish Cunningham, 19/Jan/2001
 *
 *  $Id$
 */

package gate.persist;

import java.util.*;
import java.util.zip.*;
import java.net.*;
import java.io.*;

import gate.*;
import gate.creole.*;
import gate.util.*;
import gate.event.*;
import gate.security.*;

/**
 * A data store based on Java serialisation.
 */
public class SerialDataStore
extends AbstractFeatureBearer implements DataStore {

  /** Debug flag */
  private static final boolean DEBUG = false;

  /**
   * Construction requires a file protocol URL
   * pointing to the storage directory used for
   * the serialised classes. <B>NOTE:</B> should not be called except by
   * GATE code.
   */
  public SerialDataStore(URL storageDirUrl) throws PersistenceException {
    setStorageUrl(storageDirUrl);
  } // construction from URL

  /**
   * Default construction. <B>NOTE:</B> should not be called except by
   * GATE code.
   */
  public SerialDataStore() { };

  /**
   * The directory used for the serialised classes.
   */
  protected File storageDir;

  /** Set method for storage URL */
  public void setStorageDir(File storageDir) { this.storageDir = storageDir; }

  /** Get method for storage URL */
  public File getStorageDir() { return storageDir; }

  /** Set the URL for the underlying storage mechanism. */
  public void setStorageUrl(URL storageUrl) throws PersistenceException {
    if(! storageUrl.getProtocol().equalsIgnoreCase("file"))
      throw new PersistenceException(
        "A serial data store needs a file URL, not " + storageUrl
      );
    this.storageDir = new File(storageUrl.getFile());
  } // setStorageUrl

  /** Get the URL for the underlying storage mechanism. */
  public URL getStorageUrl() {
    if(storageDir == null) return null;

    URL u = null;
    try { u = storageDir.toURL(); } catch(MalformedURLException e) {
      // we can assume that this never happens as storageUrl should always
      // be a valid file and therefore convertable to URL
    }

    return u;
  } // getStorageUrl()

  /** Create a new data store. This tries to create a directory in
    * the local file system. If the directory already exists and is
    * non-empty, or is
    * a file, or cannot be created, PersistenceException is thrown.
    */
  public void create()
  throws PersistenceException {
    if(storageDir == null)
      throw new PersistenceException("null storage directory: cannot create");

    if(! storageDir.exists()) { // if doesn't exist create it
      if(! storageDir.mkdir())
        throw new
          PersistenceException("cannot create directory " + storageDir);
    } else { // must be empty
      String[] existingFiles = storageDir.list();
      if(! (existingFiles == null || existingFiles.length == 0) )
        throw new PersistenceException(
          "directory "+ storageDir +" is not empty: cannot use for data store"
        );
    }

    // dump the version file
    try {
      File versionFile = getVersionFile();
      OutputStreamWriter osw = new OutputStreamWriter(
        new FileOutputStream(versionFile)
      );
      osw.write(versionNumber + Strings.getNl());
      osw.close();
    } catch(IOException e) {
      throw new PersistenceException("couldn't write version file: " + e);
    }
  } // create()

  /** The name of the version file */
  protected static String versionFileName = "__GATE_SerialDataStore__";

  /** The protocol version of the currently open data store */
  protected static String currentProtocolVersion = null;

  /** Get a File for the protocol version file. */
  protected File getVersionFile() throws IOException {
    return new File(storageDir, versionFileName);
  } // getVersionFile

  /**
   * Version number for variations in the storage protocol.
   * Protocol versions:
   * <UL>
   * <LI>
   * 1.0: uncompressed. Originally had no version file - to read a 1.0
   * SerialDataStore that has no version file add a version file containing
   * the line "1.0".
   * <LI>
   * 1.1: has a version file. Uses GZIP compression.
   * </UL>
   * This variable stores the version of the current level of the
   * protocol, NOT the level in use in the currently open data store.
   */
  protected String versionNumber = "1.1";

  /** List of valid protocol version numbers. */
  protected String[] protocolVersionNumbers = {
    "1.0",
    "1.1"
  }; // protocolVersionNumbers

  /** Check a version number for validity. */
  protected boolean isValidProtocolVersion(String versionNumber) {
    if(versionNumber == null)
      return false;

    for(int i = 0; i < protocolVersionNumbers.length; i++)
      if(protocolVersionNumbers[i].equals(versionNumber))
        return true;

    return false;
  } // isValidProtocolVersion

  /** Delete the data store.
    */
  public void delete() throws PersistenceException {
    if(storageDir == null || ! Files.rmdir(storageDir))
      throw new PersistenceException("couldn't delete " + storageDir);

    Gate.getDataStoreRegister().remove(this);
  } // delete()

  /** Delete a resource from the data store.
    */
  public void delete(String lrClassName, Object lrPersistenceId)
  throws PersistenceException {

    // find the subdirectory for resources of this type
    File resourceTypeDirectory = new File(storageDir, lrClassName);
    if(
      (! resourceTypeDirectory.exists()) ||
      (! resourceTypeDirectory.isDirectory())
    ) {
      throw new PersistenceException("Can't find " + resourceTypeDirectory);
    }

    // create a File to representing the resource storage file
    File resourceFile = new File(resourceTypeDirectory, (String)lrPersistenceId);
    if(! resourceFile.exists() || ! resourceFile.isFile())
      throw new PersistenceException("Can't find file " + resourceFile);

    // delete the beast
    if(! resourceFile.delete())
      throw new PersistenceException("Can't delete file " + resourceFile);

    // if there are no more resources of this type, delete the dir too
    if(resourceTypeDirectory.list().length == 0)
      if(! resourceTypeDirectory.delete())
        throw new PersistenceException("Can't delete " + resourceTypeDirectory);

    //let the world know about it
    fireResourceDeleted(
      new DatastoreEvent(
        this, DatastoreEvent.RESOURCE_DELETED, null, (String) lrPersistenceId
      )
    );
  } // delete(lr)

  /** Adopt a resource for persistence. */
  public LanguageResource adopt(LanguageResource lr)
  throws PersistenceException {

    // check the LR's current DS
    DataStore currentDS = lr.getDataStore();
    if(currentDS == this)         // adopted already
      return lr;
    else if(currentDS == null) {  // an orphan - do the adoption
      lr.setDataStore(this);

      // let the world know
      fireResourceAdopted(
          new DatastoreEvent(this, DatastoreEvent.RESOURCE_ADOPTED, lr, null)
      );
      return lr;
    } else {                      // someone else's child
      throw new PersistenceException(
        "Can't adopt a resource which is already in a different datastore"
      );
    }

  } // adopt(LR)

  /** Open a connection to the data store. */
  public void open() throws PersistenceException {
    if(storageDir == null)
      throw new PersistenceException("Can't open: storage dir is null");

    // check storage directory is readable
    if(! storageDir.canRead()) {
      throw new PersistenceException("Can't read " + storageDir);
    }

    // check storage directory is a valid serial datastore
// if we want to support old style:
// String versionInVersionFile = "1.0";
// (but this means it will open *any* directory)
    try {
      FileReader fis = new FileReader(getVersionFile());
      BufferedReader isr = new BufferedReader(fis);
      currentProtocolVersion = isr.readLine();
      if(DEBUG) Out.prln("opening SDS version " + currentProtocolVersion);
      isr.close();
    } catch(IOException e) {
      throw new PersistenceException(
        "Invalid storage directory: " + e
      );
    }
    if(! isValidProtocolVersion(currentProtocolVersion))
      throw new PersistenceException(
        "Invalid protocol version number: " + currentProtocolVersion
      );

  } // open()

  /** Close the data store. */
  public void close() throws PersistenceException {
    Gate.getDataStoreRegister().remove(this);
  } // close()

  /** Save: synchonise the in-memory image of the LR with the persistent
    * image.
    */
  public void sync(LanguageResource lr) throws PersistenceException {
    // check that this LR is one of ours (i.e. has been adopted)
    if(lr.getDataStore() != this)
      throw new PersistenceException(
        "This LR is not stored in this DataStore"
      );

    // find the resource data for this LR
    ResourceData lrData =
      (ResourceData) Gate.getCreoleRegister().get(lr.getClass().getName());

    // create a subdirectory for resources of this type if none exists
    File resourceTypeDirectory = new File(storageDir, lrData.getClassName());
    if(
      (! resourceTypeDirectory.exists()) ||
      (! resourceTypeDirectory.isDirectory())
    ) {
      if(! resourceTypeDirectory.mkdir())
        throw new PersistenceException("Can't write " + resourceTypeDirectory);
    }

    // create an indentifier for this resource
    String lrName = null;
    Object lrPersistenceId = null;
    lrName = lr.getName();
    lrPersistenceId = lr.getLRPersistenceId();
    if(lrName == null)
      lrName = lrData.getName();
    if(lrPersistenceId == null) {
      lrPersistenceId = constructPersistenceId(lrName);
      lr.setLRPersistenceId(lrPersistenceId);
    }

    // create a File to store the resource in
    File resourceFile = new File(resourceTypeDirectory, (String) lrPersistenceId);

    // dump the LR into the new File
    try {
      OutputStream os = new FileOutputStream(resourceFile);

      // after 1.1 the serialised files are compressed
      if(! currentProtocolVersion.equals("1.0"))
        os = new GZIPOutputStream(os);

      ObjectOutputStream oos = new ObjectOutputStream(os);
      oos.writeObject(lr);
      oos.close();
    } catch(IOException e) {
      throw new PersistenceException("Couldn't write to storage file: " + e);
    }

    // let the world know about it
    fireResourceWritten(
      new DatastoreEvent(
        this, DatastoreEvent.RESOURCE_WRITTEN, lr, (String) lrPersistenceId
      )
    );
  } // sync(LR)

  /** Create a persistent store Id from the name of a resource. */
  protected String constructPersistenceId(String lrName) {
    return lrName + "___" + new Date().getTime() + "___" + random();
  } // constructPersistenceId

  /** Get a resource from the persistent store.
    * <B>Don't use this method - use Factory.createResource with
    * DataStore and DataStoreInstanceId parameters set instead.</B>
    * (Sometimes I wish Java had "friend" declarations...)
    */
  public LanguageResource getLr(String lrClassName, Object lrPersistenceId)
  throws PersistenceException {

    // find the subdirectory for resources of this type
    File resourceTypeDirectory = new File(storageDir, lrClassName);
    if(
      (! resourceTypeDirectory.exists()) ||
      (! resourceTypeDirectory.isDirectory())
    ) {
        throw new PersistenceException("Can't find " + resourceTypeDirectory);
    }

    // create a File to representing the resource storage file
    File resourceFile = new File(resourceTypeDirectory, (String)lrPersistenceId);
    if(! resourceFile.exists() || ! resourceFile.isFile())
      throw new PersistenceException("Can't find file " + resourceFile);

    // try and read the file and deserialise it
    LanguageResource lr = null;
    try {
      InputStream is = new FileInputStream(resourceFile);

      // after 1.1 the serialised files are compressed
      if(! currentProtocolVersion.equals("1.0"))
        is = new GZIPInputStream(is);

      ObjectInputStream ois = new ObjectInputStream(is);
      lr = (LanguageResource) ois.readObject();
      ois.close();
    } catch(IOException e) {
      throw
        new PersistenceException("Couldn't read file "+resourceFile+": "+e);
    } catch(ClassNotFoundException ee) {
      throw
        new PersistenceException("Couldn't find class "+lrClassName+": "+ee);
    }

    // set the dataStore property of the LR (which is transient and therefore
    // not serialised)
    lr.setDataStore(this);

    return lr;
  } // getLr(id)

  /** Get a list of the types of LR that are present in the data store. */
  public List getLrTypes() throws PersistenceException {
    if(storageDir == null || ! storageDir.exists())
      throw new PersistenceException("Can't read storage directory");

    // filter out the version file
    String[] fileArray = storageDir.list();
    List lrTypes = new ArrayList();
    for(int i=0; i<fileArray.length; i++)
      if(! fileArray[i].equals(versionFileName))
        lrTypes.add(fileArray[i]);

    return lrTypes;
  } // getLrTypes()

  /** Get a list of the IDs of LRs of a particular type that are present. */
  public List getLrIds(String lrType) throws PersistenceException {
    // a File to represent the directory for this type
    File resourceTypeDir = new File(storageDir, lrType);
    if(! resourceTypeDir.exists())
      return Arrays.asList(new String[0]);

    return Arrays.asList(resourceTypeDir.list());
  } // getLrIds(lrType)

  /** Get a list of the names of LRs of a particular type that are present. */
  public List getLrNames(String lrType) throws PersistenceException {
    // the list of files storing LRs of this type; an array for the names
    String[] lrFileNames = (String[]) getLrIds(lrType).toArray();
    ArrayList lrNames = new ArrayList();

    // for each lr file name, munge its name and add to the lrNames list
    for(int i = 0; i<lrFileNames.length; i++) {
      String name = getLrName(lrFileNames[i]);
      lrNames.add(name);
    }

    return lrNames;
  } // getLrNames(lrType)

  /** Get the name of an LR from its ID. */
  public String getLrName(Object lrId) {
    int secondSeparator = ((String) lrId).lastIndexOf("___");
    lrId = ((String) lrId).substring(0, secondSeparator);
    int firstSeparator = ((String) lrId).lastIndexOf("___");

    return ((String) lrId).substring(0, firstSeparator);
  } // getLrName

  /** Set method for the autosaving behaviour of the data store.
    * <B>NOTE:</B> this type of datastore has no auto-save function,
    * therefore this method throws an UnsupportedOperationException.
    */
  public void setAutoSaving(boolean autoSaving)
  throws UnsupportedOperationException {
    throw new UnsupportedOperationException(
      "SerialDataStore has no auto-save capability"
    );
  } // setAutoSaving

  /** Get the autosaving behaviour of the LR. */
  public boolean isAutoSaving() { return autoSaving; }

  /** Flag for autosaving behaviour. */
  protected boolean autoSaving = false;

  /** Generate a random integer between 0 and 9999 for file naming. */
  protected static int random() {
    return randomiser.nextInt(9999);
  } // random

  /** Random number generator */
  protected static Random randomiser = new Random();
  private transient Vector datastoreListeners;

  /** String representation */
  public String toString() {
    String nl = Strings.getNl();
    StringBuffer s = new StringBuffer("SerialDataStore: ");
    s.append("autoSaving: " + autoSaving);
    s.append("; storageDir: " + storageDir);
    s.append(nl);

    return s.toString();
  } // toString()

  /** Calculate a hash code based on the class and the storage dir. */
  public int hashCode(){
    return getClass().hashCode() ^ storageDir.hashCode();
  } // hashCode

  /** Equality: based on storage dir of other. */
  public boolean equals(Object other) {
    return
      other instanceof SerialDataStore
      &&
      ((SerialDataStore)other).storageDir.equals(storageDir);
  } // equals

  public synchronized void removeDatastoreListener(DatastoreListener l) {
    if (datastoreListeners != null && datastoreListeners.contains(l)) {
      Vector v = (Vector) datastoreListeners.clone();
      v.removeElement(l);
      datastoreListeners = v;
    }
  }
  public synchronized void addDatastoreListener(DatastoreListener l) {
    Vector v = datastoreListeners == null ? new Vector(2) : (Vector) datastoreListeners.clone();
    if (!v.contains(l)) {
      v.addElement(l);
      datastoreListeners = v;
    }
  }
  protected void fireResourceAdopted(DatastoreEvent e) {
    if (datastoreListeners != null) {
      Vector listeners = datastoreListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((DatastoreListener) listeners.elementAt(i)).resourceAdopted(e);
      }
    }
  }
  protected void fireResourceDeleted(DatastoreEvent e) {
    if (datastoreListeners != null) {
      Vector listeners = datastoreListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((DatastoreListener) listeners.elementAt(i)).resourceDeleted(e);
      }
    }
  }
  protected void fireResourceWritten(DatastoreEvent e) {
    if (datastoreListeners != null) {
      Vector listeners = datastoreListeners;
      int count = listeners.size();
      for (int i = 0; i < count; i++) {
        ((DatastoreListener) listeners.elementAt(i)).resourceWritten(e);
      }
    }
  }

  /**
   * Returns the name of the icon to be used when this datastore is displayed
   * in the GUI
   */
  public String getIconName(){
    return "ds.gif";
  }

  /**
   * Returns the comment displayed by the GUI for this DataStore
   */
  public String getComment(){
    return "GATE serial datastore";
  }

  /**
   * Checks if the user (identified by the sessionID)
   *  has read access to the LR
   */
  public boolean canReadLR(Object lrID, Session s)
    throws PersistenceException, gate.security.SecurityException{

    throw new MethodNotImplementedException();
  }
  /**
   * Checks if the user (identified by the sessionID)
   * has write access to the LR
   */
  public boolean canWriteLR(Object lrID, Session s)
    throws PersistenceException, gate.security.SecurityException{

    throw new MethodNotImplementedException();
  }

    /** Sets the name of this resource*/
  public void setName(String name){
    this.name = name;
  }

  /** Returns the name of this resource*/
  public String getName(){
    return name;
  }

  /** The name of the datastore */
  protected String name;
} // class SerialDataStore
