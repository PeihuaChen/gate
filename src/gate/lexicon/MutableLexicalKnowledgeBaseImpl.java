/*
 *  MutableLexicalKnowledgeBaseImpl.java
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

public class MutableLexicalKnowledgeBaseImpl extends AbstractLanguageResource
                            implements MutableLexicalKnowledgeBase {

  private String version = "1.0";
  protected List synsets = new ArrayList();
  protected HashMap words = new HashMap();
  protected List posTypes = new ArrayList();
  static final long serialVersionUID = -2543190013851016324L;
  private Object lexId = "MIAKT Lexical KB Lexicon";

  public MutableLexicalKnowledgeBaseImpl() {
    for (int i = 0; i < POS_TYPES.length; i++)
      posTypes.add(POS_TYPES[i]);
  }

  public Resource init() throws gate.creole.ResourceInstantiationException {
    return this;
  }

  public Iterator getSynsets() {
    return synsets.iterator();
  }

  public Iterator getSynsets(Object pos) {
    if (pos == null)
      return null;
    List tempList = new ArrayList();
    for (int i=0; i<synsets.size(); i++) {
      LexKBSynset synset = (LexKBSynset) synsets.get(i);
      if (pos.equals(synset.getPOS()))
        tempList.add(synset);
    }//for
    return tempList.iterator();
  }

  public List lookupWord(String lemma) {
    if (lemma == null)
      return null;
    Word myWord = (Word) words.get(lemma);
    if (myWord == null)
      return null;
    return myWord.getWordSenses();
  }

  public List lookupWord(String lemma, Object pos) {
    if (lemma == null || pos == null)
      return null;
    Word myWord = (Word) words.get(lemma);
    if (myWord == null)
      return null;
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

  public Object[] getPOSTypes() {
    return posTypes.toArray();
  }

  public void addPOSType(Object newPOSType) {
    if (newPOSType == null)
      return;
    posTypes.add(newPOSType);
  }

  public void removeWord(MutableWord theWord) {
    if (theWord == null)
      return;
    theWord.removeSenses();
    words.remove(theWord.getLemma());
  }//removeWord

  public void removeSynset(MutableLexKBSynset synset) {
    if (synset == null)
      return;
    List senses = synset.getWordSenses();
    for (int i = 0; i < senses.size(); i++) {
      LexKBWordSense sense = (LexKBWordSense) senses.get(i);
      ((MutableWord) sense.getWord()).removeSense(sense);
    }//for
    synset.removeSenses();
    synsets.remove(synset);
  }//removeSynset

  public Object getLexiconId() {
    return lexId;
  }

  public void setLexiconId(Object Id) {
    lexId = Id;
  }
}