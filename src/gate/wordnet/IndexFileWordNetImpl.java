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
import java.util.List;
import java.util.ArrayList;
import java.io.*;

import net.didion.jwnl.*;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.dictionary.Dictionary;
import net.didion.jwnl.data.IndexWordSet;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.Word;
//import net.didion.jwnl.data.POS;

import junit.framework.*;

import gate.*;
import gate.util.*;
import gate.creole.*;
import gate.persist.PersistenceException;


public class IndexFileWordNetImpl extends AbstractLanguageResource
                                  implements WordNet {


  /** JWNL dictionary */
  private Dictionary wnDictionary;
  /** JWNL property file  */
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


  /** helper method */
  public Dictionary getJWNLDictionary() {
    return this.wnDictionary;
  }


  public void setPropertyFile(File properties) {

    //0.
    Assert.assertNotNull(properties);

    if (null != this.propertyFile) {
      throw new GateRuntimeException("props are alredy set");
    }

    this.propertyFile = properties;
  }

  /** returns the WordNet version */
  public String getVersion() {

    JWNL.Version ver = JWNL.getVersion();
    return ver.toString();
  }

  /** returns all synsets for specific POS */
  public Iterator getSynsets(int _pos)
    throws WordNetException {

    net.didion.jwnl.data.POS pos = WNHelper.int2POS(_pos);

    try {
      net.didion.jwnl.data.Synset jwnSynset = null;

      Iterator itSynsets = this.wnDictionary.getSynsetIterator(pos);
      return new SynsetIterator(itSynsets);
    }
    catch(JWNLException jwne) {
      throw new WordNetException(jwne);
    }

  }

  /** returns all unique beginners */
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


  /** returns list of WordSense-s for specific lemma */
  public List lookupWord(String lemma) throws WordNetException {

    try {
      IndexWord[] jwIndexWordArr = this.wnDictionary.lookupAllIndexWords(lemma).getIndexWordArray();
      return _lookupWord(lemma,jwIndexWordArr);
    }
    catch(JWNLException jex) {
      throw new WordNetException(jex);
    }
  }

  /** returns list of WordSense-s for specific lemma of the specified POS */
  public List lookupWord(String lemma, int pos) throws WordNetException {

    try {
      IndexWord jwIndexWord = this.wnDictionary.lookupIndexWord(WNHelper.int2POS(pos), lemma);

      IndexWord[] jwIndexWordArr = new IndexWord[1];
      jwIndexWordArr[0] = jwIndexWord;

      return _lookupWord(lemma,jwIndexWordArr);
    }
    catch(JWNLException jex) {
      throw new WordNetException(jex);
    }
  }

  /** helper method */
  private List _lookupWord(String lemma, IndexWord[] jwIndexWords) throws WordNetException{

    List result = new ArrayList();

    try {
      for (int i=0; i< jwIndexWords.length; i++) {
        IndexWord iw = jwIndexWords[i];
        net.didion.jwnl.data.Synset[] jwSynsetArr = iw.getSenses();

        for (int j=0; j< jwSynsetArr.length; j++) {
          net.didion.jwnl.data.Synset jwSynset = jwSynsetArr[j];
          Synset gateSynset = new SynsetImpl(jwSynset,this.wnDictionary);
          //find the word of interest
          List wordSenses = gateSynset.getWordSenses();

          Iterator itSenses = wordSenses.iterator();
          while (itSenses.hasNext()) {
            WordSense currSynsetMember = (WordSense)itSenses.next();
            if (currSynsetMember.getWord().getLemma().equalsIgnoreCase(lemma)) {
              //found match
              result.add(currSynsetMember);
              break;
            }
          }
        }
      }
    }
    catch(JWNLException jex) {
      throw new WordNetException(jex);
    }

    return result;
  }

  /** iterator for synsets - may load synsets when necessary, not all at once */
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
      Synset gateSynset = new SynsetImpl(jwnlSynset, wnDictionary);
      return gateSynset;
    }
  }
}