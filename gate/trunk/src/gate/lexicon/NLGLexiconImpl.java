/*
 *  NLGLexiconImpl.java
 *
 *  Copyright (c) 1998-2003, The University of Sheffield.
 *
 *  This file is part of GATE (see http://gate.ac.uk/), and is free
 *  software, licenced under the GNU Library General Public License,
 *  Version 2, June 1991 (in the distribution as file licence.html,
 *  and also available at http://gate.ac.uk/gate/licence.html).
 *
 *  Kalina Bontcheva, 28/January/2003
 *
 *  $Id$

 */

package gate.lexicon;

import gate.creole.AbstractLanguageResource;
import gate.util.*;
import gate.*;
import java.util.*;
import gate.persist.PersistenceException;
import gate.security.SecurityException;
import gate.creole.ResourceInstantiationException;
import java.net.*;
import java.io.*;

public class NLGLexiconImpl extends AbstractLanguageResource
                            implements NLGLexicon {

  private String version = "1.0";
  private List synsets = new ArrayList();
  private HashMap words = new HashMap();

  public NLGLexiconImpl() {
  }

  public Resource init() throws gate.creole.ResourceInstantiationException {
    return this;
  }

  public Iterator getSynsets() {
    return synsets.iterator();
  }

  public Iterator getSynsets(Object pos) {
    List tempList = new ArrayList();
    for (int i=0; i<synsets.size(); i++) {
      LexKBSynset synset = (LexKBSynset) synsets.get(i);
      if (synset.getPOS().equals(pos))
        tempList.add(synset);
    }//for
    return tempList.iterator();
  }

  public List lookupWord(String lemma) {
    Word myWord = (Word) words.get(lemma);
    return myWord.getWordSenses();
  }

  public List lookupWord(String lemma, Object pos) {
    Word myWord = (Word) words.get(lemma);
    List posSenses = new ArrayList();
    Iterator iter = myWord.getWordSenses().iterator();
    while (iter.hasNext()) {
      LexKBWordSense sense = (LexKBWordSense) iter.next();
      if (sense.getPOS().equals(pos))
        posSenses.add(sense);
    } //while loop through senses
    return posSenses;
  }

  /** add a new word */
  public MutableWord addWord(String lemma){
    if (words.containsKey(lemma))
      return (MutableWord) words.get(lemma);

    MutableWordImpl newWord = new MutableWordImpl(lemma);
    words.put(lemma, newWord);
    return newWord;
  }

  /** sets the lexicon version */
  public void setVersion(String newVersion){
    version = newVersion;
  }

  /** returns the lexicon version */
  public String getVersion() {
    return version;
  }

  public MutableLexKBSynset addSynset() {
    MutableLexKBSynset newSynset =  new MutableLexKBSynsetImpl();
    synsets.add(newSynset);
    return newSynset;
  }

}