/*
 *  WordNet.java
 *
 *  Copyright (c) 1998-2002, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Marin Dimitrov, 16/May/2002
 *
 *  $Id$
 */

package gate.wordnet;

import java.util.Iterator;
import java.io.*;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.dictionary.Dictionary;
//import net.didion.jwnl.data;

import junit.framework.*;

import gate.*;
import gate.util.*;
import gate.creole.*;
import gate.persist.PersistenceException;


public class IndexFileWordNetImpl extends AbstractLanguageResource
                                  implements WordNet {


  private Dictionary wnDictionary;
  private File       propertyFile;


  public IndexFileWordNetImpl() {
  }

  /** Initialise this resource, and return it. */
  public Resource init() throws ResourceInstantiationException {

    try {
      InputStream inProps = new FileInputStream(this.propertyFile);

      JWNL.initialize(inProps);
      this.wnDictionary = Dictionary.getInstance();
      Assert.assertNotNull(this.wnDictionary);
    }
    catch(Exception e) {
      throw new ResourceInstantiationException(e);
    }

    return this;
  } // init()


  public void setPropertyFile(File properties) {

    //0.
    Assert.assertNotNull(properties);

    if (null != this.propertyFile) {
      throw new GateRuntimeException("props are alredy set");
    }

    this.propertyFile = properties;
  }


  public String getVersion() {

    JWNL.Version ver = JWNL.getVersion();
    return ver.toString();
  }


  public Iterator getSynsets() {
    throw new MethodNotImplementedException();
  }


  public Iterator getSynsets(int POS) {
    throw new MethodNotImplementedException();
  }

  public Iterator getUniqueBeginners() {
    throw new MethodNotImplementedException();
  }

  /**
   * Sets the parent LR of this LR.
   * Only relevant for LRs that support shadowing. Most do not by default.
   */
  public void setParent(LanguageResource parentLR)
    throws PersistenceException,SecurityException {

    throw new UnsupportedOperationException();
  }

  /**
   * Returns the parent LR of this LR.
   * Only relevant for LRs that support shadowing. Most do not by default.
   */
  public LanguageResource getParent()
    throws PersistenceException,SecurityException{

    throw new UnsupportedOperationException();
  }

  /**
   * Returns true of an LR has been modified since the last sync.
   * Always returns false for transient LRs.
   */
  public boolean isModified() {
    return false;
  }

  /** Save: synchonise the in-memory image of the LR with the persistent
    * image.
    */
  public void sync() throws PersistenceException,SecurityException {
    throw new UnsupportedOperationException();
  }

  /** Sets the persistence id of this LR. To be used only in the
   *  Factory and DataStore code.
   */
  public void setLRPersistenceId(Object lrID){
    throw new UnsupportedOperationException();
  }

   /** Returns the persistence id of this LR, if it has been stored in
   *  a datastore. Null otherwise.
   */
  public Object getLRPersistenceId(){
    throw new UnsupportedOperationException();
  }

  /** Get the data store that this LR lives in. Null for transient LRs. */
  public DataStore getDataStore(){
    throw new UnsupportedOperationException();
  }

   /** Set the data store that this LR lives in. */
  public void setDataStore(DataStore dataStore) throws PersistenceException{
    throw new UnsupportedOperationException();
  }

  class SynsetIterator implements java.util.Iterator {

    private Iterator it;

    public SynsetIterator(Iterator _it) {

      Assert.assertNotNull(_it);
      this.it = _it;
    }

    public boolean hasNext() {
      return this.it.hasNext();
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }

    public Object next() {

      net.didion.jwnl.data.Synset jwnlSynset = (net.didion.jwnl.data.Synset)this.it.next();
      throw new UnsupportedOperationException();
    }
  }
}